package com.hazel.lms.ui.students.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val studentsRepo: StudentsRepo
) : ViewModel() {

    private val dispatcherIO : CoroutineDispatcher get() = Dispatchers.IO

    private val _deleteStudent = MutableStateFlow<RequestResult<Int>>(RequestResult.Idle())
    val deleteStudent = _deleteStudent.asStateFlow()

    private val _students = MutableStateFlow<PagingData<Student>>(PagingData.empty())
    val students = _students.asStateFlow()


    fun getStudents(departmentID: Int) {
        Timber.e("Department ID: $departmentID")
        viewModelScope.launch {
            withContext(dispatcherIO) {
                studentsRepo.getStudents(departmentID).cachedIn(viewModelScope).collectLatest {
                    _students.value = it
                }
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            withContext(dispatcherIO) {
                _deleteStudent.value = RequestResult.Loading()
                studentsRepo.deleteStudent(student).catch {
                    _deleteStudent.value = RequestResult.Error(it.message.toString())
                }.collectLatest {
                    if (it > 0) {
                        _deleteStudent.value =
                            RequestResult.Success(it, "Student deleted successfully")
                    } else
                        _deleteStudent.value = RequestResult.Error("Error deleting student")
                }
            }
        }
    }

    fun resetDeleteStudent() {
        _deleteStudent.value = RequestResult.Idle()
    }
}