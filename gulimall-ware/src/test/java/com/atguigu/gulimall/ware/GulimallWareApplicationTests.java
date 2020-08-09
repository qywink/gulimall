package com.atguigu.gulimall.ware;

import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
class GulimallWareApplicationTests {

    @Autowired
    PurchaseService purchaseService;

    @Test
    void contextLoads() {
        PurchaseEntity entity = new PurchaseEntity();
        entity.setId(1L);
        entity.setAmount(new BigDecimal(99999));
        boolean save = purchaseService.save(entity);
        System.out.println("保存成功：" + save);
        List<PurchaseEntity> list = purchaseService.list(new QueryWrapper<PurchaseEntity>().eq("id", 1L));
        list.forEach((item) -> {
            System.out.println(item);
        });

    }

}
