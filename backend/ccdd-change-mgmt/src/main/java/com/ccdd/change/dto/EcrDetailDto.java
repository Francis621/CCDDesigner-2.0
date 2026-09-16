package com.ccdd.change.dto;

import com.ccdd.change.entity.ChangeOrderEntity;
import com.ccdd.change.entity.ChangeRequestEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * 变更请求 (ECR) 全要素详情 DTO
 */
public class EcrDetailDto {

    private ChangeRequestEntity ecr;
    private List<ChangeOrderEntity> associatedEcos;

    public EcrDetailDto() {
        this.associatedEcos = new ArrayList<>();
    }

    public EcrDetailDto(ChangeRequestEntity ecr, List<ChangeOrderEntity> associatedEcos) {
        this.ecr = ecr;
        this.associatedEcos = associatedEcos != null ? associatedEcos : new ArrayList<>();
    }

    public ChangeRequestEntity getEcr() {
        return ecr;
    }

    public void setEcr(ChangeRequestEntity ecr) {
        this.ecr = ecr;
    }

    public List<ChangeOrderEntity> getAssociatedEcos() {
        return associatedEcos;
    }

    public void setAssociatedEcos(List<ChangeOrderEntity> associatedEcos) {
        this.associatedEcos = associatedEcos;
    }
}
