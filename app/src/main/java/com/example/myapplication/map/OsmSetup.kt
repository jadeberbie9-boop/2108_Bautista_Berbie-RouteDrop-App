package com.example.myapplication.map

import android.content.Context
import org.osmdroid.config.Configuration
import java.io.File

object OsmSetup {
    fun init(context: Context) {
        val appContext = context.applicationContext
        val config = Configuration.getInstance()

        // 1. Load existing preferences FIRST
        config.load(appContext, appContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        // 2. Set custom User-Agent
        config.userAgentValue = "RouteDrop/1.0 (donnamaepbautista@gmail.com)"

        // 3. Clear ALL old corrupted tile caches (both internal and external)
        val oldTileCache = File(appContext.cacheDir, "osmdroid/tiles")
        if (oldTileCache.exists()) oldTileCache.deleteRecursively()
        
        val extCache = File(appContext.getExternalFilesDir(null), "osmdroid")
        if (extCache.exists()) extCache.deleteRecursively()
        
        val oldV2 = File(appContext.cacheDir, "osmdroid/tiles_v2")
        if (oldV2.exists()) oldV2.deleteRecursively()

        // 4. Assign our paths (using a fresh v3 tile cache path to force redownloads)
        config.osmdroidBasePath = File(appContext.cacheDir, "osmdroid")
        config.osmdroidTileCache = File(appContext.cacheDir, "osmdroid/tiles_v3")

        // 5. Save the configuration
        config.save(appContext, appContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    }
}
