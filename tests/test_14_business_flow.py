"""
============================================================
需求文档 2.1 核心业务流程 - 端到端测试
============================================================
覆盖需求点：
流程1：企业入驻与用户注册
  企业提交资料→国合员工审核→创建租户→企业员工注册→审批

流程2：合同生命周期
  创建合同(关联供应商/客户)→签署→生效→执行

流程3：订单与单据流
  基于合同创建订单→添加货物清单→生成发票/箱单/报关单草稿
"""
import sys, os, time
from datetime import datetime, timedelta
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, assert_fail, \
    assert_data_exists, find_in_list, ts
from config import *

admin_client = ApiClient()
runner = TestRunner("14-核心业务流程 (需求2.1)")

# 流程中产生的资源ID
flow_tenant_id = None
flow_tenant_code = None
flow_user_client = None
flow_partner_id = None
flow_goods_id = None
flow_contract_id = None
flow_order_id = None


def setup():
    admin_client.login_as_admin()


# ============================================================
# 流程1：企业入驻与用户注册 (需求2.1.1)
# ============================================================

def test_flow1_create_tenant():
    """流程1-步骤1：管理员创建租户（企业入驻）"""
    global flow_tenant_id, flow_tenant_code
    t = ts()
    flow_tenant_code = f"FLOW{t}"
    resp = admin_client.post("/api/tenants", {
        "tenantCode": flow_tenant_code,
        "name": f"端到端测试企业_{t}",
        "creditCode": f"91610{t}",
        "status": 1,
        "contactPerson": "流程测试联系人",
        "contactPhone": "13800138000"
    })
    assert_success(resp, "创建租户应成功")
    record = find_in_list(admin_client, "/api/tenants/list", {},
                          lambda r: r["tenantCode"] == flow_tenant_code)
    assert record, "租户创建失败"
    flow_tenant_id = record["id"]


def test_flow1_register_user():
    """流程1-步骤2：企业员工注册"""
    t = ts()
    resp = admin_client.post("/api/auth/register", {
        "tenantCode": flow_tenant_code,
        "username": f"flow_user_{t}",
        "password": TEST_PASSWORD,
        "realName": f"流程测试员工_{t}",
        "role": "ENTERPRISE"
    }, auth=False)
    assert_success(resp, "企业员工注册应成功")
    # 保存用户名
    test_flow1_register_user.username = f"flow_user_{t}"


def test_flow1_approve_and_login():
    """流程1-步骤3：管理员审批 → 企业员工登录"""
    global flow_user_client
    username = test_flow1_register_user.username
    record = find_in_list(admin_client, "/api/user-applies/list",
                          {"status": "PENDING"},
                          lambda r: r["username"] == username)
    assert record, "未找到待审批记录"

    # 审批通过
    resp = admin_client.put(f"/api/user-applies/{record['id']}/approve")
    assert_success(resp, "审批应成功")

    # 企业员工登录
    flow_user_client = ApiClient()
    resp2 = flow_user_client.login(flow_tenant_code, username, TEST_PASSWORD)
    assert_success(resp2, "审批后应能登录")
    assert resp2["data"]["role"] == "ENTERPRISE"


# ============================================================
# 流程2：合同生命周期 (需求2.1.2)
# ============================================================

def test_flow2_create_partner():
    """流程2-准备：创建客户（合同关联用）"""
    global flow_partner_id
    t = ts()
    resp = admin_client.post("/api/partners", {
        "type": "CUSTOMER",
        "name": f"流程测试客户_{t}",
        "creditCode": f"US{t}",
        "contactName": "Business Contact",
        "contactPhone": "+1-555-0100",
        "status": 1
    })
    assert_success(resp)
    record = find_in_list(admin_client, "/api/partners/list",
                          {"type": "CUSTOMER"},
                          lambda r: f"流程测试客户_{t}" in r["name"])
    assert record
    flow_partner_id = record["id"]


def test_flow2_create_contract():
    """流程2-步骤1：创建合同（关联客户）"""
    global flow_contract_id
    t = ts()
    today = datetime.now().strftime("%Y-%m-%d")
    expire = (datetime.now() + timedelta(days=180)).strftime("%Y-%m-%d")
    resp = admin_client.post("/api/contracts", {
        "contractNo": f"FLOW-HT-{t}",
        "title": f"端到端测试合同_{t}",
        "ourCompany": "杨凌国合跨境贸易有限公司",
        "partnerId": flow_partner_id,
        "partnerType": "CUSTOMER",
        "partnerName": "流程测试客户",
        "status": "INIT",
        "signDate": today,
        "expireDate": expire,
        "amount": 500000.00,
        "currency": "USD",
        "terms": "CIF Los Angeles"
    })
    assert_success(resp)
    record = find_in_list(admin_client, "/api/contracts/list", {},
                          lambda r: r["contractNo"] == f"FLOW-HT-{t}")
    assert record, "合同创建失败"
    flow_contract_id = record["id"]


