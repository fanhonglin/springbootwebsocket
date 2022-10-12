package com.suyu.websocket.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suyu.websocket.controller.TextWebSocketController;
import com.suyu.websocket.util.SpringUtils;
import com.suyu.websocket.util.WriteText;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class ClientInstant {

    private static final String SERVER = "10.40.7.30:9097";
    private static final String sampleRate = "16k";
    private boolean isCompleted = false;
    private IatClient client;

    public ClientInstant(String sn) {

        final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        IatSessionParam param = new IatSessionParam(uuid, sampleRate);
        client = new IatClient(SERVER, param);

        //回调方法
        IatSessionResponse iatSessionResponse = new IatSessionResponse() {

            @Override
            public void onCallback(IatSessionResult iatSessionResult) {

                log.info("sn:{}, content:{}", sn, JSON.toJSONString(iatSessionResult));

                //格式化转写结果
                String sentence = formatSentence(iatSessionResult.getAnsStr());

                //打印转写结果到文件
                WriteText.writeLog(sentence);

                if (!StringUtils.isEmpty(sentence)) {
                    // 发送文档
                    try {
                        SpringUtils.getBean(TextWebSocketController.class).sendMessage(sentence, sn);
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
