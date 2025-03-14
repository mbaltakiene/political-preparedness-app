package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionRepository
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

class ElectionsViewModel(private val repository: ElectionRepository): ViewModel(){

    // Api.
    private val api = CivicsApi.retrofitService

    // Live data val for upcoming elections
    private var _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
            get() = _upcomingElections

    //Live data val for saved elections
    private var _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    // Live date for loading status
    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    // Refresh data
    fun refreshData() {
        fetchUpcomingElections()
        fetchSavedElections()
    }

    // Fetch upcoming elections from api.
    private fun fetchUpcomingElections() {
        viewModelScope.launch {
            _loading.value = true
            val response = api.getElections()
            _upcomingElections.value = response.elections
            _loading.value = false
        }
    }

    // Fetch saved elections from database.
    private fun fetchSavedElections() {
        viewModelScope.launch {
            _savedElections.value = repository.getAllElections()
        }
    }

}