# ==============================================================================
# OpenSysML sysml-grpc 无状态语义解析与诊断计算服务 Dockerfile
# 特性: 纯无状态、高并发、支持水平扩展
# ==============================================================================
FROM python:3.11-slim

WORKDIR /app

COPY server-config.json /app/server-config.json
COPY mock_sysml_grpc_server.py /app/server.py

RUN chmod +x /app/server.py

EXPOSE 50051 8086

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD python3 -c "import urllib.request; urllib.request.urlopen('http://localhost:8086/health')" || exit 1

ENTRYPOINT ["python3", "/app/server.py"]
