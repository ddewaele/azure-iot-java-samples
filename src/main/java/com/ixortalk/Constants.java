package com.ixortalk;

public class Constants {

    public static final String IOT_HUB_CONNECTIONSTRING = "HostName=xxxxx.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=xxxxxxxxx=";

    public static final String DEVICE_CONNECTIONSTRING = "HostName=xxxxx.azure-devices.net;DeviceId=nodemcu-amica;SharedAccessKey=xxxxxxxxxxx=";

    // Notice the additional entitypath in this connection string.
    public static final String EVENTHUB_CONNECTIONSTRING= "Endpoint=sb://iothub-ns-xxxxxxx.servicebus.windows.net/;EntityPath=ixortalk-iothub;SharedAccessKeyName=iothubowner;SharedAccessKey=xxxxxxxxxxxx=";

    public static final String DEVICE_ID= "nodemcu-amica";

}
