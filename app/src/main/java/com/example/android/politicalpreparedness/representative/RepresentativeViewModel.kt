package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch
import timber.log.Timber

private const val LIST_KEY = "representativeListKey"

class RepresentativeViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {

    // Api.
    private val api = CivicsApi.retrofitService

    // Live data val for representatives
    private var _representatives: MutableLiveData<List<Representative>?> =
        savedStateHandle.getLiveData(LIST_KEY)
    val representatives: LiveData<List<Representative>?>
        get() = _representatives

    // Live data val for address
    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    // Function to get representatives with address
    fun fetchRepresentatives(){
        if (_representatives.value != null) {
            _representatives.postValue(null)
        }
        _address.value?.let {
            viewModelScope.launch {
                try {
                    val representativeResponse = api.getRepresentatives(it.toSearchString())
                    val offices = representativeResponse.offices
                    val officials = representativeResponse.officials
                    _representatives.value = offices.flatMap { it.getRepresentatives(officials) }
                } catch (e: Exception) {
                    _representatives.postValue(null)
                    Timber.tag("RepresentativeViewModel")
                        .e(e, "Error retrieving representatives")
                } finally {
                    setQuery(_representatives.value)
                }
            }
        }
    }

    // Save representative list to view model state handle
    private fun setQuery(list: List<Representative>?) {
        savedStateHandle[LIST_KEY] = list
    }

    // Get address from input fields.
    fun getAddressFromInput(line1: String, line2: String, city: String, state: String, zip: String){
        val address = Address(line1, line2, city, state, zip)
        _address.value = address
    }

    // Get address from my location.
    fun getAddressFromLocation(address: Address){
        _address.postValue(address)
    }

}
