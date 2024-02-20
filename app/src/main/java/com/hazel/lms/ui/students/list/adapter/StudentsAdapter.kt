package com.hazel.lms.ui.students.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hazel.lms.data.beans.Student
import com.hazel.lms.databinding.LayoutItemDeptBinding
import timber.log.Timber
import java.io.File

class StudentsAdapter (
    private val onItemClicked: (Student) -> Unit,
    private val onItemEditClicked: (Student) -> Unit,
    private val onItemDeleteClicked: (Student) -> Unit
) : PagingDataAdapter<Student, StudentsAdapter.ViewHolder>(DiffUtilCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutItemDeptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Timber.e("Position: $position")
        holder.bind(getItem(position)!!)
    }

    inner class ViewHolder(private val binding: LayoutItemDeptBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(student: Student){
            binding.apply {
                tvDepartment.text = student.name
                ivDepartment.load(File(student.image))
                root.setOnClickListener {
                    onItemClicked(student)
                }
                ibEdit.setOnClickListener {
                    onItemEditClicked(student)
                }
                ibDelete.setOnClickListener {
                    onItemDeleteClicked(student)
                }
            }
        }
    }

    object DiffUtilCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem == newItem
        }
    }
}