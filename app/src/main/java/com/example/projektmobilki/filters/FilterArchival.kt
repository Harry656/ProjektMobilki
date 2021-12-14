package com.example.projektmobilki.filters

import android.widget.Filter
import com.example.projektmobilki.adapters.AdapterArchival
import com.example.projektmobilki.models.ModelArchival

class FilterArchival: Filter {
    private var filterList: ArrayList<ModelArchival>

    private var adapterArchilav: AdapterArchival

    constructor(filterList: ArrayList<ModelArchival>, adapterArchival: AdapterArchival):super(){
        this.filterList = filterList
        this.adapterArchilav = adapterArchival
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if(constraint != null && constraint.isNotEmpty()){
            constraint = constraint.toString().uppercase()
            val filteredModels:ArrayList<ModelArchival> = ArrayList()
            for (i in 0 until filterList.size){
                if (filterList[i].name.uppercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterArchilav.archivalArrayList = results.values as ArrayList<ModelArchival>
        adapterArchilav.notifyDataSetChanged()
    }
}