package com.example.projektmobilki.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.projektmobilki.adapters.AdapterArchival
import com.example.projektmobilki.databinding.ActivityDashboardArchivalBinding
import com.example.projektmobilki.models.ModelArchival
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Exception

class DashboardArchivalActivity : AppCompatActivity() {

    private lateinit var binding:ActivityDashboardArchivalBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var database: DatabaseReference

    private lateinit var archivalArrayList: ArrayList<ModelArchival>

    private lateinit var adapterArchival: AdapterArchival

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardArchivalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        firebaseAuth = FirebaseAuth.getInstance()

        loadArchival()

        binding.searchEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterArchival.filter.filter(s)
                }catch (e: Exception){

                }
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun loadArchival() {
        val uid = firebaseAuth.uid
        database = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        database.child(uid!!).get().addOnSuccessListener {
            val roomName = it.child("room").value
            archivalArrayList = ArrayList()
            val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Archives")
            ref.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    archivalArrayList.clear()
                    for(ds in snapshot.children){
                        val model = ds.getValue(ModelArchival::class.java)
                        if (roomName == model!!.room) {
                            archivalArrayList.add(model!!)
                        }else{

                        }
                    }
                    adapterArchival = AdapterArchival(this@DashboardArchivalActivity, archivalArrayList)
                    binding.archivalRv.adapter = adapterArchival
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
            .addOnFailureListener {

            }
    }
}