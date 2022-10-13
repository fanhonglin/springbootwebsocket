package com.suyu.websocket.util.send;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Copy from iot demo
 */
public class IoTRequest {

    /**
     * post请求
     *
     * @param host      主机
     * @param path      请求路径
     * @param apiKey    apiKey
     * @param apiSecret api密钥
     * @param data      数据体
     * @return post请求结果
     */
    public String post(String host, String path, String apiKey, String apiSecret, JSONObject data) throws Exception {
        String url = "http://" + host + path;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String resp = "";
        try {
            String b = new String(data.toString().getBytes(), StandardCharsets.UTF_8);
            StringEntity entity = new StringEntity(b, StandardCharsets.UTF_8);
            String dateStr = getCurrentUTCTime();
            String digestBody = "SHA-256=" + getSHA256(data.toString());
            String requestLine = "POST " + path + " HTTP/1.1";
            String rawSignStr = "host: " + host + "\n"
                    + "date: " + dateStr + "\n"
                    + requestLine;
            String digest = hmacSHA256(rawSignStr, apiSecret);
            String authValue = String.format("hmac username=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                    apiKey, "hmac-sha256", "host date request-line", digest);
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Host", host);
            post.setHeader("Date", dateStr);
            post.setHeader("Digest", digestBody);
            post.setHeader("Authorization", authValue);
            post.setEntity(entity);
            CloseableHttpResponse response = httpclient.execute(post);
            try {
                // 获取响应实体
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    resp = EntityUtils.toString(resEntity);
                }
            } finally {
                response.close();
            }
        } finally {
            // 关闭连接,释放资源
            httpclient.close();
        }
        return resp;
    }

    /**
     * post请求
     *
     * @param host      主机
     * @param path      请求路径
     * @param apiKey    apiKey
     * @param apiSecret api密钥
     * @param jsonData  数据体
     * @return post请求结果
     */
    public String post(String host, String path, String apiKey, String apiSecret, String jsonData) throws Exception {
        String url = "http://" + host + path;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String resp = "";
        try {
//            String b = new String(data.toString().getBytes(), StandardCharsets.UTF_8);
            StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
            String dateStr = getCurrentUTCTime();
            String digestBody = "SHA-256=" + getSHA256(jsonData);
            String requestLine = "POST " + path + " HTTP/1.1";
            String rawSignStr = "host: " + host + "\n"
                    + "date: " + dateStr + "\n"
                    + requestLine;
            String digest = hmacSHA256(rawSignStr, apiSecret);
            String authValue = String.format("hmac username=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                    apiKey, "hmac-sha256", "host date request-line", digest);
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Host", host);
            post.setHeader("Date", dateStr);
            post.setHeader("Digest", digestBody);
            post.setHeader("Authorization", authValue);
            post.setEntity(entity);
            CloseableHttpResponse response = httpclient.execute(post);
            try {
                // 获取响应实体
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    resp = EntityUtils.toString(resEntity);
                }
            } finally {
                response.close();
            }
        } finally {
            // 关闭连接,释放资源
            httpclient.close();
        }
        return resp;
    }

    /**
     * put请求
     *
     * @param host      主机
     * @param path      请求路径
     * @param apiKey    apiKey
     * @param apiSecret api密钥
     * @param data      数据体
     * @return post请求结果
     */
    public String put(String host, String path, String apiKey, String apiSecret, JSONObject data) throws Exception {
        String url = "http://" + host + path;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String resp = "";
        try {
            String b = new String(data.toString().getBytes(), StandardCharsets.UTF_8);
            StringEntity entity = new StringEntity(b, StandardCharsets.UTF_8);
            String dateStr = getCurrentUTCTime();
            String digestBody = "SHA-256=" + getSHA256(data.toString());
            String requestLine = "PUT " + path + " HTTP/1.1";
            String rawSignStr = "host: " + host + "\n"
                    + "date: " + dateStr + "\n"
                    + requestLine;
            String digest = hmacSHA256(rawSignStr, apiSecret);
            String authValue = String.format("hmac username=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                    apiKey, "hmac-sha256", "host date request-line", digest);
            HttpPut put = new HttpPut(url);
            put.setHeader("Content-Type", "application/json");
            put.setHeader("Host", host);
            put.setHeader("Date", dateStr);
            put.setHeader("Digest", digestBody);
            put.setHeader("Authorization", authValue);
            put.setEntity(entity);
            CloseableHttpResponse response = httpclient.execute(put);
            try {
                // 获取响应实体
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    resp = EntityUtils.toString(resEntity);
                }
            } finally {
                response.close();
            }
        } finally {
            // 关闭连接,释放资源
            httpclient.close();
        }
        return resp;
    }

    /**
     * get请求
     *
     * @param host      主机
     * @param path      请求路径
     * @param apiKey    apiKey
     * @param apiSecret api密钥
     * @return get请求结果
     */
    public String get(String host, String path, String apiKey, String apiSecret) throws Exception {
        String url = "http://" + host + path;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String resp = null;
        try {
            String dateStr = getCurrentUTCTime();
            HttpGet http = new HttpGet(url);
            http.setHeader("Content-Type", "application/json");
            http.setHeader("Host", host);
            http.setHeader("Date", dateStr);
            String requestLine = "GET " + path + " HTTP/1.1";
            String rawSignStr = "host: " + host + "\n" + "date: " + dateStr + "\n" + requestLine;
            String digest = hmacSHA256(rawSignStr, apiSecret);
            String authValue = String.format("hmac username=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                    apiKey, "hmac-sha256", "host date request-line", digest);
            http.setHeader("Authorization", authValue);
            CloseableHttpResponse response = httpclient.execute(http);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // 打印响应内容
                    resp = EntityUtils.toString(entity);
                }
            } finally {
                response.close();
            }
        } finally {
            // 关闭连接,释放资源
            httpclient.close();
        }
        return resp;
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间
     */
    private String getCurrentUTCTime() {
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'UTC'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(cd.getTime());
    }

    /**
     * hmac sha256
     *
     * @param message 消息
     * @param secret  密钥
     * @return hmac sha256结果
     */
    private String hmacSHA256(String message, String secret) throws Exception {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        hmacSha256.init(secretKey);
        byte[] bytes = hmacSha256.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * sha256
     *
     * @param str 输入
     * @return sha256结果
     */
    private String getSHA256(String str) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(str.getBytes());
        return Base64.getEncoder().encodeToString(messageDigest.digest());

    }
}
