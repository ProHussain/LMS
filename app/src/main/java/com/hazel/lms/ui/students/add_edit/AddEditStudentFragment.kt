package com.hazel.lms.ui.students.add_edit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hazel.lms.data.beans.Student
import com.hazel.lms.data.sealed.RequestResult
import com.hazel.lms.databinding.FragmentAddEditStudentBinding
import com.hazel.lms.utils.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

@AndroidEntryPoint
class AddEditStudentFragment : Fragment() {
    private lateinit var binding: FragmentAddEditStudentBinding
    private val viewModel: AddEditStudentViewModel by viewModels()
    private val progressDialog by lazy { ProgressDialog(requireContext()) }
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImage: File

    // Edit student Fields
    private var editID = 0
    private var departmentID = 0
    private var isEdit = false
    private var editImage = false
    private var editImagePath = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEditStudentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initArgs()
        initLaunchers()
        initClickListeners()
        initCollectors()
    }

    private fun initCollectors() {

        lifecycleScope.launch {
            viewModel.student.collectLatest {
                when (it) {
                    is RequestResult.Loading -> {
                        progressDialog.show()
                        progressDialog.updateMessage("Loading Student...")
                    }

                    is RequestResult.Success -> {
                        progressDialog.dismiss()
                        val student = it.data
                        editImagePath = student.image
                        binding.btnSave.text = "Update"
                        binding.btnSave.tag = "update"
                        selectedImage = File(student.image)
                        binding.apply {
                            etStudentName.setText(student.name)
                            etEmail.setText(student.email)
                            etPhone.setText(student.phone)
                            etAddress.setText(student.address)
                            etSemester.setText(student.semester)
                            etRollNo.setText(student.rollNo)
                            if (student.image.isNotEmpty()) {
                                ivStudent.setImageURI(Uri.fromFile(File(student.image)))
                            }
                        }
                    }

                    is RequestResult.Error -> {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    is RequestResult.Idle -> {
                        // Do nothing
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.addStudent.collectLatest {
                when (it) {
                    is RequestResult.Loading -> {
                        progressDialog.show()
                        progressDialog.updateMessage("Adding Student...")
                    }

                    is RequestResult.Success -> {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }

                    is RequestResult.Error -> {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    is RequestResult.Idle -> {
                        // Do nothing
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.updateStudent.collectLatest {
                when (it) {
                    is RequestResult.Loading -> {
                        progressDialog.show()
                        progressDialog.updateMessage("updating Student...")
                    }

                    is RequestResult.Success -> {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }

                    is RequestResult.Error -> {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    is RequestResult.Idle -> {
                        // Do nothing
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initArgs() {
        isEdit = arguments?.getBoolean("isEdit") ?: false
        departmentID = arguments?.getInt("departmentID") ?: 0
        if (isEdit) {
            editID = arguments?.getInt("studentID") ?: 0
            viewModel.getStudent(editID)
        }
    }

    private fun initLaunchers() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    selectedImage = uriToFile(data?.data!!)
                    binding.ivStudent.setImageURI(data.data)
                    if (isEdit)
                        editImage = true
                }
            }
    }

    private fun initClickListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        binding.ivStudent.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            validateInputData()
        }
    }

    private fun validateInputData() {
        val name = binding.etStudentName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val semester = binding.etSemester.text.toString().trim()
        val rollNo = binding.etRollNo.text.toString().trim()

        if (name.isEmpty()) {
            binding.etStudentName.error = "Student name is required"
            binding.etStudentName.requestFocus()
            return
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Please enter a valid email"
            binding.etEmail.requestFocus()
            return
        }
        if (phone.isEmpty()) {
            binding.etPhone.error = "Phone is required"
            binding.etPhone.requestFocus()
            return
        }
        if (address.isEmpty()) {
            binding.etAddress.error = "Address is required"
            binding.etAddress.requestFocus()
            return
        }
        if (semester.isEmpty()) {
            binding.etSemester.error = "Semester is required"
            binding.etSemester.requestFocus()
            return
        }
        if (rollNo.isEmpty()) {
            binding.etRollNo.error = "Roll No is required"
            binding.etRollNo.requestFocus()
            return
        }
        if (!isEdit && !::selectedImage.isInitialized) {
            val snack =
                Snackbar.make(binding.root, "Please select an image", Snackbar.LENGTH_LONG)
            snack.setAction("OK") {
                snack.dismiss()
            }
            snack.show()
            return
        }
        val student = Student(
            department = departmentID,
            name = name,
            email = email,
            phone = phone,
            address = address,
            semester = semester,
            rollNo = rollNo,
            image = editImagePath
        )

        if (isEdit) {
            student.id = editID
            viewModel.updateStudent(student, selectedImage, editImage)
        } else {
            viewModel.addStudent(student, selectedImage)
        }
    }

    @SuppressLint("Recycle")
    private fun uriToFile(uri: Uri): File {
        val parcelFileDescriptor =
            requireContext().contentResolver.openFileDescriptor(uri, "r", null)
                ?: throw FileNotFoundException()
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(requireContext().cacheDir, "temp.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        return file
    }
}