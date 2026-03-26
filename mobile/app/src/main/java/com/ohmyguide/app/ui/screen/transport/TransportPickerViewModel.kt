package com.ohmyguide.app.ui.screen.transport

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.data.model.OdsayPath
import com.ohmyguide.app.data.repository.OdsayRepository
import com.ohmyguide.app.domain.model.PlaceDetailCache
import com.ohmyguide.app.domain.model.ThemeCourseCache
import com.ohmyguide.app.domain.usecase.GetBusArrivalUseCase
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.service.LocationForegroundService
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.ohmyguide.app.ui.theme.LanguageManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class TransportTimeInfo(
    val walkTime: String = "...",
    val walkEta: String = "",
    val walkMinutes: Int = 0,
    val transitTime: String = "...",
    val transitEta: String = "",
    val carTime: String = "...",
    val carEta: String = "",
)

data class TransitPreview(
    val busNo: String,
    val stationName: String,
    val arrivalMin: Int,
    val remainStops: Int,
    val totalTime: String,
)

@HiltViewModel
class TransportPickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val odsayRepository: OdsayRepository,
    private val getBusArrivalUseCase: GetBusArrivalUseCase,
) : ViewModel() {

    private val placeId: String = savedStateHandle["placeId"] ?: ""
    private val courseId: String? = savedStateHandle.get<String>("courseId")?.ifEmpty { null }

    private val _timeInfo = MutableStateFlow(TransportTimeInfo())
    val timeInfo: StateFlow<TransportTimeInfo> = _timeInfo.asStateFlow()

    private val _transitPreview = MutableStateFlow<TransitPreview?>(null)
    val transitPreview: StateFlow<TransitPreview?> = _transitPreview.asStateFlow()

    init {
        fetchTimes()
    }

    private fun fetchTimes() {
        // Try theme course first
        val themeCourse = courseId?.let { ThemeCourseCache.get(it) }
        val destLat: Double
        val destLng: Double

        if (themeCourse != null && themeCourse.spots.isNotEmpty()) {
            // For theme course: use last spot as destination for total time estimation
            val lastSpot = themeCourse.spots.last()
            destLat = lastSpot.lat
            destLng = lastSpot.lng
        } else {
            val place = PlaceDetailCache.get(placeId)?.place
                ?: SAMPLE_PLACE_DETAILS[placeId]?.place ?: return
            destLat = place.lat
            destLng = place.lng
        }
        if (destLat == 0.0 || destLng == 0.0) return

        viewModelScope.launch {
            val location = LocationForegroundService.locationFlow.value
                ?: kotlinx.coroutines.withTimeoutOrNull(5000L) {
                    LocationForegroundService.locationFlow
                        .filterNotNull().first()
                }
            val rawLat = location?.latitude
            val rawLng = location?.longitude
            val inKorea = rawLat != null && rawLng != null
                && rawLat in 33.0..39.0 && rawLng in 124.0..132.0
            val startLat = if (inKorea) rawLat!! else DEFAULT_LAT
            val startLng = if (inKorea) rawLng!! else DEFAULT_LNG

            val distMeters = haversineMeters(startLat, startLng, destLat, destLng)
            val walkMin = (distMeters / 80.0).roundToInt()
            val carMin = maxOf(1, (distMeters / 500.0).roundToInt())
            val now = LocalTime.now()
            val s = LanguageManager.current.value.strings
            val fmt = DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH)

            _timeInfo.update {
                it.copy(
                    walkTime = if (walkMin >= 200) s.notAvailable else "$walkMin ${s.minSuffix}",
                    walkEta = if (walkMin >= 200) "" else "${s.etaPrefix} ${now.plusMinutes(walkMin.toLong()).format(fmt)}",
                    walkMinutes = walkMin,
                    carTime = "$carMin ${s.minSuffix}",
                    carEta = "${s.etaPrefix} ${now.plusMinutes(carMin.toLong()).format(fmt)}",
                )
            }

            when (val result = odsayRepository.searchTransitPath(startLat, startLng, destLat, destLng)) {
                is ApiResult.Success -> {
                    val fastest = result.data.result?.path?.minByOrNull { it.info.totalTime }
                    if (fastest != null) {
                        val transitMin = fastest.info.totalTime
                        _timeInfo.update {
                            it.copy(
                                transitTime = "$transitMin ${s.minSuffix}",
                                transitEta = "${s.etaPrefix} ${now.plusMinutes(transitMin.toLong()).format(fmt)}",
                            )
                        }
                        fetchRealtimePreview(fastest)
                    } else {
                        _timeInfo.update { it.copy(transitTime = s.notAvailable, transitEta = "") }
                    }
                }
                is ApiResult.Error -> {
                    _timeInfo.update { it.copy(transitTime = s.notAvailable, transitEta = "") }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    private fun fetchRealtimePreview(fastest: OdsayPath) {
        val busSubPath = fastest.subPath.firstOrNull { it.trafficType == 2 } ?: return
        val busNo = busSubPath.lane?.firstOrNull()?.busNo?.trim() ?: return

        viewModelScope.launch {
            when (val r = getBusArrivalUseCase.execute(busSubPath)) {
                is ApiResult.Success -> {
                    _transitPreview.value = TransitPreview(
                        busNo = r.data.busNo,
                        stationName = r.data.stationName,
                        arrivalMin = r.data.min1,
                        remainStops = r.data.station1,
                        totalTime = _timeInfo.value.transitTime,
                    )
                }
                else -> {}
            }
        }
    }

    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    companion object {
        private const val DEFAULT_LAT = 35.0950
        private const val DEFAULT_LNG = 128.8560
    }
}