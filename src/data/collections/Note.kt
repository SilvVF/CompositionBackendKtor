package com.androiddevs.data.collections

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Note(
    val title: String,
    val content: String,
    val date: Long, //will be a timestamp
    val owners: List<String>, //multiple emails of people allowed to edit
    val color: String, // hex value
    @BsonId //binary json -> id is a randomly generated string num
    val id: String = ObjectId().toString() // string id - up to 25
)
