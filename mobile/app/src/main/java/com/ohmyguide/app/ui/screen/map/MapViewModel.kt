package com.ohmyguide.app.ui.screen.map

import androidx.lifecycle.ViewModel
import com.ohmyguide.app.service.LocationData
import com.ohmyguide.app.service.LocationForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    val locationState: StateFlow<LocationData?> = LocationForegroundService.locationFlow
}