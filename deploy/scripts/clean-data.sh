#!/usr/bin/env bash
# ==============================================================================
# CCDDesigner 2.0 - MBSE 数据清理与重置脚本
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="${SCRIPT_DIR}/../docker-compose"

echo "⚠️ 警告：该脚本将销毁所有运行中的 MBSE 容器并清理持久化数据卷 (SysON DB 与 Fuseki RDF 数据)！"
read -r -p "确认清理？[y/N] " confirm

if [[ "$confirm" =~ ^[Yy]$ ]]; then
    cd "${COMPOSE_DIR}"
    echo ">>> 正在停止容器并删除持久化卷..."
    docker compose down -v
    echo "✅ 数据已安全重置清理完毕。"
else
    echo "操作已取消。"
fi
