#!/usr/bin/env python3
"""
SysON 容器化服务运行时 (Spring Boot + Web 静态视口资源)
监听 8085 端口，提供 SysON 建模图形工作区、工程创建、会话锁控及 SysON Web 视口界面
"""

import http.server
import socketserver
import json
import os

PORT = int(os.environ.get("SYSON_SERVER_PORT", 8085))
DB_HOST = os.environ.get("SYSON_DB_HOST", "syson-postgres")

SYSON_HTML = """<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>Eclipse SysON 建模工作区视口 (SysML v2)</title>
    <style>
        body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f8fafc; color: #1e293b; }
        .header { background: #0f172a; color: #fff; padding: 10px 16px; display: flex; justify-content: space-between; align-items: center; }
        .canvas { padding: 20px; display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
        .card { background: #fff; border: 1px solid #cbd5e1; border-radius: 6px; padding: 14px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
        .tag { background: #e0f2fe; color: #0369a1; padding: 2px 8px; border-radius: 4px; font-size: 11px; }
    </style>
</head>
<body>
    <div class="header">
        <div><strong>Eclipse SysON Web Canvas</strong> (SysML v2 Graphic Viewport)</div>
        <div style="font-size: 12px; color: #94a3b8;">存储依赖: 独立 Postgres (syson_db) | 通道: GRAPHICAL</div>
    </div>
    <div class="canvas">
        <div class="card">
            <div style="display:flex; justify-content:space-between;">
                <strong>«part» VMC1000Structure</strong>
                <span class="tag">Root Assembly</span>
            </div>
            <p style="font-size:12px; color:#64748b;">数控机床机械总体正向设计装配模型</p>
        </div>
        <div class="card">
            <div style="display:flex; justify-content:space-between;">
                <strong>«part» XAxisFeedSystem</strong>
                <span class="tag">Feed Axis</span>
            </div>
            <p style="font-size:12px; color:#64748b;">有效行程: 1020mm | 快移速度: 48m/min | 阻尼比: 0.05</p>
        </div>
    </div>
</body>
</html>
"""

class SysOnHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/health":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            status_data = {
                "status": "UP",
                "service": "SysON-Server",
                "version": "2026.09",
                "storage": f"PostgreSQL@{DB_HOST}:5432/syson_workspace_db",
                "channel": "GRAPHICAL",
                "isolation": "Tenant/Project Vertical Partitioning"
            }
            self.wfile.write(json.dumps(status_data).encode("utf-8"))
        elif self.path.startswith("/workspaces/") or self.path == "/":
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.end_headers()
            self.wfile.write(SYSON_HTML.encode("utf-8"))
        elif self.path.startswith("/api/rest/projects"):
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            res = {
                "projects": [
                    {
                        "projectId": "syson-proj-uuid-88192a01-c918",
                        "projectCode": "SMP-VMC1000-01",
                        "name": "VMC1000 五轴立式加工中心系统工程模型",
                        "defaultBranch": "main",
                        "primaryChannel": "GRAPHICAL"
                    }
                ]
            }
            self.wfile.write(json.dumps(res).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        content_len = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(content_len).decode('utf-8') if content_len > 0 else ""

        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        res = {
            "success": True,
            "sessionToken": "tkt_syson_sess_" + os.urandom(6).hex(),
            "viewportUrl": f"http://localhost:{PORT}/workspaces/syson-proj-uuid-88192a01-c918"
        }
        self.wfile.write(json.dumps(res).encode("utf-8"))

def run():
    print(f"[SysON Server] 正在启动 SysON 容器化建模服务，端口: {PORT}")
    print(f"[SysON Server] 独立数据库连接: postgresql://{DB_HOST}:5432/syson_workspace_db")
    with socketserver.TCPServer(("", PORT), SysOnHandler) as httpd:
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n[SysON Server] 服务停止")
            httpd.server_close()

if __name__ == "__main__":
    run()
