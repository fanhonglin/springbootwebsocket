package com.suyu.websocket.util.send;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Author Joey Fan
 * @Description MD5加密工具类
 * @Date 16:12 2022/4/6
 **/

@Slf4j
public class MD5Util {

    /**
     * @return java.lang.String
     * @Author Joey Fan
     * @Description 设备sn后五位获取生成设备名称
     * @Date 16:13 2022/4/6
     * @Param [sn]
     **/
    public static String getDeviceNameBySN(String sn) {

        StringBuffer stringBuffer = new StringBuffer("XFTJ_");
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            log.error("生成MD5失败");
        }

        md.update(stringBuffer.append(sn.substring(9)).toString().getBytes());
        String deviceName = new BigInteger(1, md.digest()).toString(16);

        return fillMD5(deviceName);
    }

    /**
     * @return java.lang.String
     * @Author Joey Fan
     * @Description 解决MD5 加密后第一位为0被去掉的问题
     * @Date 16:14 2022/4/6
     * @Param [md5]
     **/
    private static String fillMD5(String md5) {
        return md5.length() == 32 ? md5 : fillMD5("0" + md5);
    }
}
