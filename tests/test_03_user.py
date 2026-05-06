"""
============================================================
需求文档 3.1.2 个人信息 + 3.1.3 用户列表
============================================================
覆盖需求点：
- 3.1.2 查看个人信息（姓名、企业、角色、联系方式）
- 3.1.2 修改基本信息（手机号、邮箱）
- 3.1.2 修改登录密码（需验证原密码）
- 3.1.3 用户列表（用户名、姓名、企业、角色、状态）
- 3.1.3 筛选：按企业、状态筛选
- 3.1.3 禁用/启用用户
- 3.1.3 重置密码
"""
import sys, os, time
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, assert_fail, \
    assert_list_result, assert_data_exists, ts
from config import *

client = ApiClient()
runner = TestRunner("03-用户管理 (需求3.1.2+3.1.3)")


def setup():
    client.login_as_admin()


# ========== 3.1.2 个人信息 ==========

def test_get_current_user_info():
    """需求3.1.2：查看个人信息 - 姓名、所属企业、角色、联系方式"""
    resp = client.get("/api/users/info")
    assert_data_exists(resp)
    data = resp["data"]
    assert data.get("username") == ADMIN_USERNAME
    assert data.get("role") == "ADMIN"
    # 验证关键字段存在
    for field in ["realName", "role", "username"]:
        assert field in data, f"个人信息缺少字段: {field}"


def test_update_user_info():
    """需求3.1.2：修改基本信息（手机号、邮箱等）"""
    resp = client.put("/api/users/info", {
        "realName": "系统管理员",
        "phone": "13800000001",
        "email": "admin@guohe.com"
    })
    assert_success(resp)
    # 验证修改生效
    resp2 = client.get("/api/users/info")
    assert resp2["data"]["email"] == "admin@guohe.com"


def test_change_password():
    """需求3.1.2：修改登录密码（需验证原密码）"""
    new_pwd = "NewPass@999"
    # 改密码
    resp = client.put("/api/users/password", {
        "oldPassword": ADMIN_PASSWORD,
        "newPassword": new_pwd
    })
    assert_success(resp, "修改密码应成功")
    # 用新密码登录
    client.login(ADMIN_TENANT_CODE, ADMIN_USERNAME, new_pwd)
    # 改回原密码
    resp2 = client.put("/api/users/password", {
        "oldPassword": new_pwd,
        "newPassword": ADMIN_PASSWORD
    })
    assert_success(resp2, "恢复密码应成功")
    client.login_as_admin()


def test_change_password_wrong_old():
    """需求3.1.2：旧密码错误时修改应失败"""
    resp = client.put("/api/users/password", {
        "oldPassword": "completely_wrong",
        "newPassword": "whatever123"
    })
    assert_fail(resp, "旧密码错误应拒绝修改")


# ========== 3.1.3 用户列表 ==========

def test_list_users():
    """需求3.1.3：列表展示所有用户（用户名、姓名、企业、角色、状态、注册时间）"""
    resp = client.post("/api/users/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=1)
    record = resp["data"]["records"][0]
    for field in ["username", "role", "status"]:
        assert field in record, f"用户列表缺少字段: {field}"


def test_list_users_filter_by_tenant():
    """需求3.1.3：支持按企业筛选"""
    resp = client.post("/api/users/list", {
        "tenantId": 1,
        "current": 1, "size": 20
    })
    assert_list_result(resp)


def test_list_users_filter_by_status():
    """需求3.1.3：支持按状态筛选"""
    resp = client.post("/api/users/list", {
        "status": 1,
        "current": 1, "size": 20
    })
    assert_list_result(resp)


def test_toggle_user_status():
    """需求3.1.3：禁用/启用 - 可禁用违规用户或重新启用"""
    # 找一个非admin用户
    resp = client.post("/api/users/list", {"current": 1, "size": 100})
    users = [u for u in resp["data"]["records"] if u["username"] != ADMIN_USERNAME]
    if not users:
        print("    (跳过：无非admin用户)")
        return
    uid = users[0]["id"]
    original_status = users[0]["status"]
    # 禁用
    resp2 = client.put(f"/api/users/{uid}/status", params={"status": 0})
    assert_success(resp2, "禁用用户应成功")
    # 恢复
    resp3 = client.put(f"/api/users/{uid}/status", params={"status": original_status})
    assert_success(resp3)


def test_admin_reset_user_password():
    """需求3.1.3：重置密码 - 将用户密码重置为初始密码"""
    resp = client.post("/api/users/list", {"current": 1, "size": 100})
    users = [u for u in resp["data"]["records"] if u["username"] != ADMIN_USERNAME]
    if not users:
        print("    (跳过：无非admin用户)")
        return
    uid = users[0]["id"]
    resp2 = client.put(f"/api/users/{uid}/reset-password")
    assert_success(resp2, "管理员重置密码应成功")


if __name__ == "__main__":
    print(f"\n{'='*60}")
    print(f"  需求 3.1.2 个人信息 + 3.1.3 用户列表")
    print(f"{'='*60}")
    setup()
    runner.run("[3.1.2] 查看个人信息", test_get_current_user_info)
    runner.run("[3.1.2] 修改基本信息", test_update_user_info)
    runner.run("[3.1.2] 修改密码(验证原密码)", test_change_password)
    runner.run("[3.1.2] 旧密码错误拒绝修改", test_change_password_wrong_old)
    runner.run("[3.1.3] 用户列表查询", test_list_users)
    runner.run("[3.1.3] 按企业筛选用户", test_list_users_filter_by_tenant)
    runner.run("[3.1.3] 按状态筛选用户", test_list_users_filter_by_status)
    runner.run("[3.1.3] 禁用/启用用户", test_toggle_user_status)
    runner.run("[3.1.3] 管理员重置密码", test_admin_reset_user_password)
    runner.summary()
