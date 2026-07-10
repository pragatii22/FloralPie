package com.example.floral.repo

import com.example.floral.model.CartModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartRepoImpl : CartRepo {
    private val database = FirebaseDatabase.getInstance()
    private val cartRef = database.getReference("cart")

    override fun addToCart(cartModel: CartModel, callback: (Boolean, String) -> Unit) {
        val id = cartRef.push().key ?: ""
        cartModel.cartId = id
        cartRef.child(id).setValue(cartModel).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Added to cart")
            } else {
                callback(false, it.exception?.message ?: "Failed to add to cart")
            }
        }
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
