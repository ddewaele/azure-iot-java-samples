package com.ixortalk;

import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.ixortalk.Constants.DEVICE_CONNECTIONSTRING;
import static com.ixortalk.Constants.DEVICE_ID;

/**
 * Generates a device on an IoT HUB.
 * <p>
 * Launch the app with 2 args
 *
 * - Your Iot Connection String
 * - The Device ID you want to register
 *
 * </p>
 * Returns :
 * <p>
 * Device Id: java-app-1
 * Device key: xxxxxxxxxxxxxxxxx==
 * </p>
 *
 *
 */
public class RegisterDevice {

    public static void main(String[] args) throws IOException, URISyntaxException, Exception {

        String connectionString = args.length > 0 ? args[0] : DEVICE_CONNECTIONSTRING;
        String deviceId = args.length > 1 ? args[1] : DEVICE_ID;

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        Device device = Device.createFromId(deviceId, null, null);
        try {
            device = registryManager.addDevice(device);
        } catch (IotHubException iote) {
            try {
                device = registryManager.getDevice(deviceId);
            } catch (IotHubException iotf) {
                iotf.printStackTrace();
            }
        }

        System.out.println("Device Id: " + device.getDeviceId());
        System.out.println("Device key: " + device.getPrimaryKey());

    }
}
