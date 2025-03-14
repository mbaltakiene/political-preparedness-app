package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding

class VoterInfoFragment : Fragment() {

    // Add ViewModel values and create ViewModel
    private val viewModel: VoterInfoViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(this,
            VoterInfoViewModelFactory(activity.application))[VoterInfoViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
    : View {

        // Add binding values
        val binding = FragmentVoterInfoBinding.inflate(inflater)
        binding.lifecycleOwner = this

        val election = VoterInfoFragmentArgs.fromBundle(requireArguments()).election
        viewModel.setElection( election )

        binding.viewModel = viewModel

        // Populate voter info -- hide views without provided data.
        viewModel.loadVoterInfo()
        viewModel.voterInfo.observe(viewLifecycleOwner, Observer { voterInfo ->
            if (voterInfo == null) {
                binding.stateLocations.visibility = View.GONE
                binding.stateBallot.visibility = View.GONE
                binding.addressGroup.visibility = View.GONE
                return@Observer
            }
            voterInfo.state?.let { states ->
                binding.stateLocations.visibility =
                    if (states[0].electionAdministrationBody.votingLocationFinderUrl != null)
                        View.VISIBLE else View.GONE
                binding.stateBallot.visibility =
                    if (states[0].electionAdministrationBody.ballotInfoUrl != null)
                        View.VISIBLE else View.GONE
                binding.addressGroup.visibility =
                    if (states[0].electionAdministrationBody.correspondenceAddress != null)
                        View.VISIBLE else View.GONE
            }
        })

        // Handle loading of URLs
        viewModel.loadingUrl.observe(viewLifecycleOwner) { url ->
            openIntent(url)
        }

        return binding.root
    }

    // Method to load URL intents
    private fun openIntent(url: String?) {
        if (url != null) {
            startActivity(Intent( Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}