"""
============================================================
需求文档 3.11 供应商和客户管理模块
============================================================
覆盖需求点：
- 3.11.1 供应商列表：名称、地址、联系人、统一社会信用代码
- 3.11.2 客户列表：字段同供应商
- 手动添加、编辑、删除、禁用
- 供应商和客户信息可在创建合同时被引用
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, \
    assert_list_result, assert_data_exists, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("07-供应商客户管理 (需求3.11)")
supplier_id = None
customer_id = None


def setup():
    client.login_as_admin()


def test_create_supplier():
    """需求3.11.1：手动添加供应商（名称、地址、联系人、信用代码）"""
    global supplier_id
    t = ts()
    resp = client.post("/api/partners", {
        "type": "SUPPLIER",
        "name": f"深圳华为供应商_{t}",
        "creditCode": f"91440300{t}",
        "address": "广东省深圳市龙岗区",
        "province": "广东",
        "city": "深圳市",
        "district": "龙岗区",
        "contactName": "供应商联系人",
        "contactPhone": "13700137001",
        "contactEmail": f"supplier{t}@hw.com",
        "status": 1
    })
    assert_success(resp)
    record = find_in_list(client, "/api/partners/list", {"type": "SUPPLIER"},
                          lambda r: f"华为供应商_{t}" in r["name"])
    assert record, "供应商创建失败"
    supplier_id = record["id"]


def test_create_customer():
    """需求3.11.2：手动添加客户，字段同供应商"""
    global customer_id
    t = ts()
    resp = client.post("/api/partners", {
        "type": "CUSTOMER",
        "name": f"美国ABC贸易_{t}",
        "creditCode": f"US{t}",
        "address": "New York, USA",
        "contactName": "John Smith",
        "contactPhone": "+1-212-555-0100",
        "contactEmail": f"john{t}@abc.com",
        "status": 1
    })
    assert_success(resp)
    record = find_in_list(client, "/api/partners/list", {"type": "CUSTOMER"},
                          lambda r: f"ABC贸易_{t}" in r["name"])
    assert record, "客户创建失败"
    customer_id = record["id"]


def test_list_suppliers():
    """需求：供应商列表，按类型筛选"""
    resp = client.post("/api/partners/list", {
        "type": "SUPPLIER", "current": 1, "size": 20
    })
    assert_list_result(resp, min_count=1)


def test_list_customers():
    """需求：客户列表"""
    resp = client.post("/api/partners/list", {
        "type": "CUSTOMER", "current": 1, "size": 20
    })
    assert_list_result(resp, min_count=1)


def test_get_supplier_detail():
    """需求：查看供应商详情"""
    resp = client.get(f"/api/partners/{supplier_id}")
    assert_data_exists(resp)
    data = resp["data"]
    assert data["type"] == "SUPPLIER"
    assert data.get("contactName"), "详情缺少联系人"
    assert data.get("creditCode"), "详情缺少信用代码"


def test_update_supplier():
    """需求：编辑供应商"""
    resp = client.put("/api/partners", {
        "id": supplier_id,
        "contactName": "供应商新联系人",
        "address": "广东省深圳市南山区"
    })
    assert_success(resp)


def test_disable_enable_supplier():
    """需求：禁用/启用供应商"""
    resp = client.put(f"/api/partners/{supplier_id}/status", params={"status": 0})
    assert_success(resp, "禁用应成功")
    resp2 = client.put(f"/api/partners/{supplier_id}/status", params={"status": 1})
    assert_success(resp2, "启用应成功")


def test_get_active_suppliers_for_contract():
    """需求：供应商客户信息可在创建合同时被引用（活跃列表）"""
    resp = client.get("/api/partners/active", params={"type": "SUPPLIER"})
    assert_success(resp)
    assert isinstance(resp["data"], list)
    assert len(resp["data"]) >= 1


def test_search_by_name():
    """需求：支持按名称搜索"""
    resp = client.post("/api/partners/list", {
        "name": "华为", "current": 1, "size": 20
    })
    assert_list_result(resp)


def test_cleanup():
    """清理测试数据"""
    client.delete(f"/api/partners/{supplier_id}")
    client.delete(f"/api/partners/{customer_id}")


if __name__ == "__main__":
    print(f"\n{'='*60}")
    print(f"  需求 3.11 供应商和客户管理模块")
    print(f"{'='*60}")
    setup()
    runner.run("[3.11.1] 添加供应商", test_create_supplier)
    runner.run("[3.11.2] 添加客户", test_create_customer)
    runner.run("供应商列表(按类型)", test_list_suppliers)
    runner.run("客户列表", test_list_customers)
    runner.run("供应商详情", test_get_supplier_detail)
    runner.run("编辑供应商", test_update_supplier)
    runner.run("禁用/启用供应商", test_disable_enable_supplier)
    runner.run("获取活跃列表(合同引用)", test_get_active_suppliers_for_contract)
    runner.run("按名称搜索", test_search_by_name)
    runner.run("清理数据", test_cleanup)
    runner.summary()
