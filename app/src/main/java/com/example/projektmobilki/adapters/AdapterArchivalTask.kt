package com.example.projektmobilki.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmobilki.MyApplication
import com.example.projektmobilki.activities.TaskEditActivity
import com.example.projektmobilki.databinding.RowArchivalTasksBinding
import com.example.projektmobilki.filters.FilterArchivalTask
import com.example.projektmobilki.models.ModelArchivalTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdapterArchivalTask: RecyclerView.Adapter<AdapterArchivalTask.HolderArchivalTask>, Filterable {

    private var context: Context

    public var taskArrayList: ArrayList<ModelArchivalTask>
    private var filterList: ArrayList<ModelArchivalTask>

    private lateinit var binding: RowArchivalTasksBinding

    private var filter: FilterArchivalTask? = null

    constructor(context: Context, taskArrayList: ArrayList<ModelArchivalTask>):super(){
        this.context = context
        this.taskArrayList = taskArrayList
        this.filterList = taskArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderArchivalTask {
        binding = RowArchivalTasksBinding.inflate(LayoutInflater.from(context),parent, false)
        return HolderArchivalTask(binding.root)
    }

    private var name = ""

    override fun onBindViewHolder(holder: HolderArchivalTask, position: Int) {
        val model = taskArrayList[position]
        val taskUid = model.uid
        val categoryUid = model.categoryUID
        val title = model.title
        val description = model.description
        val isDoing = model.isDoing
        val isDone = model.isDone
        val timestamp = model.timestamp

        val date = getDate(timestamp)

        holder.titleTv.text = title
        holder.title2Tv.text = description
        holder.title7Tv.text = date

        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tasks")
        ref.child(taskUid).get().addOnSuccessListener {
            name = it.child("isDoing").value.toString()
            if(name.isNotEmpty()){
                holder.title4Tv.text = name
                //holder.cardView.setCardBackgroundColor(Color.rgb(153, 187, 255))
            }else{
                holder.title4Tv.text = ""
            }
        }

        val ref2 = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tasks")
        ref2.child(taskUid).get().addOnSuccessListener {
            val done = it.child("isDone").value
            if(done=="done") {
                holder.title3Tv.text = "Zadanie wykonane przez: "
                //holder.cardView.setCardBackgroundColor(Color.rgb(153, 255, 153))
            }else{
                holder.title3Tv.text = "Zadanie wykonuje: "
            }
        }

        if(name.isEmpty() && doing.isEmpty()){
            holder.cardView.setCardBackgroundColor(Color.rgb(255, 255, 255))
        }

        MyApplication.loadCategory(categoryUid, holder.title5Tv)

    }
//
//    private fun moreOptionDialog(model: ModelArchivalTask, holder: HolderArchivalTask) {
//        val taskUid = model.uid
//        val taskTitle = model.title
//
//        val options = arrayOf("Edytuj zadanie", "Usuń zadanie", "Podejmij zadanie", "Oznacz jako wykonane")
//        val builder = AlertDialog.Builder(context)
//        builder.setTitle("Co chcesz zrobić z zadaniem?")
//            .setItems(options){dialog, position->
//                if(position==0){//edytuj zadanie
//                    val intent = Intent(context, TaskEditActivity::class.java)
//                    intent.putExtra("taskUID", taskUid)
//                    context.startActivity(intent)
//                }else if(position==1){//usuwanie zadania
//                    val builderInside = AlertDialog.Builder(context)
//                    builderInside.setTitle("Na pewno chcesz usunąć zadanie")
//                        .setPositiveButton("Tak"){a, d->
//                            Toast.makeText(context, "Usuwanie", Toast.LENGTH_SHORT).show()
//                            MyApplication.deleteTask(context, taskUid, taskTitle)
//                        }
//                        .setNegativeButton("Nie"){a, d->
//                            a.dismiss()
//                        }
//                        .show()
//                }else if(position==2){//przypisz zadanie do konta
//                    val builderInside = AlertDialog.Builder(context)
//                    builderInside.setTitle("Czy na pewno chcesz podjąć zadanie")
//                        .setPositiveButton("Tak"){a, d->
//                            //Toast.makeText(context, "Usuwanie", Toast.LENGTH_SHORT).show()
//                            setTaskDoing(taskUid)
//                        }
//                        .setNegativeButton("Nie"){a, d->
//                            a.dismiss()
//                        }
//                        .show()
//                }else if(position==3){//oznacz zadanie jako wykonane
//                    val builderInside = AlertDialog.Builder(context)
//                    builderInside.setTitle("Na pewno chcesz usunąć zadanie")
//                        .setPositiveButton("Tak"){a, d->
//                            //Toast.makeText(context, "Usuwanie", Toast.LENGTH_SHORT).show()
//                            setTaskDone(taskUid)
//                        }
//                        .setNegativeButton("Nie"){a, d->
//                            a.dismiss()
//                        }
//                        .show()
//                }
//            }
//            .show()
//    }
//
    private var doing = ""
//
//    private fun setTaskDone(taskUid: String) {
//        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tasks")
//        ref.child(taskUid).get().addOnSuccessListener {
//            doing = it.child("isDoing").toString()
//            if(doing.isNotEmpty()){
//                val uid = FirebaseAuth.getInstance().uid
//                val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
//                ref.child(uid!!).get().addOnSuccessListener {
//                    val name = it.child("name").value
//                    val hashMap = HashMap<String, Any>()
//                    hashMap["isDoing"] = name.toString()
//                    hashMap["isDone"] = "done"
//                    val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tasks")
//                    ref.child(taskUid)
//                        .updateChildren(hashMap)
//                        .addOnSuccessListener {
//
//                        }
//                        .addOnFailureListener { e ->
//
//                        }
//                }
//            }else{
//                val refer = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tasks")
//                refer.child(taskUid).child("isDone").setValue("done")
//                    .addOnSuccessListener {
//                        Toast.makeText(context, "Zadania wykonane", Toast.LENGTH_SHORT).show()
//                    }
//            }
//        }
//
//    }
//
//    private fun setTaskDoing(taskUid: String) {
//        val uid = FirebaseAuth.getInstance().uid
//        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
//        ref.child(uid!!).get().addOnSuccessListener {
//            val name = it.child("name").value
//            val refer = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tasks")
//            refer.child(taskUid).child("isDoing").setValue(name)
//                .addOnSuccessListener {
//                    Toast.makeText(context, "Zadania podjął się $name", Toast.LENGTH_SHORT).show()
//                }
//        }.addOnFailureListener {
//
//        }
//    }

    private fun getDate(timestamp: Long): String  {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a")
            val netDate = Date(timestamp)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    override fun getItemCount(): Int {
        return taskArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter ==null){
            filter = FilterArchivalTask(filterList, this)
        }
        return filter as FilterArchivalTask
    }

    inner class HolderArchivalTask(itemView: View):RecyclerView.ViewHolder(itemView){
        val titleTv = binding.titleTv
        val title2Tv = binding.title2Tv
        val title3Tv = binding.title3Tv
        val title4Tv = binding.title4Tv
        val title5Tv = binding.title5Tv
        val title6Tv = binding.title6Tv
        val title7Tv = binding.title7Tv
        val cardView = binding.CardView185
    }
}