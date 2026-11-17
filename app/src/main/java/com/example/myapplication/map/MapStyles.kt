package com.example.myapplication.map

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

object MapStyles {
    // Standard unblocked Google Maps tile layer for crisp, modern street maps
    val GOOGLE_MAPS = object : OnlineTileSourceBase(
        "GoogleMaps",
        0, 19, 256, ".png",
        arrayOf(
            "https://mt0.google.com/vt/lyrs=m",
            "https://mt1.google.com/vt/lyrs=m",
            "https://mt2.google.com/vt/lyrs=m",
            "https://mt3.google.com/vt/lyrs=m"
        )
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            val z = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            return "$baseUrl&x=$x&y=$y&z=$z"
        }
    }
}