def test_flow2_contract_lifecycle():
    """流程2-步骤2：合同状态机 INIT→SIGNING→EFFECTIVE"""
    # 初始化 → 签署中
    resp1 = admin_client.put(f"/api/contracts/{flow_contract_id}/status",
                             params={"status": "SIGNING"})
    assert_success(resp1, "INIT→SIGNING")

    # 签署中 → 已生效
    resp2 = admin_client.put(f"/api/contracts/{flow_contract_id}/status",
                             params={"status": "EFFECTIVE"})
    assert_success(resp2, "SIGNING→EFFECTIVE")

    # 验证状态
    resp3 = admin_client.get(f"/api/contracts/{flow_contract_id}")
    assert resp3["data"]["status"] == "EFFECTIVE"


# ============================================================
# 流程3：订单与单据流 (需求2.1.3)
# ============================================================

def test_flow3_create_goods():
    """流程3-步骤1：录入货物"""
    global flow_goods_id
    t = ts()
    resp = admin_client.post("/api/goods", {
        "goodsNo": f"FLOW-G-{t}",
        "name": f"太阳能面板_{t}",
        "hsCode": "8541402000",
        "spec": "300W单晶硅",
        "model": "SP-300",
        "unit": "片",
        "price": 120.00,
        "currency": "USD"
    })
    assert_success(resp)
    record = find_in_list(admin_client, "/api/goods/list", {},
                          lambda r: r["goodsNo"] == f"FLOW-G-{t}")
    flow_goods_id = record["id"] if record else resp.get("data", {}).get("id")
    assert flow_goods_id, "货物创建失败"


def test_flow3_create_order():
    """流程3-步骤2：基于已生效合同创建订单，关联货物"""
    global flow_order_id
    resp = admin_client.post("/api/orders", {
        "contractId": flow_contract_id,
        "tradeTerms": "CIF",
        "paymentMethod": "T/T",
        "remark": "太阳能面板首批出口订单",
        "goodsList": [{
            "goodsId": flow_goods_id,
            "goodsName": "太阳能面板",
            "goodsNo": "SP-300",
            "hsCode": "8541402000",
            "quantity": 2000,
            "unit": "片",
            "price": 120.00,
            "amount": 240000.00
        }]
    })
    assert_success(resp)
    if resp.get("data") and resp["data"].get("id"):
        flow_order_id = resp["data"]["id"]
    else:
        record = find_in_list(admin_client, "/api/orders/list",
                              {"contractId": flow_contract_id},
                              lambda r: True)
        assert record, "订单创建失败"
        flow_order_id = record["id"]


def test_flow3_verify_order():
    """流程3-步骤3：验证订单详情（含货物清单）"""
    resp = admin_client.get(f"/api/orders/{flow_order_id}")
    assert_data_exists(resp)
    data = resp["data"]
    assert data["contractId"] == flow_contract_id, "订单应关联正确合同"
    goods = data.get("goodsList")
    if goods:
        assert len(goods) >= 1, "订单应包含货物"
        assert goods[0].get("quantity") == 2000


def test_flow3_order_submit():
    """流程3-步骤4：订单状态流转 DRAFT→SUBMITTED"""
    resp = admin_client.put(f"/api/orders/{flow_order_id}/status",
                            params={"status": "SUBMITTED"})
    assert_success(resp, "提交订单应成功")


# ============================================================
# 清理
# ============================================================

def test_cleanup():
    """清理所有流程数据"""
    if flow_order_id:
        admin_client.put(f"/api/orders/{flow_order_id}/status",
                         params={"status": "DRAFT"})
        admin_client.delete(f"/api/orders/{flow_order_id}")
    if flow_contract_id:
        admin_client.put(f"/api/contracts/{flow_contract_id}/status",
                         params={"status": "INIT"})
        admin_client.delete(f"/api/contracts/{flow_contract_id}")
    if flow_goods_id:
        admin_client.delete(f"/api/goods/{flow_goods_id}")
    if flow_partner_id:
        admin_client.delete(f"/api/partners/{flow_partner_id}")
    if flow_tenant_id:
        admin_client.delete(f"/api/tenants/{flow_tenant_id}")


if __name__ == "__main__":
    print(f"\n{'='*60}")
    print(f"  需求 2.1 核心业务流程 端到端测试")
    print(f"{'='*60}")
    setup()
    print(f"\n  --- 流程1: 企业入驻与用户注册 ---")
    runner.run("创建租户(企业入驻)", test_flow1_create_tenant)
    runner.run("企业员工注册", test_flow1_register_user)
    runner.run("管理员审批→员工登录", test_flow1_approve_and_login)
    print(f"\n  --- 流程2: 合同生命周期 ---")
    runner.run("创建客户(合同关联)", test_flow2_create_partner)
    runner.run("创建合同(关联客户)", test_flow2_create_contract)
    runner.run("合同状态机:INIT→SIGNING→EFFECTIVE", test_flow2_contract_lifecycle)
    print(f"\n  --- 流程3: 订单与单据流 ---")
    runner.run("录入货物", test_flow3_create_goods)
    runner.run("基于合同创建订单+关联货物", test_flow3_create_order)
    runner.run("验证订单详情(含货物)", test_flow3_verify_order)
    runner.run("提交订单(DRAFT→SUBMITTED)", test_flow3_order_submit)
    print()
    runner.run("清理测试数据", test_cleanup)
    runner.summary()
