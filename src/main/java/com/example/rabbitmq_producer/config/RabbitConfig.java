package com.example.rabbitmq_producer.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author qi_coding
 * @version 1.00
 * @time 2023/10/15 14:12
 * rabbitmq实现rpc
 */
@Configuration
public class RabbitConfig {

    //定义一个返回队列一个发送队列一个交换机
    public final static String RPC_QUEUE1 = "queue_1";
    public final static String RPC_QUEUE2 = "queue_2";
    public final static String RPC_EXCHANGE = "exchange_rpc";

    @Bean
    public Queue msgQueue(){
        return new Queue(RPC_QUEUE1);
    }
    @Bean
    public Queue replyQueue(){
        return new Queue(RPC_QUEUE2);
    }
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(RPC_EXCHANGE);
    }

    //绑定交换机和队列
    @Bean
    public Binding msgBinging(){
        return BindingBuilder.bind(msgQueue()).to(exchange()).with(RPC_QUEUE1);
    }
    @Bean
    public Binding replyBinging(){
        return BindingBuilder.bind(replyQueue()).to(exchange()).with(RPC_QUEUE2);
    }


    /**
     * 使用 RabbitTemplate发送和接收消息
     * 并设置回调队列地址
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        //回复队列的名称，即用于接收服务端处理结果的队列名称
        rabbitTemplate.setReplyAddress(RPC_QUEUE2);
        //设置等待响应的最大超时时间，单位为毫秒。如果在超时时间内还未收到服务端响应，则会抛出异常。
        rabbitTemplate.setReplyTimeout(6000);
        return rabbitTemplate;
    }

    /**
     * 给返回队列设置监听器
     */
    @Bean
    public SimpleMessageListenerContainer replyContainer(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        //连接工厂：用于提供连接到 RabbitMQ 的配置信息。
        container.setConnectionFactory(connectionFactory);
        //队列名称：用于指定监听哪个队列来接收服务端的响应。
        container.setQueueNames(RPC_QUEUE2);
        //消息监听器：用于在接收到服务端的响应后，执行相应的操作。
        container.setMessageListener(rabbitTemplate(connectionFactory));
        return container;
    }

/*    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();

        connectionFactory.setHost("localhost"); // RabbitMQ 服务器主机名
        connectionFactory.setPort(5672); // RabbitMQ 服务器端口号
        connectionFactory.setUsername("guest"); // RabbitMQ 登录用户名
        connectionFactory.setPassword("guest"); // RabbitMQ 登录密码
        // 其他可选的配置项
        // connectionFactory.setVirtualHost("/"); // RabbitMQ 虚拟主机
        // connectionFactory.setConnectionTimeout(30000); // 连接超时时间

        return connectionFactory;
    }*/

}
