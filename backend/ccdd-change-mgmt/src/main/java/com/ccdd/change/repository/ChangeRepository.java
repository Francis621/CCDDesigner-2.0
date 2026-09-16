package com.ccdd.change.repository;

import com.ccdd.change.entity.ChangeOrderEntity;
import com.ccdd.change.entity.ChangeReasonType;
import com.ccdd.change.entity.ChangeRequestEntity;
import com.ccdd.change.entity.ChangeTaskEntity;
import com.ccdd.change.entity.DispositionActionType;
import com.ccdd.change.entity.EcrStatus;
import com.ccdd.change.entity.EcoStatus;
import com.ccdd.change.entity.EffectivityDispositionEntity;
import com.ccdd.change.entity.ImpactDecisionEntity;
import com.ccdd.change.entity.ImpactDecisionType;
import com.ccdd.change.entity.ImpactItemEntity;
import com.ccdd.change.entity.ImplementationRecordEntity;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * M22 工程变更仓储层
 * 实现了并发安全内存映射，以及与 V1.7.0 迁移脚本种子数据完全一致的装配
 */
@Repository
public class ChangeRepository {

    private final Map<Long, ChangeRequestEntity> ecrStore = new ConcurrentHashMap<>();
    private final Map<Long, ChangeOrderEntity> ecoStore = new ConcurrentHashMap<>();
    private final Map<Long, ImpactItemEntity> impactItemStore = new ConcurrentHashMap<>();
    private final Map<Long, ImpactDecisionEntity> impactDecisionStore = new ConcurrentHashMap<>();
    private final Map<Long, ChangeTaskEntity> taskStore = new ConcurrentHashMap<>();
    private final Map<Long, EffectivityDispositionEntity> dispositionStore = new ConcurrentHashMap<>();
    private final Map<Long, ImplementationRecordEntity> implRecordStore = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(9000L);

    public ChangeRepository() {
        initSeedData();
    }

