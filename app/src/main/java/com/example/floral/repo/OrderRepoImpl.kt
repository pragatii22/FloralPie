package com.example.floral.repo

import com.example.floral.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class OrderRepoImpl : OrderRepo {
    private val database = FirebaseDatabase.getInstance()
    private val orderRef = database.getReference("orders")
    private val productRef = database.getReference("products")

    override fun placeOrder(orderModel: OrderModel, callback: (Boolean, String) -> Unit) {
        val id = orderRef.push().key ?: ""
        orderModel.orderId = id
        
        // 1. First, try to place the order
        orderRef.child(id).setValue(orderModel).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 2. If order placed, reduce stock for each item
                var itemsProcessed = 0
                val totalItems = orderModel.items.size
                var someFailed = false

                for (item in orderModel.items) {
                    val pRef = productRef.child(item.productId).child("quantity")
                    pRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val currentQty = currentData.getValue(Int::class.java) ?: 0
                            if (currentQty >= item.quantity) {
                                currentData.value = currentQty - item.quantity
                                return Transaction.success(currentData)
                            } else {
                                return Transaction.abort()
                            }
                        }

                        override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                            itemsProcessed++
                            if (!committed) someFailed = true
                            
                            if (itemsProcessed == totalItems) {
                                if (someFailed) {
                                    callback(true, "Order placed, but some items had stock issues")
                                } else {
                                    callback(true, "Order placed successfully")
                                }
                            }
                        }
                    })
                }
            } else {
                callback(false, task.exception?.message ?: "Failed to place order")
            }
        }
    }

    override fun getAllOrders(callback: (Boolean, List<OrderModel>?) -> Unit) {
        orderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<OrderModel>()
                for (data in snapshot.children) {
                    val model = data.getValue(OrderModel::class.java)
                    if (model != null) {
                        list.add(model)
                    }
                }
                callback(true, list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, null)
            }
        })
    }

    override fun getOrdersByUser(userId: String, callback: (Boolean, List<OrderModel>?) -> Unit) {
        orderRef.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<OrderModel>()
                for (data in snapshot.children) {
                    val model = data.getValue(OrderModel::class.java)
                    if (model != null) {
                        list.add(model)
                    }
                }
                callback(true, list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, null)
            }
        })
    }

    override fun updateOrderStatus(orderId: String, status: String, callback: (Boolean, String) -> Unit) {
        orderRef.child(orderId).child("status").setValue(status).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Status updated to $status")
            } else {
                callback(false, it.exception?.message ?: "Failed to update status")
            }
        }
    }
}
