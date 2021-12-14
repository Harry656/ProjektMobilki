package com.example.projektmobilki.filters

import android.widget.Filter
import com.example.projektmobilki.models.ModelTask
import com.example.projektmobilki.adapters.AdapterTask

class FilterTasks: Filter {

    var filterList: ArrayList<ModelTask>

    var adapterTask: AdapterTask

    constructor(filterList: ArrayList<ModelTask>, adapterTask: AdapterTask){
        this.filterList = filterList
        this.adapterTask = adapterTask
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint
        var results = FilterResults()
        if(constraint != null && constraint.isNotEmpty()){
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelTask>()
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

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterTask.taskArrayList = results.values as ArrayList<ModelTask>

        adapterTask.notifyDataSetChanged()
    }
}