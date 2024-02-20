package com.hazel.lms.data.room.db

import androidx.room.RoomDatabase
import com.hazel.lms.data.beans.Department
import com.hazel.lms.data.beans.Student
import com.hazel.lms.data.room.dao.DepartmentDao
import com.hazel.lms.data.room.dao.StudentsDao

@androidx.room.Database(entities = [Department::class, Student::class], version = 2, exportSchema = true)
abstract class LmsRoom : RoomDatabase() {
    abstract fun departmentDao(): DepartmentDao
    abstract fun studentDao(): StudentsDao
}