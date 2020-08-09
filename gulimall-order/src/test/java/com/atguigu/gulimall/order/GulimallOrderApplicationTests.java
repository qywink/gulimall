package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    OrderService orderService;

    @Test
    void contextLoads() {
        OrderEntity entity = new OrderEntity();
        entity.setId(1L);
        entity.setBillContent("这是一个账单");
        boolean save = orderService.save(entity);
        System.out.println("保存成功：" + save);
        List<OrderEntity> list = orderService.list(new QueryWrapper<OrderEntity>().eq("id", 1L));
        list.forEach((item)->{
            System.out.println(item);
        });
    }

}
