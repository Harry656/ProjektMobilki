package com.example.projektmobilki.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.projektmobilki.adapters.AdapterTask
import com.example.projektmobilki.databinding.ActivityListOfTasksBinding
import com.example.projektmobilki.models.ModelTask
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class ListOfTasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListOfTasksBinding

    private var categoryId = ""
    private var categoryName = ""

    private lateinit var taskArrayList: ArrayList<ModelTask>

    private lateinit var adapterTask: AdapterTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListOfTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        categoryName = intent.getStringExtra("categoryName")!!

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.subTitleTv.text = categoryName

        loadTaskList()

        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    adapterTask.filter!!.filter(s)
                }catch (e: Exception){

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun loadTaskList() {
        taskArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tasks")
        ref.orderByChild("categoryUID").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    taskArrayList.clear()
                    for(ds in snapshot.children){
                        val model = ds.getValue(ModelTask::class.java)
                        if (model != null) {
                            taskArrayList.add(model)
                        }
                    }
                    adapterTask = AdapterTask(this@ListOfTasksActivity, taskArrayList)
                    binding.tasksRv.adapter = adapterTask
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}