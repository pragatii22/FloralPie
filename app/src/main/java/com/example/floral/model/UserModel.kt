package com.example.floral.model

data class UserModel (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val address: String = "",
    val contact: String = "",
    val role: String = "user",
    val imageUrl: String = ""
){
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "name" to name,
            "email" to email,
            "address" to address,
            "contact" to contact,
            "role" to role,
            "imageUrl" to imageUrl
        )
    }
}
