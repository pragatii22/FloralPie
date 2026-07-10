package com.example.floral.model

data class CartModel(
    var cartId: String = "",
    var productId: String = "",
    var userId: String = "",
    var productName: String = "",
    var price: Double = 0.0,
    var quantity: Int = 1,
    var imageUrl: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "cartId" to cartId,
            "productId" to productId,
            "userId" to userId,
            "productName" to productName,
            "price" to price,
            "quantity" to quantity,
            "imageUrl" to imageUrl
        )
    }
}
