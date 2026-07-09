package com.example.floral.model 

data class ProductModel(
    var productId: String=" ",
    var productName: String=" ",
    var price: Double=0.0,
    var description: String=" ",
    var isActive: Boolean= false,
    var quantity: Int = 0,
    var categoryId: String = "",
    var imageUrl: String = ""
){
    fun toMap(): Map<String,Any?>{
        return mapOf(
            "productName" to productName,
            "price" to price,
            "description" to description,
            "isActive" to isActive,
            "quantity" to quantity,
            "categoryId" to categoryId,
            "imageUrl" to imageUrl
        )
    }
}