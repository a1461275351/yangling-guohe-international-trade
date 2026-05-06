"""
============================================================
需求文档 3.6 货物管理模块
============================================================
覆盖需求点：
- 3.6.1 手动录入：货物编号、品名、HS编码、规格、型号、单位、单价、图片
- 3.6.2 货物列表：分页展示，支持按品名、HS编码搜索
- 3.6.3 货物删除与修改
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, \
    assert_list_result, assert_data_exists, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("08-货物管理 (需求3.6)")
goods_id_1 = None
goods_id_2 = None


def setup():
    client.login_as_admin()


def test_create_goods_electronic():
    """需求3.6.1：手动录入 - 电子产品（含所有关键字段）"""
    global goods_id_1
    t = ts()
    resp = client.post("/api/goods", {
        "goodsNo": f"LAPTOP_{t}",
        "name": f"笔记本电脑_{t}",
        "hsCode": "8471300000",
        "spec": "15.6英寸 IPS屏幕",
        "model": "ThinkPad T14",
        "unit": "台",
        "price": 8999.00,
        "currency": "CNY",
        "category": "电子产品"
    })
    assert_success(resp)
    if resp.get("data") and resp["data"].get("id"):
        goods_id_1 = resp["data"]["id"]
    else:
        record = find_in_list(client, "/api/goods/list", {},
                              lambda r: r["goodsNo"] == f"LAPTOP_{t}")
        assert record, "电子产品录入失败"
        goods_id_1 = record["id"]


def test_create_goods_textile():
    """需求3.6.1：手动录入 - 纺织品"""
    global goods_id_2
    t = ts()
    resp = client.post("/api/goods", {
        "goodsNo": f"CLOTH_{t}",
        "name": f"棉质T恤_{t}",
        "hsCode": "6109100010",
        "spec": "100%纯棉 XL码",
        "model": "CT-2026",
        "unit": "件",
        "price": 49.90,
        "currency": "CNY",
        "category": "纺织品"
    })
    assert_success(resp)
    if resp.get("data") and resp["data"].get("id"):
        goods_id_2 = resp["data"]["id"]
    else:
        record = find_in_list(client, "/api/goods/list", {},
                              lambda r: r["goodsNo"] == f"CLOTH_{t}")
        assert record, "纺织品录入失败"
        goods_id_2 = record["id"]


def test_list_goods():
    """需求3.6.2：分页展示本企业所有货物"""
    resp = client.post("/api/goods/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=2)


def test_search_by_name():
    """需求3.6.2：按品名搜索"""
    resp = client.post("/api/goods/list", {
        "name": "笔记本",
        "current": 1, "size": 20
    })
    assert_list_result(resp, min_count=1)


def test_search_by_hs_code():
    """需求3.6.2：按HS编码搜索"""
    resp = client.post("/api/goods/list", {
        "hsCode": "8471",
        "current": 1, "size": 20
    })
    assert_list_result(resp, min_count=1)


def test_search_by_category():
    """需求3.6.2：按分类筛选"""
    resp = client.post("/api/goods/list", {
        "category": "电子产品",
        "current": 1, "size": 20
    })
    assert_list_result(resp)


def test_get_goods_detail():
    """需求：查看货物详情"""
    resp = client.get(f"/api/goods/{goods_id_1}")
    assert_data_exists(resp)
    data = resp["data"]
    assert data.get("hsCode") == "8471300000"
    assert data.get("unit") == "台"
    assert float(data.get("price", 0)) > 0


def test_update_goods():
    """需求3.6.3：编辑货物信息"""
    resp = client.put("/api/goods", {
        "id": goods_id_1,
        "price": 7999.00,
        "spec": "14英寸 OLED屏幕"
    })
    assert_success(resp)


def test_get_goods_selection():
    """需求：获取货物选择列表（订单关联用）"""
    resp = client.get("/api/goods/selection")
    assert_success(resp)
    assert isinstance(resp["data"], list)
    assert len(resp["data"]) >= 2


def test_batch_delete():
    """需求3.6.3：批量删除"""
    resp = client.delete("/api/goods/batch", data=[goods_id_2])
    assert_success(resp)


def test_delete_goods():
    """需求3.6.3：单个删除"""
    resp = client.delete(f"/api/goods/{goods_id_1}")
    assert_success(resp)


if __name__ == "__main__":
    print(f"\n{'='*60}")
    print(f"  需求 3.6 货物管理模块")
    print(f"{'='*60}")
    setup()
    runner.run("[3.6.1] 录入电子产品", test_create_goods_electronic)
    runner.run("[3.6.1] 录入纺织品", test_create_goods_textile)
    runner.run("[3.6.2] 货物列表(分页)", test_list_goods)
    runner.run("[3.6.2] 按品名搜索", test_search_by_name)
    runner.run("[3.6.2] 按HS编码搜索", test_search_by_hs_code)
    runner.run("[3.6.2] 按分类筛选", test_search_by_category)
    runner.run("货物详情", test_get_goods_detail)
    runner.run("[3.6.3] 修改货物", test_update_goods)
    runner.run("获取选择列表(订单用)", test_get_goods_selection)
    runner.run("[3.6.3] 批量删除", test_batch_delete)
    runner.run("[3.6.3] 单个删除", test_delete_goods)
    runner.summary()
