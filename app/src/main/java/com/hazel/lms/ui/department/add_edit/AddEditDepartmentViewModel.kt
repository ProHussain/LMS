package com.hazel.lms.ui.department.add_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazel.lms.data.beans.Department
import com.hazel.lms.data.repo.DepartmentsRepo
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
class AddEditDepartmentViewModel @Inject constructor(
    private val departmentsRepo: DepartmentsRepo
) : ViewModel() {

    private val dispatcherIO : CoroutineDispatcher get() = Dispatchers.IO

    private val _addDepartment = MutableStateFlow<RequestResult<Long>>(RequestResult.Idle())
    val addDepartment = _addDepartment.asStateFlow()

    private val _updateDepartment = MutableStateFlow<RequestResult<Int>>(RequestResult.Idle())
    val updateDepartment = _updateDepartment.asStateFlow()

    private val _department = MutableStateFlow<RequestResult<Department>>(RequestResult.Idle())
    val department = _department.asStateFlow()

    fun addDepartment(department: Department, image: File) {
        viewModelScope.launch {
            withContext(dispatcherIO) {
                _addDepartment.value = RequestResult.Loading()
                val path = departmentsRepo.copyImage(image, department.image)
                department.image = path
                departmentsRepo.insertDepartment(department).catch {
                    _addDepartment.value = RequestResult.Error(it.message!!)
                }.collectLatest {
                    if (it > 0)
                        _addDepartment.value =
                            RequestResult.Success(it, "Department added successfully")
                    else
                        _addDepartment.value = RequestResult.Error("Error adding department")
                }
            }
        }
    }

    fun updateDepartment(department: Department, selectedImage: File, editImage: Boolean) {
        viewModelScope.launch {
            withContext(dispatcherIO) {
                _updateDepartment.value = RequestResult.Loading()
                if (editImage) {
                    val path = departmentsRepo.copyImage(selectedImage, department.image)
                    department.image = path
                }
                departmentsRepo.updateDepartment(department).catch {
                    _updateDepartment.value = RequestResult.Error(it.message!!)
                }.collectLatest {
                    if (it > 0)
                        _updateDepartment.value =
                            RequestResult.Success(it, "Department updated successfully")
                    else
                        _updateDepartment.value = RequestResult.Error("Error updating department")
                }
            }
        }
    }

    fun getDepartment(editID: Int) {
        viewModelScope.launch {
            withContext(dispatcherIO) {
                _department.value = RequestResult.Loading()
                departmentsRepo.getDepartment(editID).catch {
                    _department.value = RequestResult.Error(it.message!!)
                }.collectLatest {
                    _department.value =
                        RequestResult.Success(it, "Department retrieved successfully")
                }
            }
        }
    }
}