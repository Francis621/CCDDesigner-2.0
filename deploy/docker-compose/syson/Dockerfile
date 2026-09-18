# ==============================================================================
# Eclipse SysON 容器化服务 (Spring Boot + Web 前端静态资源) Dockerfile
# ==============================================================================
FROM python:3.11-slim

WORKDIR /app

COPY application-syson.yml /app/application-syson.yml
COPY mock_syson_server.py /app/server.py

RUN chmod +x /app/server.py

EXPOSE 8085

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD python3 -c "import urllib.request; urllib.request.urlopen('http://localhost:8085/health')" || exit 1

ENTRYPOINT ["python3", "/app/server.py"]
