package com.ixortalk;

import com.microsoft.azure.sdk.iot.service.DeliveryAcknowledgement;
import com.microsoft.azure.sdk.iot.service.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.FeedbackReceiver;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.ServiceClient;

import java.util.HashMap;
import java.util.Map;

import static com.ixortalk.Constants.DEVICE_ID;
import static com.ixortalk.Constants.IOT_HUB_CONNECTIONSTRING;

public class CloudToDeviceSender {

    private static ServiceClient serviceClient;

    private static String expectedCorrelationId = "1234";
    private static String expectedMessageId = "5678";

    private static final String PAYLOAD = "{\"Name\" : \"TurnFanOn\", \"Parameters\" : {}}";

    public static void main( String[] args ) throws Exception {

        String connString = args.length > 0 ? args[0] : IOT_HUB_CONNECTIONSTRING;
        String deviceId = args.length > 1 ? args[1] : DEVICE_ID;

        serviceClient = ServiceClient.createFromConnectionString(connString, IotHubServiceClientProtocol.AMQPS);
        serviceClient.open();

        FeedbackReceiver feedbackReceiver = serviceClient.getFeedbackReceiver();
        feedbackReceiver.open();

        Map<String, String> messageProperties = new HashMap<>(3);
        messageProperties.put("name1", "value1");
        messageProperties.put("name2", "value2");
        messageProperties.put("name3", "value3");

        Message serviceMessage = new Message(PAYLOAD);
        serviceMessage.setDeliveryAcknowledgement(DeliveryAcknowledgement.Full);
        serviceMessage.setCorrelationId(expectedCorrelationId);
        serviceMessage.setMessageId(expectedMessageId);
        serviceMessage.setProperties(messageProperties);
        serviceClient.send(deviceId, serviceMessage);

        FeedbackBatch feedbackBatch = feedbackReceiver.receive(10000);
        if (feedbackBatch != null) {
            System.out.println("Message feedback received, feedback time: "
                    + feedbackBatch.getEnqueuedTimeUtc().toString());
        } else {
            System.out.println("no feedbackBatch");
        }

        if (feedbackReceiver != null) feedbackReceiver.close();
        serviceClient.close();


    }
}
