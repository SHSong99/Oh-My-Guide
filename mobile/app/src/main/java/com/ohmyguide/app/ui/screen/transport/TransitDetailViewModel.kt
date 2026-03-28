package com.ohmyguide.app.ui.screen.transport

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.data.model.OdsayPath
import com.ohmyguide.app.data.model.OdsaySubPath
import com.ohmyguide.app.data.repository.OdsayRepository
import com.ohmyguide.app.domain.model.NaviRouteCache
import com.ohmyguide.app.domain.model.NaviRouteData
import com.ohmyguide.app.domain.model.RouteCoord
import com.ohmyguide.app.domain.model.RouteSegmentGeo
import com.ohmyguide.app.domain.usecase.GetBusArrivalUseCase
import com.ohmyguide.app.service.LocationForegroundService
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import androidx.compose.ui.graphics.Color
import com.ohmyguide.app.ui.theme.BusDefault
import com.ohmyguide.app.ui.theme.BusExpress
import com.ohmyguide.app.ui.theme.BusTown
import com.ohmyguide.app.ui.theme.InfoBlue
import com.ohmyguide.app.ui.theme.SubwayBGK
import com.ohmyguide.app.ui.theme.SubwayDonghae
import com.ohmyguide.app.ui.theme.SubwayLine1
import com.ohmyguide.app.ui.theme.SubwayLine2
import com.ohmyguide.app.ui.theme.SubwayLine3
import com.ohmyguide.app.ui.theme.SubwayLine4
import com.ohmyguide.app.ui.theme.TransitAmber
import com.ohmyguide.app.ui.theme.TransitGray
import com.ohmyguide.app.ui.theme.TransitWalk
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.util.KoreanRomanizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class TransitDetailUiState(
    val routes: List<TransitRoute> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class TransitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val odsayRepository: OdsayRepository,
    private val getBusArrivalUseCase: GetBusArrivalUseCase,
    private val naviRouteCache: NaviRouteCache,
) : ViewModel() {

    val placeId: String = savedStateHandle["placeId"] ?: ""
    private val destLat: Double = savedStateHandle.get<String>("destLat")?.toDoubleOrNull() ?: 0.0
    private val destLng: Double = savedStateHandle.get<String>("destLng")?.toDoubleOrNull() ?: 0.0

    private val _uiState = MutableStateFlow(TransitDetailUiState())
    val uiState: StateFlow<TransitDetailUiState> = _uiState.asStateFlow()

    private var cachedPaths: List<OdsayPath> = emptyList()

    companion object {
        private const val DEFAULT_LAT = 35.0950
        private const val DEFAULT_LNG = 128.8560
        private val TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        private val WON_FMT = NumberFormat.getNumberInstance(Locale.KOREA)

        fun busTypeColor(type: Int?): Color = when (type) {
            3 -> BusTown
            4, 5, 6, 14, 15 -> BusExpress
            else -> BusDefault
        }

        fun subwayLineName(rawName: String, code: Int?): String {
            // Code-based mapping
            when (code) {
                1, 31 -> return "Line 1"
                2, 32 -> return "Line 2"
                3, 33 -> return "Line 3"
                4, 34 -> return "Line 4"
                35 -> return "BGK Line"
                36 -> return "Donghae Line"
            }
            // Name-based fallback (e.g. "부산 1호선" → "Line 1")
            val lineNum = Regex("(\\d+)호선").find(rawName)?.groupValues?.get(1)
            if (lineNum != null) return "Line $lineNum"
            return KoreanRomanizer.romanize(rawName)
        }

        fun subwayLineColor(code: Int?): Color = when (code) {
            1, 31 -> SubwayLine1
            2, 32 -> SubwayLine2
            3, 33 -> SubwayLine3
            4, 34 -> SubwayLine4
            35 -> SubwayBGK
            36 -> SubwayDonghae
            else -> SubwayLine1
        }
    }

    init {
        searchTransitRoutes()
    }

    fun searchTransitRoutes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

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

            when (val result = odsayRepository.searchTransitPath(
                startLat = startLat,
                startLng = startLng,
                endLat = destLat,
                endLng = destLng,
            )) {
                is ApiResult.Success -> {
                    val paths = result.data.result?.path ?: emptyList()
                    cachedPaths = paths
                    val routes = paths.mapIndexed { index, path -> path.toTransitRoute(index) }
                    _uiState.update { it.copy(routes = routes, isLoading = false) }
                    fetchRealtimeArrivals(paths, routes)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    private fun fetchRealtimeArrivals(paths: List<OdsayPath>, routes: List<TransitRoute>) {
        viewModelScope.launch {
            val arrivalMap = mutableMapOf<String, Pair<Int, Int>>()

            val busSubPaths = paths.flatMapIndexed { pathIdx, path ->
                path.subPath.mapIndexedNotNull { subIdx, sub ->
                    if (sub.trafficType == 2) Triple(pathIdx, subIdx, sub) else null
                }
            }

            val results = busSubPaths.map { (pathIdx, subIdx, sub) ->
                async {
                    val key = "${pathIdx}_${subIdx}"
                    when (val r = getBusArrivalUseCase.execute(sub)) {
                        is ApiResult.Success -> key to (r.data.min1 to r.data.station1)
                        else -> null
                    }
                }
            }.awaitAll()

            results.filterNotNull().forEach { (key, pair) -> arrivalMap[key] = pair }

            if (arrivalMap.isEmpty()) return@launch

            val updatedRoutes = routes.mapIndexed { pathIdx, route ->
                var busSubIdx = 0
                route.copy(
                    segments = route.segments.map { segment ->
                        if (segment.type == "bus") {
                            val key = "${pathIdx}_${findOriginalSubIdx(paths[pathIdx], busSubIdx)}"
                            busSubIdx++
                            val arrival = arrivalMap[key]
                            if (arrival != null) {
                                segment.copy(
                                    realtimeMin = arrival.first,
                                    realtimeStations = arrival.second,
                                )
                            } else segment
                        } else segment
                    },
                )
            }
            _uiState.update { it.copy(routes = updatedRoutes) }
        }
    }

    private fun findOriginalSubIdx(path: OdsayPath, busOrdinal: Int): Int {
        var count = 0
        path.subPath.forEachIndexed { idx, sub ->
            if (sub.trafficType == 2) {
                if (count == busOrdinal) return idx
                count++
            }
        }
        return -1
    }

    private fun OdsayPath.toTransitRoute(index: Int): TransitRoute {
        val s = LanguageManager.current.value.strings
        val now = LocalTime.now()
        val arrival = now.plusMinutes(info.totalTime.toLong())
        val timeRange = "${now.format(TIME_FMT)} - ${arrival.format(TIME_FMT)}"
        val payment = "\u20A9${WON_FMT.format(info.payment)}"

        val totalTimeStr = if (info.totalTime >= 60) {
            "${info.totalTime / 60}${s.hourSuffix} ${info.totalTime % 60}${s.minSuffix}"
        } else {
            "${info.totalTime} ${s.minSuffix}"
        }

        val badge = when (pathType) {
            1 -> "SUBWAY"
            2 -> "BUS"
            3 -> "BUS+SUBWAY"
            else -> "TRANSIT"
        }
        val badgeColor = when (pathType) {
            1 -> SubwayLine1
            2 -> BusDefault
            3 -> TransitAmber
            else -> TransitGray
        }

        return TransitRoute(
            id = "route_$index",
            pathType = pathType,
            title = badge,
            badge = badge,
            badgeColor = badgeColor,
            totalTime = totalTimeStr,
            totalMinutes = info.totalTime,
            eta = "${s.etaPrefix} ${arrival.format(TIME_FMT)}",
            timeRange = timeRange,
            payment = payment,
            segments = subPath.map { it.toTransitSegment() },
        )
    }

    private fun OdsaySubPath.toTransitSegment(): TransitSegment {
        val s = LanguageManager.current.value.strings
        val type = when (trafficType) {
            1 -> "subway"
            2 -> "bus"
            else -> "walk"
        }
        val lineName = when (trafficType) {
            1 -> {
                val rawName = lane?.firstOrNull()?.name ?: s.subway
                val lineCode = lane?.firstOrNull()?.subwayCode
                subwayLineName(rawName, lineCode)
            }
            2 -> {
                val busNo = lane?.firstOrNull()?.busNo ?: ""
                "${s.busLabel} ${KoreanRomanizer.romanize(busNo)}"
            }
            else -> s.walk
        }
        val color = when (trafficType) {
            1 -> subwayLineColor(lane?.firstOrNull()?.subwayCode)
            2 -> busTypeColor(lane?.firstOrNull()?.busType)
            else -> TransitWalk
        }
        val durationStr = if (trafficType == 3) "~$sectionTime ${s.minWalkSuffix}" else "$sectionTime ${s.minSuffix}"
        val fromKr = startName ?: ""
        val toKr = endName ?: ""

        val coords = if (trafficType == 1 || trafficType == 2) {
            passStopList?.stations?.mapNotNull { station ->
                val lat = station.lat?.toDoubleOrNull()
                val lng = station.lng?.toDoubleOrNull()
                if (lat != null && lng != null) lat to lng else null
            } ?: emptyList()
        } else {
            listOfNotNull(
                if (startLat != null && startLng != null) startLat to startLng else null,
                if (endLat != null && endLng != null) endLat to endLng else null,
            )
        }

        return TransitSegment(
            type = type,
            lineName = lineName,
            color = color,
            from = KoreanRomanizer.romanize(fromKr),
            fromKr = fromKr,
            to = KoreanRomanizer.romanize(toKr),
            toKr = toKr,
            stops = stationCount ?: 0,
            duration = durationStr,
            sectionTime = sectionTime,
            coords = coords,
        )
    }

    fun selectRouteForNavi(routeId: String) {
        val route = _uiState.value.routes.find { it.id == routeId } ?: return
        val segments = route.segments.map { seg ->
            RouteSegmentGeo(
                type = seg.type,
                coords = seg.coords.map { RouteCoord(it.first, it.second) },
                color = seg.color,
                lineName = seg.lineName,
                fromName = seg.from,
                toName = seg.to,
                fromNameKr = seg.fromKr,
                toNameKr = seg.toKr,
                stopsCount = seg.stops,
            )
        }
        naviRouteCache.store(
            NaviRouteData(
                mode = "transit",
                segments = segments,
                totalDurationMin = route.totalMinutes,
            )
        )
    }
}