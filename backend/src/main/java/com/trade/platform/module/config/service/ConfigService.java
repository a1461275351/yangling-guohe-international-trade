package com.trade.platform.module.config.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.config.dto.ConfigItemQueryDTO;
import com.trade.platform.module.config.dto.ConfigValueQueryDTO;
import com.trade.platform.module.config.entity.ConfigItem;
import com.trade.platform.module.config.entity.ConfigValue;
import com.trade.platform.module.config.mapper.ConfigItemMapper;
import com.trade.platform.module.config.mapper.ConfigValueMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ConfigService {

    @Resource
    private ConfigItemMapper configItemMapper;

    @Resource
    private ConfigValueMapper configValueMapper;

    public PageResult<ConfigItem> getItemList(ConfigItemQueryDTO dto) {
        LambdaQueryWrapper<ConfigItem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getCode())) {
            wrapper.like(ConfigItem::getCode, dto.getCode());
        }
        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(ConfigItem::getName, dto.getName());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(ConfigItem::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(ConfigItem::getCreateTime);

        Page<ConfigItem> page = new Page<>(dto.getCurrent(), dto.getSize());
        configItemMapper.selectPage(page, wrapper);

        PageResult<ConfigItem> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    public void createItem(ConfigItem item) {
        LambdaQueryWrapper<ConfigItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConfigItem::getCode, item.getCode());
        Long count = configItemMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("配置项编码已存在");
        }
        configItemMapper.insert(item);
    }

    public void updateItem(ConfigItem item) {
        ConfigItem existing = configItemMapper.selectById(item.getId());
        if (existing == null) {
            throw new BusinessException("配置项不存在");
        }
        LambdaQueryWrapper<ConfigItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConfigItem::getCode, item.getCode());
        wrapper.ne(ConfigItem::getId, item.getId());
        Long count = configItemMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("配置项编码已存在");
        }
        configItemMapper.updateById(item);
    }

    public void deleteItem(Long id) {
        ConfigItem existing = configItemMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("配置项不存在");
        }
        LambdaQueryWrapper<ConfigValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConfigValue::getConfigItemId, id);
        Long count = configValueMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该配置项下存在配置值，无法删除");
        }
        configItemMapper.deleteById(id);
    }

    public void updateItemStatus(Long id, Integer status) {
        ConfigItem item = configItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("配置项不存在");
        }
        item.setStatus(status);
        configItemMapper.updateById(item);
    }

    public PageResult<ConfigValue> getValueList(ConfigValueQueryDTO dto) {
        LambdaQueryWrapper<ConfigValue> wrapper = new LambdaQueryWrapper<>();
        if (dto.getConfigItemId() != null) {
            wrapper.eq(ConfigValue::getConfigItemId, dto.getConfigItemId());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(ConfigValue::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(ConfigValue::getCode, dto.getKeyword())
                    .or().like(ConfigValue::getName, dto.getKeyword()));
        }
        wrapper.orderByDesc(ConfigValue::getCreateTime);

        Page<ConfigValue> page = new Page<>(dto.getCurrent(), dto.getSize());
        configValueMapper.selectPage(page, wrapper);

        PageResult<ConfigValue> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    public void createValue(ConfigValue value) {
        ConfigItem item = configItemMapper.selectById(value.getConfigItemId());
        if (item == null) {
            throw new BusinessException("配置项不存在");
        }
        LambdaQueryWrapper<ConfigValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConfigValue::getConfigItemId, value.getConfigItemId());
        wrapper.eq(ConfigValue::getCode, value.getCode());
        Long count = configValueMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("同一配置项下配置值编码已存在");
        }
        configValueMapper.insert(value);
    }

    public void updateValue(ConfigValue value) {
        ConfigValue existing = configValueMapper.selectById(value.getId());
        if (existing == null) {
            throw new BusinessException("配置值不存在");
        }
        LambdaQueryWrapper<ConfigValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConfigValue::getConfigItemId, value.getConfigItemId());
        wrapper.eq(ConfigValue::getCode, value.getCode());
        wrapper.ne(ConfigValue::getId, value.getId());
        Long count = configValueMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("同一配置项下配置值编码已存在");
        }
        configValueMapper.updateById(value);
    }

    public void deleteValue(Long id) {
        ConfigValue existing = configValueMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("配置值不存在");
        }
        configValueMapper.deleteById(id);
    }

    public void updateValueStatus(Long id, Integer status) {
        ConfigValue value = configValueMapper.selectById(id);
        if (value == null) {
            throw new BusinessException("配置值不存在");
        }
        value.setStatus(status);
        configValueMapper.updateById(value);
    }

    public void batchUpdateValueStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要操作的配置值");
        }
        for (Long id : ids) {
            ConfigValue value = configValueMapper.selectById(id);
            if (value != null) {
                value.setStatus(status);
                configValueMapper.updateById(value);
            }
        }
    }

    public List<ConfigValue> getActiveValuesByItemCode(String itemCode) {
        LambdaQueryWrapper<ConfigItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(ConfigItem::getCode, itemCode);
        ConfigItem item = configItemMapper.selectOne(itemWrapper);
        if (item == null) {
            throw new BusinessException("配置项不存在");
        }
        LambdaQueryWrapper<ConfigValue> valueWrapper = new LambdaQueryWrapper<>();
        valueWrapper.eq(ConfigValue::getConfigItemId, item.getId());
        valueWrapper.eq(ConfigValue::getStatus, 1);
        valueWrapper.orderByAsc(ConfigValue::getCode);
        return configValueMapper.selectList(valueWrapper);
    }
}
