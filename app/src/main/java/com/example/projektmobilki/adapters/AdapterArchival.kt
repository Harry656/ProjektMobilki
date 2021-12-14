package com.example.projektmobilki.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmobilki.activities.ListOfArchivalTasksActivity
import com.example.projektmobilki.filters.FilterArchival
import com.example.projektmobilki.filters.FilterCategory
import com.example.projektmobilki.activities.ListOfTasksActivity
import com.example.projektmobilki.models.ModelArchival
import com.example.projektmobilki.databinding.RowArchivalBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdapterArchival : RecyclerView.Adapter<AdapterArchival.HolderArchival>, Filterable {
    private val context: Context
    public var archivalArrayList: ArrayList<ModelArchival>
    private var filterList: ArrayList<ModelArchival>

    private var filter: FilterArchival? = null

    private lateinit var binding: RowArchivalBinding

    constructor(context: Context, archivalArrayList: ArrayList<ModelArchival>){
        this.context = context
        this.archivalArrayList = archivalArrayList
        this.filterList = archivalArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderArchival {
        binding = RowArchivalBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderArchival(binding.root)
    }

    override fun onBindViewHolder(holder: HolderArchival, position: Int) {
        val model = archivalArrayList[position]
        val name = model.name
        val room = model.room
        val uid = model.uid
        val timestamp = model.timestamp
        val date = getDate(timestamp)

        holder.categoryTv.text = name

        holder.archiveTv.text = date

        holder.deleteBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Usuń")
                .setMessage("Czy na pewno chcesz usunąć kategorie?")
                .setPositiveButton("Tak"){a, d->
                    Toast.makeText(context, "Usuwanie", Toast.LENGTH_SHORT).show()
                    deleteArchival(model, holder)
                }
                .setNegativeButton("Nie"){a, d->
                    a.dismiss()
                }
                .show()
        }

//        holder.archiveBtn.setOnClickListener{
//            val builder = AlertDialog.Builder(context)
//            builder.setTitle("Przenieś do archiwum")
//                .setMessage("Czy na pewno chcesz zarchiwizować listę zadań?")
//                .setPositiveButton("Tak"){a, d->
//                    Toast.makeText(context, "Usuwanie", Toast.LENGTH_SHORT).show()
//                    //deleteCategory(model, holder)
//                    archivesCategory(model, holder)
//                }
//                .setNegativeButton("Nie"){a, d->
//                    a.dismiss()
//                }
//                .show()
//        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ListOfArchivalTasksActivity::class.java)
            intent.putExtra("categoryId", uid)
            intent.putExtra("categoryName", name)
            context.startActivity(intent)
        }

    }

//    private fun archivesArchives(model: ModelArchival, holder: HolderArchival) {
//        val uid = model.uid
//        val room = model.room
//        val name = model.name
//
//        val timestamp = System.currentTimeMillis()
//        val hashMap = HashMap<String, Any>()
//        hashMap["uid"] = uid
//        hashMap["room"] = room
//        hashMap["name"] = name
//        hashMap["timestamp"] = timestamp
//
//        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Archives")
//        ref.child(uid!!)
//            .setValue(hashMap)
//            .addOnSuccessListener {
//                Toast.makeText(context, "Dodano listę do archiwum", Toast.LENGTH_SHORT).show()
//            } .addOnFailureListener{ e->
//                Toast.makeText(context, "Nie udało się dodać listy do archiwum ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

    override fun getItemCount(): Int {
        return archivalArrayList.size
    }

//    private fun getDate(timestamp: Long) :String {
//        val calendar = Calendar.getInstance(Locale.ENGLISH)
//        calendar.timeInMillis
//        val date = DateFormat.format("dd-MM-yyyy hh:mm a",calendar).toString()
//        return date
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

    private fun deleteArchival(model: ModelArchival, holder: HolderArchival) {
        val uid = model.uid

        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Archives")
        ref.child(uid)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Usunięto kategorię", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context, "Nie można usunąć ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    inner class HolderArchival(itemView: View): RecyclerView.ViewHolder(itemView){
        var categoryTv: TextView = binding.categoryTv
        var deleteBtn: ImageButton = binding.deleteBtn
        var archiveTv: TextView = binding.archiveTv
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterArchival(filterList, this)
        }
        return filter as FilterArchival
    }
}