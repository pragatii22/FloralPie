package com.example.floral.repo 

import android.net.Uri
import com.example.floral.model.ProductModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProductRepoImpl : ProductRepo {
    private val database by lazy { FirebaseDatabase.getInstance() }
    private val ref by lazy { database.getReference("products") }
    private val storage by lazy { FirebaseStorage.getInstance() }

    override fun uploadImage(imageUri: Uri, callback: (Boolean, String) -> Unit) {
        val fileName = "products/${System.currentTimeMillis()}.jpg"
        val storageRef = storage.getReference(fileName)

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { url ->
                    callback(true, url.toString())
                }
            }
            .addOnFailureListener {
                callback(false, it.message ?: "Upload failed")
            }
    }

    override fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        val id = ref.push().key.toString()
        model.productId = id

        ref.child(id).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product added successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(model.productId)
            .updateChildren(model.toMap()).addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Product updated successfully")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun deleteProduct(
        id: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(id).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product deleted successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getProductById(
        id: String,
        callback: (Boolean, ProductModel?) -> Unit
    ) {
        ref.child(id).addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val product = snapshot.getValue(ProductModel::class.java)
                        callback(true, product)
                    } else {
                        callback(true, null)
                    }
                }
                override fun onCancelled(p0: DatabaseError) {
                    callback(false, null)
                }
            }
        )
    }

    override fun getAllProduct(callback: (Boolean, List<ProductModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val allProducts = mutableListOf<ProductModel>()
                if(snapshot.exists()){
                    for(data in snapshot.children){
                        val product = data.getValue(ProductModel::class.java)
                        product?.let {
                            allProducts.add(it)
                        }
                    }
                }
                callback(true, allProducts)
            }

            override fun onCancelled(p0: DatabaseError) {
                callback(false, null)
            }
        })
    }

    override fun filterProduct(
        isActive: Boolean,
        callback: (Boolean, List<ProductModel>?) -> Unit
    ) {
        ref.orderByChild("isActive").equalTo(isActive).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<ProductModel>()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val product = data.getValue(ProductModel::class.java)
                        product?.let { products.add(it) }
                    }
                }
                callback(true, products)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, null)
            }
        })
    }

    override fun searchProduct(
        name: String,
        callback: (Boolean, List<ProductModel>?) -> Unit
    ) {
        ref.orderByChild("productName").startAt(name).endAt(name + "\uf8ff")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = mutableListOf<ProductModel>()
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val product = data.getValue(ProductModel::class.java)
                            product?.let { products.add(it) }
                        }
                    }
                    callback(true, products)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, null)
                }
            })
    }

    override fun getProductByCategory(
        categoryID: String,
        callback: (Boolean, List<ProductModel>?) -> Unit
    ) {
        ref.orderByChild("categoryId").equalTo(categoryID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = mutableListOf<ProductModel>()
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val product = data.getValue(ProductModel::class.java)
                            product?.let { products.add(it) }
                        }
                    }
                    callback(true, products)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, null)
                }
            })
    }
}
