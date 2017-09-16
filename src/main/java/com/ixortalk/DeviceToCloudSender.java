package com.ixortalk;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * Device to cloud scenario where device is sending data to IoT HUB
 *
 */
public class DeviceToCloudSender {

    private static DeviceClient client;

    public static void main( String[] args ) throws IOException, URISyntaxException {

        String connString = args[0];
        String deviceId = args[1];

        client = new DeviceClient(connString, IotHubClientProtocol.MQTT);
        client.open();

        HumidityMessageSender sender = new HumidityMessageSender(deviceId);

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(sender);

        System.out.println("Press ENTER to exit.");
        System.in.read();
        executor.shutdownNow();
        client.closeNow();
    }

    private static class TelemetryDataPoint {
        public String deviceId;
        public double temperature;
        public double humidity;

        public String serialize() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }

    private static class EventCallback implements IotHubEventCallback {
        public void execute(IotHubStatusCode status, Object context) {
            System.out.println("IoT Hub responded to message with status: " + status.name());

            if (context != null) {
                synchronized (context) {
                    context.notify();
                }
            }
        }
    }

    private static class HumidityMessageSender implements Runnable {

        private String deviceId;

        public HumidityMessageSender(String deviceId) {

            this.deviceId = deviceId;
        }

        public void run()  {
            try {
                double minTemperature = 20;
                double minHumidity = 60;
                Random rand = new Random();

                while (true) {
                    double currentTemperature = minTemperature + rand.nextDouble() * 15;
                    double currentHumidity = minHumidity + rand.nextDouble() * 20;
                    TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();
                    telemetryDataPoint.deviceId = deviceId;
                    telemetryDataPoint.temperature = currentTemperature;
                    telemetryDataPoint.humidity = currentHumidity;

//                    String msgStr = telemetryDataPoint.serialize();
//                    Message msg = new Message(msgStr);
//                        msg.setProperty("temperatureAlert", (currentTemperature > 30) ? "true" : "false");
//                    msg.setMessageId(java.util.UUID.randomUUID().toString());

                    Message msg = new Message("{\"deviceId\":\"Feather HUZZAH ESP8266 WiFi\",\"messageId\":1,\"temperature\":21,\"humidity\":36}");
//                    System.out.println("Sending: " + msgStr);

                    Object lockobj = new Object();
                    EventCallback callback = new EventCallback();
                    client.sendEventAsync(msg, callback, lockobj);

                    synchronized (lockobj) {
                        lockobj.wait();
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("Finished.");
            }
        }
    }

}
