package com.hazel.lms.data.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import com.hazel.lms.data.beans.Department

@Dao
interface DepartmentDao {

    @androidx.room.Insert
    suspend fun insertDepartment(department: Department): Long

    @androidx.room.Query("SELECT * FROM department ORDER BY id ASC")
    fun getPagedDepartments(): PagingSource<Int, Department>

    @androidx.room.Update
    suspend fun updateDepartment(department: Department): Int

    @androidx.room.Delete
    suspend fun deleteDepartment(it: Department): Int

    @androidx.room.Query("SELECT * FROM department WHERE id = :editID")
    suspend fun getDepartment(editID: Int): Department
}