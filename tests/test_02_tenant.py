"""
============================================================
需求文档 3.2 租户管理模块
============================================================
覆盖需求点：
- 租户列表：展示所有企业租户信息（名称、信用代码、状态、联系人等）
- 添加租户：由国合员工或管理员手动创建，录入企业基本信息
- 编辑租户：修改企业基本信息
- 禁用/启用租户：禁用后该企业所有用户无法登录
- 删除租户：彻底删除（需谨慎，涉及数据关联）
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, assert_fail, \
    assert_list_result, assert_data_exists, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("02-租户管理 (需求3.2)")
created_tenant_id = None
created_tenant_code = None


def setup():
    client.login_as_admin()


def test_list_tenants():
    """需求：租户列表展示所有企业租户信息"""
    resp = client.post("/api/tenants/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=1, msg="至少应有默认租户GUOHE")


def test_create_tenant():
    """需求：由管理员手动创建企业租户，录入企业基本信息及资质"""
    global created_tenant_id, created_tenant_code
    t = ts()
    created_tenant_code = f"TC{t}"
    resp = client.post("/api/tenants", {
        "tenantCode": created_tenant_code,
        "name": f"自动化测试企业_{t}",
        "creditCode": f"9161{t}",
        "status": 1,
        "contactPerson": "张经理",
        "contactPhone": "13800000001",
        "address": "陕西省杨凌示范区"
    })
    assert_success(resp)
    # 验证创建成功
    record = find_in_list(client, "/api/tenants/list", {},
                          lambda r: r["tenantCode"] == created_tenant_code)
    assert record, "创建的租户在列表中未找到"
    created_tenant_id = record["id"]


def test_get_tenant_detail():
    """需求：查看租户详情 - 基本信息"""
    resp = client.get(f"/api/tenants/{created_tenant_id}")
    assert_data_exists(resp)
    data = resp["data"]
    assert data["tenantCode"] == created_tenant_code
    assert data["contactPerson"] == "张经理"


def test_update_tenant():
    """需求：修改企业基本信息"""
    resp = client.put("/api/tenants", {
        "id": created_tenant_id,
        "contactPerson": "李总监",
        "contactPhone": "13900000002",
        "address": "陕西省西安市高新区"
    })
    assert_success(resp)
    # 验证修改生效
    resp2 = client.get(f"/api/tenants/{created_tenant_id}")
    assert resp2["data"]["contactPerson"] == "李总监"


def test_disable_tenant():
    """需求：禁用租户 - 禁用后该企业所有用户无法登录"""
    resp = client.put(f"/api/tenants/{created_tenant_id}/status", params={"status": 0})
    assert_success(resp)
    # 验证状态已变更
    resp2 = client.get(f"/api/tenants/{created_tenant_id}")
    assert resp2["data"]["status"] == 0, "租户状态应为禁用(0)"


def test_enable_tenant():
    """需求：重新启用已禁用租户"""
    resp = client.put(f"/api/tenants/{created_tenant_id}/status", params={"status": 1})
    assert_success(resp)


def test_get_all_active_tenants():
    """需求：获取所有活跃租户（用于下拉选择）"""
    resp = client.get("/api/tenants/all")
    assert_success(resp)
    data = resp["data"]
    assert isinstance(data, list)
    assert len(data) >= 1, "至少应有默认租户"
    # 验证只返回活跃租户
    for t in data:
        assert t.get("status") == 1, f"非活跃租户不应出现: {t.get('name')}"


def test_search_tenant_by_name():
    """需求：支持按企业名称搜索"""
    resp = client.post("/api/tenants/list", {
        "name": "自动化测试",
        "current": 1, "size": 20
    })
    assert_list_result(resp, min_count=1)


def test_delete_tenant():
    """需求：删除租户（需谨慎操作，涉及数据关联）"""
    resp = client.delete(f"/api/tenants/{created_tenant_id}")
    assert_success(resp)
    # 验证已删除（逻辑删除）
    record = find_in_list(client, "/api/tenants/list", {},
                          lambda r: r["id"] == created_tenant_id)
    assert record is None, "删除后不应在列表中出现"


if __name__ == "__main__":
    print(f"\n{'='*60}")
    print(f"  需求 3.2 租户管理模块")
    print(f"{'='*60}")
    setup()
    runner.run("租户列表查询", test_list_tenants)
    runner.run("创建新租户", test_create_tenant)
    runner.run("获取租户详情", test_get_tenant_detail)
    runner.run("编辑租户信息", test_update_tenant)
    runner.run("禁用租户", test_disable_tenant)
    runner.run("启用租户", test_enable_tenant)
    runner.run("获取所有活跃租户", test_get_all_active_tenants)
    runner.run("按名称搜索租户", test_search_tenant_by_name)
    runner.run("删除租户", test_delete_tenant)
    runner.summary()
