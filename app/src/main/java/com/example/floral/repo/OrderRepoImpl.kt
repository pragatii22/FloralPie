package com.example.floral.repo

import com.example.floral.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrderRepoImpl : OrderRepo {
    private val database = FirebaseDatabase.getInstance()
    private val orderRef = database.getReference("orders")

    override fun placeOrder(orderModel: OrderModel, callback: (Boolean, String) -> Unit) {
        val id = orderRef.push().key ?: ""
        orderModel.orderId = id
        orderRef.child(id).setValue(orderModel).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Order placed successfully")
            } else {
                callback(false, it.exception?.message ?: "Failed to place order")
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
}
