"""
============================================================
P3 模块测试：省级外综服认定驾驶舱 + Excel 导出
============================================================
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, \
    assert_data_exists
from config import *

client = ApiClient()
runner = TestRunner("21-认定驾驶舱 (P3)")


def setup():
    client.login_as_admin()


def test_dashboard():
    """获取认定驾驶舱核心指标"""
    resp = client.get("/api/stats/certification-dashboard")
    assert_success(resp)
    data = resp.get("data") or {}
    # 三大核心指标必须存在
    for key in ["enterpriseCount", "annualTradeTotal", "serviceCoverage",
                "enterpriseThreshold", "tradeThreshold"]:
        assert key in data, f"驾驶舱数据缺少字段 {key}"
    # 达标标识
    assert "enterpriseMet" in data, "缺少企业数达标标识"
    assert "tradeMet" in data, "缺少进出口额达标标识"


def test_dashboard_service_breakdown():
    """六大服务模块数据量统计"""
    resp = client.get("/api/stats/certification-dashboard")
    data = resp.get("data") or {}
    for key in ["logisticsCount", "taxRefundCount",
                "settlementCount", "insuranceCount", "financingCount"]:
        assert key in data, f"驾驶舱缺少 {key}"


def test_export_enterprises():
    """导出服务企业清单 Excel"""
    r = client.download("/api/stats/export/enterprises")
    assert r.status_code == 200, f"导出失败 status={r.status_code}"
    assert r.headers.get("Content-Type", "").startswith(
        "application/vnd.openxmlformats-officedocument"
    ) or "spreadsheet" in r.headers.get("Content-Type", "").lower(), \
        f"返回非 Excel: {r.headers.get('Content-Type')}"
    assert len(r.content) > 1000, "导出内容过小"


def test_export_trade_stats():
    """导出进出口统计 Excel"""
    r = client.download("/api/stats/export/trade-stats")
    assert r.status_code == 200, f"导出失败 status={r.status_code}"
    assert len(r.content) > 500, "导出内容过小"


def test_export_service_summary():
    """导出综合服务概览 Excel"""
    r = client.download("/api/stats/export/service-summary")
    assert r.status_code == 200, f"导出失败 status={r.status_code}"
    assert len(r.content) > 500, "导出内容过小"


def main():
    setup()
    runner.run("驾驶舱核心指标", test_dashboard)
    runner.run("六大服务模块数据量", test_dashboard_service_breakdown)
    runner.run("导出服务企业清单", test_export_enterprises)
    runner.run("导出进出口统计", test_export_trade_stats)
    runner.run("导出综合服务概览", test_export_service_summary)
    return runner.summary()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
