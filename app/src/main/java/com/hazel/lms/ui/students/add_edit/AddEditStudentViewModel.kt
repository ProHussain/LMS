package com.hazel.lms.ui.students.add_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazel.lms.data.beans.Student
import com.hazel.lms.data.repo.StudentsRepo
import com.hazel.lms.data.sealed.RequestResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddEditStudentViewModel @Inject constructor(
    private val studentsRepo: StudentsRepo
) : ViewModel() {

    private val dispatcherIO : CoroutineDispatcher get() = Dispatchers.IO

    private val _addStudent = MutableStateFlow<RequestResult<Long>>(RequestResult.Idle())
    val addStudent = _addStudent.asStateFlow()

    private val _updateStudent = MutableStateFlow<RequestResult<Int>>(RequestResult.Idle())
    val updateStudent = _updateStudent.asStateFlow()

    private val _student = MutableStateFlow<RequestResult<Student>>(RequestResult.Idle())
    val student = _student.asStateFlow()

    fun updateStudent(student: Student, selectedImage: File, editImage: Boolean) {
        viewModelScope.launch{
            withContext(dispatcherIO) {
                _updateStudent.value = RequestResult.Loading()
                if (editImage) {
                    val path = studentsRepo.copyImage(selectedImage, student.image)
                    student.image = path
                }
                studentsRepo.updateStudent(student).catch {
                    _updateStudent.value = RequestResult.Error(it.message!!)
                }.collectLatest {
                    if (it > 0)
                        _updateStudent.value = RequestResult.Success(it, "Student updated successfully")
                    else
                        _updateStudent.value = RequestResult.Error("Error updating student")
                }
            }
        }
    }

    fun addStudent(student: Student, selectedImage: File) {
        viewModelScope.launch {
            withContext(dispatcherIO) {
                _addStudent.value = RequestResult.Loading()
                val path = studentsRepo.copyImage(selectedImage,student.image)
                student.image = path
                studentsRepo.insertStudent(student).catch {
                    _addStudent.value = RequestResult.Error(it.message!!)
                }.collectLatest {
                    if (it > 0)
                        _addStudent.value = RequestResult.Success(it, "Student added successfully")
                    else
                        _addStudent.value = RequestResult.Error("Error adding student")
                }
            }
        }
    }

    fun getStudent(editID: Int) {
        viewModelScope.launch {
            withContext(dispatcherIO) {
                _student.value = RequestResult.Loading()
                studentsRepo.getStudent(editID).catch {
                    _student.value = RequestResult.Error(it.message!!)
                }.collectLatest {
                    _student.value = RequestResult.Success(it, "Student fetched successfully")
                }
            }
        }
    }
}