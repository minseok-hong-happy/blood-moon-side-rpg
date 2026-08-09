package com.example.bloodmoonnightfall;

/**
 * Single source of truth for the portrait combat controls.
 *
 * <p>Drawing and touch handling must use these coordinates together. The JVM regression tests
 * verify that the automatic-skill chips cannot overlap the manual skill buttons.</p>
 */
public final class GameUiLayout {
    public static final float LOGICAL_HEIGHT = 1280f;

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
}
