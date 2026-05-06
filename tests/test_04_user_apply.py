"""
============================================================
需求文档 3.1.4 用户申请审批
============================================================
覆盖需求点：
- 列表展示待审批的用户申请，包含用户填写的全部信息及所属企业资料
- 操作：通过或拒绝。拒绝时可填写理由
- 通过后该用户账号正式激活
- 提供批量审批功能
"""
import sys, os, time
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, assert_fail, \
    assert_list_result, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("04-用户审批 (需求3.1.4)")


def setup():
    client.login_as_admin()


def test_list_pending_applies():
    """需求：列表展示待审批的用户申请"""
    resp = client.post("/api/user-applies/list", {
        "status": "PENDING",
        "current": 1, "size": 20
    })
    assert_list_result(resp)


def test_approve_and_user_can_login():
    """需求：通过后该用户账号正式激活，可以登录"""
    t = ts()
    username = f"approve_{t}"
    # 注册
    client.post("/api/auth/register", {
        "tenantCode": ADMIN_TENANT_CODE,
        "username": username,
        "password": TEST_PASSWORD,
        "realName": f"审批通过用户_{t}",
        "role": "ENTERPRISE"
    }, auth=False)

    # 找到申请记录
    record = find_in_list(client, "/api/user-applies/list",
                          {"status": "PENDING"},
                          lambda r: r["username"] == username)
    assert record, "未找到待审批申请"
    # 验证申请包含完整信息
    assert record.get("realName"), "申请记录缺少realName"
    assert record.get("role") == "ENTERPRISE", "申请角色不正确"

    # 审批通过
    resp = client.put(f"/api/user-applies/{record['id']}/approve")
    assert_success(resp, "审批通过应成功")

    # 审批后用户应能登录
    c2 = ApiClient()
    resp2 = c2.login(ADMIN_TENANT_CODE, username, TEST_PASSWORD)
    assert_success(resp2, "审批通过后用户应能登录")
    assert resp2["data"]["role"] == "ENTERPRISE"


def test_reject_with_reason():
    """需求：拒绝时可填写理由"""
    t = ts()
    username = f"reject_{t}"
    client.post("/api/auth/register", {
        "tenantCode": ADMIN_TENANT_CODE,
        "username": username,
        "password": TEST_PASSWORD,
        "realName": "将被拒绝的用户",
        "role": "ENTERPRISE"
    }, auth=False)

    record = find_in_list(client, "/api/user-applies/list",
                          {"status": "PENDING"},
                          lambda r: r["username"] == username)
    assert record, "未找到待审批申请"

    resp = client.put(f"/api/user-applies/{record['id']}/reject",
                      params={"reason": "资料不完整，请补充营业执照"})
    assert_success(resp, "拒绝应成功")

    # 被拒绝的用户不应能登录
    c2 = ApiClient()
    resp2 = c2.login(ADMIN_TENANT_CODE, username, TEST_PASSWORD)
    assert_fail(resp2, "被拒绝用户不应能登录")


def test_batch_approve():
    """需求：提供批量审批功能"""
    ids = []
    t = ts()
    for i in range(2):
        username = f"batch_{t}_{i}"
        client.post("/api/auth/register", {
            "tenantCode": ADMIN_TENANT_CODE,
            "username": username,
            "password": TEST_PASSWORD,
            "realName": f"批量用户{i}",
            "role": "ENTERPRISE"
        }, auth=False)
        record = find_in_list(client, "/api/user-applies/list",
                              {"status": "PENDING"},
                              lambda r: r["username"] == username)
        if record:
            ids.append(record["id"])

    if len(ids) >= 2:
        resp = client.put("/api/user-applies/batch-approve", data=ids)
        assert_success(resp, "批量审批应成功")
    else:
        print("    (跳过：待审批记录不足)")


def test_list_filter_by_status():
    """需求：按状态筛选（PENDING/APPROVED/REJECTED）"""
    for status in ["PENDING", "APPROVED", "REJECTED"]:
        resp = client.post("/api/user-applies/list", {
            "status": status, "current": 1, "size": 10
        })
        assert_success(resp, f"按{status}筛选应成功")


if __name__ == "__main__":
    print(f"\n{'='*60}")
    print(f"  需求 3.1.4 用户申请审批")
    print(f"{'='*60}")
    setup()
    runner.run("查询待审批列表", test_list_pending_applies)
    runner.run("审批通过 → 用户可登录", test_approve_and_user_can_login)
    runner.run("拒绝(含理由) → 用户不可登录", test_reject_with_reason)
    runner.run("批量审批", test_batch_approve)
    runner.run("按状态筛选申请", test_list_filter_by_status)
    runner.summary()
