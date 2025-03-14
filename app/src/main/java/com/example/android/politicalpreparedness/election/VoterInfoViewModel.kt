package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionRepository
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch
import timber.log.Timber

class VoterInfoViewModel(private val electionRepository: ElectionRepository) : ViewModel() {

    // Api.
    private val api = CivicsApi.retrofitService

    // Live data val election
    private var _election = MutableLiveData<Election>()
    val election: LiveData<Election>
        get() = _election

    // Live data to hold voter info
    private var _voterInfo = MutableLiveData<VoterInfoResponse?>()
    val voterInfo: LiveData<VoterInfoResponse?>
        get() = _voterInfo

    // Live date for loading status
    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    // Internal followed election status variable.
    private var _followedElection = MutableLiveData<Boolean>()
    val followedElection: LiveData<Boolean>
        get() = _followedElection

    // Live data for loading url
    private var _loadingUrl = MutableLiveData<String?>()
    val loadingUrl: LiveData<String?>
        get() = _loadingUrl


    // Set selected election
    fun setElection( election: Election ) {
        _election.value = election
    }

    // Populate voter info
    fun loadVoterInfo() {
        _election.value?.let {
            viewModelScope.launch {
                initSaveButtonAction()
                val address = "${it.division.state},${it.division.country}"
                try {
                    _loading.value = true
                    _voterInfo.value = api.getVoterInfo(address, it.id)
                } catch (e: Exception) {
                    _voterInfo.value = null
                    Timber.tag("VoterInfoViewModel").e( e, "Error retrieving voter info." )
                }
                finally {
                    _loading.value = false
                }
            }
        }
    }

    // Set loading URL for ballot information
    fun openBallotInformationUrl() {
        _voterInfo.value?.state?.let {
            _loadingUrl.value = it[0].electionAdministrationBody.ballotInfoUrl
        }
    }

    // Set loading URL for voting location
    fun openVotingLocationUrl() {
        _voterInfo.value?.state?.let {
            _loadingUrl.value = it[0].electionAdministrationBody.votingLocationFinderUrl
        }
    }

    // Populate initial state of save button to reflect proper action based on election saved status
    private suspend fun initSaveButtonAction() {
        _election.value?.let {
            val savedElection = electionRepository.getElectionById(it.id)
            _followedElection.value = savedElection != null
        }
    }

    // Handle add save or delete election from database.
    fun followOrUnfollowElection() {
        when (_followedElection.value) {
            true -> unfollowElection()
            false -> followElection()
            else -> Timber.tag("VoterInfoViewModel").e("Invalid action")
        }
    }

    // Remove election.
    private fun followElection() {
        _election.value?.let {
            viewModelScope.launch {
                electionRepository.insert(it)
                _followedElection.value = true
            }
        }
    }

    // Save election.
    private fun unfollowElection() {
        _election.value?.let {
            viewModelScope.launch {
                electionRepository.deleteElectionById(it.id)
                _followedElection.value = false
            }
        }
    }

}