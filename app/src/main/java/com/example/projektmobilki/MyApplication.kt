package com.example.projektmobilki

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyApplication: Application() {

    override fun onCreate(){
        super.onCreate()
    }

    companion object{
        fun loadCategory(categoryUid: String, subTitleTv:TextView){
            val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
            ref.child(categoryUid)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val name = "${snapshot.child("name").value}"
                        subTitleTv.text = name
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
        fun deleteTask(context: Context, taskUid: String, taskTitle: String){
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Proszę czekać")
            progressDialog.setMessage("Deleting $taskTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tasks")
            ref.child(taskUid)
                .removeValue()
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(context, "Usunięto zadanie", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e->
                    progressDialog.dismiss()
                    Toast.makeText(context, "Nie udało się usunąć zadania ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

//        fun setTaskDone(context: Context, taskUid: String) {
//
//        }
//
//        fun setTaskDoing(context: Context, taskUid: String) {
//            val uid = FirebaseAuth.getInstance().uid
//            val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
//            ref.child(uid!!).get().addOnSuccessListener {
//                val name = it.child("name").value
//
//            }.addOnFailureListener {
//
//            }
//        }
    }
}