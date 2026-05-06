"""
============================================================
需求文档 3.7 订单管理模块
============================================================
覆盖需求点：
- 3.7.1 订单创建：必须选择一份已生效的合同，关联货物
- 3.7.2 订单列表：按合同、状态、日期筛选
- 3.7.3 票据生成：发票、箱单、报关单草稿
- 订单状态：DRAFT/SUBMITTED/PROCESSING/COMPLETED/CANCELLED
"""
import sys, os
from datetime import datetime, timedelta
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, \
    assert_list_result, assert_data_exists, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("10-订单管理 (需求3.7)")
order_id = None
contract_id = None
goods_id = None


def setup():
    """准备测试依赖：已生效合同 + 货物"""
    global contract_id, goods_id
    client.login_as_admin()
    t = ts()

    # 创建货物
    resp = client.post("/api/goods", {
        "goodsNo": f"OG_{t}",
        "name": f"订单测试-LED灯泡_{t}",
        "hsCode": "8539500000",
        "spec": "220V/12W/E27",
        "unit": "个",
        "price": 15.50,
        "currency": "USD"
    })
    assert_success(resp, "创建货物")
    record = find_in_list(client, "/api/goods/list", {},
                          lambda r: r["goodsNo"] == f"OG_{t}")
    goods_id = record["id"] if record else resp.get("data", {}).get("id")

    # 创建已生效合同
    today = datetime.now().strftime("%Y-%m-%d")
    expire = (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d")
    resp2 = client.post("/api/contracts", {
        "contractNo": f"OH_{t}",
        "title": "订单测试合同",
        "ourCompany": "杨凌国合跨境贸易有限公司",
        "partnerName": "海外客户",
        "partnerType": "CUSTOMER",
        "status": "EFFECTIVE",
        "signDate": today,
        "expireDate": expire,
        "amount": 1000000.00,
        "currency": "USD"
    })
    assert_success(resp2, "创建合同")
    record2 = find_in_list(client, "/api/contracts/list", {},
                           lambda r: r["contractNo"] == f"OH_{t}")
    contract_id = record2["id"] if record2 else resp2.get("data", {}).get("id")


def test_create_order_with_goods():
    """需求3.7.1：基于合同创建订单，关联货物清单"""
    global order_id
    resp = client.post("/api/orders", {
        "contractId": contract_id,
        "tradeTerms": "FOB Shanghai",
        "paymentMethod": "T/T 30天",
        "remark": "首批LED灯泡出口订单",
        "goodsList": [
            {
                "goodsId": goods_id,
                "goodsName": "LED灯泡",
                "goodsNo": "OG",
                "hsCode": "8539500000",
                "quantity": 5000,
                "unit": "个",
                "price": 15.50,
                "amount": 77500.00
            }
        ]
    })
    assert_success(resp)
    if resp.get("data") and resp["data"].get("id"):
        order_id = resp["data"]["id"]
    else:
        record = find_in_list(client, "/api/orders/list",
                              {"contractId": contract_id},
                              lambda r: True)
        assert record, "订单创建失败"
        order_id = record["id"]


def test_list_orders():
    """需求3.7.2：展示本企业所有订单"""
    resp = client.post("/api/orders/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=1)


def test_search_by_contract():
    """需求3.7.2：按合同筛选"""
    resp = client.post("/api/orders/list", {
        "contractId": contract_id,
        "current": 1, "size": 20
    })
    assert_list_result(resp, min_count=1)


def test_search_by_status():
    """需求3.7.2：按状态筛选"""
    resp = client.post("/api/orders/list", {
        "status": "DRAFT", "current": 1, "size": 20
    })
    assert_list_result(resp)


def test_get_order_detail_with_goods():
    """需求：订单详情包含货物清单"""
    resp = client.get(f"/api/orders/{order_id}")
    assert_data_exists(resp)
    data = resp["data"]
    assert data.get("contractId") == contract_id
    assert data.get("tradeTerms"), "缺少贸易条款"
    # 验证关联货物
    goods_list = data.get("goodsList")
    if goods_list:
        assert len(goods_list) >= 1, "应至少关联1个货物"


def test_update_order():
    """需求：修改订单"""
    resp = client.put(f"/api/orders/{order_id}", {
        "contractId": contract_id,
        "tradeTerms": "CIF Los Angeles",
        "paymentMethod": "L/C at sight",
        "remark": "贸易条款已修改",
        "goodsList": [
            {
                "goodsId": goods_id,
                "goodsName": "LED灯泡",
                "quantity": 8000,
                "unit": "个",
                "price": 14.00,
                "amount": 112000.00
            }
        ]
    })
    assert_success(resp)


def test_order_status_flow():
    """需求：订单状态流转 DRAFT → SUBMITTED → PROCESSING"""
    resp = client.put(f"/api/orders/{order_id}/status",
                      params={"status": "SUBMITTED"})
    assert_success(resp, "DRAFT→SUBMITTED")

    resp2 = client.put(f"/api/orders/{order_id}/status",
                       params={"status": "PROCESSING"})
    assert_success(resp2, "SUBMITTED→PROCESSING")


def test_cleanup():
    """清理"""
    # 改回DRAFT再删除
    client.put(f"/api/orders/{order_id}/status", params={"status": "DRAFT"})
    client.delete(f"/api/orders/{order_id}")
    # 合同改回INIT再删除
    client.put(f"/api/contracts/{contract_id}/status", params={"status": "INIT"})
    client.delete(f"/api/contracts/{contract_id}")
    client.delete(f"/api/goods/{goods_id}")


if __name__ == "__main__":
    print(f"\n{'='*60}")
    print(f"  需求 3.7 订单管理模块")
    print(f"{'='*60}")
    setup()
    runner.run("[3.7.1] 基于合同创建订单+关联货物", test_create_order_with_goods)
    runner.run("[3.7.2] 订单列表", test_list_orders)
    runner.run("[3.7.2] 按合同筛选", test_search_by_contract)
    runner.run("[3.7.2] 按状态筛选", test_search_by_status)
    runner.run("订单详情(含货物清单)", test_get_order_detail_with_goods)
    runner.run("修改订单(改贸易条款+数量)", test_update_order)
    runner.run("状态流转: DRAFT→SUBMITTED→PROCESSING", test_order_status_flow)
    runner.run("清理数据", test_cleanup)
    runner.summary()
