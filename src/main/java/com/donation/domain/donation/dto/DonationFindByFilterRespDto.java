package com.donation.domain.donation.dto;

import com.donation.domain.post.entity.Category;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DonationFindByFilterRespDto {

    private Long donateId;
    private Long postId;
    private Long userId;
    private String title;
    private String amount;
    private float currentAmount;
    private String sponsor;
    private String beneficiary;

    private Category category;

    @Builder
    @QueryProjection
    public DonationFindByFilterRespDto(Long donateId, Long postId, Long userId, String title, String amount,float currentAmount, String sponsor, String beneficiary,  Category category) {
        this.donateId = donateId;
        this.postId = postId;
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.currentAmount = currentAmount;
        this.sponsor = sponsor;
        this.beneficiary = beneficiary;
        this.category = category;
    }

}