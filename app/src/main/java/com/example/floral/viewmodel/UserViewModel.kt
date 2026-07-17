package com.example.floral.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.floral.model.UserModel
import com.example.floral.repo.UserRepo

class UserViewModel(val repo: UserRepo): ViewModel() {

    fun uploadImage(context: Context, imageUri: Uri, callback: (Boolean, String) -> Unit) {
        repo.uploadImage(context, imageUri, callback)
    }

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        repo.login(email, password) { success, message ->
            _loading.value = false
            callback(success, message)
        }
    }

    fun register(
        email: String, password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        _loading.value = true
        repo.register(email, password) { success, message, uid ->
            if (!success) _loading.value = false
            callback(success, message, uid)
        }
    }

    fun editProfile(
        id: String, model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.editProfile(id, model, callback)
    }


    fun addUser(
        id: String, model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addUser(id, model) { success, message ->
            _loading.value = false
            callback(success, message)
        }
    }

    fun deleteUsr(
        id: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteUsr(id, callback)
    }

    fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.forgetPassword(email, callback)
    }

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    private val _users = MutableLiveData<UserModel?>()
    val users: MutableLiveData<UserModel?> get() = _users


    fun getUserId(
        id: String
    ) {
        _loading.value = true
        repo.getUserId(id) { success, msg, data ->
            if (success) {
                _users.value = data
                _loading.value = false
            } else {
                _users.value = null
                _loading.value = false
            }
        }
    }

    private val _allUsers = MutableLiveData<List<UserModel?>>()
    val allUsers: MutableLiveData<List<UserModel?>> get() = _allUsers


    fun getAllUser() {
        repo.getAllUser { success, message, data ->
            if (success) {
                _loading.value = false
                _allUsers.value = data
            } else {
                _loading.value = false
                _allUsers.value = emptyList()
            }
        }
    }


    fun logout(callback: (Boolean, String) -> Unit) {
        repo.logout(callback);
    }
}
