package com.trade.platform.module.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.contract.entity.Contract;
import com.trade.platform.module.contract.mapper.ContractMapper;
import com.trade.platform.module.order.dto.OrderCreateDTO;
import com.trade.platform.module.order.dto.OrderQueryDTO;
import com.trade.platform.module.order.entity.Order;
import com.trade.platform.module.order.entity.OrderGoods;
import com.trade.platform.module.order.mapper.OrderGoodsMapper;
import com.trade.platform.module.order.mapper.OrderMapper;
import com.trade.platform.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderGoodsMapper orderGoodsMapper;

    @Autowired
    private ContractMapper contractMapper;

    public PageResult<Order> getList(OrderQueryDTO dto) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getTenantId, UserContext.getTenantId());
        if (StringUtils.hasText(dto.getOrderNo())) {
            wrapper.like(Order::getOrderNo, dto.getOrderNo());
        }
        if (dto.getContractId() != null) {
            wrapper.eq(Order::getContractId, dto.getContractId());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Order::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getStartDate()) && StringUtils.hasText(dto.getEndDate())) {
            wrapper.between(Order::getCreateTime,
                    LocalDate.parse(dto.getStartDate()).atStartOfDay(),
                    LocalDate.parse(dto.getEndDate()).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> page = new Page<>(dto.getCurrent(), dto.getSize());
        orderMapper.selectPage(page, wrapper);
        return PageResult.from(page);
    }

    public Order getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        List<OrderGoods> goodsList = orderGoodsMapper.selectList(
                new LambdaQueryWrapper<OrderGoods>().eq(OrderGoods::getOrderId, id));
        order.setGoodsList(goodsList);
        return order;
    }

    @Transactional
    public Order create(OrderCreateDTO dto) {
        // Validate contract
        Contract contract = contractMapper.selectById(dto.getContractId());
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }
        if (!Arrays.asList("EFFECTIVE", "EXECUTING").contains(contract.getStatus())) {
            throw new BusinessException("合同状态不允许创建订单，合同需为生效或执行中状态");
        }

        // Build order
        Order order = new Order();
        order.setTenantId(UserContext.getTenantId());
        order.setOrderNo(generateOrderNo());
        order.setContractId(dto.getContractId());
        order.setTradeTerms(dto.getTradeTerms());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setRemark(dto.getRemark());
        order.setStatus("DRAFT");
        order.setCurrency("CNY");

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getGoodsList() != null) {
            for (OrderGoods goods : dto.getGoodsList()) {
                if (goods.getAmount() != null) {
                    totalAmount = totalAmount.add(goods.getAmount());
                } else if (goods.getPrice() != null && goods.getQuantity() != null) {
                    BigDecimal amount = goods.getPrice().multiply(goods.getQuantity());
                    goods.setAmount(amount);
                    totalAmount = totalAmount.add(amount);
                }
            }
        }
        order.setTotalAmount(totalAmount);

        orderMapper.insert(order);

        // Save order goods
        if (dto.getGoodsList() != null) {
            for (OrderGoods goods : dto.getGoodsList()) {
                goods.setId(null);
                goods.setOrderId(order.getId());
                orderGoodsMapper.insert(goods);
            }
        }

        return order;
    }

    @Transactional
    public Order update(Long id, OrderCreateDTO dto) {
        Order existing = orderMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new BusinessException("只有草稿状态的订单才能修改");
        }

        // Update order fields
        existing.setContractId(dto.getContractId());
        existing.setTradeTerms(dto.getTradeTerms());
        existing.setPaymentMethod(dto.getPaymentMethod());
        existing.setRemark(dto.getRemark());

        // Delete old goods
        orderGoodsMapper.delete(new LambdaQueryWrapper<OrderGoods>()
                .eq(OrderGoods::getOrderId, id));

        // Calculate total and insert new goods
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getGoodsList() != null) {
            for (OrderGoods goods : dto.getGoodsList()) {
                if (goods.getAmount() == null && goods.getPrice() != null && goods.getQuantity() != null) {
                    goods.setAmount(goods.getPrice().multiply(goods.getQuantity()));
                }
                if (goods.getAmount() != null) {
                    totalAmount = totalAmount.add(goods.getAmount());
                }
                goods.setId(null);
                goods.setOrderId(id);
                orderGoodsMapper.insert(goods);
            }
        }
        existing.setTotalAmount(totalAmount);

        orderMapper.updateById(existing);
        return existing;
    }

    public void updateStatus(Long id, String status) {
        Order existing = orderMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("订单不存在");
        }
        Order update = new Order();
        update.setId(id);
        update.setStatus(status);
        orderMapper.updateById(update);
    }

    @Transactional
    public void delete(Long id) {
        Order existing = orderMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new BusinessException("只有草稿状态的订单才能删除");
        }
        orderGoodsMapper.delete(new LambdaQueryWrapper<OrderGoods>()
                .eq(OrderGoods::getOrderId, id));
        orderMapper.deleteById(id);
    }

    public Order getOrderDetail(Long id) {
        Order order = getById(id);
        if (order.getContractId() != null) {
            Contract contract = contractMapper.selectById(order.getContractId());
            if (contract != null) {
                order.setContractNo(contract.getContractNo());
                order.setContractTitle(contract.getTitle());
            }
        }
        return order;
    }

    private String generateOrderNo() {
        String prefix = "DD";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .likeRight(Order::getOrderNo, prefix + date));
        return prefix + date + String.format("%04d", count + 1);
    }
}
