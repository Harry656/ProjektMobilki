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
import com.example.projektmobilki.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progresDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Proszę czekać")
        progresDialog.setCanceledOnTouchOutside(false)

        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.loginBtn.setOnClickListener{
            validateData()
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.forgotTv.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Podaj poprawny email", Toast.LENGTH_SHORT).show()
        }else if(password.isEmpty()){
            Toast.makeText(this, "Podaj hasło", Toast.LENGTH_SHORT).show()
        }else{
            loginUser()
        }
    }

    private fun loginUser() {
        progresDialog.setMessage("Trwa logowanie")
        progresDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                FirebaseService.sharedPref =
                    getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
                    if (!task.isSuccessful) {
                        //Log.w(TAG, "Exception while registering FCM token with Braze.", task.exception)
                        return@addOnCompleteListener
                    }
                    val currentToken = task.result
                    //binding.et3.setText(currentToken)
                    val firebaseUser = firebaseAuth.currentUser!!
                    val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/") .getReference("Users")
                    ref.child(firebaseUser.uid).child("deviceToken").setValue(currentToken)
                        .addOnSuccessListener {
                            checkUser()
                        }
                        .addOnFailureListener { e->
                            Toast.makeText(this, "Nie udało się zalogowac ${e.message}", Toast.LENGTH_SHORT).show()
                        }
//                    checkUser()
//                } .addOnFailureListener { e->
//                    Toast.makeText(this, "Nie udało się zalogowac ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//                }
//                val firebaseUser = firebaseAuth.currentUser!!
//                val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
//                ref.child(firebaseUser.uid).child("deviceToken").setValue()
//                checkUser()
//            } .addOnFailureListener { e->
//                Toast.makeText(this, "Nie udało się zalogowac ${e.message}", Toast.LENGTH_SHORT).show()
//            }
                }
            }
    }

    private fun checkUser() {
        progresDialog.setMessage("Sprawdzanie")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progresDialog.dismiss()
                    val userType = snapshot.child("userType").value
                    if(userType == "user"){
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()
                    }else if(userType == "admin"){
                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                        finish()
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}