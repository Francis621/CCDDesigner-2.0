#!/usr/bin/env bash
# ==============================================================================
# CCDDesigner 2.0 - MBSE 核心组件 (SysON, OpenSysML, Flexo MMS) 本地容器部署脚本
# 遵循纯容器化规范，支持 OpenSysML 多副本无状态水平扩展
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="${SCRIPT_DIR}/../docker-compose"

print_usage() {
    echo "======================================================================"
    echo "CCDDesigner 2.0 - MBSE 核心组件容器化启停与伸缩工具"
    echo "======================================================================"
    echo "用法:"
    echo "  $0 up                   启动所有 MBSE 基础组件 (默认 OpenSysML 单实例)"
    echo "  $0 up --scale N         启动并水平扩展 OpenSysML 为 N 个无状态计算副本"
    echo "  $0 down                 停止并销毁当前运行的 MBSE 容器网络"
    echo "  $0 status               查看各 MBSE 组件容器健康与运行状态"
    echo "  $0 logs [服务名]        查看容器实时运行日志 (例如: $0 logs opensysml)"
    echo "  $0 verify               执行全链路健康检查与端到端接口探活"
    echo "======================================================================"
}

ACTION="${1:-up}"
SCALE_NUM=1

if [ "$ACTION" == "up" ] && [ "$2" == "--scale" ] && [ -n "$3" ]; then
    SCALE_NUM="$3"
fi

cd "${COMPOSE_DIR}"

case "$ACTION" in
    up)
        echo ">>> [1/3] 检查环境配置文件..."
        if [ ! -f .env ]; then
            echo "未检测到 .env 文件，自动从 .env.example 生成默认配置..."
            cp .env.example .env
        fi

        echo ">>> [2/3] 正在启动 SysON, OpenSysML, Flexo MMS 容器栈 (OpenSysML 副本数: ${SCALE_NUM})..."
        docker compose up -d --build --scale opensysml="${SCALE_NUM}"

        echo ">>> [3/3] 容器启动完成，正在打印服务集群运行状态:"
        docker compose ps
        echo ""
        echo "✅ MBSE 基础设施已就绪！"
        echo "  - SysON 建模工作区:      http://localhost:8085 (独立 Postgres: 5434)"
        echo "  - OpenSysML 语义语言服务: http://localhost:8086 / gRPC: 50051 (当前副本数: ${SCALE_NUM})"
        echo "  - Flexo MMS 模型仓库:     http://localhost:8088 (Jena Fuseki SPARQL: 3030)"
        echo "  - MBSE 统一网关入口:      http://localhost:8084"
        echo ""
        echo "提示: 可运行 '${SCRIPT_DIR}/verify-deployment.sh' 校验各端点连通性。"
        ;;

    down)
        echo ">>> 正在停止 MBSE 容器栈..."
        docker compose down
        echo "✅ MBSE 容器服务已全部安全停止。"
        ;;

    status)
        echo ">>> 当前 MBSE 各组件容器运行状态:"
        docker compose ps
        ;;

    logs)
        SERVICE_NAME="${2:-}"
        if [ -n "$SERVICE_NAME" ]; then
            docker compose logs -f "$SERVICE_NAME"
        else
            docker compose logs -f
        fi
        ;;

    verify)
        bash "${SCRIPT_DIR}/verify-deployment.sh"
        ;;

    *)
        print_usage
        exit 1
        ;;
esac
