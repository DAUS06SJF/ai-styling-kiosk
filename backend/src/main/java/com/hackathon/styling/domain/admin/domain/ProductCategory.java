package com.hackathon.styling.domain.admin.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 카테고리.
 *
 * MCM 상품군(가방·가죽소품 중심)에 맞춰 정의했다.
 * 코디 추천 규칙이 이 값을 기준으로 동작하므로, 문자열이 아닌 enum 으로 고정한다.
 * 값을 추가·변경할 때는 코디 매칭 규칙(MatchingRule)도 함께 확인할 것.
 */
@Getter
@RequiredArgsConstructor
public enum ProductCategory {

    // 가방류 — 고객이 집는 "중심 상품"이 되는 주력군
    BACKPACK("백팩", Group.BAG),
    TOTE("토트백", Group.BAG),
    SHOULDER("숄더백", Group.BAG),
    CROSSBODY("크로스백", Group.BAG),
    CLUTCH("클러치", Group.BAG),

    // 가죽소품 — 가방에 매칭되는 아이템으로 가장 자주 쓰임
    WALLET("지갑", Group.SMALL_LEATHER),
    CARD_HOLDER("카드홀더", Group.SMALL_LEATHER),
    POUCH("파우치", Group.SMALL_LEATHER),

    // 그 외
    BELT("벨트", Group.ACCESSORY),
    SHOES("신발", Group.ACCESSORY),
    APPAREL("의류", Group.APPAREL);

    /** 화면에 표시할 한글 이름. 키오스크·관리자 페이지에서 그대로 쓴다. */
    private final String displayName;

    /** 코디 매칭 규칙에서 사용하는 큰 분류. */
    private final Group group;

    /**
     * 카테고리 묶음.
     * 개별 카테고리 11개로 규칙을 짜면 조합이 너무 많아지므로, 그룹 단위로 규칙을 정의한다.
     */
    public enum Group {
        BAG,            // 가방
        SMALL_LEATHER,  // 가죽소품
        ACCESSORY,      // 액세서리
        APPAREL         // 의류
    }

    public boolean isBag() {
        return group == Group.BAG;
    }

    public boolean isSmallLeather() {
        return group == Group.SMALL_LEATHER;
    }
}
