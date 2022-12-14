package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.product.AttrConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.vo.product.*;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.common.entity.product.AttrAttrgroupRelationEntity;
import com.atguigu.common.entity.product.AttrEntity;
import com.atguigu.common.entity.product.AttrGroupEntity;
import com.atguigu.common.entity.product.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryService categoryService;
    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // ??????????????????
        this.save(attrEntity);
        if (AttrConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == attrEntity.getAttrType() &&
                !StringUtils.isEmpty(attr.getAttrGroupId())) {
            // ?????????????????????????????????
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());// ??????ID
            relationEntity.setAttrId(attrEntity.getAttrId());// ??????ID
            relationDao.insert(relationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().
                eq("attr_type", "base".equalsIgnoreCase(type) ?
                        AttrConstant.AttrEnum.ATTR_TYPE_BASE.getCode() :
                        AttrConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        // ?????????
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> wrapper.eq("attr_id", key).or().like("attr_name", key));
        }
        // ????????????
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        // ??????vo????????????
        List<AttrRespVO> attrRespVos = page.getRecords().stream().map(attr -> {
            AttrRespVO attrRespVo = new AttrRespVO();
            BeanUtils.copyProperties(attr, attrRespVo);

            // ?????????????????????
            CategoryEntity categoryEntity = categoryDao.selectById(attr.getCatelogId());
            attrRespVo.setCatelogName(categoryEntity.getName());

            if (AttrConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == attr.getAttrType()) {
                // ????????????????????????????????????
                AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
                if (relationEntity != null && !StringUtils.isEmpty(relationEntity.getAttrGroupId())) {
                    AttrGroupEntity groupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    attrRespVo.setGroupName(groupEntity.getAttrGroupName());
                }
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        // ????????????
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(attrRespVos);
        return pageUtils;
    }

    @Cacheable(value = "attr", key = "'attrinfo:'+#root.args[0]")// attrinfo:attrId
    @Override
    public AttrRespVO getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrRespVO respVo = new AttrRespVO();
        BeanUtils.copyProperties(attrEntity, respVo);

        if (attrEntity.getAttrType().equals(AttrConstant.AttrEnum.ATTR_TYPE_BASE.getCode())) {
            // ???????????????1.??????????????????ID
            AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity != null) {
                respVo.setAttrGroupId(relationEntity.getAttrGroupId());
                // ???????????????
                AttrGroupEntity groupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                if (groupEntity != null) {
                    respVo.setGroupName(groupEntity.getAttrGroupName());
                }
            }
        }
        // 2.??????????????????path
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        respVo.setCatelogPath(catelogPath);

        return respVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // ????????????
        this.updateById(attrEntity);

        // ????????????
        AttrEntity _attr = this.getById(attrEntity.getAttrId());
        if (AttrConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == _attr.getAttrType()) {
            // ?????????????????????????????????
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attr.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            int count = relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count == 0) {
                // ???????????????????????????????????????
                relationDao.insert(relationEntity);
            }
        }
    }

    /**
     * ?????????????????? ????????? ????????????
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        // ????????????id????????????????????????
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.
                selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        // ??????????????????id
        List<Long> attrIds = relationEntities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        // ??????id????????????????????????
        if (CollectionUtils.isEmpty(attrIds)) {
            return new ArrayList<>();
        }
        // ??????????????????
        List<AttrEntity> attrEntities = this.listByIds(attrIds);
        return attrEntities;
    }

    /**
     * ????????????????????????
     */
    @Override
    public void deleteRelation(AttrGroupRelationVO[] vos) {
        List<AttrAttrgroupRelationEntity> entities = Arrays.stream(vos).map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        attrAttrgroupRelationDao.deleteBatchRelation(entities);
    }

    /**
     * ?????????????????????????????????????????????
     * 1????????????????????????????????????????????????
     * 2????????????????????????????????????????????????????????????
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        // 1?????????????????????????????????????????????????????????
        // ???????????????ID
        AttrGroupEntity groupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = groupEntity.getCatelogId();
        // 2??????????????????????????????????????????????????????
        // 2.1???????????????????????????????????????
        List<AttrGroupEntity> groups = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> groupIds = groups.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        // 2.2??????????????????????????????????????????
        List<AttrAttrgroupRelationEntity> relationEntities = new ArrayList<AttrAttrgroupRelationEntity>();
        if (!CollectionUtils.isEmpty(groupIds)) {
            relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        }
        List<Long> attrIds = relationEntities.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        // 2.3??????????????????????????????????????? ????????????????????????
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", AttrConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (!CollectionUtils.isEmpty(attrIds)) {
            queryWrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

    @Override
    public List<AttrEntity> getBatchIds(List<Long> attrIds) {
        return this.baseMapper.selectBatchIds(attrIds);
    }

    /**
     * ??????????????????????????????ID??????
     */
    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        return baseMapper.selectSearchAttrIds(attrIds);
    }


}