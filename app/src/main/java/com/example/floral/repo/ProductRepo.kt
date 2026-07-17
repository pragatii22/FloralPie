package com.example.floral.repo

import android.content.Context
import android.net.Uri
import com.example.floral.model.ProductModel

interface ProductRepo {
    fun uploadImage(context: Context, imageUri: Uri, callback: (Boolean, String) -> Unit)
    fun addProduct(model: ProductModel, callback: (Boolean, String) -> Unit)
    fun updateProduct(model: ProductModel, callback: (Boolean, String) -> Unit)
    fun deleteProduct(id: String, callback: (Boolean, String) -> Unit)
    fun getProductById(id: String, callback: (Boolean, ProductModel?) -> Unit)
    fun getAllProduct(callback: (Boolean, List<ProductModel>?) -> Unit)
    fun filterProduct(isActive: Boolean, callback: (Boolean, List<ProductModel>?) -> Unit)
    fun searchProduct(name: String, callback: (Boolean, List<ProductModel>?) -> Unit)
    fun getProductByCategory(categoryID: String, callback: (Boolean, List<ProductModel>?) -> Unit)
}
