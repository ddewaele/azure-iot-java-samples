package com.ixortalk;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.MessageProperty;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.ixortalk.Constants.DEVICE_CONNECTIONSTRING;

/**
 *  To be used on the device.
 * Handles messages from an IoT Hub. Default protocol is to use MQTT transport.
 */
public class CloudToDeviceReceiver {
    private static List failedMessageListOnClose = new ArrayList(); // List of messages that failed on close

    /**
     * Used as a counter in the message callback.
     */
    protected static class Counter {
        protected int num;

        public Counter(int num) {
            this.num = num;
        }

        public int get() {
            return this.num;
        }

        public void increment() {
            this.num++;
        }

        @Override
        public String toString() {
            return Integer.toString(this.num);
        }
    }


    // Our MQTT doesn't support abandon/reject, so we will only display the messaged received
    // from IoTHub and return COMPLETE
    protected static class MessageCallbackMqtt implements MessageCallback {
        public IotHubMessageResult execute(Message msg, Object context) {
            Counter counter = (Counter) context;
            System.out.println(
                    "Received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            for (MessageProperty messageProperty : msg.getProperties()) {
                System.out.println(messageProperty.getName() + " : " + messageProperty.getValue());
            }

            counter.increment();

            return IotHubMessageResult.COMPLETE;
        }
    }

    public static void main(String[] args)
            throws IOException, URISyntaxException {
        String connString = args.length > 0 ? args[0] : DEVICE_CONNECTIONSTRING;

        DeviceClient client = new DeviceClient(connString, IotHubClientProtocol.MQTT);

        System.out.println("Successfully created an IoT Hub client.");

        MessageCallbackMqtt callback = new MessageCallbackMqtt();
        Counter counter = new Counter(0);
        client.setMessageCallback(callback, counter);

        // Set your token expiry time limit here
        long time = 2400;
        client.setOption("SetSASTokenExpiryTime", time);

        client.open();

        System.out.println("Opened connection to IoT Hub.");
        System.out.println("In receive mode. Waiting for receiving C2D messages. Press ENTER to close");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        // close the connection
        System.out.println("Closing");
        client.close();

        if (!failedMessageListOnClose.isEmpty()) {
            System.out.println("List of messages that were cancelled on close:" + failedMessageListOnClose.toString());
        }

        System.out.println("Shutting down...");
    }
}
