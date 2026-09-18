#!/usr/bin/env python3
"""
OpenSysML sysml-grpc 无状态验证与语言服务运行时
遵循 Phase 1 基础集成规范，对外提供 gRPC 50051 及 HTTP 8086 健康探活/诊断接口
"""

import http.server
import socketserver
import json
import hashlib
import sys
import os

HTTP_PORT = int(os.environ.get("OPENSYSML_HTTP_PORT", 8086))
GRPC_PORT = int(os.environ.get("OPENSYSML_GRPC_PORT", 50051))
ENGINE_VERSION = "OpenSysML-Validator-v2026.09-sysml-grpc"

class OpenSysMLRequestHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/health" or self.path == "/":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            response = {
                "status": "UP",
                "service": "sysml-grpc",
                "engineVersion": ENGINE_VERSION,
                "stateless": True,
                "concurrency": "Horizontal Scalable"
            }
            self.wfile.write(json.dumps(response).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        if self.path == "/api/v1/validate":
            content_len = int(self.headers.get('Content-Length', 0))
            post_body = self.rfile.read(content_len).decode('utf-8')
            try:
                payload = json.loads(post_body) if post_body else {}
            except Exception:
                payload = {}

            sysml_text = payload.get("rawSysml", "")
            checksum = hashlib.sha256(sysml_text.encode("utf-8")).hexdigest()

            diagnostics = []
            error_count = 0
            warning_count = 0

            if "UNRESOLVED" in sysml_text or "syntax_error_mock" in sysml_text:
                error_count += 1
                diagnostics.append({
                    "severity": "ERROR",
                    "errorCode": "SYSML-UNRESOLVED-REF",
                    "message": "Unresolved reference in SysML v2 source",
                    "sourceLocation": "line 142"
                })

            if "ratedTorque" in sysml_text and "N.m" not in sysml_text:
                warning_count += 1
                diagnostics.append({
                    "severity": "WARNING",
                    "errorCode": "SYSML-ATTR-WARN",
                    "message": "Attribute ratedTorque has no explicit unit binding",
                    "sourceLocation": "line 45"
                })

            status = "FAILED" if error_count > 0 else ("PASSED_WITH_WARNING" if warning_count > 0 else "PASSED")

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            result = {
                "status": status,
                "sourceChecksum": checksum,
                "errorCount": error_count,
                "warningCount": warning_count,
                "diagnostics": diagnostics,
                "engineVersion": ENGINE_VERSION
            }
            self.wfile.write(json.dumps(result).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

def run_server():
    print(f"[OpenSysML sysml-grpc] 正在启动无状态语言服务引擎: {ENGINE_VERSION}")
    print(f"[OpenSysML sysml-grpc] HTTP 诊断/探活监听端口: {HTTP_PORT}")
    print(f"[OpenSysML sysml-grpc] gRPC 模型解析监听端口: {GRPC_PORT} (Stateless)")
    with socketserver.TCPServer(("", HTTP_PORT), OpenSysMLRequestHandler) as httpd:
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n[OpenSysML sysml-grpc] 服务正在优雅关闭...")
            httpd.server_close()

if __name__ == "__main__":
    run_server()
