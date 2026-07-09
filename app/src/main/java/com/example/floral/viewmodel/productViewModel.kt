package com.example.floral.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.floral.model.ProductModel
import com.example.floral.repo.ProductRepo

class ProductViewModel(val repo: ProductRepo): ViewModel ( ) {

    fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addProduct(model, callback)
    }

    fun updateProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateProduct(model, callback)
    }

    fun deleteProduct(
        id: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteProduct(id, callback)
    }

    private val _products = MutableLiveData<ProductModel?>()
    val products: MutableLiveData<ProductModel?> get() = _products

    private val _allProducts = MutableLiveData<List<ProductModel>?>()
    val allProducts: MutableLiveData<List<ProductModel>?> get() = _allProducts

    private val _filterProducts = MutableLiveData<List<ProductModel>?>()
    val filterProducts: MutableLiveData<List<ProductModel>?> get() = _filterProducts

    private val _searchProducts = MutableLiveData<List<ProductModel>?>()
    val searchProducts: MutableLiveData<List<ProductModel>?> get() = _searchProducts


    private val _categoryProducts = MutableLiveData<List<ProductModel>?>()
    val productByCategory: MutableLiveData<List<ProductModel>?> get() = _categoryProducts

    //    loading
    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    fun getProductById(
        id: String,
    ) {
        repo.getProductById(id) { success, data ->
            if (success) {
                _products.value = data
                _loading.value = false
            } else {
                _products.value = null
                _loading.value = false
            }
        }
    }

    fun getAllProduct() {
        _loading.value = true
        repo.getAllProduct { success, data ->
            if (success) {
                _allProducts.value = data
                _loading.value = false
            } else {
                _allProducts.value = emptyList()
                _loading.value = false
            }
        }


    }

    fun filterProduct(isActive: Boolean, ) {
        _loading.value = true
        repo.filterProduct(isActive) { success, data ->
            if (success) {
                _filterProducts.value = data
                _loading.value = false
            } else {
                _filterProducts.value = emptyList()
                _loading.value = false
            }
        }

    }

    fun searchProduct(name: String, ) {
        _loading.value = true
        repo.searchProduct(name) { success, data ->
            if (success) {
                _searchProducts.value = data
                _loading.value = false
            } else {
                _searchProducts.value = emptyList()
                _loading.value = false
            }
        }

    }

    fun getProductByCategory(categoryID: String, ) {
        _loading.value = true
        repo.getProductByCategory(categoryID) { success, data ->
            if (success) {
                _categoryProducts.value = data
                _loading.value = false
            } else {
                _categoryProducts.value = emptyList()
                _loading.value = false
            }
        }
    }
}
