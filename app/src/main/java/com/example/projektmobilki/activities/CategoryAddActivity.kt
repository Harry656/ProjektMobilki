package com.example.projektmobilki.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.projektmobilki.NotificationData
import com.example.projektmobilki.PushNotification
import com.example.projektmobilki.RetrofitInstance
import com.example.projektmobilki.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Proszę czekać")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var category = ""

    private fun randomID(): String = List(14) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")

    private fun validateData() {
        category = binding.categoryEt.text.toString().trim()
        if(category.isEmpty()){
            Toast.makeText(this, "Podaj nazwę...", Toast.LENGTH_SHORT).show()
        }else{
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        progressDialog.show()
        val uid = firebaseAuth.uid
        val reference = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        reference.child(uid!!).get().addOnSuccessListener {
            val roomName = it.child("room").value
            val random = randomID()
            val timestamp = System.currentTimeMillis()
            val hashMap = HashMap<String, Any>()
            hashMap["uid"]= random
            hashMap["name"] = category
            hashMap["room"] = roomName.toString()
            hashMap["timestamp"] = timestamp

            val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
            ref.child(random)
                .setValue(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Dodano kategorie", Toast.LENGTH_SHORT).show()
                    val user = firebaseAuth.currentUser!!
                    val uid = user.uid
                    val database = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
                    database.child(uid!!).get().addOnSuccessListener {
                        val roomName = it.child("room").value
                        val reference = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
                        reference.orderByChild("room").equalTo(roomName.toString())
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(ds in snapshot.children){
                                        val uid = "${ds.child("uid").value}"
                                        val deviceToken = "${ds.child("deviceToken").value}"
                                        if (deviceToken.isNotEmpty()){
                                            PushNotification(
                                                NotificationData("Dodano nową kategorię", "Do twojego pokoju została dodana nowa kategoria. Kliknij aby sprawdzić wprowadzone zmiany!"),
                                                deviceToken
                                            ).also {
                                                sendNotification(it)
                                            }
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                        startActivity(Intent(this@CategoryAddActivity, DashboardAdminActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener { e->
                    Toast.makeText(this, "Nie udało się dodać kategorii ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
            .addOnFailureListener {

            }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful){
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            }else{
                Log.e(TAG, response.errorBody().toString())
            }
        }catch (e:Exception){
            Log.e(TAG, e.toString())
        }
    }
}