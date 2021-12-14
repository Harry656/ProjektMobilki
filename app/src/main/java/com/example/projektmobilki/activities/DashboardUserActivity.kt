package com.example.projektmobilki.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.projektmobilki.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        binding.createRoomBtn.setOnClickListener {
            checkData()
        }

        binding.joinRoomBtn.setOnClickListener {
            validateData()
        }
    }

    private fun randomID(): String = List(10) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")

    private var roomName=""

    private fun checkData() {
        roomName=binding.nameEt.text.toString().trim()
        if(roomName.isEmpty()){
            Toast.makeText(this, "Podaj nazwę pokoju", Toast.LENGTH_SHORT).show()
        }else{
            createRoom()
        }
    }

    private fun createRoom() {
        val hashMap: HashMap<String, Any> = HashMap()
        val random = randomID()
        hashMap["uid"]= random
        hashMap["name"] = "$roomName"
        hashMap["creator"] = firebaseAuth.uid!!

        val reference = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Rooms")
        reference.child(random.toString())
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Utworzono pokój", Toast.LENGTH_SHORT).show()
                val hashMap2: HashMap<String, Any> = HashMap()
                val timestamp = System.currentTimeMillis()
                hashMap2["room"]= random
                hashMap2["userType"] = "admin"
                val reference = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
                reference.child(firebaseAuth.uid!!)
                    .updateChildren(hashMap2)
                    .addOnSuccessListener {
                        //progressDialog.dismiss()
                        Toast.makeText(this, "Dołączono do pokoju", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@DashboardUserActivity, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e->
                        //progressDialog.dismiss()
                        Toast.makeText(this, "Nie udało się zupdatować ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                //startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                //finish()
            } .addOnFailureListener{ e->
                Toast.makeText(this, "Nie udało się utworzyć konta ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var room=""
    private fun validateData() {
        room=binding.name2Et.text.toString().trim()
        if(room.isEmpty()){
            Toast.makeText(this, "Podaj kod pokoju", Toast.LENGTH_SHORT).show()
        }else{
            updateProfile()
        }
    }

    private fun updateProfile() {
        //progressDialog.setMessage("Aktualizowanie profilu")
        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["room"] = "$room"
        hashmap["userType"] = "admin"

        val reference = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                //progressDialog.dismiss()
                Toast.makeText(this, "Dołączono do pokoju", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@DashboardUserActivity, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                //progressDialog.dismiss()
                Toast.makeText(this, "Nie udało się zupdatować ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }else{
            val email = firebaseUser.email
            binding.subTitleTv.text = email
        }
    }
}