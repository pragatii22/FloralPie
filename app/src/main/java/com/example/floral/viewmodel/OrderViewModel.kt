package com.example.floral.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.floral.model.OrderModel
import com.example.floral.repo.OrderRepo

class OrderViewModel(val repo: OrderRepo) : ViewModel() {
    private val _orders = MutableLiveData<List<OrderModel>?>()
    val orders: MutableLiveData<List<OrderModel>?> get() = _orders

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    fun placeOrder(orderModel: OrderModel, callback: (Boolean, String) -> Unit) {
        repo.placeOrder(orderModel, callback)
    }

    fun getAllOrders() {
        _loading.value = true
        repo.getAllOrders { success, data ->
            if (success) {
                _orders.value = data
            }
            _loading.value = false
        }
    }

    fun getOrdersByUser(userId: String) {
        _loading.value = true
        repo.getOrdersByUser(userId) { success, data ->
            if (success) {
                _orders.value = data
            }
            _loading.value = false
        }
    }

    fun updateOrderStatus(orderId: String, status: String, callback: (Boolean, String) -> Unit) {
        repo.updateOrderStatus(orderId, status, callback)
    }
}
