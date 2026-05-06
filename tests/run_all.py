"""
============================================================
外综服平台 API 自动化测试 - 一键运行
对标：《软件开发需求文档 V1.0》全部功能模块
============================================================
"""
import subprocess
import sys
import os
import time

TESTS_DIR = os.path.dirname(os.path.abspath(__file__))

test_files = [
    ("test_01_auth.py",         "需求3.1.1 登录与注册"),
    ("test_02_tenant.py",       "需求3.2   租户管理"),
    ("test_03_user.py",         "需求3.1.2 个人信息 + 3.1.3 用户列表"),
    ("test_04_user_apply.py",   "需求3.1.4 用户审批"),
    ("test_05_config.py",       "需求3.3   配置管理"),
    ("test_06_template.py",     "需求3.4   模板管理"),
    ("test_07_partner.py",      "需求3.11  供应商客户管理"),
    ("test_08_goods.py",        "需求3.6   货物管理"),
    ("test_09_contract.py",     "需求3.5   合同管理"),
    ("test_10_order.py",        "需求3.7   订单管理"),
    ("test_11_customs.py",      "需求3.8   报关单管理"),
    ("test_12_file.py",         "需求3.9   文件管理"),
    ("test_13_permission.py",   "需求3.10  权限管理"),
    ("test_14_business_flow.py","需求2.1   核心业务流程"),
]


def main():
    print("=" * 60)
    print("  外综服平台 API 自动化测试")
    print(f"  对标：《软件开发需求文档 V1.0》")
    print(f"  时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"  后端: http://localhost:8081")
    print("=" * 60)

    results = []
    total_start = time.time()

    for tf, desc in test_files:
        path = os.path.join(TESTS_DIR, tf)
        if not os.path.exists(path):
            print(f"\n  [SKIP] {desc} ({tf})")
            results.append((tf, desc, "SKIP", 0))
            continue

        print(f"\n  >>> {desc}")
        start = time.time()
        try:
            ret = subprocess.run(
                [sys.executable, path],
                cwd=TESTS_DIR,
                timeout=120
            )
            elapsed = time.time() - start
            status = "OK" if ret.returncode == 0 else "FAIL"
        except subprocess.TimeoutExpired:
            elapsed = 120
            status = "TIMEOUT"

        results.append((tf, desc, status, elapsed))

    total_elapsed = time.time() - total_start

    # 总结报告
    print("\n\n")
    print("=" * 70)
    print("  测试总结报告")
    print("=" * 70)

    ok = sum(1 for r in results if r[2] == "OK")
    fail = sum(1 for r in results if r[2] == "FAIL")
    skip = sum(1 for r in results if r[2] == "SKIP")

    for tf, desc, status, elapsed in results:
        icons = {"OK": "\033[32mPASS\033[0m", "FAIL": "\033[31mFAIL\033[0m",
                 "SKIP": "\033[33mSKIP\033[0m", "TIMEOUT": "\033[31mTIME\033[0m"}
        icon = icons.get(status, status)
        print(f"  [{icon}] {desc:<40} ({elapsed:.1f}s)")

    print(f"\n  {'='*50}")
    print(f"  总计: \033[32m{ok} 通过\033[0m, \033[31m{fail} 失败\033[0m, {skip} 跳过")
    print(f"  覆盖需求章节: 2.1, 3.1~3.11 (共14个模块)")
    print(f"  总耗时: {total_elapsed:.1f}s")
    print(f"  {'='*50}")

    sys.exit(1 if fail > 0 else 0)


if __name__ == "__main__":
    main()
