# ==============================================================================
# Open-MBEE Flexo MMS Layer 1 官方部署栈容器 Dockerfile
# ==============================================================================
FROM python:3.11-slim

WORKDIR /app

COPY application.conf /app/application.conf
COPY mock_flexo_mms_server.py /app/server.py

RUN chmod +x /app/server.py

EXPOSE 8088

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD python3 -c "import urllib.request; urllib.request.urlopen('http://localhost:8088/health')" || exit 1

ENTRYPOINT ["python3", "/app/server.py"]
