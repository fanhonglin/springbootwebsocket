package com.suyu.websocket.util.send;

/**
 * @author lazycece
 */
public class IoTConstants {

    public static final String SERVICE_ID = "ServiceM1";

    public static final String REQUEST_HOST = "linkapi.xfyun.cn";
    public static final String PRODUCT_KEY = "A0B95C1790EC4E4493DB40C13715635B";
    public static final String API_KEY = "D677BB98ACCB4C97B9505B467223046C";
    public static final String API_SECRET = "QwYn2cdOlT7RWsuiU7LSKfV51XWoqd4v";
    public static final String DEVICE_SECRET = "YYSBUCQ63ED34CBD92E74976C1811C23";


    public static final String API_DEVICES_CREATE = "/api/v1/devices";
    public static final String API_DEVICES_ACTIVE = "/api/v1/devices/{deviceName}/active";
    public static final String API_DEVICES_CREATE_AND_ACTIVE = "/api/v1/devices/createActive";
    public static final String API_DEVICES_DETAIL = "/api/v1/devices/{deviceName}/detail";
    public static final String API_DEVICES_LIST = "/api/v1/devices/all";
    public static final String API_DEVICES_STATUS = "/api/v1/devices/{deviceName}/status";
    public static final String API_DEVICES_STATUS_LIST = "/api/v1/devices/status";
    public static final String API_DEVICES_BIND = "/api/v1/devices/bind";
    public static final String API_DEVICES_UNBIND = "/api/v1/devices/unbind";
    public static final String API_DEVICES_BIND_STATUS = "/api/v1/devices/{deviceName}/bind";

    public static final String API_COMMAND_OBTAIN_PROPERTIES = "/api/v1/commands/obtain/properties";
    public static final String API_COMMAND_SETUP_PROPERTIES = "/api/v1/commands/setup/properties";
    public static final String API_COMMAND_PUSH = "/api/v1/commands/push/commands";


    // 批量下发指令
    public static final String API_COMMANDS_PUSH = "/api/v1/commands/batchPush/commands";


    // 批量获取设备属性值
    public static final String API_COMMANDS_PROPERTIES = "/api/v1/commands/batchObtain/properties";

    public static final String API_THINGS_EVENT_DATA = "/api/v1/things/device/event";
    public static final String API_THINGS_PROPERTY_DATA = "/api/v1/things/device/property";
    public static final String API_THINGS_HISTORY = "/api/v1/things/device/history/service";

}
