package com.ohmyguide.app.ui.screen.explore

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.BuildConfig
import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.data.repository.NaverDirectionsRepository
import com.ohmyguide.app.domain.model.NaviRouteData
import com.ohmyguide.app.domain.model.RouteCoord
import com.ohmyguide.app.domain.model.RouteSegmentGeo
import com.ohmyguide.app.fixtures.Course
import com.ohmyguide.app.fixtures.EXPLORE_COURSES
import com.ohmyguide.app.fixtures.Spot
import com.ohmyguide.app.service.LocationForegroundService
import com.ohmyguide.app.ui.theme.CourseLeg1
import com.ohmyguide.app.ui.theme.CourseLeg2
import com.ohmyguide.app.ui.theme.CourseLeg3
import com.ohmyguide.app.ui.theme.CourseLeg4
import com.ohmyguide.app.ui.theme.CourseLeg5
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val LEG_COLORS = listOf(CourseLeg1, CourseLeg2, CourseLeg3, CourseLeg4, CourseLeg5)

data class CourseNaviUiState(
    val currentSpotIndex: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class CourseNaviViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val directionsRepository: NaverDirectionsRepository,
) : ViewModel() {

    val courseId: String = savedStateHandle["courseId"] ?: ""
    val course: Course? = EXPLORE_COURSES.find { it.id == courseId }

    private val _routeData = MutableStateFlow<NaviRouteData?>(null)
    val routeData: StateFlow<NaviRouteData?> = _routeData.asStateFlow()

    private val _uiState = MutableStateFlow(CourseNaviUiState())
    val uiState: StateFlow<CourseNaviUiState> = _uiState.asStateFlow()

    init {
        fetchCourseRoute()
    }

    private fun fetchCourseRoute() {
        val spots = course?.spots ?: return
        if (spots.size < 2) return

        viewModelScope.launch {
            val location = LocationForegroundService.locationFlow.value
            val rawLat = location?.latitude
            val rawLng = location?.longitude
            val inKorea = rawLat != null && rawLng != null
                    && rawLat in 33.0..39.0 && rawLng in 124.0..132.0

            // Use first spot as start if GPS not in Korea
            val startLat = if (inKorea) rawLat!! else spots.first().lat
            val startLng = if (inKorea) rawLng!! else spots.first().lng

            // Build waypoints: intermediate spots (skip first if using GPS, skip last = goal)
            val allPoints = if (inKorea) {
                listOf(startLat to startLng) + spots.map { it.lat to it.lng }
            } else {
                spots.map { it.lat to it.lng }
            }

            val start = allPoints.first()
            val goal = allPoints.last()
            val waypoints = allPoints.drop(1).dropLast(1)

            // Try Naver Directions 5 with waypoints
            val result = if (waypoints.isNotEmpty()) {
                directionsRepository.getDrivingRouteWithWaypoints(
                    startLat = start.first,
                    startLng = start.second,
                    waypoints = waypoints,
                    endLat = goal.first,
                    endLng = goal.second,
                )
            } else {
                directionsRepository.getDrivingRoute(
                    startLat = start.first,
                    startLng = start.second,
                    endLat = goal.first,
                    endLng = goal.second,
                )
            }

            when (result) {
                is ApiResult.Success -> {
                    if (BuildConfig.DEBUG) {
                        Log.d("CourseNaviVM", "Route OK: ${result.data.size} coords")
                    }
                    buildRouteSegments(result.data, allPoints)
                }
                is ApiResult.Error -> {
                    if (BuildConfig.DEBUG) {
                        Log.e("CourseNaviVM", "Route FAIL: ${result.message}")
                    }
                    // Fallback: straight lines between spots
                    buildFallbackRoute(allPoints)
                }
                else -> {}
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun buildRouteSegments(
        allCoords: List<RouteCoord>,
        waypoints: List<Pair<Double, Double>>,
    ) {
        // Single path from API — split into segments by nearest waypoint
        val segments = mutableListOf<RouteSegmentGeo>()
        val spots = course?.spots ?: return

        if (waypoints.size <= 2) {
            // Simple A→B, one segment
            segments.add(
                RouteSegmentGeo(
                    type = "car",
                    coords = allCoords,
                    color = LEG_COLORS[0],
                    lineName = "${spots.first().name} → ${spots.last().name}",
                    fromName = spots.first().name,
                    toName = spots.last().name,
                )
            )
        } else {
            // Split path by finding closest point to each waypoint
            val splitIndices = mutableListOf(0)
            for (wpIdx in 1 until waypoints.size - 1) {
                val (wpLat, wpLng) = waypoints[wpIdx]
                var minDist = Double.MAX_VALUE
                var minIdx = splitIndices.last()
                for (i in splitIndices.last() until allCoords.size) {
                    val d = sqDist(allCoords[i].lat, allCoords[i].lng, wpLat, wpLng)
                    if (d < minDist) {
                        minDist = d
                        minIdx = i
                    }
                }
                splitIndices.add(minIdx)
            }
            splitIndices.add(allCoords.size - 1)

            for (i in 0 until splitIndices.size - 1) {
                val from = splitIndices[i]
                val to = splitIndices[i + 1] + 1
                val segCoords = allCoords.subList(from.coerceAtMost(allCoords.size - 1), to.coerceAtMost(allCoords.size))
                if (segCoords.size >= 2) {
                    val spotIdx = i.coerceAtMost(spots.size - 1)
                    val nextSpotIdx = (i + 1).coerceAtMost(spots.size - 1)
                    segments.add(
                        RouteSegmentGeo(
                            type = "car",
                            coords = segCoords,
                            color = LEG_COLORS[i % LEG_COLORS.size],
                            lineName = "Leg ${i + 1}",
                            fromName = spots[spotIdx].name,
                            toName = spots[nextSpotIdx].name,
                        )
                    )
                }
            }
        }

        _routeData.value = NaviRouteData(
            mode = "car",
            segments = segments,
            totalDurationMin = 0,
        )
    }

    private fun buildFallbackRoute(points: List<Pair<Double, Double>>) {
        val spots = course?.spots ?: return
        val segments = mutableListOf<RouteSegmentGeo>()

        for (i in 0 until points.size - 1) {
            val spotIdx = i.coerceAtMost(spots.size - 1)
            val nextSpotIdx = (i + 1).coerceAtMost(spots.size - 1)
            segments.add(
                RouteSegmentGeo(
                    type = "car",
                    coords = listOf(
                        RouteCoord(points[i].first, points[i].second),
                        RouteCoord(points[i + 1].first, points[i + 1].second),
                    ),
                    color = LEG_COLORS[i % LEG_COLORS.size],
                    lineName = "Leg ${i + 1}",
                    fromName = spots[spotIdx].name,
                    toName = spots[nextSpotIdx].name,
                )
            )
        }

        _routeData.value = NaviRouteData(
            mode = "car",
            segments = segments,
            totalDurationMin = 0,
        )
    }

    fun selectSpot(index: Int) {
        _uiState.update { it.copy(currentSpotIndex = index) }
    }

    private fun sqDist(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = lat1 - lat2
        val dLng = lng1 - lng2
        return dLat * dLat + dLng * dLng
    }
}