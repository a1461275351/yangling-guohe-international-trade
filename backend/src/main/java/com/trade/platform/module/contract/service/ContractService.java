package com.trade.platform.module.contract.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.contract.dto.ContractQueryDTO;
import com.trade.platform.module.contract.entity.Contract;
import com.trade.platform.module.contract.mapper.ContractMapper;
import com.trade.platform.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContractService {

    @Autowired
    private ContractMapper contractMapper;

    public PageResult<Contract> getList(ContractQueryDTO dto) {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contract::getTenantId, UserContext.getTenantId());
        if (StringUtils.hasText(dto.getContractNo())) {
            wrapper.like(Contract::getContractNo, dto.getContractNo());
        }
        if (StringUtils.hasText(dto.getPartnerName())) {
            wrapper.like(Contract::getPartnerName, dto.getPartnerName());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Contract::getStatus, dto.getStatus());
        }
        if (dto.getEnterpriseId() != null) {
            wrapper.eq(Contract::getEnterpriseId, dto.getEnterpriseId());
        }
        if (StringUtils.hasText(dto.getContractType())) {
            wrapper.eq(Contract::getContractType, dto.getContractType());
        }
        if (StringUtils.hasText(dto.getStartDate()) && StringUtils.hasText(dto.getEndDate())) {
            wrapper.between(Contract::getCreateTime,
                    LocalDate.parse(dto.getStartDate()).atStartOfDay(),
                    LocalDate.parse(dto.getEndDate()).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(Contract::getCreateTime);

        Page<Contract> page = new Page<>(dto.getCurrent(), dto.getSize());
        contractMapper.selectPage(page, wrapper);
        return PageResult.from(page);
    }

    public Contract getById(Long id) {
        return contractMapper.selectById(id);
    }

    public Contract create(Contract contract) {
        contract.setTenantId(UserContext.getTenantId());
        if (!StringUtils.hasText(contract.getContractNo())) {
            contract.setContractNo(generateContractNo());
        }
        if (!StringUtils.hasText(contract.getCurrency())) {
            contract.setCurrency("CNY");
        }
        if (!StringUtils.hasText(contract.getStatus())) {
            contract.setStatus("INIT");
        }
        contractMapper.insert(contract);
        return contract;
    }

    public Contract update(Contract contract) {
        Contract existing = contractMapper.selectById(contract.getId());
        if (existing == null) {
            throw new BusinessException("合同不存在");
        }
        if (!"INIT".equals(existing.getStatus()) && !"SIGNING".equals(existing.getStatus())) {
            throw new BusinessException("只有初始或签订中状态的合同才能修改");
        }
        contractMapper.updateById(contract);
        return contract;
    }

    public void updateStatus(Long id, String status) {
        Contract existing = contractMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("合同不存在");
        }
        validateStatusTransition(existing.getStatus(), status);
        Contract update = new Contract();
        update.setId(id);
        update.setStatus(status);
        contractMapper.updateById(update);
    }

    public void delete(Long id) {
        Contract existing = contractMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("合同不存在");
        }
        if (!"INIT".equals(existing.getStatus())) {
            throw new BusinessException("只有初始状态的合同才能删除");
        }
        contractMapper.deleteById(id);
    }

    public List<Contract> getActiveContracts() {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contract::getTenantId, UserContext.getTenantId());
        wrapper.in(Contract::getStatus, Arrays.asList("EFFECTIVE", "EXECUTING", "SIGNING"));
        wrapper.orderByDesc(Contract::getCreateTime);
        return contractMapper.selectList(wrapper);
    }

    public List<Contract> getExpiringContracts(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contract::getTenantId, UserContext.getTenantId());
        wrapper.in(Contract::getStatus, Arrays.asList("EFFECTIVE", "EXECUTING"));
        wrapper.le(Contract::getExpireDate, deadline);
        wrapper.ge(Contract::getExpireDate, LocalDate.now());
        wrapper.orderByAsc(Contract::getExpireDate);
        return contractMapper.selectList(wrapper);
    }

    public Map<String, Object> getExpiringStats() {
        Long tenantId = UserContext.getTenantId();
        LocalDate now = LocalDate.now();

        LambdaQueryWrapper<Contract> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.eq(Contract::getTenantId, tenantId);
        baseWrapper.in(Contract::getStatus, Arrays.asList("EFFECTIVE", "EXECUTING"));
        baseWrapper.ge(Contract::getExpireDate, now);

        // total expiring (within 90 days as a reasonable default)
        LambdaQueryWrapper<Contract> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Contract::getTenantId, tenantId);
        totalWrapper.in(Contract::getStatus, Arrays.asList("EFFECTIVE", "EXECUTING"));
        totalWrapper.ge(Contract::getExpireDate, now);
        totalWrapper.le(Contract::getExpireDate, now.plusDays(90));
        long total = contractMapper.selectCount(totalWrapper);

        // this week
        LambdaQueryWrapper<Contract> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.eq(Contract::getTenantId, tenantId);
        weekWrapper.in(Contract::getStatus, Arrays.asList("EFFECTIVE", "EXECUTING"));
        weekWrapper.ge(Contract::getExpireDate, now);
        weekWrapper.le(Contract::getExpireDate, now.plusDays(7));
        long thisWeek = contractMapper.selectCount(weekWrapper);

        // this month
        LambdaQueryWrapper<Contract> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.eq(Contract::getTenantId, tenantId);
        monthWrapper.in(Contract::getStatus, Arrays.asList("EFFECTIVE", "EXECUTING"));
        monthWrapper.ge(Contract::getExpireDate, now);
        monthWrapper.le(Contract::getExpireDate, now.plusDays(30));
        long thisMonth = contractMapper.selectCount(monthWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("thisWeek", thisWeek);
        stats.put("thisMonth", thisMonth);
        return stats;
    }

    private String generateContractNo() {
        String prefix = "HT";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = contractMapper.selectCount(new LambdaQueryWrapper<Contract>()
                .likeRight(Contract::getContractNo, prefix + date));
        return prefix + date + String.format("%04d", count + 1);
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        Map<String, List<String>> transitions = new HashMap<>();
        transitions.put("INIT", Arrays.asList("SIGNING", "DESTROYED"));
        transitions.put("SIGNING", Arrays.asList("EFFECTIVE", "DESTROYED"));
        transitions.put("EFFECTIVE", Arrays.asList("EXECUTING", "EXPIRED", "DESTROYED"));
        transitions.put("EXECUTING", Arrays.asList("COMPLETED", "EXPIRED", "DESTROYED"));
        transitions.put("COMPLETED", Arrays.asList());
        transitions.put("EXPIRED", Arrays.asList());
        transitions.put("DESTROYED", Arrays.asList());

        List<String> allowed = transitions.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new BusinessException("不允许从状态[" + currentStatus + "]变更为[" + newStatus + "]");
        }
    }
}
