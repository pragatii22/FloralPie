package com.example.floral.repo

import com.example.floral.model.OrderModel

interface OrderRepo {
    fun placeOrder(orderModel: OrderModel, callback: (Boolean, String) -> Unit)
    fun getAllOrders(callback: (Boolean, List<OrderModel>?) -> Unit)
    fun getOrdersByUser(userId: String, callback: (Boolean, List<OrderModel>?) -> Unit)
    fun updateOrderStatus(orderId: String, status: String, callback: (Boolean, String) -> Unit)
}
