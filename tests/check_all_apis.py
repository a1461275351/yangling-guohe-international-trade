"""
全量接口健康检查 - 排查所有API报错
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))

import requests
from config import *

BASE = BASE_URL
TOKEN = None

def login():
    global TOKEN
    r = requests.post(f"{BASE}/api/auth/login", json={
        "tenantCode": ADMIN_TENANT_CODE,
        "username": ADMIN_USERNAME,
        "password": ADMIN_PASSWORD
    })
    TOKEN = r.json()["data"]["token"]

def h():
    return {"Authorization": f"Bearer {TOKEN}", "Content-Type": "application/json"}

def check(method, path, data=None, params=None, label=""):
    try:
        if method == "GET":
            r = requests.get(f"{BASE}{path}", params=params, headers=h(), timeout=10)
        elif method == "POST":
            r = requests.post(f"{BASE}{path}", json=data, headers=h(), timeout=10)
        elif method == "PUT":
            r = requests.put(f"{BASE}{path}", json=data, params=params, headers=h(), timeout=10)
        elif method == "DELETE":
            r = requests.delete(f"{BASE}{path}", headers=h(), timeout=10)

        resp = r.json()
        code = resp.get("code", r.status_code)
        if code != 200:
            msg = resp.get("message", "")[:120]
            print(f"  \033[31m[ERROR]\033[0m {method:6} {path:<45} {label}")
            print(f"          code={code} | {msg}")
            return False
        else:
            print(f"  \033[32m[  OK ]\033[0m {method:6} {path:<45} {label}")
            return True
    except Exception as e:
        print(f"  \033[31m[CRASH]\033[0m {method:6} {path:<45} {type(e).__name__}: {e}")
        return False

login()
errors = 0
total = 0

print("=" * 80)
print("  全量 API 接口健康检查")
print("=" * 80)

# ========== 认证模块 ==========
print("\n--- 认证模块 /api/auth ---")
for m, p, d, pm, l in [
    ("POST", "/api/auth/login", {"tenantCode":"GUOHE","username":"admin","password":"Admin@123"}, None, "登录"),
    ("POST", "/api/auth/login", {"tenantCode":"GUOHE","username":"admin","password":"wrong"}, None, "错误登录(预期失败)"),
]:
    total += 1
    if not check(m, p, d, pm, l):
        if "预期失败" not in l:
            errors += 1

# ========== 用户模块 ==========
print("\n--- 用户模块 /api/users ---")
for m, p, d, pm, l in [
    ("POST", "/api/users/list", {"current":1,"size":10}, None, "用户列表"),
    ("GET",  "/api/users/info", None, None, "当前用户信息"),
]:
    total += 1
    if not check(m, p, d, pm, l): errors += 1

# ========== 用户审批 ==========
print("\n--- 用户审批 /api/user-applies ---")
for m, p, d, pm, l in [
    ("POST", "/api/user-applies/list", {"current":1,"size":10}, None, "审批列表"),
    ("POST", "/api/user-applies/list", {"status":"PENDING","current":1,"size":10}, None, "待审批列表"),
]:
    total += 1
    if not check(m, p, d, pm, l): errors += 1

# ========== 租户模块 ==========
print("\n--- 租户模块 /api/tenants ---")
for m, p, d, pm, l in [
    ("POST", "/api/tenants/list", {"current":1,"size":10}, None, "租户列表"),
    ("GET",  "/api/tenants/1", None, None, "租户详情(ID=1)"),
    ("GET",  "/api/tenants/all", None, None, "所有活跃租户"),
]:
    total += 1
    if not check(m, p, d, pm, l): errors += 1

# ========== 配置管理 ==========
print("\n--- 配置管理 /api/config ---")
for m, p, d, pm, l in [
    ("POST", "/api/config/items/list", {"current":1,"size":10}, None, "配置项列表"),
    ("POST", "/api/config/values/list", {"current":1,"size":10}, None, "配置值列表"),
    ("GET",  "/api/config/values/by-item-code", None, {"code":"COUNTRY"}, "按编码获取配置值"),
]:
    total += 1
    if not check(m, p, d, pm, l): errors += 1

# ========== 模板管理 ==========
print("\n--- 模板管理 /api/templates ---")
for m, p, d, pm, l in [
    ("POST", "/api/templates/list", {"current":1,"size":10}, None, "模板列表"),
    ("POST", "/api/templates/list", {"type":"CONTRACT","current":1,"size":10}, None, "合同模板列表"),
    ("POST", "/api/templates/list", {"type":"ORDER","current":1,"size":10}, None, "订单模板列表"),
    ("POST", "/api/templates/list", {"type":"INVOICE","current":1,"size":10}, None, "发票模板列表"),
    ("POST", "/api/templates/list", {"type":"PACKING","current":1,"size":10}, None, "箱单模板列表"),
    ("POST", "/api/templates/list", {"type":"CUSTOMS","current":1,"size":10}, None, "报关单模板列表"),
    ("GET",  "/api/templates/by-type", None, {"type":"CONTRACT"}, "按类型获取模板"),
    ("GET",  "/api/templates/by-type", None, {"type":"ORDER"}, "按类型获取订单模板"),
    ("GET",  "/api/templates/1", None, None, "模板详情(ID=1)"),
]:
    total += 1
    if not check(m, p, d, pm, l): errors += 1

# ========== 合同管理 ==========
print("\n--- 合同管理 /api/contracts ---")
for m, p, d, pm, l in [
    ("POST", "/api/contracts/list", {"current":1,"size":10}, None, "合同列表"),
    ("POST", "/api/contracts/list", {"status":"INIT","current":1,"size":10}, None, "初始化合同"),
    ("POST", "/api/contracts/list", {"status":"EFFECTIVE","current":1,"size":10}, None, "已生效合同"),
    ("GET",  "/api/contracts/expiring", None, {"days":30}, "临期合同(30天)"),
    ("GET",  "/api/contracts/expiring", None, {"days":15}, "临期合同(15天)"),
    ("GET",  "/api/contracts/expiring/stats", None, None, "临期统计"),
]:
    total += 1
    if not check(m, p, d, pm, l): errors += 1

# ========== 货物管理 ==========
print("\n--- 货物管理 /api/goods ---")
for m, p, d, pm, l in [
    ("POST", "/api/goods/list", {"current":1,"size":10}, None, "货物列表"),
    ("POST", "/api/goods/list", {"name":"笔记本","current":1,"size":10}, None, "按品名搜索"),
    ("POST", "/api/goods/list", {"hsCode":"8471","current":1,"size":10}, None, "按HS编码搜索"),
    ("GET",  "/api/goods/selection", None, None, "货物选择列表"),
]:
    total += 1
    if not check(m, p, d, pm, l): errors += 1

# ========== 订单管理 ==========
print("\n--- 订单管理 /api/orders ---")
for m, p, d, pm, l in [
    ("POST", "/api/orders/list", {"current":1,"size":10}, None, "订单列表"),
    ("POST", "/api/orders/list", {"status":"DRAFT","current":1,"size":10}, None, "草稿订单"),
    ("POST", "/api/orders/list", {"status":"SUBMITTED","current":1,"size":10}, None, "已提交订单"),
]:
    total += 1
    if not check(m, p, d, pm, l): errors += 1

# ========== 报关单管理 ==========
print("\n--- 报关单管理 /api/customs ---")
for m, p, d, pm, l in [
    ("POST", "/api/customs/list", {"current":1,"size":10}, None, "报关单列表"),
    ("POST", "/api/customs/list", {"ieType":"E","current":1,"size":10}, None, "出口报关单"),
    ("POST", "/api/customs/list", {"ieType":"I","current":1,"size":10}, None, "进口报关单"),
]:
    total += 1
    if not check(m, p, d, pm, l): errors += 1

# ========== 文件管理 ==========
print("\n--- 文件管理 /api/files ---")
for m, p, d, pm, l in [
    ("POST", "/api/files/list", {"current":1,"size":10}, None, "文件列表"),
    ("POST", "/api/files/list", {"businessType":"企业资质","current":1,"size":10}, None, "企业资质文件"),
    ("POST", "/api/files/list", {"businessType":"合同附件","current":1,"size":10}, None, "合同附件"),
]:
    total += 1
    if not check(m, p, d, pm, l): errors += 1

# ========== 供应商客户 ==========
print("\n--- 供应商客户 /api/partners ---")
for m, p, d, pm, l in [
    ("POST", "/api/partners/list", {"current":1,"size":10}, None, "合作伙伴列表"),
    ("POST", "/api/partners/list", {"type":"SUPPLIER","current":1,"size":10}, None, "供应商列表"),
    ("POST", "/api/partners/list", {"type":"CUSTOMER","current":1,"size":10}, None, "客户列表"),
    ("GET",  "/api/partners/active", None, {"type":"SUPPLIER"}, "活跃供应商"),
    ("GET",  "/api/partners/active", None, {"type":"CUSTOMER"}, "活跃客户"),
]:
    total += 1
    if not check(m, p, d, pm, l): errors += 1

# ========== 总结 ==========
print(f"\n{'='*80}")
print(f"  检查完成: {total} 个接口, \033[32m{total-errors} 正常\033[0m, \033[31m{errors} 报错\033[0m")
print(f"{'='*80}")
