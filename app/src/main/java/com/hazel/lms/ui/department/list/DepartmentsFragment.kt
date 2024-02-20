package com.hazel.lms.ui.department.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hazel.lms.R
import com.hazel.lms.data.sealed.RequestResult
import com.hazel.lms.databinding.FragmentDepartmentsBinding
import com.hazel.lms.ui.department.list.adapter.DepartmentsAdapter
import com.hazel.lms.utils.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class DepartmentsFragment : Fragment() {
    private val viewModel: DepartmentsViewModel by viewModels()
    private lateinit var binding: FragmentDepartmentsBinding
    private lateinit var adapter: DepartmentsAdapter
    private val progressDialog by lazy { ProgressDialog(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDepartmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getAllDepartments()
        initAdapter()
        initClickListeners()
        initCollectors()
    }

    private fun initCollectors() {
        lifecycleScope.launch {
            viewModel.departments.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            viewModel.deleteDepartment.collectLatest {
                when (it) {
                    is RequestResult.Error -> {
                        progressDialog.dismiss()
                        Timber.e(it.message)
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    is RequestResult.Idle -> {
                        // Do nothing
                    }

                    is RequestResult.Loading -> {
                        progressDialog.show()
                        progressDialog.updateMessage("Deleting department...")
                    }

                    is RequestResult.Success -> {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun initClickListeners() {
        binding.fabAddDepartment.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("isEdit", false)
            findNavController().navigate(
                R.id.action_departmentsFragment_to_addEditDepartmentFragment,
                bundle
            )
        }
    }

    private fun initAdapter() {
        adapter = DepartmentsAdapter(
            onItemClicked = {
                val bundle = Bundle()
                bundle.putInt("departmentID", it.id)
                findNavController().navigate(
                    R.id.action_departmentsFragment_to_studentsFragment,
                    bundle
                )
            },
            onItemEditClicked = {
                val bundle = Bundle()
                bundle.putBoolean("isEdit", true)
                bundle.putInt("departmentID", it.id)
                findNavController().navigate(
                    R.id.action_departmentsFragment_to_addEditDepartmentFragment,
                    bundle
                )
            },
            onItemDeleteClicked = { department ->
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Delete Department")
                    .setMessage("Are you sure you want to delete this department?")
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.deleteDepartment(department)
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
                    binding.rvDepartments.visibility = View.GONE
                } else {
                    binding.ivError.visibility = View.GONE
                    binding.rvDepartments.visibility = View.VISIBLE
                }
            }
        }
        binding.rvDepartments.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetFlow()
    }
}