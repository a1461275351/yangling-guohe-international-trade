"""
============================================================
P3 模块测试：结算收汇
============================================================
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, \
    assert_list_result, assert_data_exists, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("18-结算收汇 (P3)")
settlement_id = None


def setup():
    client.login_as_admin()


def test_create_settlement():
    global settlement_id
    t = ts()
    resp = client.post("/api/settlements", {
        "settlementNo": f"ST{t}",
        "settlementType": "RECEIPT",
        "currency": "USD",
        "amount": 50000.00,
        "exchangeRate": 7.1500,
        "rmbAmount": 357500.00,
        "bankName": "中国银行",
        "bankAccount": "6217001234567890"
    })
    assert_success(resp)
    record = find_in_list(client, "/api/settlements/list",
                          {"current": 1, "size": 50},
                          lambda r: r.get("settlementNo") == f"ST{t}")
    assert record, "结算单创建后查不到"
    settlement_id = record["id"]


def test_list():
    resp = client.post("/api/settlements/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=1)


def test_filter_by_type():
    resp = client.post("/api/settlements/list",
                       {"settlementType": "RECEIPT", "current": 1, "size": 20})
    assert_success(resp)


def test_get_detail():
    resp = client.get(f"/api/settlements/{settlement_id}")
    assert_data_exists(resp, "settlementNo")


def test_update():
    resp = client.put("/api/settlements", {
        "id": settlement_id,
        "exchangeRate": 7.2000,
        "rmbAmount": 360000.00
    })
    assert_success(resp)


def test_delete():
    resp = client.delete(f"/api/settlements/{settlement_id}")
    assert_success(resp)


def main():
    setup()
    runner.run("创建结算单", test_create_settlement)
    runner.run("分页查询", test_list)
    runner.run("按类型筛选", test_filter_by_type)
    runner.run("查询详情", test_get_detail)
    runner.run("更新结算信息", test_update)
    runner.run("删除结算单", test_delete)
    return runner.summary()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
