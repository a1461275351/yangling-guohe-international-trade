"""
============================================================
P3 模块测试：物流服务
============================================================
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, \
    assert_list_result, assert_data_exists, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("16-物流服务 (P3)")
logistics_id = None


def setup():
    client.login_as_admin()


def test_create_logistics():
    """创建物流运单"""
    global logistics_id
    t = ts()
    resp = client.post("/api/logistics", {
        "logisticsNo": f"LG{t}",
        "transportMode": "SEA",
        "departurePort": "上海港",
        "destinationPort": "鹿特丹港",
        "vesselVoyage": "MAERSK-001/2026",
        "blNo": f"BL{t}",
        "logisticsProvider": "马士基航运",
        "freightAmount": 12500.00,
        "freightCurrency": "USD",
        "insuranceAmount": 500.00,
        "etd": "2026-05-20",
        "status": 1
    })
    assert_success(resp)
    record = find_in_list(client, "/api/logistics/list",
                          {"current": 1, "size": 50},
                          lambda r: r.get("logisticsNo") == f"LG{t}")
    assert record, "运单创建后查不到"
    logistics_id = record["id"]


def test_list_logistics():
    """分页查询物流"""
    resp = client.post("/api/logistics/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=1)


def test_filter_by_transport_mode():
    """按运输方式筛选"""
    resp = client.post("/api/logistics/list",
                       {"transportMode": "SEA", "current": 1, "size": 20})
    assert_success(resp)


def test_get_detail():
    """查询物流详情"""
    resp = client.get(f"/api/logistics/{logistics_id}")
    assert_data_exists(resp, "logisticsNo")


def test_update_logistics():
    """更新物流信息"""
    resp = client.put("/api/logistics", {
        "id": logistics_id,
        "freightAmount": 13000.00,
        "vesselVoyage": "MAERSK-002/2026"
    })
    assert_success(resp)


def test_update_status():
    """更新物流状态"""
    resp = client.put(f"/api/logistics/{logistics_id}/status?status=2")
    assert_success(resp)


def test_delete_logistics():
    """删除物流运单"""
    resp = client.delete(f"/api/logistics/{logistics_id}")
    assert_success(resp)


def main():
    setup()
    runner.run("创建物流运单", test_create_logistics)
    runner.run("分页查询物流", test_list_logistics)
    runner.run("按运输方式筛选", test_filter_by_transport_mode)
    runner.run("查询物流详情", test_get_detail)
    runner.run("更新物流信息", test_update_logistics)
    runner.run("更新物流状态", test_update_status)
    runner.run("删除物流运单", test_delete_logistics)
    return runner.summary()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
