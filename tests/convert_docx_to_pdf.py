# -*- coding: utf-8 -*-
"""用 WPS 将所有 docx 模板转成 PDF，更新数据库"""
import subprocess, os, sys, time, glob

MYSQL = r'D:\phpstudy_pro\Extensions\MySQL8.0.12\bin\mysql.exe'
UPLOADS = 'D:/work/product/autoWorkPlatform-v1.0.0/uploads'

sys.path.insert(0, os.path.dirname(__file__))
from api_client import ApiClient

def convert_with_wps(docx_path):
    """用WPS将docx转PDF"""
    pdf_path = docx_path.rsplit('.', 1)[0] + '.pdf'
    if os.path.exists(pdf_path):
        return pdf_path

    try:
        # 用Python的comtypes调用WPS COM接口
        import comtypes.client
        wps = comtypes.client.CreateObject('Kwps.Application')
        wps.Visible = False
        doc = wps.Documents.Open(docx_path.replace('/', '\\'))
        doc.SaveAs(pdf_path.replace('/', '\\'), FileFormat=17)  # 17 = PDF
        doc.Close()
        wps.Quit()
        if os.path.exists(pdf_path):
            print(f"  WPS转换成功: {os.path.basename(pdf_path)}")
            return pdf_path
    except Exception as e:
        print(f"  WPS转换失败: {e}")

    return None

def main():
    print("=" * 50)
    print("  docx → PDF 批量转换")
    print("=" * 50)

    c = ApiClient()
    c.login_as_admin()

    # 查所有有docx文件但没有PDF的模板
    r = c.post("/api/templates/list", {"current": 1, "size": 100})
    if r["code"] != 200:
        print("获取列表失败")
        return

    count = 0
    for tpl in r["data"]["records"]:
        fp = tpl.get("filePath")
        if not fp:
            continue
        fp = fp.replace("\\", "/")
        if not fp.endswith(".docx"):
            continue
        if tpl.get("pdfPath"):
            print(f"  id={tpl['id']} {tpl['name']}: 已有PDF，跳过")
            continue

        print(f"  id={tpl['id']} {tpl['name']}: 转换中...")
        pdf = convert_with_wps(fp)
        if pdf:
            # 更新数据库
            escaped = pdf.replace("\\", "/").replace("'", "\\'")
            result = subprocess.run(
                [MYSQL, '-u', 'root', '-proot', 'trade_platform', '-e',
                 f"UPDATE biz_template SET pdf_path='{escaped}' WHERE id={tpl['id']};"],
                capture_output=True, text=True, encoding='utf-8'
            )
            if result.returncode == 0:
                print(f"    数据库已更新")
                count += 1
            else:
                print(f"    数据库更新失败: {result.stderr[:80]}")

    print(f"\n完成！转换了 {count} 个文件")

if __name__ == "__main__":
    main()
