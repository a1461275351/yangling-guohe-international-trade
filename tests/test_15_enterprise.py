"""
============================================================
P3 模块测试：服务企业档案（省级外综服认定 - 服务企业清单）
============================================================
覆盖：
- 服务企业 CRUD
- 状态变更
- 活跃列表查询（合同关联用）
- 批量删除
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, \
    assert_list_result, assert_data_exists, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("15-服务企业档案 (P3)")
enterprise_id = None


def setup():
    client.login_as_admin()


def test_create_enterprise():
    """创建服务企业档案"""
    global enterprise_id
    t = ts()
    resp = client.post("/api/enterprises", {
        "enterpriseName": f"陕西测试外贸_{t}",
        "creditCode": f"91610000{t}",
        "region": "陕西省咸阳市",
        "industry": "农副产品",
        "productType": "鲜果出口",
        "isFirstTrade": 1,
        "agreementStatus": "SIGNED",
        "agreementNo": f"XY-{t}",
        "serviceScope": "通关,物流,退税",
        "bizContactName": "张三",
        "bizContactPhone": "13800001111",
        "annualExportAmount": 1500.50,
        "annualImportAmount": 800.00,
        "riskLevel": "LOW",
        "status": 1
    })
    assert_success(resp)
    record = find_in_list(client, "/api/enterprises/list",
                          {"keyword": f"测试外贸_{t}"},
                          lambda r: f"测试外贸_{t}" in r["enterpriseName"])
    assert record, "服务企业创建后查不到"
    enterprise_id = record["id"]


def test_list_enterprises():
    """分页查询服务企业"""
    resp = client.post("/api/enterprises/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=1)


def test_get_enterprise_detail():
    """查询企业详情"""
    resp = client.get(f"/api/enterprises/{enterprise_id}")
    assert_data_exists(resp, "enterpriseName")


def test_update_enterprise():
    """更新企业信息"""
    resp = client.put("/api/enterprises", {
        "id": enterprise_id,
        "enterpriseName": "陕西测试外贸_更新",
        "riskLevel": "MEDIUM",
        "annualExportAmount": 2000.00
    })
    assert_success(resp)
    detail = client.get(f"/api/enterprises/{enterprise_id}")
    assert detail["data"]["riskLevel"] == "MEDIUM", "风险等级未更新"


def test_active_enterprises():
    """活跃企业列表（供合同表单选择）"""
    resp = client.get("/api/enterprises/active")
    assert_success(resp)
    assert isinstance(resp["data"], list), "active 应返回列表"


def test_update_status():
    """禁用服务企业"""
    resp = client.put(f"/api/enterprises/{enterprise_id}/status?status=0")
    assert_success(resp)


def test_filter_by_status():
    """按状态筛选（已禁用）"""
    resp = client.post("/api/enterprises/list",
                       {"status": 0, "current": 1, "size": 20})
    assert_success(resp)


def test_delete_enterprise():
    """删除服务企业"""
    resp = client.delete(f"/api/enterprises/{enterprise_id}")
    assert_success(resp)


def main():
    setup()
    runner.run("创建服务企业档案", test_create_enterprise)
    runner.run("分页查询服务企业", test_list_enterprises)
    runner.run("查询企业详情", test_get_enterprise_detail)
    runner.run("更新企业信息", test_update_enterprise)
    runner.run("活跃企业列表", test_active_enterprises)
    runner.run("禁用服务企业", test_update_status)
    runner.run("按状态筛选", test_filter_by_status)
    runner.run("删除服务企业", test_delete_enterprise)
    return runner.summary()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
