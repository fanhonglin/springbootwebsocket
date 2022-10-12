package com.suyu.websocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * websocket
 * 消息推送(个人和广播)
 */
@Controller
public class WebSocketController {

    @Resource
    private TextWebSocketController textWebSocketController;


    /**
     * 信息推送
     *
     * @param msg
     * @return
     * @throws IOException
     */
    @RequestMapping("sendmsg")
    @ResponseBody
    public String sendMsg(String msg) throws IOException {
        textWebSocketController.sendMessage(msg,null);
        return "success";
    }

}
