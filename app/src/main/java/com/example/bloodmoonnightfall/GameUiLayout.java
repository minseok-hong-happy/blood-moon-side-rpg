package com.example.bloodmoonnightfall;

/**
 * Single source of truth for the portrait combat controls.
 *
 * <p>Drawing and touch handling must use these coordinates together. The JVM regression tests
 * verify that the automatic-skill chips cannot overlap the manual skill buttons.</p>
 */
public final class GameUiLayout {
    public static final float LOGICAL_HEIGHT = 1280f;

    public static final float HUD_AUXILIARY_BOTTOM = 286f;
    public static final float STORY_ROUTE_TOP = 296f;
    public static final float STORY_ROUTE_BOTTOM = 354f;
    public static final float HIT_COUNTER_RIGHT = 190f;
    public static final float COMBAT_NARRATIVE_LEFT = 204f;
    public static final float COMBAT_NARRATIVE_RIGHT = 702f;
    public static final float COMBAT_NARRATIVE_TOP = 368f;
    public static final float COMBAT_NARRATIVE_BOTTOM = 462f;

    public static final float AUTO_BATTLE_LEFT = 28f;
    public static final float AUTO_BATTLE_TOP = 958f;
    public static final float AUTO_BATTLE_RIGHT = 692f;
    public static final float AUTO_BATTLE_BOTTOM = 1018f;
    public static final float AUTO_BATTLE_TEXT_Y = 996f;

    public static final float AUTO_SKILL_CENTER_Y = 1048f;
    public static final float AUTO_SKILL_HALF_HEIGHT = 22f;

    public static final float MANUAL_SKILL_CENTER_Y = 1144f;
    public static final float MANUAL_SKILL_MAX_RADIUS = 60f;
    public static final float MANUAL_SKILL_LABEL_OFFSET = 19f;

    public static final float MIN_SECTION_GAP = 8f;
    public static final float MIN_CONTROL_ROW_GAP = 12f;
    public static final float BOTTOM_SAFE_MARGIN = 48f;

    private GameUiLayout() {
    }

    public static float autoToManualRowGap() {
        float autoBottom = AUTO_SKILL_CENTER_Y + AUTO_SKILL_HALF_HEIGHT;
        float manualTop = MANUAL_SKILL_CENTER_Y - MANUAL_SKILL_MAX_RADIUS;
        return manualTop - autoBottom;
    }

    public static boolean portraitControlsAreSeparatedAndVisible() {
        float autoTop = AUTO_SKILL_CENTER_Y - AUTO_SKILL_HALF_HEIGHT;
        float labelBottom = MANUAL_SKILL_CENTER_Y + MANUAL_SKILL_MAX_RADIUS
                + MANUAL_SKILL_LABEL_OFFSET;
        return autoTop - AUTO_BATTLE_BOTTOM >= MIN_SECTION_GAP
                && autoToManualRowGap() >= MIN_CONTROL_ROW_GAP
                && labelBottom <= LOGICAL_HEIGHT - BOTTOM_SAFE_MARGIN;
    }

    public static boolean combatStoryPanelsAreSeparated() {
        return STORY_ROUTE_TOP - HUD_AUXILIARY_BOTTOM >= MIN_SECTION_GAP
                && COMBAT_NARRATIVE_TOP - STORY_ROUTE_BOTTOM >= MIN_SECTION_GAP
                && COMBAT_NARRATIVE_LEFT - HIT_COUNTER_RIGHT >= MIN_SECTION_GAP
                && COMBAT_NARRATIVE_BOTTOM < AUTO_BATTLE_TOP;
    }
}