    private void initSeedData() {
        Instant now = Instant.now();

        // 1. ECR-2026-0042
        Long ecrId = 7001L;
        ChangeRequestEntity ecr = new ChangeRequestEntity(
                ecrId, "VMC_ENTERPRISE", 101L, "ECR-2026-0042",
                "VMC1000立式加工中心高速电主轴转速提升至15000rpm变更请求",
                ChangeReasonType.CUSTOMER_REQUIREMENT,
                "航空航天薄壁结构件高速铣削客户要求主轴额定工作转速由12000rpm提升至15000rpm，原钢球轴承温升超标，驱动电机额定功率不足。",
                "将主轴前端支撑轴承升级为超精密陶瓷球角接触轴承，驱动电机功率由15kW增大至18.5kW，并重新进行热伸长有限元仿真。",
                "HIGH", EcrStatus.APPROVED, null, "sys_chief_engineer",
                now.minus(15, ChronoUnit.DAYS), now.minus(15, ChronoUnit.DAYS)
        );
        ecrStore.put(ecrId, ecr);

        // 2. ECO-2026-0042 (状态 EXECUTING)
        Long ecoId = 8001L;
        ChangeOrderEntity eco = new ChangeOrderEntity(
                ecoId, ecrId, "VMC_ENTERPRISE", "ECO-2026-0042",
                "VMC1000主轴提速至15000rpm工程实施与全生命周期现场处置单",
                "MAJOR", 1001L, EcoStatus.EXECUTING, false, 1L,
                9005L, now.minus(5, ChronoUnit.DAYS), null,
                "chief_designer", now.minus(12, ChronoUnit.DAYS), now.minus(5, ChronoUnit.DAYS)
        );
        ecoStore.put(ecoId, eco);

        // 3. 候选影响项 (Impact Items)
        ImpactItemEntity item1 = new ImpactItemEntity(
                8101L, ecoId, 5003L, "PartRevision", "M-VMC850-BRG-7014",
                "[\"REQ-VMC1000-SPEED\", \"SPINDLE_SUBSYS\", \"M-VMC850-BRG-7014\"]",
                2, "MECHANICAL", true
        );
        ImpactItemEntity item2 = new ImpactItemEntity(
                8102L, ecoId, 5005L, "VerificationCaseRevision", "TC-SPINDLE-THERMAL",
                "[\"REQ-VMC1000-SPEED\", \"TC-SPINDLE-THERMAL\"]",
                2, "SIMULATION", true
        );
        ImpactItemEntity item3 = new ImpactItemEntity(
                8103L, ecoId, 5006L, "PartRevision", "M-VMC1000-MOTOR-15KW",
                "[\"REQ-VMC1000-SPEED\", \"M-VMC1000-MOTOR-15KW\"]",
                2, "ELECTRICAL", true
        );
        ImpactItemEntity item4 = new ImpactItemEntity(
                8104L, ecoId, 5002L, "DocRevision", "DOC-VMC850-DRW-001",
                "[\"REQ-VMC1000-SPEED\", \"DOC-VMC850-DRW-001\"]",
                3, "MECHANICAL", true
        );
        impactItemStore.put(item1.getImpactItemId(), item1);
        impactItemStore.put(item2.getImpactItemId(), item2);
        impactItemStore.put(item3.getImpactItemId(), item3);
        impactItemStore.put(item4.getImpactItemId(), item4);

        // 4. 专业裁决 (Impact Decisions)
        ImpactDecisionEntity dec1 = new ImpactDecisionEntity(
                8201L, 8101L, ImpactDecisionType.MODIFY,
                "15000rpm 超出原钢球轴承极限dmn值，配合公差与配合面改变，必须创建全新陶瓷球轴承组件(ADR-05)",
                "申请新物料号VMC1000-SP-CERAMIC-001并搭建新BOM", "CREATE_NEW",
                "eng_mech_lead", now.minus(10, ChronoUnit.DAYS)
        );
        ImpactDecisionEntity dec2 = new ImpactDecisionEntity(
                8202L, 8102L, ImpactDecisionType.RE_VERIFY,
                "转速提升25%，原热平衡证据失效，严禁继承历史PASS结论(ADR-08)，需执行15000rpm工况仿真",
                "在OpenModelica中重跑热机耦合仿真模型", "REVISE_EXISTING",
                "eng_sim_lead", now.minus(10, ChronoUnit.DAYS)
        );
        ImpactDecisionEntity dec3 = new ImpactDecisionEntity(
                8203L, 8103L, ImpactDecisionType.MODIFY,
                "切削功率与扭矩要求增大，电机更换为18.5kW高刚度电机，两向互换允许升版",
                "原电机物料升版至Rev B", "REVISE_EXISTING",
                "eng_elec_lead", now.minus(10, ChronoUnit.DAYS)
        );
        ImpactDecisionEntity dec4 = new ImpactDecisionEntity(
                8204L, 8104L, ImpactDecisionType.REVIEW_ONLY,
                "电主轴外形安装法兰与定位尺寸未改变，仅需重新校核工程图公差标注",
                "复核并更新CAD图纸表面粗糙度要求", "REVISE_EXISTING",
                "eng_mech_lead", now.minus(10, ChronoUnit.DAYS)
        );
        impactDecisionStore.put(dec1.getDecisionId(), dec1);
        impactDecisionStore.put(dec2.getDecisionId(), dec2);
        impactDecisionStore.put(dec3.getDecisionId(), dec3);
        impactDecisionStore.put(dec4.getDecisionId(), dec4);

        // 5. 实施分解任务 (Change Tasks)
        ChangeTaskEntity t1 = new ChangeTaskEntity(
                8301L, ecoId, "TSK-2026-01", "新建超精密陶瓷轴承主轴总成",
                "CAD_REMODEL", "eng_mech_lead", 5003L, 6001L, "COMPLETED", now.minus(6, ChronoUnit.DAYS)
        );
        ChangeTaskEntity t2 = new ChangeTaskEntity(
                8302L, ecoId, "TSK-2026-02", "15000rpm主轴稳态与瞬态热平衡重算",
                "SIM_RERUN", "eng_sim_lead", 5005L, 6002L, "COMPLETED", now.minus(6, ChronoUnit.DAYS)
        );
        taskStore.put(t1.getTaskId(), t1);
        taskStore.put(t2.getTaskId(), t2);

        // 6. 现场生效处置策略 (Effectivity Dispositions)
        EffectivityDispositionEntity disp1 = new EffectivityDispositionEntity(
                8401L, ecoId, "INVENTORY_PART", "M-VMC850-BRG-7014", null, null,
                DispositionActionType.SCRAP, null, null,
                "库房剩余旧款钢球轴承12套执行退库报废，冲减制造费用", now.minus(5, ChronoUnit.DAYS)
        );
        EffectivityDispositionEntity disp2 = new EffectivityDispositionEntity(
                8402L, ecoId, "IN_PROCESS_ORDER", "M-VMC1000-SPN-01", 3001L, null,
                DispositionActionType.REWORK, null, null,
                "车间在制装配工单 OPD-1001 暂停，拆卸原主轴箱换装陶瓷轴承并重新动平衡", now.minus(5, ChronoUnit.DAYS)
        );
        dispositionStore.put(disp1.getDispositionId(), disp1);
        dispositionStore.put(disp2.getDispositionId(), disp2);

        // 7. 现场实施回执记录 (Implementation Records - 1 COMPLETED, 1 DISPATCHED)
        ImplementationRecordEntity rec1 = new ImplementationRecordEntity(
                8501L, ecoId, 8401L, "ERP", 9011L, "COMPLETED",
                "warehouse_admin", 7001L, now.minus(2, ChronoUnit.DAYS), now.minus(5, ChronoUnit.DAYS)
        );
        ImplementationRecordEntity rec2 = new ImplementationRecordEntity(
                8502L, ecoId, 8402L, "MES", 9012L, "DISPATCHED",
                "mes_lead_op", null, null, now.minus(5, ChronoUnit.DAYS)
        );
        implRecordStore.put(rec1.getRecordId(), rec1);
        implRecordStore.put(rec2.getRecordId(), rec2);
    }

