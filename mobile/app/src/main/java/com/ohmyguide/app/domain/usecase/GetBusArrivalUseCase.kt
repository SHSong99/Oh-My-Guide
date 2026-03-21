package com.ohmyguide.app.domain.usecase

import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.data.model.OdsaySubPath
import com.ohmyguide.app.data.repository.BusanBimsRepository
import com.ohmyguide.app.domain.model.RealtimeArrival
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetBusArrivalUseCase @Inject constructor(
    private val bimsRepository: BusanBimsRepository,
) {
    companion object {
        private const val BUSAN_CITY_CODE = 7000
    }

    suspend fun execute(busSubPath: OdsaySubPath): ApiResult<RealtimeArrival> {
        val stations = busSubPath.passStopList?.stations
        if (stations.isNullOrEmpty()) {
            return ApiResult.Error(-1, "No station info in subPath")
        }

        val firstStation = stations.first()
        val cityCode = firstStation.stationCityCode
        if (cityCode != BUSAN_CITY_CODE) {
            return ApiResult.Error(-1, "Not Busan area (cityCode=$cityCode)")
        }

        val localStationID = firstStation.localStationID
        if (localStationID.isNullOrBlank()) {
            return ApiResult.Error(-1, "localStationID is missing")
        }

        val targetBusNo = busSubPath.lane?.firstOrNull()?.busNo?.trim()
        if (targetBusNo.isNullOrBlank()) {
            return ApiResult.Error(-1, "busNo is missing")
        }

        return when (val result = bimsRepository.getArrivalByStopId(localStationID)) {
            is ApiResult.Success -> {
                val matched = result.data.find { it.lineno.trim() == targetBusNo }
                if (matched != null) {
                    ApiResult.Success(
                        RealtimeArrival(
                            busNo = matched.lineno,
                            stationName = matched.nodenm,
                            min1 = matched.min1,
                            station1 = matched.station1,
                            min2 = matched.min2,
                            station2 = matched.station2,
                            busType = matched.bustype,
                        )
                    )
                } else {
                    ApiResult.Error(-1, "Bus $targetBusNo not found at stop $localStationID")
                }
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
}