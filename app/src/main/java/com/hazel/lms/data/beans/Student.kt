package com.hazel.lms.data.beans

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Student(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val department: Int,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val semester: String,
    val rollNo: String,
    var image: String = "",
)
