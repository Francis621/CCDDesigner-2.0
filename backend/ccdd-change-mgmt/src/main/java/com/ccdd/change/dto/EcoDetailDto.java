package com.ccdd.change.dto;

import com.ccdd.change.entity.ChangeOrderEntity;
import com.ccdd.change.entity.ChangeTaskEntity;
import com.ccdd.change.entity.EffectivityDispositionEntity;
import com.ccdd.change.entity.ImpactDecisionEntity;
import com.ccdd.change.entity.ImpactItemEntity;
import com.ccdd.change.entity.ImplementationRecordEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * 变更实施单 (ECO) 全要素聚合详情 DTO
 */
public class EcoDetailDto {

    private ChangeOrderEntity eco;
    private List<ImpactItemWithDecision> impactAnalysis;
    private List<ChangeTaskEntity> tasks;
    private List<EffectivityDispositionEntity> dispositions;
    private List<ImplementationRecordEntity> implementationRecords;

    public EcoDetailDto() {
        this.impactAnalysis = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.dispositions = new ArrayList<>();
        this.implementationRecords = new ArrayList<>();
    }

    public EcoDetailDto(ChangeOrderEntity eco, List<ImpactItemWithDecision> impactAnalysis,
                        List<ChangeTaskEntity> tasks, List<EffectivityDispositionEntity> dispositions,
                        List<ImplementationRecordEntity> implementationRecords) {
        this.eco = eco;
        this.impactAnalysis = impactAnalysis != null ? impactAnalysis : new ArrayList<>();
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.dispositions = dispositions != null ? dispositions : new ArrayList<>();
        this.implementationRecords = implementationRecords != null ? implementationRecords : new ArrayList<>();
    }

    public ChangeOrderEntity getEco() {
        return eco;
    }

    public void setEco(ChangeOrderEntity eco) {
        this.eco = eco;
    }

    public List<ImpactItemWithDecision> getImpactAnalysis() {
        return impactAnalysis;
    }

    public void setImpactAnalysis(List<ImpactItemWithDecision> impactAnalysis) {
        this.impactAnalysis = impactAnalysis;
    }

    public List<ChangeTaskEntity> getTasks() {
        return tasks;
    }

    public void setTasks(List<ChangeTaskEntity> tasks) {
        this.tasks = tasks;
    }

    public List<EffectivityDispositionEntity> getDispositions() {
        return dispositions;
    }

    public void setDispositions(List<EffectivityDispositionEntity> dispositions) {
        this.dispositions = dispositions;
    }

    public List<ImplementationRecordEntity> getImplementationRecords() {
        return implementationRecords;
    }

    public void setImplementationRecords(List<ImplementationRecordEntity> implementationRecords) {
        this.implementationRecords = implementationRecords;
    }

    public static class ImpactItemWithDecision {
        private ImpactItemEntity item;
        private ImpactDecisionEntity decision;

        public ImpactItemWithDecision() {
        }

        public ImpactItemWithDecision(ImpactItemEntity item, ImpactDecisionEntity decision) {
            this.item = item;
            this.decision = decision;
        }

        public ImpactItemEntity getItem() {
            return item;
        }

        public void setItem(ImpactItemEntity item) {
            this.item = item;
        }

        public ImpactDecisionEntity getDecision() {
            return decision;
        }

        public void setDecision(ImpactDecisionEntity decision) {
            this.decision = decision;
        }
    }
}
