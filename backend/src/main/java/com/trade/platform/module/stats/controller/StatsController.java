package com.trade.platform.module.stats.controller;

import com.trade.platform.common.OpLog;
import com.trade.platform.common.Result;
import com.trade.platform.module.stats.service.ExportService;
import com.trade.platform.module.stats.service.StatsService;
import com.trade.platform.security.RequireRole;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequireRole({"ADMIN", "GUOHE"})
public class StatsController {

    @Resource
    private StatsService statsService;

    @Resource
    private ExportService exportService;

    @GetMapping("/certification-dashboard")
    public Result<Map<String, Object>> getCertificationDashboard() {
        return Result.success(statsService.getCertificationDashboard());
    }

    @OpLog(module = "认定驾驶舱", action = "EXPORT", description = "导出服务企业清单")
    @GetMapping("/export/enterprises")
    public void exportEnterprises(HttpServletResponse response) throws IOException {
        exportService.exportEnterpriseList(response);
    }

    @OpLog(module = "认定驾驶舱", action = "EXPORT", description = "导出进出口统计")
    @GetMapping("/export/trade-stats")
    public void exportTradeStats(HttpServletResponse response) throws IOException {
        exportService.exportTradeStats(response);
    }

    @OpLog(module = "认定驾驶舱", action = "EXPORT", description = "导出综合服务概览")
    @GetMapping("/export/service-summary")
    public void exportServiceSummary(HttpServletResponse response) throws IOException {
        exportService.exportServiceSummary(response);
    }
}
