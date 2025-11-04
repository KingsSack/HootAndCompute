package dev.kingssack.volt

import android.content.Context
import android.util.Log
import com.qualcomm.robotcore.util.WebHandlerManager
//import dev.kingssack.volt.web.ModeCreatorHandler
import dev.kingssack.volt.web.StaticAssetHandler
import org.firstinspires.ftc.ftccommon.external.WebHandlerRegistrar
import org.firstinspires.ftc.robotcore.internal.system.AppUtil
import java.io.IOException

class Volt {
    companion object {
        private const val TAG = "VoltWebServer"

        @JvmStatic
        @WebHandlerRegistrar
        fun attachWebServer(context: Context?, manager: WebHandlerManager) {
            Log.d(TAG, "Attaching web server handlers")
            val activity = AppUtil.getInstance().activity ?: return
            val assetManager = activity.assets

            // Register all static files from the public directory
            try {
                val publicFiles = assetManager.list("public")
                Log.d(TAG, "Found ${publicFiles?.size ?: 0} files in public directory")

                publicFiles?.forEach { file ->
                    val path = "public/$file"
                    Log.d(TAG, "Processing file: $path")

                    if (assetManager.list(path)?.isNotEmpty() == true) {
                        // Handle subdirectories
                        Log.d(TAG, "$path is a directory, adding handlers recursively")
                        addAssetHandlers(manager, assetManager, path)
                    } else {
                        // Register individual files
                        Log.d(TAG, "Registering handler for /volt/$file")
                        manager.register("/volt/$file", StaticAssetHandler(assetManager, path))
                    }
                }
                // Register index.html as the root
                Log.d(TAG, "Registering handler for /volt (index.html)")
                manager.register("/volt", StaticAssetHandler(assetManager, "public/index.html"))

                // Register the mode creator handler
//                Log.d(TAG, "Registering handler for /volt/api/*")
//                manager.register("/volt/api", ModeCreatorHandler())

                Log.d(TAG, "Web server handlers attached successfully")
            } catch (e: IOException) {
                // Log error if files can't be accessed
                Log.e(TAG, "Error attaching web server handlers", e)
                e.printStackTrace()
            }
        }

        private fun addAssetHandlers(
            manager: WebHandlerManager,
            assetManager: android.content.res.AssetManager,
            path: String
        ) {
            Log.d(TAG, "Adding asset handlers for directory: $path")
            val files = assetManager.list(path)
            Log.d(TAG, "Found ${files?.size ?: 0} files in directory: $path")

            files?.forEach { file ->
                val fullPath = "$path/$file"
                val webPath = fullPath.replace("public/", "")
                Log.d(TAG, "Processing file in directory: $fullPath, web path: $webPath")

                if (assetManager.list(fullPath)?.isNotEmpty() == true) {
                    Log.d(TAG, "$fullPath is a subdirectory, adding handlers recursively")
                    addAssetHandlers(manager, assetManager, fullPath)
                } else {
                    Log.d(TAG, "Registering handler for /volt/$webPath")
                    manager.register("/volt/$webPath", StaticAssetHandler(assetManager, fullPath))
                }
            }
            Log.d(TAG, "Finished adding asset handlers for directory: $path")
        }
    }
}
