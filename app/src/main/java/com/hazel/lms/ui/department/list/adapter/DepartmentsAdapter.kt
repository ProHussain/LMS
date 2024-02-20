package com.hazel.lms.ui.department.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hazel.lms.data.beans.Department
import com.hazel.lms.databinding.LayoutItemDeptBinding
import java.io.File

class DepartmentsAdapter (
    private val onItemClicked: (Department) -> Unit,
    private val onItemEditClicked: (Department) -> Unit,
    private val onItemDeleteClicked: (Department) -> Unit
) : PagingDataAdapter<Department, DepartmentsAdapter.ViewHolder>(DiffUtilCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutItemDeptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         holder.bind(getItem(position)!!)
    }

    inner class ViewHolder(private val binding: LayoutItemDeptBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(department: Department){
            binding.apply {
                tvDepartment.text = department.name
                ivDepartment.load(File(department.image))
                root.setOnClickListener {
                    onItemClicked(department)
                }
                ibEdit.setOnClickListener {
                    onItemEditClicked(department)
                }
                ibDelete.setOnClickListener {
                    onItemDeleteClicked(department)
                }
            }
        }
    }

    object DiffUtilCallback : DiffUtil.ItemCallback<Department>() {
        override fun areItemsTheSame(oldItem: Department, newItem: Department): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Department, newItem: Department): Boolean {
            return oldItem == newItem
        }
    }
}