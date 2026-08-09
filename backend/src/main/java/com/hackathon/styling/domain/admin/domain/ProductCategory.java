package com.hackathon.styling.domain.admin.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 카테고리.
 *
 * MCM 공식 온라인 스토어(kr.mcmworldwide.com)의 실제 분류 체계를 기준으로 정의했다.
 *   - 가방: 백팩 / 토트백·쇼퍼백 / 숄더백·크로스백 / 탑 핸들백 / 미니백 / 벨트백 / 클러치·파우치 / 트래블
 *   - 지갑 & 레더소품: 지갑 / 카드홀더 / 파우치
 *   - 패션소품: 벨트 / 스카프 / 모자
 *   - 슈즈
 *   - 의류(레디 투 웨어): 티셔츠·탑 / 스커트·팬츠 / 니트웨어·후디 / 재킷·코트
 *
 * 코디 추천 규칙이 이 값을 기준으로 동작하므로, 문자열이 아닌 enum 으로 고정한다.
 * 값을 추가·변경할 때는 코디 매칭 규칙도 함께 확인할 것.
 */
@Getter
@RequiredArgsConstructor
public enum ProductCategory {

    // ── 가방 — 고객이 집는 "중심 상품"이 되는 주력군
    BACKPACK("백팩", Group.BAG),
    TOTE("토트백·쇼퍼백", Group.BAG),
    SHOULDER("숄더백", Group.BAG),
    CROSSBODY("크로스백", Group.BAG),
    TOP_HANDLE("탑 핸들백", Group.BAG),
    MINI_BAG("미니백", Group.BAG),
    BELT_BAG("벨트백", Group.BAG),
    CLUTCH("클러치", Group.BAG),
    TRAVEL("트래블", Group.BAG),

    // ── 지갑 & 레더소품 — 가방에 매칭되는 아이템으로 가장 자주 쓰임
    WALLET("지갑", Group.SMALL_LEATHER),
    CARD_HOLDER("카드홀더", Group.SMALL_LEATHER),
    POUCH("파우치", Group.SMALL_LEATHER),

    // ── 패션소품
    BELT("벨트", Group.FASHION_ACCESSORY),
    SCARF("스카프", Group.FASHION_ACCESSORY),
    CAP_HAT("모자", Group.FASHION_ACCESSORY),

    // ── 슈즈
    SHOES("슈즈", Group.SHOES),

    // ── 의류(레디 투 웨어) — MCM 사이트의 의류 하위 분류 그대로
    TSHIRT_TOP("티셔츠·탑", Group.APPAREL),
    PANTS_SKIRT("스커트·팬츠", Group.APPAREL),
    KNIT_HOODIE("니트웨어·후디", Group.APPAREL),
    JACKET_COAT("재킷·코트", Group.APPAREL);

    /** 화면에 표시할 한글 이름. 키오스크·관리자 페이지에서 그대로 쓴다. */
    private final String displayName;

    /** 코디 매칭 규칙에서 사용하는 큰 분류. */
    private final Group group;

    /**
     * 카테고리 묶음.
     * 개별 카테고리 20개로 규칙을 짜면 조합이 너무 많아지므로, 그룹 단위로 규칙을 정의한다.
     */
    public enum Group {
        BAG,               // 가방
        SMALL_LEATHER,     // 지갑·레더소품
        FASHION_ACCESSORY, // 벨트·스카프·모자
        SHOES,             // 슈즈
        APPAREL            // 의류
    }

    public boolean isBag() {
        return group == Group.BAG;
    }

    public boolean isSmallLeather() {
        return group == Group.SMALL_LEATHER;
    }

    public boolean isApparel() {
        return group == Group.APPAREL;
    }
}
