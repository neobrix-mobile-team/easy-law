package com.easylaw.app.ui.screen.map

import com.easylaw.app.domain.model.LawPlace
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

data class PlaceCluster(
    val places: List<LawPlace>,
    val centerLat: Double,
    val centerLng: Double,
) {
    val isSingle: Boolean get() = places.size == 1
    val representative: LawPlace get() = places.first()
    val count: Int get() = places.size
}

private fun clusterRadiusByZoom(zoom: Double): Double =
    when {
        zoom >= 16.0 -> 0.0
        zoom >= 14.0 -> 0.003
        zoom >= 12.0 -> 0.010
        zoom >= 10.0 -> 0.030
        zoom >= 8.0 -> 0.100
        else -> 0.300
    }

private fun approxDistance(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double,
): Double {
    val dLat = lat1 - lat2
    val dLng = (lng1 - lng2) * cos(Math.toRadians((lat1 + lat2) / 2))
    return sqrt(dLat.pow(2) + dLng.pow(2))
}

fun clusterPlaces(
    places: List<LawPlace>,
    zoom: Double,
): List<PlaceCluster> {
    val radius = clusterRadiusByZoom(zoom)

    // 줌이 충분히 크면 클러스터링 없이 개별 반환
    if (radius == 0.0) {
        return places.map { PlaceCluster(listOf(it), it.lat, it.lng) }
    }

    val remaining = places.toMutableList()
    val clusters = mutableListOf<PlaceCluster>()

    while (remaining.isNotEmpty()) {
        val seed = remaining.removeAt(0)
        val group = mutableListOf(seed)

        val iterator = remaining.iterator()
        while (iterator.hasNext()) {
            val candidate = iterator.next()
            val dist = approxDistance(seed.lat, seed.lng, candidate.lat, candidate.lng)
            if (dist <= radius) {
                group.add(candidate)
                iterator.remove()
            }
        }

        // 클러스터 중심 = 포함된 마커들의 평균 좌표
        val centerLat = group.sumOf { it.lat } / group.size
        val centerLng = group.sumOf { it.lng } / group.size
        clusters.add(PlaceCluster(group, centerLat, centerLng))
    }

    return clusters
}
