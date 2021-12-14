package com.example.projektmobilki.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.projektmobilki.adapters.AdapterCategory
import com.example.projektmobilki.databinding.ActivityDashboardAdminBinding
import com.example.projektmobilki.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Exception

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding:ActivityDashboardAdminBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var database: DatabaseReference
    private lateinit var database2: DatabaseReference

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        readRoomValues(firebaseAuth.uid)

        loadCategories()

        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterCategory.filter.filter(s)
                }catch (e:Exception){

                }
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.burgerBtn.setOnClickListener {
            val options = arrayOf("Zrobione Listy", "Wyloguj", "Opuść pokój", "Usuń pokój", "Usuń konto")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Opcje")
                .setItems(options){dialog, position->
                    if(position==0){//archiwalne listy
                        startActivity(Intent(this, DashboardArchivalActivity::class.java))
                    }else if(position==1){//wyloguj
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Wylogowanie")
                            .setMessage("Czy na pewno chcesz się wylogować?")
                            .setPositiveButton("Tak"){a, d->
                                Toast.makeText(this, "Wylogowywanie", Toast.LENGTH_SHORT).show()
                                firebaseAuth.signOut()
                                checkUser()
                            }
                            .setNegativeButton("Nie"){a, d->
                                a.dismiss()
                            }
                            .show()
                    }else if(position==2) {//opuść pokój
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Opuszczanie pokoju")
                            .setMessage("Czy na pewno chcesz opuścić pokój?")
                            .setPositiveButton("Tak"){a, d->
                                Toast.makeText(this, "Wylogowywanie", Toast.LENGTH_SHORT).show()
                                leaveRoom()
                            }
                            .setNegativeButton("Nie"){a, d->
                                a.dismiss()
                            }
                            .show()
                    }else if(position==3){//usuwanie pokoju
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Usuwanie pokoju")
                            .setMessage("Czy na pewno chcesz usunąć pokój?")
                            .setPositiveButton("Tak"){a, d->
                                Toast.makeText(this, "Usuwanie pokoju", Toast.LENGTH_SHORT).show()
                                roomDelete()
                            }
                            .setNegativeButton("Nie"){a, d->
                                a.dismiss()
                            }
                            .show()
                    }else if(position==4){//usuwanie konta
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Usuwanie konta")
                            .setMessage("Czy na pewno chcesz usunąć konto?")
                            .setPositiveButton("Tak"){a, d->
                                Toast.makeText(this, "Usuwanie konta", Toast.LENGTH_SHORT).show()
                                accountDelete()
                            }
                            .setNegativeButton("Nie"){a, d->
                                a.dismiss()
                            }
                            .show()
                    }
                }
                .show()
        }

        binding.categotyBtn.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        binding.addTaskBtn.setOnClickListener {
            startActivity(Intent(this, TaskAddActivity::class.java))
        }

    }

    private fun accountDelete() {
        val user = firebaseAuth.currentUser!!
        val uid = user.uid
        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
                    ref.child(uid)
                        .removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Konto zostało usunięte", Toast.LENGTH_SHORT).show()
                            checkUser()
                        }
                        .addOnFailureListener { e->
                            Toast.makeText(this, "Nie można usunąć ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }else{
                    Toast.makeText(this, "Nie można usunąć konta", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun leaveRoom() {
        val user = firebaseAuth.currentUser!!
        val uid = user.uid
        val hashMap = HashMap<String,Any>()
        hashMap["room"]=""
        hashMap["userType"]="user"
        database = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        database.child(uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                firebaseAuth.signOut()
                checkUser()
            }
            .addOnFailureListener {

            }
    }

    private fun roomDelete() {
        val user = firebaseAuth.currentUser!!
        val uid = user.uid
        database = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        database.child(uid!!).get().addOnSuccessListener {
            val roomName = it.child("room").value
            val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Rooms")
            ref.child(roomName.toString())
                .removeValue()
                .addOnSuccessListener {
                    val hashMap = HashMap<String,Any>()
                    hashMap["room"]=""
                    hashMap["userType"]="user"
                    val reference = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
                    reference.orderByChild("room").equalTo(roomName.toString())
                        .addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for(ds in snapshot.children){
                                    val uid = "${ds.child("uid").value}"
                                    //val room = "${ds.child("room").value}"
                                    //val userType = "${ds.child("userType").value}"
                                    val update = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
                                    update.child(uid)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener {
                                            firebaseAuth.signOut()
                                            checkUser()
                                        }
                                        .addOnFailureListener {

                                        }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

//                    reference.child("room").updateChildren(hashMap)
//                        .addOnSuccessListener {
//                            Toast.makeText(this, "Zaktualizowano", Toast.LENGTH_SHORT).show()
//                        }
//                        .addOnFailureListener { e ->
//                            Toast.makeText(this, "Nie udało się zaktualizwać zadania ${e.message}", Toast.LENGTH_SHORT).show()
//                        }
//
                       }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Nie można usunąć pokoju", Toast.LENGTH_SHORT).show()
                }
        }

    private fun readRoomValues(uid: String?) {
        database = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        database.child(uid!!).get().addOnSuccessListener {
            val roomName = it.child("room").value
            binding.subTitleTv3.text = roomName.toString()
        }
            .addOnFailureListener {

            }
    }

    private fun loadCategories() {
        val uid = firebaseAuth.uid
        database = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        database.child(uid!!).get().addOnSuccessListener {
            val roomName = it.child("room").value
            categoryArrayList = ArrayList()
            val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
            ref.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoryArrayList.clear()
                    for(ds in snapshot.children){
                        val model = ds.getValue(ModelCategory::class.java)
                        if (roomName == model!!.room) {
                            categoryArrayList.add(model!!)
                        }else{

                        }
                    }
                    adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)
                    binding.categoriesRv.adapter = adapterCategory
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
            .addOnFailureListener {

            }
//        categoryArrayList = ArrayList()
//        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
//        ref.addValueEventListener(object: ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                categoryArrayList.clear()
//                for(ds in snapshot.children){
//                    val model = ds.getValue(ModelCategory::class.java)
//                    categoryArrayList.add(model!!)
//                }
//                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)
//                binding.categoriesRv.adapter = adapterCategory
//            }
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
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