package com.atguigu.gulimall.product;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    OSSClient ossClient;

    @Test
    void testUpload() throws FileNotFoundException {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = "oss-cn-shanghai.aliyuncs.com";
        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        String accessKeyId = "LTAI4G5VdGZu5aGhdiSyFCDP";
        String accessKeySecret = "4ZjfxJPorPFocN9orvNf46v19bNO2k";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 上传文件流。
        InputStream inputStream = new FileInputStream("C:\\Users\\Administrator\\Desktop\\spring cloud alibaba全解2.pdf");
        ossClient.putObject("gulimall-wan", "spring cloud alibaba全解2.pdf", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();

        System.out.println("上传成功");
    }

    @Test
    void contextLoads() {
        BrandEntity entity = new BrandEntity();
        entity.setName("华为");
        boolean save = brandService.save(entity);
        System.out.println("保存成功：" + save);
    }

    @Test
    void update() {
        BrandEntity entity = new BrandEntity();
        entity.setBrandId(1L);
        entity.setName("小米");
        boolean save = brandService.updateById(entity);
        System.out.println("xiugai成功：" + save);
    }

    @Test
    void queryPage() {
        //brandService.queryPage()
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach((item) -> {
            System.out.println(item);
        });
    }

}
