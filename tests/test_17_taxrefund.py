"""
============================================================
P3 模块测试：退税业务
============================================================
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, \
    assert_list_result, assert_data_exists, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("17-退税业务 (P3)")
taxrefund_id = None


def setup():
    client.login_as_admin()


def test_create_taxrefund():
    global taxrefund_id
    t = ts()
    resp = client.post("/api/tax-refunds", {
        "refundNo": f"TR{t}",
        "invoiceNo": f"INV{t}",
        "invoiceAmount": 100000.00,
        "refundRate": 13.0,
        "refundAmount": 13000.00,
        "applyDate": "2026-05-12",
        "status": "APPLIED"
    })
    assert_success(resp)
    record = find_in_list(client, "/api/tax-refunds/list",
                          {"current": 1, "size": 50},
                          lambda r: r.get("refundNo") == f"TR{t}")
    assert record, "退税申请创建后查不到"
    taxrefund_id = record["id"]


def test_list():
    resp = client.post("/api/tax-refunds/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=1)


def test_filter_by_status():
    resp = client.post("/api/tax-refunds/list",
                       {"status": "APPLIED", "current": 1, "size": 20})
    assert_success(resp)


def test_get_detail():
    resp = client.get(f"/api/tax-refunds/{taxrefund_id}")
    assert_data_exists(resp, "refundNo")


def test_update():
    resp = client.put("/api/tax-refunds", {
        "id": taxrefund_id,
        "actualRefund": 12800.00,
        "refundDate": "2026-06-01"
    })
    assert_success(resp)


def test_update_status():
    resp = client.put(f"/api/tax-refunds/{taxrefund_id}/status?status=APPROVED")
    assert_success(resp)


def test_delete():
    resp = client.delete(f"/api/tax-refunds/{taxrefund_id}")
    assert_success(resp)


def main():
    setup()
    runner.run("创建退税申请", test_create_taxrefund)
    runner.run("分页查询", test_list)
    runner.run("按状态筛选", test_filter_by_status)
    runner.run("查询详情", test_get_detail)
    runner.run("更新退税信息", test_update)
    runner.run("更新状态", test_update_status)
    runner.run("删除退税申请", test_delete)
    return runner.summary()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
