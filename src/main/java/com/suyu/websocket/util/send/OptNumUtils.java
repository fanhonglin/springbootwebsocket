package com.suyu.websocket.util.send;

/**
 * @Author ch3ng
 * @Date 2020/2/21 15:10
 * @Version 1.0
 * @Description
 **/
public class OptNumUtils {

    private static int optNum = 0;

    public static synchronized int getOptNum(){
        if(optNum == 32767){
            optNum = 0;
        }
        return optNum++;
    }
}
