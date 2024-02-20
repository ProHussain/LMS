package com.hazel.lms.data.repo

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.hazel.lms.data.beans.Student
import com.hazel.lms.data.room.dao.StudentsDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class StudentsRepo @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val studentsDao: StudentsDao
) {

    fun insertStudent(student: Student) = flow {
        emit(studentsDao.insertStudent(student))
    }

    fun updateStudent(student: Student) = flow {
        emit(studentsDao.updateStudent(student))
    }

    fun deleteStudent(it: Student) = flow {
        val result = studentsDao.deleteStudent(it)
        if (result > 0)
            deleteStudentImage(it.image)
        emit(result)
    }

    fun getStudents(departmentID: Int) = Pager(
        config = PagingConfig(
            pageSize = 10,
            prefetchDistance = 10 / 2,
            enablePlaceholders = false,
            initialLoadSize = 10,
            maxSize = 10 + (10 * 2)
        ),
        pagingSourceFactory = { studentsDao.getPagedStudents(departmentID) }
    ).flow

    fun copyImage(image: File, oldImagePath: String): String {
        val myFolder = File(
            context.filesDir,
            "LMS"
        )
        if (!myFolder.exists())
            if ( myFolder.mkdir())
                Timber.d("Folder created")

        if (oldImagePath.isNotEmpty()) {
            val oldImage = File(oldImagePath)
            if (oldImage.exists())
                if (oldImage.delete())
                    Timber.d("Old image deleted")
        }

        val randomName = System.currentTimeMillis().toString()
        val newFile = File(myFolder, "$randomName.jpg")
        image.copyTo(newFile)
        return newFile.absolutePath
    }

    private fun deleteStudentImage(imagePath: String) {
        val image = File(imagePath)
        if (image.exists())
            if (image.delete())
                Timber.d("Student image deleted")
    }

    suspend fun getStudent(editID: Int) = flow {
        emit(studentsDao.getStudent(editID))
    }
}
