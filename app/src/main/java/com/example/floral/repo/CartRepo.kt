package com.example.floral.repo

import com.example.floral.model.CartModel

interface CartRepo {
    fun addToCart(cartModel: CartModel, callback: (Boolean, String) -> Unit)
    fun getCartItems(userId: String, callback: (Boolean, List<CartModel>?) -> Unit)
    fun removeFromCart(cartId: String, callback: (Boolean, String) -> Unit)
    fun clearCart(userId: String, callback: (Boolean, String) -> Unit)
    fun updateCartQuantity(cartId: String, newQuantity: Int, callback: (Boolean, String) -> Unit)
}
