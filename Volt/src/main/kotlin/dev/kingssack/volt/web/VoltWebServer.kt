package dev.kingssack.volt.web

import android.util.Log
import com.qualcomm.robotcore.util.WebHandlerManager
import fi.iki.elonen.NanoHTTPD
import org.firstinspires.ftc.robotcore.internal.webserver.WebHandler

/**
 * Utility class for interacting with the FTC web server.
 * 
 * This class provides methods for getting the URL of the web server
 * and for adding custom handlers.
 */
class VoltWebServer {
    companion object {
        private const val TAG = "VoltWebServer"

        /**
         * Get the base URL for the Volt web server.
         * 
         * @return The base URL for the Volt web server (e.g., "http://192.168.43.1:8080/volt")
         */
        @JvmStatic
        fun getBaseUrl(): String {
            // The FTC Robot Controller app runs on port 8080
            return "http://192.168.43.1:8080/volt"
        }

        /**
         * Register a custom handler for a specific path.
         * 
         * @param manager The WebHandlerManager to register the handler with
         * @param path The path to register the handler for (e.g., "/custom")
         * @param handler The handler to register
         */
        @JvmStatic
        fun registerHandler(manager: WebHandlerManager, path: String, handler: WebHandler) {
            Log.d(TAG, "Registering custom handler for path: $path")
            manager.register("/volt$path", handler)
        }

        /**
         * Create a simple handler that returns a fixed response.
         * 
         * @param contentType The content type of the response (e.g., "text/html")
         * @param content The content of the response
         * @return A WebHandler that returns the specified content
         */
        @JvmStatic
        fun createSimpleHandler(contentType: String, content: String): WebHandler {
            return object : WebHandler {
                override fun getResponse(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
                    return NanoHTTPD.newFixedLengthResponse(
                        NanoHTTPD.Response.Status.OK,
                        contentType,
                        content
                    )
                }
            }
        }
    }
}
