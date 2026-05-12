"""
============================================================
P3 模块测试：融资协助
============================================================
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, \
    assert_list_result, assert_data_exists, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("20-融资协助 (P3)")
financing_id = None


def setup():
    client.login_as_admin()


def test_create_financing():
    global financing_id
    t = ts()
    resp = client.post("/api/financings", {
        "financingNo": f"FN{t}",
        "financingType": "ORDER_FINANCING",
        "bankName": "中国进出口银行",
        "applyAmount": 500000.00,
        "approvedAmount": 450000.00,
        "interestRate": 4.35,
        "applyDate": "2026-05-12",
        "approveDate": "2026-05-15",
        "maturityDate": "2026-11-15"
    })
    assert_success(resp)
    record = find_in_list(client, "/api/financings/list",
                          {"current": 1, "size": 50},
                          lambda r: r.get("financingNo") == f"FN{t}")
    assert record, "融资单创建后查不到"
    financing_id = record["id"]


def test_list():
    resp = client.post("/api/financings/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=1)


def test_filter_by_type():
    resp = client.post("/api/financings/list",
                       {"financingType": "ORDER_FINANCING", "current": 1, "size": 20})
    assert_success(resp)


def test_get_detail():
    resp = client.get(f"/api/financings/{financing_id}")
    assert_data_exists(resp, "financingNo")


def test_update():
    resp = client.put("/api/financings", {
        "id": financing_id,
        "approvedAmount": 480000.00,
        "interestRate": 4.20
    })
    assert_success(resp)


def test_delete():
    resp = client.delete(f"/api/financings/{financing_id}")
    assert_success(resp)


def main():
    setup()
    runner.run("创建融资申请", test_create_financing)
    runner.run("分页查询", test_list)
    runner.run("按融资类型筛选", test_filter_by_type)
    runner.run("查询详情", test_get_detail)
    runner.run("更新融资信息", test_update)
    runner.run("删除融资申请", test_delete)
    return runner.summary()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
