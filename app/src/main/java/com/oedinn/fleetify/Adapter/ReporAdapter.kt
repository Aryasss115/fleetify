package com.oedinn.fleetify.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.oedinn.fleetify.Model.Report
import com.oedinn.fleetify.databinding.ItemReportBinding
import com.oedinn.fleetify.service.DateUtils

class ReportAdapter : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    private var reportList = listOf<Report>()

    fun submitList(list: List<Report>) {
        reportList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reportList[position])
    }

    override fun getItemCount(): Int = reportList.size

    class ReportViewHolder(private val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(report: Report) {
            // Mengikat data ke tampilan menggunakan View Binding
            binding.reportId.text = report.reportId
            binding.idDate.text = report.createdAt
            binding.idSend.text = report.reportStatus
            binding.idVehicleName.text = report.vehicleName
            binding.idBy.text = report.createdBy
            binding.nmberVehicle.text = report.vehicleLicenseNumber
            binding.idNote.text = "Catatan Keluhan: \n${report.note}"
            // Jika ada foto yang ingin ditampilkan
            Glide.with(binding.root.context)
                .load(report.photo)
                .into(binding.imageViewPhoto) // Pastikan Anda memiliki ImageView dengan ID ini di item_report.xml
        }
    }
}
