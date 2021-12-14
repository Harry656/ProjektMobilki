package com.example.projektmobilki.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmobilki.filters.FilterCategory
import com.example.projektmobilki.activities.ListOfTasksActivity
import com.example.projektmobilki.models.ModelCategory
import com.example.projektmobilki.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AdapterCategory :RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context: Context
    public var categoryArrayList: ArrayList<ModelCategory>
    private var filterList: ArrayList<ModelCategory>

    private var filter: FilterCategory? = null


    private lateinit var binding: RowCategoryBinding

    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>){
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val model = categoryArrayList[position]
        val name = model.name
        val room = model.room
        val uid = model.uid
        //val timestamp = model.timestamp

        //val date = getDate(timestamp)

        holder.categoryTv.text = name

        //holder.category2Tv.text = date
        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
        ref.child(uid).get().addOnSuccessListener {
            val timestamp = it.child("timestamp").value
            val date = getDate(timestamp as Long)
            holder.category2Tv.text = date
        }

        holder.deleteBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Usuń")
                .setMessage("Czy na pewno chcesz usunąć kategorie?")
                .setPositiveButton("Tak"){a, d->
                    Toast.makeText(context, "Usuwanie", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }
                .setNegativeButton("Nie"){a, d->
                    a.dismiss()
                }
                .show()
        }

        holder.archiveBtn.setOnClickListener{
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Przenieś do archiwum")
                .setMessage("Czy na pewno chcesz zarchiwizować listę zadań?")
                .setPositiveButton("Tak"){a, d->
                    Toast.makeText(context, "Usuwanie", Toast.LENGTH_SHORT).show()
                    //deleteCategory(model, holder)
                    archivesCategory(model, holder)
                }
                .setNegativeButton("Nie"){a, d->
                    a.dismiss()
                }
                .show()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ListOfTasksActivity::class.java)
            intent.putExtra("categoryId", uid)
            intent.putExtra("categoryName", name)
            context.startActivity(intent)
        }

    }

    private fun getDate(timestamp: Long): String  {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a")
            val netDate = Date(timestamp)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    private fun archivesCategory(model: ModelCategory, holder: HolderCategory) {
        val uid = model.uid
        val room = model.room
        val name = model.name

        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = uid
        hashMap["room"] = room
        hashMap["name"] = name
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Archives")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Dodano listę do archiwum", Toast.LENGTH_SHORT).show()
                val ref2 = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
                ref2.child(uid)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Usunięto kategorię", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e->
                        Toast.makeText(context, "Nie można usunąć ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } .addOnFailureListener{ e->
                Toast.makeText(context, "Nie udało się dodać listy do archiwum ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        val uid = model.uid

        val ref = FirebaseDatabase.getInstance("https://kotlinprojekt-ea40b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
        ref.child(uid)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Usunięto kategorię", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context, "Nie można usunąć ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    inner class HolderCategory(itemView: View): RecyclerView.ViewHolder(itemView){
        var categoryTv:TextView = binding.categoryTv
        var deleteBtn:ImageButton = binding.deleteBtn
        var archiveBtn:ImageButton = binding.archiveBtn
        var category2Tv:TextView = binding.categoty2Tv
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }
}