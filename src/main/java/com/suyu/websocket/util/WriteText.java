package com.suyu.websocket.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: mingyanliao
 * Date: 2019-04-22
 * Time: 下午7:21
 */
@Slf4j
public class WriteText {

    public static void writeLog(String str) {
        log.info("{}", str);
    }
}
