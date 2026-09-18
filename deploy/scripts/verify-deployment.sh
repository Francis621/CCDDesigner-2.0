#!/usr/bin/env bash
# ==============================================================================
# CCDDesigner 2.0 - MBSE 组件部署端到端自动化健康探活脚本
# 验证 SysON、OpenSysML (sysml-grpc)、Flexo MMS 及对应存储连通性
# ==============================================================================

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}======================================================================${NC}"
echo -e "${BLUE}  CCDDesigner 2.0 - MBSE 基础组件端到端健康检查与规范核验${NC}"
echo -e "${BLUE}======================================================================${NC}"

TOTAL_CHECKS=0
PASSED_CHECKS=0

check_endpoint() {
    local name="$1"
    local url="$2"
    local expected_pattern="$3"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    echo -n "• 正在探测 [${name}] (${url})... "

    response=$(curl -s -m 5 "${url}" || echo "CONNECT_FAILED")

    if [[ "${response}" == "CONNECT_FAILED" ]]; then
        echo -e "${RED}[失败 - 无法连接]${NC}"
        return 1
    elif [[ -n "${expected_pattern}" ]] && [[ ! "${response}" =~ ${expected_pattern} ]]; then
        echo -e "${YELLOW}[异常 - 响应内容不符合预期: ${response:0:50}...]${NC}"
        return 1
    else
        echo -e "${GREEN}[正常通过]${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    fi
}

check_port() {
    local name="$1"
    local host="$2"
    local port="$3"

    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    echo -n "• 正在检测端口连通性 [${name}] (${host}:${port})... "

    if nc -z -w 3 "${host}" "${port}" 2>/dev/null; then
        echo -e "${GREEN}[正常通过 - 端口监听活跃]${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}[失败 - 端口未开放或拒绝连接]${NC}"
        return 1
    fi
}

echo -e "\n${YELLOW}--- 1. SysON 建模创作环境规范核验 ---${NC}"
echo "  [形态]: 容器化 Spring Boot + Web 视口静态资源 | [存储]: 独立 Postgres"
check_endpoint "SysON Server 健康探针" "http://localhost:8085/health" "UP" || true
check_endpoint "SysON Web 视口加载" "http://localhost:8085/workspaces/syson-proj-uuid-88192a01-c918" "SysON Web Canvas" || true
check_port "SysON 独立 PostgreSQL 存储实例" "localhost" "5434" || true

echo -e "\n${YELLOW}--- 2. OpenSysML 语义诊断计算服务规范核验 ---${NC}"
echo "  [形态]: sysml-grpc 容器，无状态 | [扩缩容]: 支持水平扩展"
check_endpoint "OpenSysML 无状态计算探针" "http://localhost:8086/health" "UP" || true
check_port "OpenSysML gRPC 语言服务接口" "localhost" "50051" || true

echo -e "\n${YELLOW}--- 3. Flexo MMS 模型仓库与 RDF 存储规范核验 ---${NC}"
echo "  [形态]: Docker Compose 官方部署栈 | [存储]: Apache Jena Fuseki 四元组"
check_endpoint "Flexo MMS Layer 1 模型服务" "http://localhost:8088/health" "UP" || true
check_endpoint "Apache Jena Fuseki SPARQL 引擎" "http://localhost:3030/$/ping" "" || true

echo -e "\n${YELLOW}--- 4. MBSE Gateway 统一网关与多副本负载分发 ---${NC}"
check_endpoint "MBSE 统一网关综合探针" "http://localhost:8084/health" "UP" || true
check_endpoint "网关路由: SysON 代理" "http://localhost:8084/syson/health" "UP" || true
check_endpoint "网关路由: OpenSysML 负载分发" "http://localhost:8084/opensysml/health" "UP" || true
check_endpoint "网关路由: Flexo MMS 代理" "http://localhost:8084/flexo/health" "UP" || true

echo -e "\n${BLUE}======================================================================${NC}"
echo -e "探针核验总结: 共检测 ${TOTAL_CHECKS} 项，成功 ${PASSED_CHECKS} 项。"
if [ "${PASSED_CHECKS}" -eq "${TOTAL_CHECKS}" ]; then
    echo -e "${GREEN}🎉 所有 MBSE 基础组件容器化部署与规范核验 100% 达标！${NC}"
else
    echo -e "${YELLOW}ℹ️ 若容器尚未启动，请先运行: ./deploy-local.sh up${NC}"
fi
echo -e "${BLUE}======================================================================${NC}"
