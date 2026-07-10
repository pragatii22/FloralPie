package com.example.floral.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.floral.model.CartModel
import com.example.floral.repo.CartRepo

class CartViewModel(val repo: CartRepo) : ViewModel() {
    private val _cartItems = MutableLiveData<List<CartModel>?>()
    val cartItems: MutableLiveData<List<CartModel>?> get() = _cartItems

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    fun addToCart(cartModel: CartModel, callback: (Boolean, String) -> Unit) {
        repo.addToCart(cartModel, callback)
    }

    fun getCartItems(userId: String) {
        _loading.value = true
        repo.getCartItems(userId) { success, data ->
            if (success) {
                _cartItems.value = data
            }
            _loading.value = false
        }
    }

    fun removeFromCart(cartId: String, callback: (Boolean, String) -> Unit) {
        repo.removeFromCart(cartId, callback)
    }

    fun clearCart(userId: String, callback: (Boolean, String) -> Unit) {
        repo.clearCart(userId, callback)
    }

    fun updateCartQuantity(cartId: String, newQuantity: Int, callback: (Boolean, String) -> Unit) {
        repo.updateCartQuantity(cartId, newQuantity, callback)
    }
}
