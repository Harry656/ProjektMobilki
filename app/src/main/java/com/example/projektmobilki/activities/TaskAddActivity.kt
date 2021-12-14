package com.example.projektmobilki.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.projektmobilki.databinding.ActivityTaskAddBinding
import com.example.projektmobilki.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TaskAddActivity : AppCompatActivity() {

    private lateinit var binding:ActivityTaskAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var database: DatabaseReference

    private lateinit var  progressDialog: ProgressDialog

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Proszę czekać")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        name = binding.nameEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        if(name.isEmpty()){
            Toast.makeText(this, "Podaj nazwę", Toast.LENGTH_SHORT).show()
        }else if(category.isEmpty()){
            Toast.makeText(this, "Wybierz kategorię", Toast.LENGTH_SHORT).show()
        }else{
            uploadTask()
        }
    }

    private fun randomID(): String = List(20) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")

    private fun uploadTask() {
        progressDialog.setMessage("Dodawanie")
        progressDialog.show()

        val random = randomID()

        val hashMap: HashMap<String, Any> = HashMap()
        val timestamp = System.currentTimeMillis()
        hashMap["uid"] = random
        hashMap["title"] = "$name"
        hashMap["description"] = "$description"
        hashMap["categoryUID"] = "$selectedCategoryUid"
        hashMap["isDoing"] = ""
        hashMap["isDone"] = ""
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tasks")
        ref.child(random)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                val ref2 = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
                val timestamp = System.currentTimeMillis()
                ref2.child("$selectedCategoryUid").child("timestamp").setValue(timestamp)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Dodano zadanie", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {

                    }
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Nie udało się dodać zadania", Toast.LENGTH_SHORT).show()

            }
    }

    private fun loadCategories() {
        val uid = firebaseAuth.uid
        database = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        database.child(uid!!).get().addOnSuccessListener {
            val roomName = it.child("room").value
            categoryArrayList = ArrayList()
            val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
            ref.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoryArrayList.clear()
                    for(ds in snapshot.children){
                        val model = ds.getValue(ModelCategory::class.java)
                        if (roomName == model!!.room) {
                            categoryArrayList.add(model!!)
                        }else{

                        }
                    }

                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
            .addOnFailureListener {

            }
    }

    private var selectedCategoryUid = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog(){
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].name
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Wybierz Listę")
            .setItems(categoriesArray){dialog, which->
                selectedCategoryTitle = categoryArrayList[which].name
                selectedCategoryUid = categoryArrayList[which].uid

                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

}