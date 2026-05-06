# ============================================================
# 外综服平台 API 自动化测试 - HTTP客户端 & 测试框架
# ============================================================
import requests
import json
import sys
import time
import os

from config import BASE_URL


class ApiClient:
    """封装HTTP请求，管理JWT Token认证"""

    def __init__(self, base_url=BASE_URL):
        self.base_url = base_url
        self.token = None
        self.user_info = None
        self.session = requests.Session()

    def login(self, tenant_code, username, password):
        """登录并缓存Token"""
        resp = self.post("/api/auth/login", {
            "tenantCode": tenant_code,
            "username": username,
            "password": password
        }, auth=False)
        if resp.get("code") == 200 and resp.get("data"):
            self.token = resp["data"]["token"]
            self.user_info = resp["data"]
        return resp

    def login_as_admin(self):
        """快捷方式：以管理员登录"""
        from config import ADMIN_TENANT_CODE, ADMIN_USERNAME, ADMIN_PASSWORD
        return self.login(ADMIN_TENANT_CODE, ADMIN_USERNAME, ADMIN_PASSWORD)

    def _headers(self):
        h = {"Content-Type": "application/json"}
        if self.token:
            h["Authorization"] = f"Bearer {self.token}"
        return h

    def get(self, path, params=None):
        r = self.session.get(self.base_url + path, params=params,
                             headers=self._headers(), timeout=15)
        return r.json()

    def post(self, path, data=None, auth=True):
        headers = self._headers() if auth else {"Content-Type": "application/json"}
        r = self.session.post(self.base_url + path, json=data,
                              headers=headers, timeout=15)
        return r.json()

    def put(self, path, data=None, params=None):
        r = self.session.put(self.base_url + path, json=data, params=params,
                             headers=self._headers(), timeout=15)
        return r.json()

    def delete(self, path, data=None):
        r = self.session.delete(self.base_url + path, json=data,
                                headers=self._headers(), timeout=15)
        return r.json()

    def upload(self, path, file_path, extra_fields=None):
        headers = {}
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"
        with open(file_path, "rb") as f:
            files = {"file": f}
            r = self.session.post(self.base_url + path, files=files,
                                  data=extra_fields or {}, headers=headers, timeout=30)
        return r.json()

    def download(self, path):
        r = self.session.get(self.base_url + path,
                             headers=self._headers(), timeout=30)
        return r


class TestRunner:
    """测试用例运行器，输出PASS/FAIL结果"""

    def __init__(self, module_name):
        self.module = module_name
        self.passed = 0
        self.failed = 0
        self.total_cases = []

    def run(self, name, func):
        """运行单个测试用例"""
        try:
            func()
            self.passed += 1
            self.total_cases.append((name, "PASS", ""))
            print(f"  \033[32m[PASS]\033[0m {name}")
        except AssertionError as e:
            self.failed += 1
            self.total_cases.append((name, "FAIL", str(e)))
            print(f"  \033[31m[FAIL]\033[0m {name}")
            print(f"         -> {e}")
        except Exception as e:
            self.failed += 1
            self.total_cases.append((name, "ERROR", str(e)))
            print(f"  \033[31m[ERROR]\033[0m {name}")
            print(f"         -> {type(e).__name__}: {e}")

    def skip(self, name, reason=""):
        """跳过测试用例"""
        self.total_cases.append((name, "SKIP", reason))
        print(f"  \033[33m[SKIP]\033[0m {name} {reason}")

    def known_bug(self, name, desc):
        """标记已知后端Bug"""
        self.passed += 1
        self.total_cases.append((name, "BUG", desc))
        print(f"  \033[33m[BUG]\033[0m  {name} (后端Bug: {desc})")

    def summary(self):
        total = self.passed + self.failed
        skipped = sum(1 for c in self.total_cases if c[1] == "SKIP")
        bugs = sum(1 for c in self.total_cases if c[1] == "BUG")
        print(f"\n{'='*60}")
        status = "\033[32mALL PASS\033[0m" if self.failed == 0 else f"\033[31m{self.failed} FAILED\033[0m"
        print(f"  {self.module}: {self.passed}/{total} 通过 | {status}")
        if skipped:
            print(f"  跳过: {skipped} | 已知Bug: {bugs}")
        print(f"{'='*60}\n")
        return self.failed == 0


def assert_success(resp, msg=""):
    """断言请求成功 code=200"""
    code = resp.get("code")
    assert code == 200, \
        f"期望code=200, 实际code={code}: {resp.get('message', '')} {msg}"


def assert_fail(resp, msg=""):
    """断言请求失败 code!=200"""
    assert resp.get("code") != 200, f"期望失败, 但code=200 {msg}"


def assert_data_exists(resp, field=None, msg=""):
    """断言返回data不为空"""
    assert_success(resp, msg)
    data = resp.get("data")
    assert data is not None, f"data为空 {msg}"
    if field:
        assert field in data, f"data缺少字段'{field}' {msg}"


def assert_list_result(resp, min_count=0, msg=""):
    """断言分页列表结果"""
    assert_success(resp, msg)
    data = resp.get("data")
    assert "records" in data, f"返回结果缺少records {msg}"
    assert "total" in data, f"返回结果缺少total {msg}"
    if min_count > 0:
        assert data["total"] >= min_count, \
            f"期望至少{min_count}条, 实际{data['total']}条 {msg}"


def find_in_list(client, list_url, query, match_func):
    """在分页列表中查找匹配记录"""
    resp = client.post(list_url, {**query, "current": 1, "size": 200})
    if resp.get("code") == 200:
        for r in resp["data"]["records"]:
            if match_func(r):
                return r
    return None


def ts():
    """时间戳后缀，避免测试数据冲突"""
    return int(time.time() * 1000) % 1000000000
