package com.suyu.websocket.config;

import com.suyu.websocket.controller.AudioWebSocketController;
import com.suyu.websocket.controller.TextWebSocketController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;


@Configuration
@EnableWebMvc
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

    @Resource
    private AudioWebSocketController audioWebSocketController;

    @Resource
    private TextWebSocketController textWebSocketController;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(audioWebSocketController, "/socketServer").addInterceptors(new TestHandShakeInterceptor()).setAllowedOrigins("*");
        registry.addHandler(textWebSocketController, "/textSocketServer").addInterceptors(new TestHandShakeInterceptor()).setAllowedOrigins("*");

    }

}
