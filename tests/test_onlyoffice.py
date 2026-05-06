# -*- coding: utf-8 -*-
"""ONLYOFFICE 集成全链路测试"""
import sys, os, requests, json, time
sys.path.insert(0, os.path.dirname(__file__))
from api_client import ApiClient

BASE = "http://localhost:8081"
DS = "http://localhost:8080"

def test():
    print("=" * 60)
    print("  ONLYOFFICE 集成全链路测试")
    print("=" * 60)

    c = ApiClient()
    r = c.login_as_admin()
    assert r.get("code") == 200, f"登录失败: {r}"
    print("[PASS] 1. 管理员登录成功")

    # 2. 查模板列表，找有文件的模板
    r = c.post("/api/templates/list", {"type": "CONTRACT", "current": 1, "size": 20})
    assert r["code"] == 200, f"模板列表失败: {r['message']}"
    templates = r["data"]["records"]
    print(f"[PASS] 2. 模板列表: {len(templates)} 条")

    # 找一个有文件的
    tpl = None
    for t in templates:
        if t.get("filePath"):
            tpl = t
            break
    if not tpl:
        print("[SKIP] 没有带文件的模板，跳过ONLYOFFICE测试")
        return

    tid = tpl["id"]
    print(f"[INFO] 使用模板: id={tid} name={tpl['name']} file={tpl.get('fileName')}")

    # 3. 测试文件直接下载
    fp = tpl["filePath"].replace("\\", "/")
    idx = fp.find("/uploads/")
    if idx >= 0:
        rel = fp[idx + len("/uploads/"):]
    else:
        rel = fp
    file_url = f"{BASE}/uploads/{rel}"
    print(f"[INFO] 文件URL: {file_url}")

    r3 = requests.get(file_url, timeout=10)
    assert r3.status_code == 200, f"文件下载失败: HTTP {r3.status_code}"
    print(f"[PASS] 3. 后端文件下载: {r3.status_code} ({len(r3.content)} bytes)")

    # 4. 测试ONLYOFFICE服务
    r4 = requests.get(f"{DS}/healthcheck", timeout=10)
    assert r4.status_code == 200, f"ONLYOFFICE不可用: {r4.status_code}"
    print(f"[PASS] 4. ONLYOFFICE健康检查: {r4.text.strip()}")

    r4b = requests.get(f"{DS}/web-apps/apps/api/documents/api.js", timeout=10)
    assert r4b.status_code == 200, f"ONLYOFFICE API不可用"
    print(f"[PASS] 5. ONLYOFFICE JS API: 可用 ({len(r4b.content)} bytes)")

    # 5. 测试view模式config
    r5 = c.get(f"/api/templates/onlyoffice/config/{tid}", params={"mode": "view"})
    assert r5["code"] == 200, f"获取view config失败: {r5.get('message')}"
    config = r5["data"]["config"]
    doc_url = config["document"]["url"]
    doc_key = config["document"]["key"]
    ds_url = r5["data"]["documentServerUrl"]
    mode = config["editorConfig"]["mode"]
    has_callback = "callbackUrl" in config.get("editorConfig", {})
    print(f"[PASS] 6. View Config:")
    print(f"         key={doc_key}")
    print(f"         url={doc_url}")
    print(f"         mode={mode} (应为view)")
    print(f"         callback={has_callback} (应为False)")
    assert mode == "view", f"mode应为view，实际为{mode}"
    assert not has_callback, "view模式不应有callbackUrl"

    # 6. 测试edit模式config
    r6 = c.get(f"/api/templates/onlyoffice/config/{tid}", params={"mode": "edit"})
    assert r6["code"] == 200, f"获取edit config失败"
    econfig = r6["data"]["config"]
    emode = econfig["editorConfig"]["mode"]
    ecallback = econfig["editorConfig"].get("callbackUrl", "")
    print(f"[PASS] 7. Edit Config:")
    print(f"         mode={emode} (应为edit)")
    print(f"         callback={ecallback}")
    assert emode == "edit"
    assert ecallback, "edit模式应有callbackUrl"

    # 7. 模拟Docker容器访问文件（用host.docker.internal）
    print(f"\n[INFO] === Docker容器连通性测试 ===")
    import subprocess
    # 容器内curl下载文件
    cmd = f'docker exec onlyoffice curl -s -o /dev/null -w "%{{http_code}}" "{doc_url}"'
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=15)
    container_status = result.stdout.strip()
    print(f"[{'PASS' if container_status=='200' else 'FAIL'}] 8. 容器内下载文件: HTTP {container_status}")
    if container_status != "200":
        print(f"         这是关键问题！ONLYOFFICE容器无法下载文件")
        print(f"         测试URL: {doc_url}")
        # 尝试用IP替代
        import socket
        host_ip = socket.gethostbyname(socket.gethostname())
        alt_url = doc_url.replace("host.docker.internal", host_ip)
        cmd2 = f'docker exec onlyoffice curl -s -o /dev/null -w "%{{http_code}}" "{alt_url}"'
        r2 = subprocess.run(cmd2, shell=True, capture_output=True, text=True, timeout=15)
        print(f"         用IP({host_ip})重试: HTTP {r2.stdout.strip()}")

    # 8. 检查ONLYOFFICE错误日志
    print(f"\n[INFO] === ONLYOFFICE错误日志 ===")
    cmd3 = 'docker logs onlyoffice --tail 50 2>&1'
    result3 = subprocess.run(cmd3, shell=True, capture_output=True, text=True, timeout=10)
    errors = [l for l in result3.stdout.split('\n') if 'error' in l.lower() or 'private' in l.lower() or 'jwt' in l.lower()]
    if errors:
        for e in errors[-5:]:
            print(f"  LOG: {e.strip()[:120]}")
    else:
        print("  无错误日志")

    # 9. 检查ONLYOFFICE local.json配置
    print(f"\n[INFO] === ONLYOFFICE配置 ===")
    cmd4 = 'docker exec onlyoffice cat /etc/onlyoffice/documentserver/local.json'
    result4 = subprocess.run(cmd4, shell=True, capture_output=True, text=True, timeout=10)
    try:
        cfg = json.loads(result4.stdout)
        token_cfg = cfg.get("services",{}).get("CoAuthoring",{}).get("token",{}).get("enable",{})
        filter_cfg = cfg.get("services",{}).get("CoAuthoring",{}).get("request-filtering-agent",{})
        jwt_browser = token_cfg.get("browser", "未设置")
        jwt_inbox = token_cfg.get("request",{}).get("inbox", "未设置")
        allow_private = filter_cfg.get("allowPrivateIPAddress", "未设置")
        print(f"  JWT browser={jwt_browser} (应为false)")
        print(f"  JWT inbox={jwt_inbox} (应为false)")
        print(f"  allowPrivateIP={allow_private} (应为true)")

        issues = []
        if jwt_browser != False: issues.append("JWT browser未关闭")
        if jwt_inbox != False: issues.append("JWT inbox未关闭")
        if allow_private != True: issues.append("私有IP未允许")
        if issues:
            print(f"\n  [WARN] 配置问题: {', '.join(issues)}")
    except:
        print(f"  配置解析失败: {result4.stdout[:200]}")

    # 总结
    print(f"\n{'='*60}")
    print(f"  测试完成")
    if container_status == "200":
        print(f"  所有检查通过！刷新浏览器重试")
    else:
        print(f"  问题: 容器无法下载文件，需要修复网络")
    print(f"{'='*60}")


if __name__ == "__main__":
    test()
