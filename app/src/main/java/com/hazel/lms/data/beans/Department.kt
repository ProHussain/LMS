package com.hazel.lms.data.beans

import androidx.room.Entity

@Entity
data class Department(
    @androidx.room.PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String,
    val location: String,
    val date: String,
    val hod: String,
    val phone: String,
    val description: String,
    var image: String
)
