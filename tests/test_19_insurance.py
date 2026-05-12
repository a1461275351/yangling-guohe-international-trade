"""
============================================================
P3 模块测试：信保服务
============================================================
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, \
    assert_list_result, assert_data_exists, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("19-信保服务 (P3)")
insurance_id = None


def setup():
    client.login_as_admin()


def test_create_insurance():
    global insurance_id
    t = ts()
    resp = client.post("/api/insurances", {
        "policyNo": f"PL{t}",
        "buyerName": "EU Trade Corp",
        "buyerCountry": "Netherlands",
        "creditLimit": 100000.00,
        "insuredAmount": 80000.00,
        "premium": 720.00,
        "coverageStart": "2026-05-12",
        "coverageEnd": "2027-05-11",
        "shipmentAmount": 80000.00
    })
    assert_success(resp)
    record = find_in_list(client, "/api/insurances/list",
                          {"current": 1, "size": 50},
                          lambda r: r.get("policyNo") == f"PL{t}")
    assert record, "保单创建后查不到"
    insurance_id = record["id"]


def test_list():
    resp = client.post("/api/insurances/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=1)


def test_get_detail():
    resp = client.get(f"/api/insurances/{insurance_id}")
    assert_data_exists(resp, "policyNo")


def test_update():
    resp = client.put("/api/insurances", {
        "id": insurance_id,
        "creditLimit": 120000.00,
        "premium": 850.00
    })
    assert_success(resp)


def test_delete():
    resp = client.delete(f"/api/insurances/{insurance_id}")
    assert_success(resp)


def main():
    setup()
    runner.run("创建信保保单", test_create_insurance)
    runner.run("分页查询", test_list)
    runner.run("查询详情", test_get_detail)
    runner.run("更新保单", test_update)
    runner.run("删除保单", test_delete)
    return runner.summary()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
