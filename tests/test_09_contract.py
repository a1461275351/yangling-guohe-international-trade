"""
============================================================
需求文档 3.5 合同管理模块
============================================================
覆盖需求点：
- 3.5.1 合同创建：录入信息，关联本方企业、对方客户/供应商
- 合同状态机：初始化→签署中→已生效→履行中→已完成/已过期/已销毁
- 3.5.2 合同列表：按编号、对方名称、状态、日期筛选
- 初始化/签署中状态允许编辑
- 3.5.4 临期合同预警：距到期日不足N天的合同
"""
import sys, os
from datetime import datetime, timedelta
sys.path.insert(0, os.path.dirname(__file__))

from api_client import ApiClient, TestRunner, assert_success, assert_fail, \
    assert_list_result, assert_data_exists, find_in_list, ts
from config import *

client = ApiClient()
runner = TestRunner("09-合同管理 (需求3.5)")
contract_id = None
partner_id = None


def setup():
    client.login_as_admin()
    # 创建一个客户用于合同关联
    global partner_id
    t = ts()
    client.post("/api/partners", {
        "type": "CUSTOMER",
        "name": f"合同客户_{t}",
        "contactName": "合同联系人",
        "status": 1
    })
    record = find_in_list(client, "/api/partners/list", {"type": "CUSTOMER"},
                          lambda r: f"合同客户_{t}" in r["name"])
    if record:
        partner_id = record["id"]


def test_create_contract():
    """需求3.5.1：录入信息，关联本方企业、对方客户/供应商"""
    global contract_id
    t = ts()
    today = datetime.now().strftime("%Y-%m-%d")
    # 设置20天后到期，用于临期预警测试
    expire = (datetime.now() + timedelta(days=20)).strftime("%Y-%m-%d")
    resp = client.post("/api/contracts", {
        "contractNo": f"HT-{t}",
        "title": f"国际贸易合同_{t}",
        "ourCompany": "杨凌国合跨境贸易有限公司",
        "partnerId": partner_id,
        "partnerType": "CUSTOMER",
        "partnerName": "合同客户",
        "status": "INIT",
        "signDate": today,
        "expireDate": expire,
        "amount": 250000.00,
        "currency": "USD",
        "terms": "FOB Shanghai, 30天信用证付款",
        "remark": "自动化测试合同"
    })
    assert_success(resp)
    if resp.get("data") and resp["data"].get("id"):
        contract_id = resp["data"]["id"]
    else:
        record = find_in_list(client, "/api/contracts/list", {},
                              lambda r: r["contractNo"] == f"HT-{t}")
        assert record, "合同创建失败"
        contract_id = record["id"]


def test_list_contracts():
    """需求3.5.2：按企业维度展示合同"""
    resp = client.post("/api/contracts/list", {"current": 1, "size": 20})
    assert_list_result(resp, min_count=1)


def test_search_by_status():
    """需求3.5.2：按状态筛选"""
    resp = client.post("/api/contracts/list", {
        "status": "INIT", "current": 1, "size": 20
    })
    assert_list_result(resp)


def test_search_by_contract_no():
    """需求3.5.2：按合同编号筛选"""
    detail = client.get(f"/api/contracts/{contract_id}")
    contract_no = detail["data"]["contractNo"]
    resp = client.post("/api/contracts/list", {
        "contractNo": contract_no, "current": 1, "size": 20
    })
    assert_list_result(resp, min_count=1)


def test_get_contract_detail():
    """需求：合同详情"""
    resp = client.get(f"/api/contracts/{contract_id}")
    assert_data_exists(resp)
    data = resp["data"]
    assert data.get("ourCompany"), "缺少本方企业"
    assert data.get("partnerName"), "缺少对方名称"
    assert data.get("status") == "INIT"


def test_edit_init_contract():
    """需求3.5.2：初始化状态允许编辑"""
    resp = client.put("/api/contracts", {
        "id": contract_id,
        "amount": 300000.00,
        "terms": "CIF Shanghai, 60天信用证付款"
    })
    assert_success(resp, "初始化状态合同应可编辑")


def test_contract_status_flow():
    """需求3.5.1：合同状态机 INIT → SIGNING → EFFECTIVE"""
    # INIT → SIGNING (签署中)
    resp = client.put(f"/api/contracts/{contract_id}/status",
                      params={"status": "SIGNING"})
    assert_success(resp, "INIT→SIGNING应成功")

    # SIGNING → EFFECTIVE (已生效)
    resp2 = client.put(f"/api/contracts/{contract_id}/status",
                       params={"status": "EFFECTIVE"})
    assert_success(resp2, "SIGNING→EFFECTIVE应成功")


def test_expiring_contracts():
    """需求3.5.4：临期合同预警 - 距到期日不足N天"""
    resp = client.get("/api/contracts/expiring", params={"days": 30})
    assert_success(resp)
    assert isinstance(resp["data"], list), "应返回列表"


def test_expiring_stats():
    """需求3.5.4：临期统计 - 本周到期数、本月到期数"""
    resp = client.get("/api/contracts/expiring/stats")
    assert_success(resp)


def test_cleanup():
    """清理测试数据"""
    # 先改回INIT再删除
    client.put(f"/api/contracts/{contract_id}/status", params={"status": "INIT"})
    resp = client.delete(f"/api/contracts/{contract_id}")
    if resp.get("code") != 200:
        print(f"    (合同状态限制无法删除，属正常业务逻辑)")
    if partner_id:
        client.delete(f"/api/partners/{partner_id}")


if __name__ == "__main__":
    print(f"\n{'='*60}")
    print(f"  需求 3.5 合同管理模块")
    print(f"{'='*60}")
    setup()
    runner.run("[3.5.1] 创建合同(关联客户)", test_create_contract)
    runner.run("[3.5.2] 合同列表", test_list_contracts)
    runner.run("[3.5.2] 按状态筛选", test_search_by_status)
    runner.run("[3.5.2] 按编号筛选", test_search_by_contract_no)
    runner.run("合同详情", test_get_contract_detail)
    runner.run("[3.5.2] 编辑初始化合同", test_edit_init_contract)
    runner.run("[3.5.1] 状态机: INIT→SIGNING→EFFECTIVE", test_contract_status_flow)
    runner.run("[3.5.4] 临期合同查询", test_expiring_contracts)
    runner.run("[3.5.4] 临期统计", test_expiring_stats)
    runner.run("清理数据", test_cleanup)
    runner.summary()
