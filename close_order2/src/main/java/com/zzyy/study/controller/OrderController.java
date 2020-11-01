package com.zzyy.study.controller;

import com.sun.org.apache.xpath.internal.operations.Or;
import com.zzyy.study.config.RabbitMQConfig;
import com.zzyy.study.entities.Order;
import com.zzyy.study.service.OrderService;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @auther zzyy
 * @create 2020-10-25 14:06
 */
@RestController
public class OrderController
{
    @Resource
    private OrderService orderService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/order/add")
    public String addOrder(Order order)
    {
        //1 插入orderToken进redis,作为监听key
        redisTemplate.opsForValue().setIfAbsent(order.getOrderToken(),order.getOrderSerial(),10L,TimeUnit.SECONDS);

        //2 插入记录进mysql
        int i = orderService.addOrder(order);

        return "下单成功，返回值: "+i;
    }





    @PostMapping("/order/addbymq")
    public String addOrderByMQ(Order order)
    {
        //1 插入记录进mysql
        int i = orderService.addOrder(order);
        //2 插入消息进RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,"boot.abc",order.getOrderSerial(),message -> {
            message.getMessageProperties().setExpiration("10000");
            return message;
        });


        return "下单成功 By RabbitMQ，返回值: "+i;
    }

}