    // ====== ECR 变更请求操作 ======

    public ChangeRequestEntity saveEcr(ChangeRequestEntity ecr) {
        if (ecr.getEcrId() == null) {
            ecr.setEcrId(idGenerator.incrementAndGet());
        }
        if (ecr.getCreatedAt() == null) {
            ecr.setCreatedAt(Instant.now());
        }
        ecr.setUpdatedAt(Instant.now());
        ecrStore.put(ecr.getEcrId(), ecr);
        return ecr;
    }

    public Optional<ChangeRequestEntity> findEcrById(Long ecrId) {
        return Optional.ofNullable(ecrStore.get(ecrId));
    }

    public Optional<ChangeRequestEntity> findEcrByNumber(String number) {
        return ecrStore.values().stream()
                .filter(e -> e.getEcrNumber().equalsIgnoreCase(number))
                .findFirst();
    }

    public List<ChangeRequestEntity> findAllEcrs() {
        return new ArrayList<>(ecrStore.values());
    }

    // ====== ECO 变更实施单操作 ======

    public ChangeOrderEntity saveEco(ChangeOrderEntity eco) {
        if (eco.getEcoId() == null) {
            eco.setEcoId(idGenerator.incrementAndGet());
        }
        if (eco.getCreatedAt() == null) {
            eco.setCreatedAt(Instant.now());
        }
        eco.setUpdatedAt(Instant.now());
        ecoStore.put(eco.getEcoId(), eco);
        return eco;
    }

    public Optional<ChangeOrderEntity> findEcoById(Long ecoId) {
        return Optional.ofNullable(ecoStore.get(ecoId));
    }

    public Optional<ChangeOrderEntity> findEcoByNumber(String number) {
        return ecoStore.values().stream()
                .filter(e -> e.getEcoNumber().equalsIgnoreCase(number))
                .findFirst();
    }

    public List<ChangeOrderEntity> findAllEcos() {
        return new ArrayList<>(ecoStore.values());
    }

    public List<ChangeOrderEntity> findEcosByEcrId(Long ecrId) {
        return ecoStore.values().stream()
                .filter(e -> ecrId.equals(e.getEcrId()))
                .collect(Collectors.toList());
    }

    // ====== 影响面分析候选与裁定 ======

