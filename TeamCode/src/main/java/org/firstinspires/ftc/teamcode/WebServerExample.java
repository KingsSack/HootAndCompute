package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.WebHandlerManager;
import dev.kingssack.volt.web.VoltWebServer;
import org.firstinspires.ftc.robotcore.internal.webserver.WebHandler;

@TeleOp(name = "Web Server Example")
public class WebServerExample extends OpMode {
    @Override
    public void init() {
        // Get the WebHandlerManager from the hardwareMap
        WebHandlerManager webHandlerManager = hardwareMap.appContext.getSystemService(WebHandlerManager.class);

        // Create a simple handler that returns a fixed response
        WebHandler handler = VoltWebServer.createSimpleHandler("text/html", "<h1>Hello from OpMode!</h1>");

        // Register the handler for the path "/opmode"
        VoltWebServer.registerHandler(webHandlerManager, "/opmode", handler);

        telemetry.addData("Web Server", "Handlers registered");
        telemetry.addData("URL", VoltWebServer.getBaseUrl() + "/opmode");
        telemetry.update();
    }

    @Override
    public void loop() {
        // Nothing to do here
    }
}