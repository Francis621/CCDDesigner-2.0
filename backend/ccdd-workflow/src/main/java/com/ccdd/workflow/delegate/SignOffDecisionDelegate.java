package com.ccdd.workflow.delegate;

import com.ccdd.workflow.crypto.DigitalSignatureService;
import com.ccdd.workflow.entity.ApprovalConclusion;
import com.ccdd.workflow.entity.ApprovalDecisionEntity;
import com.ccdd.workflow.entity.InstanceStatus;
import com.ccdd.workflow.entity.WorkflowInstanceEntity;
import com.ccdd.workflow.exception.HashTamperingDetectedException;
import com.ccdd.workflow.repository.WorkflowRepository;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * M24: 引擎自定义委派组件：审批决议安全签发
 * 在会签终点生成不可抵赖数字签名，签发 ApprovalDecision 只读凭证 (CST-M24-01 严禁直接更新业务表)
 */
@Component("signOffDecisionDelegate")
public class SignOffDecisionDelegate implements JavaDelegate {

    private final WorkflowRepository workflowRepository;
    private final DigitalSignatureService signatureService;

    @Autowired
    public SignOffDecisionDelegate(WorkflowRepository workflowRepository,
                                  DigitalSignatureService signatureService) {
        this.workflowRepository = workflowRepository;
        this.signatureService = signatureService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String procInstId = execution.getProcessInstanceId();
        WorkflowInstanceEntity wfInst = workflowRepository.findInstanceByProcInstId(procInstId)
                .orElseThrow(() -> new IllegalStateException("流程实例未在 PLM 业务台账中登记: " + procInstId));

        // 1. 提取综合决议结论 (默认 APPROVED，若节点变量包含 REJECTED 则为 REJECTED)
        Object conclusionVar = execution.getVariable("finalConclusion");
        ApprovalConclusion conclusion = ApprovalConclusion.APPROVED;
        if (conclusionVar != null) {
            String cStr = conclusionVar.toString();
            if ("REJECTED".equalsIgnoreCase(cStr)) {
                conclusion = ApprovalConclusion.REJECTED;
            } else if ("WITHDRAWN".equalsIgnoreCase(cStr)) {
                conclusion = ApprovalConclusion.WITHDRAWN;
            }
        }

        // 2. AT-16 物理防线：比对目标工程实体当前最新哈希，探测审批中内容是否被篡改
        String initialContentHash = wfInst.getTargetContentHash();
        if (conclusion == ApprovalConclusion.APPROVED) {
            String liveHash = workflowRepository.getLiveTargetEntityHash(wfInst.getTargetObjectType(), wfInst.getTargetObjectId());
            if (liveHash != null && !liveHash.equalsIgnoreCase(initialContentHash)) {
                wfInst.setStatus(InstanceStatus.TERMINATED);
                wfInst.setTerminationReason("Security Tampering Detected (AT-16): 被审实体在会签期间内容发生篡改，审批强行熔断废弃！");
                workflowRepository.saveInstance(wfInst);
                throw new HashTamperingDetectedException(
                        String.format("Security Tampering Detected (AT-16): 被审机床工程对象 [%s:%d] 在审批期间内容发生变更! 实时哈希 [%s] != 流程初始固化哈希 [%s]，会签流程已强制作废！",
                                wfInst.getTargetObjectType(), wfInst.getTargetObjectId(), liveHash, initialContentHash)
                );
            }
        }

        // 3. 构造不可抵赖签名载荷原文并生成数字印章
        Instant now = Instant.now();
        String signaturePayload = String.format("INST:%d|OBJ:%s:%d|HASH:%s|OUTCOME:%s|TIME:%d",
                wfInst.getWorkflowInstId(), wfInst.getTargetObjectType(), wfInst.getTargetObjectId(),
                initialContentHash, conclusion.name(), now.toEpochMilli());

        String cryptoStamp = signatureService.signWithPlatformKey(signaturePayload);
        String digest = signatureService.sha256(signaturePayload);

        // 4. 持久化不可篡改 ApprovalDecision 凭证 (严禁直接修改业务主表！)
        ApprovalDecisionEntity decision = new ApprovalDecisionEntity(
                workflowRepository.nextId(),
                wfInst.getWorkflowInstId(),
                wfInst.getTargetObjectType(),
                wfInst.getTargetObjectId(),
                initialContentHash,
                conclusion,
                false,
                null,
                null,
                cryptoStamp,
                digest,
                now
        );
        workflowRepository.saveDecision(decision);

        // 5. 更新 PLM 流程实例为 COMPLETED 状态
        wfInst.setStatus(InstanceStatus.COMPLETED);
        wfInst.setConclusion(conclusion);
        wfInst.setCompletedAt(now);
        workflowRepository.saveInstance(wfInst);

        // 6. 将决议凭据编号注入流程执行上下文
        execution.setVariable("generatedTicketId", decision.getDecisionTicketId());
    }
}
