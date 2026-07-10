package com.example.floral.model

data class OrderModel(
    var orderId: String = "",
    var userId: String = "",
    var items: List<CartModel> = emptyList(),
    var totalAmount: Double = 0.0,
    var orderDate: Long = System.currentTimeMillis(),
    var status: String = "Pending"
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "orderId" to orderId,
            "userId" to userId,
            "items" to items.map { it.toMap() },
            "totalAmount" to totalAmount,
            "orderDate" to orderDate,
            "status" to status
        )
    }
}
