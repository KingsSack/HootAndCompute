package dev.kingssack.volt

import android.content.Context
import android.util.Log
import com.qualcomm.robotcore.util.WebHandlerManager
import dev.frozenmilk.sinister.sdk.apphooks.WebHandlerRegistrar
import dev.kingssack.volt.web.FlowEditorApiHandler
import dev.kingssack.volt.web.StaticAssetHandler
import dev.kingssack.volt.web.VoltWebServer
import java.io.IOException
import org.firstinspires.ftc.robotcore.internal.system.AppUtil

@Suppress("unused")
class Volt {
    object AppHook : WebHandlerRegistrar {
        private const val TAG = "VoltWebServer"

        override fun webHandlerRegistrar(context: Context, webHandlerManager: WebHandlerManager) {
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

                    if (isAssetDirectory(assetManager, path)) {
                        // Handle subdirectories
                        Log.d(TAG, "$path is a directory, adding handlers recursively")
                        addAssetHandlers(webHandlerManager, assetManager, path)
                    } else {
                        // Register individual files
                        Log.d(TAG, "Registering handler for /volt/$file")
                        webHandlerManager.register("/volt/$file", StaticAssetHandler(assetManager, path))
                    }
                }
                // Register index.html as the root
                Log.d(TAG, "Registering handler for /volt (index.html)")
                webHandlerManager.register("/volt", StaticAssetHandler(assetManager, "public/index.html"))

                // Register the flow editor API handler
                Log.d(TAG, "Registering handler for /volt/api/*")
                webHandlerManager.register("/volt/api/*", FlowEditorApiHandler())
                webHandlerManager.register("/volt/api/metadata", VoltWebServer.createMetadataHandler())

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
            path: String,
        ) {
            Log.d(TAG, "Adding asset handlers for directory: $path")
            val files = assetManager.list(path)
            Log.d(TAG, "Found ${files?.size ?: 0} files in directory: $path")

            files?.forEach { file ->
                val fullPath = "$path/$file"
                val webPath = fullPath.replace("public/", "")
                Log.d(TAG, "Processing file in directory: $fullPath, web path: $webPath")

                if (isAssetDirectory(assetManager, fullPath)) {
                    Log.d(TAG, "$fullPath is a subdirectory, adding handlers recursively")
                    addAssetHandlers(manager, assetManager, fullPath)
                } else {
                    Log.d(TAG, "Registering handler for /volt/$webPath")
                    manager.register("/volt/$webPath", StaticAssetHandler(assetManager, fullPath))
                }
            }
            Log.d(TAG, "Finished adding asset handlers for directory: $path")
        }

        /**
         * Reliably check if an asset path is a directory.
         *
         * This method tries to open the path as a file first. If it succeeds, it's a file. If it
         * fails with an IOException, we check if it has contents (making it a directory).
         *
         * This is more reliable than just checking assetManager.list() because list() can return
         * unexpected results for files on some Android versions.
         */
        private fun isAssetDirectory(
            assetManager: android.content.res.AssetManager,
            path: String,
        ): Boolean {
            return try {
                // Try to open as a file - if successful, it's a file, not a directory
                assetManager.open(path).close()
                false
            } catch (e: IOException) {
                // If we can't open it as a file, check if it has contents (directory)
                val contents = assetManager.list(path)
                !contents.isNullOrEmpty()
            }
        }
    }
}
