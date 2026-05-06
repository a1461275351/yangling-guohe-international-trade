import subprocess

with open('D:/work/product/autoWorkPlatform-v1.0.0/tests/temp_html.txt', 'r', encoding='utf-8') as f:
    html = f.read()

html_escaped = html.replace('\\', '\\\\').replace("'", "\\'")
sql = "UPDATE biz_template SET html_content='" + html_escaped + "' WHERE id=20;"

with open('D:/work/product/autoWorkPlatform-v1.0.0/tests/temp_update.sql', 'w', encoding='utf-8') as f:
    f.write(sql)

result = subprocess.run([
    r'D:\phpstudy_pro\Extensions\MySQL8.0.12\bin\mysql.exe',
    '-u', 'root', '-proot', 'trade_platform',
    '-e', 'source D:/work/product/autoWorkPlatform-v1.0.0/tests/temp_update.sql'
], capture_output=True, text=True, encoding='utf-8')
print('rc:', result.returncode)
if result.returncode == 0:
    print('OK - html_content updated for template id=20')
else:
    print('Error:', result.stderr[:200])
