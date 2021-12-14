package com.example.projektmobilki.models

class ModelCategory{

    var name:String = ""
    var room:String = ""
    var uid:String = ""
    var timestamp:Long =0

    constructor()

    constructor(name:String, room:String, uid:String, timestamp:Long){
        this.name = name
        this.room = room
        this.uid = uid
        this.timestamp = timestamp
    }

}