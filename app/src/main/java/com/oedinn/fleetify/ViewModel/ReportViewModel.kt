package com.oedinn.fleetify.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oedinn.fleetify.Model.LaporanData
import com.oedinn.fleetify.Model.Report
import com.oedinn.fleetify.Model.ReportRepository
import com.oedinn.fleetify.Model.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class ReportViewModel(private val repository: ReportRepository) : ViewModel() {

    private val _reports = MutableLiveData<List<Report>>()//viewModel to get data reports
    val reports: LiveData<List<Report>> = _reports

    private val _vehicleList = MutableLiveData<List<Vehicle>>() // MutableLiveData for vehicle list
    val vehicleList: LiveData<List<Vehicle>> = _vehicleList

    private val _uploadStatus = MutableLiveData<Response<Unit>>() // LiveData for the upload status
    val uploadStatus: LiveData<Response<Unit>> = _uploadStatus

    fun fetchVehicleList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val vehicles = repository.getVehicleList() // Fetch vehicle list from the repository
                _vehicleList.postValue(vehicles)
            } catch (e: Exception) {
                // Handle the error, e.g., show a message to the user
                _vehicleList.postValue(emptyList())
            }
        }
    }

    fun fetchReports(userId: String) {
        viewModelScope.launch {
            try {
                val response = repository.getAllReports(userId)
                _reports.value = response
            } catch (e: Exception) {
                // Handle the error, e.g., show a message to the user
                _reports.value = emptyList()
            }
        }
    }

    fun uploadReport(laporanData: LaporanData) {
        CoroutineScope(Dispatchers.IO).launch {
            // Call your repository method to upload the report
            repository.uploadLaporan(laporanData)
        }
    }
}