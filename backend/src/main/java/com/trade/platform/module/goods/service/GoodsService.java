package com.trade.platform.module.goods.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.goods.dto.GoodsQueryDTO;
import com.trade.platform.module.goods.entity.Goods;
import com.trade.platform.module.goods.mapper.GoodsMapper;
import com.trade.platform.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    public PageResult<Goods> getList(GoodsQueryDTO dto) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getTenantId, UserContext.getTenantId());
        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(Goods::getName, dto.getName());
        }
        if (StringUtils.hasText(dto.getHsCode())) {
            wrapper.like(Goods::getHsCode, dto.getHsCode());
        }
        if (StringUtils.hasText(dto.getGoodsNo())) {
            wrapper.like(Goods::getGoodsNo, dto.getGoodsNo());
        }
        if (StringUtils.hasText(dto.getCategory())) {
            wrapper.eq(Goods::getCategory, dto.getCategory());
        }
        wrapper.orderByDesc(Goods::getCreateTime);

        Page<Goods> page = new Page<>(dto.getCurrent(), dto.getSize());
        goodsMapper.selectPage(page, wrapper);
        return PageResult.from(page);
    }

    public Goods getById(Long id) {
        return goodsMapper.selectById(id);
    }

    public Goods create(Goods goods) {
        goods.setTenantId(UserContext.getTenantId());
        if (!StringUtils.hasText(goods.getCurrency())) {
            goods.setCurrency("CNY");
        }
        goodsMapper.insert(goods);
        return goods;
    }

    public Goods update(Goods goods) {
        Goods existing = goodsMapper.selectById(goods.getId());
        if (existing == null) {
            throw new BusinessException("商品不存在");
        }
        goodsMapper.updateById(goods);
        return goods;
    }

    public void delete(Long id) {
        Goods existing = goodsMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("商品不存在");
        }
        goodsMapper.deleteById(id);
    }

    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的商品");
        }
        goodsMapper.deleteBatchIds(ids);
    }

    public List<Goods> getGoodsForSelection() {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getTenantId, UserContext.getTenantId());
        wrapper.orderByAsc(Goods::getName);
        return goodsMapper.selectList(wrapper);
    }
}
