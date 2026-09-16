package com.ccdd.bom.dsl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 150% Super BOM 规则 DSL 语法解析与求值引擎 (落实 D05 专项规格)
 * 纯原生 Java 递归下降求值器，支持特征变量代入、逻辑运算、数值比较与集合判定
 */
@Component
public class BomRuleDslEvaluator {

    private static final Logger log = LoggerFactory.getLogger(BomRuleDslEvaluator.class);

    /**
     * 评估选用条件表达式
     * @param dslExpression DSL 文本，如: $CNC_SYSTEM == "SIEMENS_840D" AND $COOLANT_PRESSURE >= 5.0
     * @param contextVariables 当前输入的特征字典
     * @return true 表示入选 100% BOM，false 表示排除
     */
    public boolean evaluateCondition(String dslExpression, Map<String, Object> contextVariables) {
        if (dslExpression == null || dslExpression.trim().isEmpty() || "TRUE".equalsIgnoreCase(dslExpression.trim()) || "1".equals(dslExpression.trim())) {
            return true;
        }

        try {
            String expr = dslExpression.trim();

            // 1. 处理顶级 OR 分支
            if (expr.contains(" OR ")) {
                String[] orParts = splitTopLevel(expr, " OR ");
                for (String part : orParts) {
                    if (evaluateCondition(part, contextVariables)) {
                        return true;
                    }
                }
                return false;
            }

            // 2. 处理顶级 AND 分支
            if (expr.contains(" AND ")) {
                String[] andParts = splitTopLevel(expr, " AND ");
                for (String part : andParts) {
                    if (!evaluateCondition(part, contextVariables)) {
                        return false;
                    }
                }
                return true;
            }

            // 3. 处理 NOT 取反
            if (expr.startsWith("NOT ") || expr.startsWith("!")) {
                String inner = expr.startsWith("NOT ") ? expr.substring(4) : expr.substring(1);
                return !evaluateCondition(inner.trim(), contextVariables);
            }

            // 4. 处理外层括号剥离
            if (expr.startsWith("(") && expr.endsWith(")")) {
                return evaluateCondition(expr.substring(1, expr.length() - 1), contextVariables);
            }

            // 5. 原子比较表达式求值
            return evaluateAtomicComparison(expr, contextVariables);
        } catch (Exception e) {
            log.warn("[BomRuleDslEvaluator] 评估表达式异常: expr='{}', error={}", dslExpression, e.getMessage());
            return false;
        }
    }

    /**
     * 原子比较表达式求值
     */
    private boolean evaluateAtomicComparison(String expr, Map<String, Object> context) {
        String trimmed = expr.trim();

        // 集合包含：$VAR IN ["A", "B"]
        if (trimmed.contains(" IN ")) {
            String[] parts = trimmed.split(" IN ", 2);
            String varName = extractVarName(parts[0]);
            Object varVal = context.get(varName);
            String listStr = parts[1].trim();
            if (listStr.startsWith("[") && listStr.endsWith("]")) {
                String inner = listStr.substring(1, listStr.length() - 1);
                String[] elements = inner.split(",");
                for (String elem : elements) {
                    String cleanElem = elem.trim().replace("\"", "").replace("'", "");
                    if (String.valueOf(varVal).equalsIgnoreCase(cleanElem)) {
                        return true;
                    }
                }
            }
            return false;
        }

        // 等于比较：$VAR == "VAL"
        if (trimmed.contains("==")) {
            String[] parts = trimmed.split("==", 2);
            String varName = extractVarName(parts[0]);
            Object varVal = context.get(varName);
            String expected = cleanLiteral(parts[1]);
            return varVal != null && String.valueOf(varVal).equalsIgnoreCase(expected);
        }

        // 不等于：$VAR != "VAL"
        if (trimmed.contains("!=")) {
            String[] parts = trimmed.split("!=", 2);
            String varName = extractVarName(parts[0]);
            Object varVal = context.get(varName);
            String expected = cleanLiteral(parts[1]);
            return varVal == null || !String.valueOf(varVal).equalsIgnoreCase(expected);
        }

        // 数值比较: >=, <=, >, <
        if (trimmed.contains(">=")) {
            String[] parts = trimmed.split(">=", 2);
            return compareNumbers(parts[0], parts[1], context) >= 0;
        }
        if (trimmed.contains("<=")) {
            String[] parts = trimmed.split("<=", 2);
            return compareNumbers(parts[0], parts[1], context) <= 0;
        }
        if (trimmed.contains(">")) {
            String[] parts = trimmed.split(">", 2);
            return compareNumbers(parts[0], parts[1], context) > 0;
        }
        if (trimmed.contains("<")) {
            String[] parts = trimmed.split("<", 2);
            return compareNumbers(parts[0], parts[1], context) < 0;
        }

        // 单独布尔变量判断：$ENABLE_AIR_COOLING
        if (trimmed.startsWith("$")) {
            String varName = extractVarName(trimmed);
            Object val = context.get(varName);
            return Boolean.parseBoolean(String.valueOf(val));
        }

        return false;
    }

    private int compareNumbers(String left, String right, Map<String, Object> context) {
        String varName = extractVarName(left);
        Object val = context.get(varName);
        double leftNum = val instanceof Number ? ((Number) val).doubleValue() : Double.parseDouble(String.valueOf(val));
        double rightNum = Double.parseDouble(cleanLiteral(right));
        return Double.compare(leftNum, rightNum);
    }

    private String extractVarName(String token) {
        String t = token.trim();
        if (t.startsWith("$")) {
            return t.substring(1).trim();
        }
        return t;
    }

    private String cleanLiteral(String token) {
        return token.trim().replace("\"", "").replace("'", "");
    }

    private String[] splitTopLevel(String expr, String delimiter) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int lastIndex = 0;
        int len = expr.length();
        int dLen = delimiter.length();

        for (int i = 0; i <= len - dLen; i++) {
            char c = expr.charAt(i);
            if (c == '(' || c == '[') depth++;
            else if (c == ')' || c == ']') depth--;
            else if (depth == 0 && expr.startsWith(delimiter, i)) {
                parts.add(expr.substring(lastIndex, i).trim());
                lastIndex = i + dLen;
                i += dLen - 1;
            }
        }
        parts.add(expr.substring(lastIndex).trim());
        return parts.toArray(new String[0]);
    }

    /**
     * 计算物料数量公式
     */
    public double evaluateQuantityFormula(String formula, Map<String, Object> contextVariables) {
        if (formula == null || formula.trim().isEmpty()) {
            return 1.0;
        }
        String trimmed = formula.trim();
        try {
            if (trimmed.startsWith("$")) {
                String varName = extractVarName(trimmed);
                Object val = contextVariables.get(varName);
                if (val instanceof Number) {
                    return ((Number) val).doubleValue();
                }
                return Double.parseDouble(String.valueOf(val));
            }
            return Double.parseDouble(trimmed);
        } catch (Exception e) {
            return 1.0;
        }
    }
}
