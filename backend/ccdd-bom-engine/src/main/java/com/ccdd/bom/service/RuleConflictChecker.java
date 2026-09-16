package com.ccdd.bom.service;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 规则冲突静态检查器 (落实 D05 专项规格与 AT-05-02)
 * 校验输入特征集合是否触碰互斥 (MUTEX)、依赖缺失 (REQUIRES) 或物理参数边界
 */
@Service
public class RuleConflictChecker {

    private static final Logger log = LoggerFactory.getLogger(RuleConflictChecker.class);

    public static class ConflictReport {
        private final boolean hasConflict;
        private final List<String> errorMessages;

        public ConflictReport(boolean hasConflict, List<String> errorMessages) {
            this.hasConflict = hasConflict;
            this.errorMessages = errorMessages;
        }

        public boolean hasConflict() { return hasConflict; }
        public List<String> getErrorMessages() { return errorMessages; }
    }

    /**
     * 静态校验特征输入组合是否合法
     */
    public ConflictReport validateInputFeatures(Map<String, Object> features) {
        List<String> errors = new ArrayList<>();

        if (features == null || features.isEmpty()) {
            return new ConflictReport(false, errors);
        }

        // 1. 互斥校验 (MUTEX 规则范例: 华中数控系统与西门子专有主轴驱动互斥)
        Object cnc = features.get("CNC_SYSTEM");
        Object drive = features.get("SPINDLE_DRIVE");
        if ("HNC_848D".equals(cnc) && "SIEMENS_S120".equals(drive)) {
            errors.add("【规则冲突 MUTEX-01】华中数控 HNC_848D 互斥西门子专有主轴驱动器 SIEMENS_S120，无法实现总线伺服闭环");
        }

        // 2. 依赖校验 (REQUIRES 规则范例: 中心出水主轴必须配备 >= 5.0MPa 的高压出水泵组)
        Object spindle = features.get("SPINDLE_TYPE");
        Object coolantPressure = features.get("COOLANT_PRESSURE");
        if (spindle != null && String.valueOf(spindle).contains("CTS")) {
            if (coolantPressure == null) {
                errors.add("【前置依赖缺失 REQUIRES-02】选配中心出水主轴 (" + spindle + ") 必须指定高压冷却压力 COOLANT_PRESSURE");
            } else {
                double p = coolantPressure instanceof Number ? ((Number) coolantPressure).doubleValue() : Double.parseDouble(String.valueOf(coolantPressure));
                if (p < 5.0) {
                    errors.add("【参数物理约束 NUMERIC-03】中心出水主轴 CTS 最低要求冷却压力 5.0 MPa，当前设定为 " + p + " MPa，低于允许下限");
                }
            }
        }

        boolean hasConflict = !errors.isEmpty();
        if (hasConflict) {
            log.warn("[RuleConflictChecker] 检测到配置规则冲突项: count={}, details={}", errors.size(), errors);
        }

        return new ConflictReport(hasConflict, errors);
    }

    /**
     * 校验若存在冲突，则强制硬阻断并抛出 422 异常
     */
    public void checkAndEnforce(Map<String, Object> features) {
        ConflictReport report = validateInputFeatures(features);
        if (report.hasConflict()) {
            throw new BusinessException(ErrorCode.UNPROCESSABLE_ENTITY, report.getErrorMessages());
        }
    }
}
