package com.example.projektmobilki.filters

import android.widget.Filter
import com.example.projektmobilki.adapters.AdapterArchivalTask
import com.example.projektmobilki.adapters.AdapterTask
import com.example.projektmobilki.models.ModelArchivalTask
import com.example.projektmobilki.models.ModelTask

class FilterArchivalTask: Filter {
    var filterList: ArrayList<ModelArchivalTask>

    var adapterTask: AdapterArchivalTask

    constructor(filterList: ArrayList<ModelArchivalTask>, adapterArchivalTask: AdapterArchivalTask){
        this.filterList = filterList
        this.adapterTask = adapterArchivalTask
    }

    override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
        var constraint:CharSequence? = constraint
        var results = Filter.FilterResults()
        if(constraint != null && constraint.isNotEmpty()){
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelArchivalTask>()
            for(i in filterList.indices){
                if(filterList[i].title.lowercase().contains(constraint)){
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

    override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults) {
        adapterTask.taskArrayList = results.values as ArrayList<ModelArchivalTask>

        adapterTask.notifyDataSetChanged()
    }
}