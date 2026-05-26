package com.example.floral

import android.R.attr.path
import androidx.compose.runtime.mutableStateOf
import com.example.floral.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepoImpl : UserRepo{
     val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()

//    val ref =  database.getReference(path="users")
    val ref  = database.getReference("users")

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true, "Login success")
            } else{
                callback(false,"${it.exception?.message}")
            }
            }
    }

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true, "Registration success", "${auth.currentUser?.uid}")
            } else{
                callback(false,"${it.exception?.message}", "")
            }
            }
    }

    override fun editProfile(
        id: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(id).updateChildren(model.toMap()).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "User updated successfully")

            }else {
                callback(false, "${it.exception?.message}")
            }
        }
    }
//create C rud
    override fun addUser(
        id: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
//to auto generate id
//        val id = ref.push().key.toString()

        ref.child(id).setValue(model).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "User added successfully")

            }else{
                callback(false,"${it.exception?.message}")

            }
        }

    }

    override fun deleteUsr(
        id: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(id).removeValue().addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "User deleted  successfully")

            }else{
                callback(false,"${it.exception?.message}")

            }
        }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "Reset Link sent to $email")
            } else{
                callback(false,"${it.exception?.message}")
            }
        }
    }

    override fun getUserId(
        id: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
            ref.child(id).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val user = snapshot.getValue(UserModel::class.java)
                        user.let {
                            callback(true,"User fetched successfully",it)


                        }
                    }
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                   callback(false,"${error.message}",null)
                }

            })
            }

    override fun getAllUser(callback: (Boolean, String, List<UserModel?>) -> Unit) {
        ref.addValueEventListener(object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val allUsers = mutableListOf<UserModel>()
                    for(user in snapshot.children){
                        val data = user.getValue(UserModel::class.java)
                        if(data != null){
                            allUsers.add(data)
                        }
                    }
                    callback(true,"fetched",allUsers)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false,error.message,emptyList())
            }
        })
    }


    override fun logout(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true, "Logout Successful")
        } catch (e: Exception) {
            callback(false, "Logout Failed: e.toString()")
        }


    }
}