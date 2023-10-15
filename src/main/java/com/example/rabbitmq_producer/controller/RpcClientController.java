package com.example.rabbitmq_producer.controller;

import com.example.rabbitmq_producer.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author qi_coding
 * @version 1.00
 * @time 2023/10/15 14:51
 */
@RestController
public class RpcClientController {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientController.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/send")
    public String send(String message) {
        // 创建消息对象
        Message newMessage = MessageBuilder.withBody(message.getBytes()).build();

        logger.info("client send：{}", newMessage);

        //客户端发送消息
        //返回值就是服务端返回的消息
        Message result = rabbitTemplate.sendAndReceive(RabbitConfig.RPC_EXCHANGE, RabbitConfig.RPC_QUEUE1, newMessage);

        String response = "";
        if (result != null) {
            // 获取已发送的消息的 correlationId
            String correlationId = newMessage.getMessageProperties().getCorrelationId();
            logger.info("correlationId:{}", correlationId);

            // 获取响应头信息
            HashMap<String, Object> headers = (HashMap<String, Object>) result.getMessageProperties().getHeaders();

            // 获取 server 返回的消息 id
            String msgId = (String) headers.get("spring_returned_message_correlation");

            if (msgId.equals(correlationId)) {
                response = new String(result.getBody());
                logger.info("client receive：{}", response);
            }
        }
        return response;
    }
}
