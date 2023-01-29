package com.suyu.websocket.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suyu.websocket.controller.AudioWebSocketController;
import com.suyu.websocket.controller.TextWebSocketController;
import com.suyu.websocket.entity.ClientInfo;
import com.suyu.websocket.util.SpringUtils;
import com.suyu.websocket.util.WriteText;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

@Slf4j
public class ClientInstant {

    private static final String sampleRate = "16k";
    private boolean isCompleted = false;
    private IatClient client;

    public ClientInstant(String sn, int channel, String engineUrl) {

        final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        IatSessionParam param = new IatSessionParam(uuid, sampleRate);
        client = new IatClient(engineUrl, param);

        //回调方法
        IatSessionResponse iatSessionResponse = new IatSessionResponse() {

            @Override
            public void onCallback(IatSessionResult iatSessionResult) {

                log.info("sn:{},channel:{}, content:{}", sn, channel, JSON.toJSONString(iatSessionResult));

                //格式化转写结果
                String sentence = formatSentence(iatSessionResult.getAnsStr());

                //打印转写结果到文件
                WriteText.writeLog(sentence);

                if (!StringUtils.isEmpty(sentence)) {

                    ClientInfo clientInfo = AudioWebSocketController.SN2ClientInfoMap.get(sn);
                    if (Objects.nonNull(clientInfo)) {
                        Date now = new Date();
                        // 连接时间
                        Date createTime = clientInfo.getCreateTime();

                        log.info("转写的时间：{}", (now.getTime() - createTime.getTime()));

                        String ansStr = iatSessionResult.getAnsStr();
                        JSONObject jsonObject = JSON.parseObject(ansStr);
                        Long ed = jsonObject.getLong("ed") * 10;

                        long endTime = createTime.getTime() + ed;

                        log.info("转写时间：{}", now.getTime() - endTime);

                    }

                    // 发送文档
                    try {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("track", channel);
                        resultMap.put("text", sentence);
                        SpringUtils.getBean(TextWebSocketController.class).sendMessage(JSON.toJSONString(resultMap), sn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (iatSessionResult.isEndFlag()) {
                    closeOutFile();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                closeOutFile();
            }

            @Override
            public void onCompleted() {
//                closeOutFile();
            }

            //关闭连接
            private void closeOutFile() {
                //设置为完成
                isCompleted = true;
                try {
                    client.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                log.debug(uuid + "：结果写入完成！");
            }
        };

        boolean ret = client.connect(iatSessionResponse);
        if (!ret) {
            log.error("【连接异常】|{}", uuid);
            return;
        }
    }

    public IatClient getClient() {
        return client;
    }

    private String formatSentence(String json) {
        if (json.length() == 0)
            return "";
        StringBuilder resultBuff = new StringBuilder();
        try {
            JSONObject rt = JSON.parseObject(json);
            if (rt == null)
                return "";
            JSONArray wsArray = rt.getJSONArray("ws");

            if (wsArray != null && wsArray.size() != 0) {
                for (Object ws : wsArray) {
                    com.alibaba.fastjson.JSONArray cwArray = ((com.alibaba.fastjson.JSONObject) ws).getJSONArray("cw");
                    for (Object cw : cwArray) {
                        String w = ((com.alibaba.fastjson.JSONObject) cw).getString("w");
                        resultBuff.append(w);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultBuff.append("exp: ").append(e.getMessage());
        }
        return resultBuff.toString();
    }

}
