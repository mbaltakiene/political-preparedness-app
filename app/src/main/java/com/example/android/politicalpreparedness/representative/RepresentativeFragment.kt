package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

private const val MOTION_LAYOUT_STATE = "bundleMotionLayoutState"

class DetailFragment : Fragment() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { _ ->
        if (isPermissionGranted() || (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION))) {
            getLocation()
        } else {
            // Location permission denied.
            Snackbar.make(
                binding.motionLayout,
                R.string.location_not_available,
                Snackbar.LENGTH_INDEFINITE
            ).show()
        }
    }

    private lateinit var viewModel: RepresentativeViewModel
    private lateinit var adapter: RepresentativeListAdapter
    private lateinit var binding: FragmentRepresentativeBinding
    private var address = Address("", "", "", "", "")

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment.
        binding = FragmentRepresentativeBinding.inflate(inflater)

        // Restore MotionLayout state
        savedInstanceState?.let{
            val state = savedInstanceState.getInt(MOTION_LAYOUT_STATE)
            binding.motionLayout.transitionToState(state)
        }

        // Get view model
        viewModel = ViewModelProvider(this)[RepresentativeViewModel::class.java]

        // Establish bindings
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Define and assign Representative adapter
        adapter = RepresentativeListAdapter()
        binding.representativeRecyclerView.adapter = adapter



        // Establish button listeners for field and location search
        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            viewModel.getAddressFromInput(
                binding.addressLine1.text.toString(),
                binding.addressLine2.text.toString(),
                binding.city.text.toString(),
                binding.state.selectedItem as String,
                binding.zip.text.toString()
            )
            viewModel.fetchRepresentatives()
        }
        binding.buttonLocation.setOnClickListener {
            hideKeyboard()
            checkLocationPermissions()
        }

        // Restore edited fields in case the fragment is recreated.
        binding.executePendingBindings()

        return binding.root
    }

    private fun checkLocationPermissions() {
        if (isPermissionGranted()) {
            getLocation()
        }
        else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
            || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {

                val snackbar = Snackbar.make(binding.motionLayout,
                    R.string.location_permission_required, Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction(R.string.OK) {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
                snackbar.show()
        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun isPermissionGranted() : Boolean {
        return ActivityCompat.checkSelfPermission( requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission( requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                geoCodeLocation(location)
            } ?: run {
                // Unable to get location.
                Toast.makeText(
                    requireContext(), getString(R.string.unable_get_location),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(MOTION_LAYOUT_STATE, binding.motionLayout.currentState)
    }

    private fun geoCodeLocation(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation( location.latitude, location.longitude, 1,  object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<android.location.Address>) {
                    address = addresses.map{ addr ->
                        Address(addr.thoroughfare, addr.subThoroughfare, addr.locality, addr.adminArea, addr.postalCode)
                    }.first()
                    viewModel.getAddressFromLocation(address)
                    viewModel.fetchRepresentatives()
                }
            })
        } else {
            // For Android SDK < 33, obtain the addresses from deprecated getFromLocation() method
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            when {
                addresses!!.isNotEmpty() -> {
                    address = addresses.map{ addr ->
                        Address(addr.thoroughfare, addr.subThoroughfare, addr.locality, addr.adminArea, addr.postalCode)
                    }.first()
                    viewModel.getAddressFromLocation(address)
                    viewModel.fetchRepresentatives()
                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }


}