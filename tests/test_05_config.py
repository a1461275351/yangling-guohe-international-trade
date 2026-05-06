"""
============================================================
需求文档 3.3 配置管理模块
============================================================
覆盖需求点：
- 配置项列表：管理配置项元数据（如"国别"、"报关口岸"）
- 配置项：新增、删除、修改、查询、启用、禁用
- 配置值列表：管理具体配置值（如"中国"、"美国"、"上海海关"）
- 配置值：CRUD，支持启用/禁用
- 禁用的配置值在前端下拉选项中不显示
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, \
    assert_list_result, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("05-配置管理 (需求3.3)")
item_id = None
item_code = None
val_id_1 = None
val_id_2 = None


def setup():
    client.login_as_admin()


# ========== 配置项 ==========

def test_create_config_item():
    """需求：管理配置项元数据，如'国别'、'报关口岸'"""
    global item_id, item_code
    t = ts()
    item_code = f"COUNTRY_{t}"
    resp = client.post("/api/config/items", {
        "code": item_code,
        "name": f"国别配置_{t}",
        "status": 1
    })
    assert_success(resp)
    record = find_in_list(client, "/api/config/items/list", {},
                          lambda r: r["code"] == item_code)
    assert record, "配置项未创建成功"
    item_id = record["id"]


def test_list_config_items():
    """需求：配置项列表查询"""
    resp = client.post("/api/config/items/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=1)


def test_update_config_item():
    """需求：修改配置项"""
    resp = client.put("/api/config/items", {
        "id": item_id,
        "name": "国别配置_已修改"
    })
    assert_success(resp)


def test_disable_enable_config_item():
    """需求：启用/禁用配置项"""
    resp = client.put(f"/api/config/items/{item_id}/status", params={"status": 0})
    assert_success(resp, "禁用配置项应成功")
    resp2 = client.put(f"/api/config/items/{item_id}/status", params={"status": 1})
    assert_success(resp2, "启用配置项应成功")


# ========== 配置值 ==========

def test_create_config_values():
    """需求：管理具体配置值，如'中国'、'美国'"""
    global val_id_1, val_id_2
    t = ts()
    for i, (code, name) in enumerate([("CN", "中国"), ("US", "美国")]):
        resp = client.post("/api/config/values", {
            "configItemId": item_id,
            "code": f"{code}_{t}",
            "name": name,
            "status": 1
        })
        assert_success(resp, f"创建配置值'{name}'应成功")

    # 获取IDs
    resp2 = client.post("/api/config/values/list", {
        "configItemId": item_id, "current": 1, "size": 100
    })
    records = resp2["data"]["records"]
    for r in records:
        if r["name"] == "中国":
            val_id_1 = r["id"]
        if r["name"] == "美国":
            val_id_2 = r["id"]
    assert val_id_1 and val_id_2, "配置值创建后未找到"


def test_list_config_values_by_item():
    """需求：支持按配置项筛选"""
    resp = client.post("/api/config/values/list", {
        "configItemId": item_id,
        "current": 1, "size": 20
    })
    assert_list_result(resp, min_count=2)


def test_update_config_value():
    """需求：修改配置值"""
    resp = client.put("/api/config/values", {
        "id": val_id_1,
        "name": "中华人民共和国"
    })
    assert_success(resp)


def test_disable_config_value():
    """需求：禁用的配置值在前端下拉选项中不显示"""
    resp = client.put(f"/api/config/values/{val_id_2}/status", params={"status": 0})
    assert_success(resp, "禁用配置值应成功")


def test_get_active_values_by_code():
    """需求：按编码获取活跃配置值（前端下拉用）"""
    resp = client.get("/api/config/values/by-item-code", params={"code": item_code})
    assert_success(resp)
    data = resp["data"]
    assert isinstance(data, list)
    # 禁用的"美国"不应出现
    names = [v["name"] for v in data]
    assert "美国" not in names, "禁用的配置值不应在活跃列表中"


def test_cleanup():
    """清理测试数据"""
    client.delete(f"/api/config/values/{val_id_1}")
    client.delete(f"/api/config/values/{val_id_2}")
    resp = client.delete(f"/api/config/items/{item_id}")
    assert_success(resp)


if __name__ == "__main__":
    print(f"\n{'='*60}")
    print(f"  需求 3.3 配置管理模块")
    print(f"{'='*60}")
    setup()
    runner.run("创建配置项(如:国别)", test_create_config_item)
    runner.run("配置项列表查询", test_list_config_items)
    runner.run("修改配置项", test_update_config_item)
    runner.run("禁用/启用配置项", test_disable_enable_config_item)
    runner.run("创建配置值(中国/美国)", test_create_config_values)
    runner.run("按配置项筛选配置值", test_list_config_values_by_item)
    runner.run("修改配置值", test_update_config_value)
    runner.run("禁用配置值", test_disable_config_value)
    runner.run("获取活跃值(禁用项不显示)", test_get_active_values_by_code)
    runner.run("清理测试数据", test_cleanup)
    runner.summary()
