package com.ccdd.baseline.dto;

import com.ccdd.baseline.entity.BaselineEntity;
import com.ccdd.baseline.entity.BaselineMemberEntity;
import com.ccdd.baseline.entity.BaselineRelationSnapshotEntity;
import com.ccdd.baseline.entity.ConfigurationStateReferenceEntity;
import com.ccdd.baseline.entity.SuccessorBaselineLinkEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * 基线全要素详情聚合 DTO
 */
public class BaselineDetailDto {

    private BaselineEntity baseline;
    private List<BaselineMemberEntity> members;
    private List<BaselineRelationSnapshotEntity> relationSnapshots;
    private List<ConfigurationStateReferenceEntity> configurationStates;
    private List<SuccessorBaselineLinkEntity> successorLinks;

    public BaselineDetailDto() {
        this.members = new ArrayList<>();
        this.relationSnapshots = new ArrayList<>();
        this.configurationStates = new ArrayList<>();
        this.successorLinks = new ArrayList<>();
    }

    public BaselineDetailDto(BaselineEntity baseline,
                             List<BaselineMemberEntity> members,
                             List<BaselineRelationSnapshotEntity> relationSnapshots,
                             List<ConfigurationStateReferenceEntity> configurationStates,
                             List<SuccessorBaselineLinkEntity> successorLinks) {
        this.baseline = baseline;
        this.members = members != null ? members : new ArrayList<>();
        this.relationSnapshots = relationSnapshots != null ? relationSnapshots : new ArrayList<>();
        this.configurationStates = configurationStates != null ? configurationStates : new ArrayList<>();
        this.successorLinks = successorLinks != null ? successorLinks : new ArrayList<>();
    }

    public BaselineEntity getBaseline() {
        return baseline;
    }

    public void setBaseline(BaselineEntity baseline) {
        this.baseline = baseline;
    }

    public List<BaselineMemberEntity> getMembers() {
        return members;
    }

    public void setMembers(List<BaselineMemberEntity> members) {
        this.members = members;
    }

    public List<BaselineRelationSnapshotEntity> getRelationSnapshots() {
        return relationSnapshots;
    }

    public void setRelationSnapshots(List<BaselineRelationSnapshotEntity> relationSnapshots) {
        this.relationSnapshots = relationSnapshots;
    }

    public List<ConfigurationStateReferenceEntity> getConfigurationStates() {
        return configurationStates;
    }

    public void setConfigurationStates(List<ConfigurationStateReferenceEntity> configurationStates) {
        this.configurationStates = configurationStates;
    }

    public List<SuccessorBaselineLinkEntity> getSuccessorLinks() {
        return successorLinks;
    }

    public void setSuccessorLinks(List<SuccessorBaselineLinkEntity> successorLinks) {
        this.successorLinks = successorLinks;
    }
}
