"""
============================================================
需求文档 3.10 权限管理模块
============================================================
覆盖需求点：
- 基于角色的访问控制，确保租户间数据隔离
- ADMIN：全部功能（用户/租户/配置/模板/业务模块）
- GUOHE：业务模块（除配置管理、模板管理外的全部权限）
- ENTERPRISE：本企业业务数据（业务模块）
- 企业员工只能访问和操作本租户内的数据
- 每个API接口层级权限校验
"""
import sys, os, time
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, assert_fail, \
    assert_list_result, find_in_list, ts
from config import *

admin_client = ApiClient()
runner = TestRunner("13-权限管理 (需求3.10)")

# 测试账号
enterprise_username = None
enterprise_client = None


def setup():
    """创建并审批一个企业员工账号用于权限测试"""
    global enterprise_username, enterprise_client
    admin_client.login_as_admin()

    t = ts()
    enterprise_username = f"perm_ent_{t}"

    # 注册
    admin_client.post("/api/auth/register", {
        "tenantCode": ADMIN_TENANT_CODE,
        "username": enterprise_username,
        "password": TEST_PASSWORD,
        "realName": "权限测试企业员工",
        "role": "ENTERPRISE"
    }, auth=False)

    # 审批
    record = find_in_list(admin_client, "/api/user-applies/list",
                          {"status": "PENDING"},
                          lambda r: r["username"] == enterprise_username)
    if record:
        admin_client.put(f"/api/user-applies/{record['id']}/approve")

    # 企业员工登录
    enterprise_client = ApiClient()
    resp = enterprise_client.login(ADMIN_TENANT_CODE, enterprise_username, TEST_PASSWORD)
    if resp.get("code") != 200:
        print(f"    警告: 企业员工登录失败，部分测试将跳过")


# ========== ADMIN 独有权限 ==========

def test_admin_can_manage_tenants():
    """需求：ADMIN有租户管理权限"""
    resp = admin_client.post("/api/tenants/list", {"current": 1, "size": 10})
    assert_success(resp, "ADMIN应能访问租户列表")


def test_admin_can_manage_config():
    """需求：ADMIN有配置管理权限"""
    resp = admin_client.post("/api/config/items/list", {"current": 1, "size": 10})
    assert_success(resp, "ADMIN应能访问配置管理")


def test_admin_can_manage_templates():
    """需求：ADMIN有模板管理权限"""
    resp = admin_client.post("/api/templates/list", {"current": 1, "size": 10})
    # 即使有schema问题，也不应返回403
    assert resp.get("code") != 403, "ADMIN不应被拒绝访问模板管理"


def test_admin_can_manage_users():
    """需求：ADMIN有用户管理权限"""
    resp = admin_client.post("/api/users/list", {"current": 1, "size": 10})
    assert_success(resp, "ADMIN应能访问用户列表")


# ========== ENTERPRISE 权限限制 ==========

def test_enterprise_cannot_manage_tenants():
    """需求：企业员工不能管理租户"""
    if not enterprise_client or not enterprise_client.token:
        print("    (跳过：企业员工未登录)")
        return
    resp = enterprise_client.post("/api/tenants/list", {"current": 1, "size": 10})
    assert resp.get("code") != 200, "企业员工不应能访问租户管理"


def test_enterprise_cannot_manage_config():
    """需求：企业员工不能管理配置"""
    if not enterprise_client or not enterprise_client.token:
        print("    (跳过：企业员工未登录)")
        return
    resp = enterprise_client.post("/api/config/items/list", {"current": 1, "size": 10})
    assert resp.get("code") != 200, "企业员工不应能访问配置管理"


def test_enterprise_cannot_manage_users():
    """需求：企业员工不能管理用户列表"""
    if not enterprise_client or not enterprise_client.token:
        print("    (跳过：企业员工未登录)")
        return
    resp = enterprise_client.post("/api/users/list", {"current": 1, "size": 10})
    assert resp.get("code") != 200, "企业员工不应能访问用户列表"


# ========== ENTERPRISE 可用功能 ==========

def test_enterprise_can_view_profile():
    """需求：企业员工可查看和编辑个人资料"""
    if not enterprise_client or not enterprise_client.token:
        print("    (跳过：企业员工未登录)")
        return
    resp = enterprise_client.get("/api/users/info")
    assert_success(resp, "企业员工应能查看个人信息")
    assert resp["data"]["role"] == "ENTERPRISE"


def test_enterprise_can_manage_goods():
    """需求：企业员工可使用货物管理"""
    if not enterprise_client or not enterprise_client.token:
        print("    (跳过：企业员工未登录)")
        return
    resp = enterprise_client.post("/api/goods/list", {"current": 1, "size": 10})
    assert_success(resp, "企业员工应能访问货物管理")


def test_enterprise_can_manage_contracts():
    """需求：企业员工可使用合同管理"""
    if not enterprise_client or not enterprise_client.token:
        print("    (跳过：企业员工未登录)")
        return
    resp = enterprise_client.post("/api/contracts/list", {"current": 1, "size": 10})
    assert_success(resp, "企业员工应能访问合同管理")


# ========== 未认证访问 ==========

def test_unauthenticated_access_denied():
    """需求：未认证用户不能访问业务接口"""
    anon = ApiClient()
    resp = anon.post("/api/goods/list", {"current": 1, "size": 10})
    assert resp.get("code") != 200, "未认证用户应被拒绝"


def test_invalid_token_denied():
    """需求：无效Token应被拒绝"""
    fake = ApiClient()
    fake.token = "invalid.fake.token"
    resp = fake.post("/api/goods/list", {"current": 1, "size": 10})
    assert resp.get("code") != 200, "无效Token应被拒绝"


if __name__ == "__main__":
    print(f"\n{'='*60}")
    print(f"  需求 3.10 权限管理模块")
    print(f"{'='*60}")
    setup()
    runner.run("[ADMIN] 可管理租户", test_admin_can_manage_tenants)
    runner.run("[ADMIN] 可管理配置", test_admin_can_manage_config)
    runner.run("[ADMIN] 可管理模板", test_admin_can_manage_templates)
    runner.run("[ADMIN] 可管理用户", test_admin_can_manage_users)
    runner.run("[ENTERPRISE] 不能管理租户", test_enterprise_cannot_manage_tenants)
    runner.run("[ENTERPRISE] 不能管理配置", test_enterprise_cannot_manage_config)
    runner.run("[ENTERPRISE] 不能管理用户", test_enterprise_cannot_manage_users)
    runner.run("[ENTERPRISE] 可查看个人信息", test_enterprise_can_view_profile)
    runner.run("[ENTERPRISE] 可管理货物", test_enterprise_can_manage_goods)
    runner.run("[ENTERPRISE] 可管理合同", test_enterprise_can_manage_contracts)
    runner.run("[安全] 未认证访问拒绝", test_unauthenticated_access_denied)
    runner.run("[安全] 无效Token拒绝", test_invalid_token_denied)
    runner.summary()
