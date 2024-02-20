package com.hazel.lms.ui.department.add_edit

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_UP
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
import com.hazel.lms.R
import com.hazel.lms.data.beans.Department
import com.hazel.lms.data.sealed.RequestResult
import com.hazel.lms.databinding.FragmentAddEditDepartmentBinding
import com.hazel.lms.utils.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream


@AndroidEntryPoint
class AddEditDepartmentFragment : Fragment() {

    private lateinit var binding: FragmentAddEditDepartmentBinding
    private val viewModel: AddEditDepartmentViewModel by viewModels()
    private val progressDialog by lazy { ProgressDialog(requireContext()) }
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImage: File

    // Edit department Fields
    private var editID = 0
    private var isEdit = false
    private var editImage = false
    private var editImagePath = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEditDepartmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initArgs()
        initLaunchers()
        initClickListeners()
        initCollectors()
    }

    private fun initArgs() {
        isEdit = arguments?.getBoolean("isEdit") ?: false
        if (isEdit) {
            editID = arguments?.getInt("departmentID") ?: 0
            viewModel.getDepartment(editID)
        }
    }

    private fun initCollectors() {

        lifecycleScope.launch {
            viewModel.department.collectLatest {
                when (it) {
                    is RequestResult.Idle -> {
                        // Do nothing
                    }

                    is RequestResult.Loading -> {
                        progressDialog.show()
                        progressDialog.updateMessage("Loading department...")
                    }

                    is RequestResult.Success -> {
                        progressDialog.dismiss()
                        val department = it.data
                        editImagePath = department.image
                        binding.btnSave.text = getString(R.string.update)
                        binding.btnSave.tag = "update"
                        binding.etDepartmentName.setText(department.name)
                        binding.etLocation.setText(department.location)
                        binding.etDate.setText(department.date)
                        binding.etHod.setText(department.hod)
                        binding.etPhone.setText(department.phone)
                        binding.etDescription.setText(department.description)
                        selectedImage = File(department.image)
                        binding.ivDepartment.setImageURI(Uri.fromFile(File(department.image)))
                    }

                    is RequestResult.Error -> {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.addDepartment.collectLatest {
                when (it) {
                    is RequestResult.Idle -> {
                        // Do nothing
                    }

                    is RequestResult.Loading -> {
                        progressDialog.show()
                        progressDialog.updateMessage("Adding department...")
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
                }
            }
        }

        lifecycleScope.launch {
            viewModel.updateDepartment.collectLatest {
                when (it) {
                    is RequestResult.Idle -> {
                        // Do nothing
                    }

                    is RequestResult.Loading -> {
                        progressDialog.show()
                        progressDialog.updateMessage("Updating department...")
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
                }
            }
        }
    }

    private fun initLaunchers() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    selectedImage = uriToFile(data?.data!!)
                    binding.ivDepartment.setImageURI(data.data)
                    if (isEdit)
                        editImage = true
                }
            }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun initClickListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        binding.ivDepartment.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        binding.etDate.setOnTouchListener { _, event ->
            if (event.action == ACTION_UP) {
                val datePickerDialog = DatePickerDialog(requireContext())
                datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                    binding.etDate.setText("$dayOfMonth/${month + 1}/$year")
                }
                datePickerDialog.show()
            }
            true
        }

        binding.btnSave.setOnClickListener {
            validateInputData()
        }
    }

    private fun validateInputData() {
        val name = binding.etDepartmentName.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val hod = binding.etHod.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (name.isEmpty()) {
            binding.etDepartmentName.error = "Department name is required"
            binding.etDepartmentName.requestFocus()
            return
        }
        if (location.isEmpty()) {
            binding.etLocation.error = "Location is required"
            binding.etLocation.requestFocus()
            return
        }
        if (date.isEmpty()) {
            binding.etDate.error = "Date is required"
            binding.etDate.requestFocus()
            return
        }
        if (hod.isEmpty()) {
            binding.etHod.error = "HOD is required"
            binding.etHod.requestFocus()
            return
        }
        if (phone.isEmpty()) {
            binding.etPhone.error = "Phone is required"
            binding.etPhone.requestFocus()
            return
        }
        if (description.isEmpty()) {
            binding.etDescription.error = "Description is required"
            binding.etDescription.requestFocus()
            return
        }
        if (!isEdit && this::selectedImage.isInitialized.not()) {
            val snack =
                Snackbar.make(binding.root, "Please select an image", Snackbar.LENGTH_LONG)
            snack.setAction("OK") {
                snack.dismiss()
            }
            snack.show()
            return
        }
        val department = Department(
            name = name,
            location = location,
            date = date,
            hod = hod,
            phone = phone,
            description = description,
            image = editImagePath
        )
        if (binding.btnSave.tag == "update") {
            department.id = editID
            viewModel.updateDepartment(department, selectedImage, editImage)
        } else {
            viewModel.addDepartment(department, selectedImage)
        }

    }

    private fun uriToFile(uri: Uri): File {
        val parcelFileDescriptor = requireContext().contentResolver.openFileDescriptor(uri, "r", null) ?: throw FileNotFoundException()
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(requireContext().cacheDir, "temp.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        parcelFileDescriptor.close()
        inputStream.close()
        outputStream.close()
        return file
    }
}