    public ImpactItemEntity saveImpactItem(ImpactItemEntity item) {
        if (item.getImpactItemId() == null) {
            item.setImpactItemId(idGenerator.incrementAndGet());
        }
        impactItemStore.put(item.getImpactItemId(), item);
        return item;
    }

    public List<ImpactItemEntity> findImpactItemsByEcoId(Long ecoId) {
        return impactItemStore.values().stream()
                .filter(i -> ecoId.equals(i.getEcoId()))
                .collect(Collectors.toList());
    }

    public Optional<ImpactItemEntity> findImpactItemById(Long id) {
        return Optional.ofNullable(impactItemStore.get(id));
    }

    public ImpactDecisionEntity saveImpactDecision(ImpactDecisionEntity decision) {
        if (decision.getDecisionId() == null) {
            decision.setDecisionId(idGenerator.incrementAndGet());
        }
        if (decision.getAssessedAt() == null) {
            decision.setAssessedAt(Instant.now());
        }
        impactDecisionStore.put(decision.getDecisionId(), decision);

        // 同步标记该 impactItem 为已裁决 (is_assessed = true)
        ImpactItemEntity item = impactItemStore.get(decision.getImpactItemId());
        if (item != null) {
            item.setIsAssessed(true);
        }
        return decision;
    }

    public Optional<ImpactDecisionEntity> findDecisionByImpactItemId(Long impactItemId) {
        return impactDecisionStore.values().stream()
                .filter(d -> impactItemId.equals(d.getImpactItemId()))
                .findFirst();
    }

    public List<ImpactDecisionEntity> findDecisionsByEcoId(Long ecoId) {
        List<Long> itemIds = findImpactItemsByEcoId(ecoId).stream()
                .map(ImpactItemEntity::getImpactItemId)
                .collect(Collectors.toList());

        return impactDecisionStore.values().stream()
                .filter(d -> itemIds.contains(d.getImpactItemId()))
                .collect(Collectors.toList());
    }

    // ====== 变更实施任务 ======

    public ChangeTaskEntity saveTask(ChangeTaskEntity task) {
        if (task.getTaskId() == null) {
            task.setTaskId(idGenerator.incrementAndGet());
        }
        taskStore.put(task.getTaskId(), task);
        return task;
    }

    public List<ChangeTaskEntity> findTasksByEcoId(Long ecoId) {
        return taskStore.values().stream()
                .filter(t -> ecoId.equals(t.getEcoId()))
                .collect(Collectors.toList());
    }

    public Optional<ChangeTaskEntity> findTaskById(Long taskId) {
        return Optional.ofNullable(taskStore.get(taskId));
    }

    // ====== 现场生效处置策略与回执 ======

    public EffectivityDispositionEntity saveDisposition(EffectivityDispositionEntity disposition) {
        if (disposition.getDispositionId() == null) {
            disposition.setDispositionId(idGenerator.incrementAndGet());
        }
        if (disposition.getCreatedAt() == null) {
            disposition.setCreatedAt(Instant.now());
        }
        dispositionStore.put(disposition.getDispositionId(), disposition);
        return disposition;
    }

    public List<EffectivityDispositionEntity> findDispositionsByEcoId(Long ecoId) {
        return dispositionStore.values().stream()
                .filter(d -> ecoId.equals(d.getEcoId()))
                .collect(Collectors.toList());
    }

    public ImplementationRecordEntity saveImplementationRecord(ImplementationRecordEntity record) {
        if (record.getRecordId() == null) {
            record.setRecordId(idGenerator.incrementAndGet());
        }
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(Instant.now());
        }
        implRecordStore.put(record.getRecordId(), record);
        return record;
    }

    public List<ImplementationRecordEntity> findImplementationRecordsByEcoId(Long ecoId) {
        return implRecordStore.values().stream()
                .filter(r -> ecoId.equals(r.getEcoId()))
                .collect(Collectors.toList());
    }

    public Optional<ImplementationRecordEntity> findImplementationRecordById(Long recordId) {
        return Optional.ofNullable(implRecordStore.get(recordId));
    }

    public Optional<ImplementationRecordEntity> findImplementationRecordByDispositionId(Long dispositionId) {
        return implRecordStore.values().stream()
                .filter(r -> dispositionId.equals(r.getDispositionId()))
                .findFirst();
    }
}
