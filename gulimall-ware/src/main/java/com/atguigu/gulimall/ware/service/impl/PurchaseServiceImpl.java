package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询未领取的采购单
     */
    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0).or().eq("status", 1);

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 04、合并采购需求
     * @param mergeVo
     */
    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        // 只有采购需求的状态必须是 新建、已分配 才可以合并
        boolean isMerge = true;
        List<Long> items = mergeVo.getItems();
        if (!CollectionUtils.isEmpty(items)) {
            List<PurchaseDetailEntity> byIds = purchaseDetailService.listByIds(items);
            for (int i = 0; i < byIds.size(); i++) {
                if (byIds.get(i).getStatus() != WareConstant.PurchaseDetailStatusEnum.CREATED.getCode() &&
                        byIds.get(i).getStatus() != WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode()) {
                    isMerge = false;
                    break;
                }
            }
        }else {
            isMerge = false;
        }
        if (isMerge) {
            Long purchaseId = mergeVo.getPurchaseId();
            if (purchaseId == null) {
                // 1、新建一个采购单
                PurchaseEntity purchaseEntity = new PurchaseEntity();
                purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
                purchaseEntity.setCreateTime(new Date());
                purchaseEntity.setUpdateTime(new Date());
                this.save(purchaseEntity);
                purchaseId = purchaseEntity.getId();
            }
            items = mergeVo.getItems();
            // 2、修改采购需求，将采购单purchaseId加进去
            Long finalPurchaseId = purchaseId;
            List<PurchaseDetailEntity> collect = items.stream().map(i -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();

                detailEntity.setId(i);
                detailEntity.setPurchaseId(finalPurchaseId);
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                return detailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect);

            // 修改更新时间
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setUpdateTime(new Date());
            this.updateById(purchaseEntity);
        }
    }

    /**
     * 06、领取采购单
     * 不考虑细节：只能是自己的采购单
     * @param ids 采购单ID
     */
    @Transactional
    @Override
    public void received(List<Long> ids) {
        // 1、修改所有采购单的状态为 "已领取"
        // 1.1）过滤采购单【必须是新建或者已分配的采购单】
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());
        // 1.2、改变采购单的状态
        this.updateBatchById(collect);

        // 2、修改采购需求为 "正在购买"【采购单关联的采购需求】
        // collect是采购单集合
        collect.forEach(item->{
            // 根据采购单id列出 采购需求信息
            List <PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> purchaseDetailEntities = entities.stream().map(entity -> {
                // 为什么要重新new一个对象？
                // 因为只要修改指定的字段
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        });
    }
}