package com.example.projektmobilki.models

class ModelTask {

    var categoryUID:String = ""
    var description:String = ""
    var isDoing:String = ""
    var isDone:String = ""
    var title:String = ""
    var uid:String = ""
    var timestamp:Long =0

    constructor()

    constructor(
        categoryUID:String,
        description:String,
        isDoing:String,
        isDone:String,
        title:String,
        uid:String,
        timestamp:Long
    ){
        this.categoryUID = categoryUID
        this.description = description
        this.isDoing = isDoing
        this.isDone = isDone
        this.title = title
        this.uid = uid
        this.timestamp = timestamp
    }
}