package dev.kingssack.volt.web

import android.content.res.AssetManager
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.firstinspires.ftc.robotcore.internal.webserver.WebHandler
import java.io.IOException
import java.io.InputStream

class StaticAssetHandler(
    private val assetManager: AssetManager,
    private val assetPath: String
) : WebHandler {
    override fun getResponse(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        return try {
            val inputStream: InputStream = assetManager.open(assetPath)
            val mimeType = getMimeType(assetPath)
            NanoHTTPD.newChunkedResponse(
                NanoHTTPD.Response.Status.OK,
                mimeType,
                inputStream
            )
        } catch (e: IOException) {
            Log.e("StaticAssetHandler", "Failed to serve asset: $assetPath", e)
            NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.NOT_FOUND,
                NanoHTTPD.MIME_PLAINTEXT,
                "File not found"
            )
        }
    }

    private fun getMimeType(path: String): String = when {
        path.endsWith(".html") -> "text/html"
        path.endsWith(".css") -> "text/css"
        path.endsWith(".js") -> "application/javascript"
        path.endsWith(".json") -> "application/json"
        path.endsWith(".png") -> "image/png"
        path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
        path.endsWith(".svg") -> "image/svg+xml"
        path.endsWith(".ico") -> "image/x-icon"
        else -> "application/octet-stream"
    }
}