package com.example.projektmobilki.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.projektmobilki.databinding.ActivityTaskEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TaskEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskEditBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private var taskUID = ""

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryNameArrayList: ArrayList<String>

    private lateinit var categoryUidArrayList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        taskUID = intent.getStringExtra("taskUID")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Proszę czekać")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadTaskInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.categoryTv.setOnClickListener {
            categoryShow()
        }
        binding.submitBtn.setOnClickListener {
            validateData()
        }
        binding.homeBtn.setOnClickListener {
            startActivity(Intent(this, DashboardAdminActivity::class.java))
        }
    }

    private fun loadTaskInfo() {
        val ref =
            FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Tasks")
        ref.child(taskUID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryUID").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    binding.nameEt.setText(title)
                    binding.descriptionEt.setText(description)

                    val refTaskCategory =
                        FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/")
                            .getReference("Categories")
                    refTaskCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val category = snapshot.child("name").value
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var name = ""
    private var descripton = ""

    private fun validateData() {
        name = binding.nameEt.text.toString().trim()
        descripton = binding.descriptionEt.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Podaj nazwę", Toast.LENGTH_SHORT).show()
        } else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Wybierz kategorię", Toast.LENGTH_SHORT).show()
        } else {
            updateTask()
        }
    }

    private fun updateTask() {
        progressDialog.setMessage("Aktualizowanie zadania")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$name"
        hashMap["description"] = "$descripton"
        hashMap["categoryUID"] = "$selectedCategoryId"

        val ref =
            FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Tasks")
        ref.child(taskUID)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Zaktualizowano zadania", Toast.LENGTH_SHORT).show()
                val refCat = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
                val timestamp = System.currentTimeMillis()
                refCat.child("$selectedCategoryId").child("timestamp").setValue(timestamp)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {

                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Nie udało się zaktualizwać zadania ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryName = ""

    private fun categoryShow() {
        val categoriesArray = arrayOfNulls<String>(categoryNameArrayList.size)
        for (i in categoryNameArrayList.indices) {
            categoriesArray[i] = categoryNameArrayList[i]
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Wybierz listę")
            .setItems(categoriesArray) { dialog, position ->
                selectedCategoryId = categoryUidArrayList[position]
                selectedCategoryName = categoryNameArrayList[position]

                binding.categoryTv.text = selectedCategoryName
            }
            .show()
    }

    private fun loadCategories() {
        categoryNameArrayList = ArrayList()
        categoryUidArrayList = ArrayList()

        val uid = firebaseAuth.uid
        val database =
            FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users")
        database.child(uid!!).get().addOnSuccessListener {
            val roomName = it.child("room").value
            val ref =
                FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("Categories")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoryUidArrayList.clear()
                    categoryNameArrayList.clear()
                    for (ds in snapshot.children) {
                        val uid = "${ds.child("uid").value}"
                        val name = "${ds.child("name").value}"
                        val room = "${ds.child("room").value}"
                        if (roomName == room) {
                            categoryUidArrayList.add(uid)
                            categoryNameArrayList.add(name)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

//        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
//        ref.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                categoryUidArrayList.clear()
//                categoryNameArrayList.clear()
//                for(ds in snapshot.children){
//                    val uid = "${ds.child("uid").value}"
//                    val name = "${ds.child("name").value}"
//
//                    categoryUidArrayList.add(uid)
//                    categoryNameArrayList.add(name)
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
        }
    }
}