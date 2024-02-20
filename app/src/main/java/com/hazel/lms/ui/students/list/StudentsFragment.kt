package com.hazel.lms.ui.students.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hazel.lms.R
import com.hazel.lms.data.sealed.RequestResult
import com.hazel.lms.databinding.FragmentStudentsBinding
import com.hazel.lms.ui.students.list.adapter.StudentsAdapter
import com.hazel.lms.utils.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class StudentsFragment : Fragment() {

    private val viewModel: StudentsViewModel by viewModels()
    private lateinit var binding: FragmentStudentsBinding
    private lateinit var adapter: StudentsAdapter
    private val progressDialog by lazy { ProgressDialog(requireContext()) }

    private var departmentID = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initArgs()
        initAdapter()
        initClickListeners()
        initCollectors()
    }

    private fun initArgs() {
        departmentID = requireArguments().getInt("departmentID")
        viewModel.getStudents(departmentID)
    }

    private fun initAdapter() {
        adapter = StudentsAdapter(
            onItemClicked = {
                Timber.e("Student clicked: $it")
            },
            onItemEditClicked = {
                val bundle = Bundle()
                bundle.putBoolean("isEdit", true)
                bundle.putInt("departmentID", departmentID)
                bundle.putInt("studentID", it.id)
                findNavController().navigate(
                    R.id.action_studentsFragment_to_addEditStudentFragment,
                    bundle
                )
            },
            onItemDeleteClicked = { student ->
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Delete Student")
                    .setMessage("Are you sure you want to delete this student?")
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.deleteStudent(student)
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialog.show()
            }
        )

        adapter.addLoadStateListener {
            if (it.append.endOfPaginationReached) {
                if (adapter.itemCount < 1) {
                    binding.ivError.visibility = View.VISIBLE
                    binding.rvStudents.visibility = View.GONE
                } else {
                    binding.ivError.visibility = View.GONE
                    binding.rvStudents.visibility = View.VISIBLE
                }
            }
        }
        binding.rvStudents.adapter = adapter
    }

    private fun initClickListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        binding.fabAddStudent.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("isEdit", false)
            bundle.putInt("departmentID", departmentID)
            findNavController().navigate(
                R.id.action_studentsFragment_to_addEditStudentFragment,
                bundle
            )
        }
    }

    private fun initCollectors() {
        lifecycleScope.launch {
            viewModel.students.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            viewModel.deleteStudent.collectLatest {
                when (it) {
                    is RequestResult.Loading -> {
                        progressDialog.show()
                    }

                    is RequestResult.Success -> {
                        progressDialog.dismiss()
                        Snackbar.make(binding.root, it.message, Snackbar.LENGTH_SHORT).show()
                    }

                    is RequestResult.Error -> {
                        Timber.e(it.message)
                        progressDialog.dismiss()
                        Snackbar.make(binding.root, it.message, Snackbar.LENGTH_SHORT).show()
                    }

                    is RequestResult.Idle -> {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetDeleteStudent()
    }
}