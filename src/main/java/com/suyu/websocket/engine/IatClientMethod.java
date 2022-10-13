package com.suyu.websocket.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suyu.websocket.controller.TextWebSocketController;
import com.suyu.websocket.util.AudioParams;
import com.suyu.websocket.util.WriteText;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.slf4j.LoggerFactory.getLogger;

@Slf4j
@Component
public class IatClientMethod {

    private final static Logger logger = getLogger(IatClientMethod.class);

    private boolean isCompleted = false;

    private List<String> results = new ArrayList<>();


    private static final String SERVER = "10.40.7.30:9097";

    //单个文件测试
    private final static String AUDIO_PATH = "D:\\work\\git\\springbootwebsocket\\src\\main\\resources\\16k.wav";

    private static final String sampleRate = "16k";

    @Resource
    private TextWebSocketController textWebSocketController;


    /**
     * @param server     服务器地址
     * @param audio      音频文件
     * @param sampleRate 引擎支持的音频采样率
     * @param outFile    转写结果输出
     * @throws Exception
     */
    public void doConvert(String server, File audio, String sampleRate, final File outFile) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        if (outFile.exists()) outFile.delete();
        outFile.getParentFile().mkdirs();

        final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        IatSessionParam param = new IatSessionParam(uuid, sampleRate);
//        param.setDwa("wpgs");
       /* IatSessionParam param = new IatSessionParam(uuid, "raw", "json", "utf8", "60000",
                "wpgs", "5", "true", "8k",
                "", "", "simple");*/

        //打印转写结果到文件
        WriteText.writeLog(audio.getName());
        IatClient client = new IatClient(server, param);

        //回调方法
        IatSessionResponse iatSessionResponse = new IatSessionResponse() {

            @Override
            public void onCallback(IatSessionResult iatSessionResult) {

                System.out.println(iatSessionResult.getAnsStr());
                System.out.println(JSON.toJSONString(iatSessionResult));
                //格式化转写结果
                String sentence = formatSentence(iatSessionResult.getAnsStr());
                //加入转写结果记录
//                results.add(sentence);
                //记录转写结果日志
                logger.debug("sentence:{}", sentence);
                //打印转写结果到文件
                WriteText.writeLog(sentence);

                // 发送文档
//                try {
//                    textWebSocketController.sendMessage(sentence);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                if (iatSessionResult.isEndFlag()) {
                    closeOutFile();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(uuid + "：IatSessionResponse.onError msg:{}", throwable.getMessage());

                closeOutFile();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.debug(uuid + "：onCompleted");
                closeOutFile();
                latch.countDown();
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
                logger.debug(uuid + "：结果写入完成！");
            }
        };

        boolean ret = client.connect(iatSessionResponse);
        if (!ret) {
            logger.error("【连接异常】|{}", uuid);
            return;
        }

        BufferedInputStream bis = getFileByteArray(audio);

        byte[] bits = new byte[1280];
        int len = 0;
        int i = 1;
        while ((len = bis.read(bits)) != -1) {
            if (len == 1280) {
                client.post(bits);
                //System.out.println(bits.toString() + "----" + bits.length);
            } else {
                byte[] temp_bits = new byte[len];
                System.arraycopy(bits, 0, temp_bits, 0, len);
                //System.out.println(temp_bits.toString()+"----"+temp_bits.length);

                client.post(temp_bits);
            }

            i++;
        }

        System.out.println("循环的次数：" + i);
    }

    /**
     * 音频文件读取
     *
     * @param file
     * @return
     * @throws IOException
     */
    private BufferedInputStream getFileByteArray(File file) throws IOException {
        InputStream source = new FileInputStream(file);
        if (file.getName().endsWith(".wav")) {
            AudioParams.parseParamsFrom(source, null);
        }

        return new BufferedInputStream(source);
    }

    /**
     * 转写结果格式化
     *
     * @param json 转写结果字符串
     * @return
     */
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
