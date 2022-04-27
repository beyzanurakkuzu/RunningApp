package com.beyzaakkuzu.runningtracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.beyzaakkuzu.runningtracker.R
import com.beyzaakkuzu.runningtracker.databinding.FragmentTrackingBinding
import com.beyzaakkuzu.runningtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.beyzaakkuzu.runningtracker.services.TrackingService
import com.beyzaakkuzu.runningtracker.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.GoogleMap

class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private val vm: MainViewModel by viewModels()
    private var map:GoogleMap? = null

    private lateinit var binding: FragmentTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentTrackingBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
        binding.mapView.getMapAsync {
            map = it
        }
    }
    private fun sendCommandToService(action:String)=
        Intent(requireContext(),TrackingService::class.java).also {
            it.action=action
            requireContext().startService(it)
        }
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
         binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState  )

    }
}