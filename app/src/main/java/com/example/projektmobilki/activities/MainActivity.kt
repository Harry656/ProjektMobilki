package com.example.projektmobilki.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.projektmobilki.FirebaseService
import com.example.projektmobilki.NotificationData
import com.example.projektmobilki.PushNotification
import com.example.projektmobilki.RetrofitInstance
import com.example.projektmobilki.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TOPIC = "/topics/myTopic"

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    private lateinit var binding:ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        checkUser()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

//        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
//            if(!task.isSuccessful){
//                Log.w(TAG, "Exception while registering FCM token with Braze.", task.exception)
//                return@addOnCompleteListener
//            }
//            val currentToken = task.result
//            binding.et3.setText(currentToken)
//        }
//        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        checkUser()
        binding.loginBtn.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.registerBtn.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
//        binding.subBtn.setOnClickListener{
//            val title = binding.et1.text.toString()
//            val message = binding.et2.text.toString()
//            val currentTokenEt = binding.et3.text.toString()
//            if (title.isNotEmpty() && message.isNotEmpty() && currentTokenEt.isNotEmpty()){
//                PushNotification(
//                    NotificationData(title, message),
//                    currentTokenEt
//                ).also {
//                    sendNotification(it)
//                }
//            }
//        }
    }

//    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
//        try {
//            val response = RetrofitInstance.api.postNotification(notification)
//            if(response.isSuccessful){
//                Log.d(TAG, "Response: ${Gson().toJson(response)}")
//            }else{
//                Log.e(TAG, response.errorBody().toString())
//            }
//        }catch (e:Exception){
//            Log.e(TAG, e.toString())
//        }
//    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser==null){
            binding.loginBtn.setOnClickListener{
                startActivity(Intent(this, LoginActivity::class.java))
            }
            binding.registerBtn.setOnClickListener{
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }else{
            val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val userType = snapshot.child("userType").value
                        if(userType == "user"){
                            startActivity(Intent(this@MainActivity, DashboardUserActivity::class.java))
                            finish()
                        }else if(userType == "admin"){
                            startActivity(Intent(this@MainActivity, DashboardAdminActivity::class.java))
                            finish()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
    }
}