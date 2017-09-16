package com.ixortalk;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.servicebus.ServiceBusException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.function.Consumer;

import static com.ixortalk.Constants.EVENTHUB_CONNECTIONSTRING;

/**
 *
 * This will listen for device-to-cloud messages using azure eventhub.
 * This is the receiving end of the device-cloud scenario
 *
 * Note: This will only capture messages that were sent by devices. If you send a message to a device, this class won't pick it up
 *
 *
 */
public class DeviceToCloudReceiver {

    public static void main( String[] args ) throws IOException {

        String connectionString = args.length > 0 ? args[0] : EVENTHUB_CONNECTIONSTRING;

        // Create receivers for partitions 0 and 1.
        EventHubClient client0 = receiveMessages(connectionString, "0");
        EventHubClient client1 = receiveMessages(connectionString, "1");
        EventHubClient client2 = receiveMessages(connectionString, "2");
        EventHubClient client3 = receiveMessages(connectionString, "3");

        System.out.println("Press ENTER to exit.");
        System.in.read();
        try {
            client0.closeSync();
            client1.closeSync();
            client2.closeSync();
            client3.closeSync();
            System.exit(0);
        } catch (ServiceBusException sbe) {
            System.exit(1);
        }
    }


    // Create a receiver on a partition.
    private static EventHubClient receiveMessages(String connectionString, final String partitionId) {
        EventHubClient client = null;
        try {
            client = EventHubClient.createFromConnectionStringSync(connectionString);
        } catch (Exception e) {
            System.out.println("Failed to create client: " + e.getMessage());
            System.exit(1);
        }
        try {
            // Create a receiver using the
            // default Event Hubs consumer group
            // that listens for messages from now on.
            client.createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME , partitionId, Instant.now())
                    .thenAccept(new Consumer<PartitionReceiver>() {
                        public void accept(PartitionReceiver receiver) {
                            System.out.println("** Created receiver on partition " + partitionId);
                            try {
                                while (true) {
                                    Iterable<EventData> receivedEvents = receiver.receive(100).get();
                                    int batchSize = 0;
                                    if (receivedEvents != null) {
                                        System.out.println("Got some evenst");
                                        for (EventData receivedEvent : receivedEvents) {
                                            System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s",
                                                    receivedEvent.getSystemProperties().getOffset(),
                                                    receivedEvent.getSystemProperties().getSequenceNumber(),
                                                    receivedEvent.getSystemProperties().getEnqueuedTime()));
                                            System.out.println(String.format("| Device ID: %s",
                                                    receivedEvent.getSystemProperties().get("iothub-connection-device-id")));
                                            System.out.println(String.format("| Message Payload: %s",
                                                    new String(receivedEvent.getBytes(), Charset.defaultCharset())));
                                            batchSize++;
                                        }
                                    }
                                    System.out.println(String.format("Partition: %s, ReceivedBatch Size: %s", partitionId, batchSize));
                                }
                            } catch (Exception e) {
                                System.out.println("Failed to receive messages: " + e.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            System.out.println("Failed to create receiver: " + e.getMessage());
        }
        return client;
    }
}
