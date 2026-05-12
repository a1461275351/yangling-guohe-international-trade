package com.trade.platform.module.stats.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trade.platform.module.enterprise.entity.ServiceEnterprise;
import com.trade.platform.module.enterprise.mapper.ServiceEnterpriseMapper;
import com.trade.platform.module.logistics.mapper.LogisticsMapper;
import com.trade.platform.module.taxrefund.entity.TaxRefund;
import com.trade.platform.module.taxrefund.mapper.TaxRefundMapper;
import com.trade.platform.module.settlement.mapper.SettlementMapper;
import com.trade.platform.module.insurance.mapper.InsuranceMapper;
import com.trade.platform.module.financing.mapper.FinancingMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    @Resource
    private ServiceEnterpriseMapper enterpriseMapper;
    @Resource
    private LogisticsMapper logisticsMapper;
    @Resource
    private TaxRefundMapper taxRefundMapper;
    @Resource
    private SettlementMapper settlementMapper;
    @Resource
    private InsuranceMapper insuranceMapper;
    @Resource
    private FinancingMapper financingMapper;

    public Map<String, Object> getCertificationDashboard() {
        Map<String, Object> result = new HashMap<>();

        List<ServiceEnterprise> enterprises = enterpriseMapper.selectList(
                new LambdaQueryWrapper<ServiceEnterprise>().eq(ServiceEnterprise::getStatus, 1));

        int totalEnterprises = enterprises.size();
        long signedCount = enterprises.stream()
                .filter(e -> "SIGNED".equals(e.getAgreementStatus())).count();
        BigDecimal totalExport = enterprises.stream()
                .map(e -> e.getAnnualExportAmount() != null ? e.getAnnualExportAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalImport = enterprises.stream()
                .map(e -> e.getAnnualImportAmount() != null ? e.getAnnualImportAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("enterpriseCount", totalEnterprises);
        result.put("signedAgreementCount", signedCount);
        result.put("annualExportTotal", totalExport);
        result.put("annualImportTotal", totalImport);
        result.put("annualTradeTotal", totalExport.add(totalImport));

        result.put("logisticsCount", logisticsMapper.selectCount(null));
        result.put("taxRefundCount", taxRefundMapper.selectCount(null));
        result.put("settlementCount", settlementMapper.selectCount(null));
        result.put("insuranceCount", insuranceMapper.selectCount(null));
        result.put("financingCount", financingMapper.selectCount(null));

        boolean hasLogistics = (long) result.get("logisticsCount") > 0;
        boolean hasTaxRefund = (long) result.get("taxRefundCount") > 0;
        boolean hasSettlement = (long) result.get("settlementCount") > 0;
        boolean hasInsurance = (long) result.get("insuranceCount") > 0;
        boolean hasFinancing = (long) result.get("financingCount") > 0;
        int coveredServices = (hasLogistics ? 1 : 0) + (hasTaxRefund ? 1 : 0) + (hasSettlement ? 1 : 0)
                + (hasInsurance ? 1 : 0) + (hasFinancing ? 1 : 0) + 1;
        result.put("serviceCoverage", coveredServices + "/6");

        int threshold = 10;
        result.put("enterpriseThreshold", threshold);
        result.put("enterpriseMet", totalEnterprises >= threshold);
        BigDecimal tradeThreshold = new BigDecimal("3500");
        result.put("tradeThreshold", tradeThreshold);
        result.put("tradeMet", totalExport.add(totalImport).compareTo(tradeThreshold) >= 0);

        return result;
    }
}
