package com.hazel.lms.data.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hazel.lms.data.beans.Student

@Dao
interface StudentsDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student): Int

    @Delete
    suspend fun deleteStudent(it: Student): Int

    @Query("SELECT * FROM student WHERE department = :departmentID ORDER BY id ASC")
    fun getPagedStudents(departmentID: Int): PagingSource<Int, Student>

    @Query("SELECT image FROM student WHERE department = :id")
    suspend fun getStudentImages(id: Int): List<String>

    @Query("DELETE FROM student WHERE department = :id")
    suspend fun deleteStudentsByDepartment(id: Int)

    @Query("SELECT * FROM student WHERE id = :editID")
    suspend fun getStudent(editID: Int): Student

}