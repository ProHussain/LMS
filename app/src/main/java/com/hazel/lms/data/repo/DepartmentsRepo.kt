package com.hazel.lms.data.repo

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.hazel.lms.data.beans.Department
import com.hazel.lms.data.room.dao.DepartmentDao
import com.hazel.lms.data.room.dao.StudentsDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class DepartmentsRepo @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val departmentDao: DepartmentDao,
    private val studentsDao: StudentsDao
) {
    private val pageSize = 10

    fun getPagedDepartments() = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            prefetchDistance = pageSize / 2,
            enablePlaceholders = false,
            initialLoadSize = pageSize,
            maxSize = pageSize + (pageSize * 2)
        ),
        pagingSourceFactory = { departmentDao.getPagedDepartments() }
    ).flow

    suspend fun insertDepartment(department: Department) = flow {
        emit(departmentDao.insertDepartment(department))
    }

    suspend fun updateDepartment(department: Department) = flow {
        emit(departmentDao.updateDepartment(department))
    }

    suspend fun deleteDepartment(it: Department) = flow {
        val result = departmentDao.deleteDepartment(it)
        if (result > 0) {
            deleteImage(it.image)
            val studentsImages = studentsDao.getStudentImages(it.id)
            studentsImages.forEach {
                deleteImage(it)
            }
            studentsDao.deleteStudentsByDepartment(it.id)
        }
        emit(result)
    }

    fun copyImage(image: File, oldImagePath: String): String {
        val myFolder = File(
            context.filesDir,
            "LMS"
        )
        if (!myFolder.exists())
            if (myFolder.mkdir())
                Timber.d("Folder created")

        if (oldImagePath.isNotEmpty()) {
            val oldImage = File(oldImagePath)
            if (oldImage.exists())
                if (oldImage.delete()) {
                    Timber.d("Old image deleted")
                }

        }
        val randomName = System.currentTimeMillis().toString()
        val newFile = File(myFolder, "$randomName.jpg")
        image.copyTo(newFile)
        return newFile.absolutePath
    }

    private fun deleteImage(imagePath: String) {
        val image = File(imagePath)
        if (image.exists())
            if (image.delete()) {
                Timber.d("Image deleted")
            }
    }

    suspend fun getDepartment(editID: Int) = flow {
        emit(departmentDao.getDepartment(editID))
    }
}