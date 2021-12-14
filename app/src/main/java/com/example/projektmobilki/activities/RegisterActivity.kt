package com.example.projektmobilki.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.projektmobilki.FirebaseService
import com.example.projektmobilki.databinding.ActivityRegisterBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRegisterBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progresDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Proszę czekać")
        progresDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.registerBtn.setOnClickListener {
            validateData()
        }

    }

    private var name = ""
    private var email = ""
    private var password = ""
    //private var deviceToken = ""

    private fun validateData() {
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()
        
        if(name.isEmpty()){
            Toast.makeText(this, "Podaj nazwę użytkownika", Toast.LENGTH_SHORT).show()
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Podaj poprawny email", Toast.LENGTH_SHORT).show()
        }else if(password.isEmpty()){
            Toast.makeText(this, "Podaj hasło", Toast.LENGTH_SHORT).show()
        }else if(cPassword.isEmpty()){
            Toast.makeText(this, "Potwierdź hasło", Toast.LENGTH_SHORT).show()
        }else if(password != cPassword){
            Toast.makeText(this, "Hasła nie są takie same", Toast.LENGTH_SHORT).show()
        }else{
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        progresDialog.setMessage("Trwa tworzenie konta")
        progresDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                updateUserInfo()
            } .addOnFailureListener{ e->
                progresDialog.dismiss()
                Toast.makeText(this, "Nie udało się utworzyć konta ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        progresDialog.setMessage("Zapisywanie danych użytkownika")
//        val timestamp = System.currentTimeMillis()
//        val uid = firebaseAuth.uid
//        val hashMap: HashMap<String, Any?> = HashMap()
        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if(!task.isSuccessful){
                //Log.w(TAG, "Exception while registering FCM token with Braze.", task.exception)
                return@addOnCompleteListener
            }
            val deviceToken = task.result!!
            val timestamp = System.currentTimeMillis()
            val uid = firebaseAuth.uid
            val hashMap: HashMap<String, Any?> = HashMap()
            //val currentToken = task.result
            //deviceToken = currentToken!!
            //binding.et3.setText(currentToken)
            hashMap["uid"] = uid
            hashMap["email"] = email
            hashMap["name"] = name
            hashMap["userType"] = "user"
            hashMap["room"] = ""
            hashMap["timestamp"] = timestamp
            hashMap["deviceToken"] = deviceToken
            val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
            ref.child(uid!!)
                .setValue(hashMap)
                .addOnSuccessListener {
                    progresDialog.dismiss()
                    Toast.makeText(this, "Utworzono konto", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                    finish()
                } .addOnFailureListener{ e->
                    progresDialog.dismiss()
                    Toast.makeText(this, "Nie udało się utworzyć konta ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
//        hashMap["uid"] = uid
//        hashMap["email"] = email
//        hashMap["name"] = name
//        hashMap["userType"] = "user"
//        hashMap["room"] = ""
//        hashMap["timestamp"] = timestamp
//        hashMap["deviceToken"] = deviceToken
//        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
//        ref.child(uid!!)
//            .setValue(hashMap)
//            .addOnSuccessListener {
//                progresDialog.dismiss()
//                Toast.makeText(this, "Utworzono konto", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
//                finish()
//            } .addOnFailureListener{ e->
//                progresDialog.dismiss()
//                Toast.makeText(this, "Nie udało się utworzyć konta ${e.message}", Toast.LENGTH_SHORT).show()
//            }
    }
}