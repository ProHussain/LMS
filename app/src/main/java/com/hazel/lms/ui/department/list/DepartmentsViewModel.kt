package com.hazel.lms.ui.department.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hazel.lms.data.beans.Department
import com.hazel.lms.data.repo.DepartmentsRepo
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
class DepartmentsViewModel @Inject constructor(
    private val departmentsRepo: DepartmentsRepo
) : ViewModel() {

    private val dispatcherIO : CoroutineDispatcher get() = Dispatchers.IO

    private val _deleteDepartment = MutableStateFlow<RequestResult<Int>>(RequestResult.Idle())
    val deleteDepartment = _deleteDepartment.asStateFlow()
    private val _departments = MutableStateFlow<PagingData<Department>>(PagingData.empty())
    val departments = _departments.asStateFlow()



    fun getAllDepartments() {
        viewModelScope.launch {
            withContext(dispatcherIO) {
                departmentsRepo.getPagedDepartments().cachedIn(viewModelScope).collectLatest {
                    Timber.d("getAllDepartments: Data received from repo")
                    _departments.value = it
                }
            }
        }
    }

    fun deleteDepartment(dept: Department) {
        viewModelScope.launch {
            withContext(dispatcherIO) {
                _deleteDepartment.value = RequestResult.Loading()
                departmentsRepo.deleteDepartment(dept).catch {
                    _deleteDepartment.value = RequestResult.Error(it.message.toString())
                }.collectLatest {
                    if (it > 0) {
                        _deleteDepartment.value =
                            RequestResult.Success(it, "Department deleted successfully")
                    } else
                        _deleteDepartment.value = RequestResult.Error("Error deleting department")
                }
            }
        }
    }

    fun resetFlow() {
        _deleteDepartment.value = RequestResult.Idle()
    }

}