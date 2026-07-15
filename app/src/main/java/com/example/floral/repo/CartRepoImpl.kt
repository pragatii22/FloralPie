package com.example.floral.repo

import com.example.floral.model.CartModel
import com.example.floral.model.ProductModel
import com.google.firebase.database.*

class CartRepoImpl : CartRepo {
    private val database = FirebaseDatabase.getInstance()
    private val cartRef = database.getReference("cart")
    private val productRef = database.getReference("products")

    override fun addToCart(cartModel: CartModel, callback: (Boolean, String) -> Unit) {
        // First check product stock
        productRef.child(cartModel.productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(productSnapshot: DataSnapshot) {
                val product = productSnapshot.getValue(ProductModel::class.java)
                if (product == null) {
                    callback(false, "Product not found")
                    return
                }

                if (product.quantity <= 0) {
                    callback(false, "Out of Stock")
                    return
                }

                cartRef.orderByChild("userId").equalTo(cartModel.userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var existingCartId: String? = null
                            var currentQtyInCart = 0
                            
                            for (child in snapshot.children) {
                                val item = child.getValue(CartModel::class.java)
                                if (item?.productId == cartModel.productId) {
                                    existingCartId = child.key
                                    currentQtyInCart = item.quantity
                                    break
                                }
                            }

                            val requestedTotalQty = currentQtyInCart + cartModel.quantity
                            if (requestedTotalQty > product.quantity) {
                                callback(false, "Only ${product.quantity} items available")
                                return
                            }

                            if (existingCartId != null) {
                                cartRef.child(existingCartId).child("quantity")
                                    .setValue(requestedTotalQty)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            callback(true, "Cart updated")
                                        } else {
                                            callback(false, task.exception?.message ?: "Failed to update cart")
                                        }
                                    }
                            } else {
                                val id = cartRef.push().key ?: ""
                                cartModel.cartId = id
                                cartRef.child(id).setValue(cartModel).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        callback(true, "Added to cart")
                                    } else {
                                        callback(false, task.exception?.message ?: "Failed to add to cart")
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            callback(false, error.message)
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun getCartItems(userId: String, callback: (Boolean, List<CartModel>?) -> Unit) {
        cartRef.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CartModel>()
                for (data in snapshot.children) {
                    val model = data.getValue(CartModel::class.java)
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

    override fun removeFromCart(cartId: String, callback: (Boolean, String) -> Unit) {
        cartRef.child(cartId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Removed from cart")
            } else {
                callback(false, it.exception?.message ?: "Failed to remove")
            }
        }
    }

    override fun clearCart(userId: String, callback: (Boolean, String) -> Unit) {
        cartRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    data.ref.removeValue()
                }
                callback(true, "Cart cleared")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Failed to clear cart")
            }
        })
    }

    override fun updateCartQuantity(cartId: String, newQuantity: Int, callback: (Boolean, String) -> Unit) {
        cartRef.child(cartId).child("quantity").setValue(newQuantity).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Quantity updated")
            } else {
                callback(false, it.exception?.message ?: "Failed to update quantity")
            }
        }
    }
}
