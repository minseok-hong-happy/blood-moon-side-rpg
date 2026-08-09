package com.example.bloodmoonnightfall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Portrait idle side-scrolling vampire RPG concept demo. */
public final class GameView extends View {
    private static final float LOGICAL_WIDTH = 720f;
    private static final float LOGICAL_HEIGHT = 1280f;
    private static final float GROUND_Y = 866f;
    private static final float CONTROL_TOP = 936f;
    private static final float FIXED_STEP = 1f / 60f;
    private static final int MAX_ACTIVE_ENEMIES = 10;
    private static final int MAX_PARTICLES = 420;

    private static final int CRIMSON = Color.rgb(214, 31, 70);
    private static final int BLOOD = Color.rgb(142, 13, 44);
    private static final int GOLD = Color.rgb(236, 193, 91);
    private static final int CYAN = Color.rgb(102, 222, 237);
    private static final int VIOLET = Color.rgb(159, 112, 232);
    private static final int NIGHT = Color.rgb(7, 9, 18);

    private static final ColorMatrixColorFilter BACKGROUND_LIFT_FILTER =
            new ColorMatrixColorFilter(new ColorMatrix(new float[]{
                    1.18f, 0f, 0f, 0f, 14f,
                    0f, 1.18f, 0f, 0f, 14f,
                    0f, 0f, 1.16f, 0f, 16f,
                    0f, 0f, 0f, 1f, 0f
            }));
    private static final LightingColorFilter ACTOR_LIFT_FILTER =
            new LightingColorFilter(Color.WHITE, Color.rgb(16, 16, 20));

    private static final int ACTION_NONE = 0;
    private static final int ACTION_ATTACK = 1;
    private static final int ACTION_SPEAR = 2;
    private static final int ACTION_SIPHON = 3;
    private static final int ACTION_NOVA = 4;
    private static final int ACTION_RUSH = 5;
    private static final int ACTION_RAIN = 6;
    private static final int ACTION_CHAIN = 7;
    private static final int ACTION_PILLAR = 8;
    private static final int ACTION_ECLIPSE = 9;

    private static final int FX_TETHER = 1;
    private static final int FX_NOVA = 2;
    private static final int FX_SPEAR_IMPACT = 3;
    private static final int FX_RUSH = 4;
    private static final int FX_RAIN = 5;
    private static final int FX_CHAIN = 6;
    private static final int FX_PILLAR = 7;
    private static final int FX_ECLIPSE = 8;

    private static final int ICON_DASH = 0;
    private static final int ICON_SPEAR = 1;
    private static final int ICON_SIPHON = 2;
    private static final int ICON_NOVA = 3;
    private static final int ICON_RUSH = 4;
    private static final int ICON_RAIN = 5;
    private static final int ICON_CHAIN = 6;
    private static final int ICON_PILLAR = 7;
    private static final int ICON_ECLIPSE = 8;

    private static final int ENEMY_MELEE = 1;
    private static final int ENEMY_HEAVY = 2;
    private static final int ENEMY_RANGED = 3;
    private static final int ENEMY_PHASE = 4;
    private static final int ENEMY_NOVA = 5;

    private static final String[] REGION_NAMES = {
            "월하 성역", "잿빛 숲", "진홍 성채"
    };
    private static final String[] REGION_SUBTITLES = {
            "잠든 혈족의 흔적", "재 속에서 속삭이는 망령", "심판관이 지키는 마지막 문"
    };
    private static final String[] ENEMY_NAMES = {
            "굶주린 혈귀", "검은 사냥꾼", "잿빛 망령", "태양의 심판관"
    };

    private static final String[] STORY_CHAPTERS = {
            "PROLOGUE  ·  피 없는 밤",
            "CHAPTER I  ·  재가 된 맹세",
            "CHAPTER II  ·  거짓 태양",
            "DEMO END  ·  혈월의 그릇"
    };
    private static final String[][] STORY_SPEAKERS = {
            {"기록", "리안", "카엘"},
            {"리안", "카엘", "잿빛 사냥꾼"},
            {"태양의 심판관", "카엘", "리안"},
            {"태양의 심판관", "카엘", "기록"}
    };
    private static final String[][] STORY_LINES = {
            {
                    "태양이 멈춘 지 일곱 번째 밤. 인간과 혈족의 피가 동시에 말라가기 시작했다.",
                    "카엘, 네 심장에 봉인된 혈월이 이 재앙의 열쇠야. 세 개의 봉인을 찾아.",
                    "끝까지 걷겠다. 내가 괴물이 되기 전에, 이 밤의 근원을 벤다."
            },
            {
                    "두 번째 봉인은 잿빛 숲에 있어. 하지만 숲은 네가 버린 기억을 먹고 자라.",
                    "기억을 잃어도 약속은 남는다. 리안에게 새벽을 돌려주겠어.",
                    "순혈의 후계자여, 네가 구하려는 인간이 첫 번째 봉인을 깨뜨렸다."
            },
            {
                    "세 봉인은 감옥이 아니다. 네 안의 혈월을 완성하는 열쇠다.",
                    "그렇다면 이 힘의 주인은 혈월이 아니라 나다. 내 피로 결말을 다시 쓴다.",
                    "성채의 왕좌 아래로 와. 진짜 새벽과 내가 감춘 죄가 그곳에 있어."
            },
            {
                    "네가 혈월을 쫓은 것이 아니다. 혈월이 자신의 몸을 되찾으러 너를 불렀다.",
                    "나는 그릇이 아니다. 밤을 삼키고도 인간으로 남겠다는 선택이다.",
                    "다음 장: 황혼 도시. 리안의 배신과 카엘의 첫 번째 일출."
            }
    };

    private enum Screen {
        TITLE,
        PLAYING,
        PAUSED,
        GROWTH,
        DEFEAT,
        NEW_CONFIRM,
        STORY,
        INVENTORY,
        OFFLINE_REWARD
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Path effectPath = new Path();
    private final Rect atlasSource = new Rect();
    private final RectF spriteDestination = new RectF();
    private final RectF trailDestination = new RectF();
    private final RectF effectBounds = new RectF();
    private final Typeface titleTypeface = Typeface.create("serif", Typeface.BOLD);
    private final Typeface uiTypeface = Typeface.create("sans-serif", Typeface.NORMAL);
    private final Typeface uiBoldTypeface = Typeface.create("sans-serif", Typeface.BOLD);
    private final Random random = new Random(0xB100D00DL);

    private Bitmap heroAtlas;
    private Bitmap enemyAtlas;
    private Bitmap bossAtlas;
    private Bitmap bloodArtsAtlas;
    private Bitmap currentBackground;
    private int currentBackgroundRegion = -1;
    private boolean combatAtlasLoadAttempted;
    private boolean bossAtlasLoadAttempted;

    private final RpgProgressStore progressStore;
    private final GameAudio audio;
    private RpgProgress progress;
    private boolean continueAvailable;

    private final Hero hero = new Hero();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private final List<SkillEffect> skillEffects = new ArrayList<>();

    private Screen screen = Screen.TITLE;
    private Screen growthReturnScreen = Screen.PLAYING;
    private Screen inventoryReturnScreen = Screen.PLAYING;
    private Screen offlineReturnScreen = Screen.STORY;
    private long lastFrameNanos;
    private float accumulator;
    private float renderScale = 1f;
    private float renderOffsetX;
    private float renderOffsetY;
    private float ambientClock;
    private float renderInterpolation;
    private float hitStop;
    private float screenShake;
    private float damageFlash;
    private float impactFlash;
    private float impactX;
    private float impactY;
    private float impactDirection = 1f;
    private float cameraKickX;
    private float cameraKickY;
    private float cameraZoomPulse;
    private float skillBloom;
    private boolean impactHeavy;
    private float toastTimer;
    private String toastText = "";

    private float heroHealth;
    private float heroMaxHealth;
    private float heroBlood;
    private float heroMaxBlood;
    private int heroAttackPower;

    private int heroAction;
    private float heroActionTimer;
    private float heroActionDuration;
    private float heroActionTrigger;
    private boolean heroActionTriggered;
    private boolean comboQueued;
    private int comboIndex;
    private float comboGrace;
    private float comboDisplayTimer;
    private float spearCooldown;
    private float siphonCooldown;
    private float novaCooldown;
    private float rushCooldown;
    private float rainCooldown;
    private float chainCooldown;
    private float pillarCooldown;
    private float eclipseCooldown;
    private float autoSkillThinkTimer;

    private int remainingToSpawn;
    private int waveTarget;
    private int defeatedThisWave;
    private int spawnSerial;
    private int nextEnemyId = 1;
    private float spawnTimer;
    private boolean waveComplete;
    private float waveClearTimer;
    private float waveBannerTimer;
    private float levelBannerTimer;
    private float chapterBannerTimer;
    private String levelBannerText = "";

    private float worldTravel;
    private int storyChapter;
    private int storyLine;
    private boolean storyStartsWave;
    private boolean storyFullRestore;
    private boolean storyFinale;
    private int selectedInventoryItem = -1;
    private int offlineElapsedSeconds;
    private int offlineGoldReward;
    private int offlineXpReward;
    private int offlineLevelsGained;

    private boolean saveDirty;
    private float saveDelay;

    private boolean leftHeld;
    private boolean rightHeld;
    private boolean attackPressed;
    private boolean dashPressed;
    private boolean spearPressed;
    private boolean siphonPressed;
    private boolean novaPressed;
    private int queuedSkillAction = ACTION_NONE;
    private float queuedSkillTimer;
    private int leftPointer = -1;
    private int rightPointer = -1;

    private final RectF continueButton = new RectF(112f, 858f, 608f, 944f);
    private final RectF newGameButton = new RectF(112f, 968f, 608f, 1054f);
    private final RectF pauseResumeButton = new RectF(152f, 520f, 568f, 604f);
    private final RectF pauseGrowthButton = new RectF(152f, 628f, 568f, 712f);
    private final RectF pauseTitleButton = new RectF(152f, 736f, 568f, 820f);
    private final RectF retryButton = new RectF(134f, 690f, 586f, 780f);
    private final RectF defeatTitleButton = new RectF(134f, 804f, 586f, 894f);
    private final RectF confirmNewButton = new RectF(84f, 720f, 346f, 808f);
    private final RectF cancelNewButton = new RectF(374f, 720f, 636f, 808f);
    private final RectF growthCloseButton = new RectF(204f, 1094f, 516f, 1168f);
    private final RectF inventoryHudButton = new RectF(398f, 232f, 544f, 286f);
    private final RectF inventoryActionButton = new RectF(52f, 1040f, 412f, 1122f);
    private final RectF inventoryCloseButton = new RectF(430f, 1040f, 668f, 1122f);
    private final RectF offlineClaimButton = new RectF(134f, 898f, 586f, 988f);
    private final RectF[] upgradeButtons = new RectF[7];

    public GameView(Context context) {
        super(context);
        setFocusable(true);
        setClickable(true);
        progressStore = new RpgProgressStore(context);
        audio = new GameAudio(context);
        RpgProgress loaded = progressStore.load();
        continueAvailable = loaded != null;
        progress = loaded == null ? RpgProgress.fresh() : loaded;
        progress.normalizeUnlocks();
        configurePaints();
        configureUpgradeBounds();
        syncHeroStats(true);
    }

    private void configurePaints() {
        paint.setDither(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(uiTypeface);
    }

    private void configureUpgradeBounds() {
        upgradeButtons[0] = new RectF(50f, 302f, 346f, 402f);
        upgradeButtons[1] = new RectF(374f, 302f, 670f, 402f);
        upgradeButtons[2] = new RectF(50f, 422f, 346f, 522f);
        upgradeButtons[3] = new RectF(374f, 422f, 670f, 522f);
        upgradeButtons[4] = new RectF(50f, 610f, 670f, 698f);
        upgradeButtons[5] = new RectF(50f, 714f, 670f, 802f);
        upgradeButtons[6] = new RectF(50f, 818f, 670f, 906f);
    }

    private Bitmap decodeBitmap(int resourceId, boolean background) {
        Bitmap decoded = decodeBitmap(resourceId, background, 1);
        if (decoded != null) {
            return decoded;
        }
        return decodeBitmap(resourceId, background, 2);
    }

    private Bitmap decodeBitmap(int resourceId, boolean background, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        options.inScaled = background;
        if (background) {
            // Generated backgrounds are 941 px wide; decode near the 720 px logical canvas.
            options.inDensity = 941;
            options.inTargetDensity = 720;
        }
        options.inPreferredConfig = background ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888;
        try {
            return BitmapFactory.decodeResource(getResources(), resourceId, options);
        } catch (OutOfMemoryError | RuntimeException ignored) {
            return null;
        }
    }

    private void ensureCombatAtlases() {
        if (combatAtlasLoadAttempted) {
            return;
        }
        combatAtlasLoadAttempted = true;
        heroAtlas = decodeBitmap(R.drawable.hero_side_atlas_v5, false);
        enemyAtlas = decodeBitmap(R.drawable.enemy_side_atlas, false);
        bloodArtsAtlas = decodeBitmap(R.drawable.vfx_blood_arts_atlas_v1, false);
    }

    private void ensureBossAtlas() {
        if (bossAtlasLoadAttempted) {
            return;
        }
        bossAtlasLoadAttempted = true;
        bossAtlas = decodeBitmap(R.drawable.boss_side_atlas, false);
    }

    private Bitmap obtainBackground(int region) {
        int safeRegion = RpgRules.clamp(region, 0, 2);
        if (currentBackgroundRegion == safeRegion) {
            return currentBackground != null && !currentBackground.isRecycled()
                    ? currentBackground : null;
        }
        recycleBitmap(currentBackground);
        currentBackground = null;
        currentBackgroundRegion = safeRegion;
        int resourceId = safeRegion == 0 ? R.drawable.bg_nocturne_sanctuary_portrait
                : safeRegion == 1 ? R.drawable.bg_ashwood_hunt_portrait
                : R.drawable.bg_duel_arena_portrait;
        currentBackground = decodeBitmap(resourceId, true);
        return currentBackground;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now = System.nanoTime();
        float frameDelta = lastFrameNanos == 0L ? FIXED_STEP
                : Math.min(0.05f, (now - lastFrameNanos) / 1_000_000_000f);
        lastFrameNanos = now;
        ambientClock += frameDelta;
        if (ambientClock > 10_000f) {
            ambientClock -= 10_000f;
        }

        if (screen == Screen.PLAYING) {
            accumulator = Math.min(accumulator + frameDelta, FIXED_STEP * 5f);
            int steps = 0;
            while (accumulator >= FIXED_STEP && steps < 5) {
                updateGame(FIXED_STEP);
                accumulator -= FIXED_STEP;
                steps++;
            }
            renderInterpolation = RpgRules.clamp(accumulator / FIXED_STEP, 0f, 1f);
        } else {
            updateMenuEffects(frameDelta);
            renderInterpolation = 1f;
        }

        renderScale = Math.min(getWidth() / LOGICAL_WIDTH, getHeight() / LOGICAL_HEIGHT);
        renderScale = Math.max(0.01f, renderScale);
        renderOffsetX = (getWidth() - LOGICAL_WIDTH * renderScale) * 0.5f;
        renderOffsetY = (getHeight() - LOGICAL_HEIGHT * renderScale) * 0.5f;

        canvas.drawColor(Color.rgb(3, 4, 9));
        canvas.save();
        canvas.translate(renderOffsetX, renderOffsetY);
        canvas.scale(renderScale, renderScale);
        drawScene(canvas);
        canvas.restore();
        postInvalidateOnAnimation();
    }

    private void updateMenuEffects(float dt) {
        screenShake = Math.max(0f, screenShake - dt * 22f);
        damageFlash = Math.max(0f, damageFlash - dt * 2.6f);
        impactFlash = Math.max(0f, impactFlash - dt * 12f);
        cameraKickX = approach(cameraKickX, 0f, 180f * dt);
        cameraKickY = approach(cameraKickY, 0f, 180f * dt);
        cameraZoomPulse = approach(cameraZoomPulse, 0f, 2.4f * dt);
        skillBloom = Math.max(0f, skillBloom - dt * 2.8f);
        toastTimer = Math.max(0f, toastTimer - dt);
        updateParticles(dt * 0.35f);
        updateSkillEffects(dt * 0.35f);
        updateFloatingTexts(dt);
    }

    private void updateGame(float dt) {
        hero.previousX = hero.x;
        for (Enemy enemy : enemies) {
            enemy.previousX = enemy.x;
        }
        waveBannerTimer = Math.max(0f, waveBannerTimer - dt);
        levelBannerTimer = Math.max(0f, levelBannerTimer - dt);
        chapterBannerTimer = Math.max(0f, chapterBannerTimer - dt);
        toastTimer = Math.max(0f, toastTimer - dt);
        screenShake = Math.max(0f, screenShake - dt * 26f);
        damageFlash = Math.max(0f, damageFlash - dt * 3.2f);
        impactFlash = Math.max(0f, impactFlash - dt * 14f);
        cameraKickX = approach(cameraKickX, 0f, 260f * dt);
        cameraKickY = approach(cameraKickY, 0f, 230f * dt);
        cameraZoomPulse = approach(cameraZoomPulse, 0f, 2.9f * dt);
        skillBloom = Math.max(0f, skillBloom - dt * 3.1f);
        comboGrace = Math.max(0f, comboGrace - dt);
        comboDisplayTimer = Math.max(0f, comboDisplayTimer - dt);
        spearCooldown = Math.max(0f, spearCooldown - dt);
        siphonCooldown = Math.max(0f, siphonCooldown - dt);
        novaCooldown = Math.max(0f, novaCooldown - dt);
        rushCooldown = Math.max(0f, rushCooldown - dt);
        rainCooldown = Math.max(0f, rainCooldown - dt);
        chainCooldown = Math.max(0f, chainCooldown - dt);
        pillarCooldown = Math.max(0f, pillarCooldown - dt);
        eclipseCooldown = Math.max(0f, eclipseCooldown - dt);
        autoSkillThinkTimer = Math.max(0f, autoSkillThinkTimer - dt);
        queuedSkillTimer = Math.max(0f, queuedSkillTimer - dt);
        if (queuedSkillTimer <= 0f) {
            queuedSkillAction = ACTION_NONE;
        }

        if (saveDirty) {
            saveDelay -= dt;
            if (saveDelay <= 0f) {
                saveNow();
            }
        }

        if (hitStop > 0f) {
            hitStop = Math.max(0f, hitStop - dt);
            updateParticles(dt * 0.2f);
            updateSkillEffects(dt * 0.2f);
            return;
        }

        processHeroInput();
        updateHero(dt);
        updateWaveSpawning(dt);
        updateEnemies(dt);
        resolveActorSpacing();
        recenterCombat(dt);
        updateProjectiles(dt);
        updateParticles(dt);
        updateSkillEffects(dt);
        updateFloatingTexts(dt);

        if (!hero.dead && !waveComplete) {
            float recovery = RpgRules.recoveryPerSecond(progress.recoveryLevel);
            heroBlood = Math.min(heroMaxBlood, heroBlood + (2.1f + recovery) * dt);
            if (enemies.isEmpty() || defeatedThisWave > 0) {
                heroHealth = Math.min(heroMaxHealth, heroHealth + recovery * dt);
            }
        }

        if (hero.dead) {
            hero.deadTimer -= dt;
            if (hero.deadTimer <= 0f) {
                screen = Screen.DEFEAT;
                resetPointers();
                saveNow();
            }
        }

        if (!waveComplete && remainingToSpawn == 0 && aliveEnemyCount() == 0) {
            beginWaveClear();
        }
        if (waveComplete) {
            waveClearTimer -= dt;
            if (waveClearTimer <= 0f) {
                advanceAdventure();
            }
        }
        consumeOneShotInput();
    }

    private void processHeroInput() {
        if (hero.dead || waveComplete) {
            return;
        }
        if (spearPressed) {
            bufferSkill(ACTION_SPEAR);
        }
        if (siphonPressed) {
            bufferSkill(ACTION_SIPHON);
        }
        if (novaPressed) {
            bufferSkill(ACTION_NOVA);
        }
        if (queuedSkillAction == ACTION_NONE && autoSkillThinkTimer <= 0f) {
            autoSkillThinkTimer = 0.16f;
            int automaticAction = chooseAutomaticSkill();
            if (automaticAction != ACTION_NONE) {
                bufferSkill(automaticAction);
            }
        }
        if (dashPressed && (heroAction == ACTION_NONE || heroAction == ACTION_ATTACK)) {
            startDash();
        }
        if (queuedSkillAction != ACTION_NONE && hero.dashTimer <= 0f) {
            if (heroAction == ACTION_NONE) {
                startQueuedSkill();
            } else if (heroAction == ACTION_ATTACK && heroActionTriggered
                    && canStartQueuedSkill()) {
                heroAction = ACTION_NONE;
                heroActionTimer = 0f;
                comboQueued = false;
                comboGrace = 0f;
                startQueuedSkill();
            }
        }
        if (heroAction == ACTION_NONE && hero.dashTimer <= 0f) {
            Enemy target = nearestEnemy(1000f);
            if (target != null) {
                hero.facing = target.x >= hero.x ? 1 : -1;
                if (Math.abs(target.x - hero.x) <= 225f) {
                    startAttack(comboGrace > 0f ? (comboIndex + 1) % 3 : 0);
                }
            }
        }
    }

    private int chooseAutomaticSkill() {
        Enemy target = nearestEnemy(620f);
        if (target == null) {
            return ACTION_NONE;
        }
        int nearbyEnemies = aliveEnemyCountWithin(285f);
        if (progress.level >= 15 && eclipseCooldown <= 0f
                && (nearbyEnemies >= 3 || target.kind == RpgRules.ENEMY_BOSS)) {
            return ACTION_ECLIPSE;
        }
        if (progress.level >= 12 && pillarCooldown <= 0f && nearbyEnemies >= 2) {
            return ACTION_PILLAR;
        }
        if (progress.level >= 9 && chainCooldown <= 0f) {
            return ACTION_CHAIN;
        }
        if (progress.level >= 7 && progress.novaLevel > 0 && novaCooldown <= 0f
                && heroBlood >= 55f
                && (nearbyEnemies >= 2 || target.kind == RpgRules.ENEMY_BOSS)) {
            return ACTION_NOVA;
        }
        if (progress.level >= 6 && rainCooldown <= 0f) {
            return ACTION_RAIN;
        }
        if (progress.level >= 4 && progress.siphonLevel > 0 && siphonCooldown <= 0f
                && heroBlood >= 30f && heroHealth <= heroMaxHealth * 0.82f
                && Math.abs(target.x - hero.x) <= 350f) {
            return ACTION_SIPHON;
        }
        if (progress.level >= 3 && rushCooldown <= 0f) {
            return ACTION_RUSH;
        }
        float automaticSpearThreshold = progress.level >= 7 ? 75f
                : progress.level >= 4 ? 50f : 20f;
        if (spearCooldown <= 0f && heroBlood >= automaticSpearThreshold
                && Math.abs(target.x - hero.x) <= 520f) {
            return ACTION_SPEAR;
        }
        return ACTION_NONE;
    }

    private int aliveEnemyCountWithin(float radius) {
        int count = 0;
        for (Enemy enemy : enemies) {
            if (!enemy.dead && enemy.spawnTimer <= 0.18f
                    && Math.abs(enemy.x - hero.x) <= radius) {
                count++;
            }
        }
        return count;
    }

    private void bufferSkill(int action) {
        queuedSkillAction = action;
        queuedSkillTimer = 0.42f;
    }

    private void startQueuedSkill() {
        int action = queuedSkillAction;
        queuedSkillAction = ACTION_NONE;
        queuedSkillTimer = 0f;
        if (action == ACTION_SPEAR) {
            tryStartSpear();
        } else if (action == ACTION_SIPHON) {
            tryStartSiphon();
        } else if (action == ACTION_NOVA) {
            tryStartNova();
        } else if (action == ACTION_RUSH) {
            tryStartRush();
        } else if (action == ACTION_RAIN) {
            tryStartRain();
        } else if (action == ACTION_CHAIN) {
            tryStartChain();
        } else if (action == ACTION_PILLAR) {
            tryStartPillar();
        } else if (action == ACTION_ECLIPSE) {
            tryStartEclipse();
        }
    }

    private boolean canStartQueuedSkill() {
        if (queuedSkillAction == ACTION_SPEAR) {
            return spearCooldown <= 0f && heroBlood >= 20f;
        }
        if (queuedSkillAction == ACTION_SIPHON) {
            return progress.level >= 4 && progress.siphonLevel > 0
                    && siphonCooldown <= 0f && heroBlood >= 30f
                    && nearestEnemy(330f) != null;
        }
        if (queuedSkillAction == ACTION_NOVA) {
            return progress.level >= 7 && progress.novaLevel > 0
                    && novaCooldown <= 0f && heroBlood >= 55f;
        }
        if (queuedSkillAction == ACTION_RUSH) {
            return progress.level >= 3 && rushCooldown <= 0f
                    && nearestEnemy(620f) != null;
        }
        if (queuedSkillAction == ACTION_RAIN) {
            return progress.level >= 6 && rainCooldown <= 0f
                    && nearestEnemy(620f) != null;
        }
        if (queuedSkillAction == ACTION_CHAIN) {
            return progress.level >= 9 && chainCooldown <= 0f
                    && nearestEnemy(620f) != null;
        }
        if (queuedSkillAction == ACTION_PILLAR) {
            return progress.level >= 12 && pillarCooldown <= 0f
                    && nearestEnemy(620f) != null;
        }
        if (queuedSkillAction == ACTION_ECLIPSE) {
            return progress.level >= 15 && eclipseCooldown <= 0f
                    && nearestEnemy(620f) != null;
        }
        return false;
    }

    private void startDash() {
        Enemy target = nearestEnemy(1000f);
        int direction = target == null || target.x >= hero.x ? 1 : -1;
        hero.facing = direction;
        hero.dashTimer = 0.19f;
        hero.invulnerability = Math.max(hero.invulnerability, 0.24f);
        hero.velocity = direction * 735f;
        heroAction = ACTION_NONE;
        comboQueued = false;
        addBurst(hero.x, GROUND_Y - 86f, Color.argb(205, 181, 25, 60), 12, 185f);
        audio.playDash();
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
    }

    private void startAttack(int nextCombo) {
        comboIndex = RpgRules.clamp(nextCombo, 0, 2);
        heroAction = ACTION_ATTACK;
        heroActionDuration = comboIndex == 0 ? 0.27f : comboIndex == 1 ? 0.29f : 0.39f;
        heroActionTimer = heroActionDuration;
        heroActionTrigger = comboIndex == 0 ? 0.08f : comboIndex == 1 ? 0.09f : 0.13f;
        heroActionTriggered = false;
        comboQueued = false;
        Enemy target = nearestEnemy(230f);
        if (target != null) {
            hero.facing = target.x >= hero.x ? 1 : -1;
            if (Math.abs(target.x - hero.x) > 72f) {
                float drive = comboIndex == 2 ? 430f : 380f;
                hero.velocity = hero.facing * Math.max(Math.abs(hero.velocity), drive);
            }
        }
    }

    private void tryStartSpear() {
        if (spearCooldown > 0f) {
            showToast("혈창 재사용까지 " + oneDecimal(spearCooldown) + "초", 0.9f);
            return;
        }
        if (heroBlood < 20f) {
            showToast("혈기가 부족합니다", 1.1f);
            return;
        }
        Enemy target = nearestEnemy(520f);
        if (target != null) {
            hero.facing = target.x >= hero.x ? 1 : -1;
        }
        heroBlood -= 20f;
        spearCooldown = 0.9f;
        startHeroSkill(ACTION_SPEAR, 0.32f, 0.085f);
    }

    private void tryStartSiphon() {
        if (progress.level < 4 || progress.siphonLevel <= 0) {
            showToast("흡혈은 레벨 4에 해금됩니다", 1.3f);
            return;
        }
        if (siphonCooldown > 0f) {
            showToast("흡혈 재사용까지 " + oneDecimal(siphonCooldown) + "초", 0.9f);
            return;
        }
        Enemy target = nearestEnemy(330f);
        if (target == null) {
            showToast("흡혈할 대상이 너무 멉니다", 1.1f);
            return;
        }
        if (heroBlood < 30f) {
            showToast("혈기가 부족합니다", 1.1f);
            return;
        }
        hero.facing = target.x >= hero.x ? 1 : -1;
        heroBlood -= 30f;
        siphonCooldown = 4.8f;
        startHeroSkill(ACTION_SIPHON, 0.42f, 0.12f);
    }

    private void tryStartNova() {
        if (progress.level < 7 || progress.novaLevel <= 0) {
            showToast("혈월 폭발은 레벨 7에 해금됩니다", 1.3f);
            return;
        }
        if (novaCooldown > 0f) {
            showToast("혈월 폭발 재사용까지 " + oneDecimal(novaCooldown) + "초", 0.9f);
            return;
        }
        if (heroBlood < 55f) {
            showToast("혈기가 부족합니다", 1.1f);
            return;
        }
        heroBlood -= 55f;
        novaCooldown = 7.5f;
        startHeroSkill(ACTION_NOVA, 0.52f, 0.17f);
    }

    private void tryStartRush() {
        Enemy target = nearestEnemy(620f);
        if (progress.level < 3 || rushCooldown > 0f || target == null) {
            return;
        }
        hero.facing = target.x >= hero.x ? 1 : -1;
        rushCooldown = 3.6f;
        hero.velocity = hero.facing * 680f;
        startHeroSkill(ACTION_RUSH, 0.34f, 0.08f);
    }

    private void tryStartRain() {
        Enemy target = nearestEnemy(620f);
        if (progress.level < 6 || rainCooldown > 0f || target == null) {
            return;
        }
        hero.facing = target.x >= hero.x ? 1 : -1;
        rainCooldown = 6.2f;
        startHeroSkill(ACTION_RAIN, 0.48f, 0.13f);
    }

    private void tryStartChain() {
        Enemy target = nearestEnemy(620f);
        if (progress.level < 9 || chainCooldown > 0f || target == null) {
            return;
        }
        hero.facing = target.x >= hero.x ? 1 : -1;
        chainCooldown = 5.2f;
        startHeroSkill(ACTION_CHAIN, 0.42f, 0.105f);
    }

    private void tryStartPillar() {
        Enemy target = nearestEnemy(620f);
        if (progress.level < 12 || pillarCooldown > 0f || target == null) {
            return;
        }
        hero.facing = target.x >= hero.x ? 1 : -1;
        pillarCooldown = 8.4f;
        startHeroSkill(ACTION_PILLAR, 0.54f, 0.16f);
    }

    private void tryStartEclipse() {
        Enemy target = nearestEnemy(620f);
        if (progress.level < 15 || eclipseCooldown > 0f || target == null) {
            return;
        }
        hero.facing = target.x >= hero.x ? 1 : -1;
        eclipseCooldown = 13.5f;
        startHeroSkill(ACTION_ECLIPSE, 0.68f, 0.22f);
    }

    private void startHeroSkill(int action, float duration, float trigger) {
        heroAction = action;
        heroActionDuration = duration;
        heroActionTimer = duration;
        heroActionTrigger = trigger;
        heroActionTriggered = false;
        comboQueued = false;
    }

    private void updateHero(float dt) {
        hero.animClock += dt;
        hero.invulnerability = Math.max(0f, hero.invulnerability - dt);
        hero.hurtTimer = Math.max(0f, hero.hurtTimer - dt);
        if (hero.dead) {
            hero.velocity = approach(hero.velocity, 0f, 760f * dt);
            hero.x = RpgRules.clamp(hero.x + hero.velocity * dt,
                    RpgRules.ARENA_LEFT, RpgRules.ARENA_RIGHT);
            return;
        }

        if (heroAction != ACTION_NONE) {
            float elapsed = heroActionDuration - heroActionTimer;
            heroActionTimer = Math.max(0f, heroActionTimer - dt);
            if (!heroActionTriggered && elapsed >= heroActionTrigger) {
                heroActionTriggered = true;
                executeHeroAction();
            }
            if (heroActionTimer <= 0f) {
                int finished = heroAction;
                heroAction = ACTION_NONE;
                if (finished == ACTION_ATTACK) {
                    comboGrace = 0.22f;
                    comboDisplayTimer = 0.8f;
                    if (comboQueued && comboIndex < 2) {
                        startAttack(comboIndex + 1);
                    }
                }
            }
        }

        if (hero.dashTimer > 0f) {
            hero.dashTimer = Math.max(0f, hero.dashTimer - dt);
            hero.x += hero.velocity * dt;
            hero.runDistance += Math.abs(hero.velocity) * dt;
            worldTravel += Math.max(0f, hero.velocity) * dt;
            if (hero.dashTimer <= 0f) {
                hero.velocity *= 0.28f;
            }
        } else {
            float movementMultiplier = heroAction == ACTION_NONE ? 1f
                    : heroAction == ACTION_ATTACK ? 1.32f
                    : heroAction == ACTION_RUSH ? 1.85f : 0.24f;
            Enemy target = nearestEnemy(1000f);
            float desired = 0f;
            if (target == null) {
                desired = remainingToSpawn > 0 ? 1f : 0f;
            } else {
                float gap = target.x - hero.x;
                float stopDistance = heroAction == ACTION_ATTACK ? 68f
                        : heroAction == ACTION_RUSH ? 54f : 104f;
                if (Math.abs(gap) > stopDistance) {
                    desired = gap > 0f ? 1f : -1f;
                }
                hero.facing = gap >= 0f ? 1 : -1;
            }
            float targetVelocity = desired * 278f * movementMultiplier;
            float acceleration = heroAction == ACTION_ATTACK || heroAction == ACTION_RUSH
                    ? 1900f : 1320f;
            hero.velocity = approach(hero.velocity, targetVelocity, acceleration * dt);
            hero.x += hero.velocity * dt;
            hero.runDistance += Math.abs(hero.velocity) * dt;
            worldTravel += Math.max(0f, hero.velocity) * dt;
            int footstep = (int) (hero.runDistance / 46f);
            if (Math.abs(hero.velocity) > 80f && footstep > hero.lastFootstep) {
                hero.lastFootstep = footstep;
                addFootstep(hero.x - hero.facing * 18f, GROUND_Y + 2f);
            }
        }
        hero.x = RpgRules.clamp(hero.x, RpgRules.ARENA_LEFT, RpgRules.ARENA_RIGHT);
    }

    private void executeHeroAction() {
        if (heroAction == ACTION_ATTACK) {
            Enemy target = nearestEnemy(205f + comboIndex * 10f);
            if (target != null && faces(hero.x, hero.facing, target.x)) {
                int damage = RpgRules.meleeDamage(heroAttackPower, comboIndex);
                damageEnemy(target, damage, 95f + comboIndex * 52f, comboIndex == 2);
                addSlashArc(hero.x + hero.facing * 63f, GROUND_Y - 112f,
                        hero.facing, comboIndex == 2 ? GOLD : CRIMSON);
                comboDisplayTimer = 1f;
            } else {
                addSlashArc(hero.x + hero.facing * 58f, GROUND_Y - 112f,
                        hero.facing, Color.argb(175, 210, 214, 230));
            }
        } else if (heroAction == ACTION_SPEAR) {
            Projectile projectile = new Projectile();
            projectile.enemyOwned = false;
            projectile.kind = 0;
            projectile.x = hero.x + hero.facing * 52f;
            projectile.previousX = projectile.x;
            projectile.y = GROUND_Y - 112f;
            projectile.velocityX = hero.facing * 920f;
            projectile.damage = RpgRules.spearDamage(heroAttackPower, progress.spearLevel);
            projectile.life = 0.88f;
            projectile.maxLife = projectile.life;
            projectile.radius = 46f;
            projectile.pierce = 3;
            projectile.color = CRIMSON;
            projectiles.add(projectile);
            addBurst(projectile.x, projectile.y, CRIMSON, 32, 345f);
            cameraKickX = -hero.facing * 4f;
            cameraZoomPulse = Math.max(cameraZoomPulse, 0.008f);
            skillBloom = Math.max(skillBloom, 0.48f);
            audio.playBloodSpear();
        } else if (heroAction == ACTION_SIPHON) {
            Enemy target = nearestEnemy(350f);
            if (target != null) {
                int damage = RpgRules.siphonDamage(heroAttackPower, progress.siphonLevel);
                damageEnemy(target, damage, 48f, true);
                float healed = Math.min(heroMaxHealth - heroHealth, damage * 0.62f);
                heroHealth += healed;
                heroBlood = Math.min(heroMaxBlood, heroBlood + damage * 0.22f);
                addBloodTether(target.x, GROUND_Y - 112f, hero.x, GROUND_Y - 112f);
                addSkillEffect(FX_SPEAR_IMPACT, target.x, GROUND_Y - 108f,
                        hero.x, GROUND_Y - 112f, CRIMSON, 0.32f, 126f, hero.facing);
                hitStop = Math.max(hitStop, 0.055f);
                cameraZoomPulse = Math.max(cameraZoomPulse, 0.018f);
                skillBloom = Math.max(skillBloom, 0.48f);
                floatingTexts.add(new FloatingText(hero.x, GROUND_Y - 230f,
                        "+" + Math.round(healed), CYAN, 1.05f));
                audio.playSiphon();
            }
        } else if (heroAction == ACTION_NOVA) {
            int hits = 0;
            int damage = RpgRules.novaDamage(heroAttackPower, progress.novaLevel);
            for (Enemy enemy : enemies) {
                if (!enemy.dead && Math.abs(enemy.x - hero.x) <= 255f) {
                    damageEnemy(enemy, damage, 180f, true);
                    hits++;
                }
            }
            addNovaBurst(hero.x, GROUND_Y - 90f, 310f);
            addSkillEffect(FX_NOVA, hero.x, GROUND_Y - 82f,
                    hero.x, GROUND_Y - 82f, CRIMSON, 0.56f, 330f, hero.facing);
            impactX = hero.x;
            impactY = GROUND_Y - 88f;
            impactDirection = hero.facing;
            impactHeavy = true;
            impactFlash = Math.max(impactFlash, 0.96f);
            screenShake = Math.max(screenShake, 6f);
            hitStop = hits > 0 ? 0.09f : 0.035f;
            cameraKickY = -3f;
            cameraZoomPulse = Math.max(cameraZoomPulse, 0.018f);
            skillBloom = 1f;
            audio.playNova();
        } else if (heroAction == ACTION_RUSH) {
            Enemy target = nearestEnemy(620f);
            if (target != null) {
                float startX = hero.x;
                hero.facing = target.x >= hero.x ? 1 : -1;
                float destination = target.x - hero.facing * 62f;
                hero.x = RpgRules.clamp(destination,
                        RpgRules.ARENA_LEFT, RpgRules.ARENA_RIGHT);
                hero.previousX = startX;
                hero.velocity = hero.facing * 410f;
                addSkillEffect(FX_RUSH, startX, GROUND_Y - 104f,
                        target.x, GROUND_Y - 104f, CRIMSON,
                        0.36f, Math.abs(target.x - startX), hero.facing);
                damageEnemy(target, RpgRules.rushDamage(heroAttackPower, progress.level),
                        135f, true);
                addSlashArc(target.x, GROUND_Y - 104f, hero.facing, Color.WHITE);
                addSlashArc(target.x - hero.facing * 24f,
                        GROUND_Y - 126f, hero.facing, CRIMSON);
                addImpactBurst(target.x, GROUND_Y - 104f,
                        hero.facing, 28, 390f);
                skillBloom = Math.max(skillBloom, 0.62f);
                audio.playDash();
            }
        } else if (heroAction == ACTION_RAIN) {
            int hits = 0;
            int damage = RpgRules.bladeRainDamage(heroAttackPower, progress.level);
            for (Enemy enemy : enemies) {
                if (enemy.dead || enemy.spawnTimer > 0.18f) {
                    continue;
                }
                addSkillEffect(FX_RAIN, enemy.x, GROUND_Y - 112f,
                        enemy.x, GROUND_Y - 112f, CRIMSON,
                        0.54f, 148f, hero.facing);
                damageEnemy(enemy, damage, 92f, true);
                addBladeRainBurst(enemy.x, GROUND_Y - 108f);
                hits++;
            }
            if (hits > 0) {
                impactX = hero.x;
                impactY = GROUND_Y - 110f;
                impactFlash = Math.max(impactFlash, 0.7f);
                screenShake = Math.max(screenShake, 5f);
                hitStop = Math.max(hitStop, 0.038f);
                cameraZoomPulse = Math.max(cameraZoomPulse, 0.015f);
                skillBloom = Math.max(skillBloom, 0.82f);
                audio.playBloodSpear();
            }
        } else if (heroAction == ACTION_CHAIN) {
            int hits = 0;
            float fromX = hero.x;
            float fromY = GROUND_Y - 118f;
            int damage = RpgRules.bloodChainDamage(heroAttackPower, progress.level);
            for (Enemy enemy : enemies) {
                if (enemy.dead || enemy.spawnTimer > 0.18f
                        || Math.abs(enemy.x - hero.x) > 620f || hits >= 4) {
                    continue;
                }
                float targetX = enemy.x;
                float targetY = GROUND_Y - 112f;
                addSkillEffect(FX_CHAIN, fromX, fromY, targetX, targetY,
                        CYAN, 0.52f, Math.abs(targetX - fromX), hero.facing);
                damageEnemy(enemy, damage, 72f, hits == 3);
                addImpactBurst(targetX, targetY, targetX >= fromX ? 1f : -1f,
                        14, 255f);
                fromX = targetX;
                fromY = targetY;
                hits++;
            }
            if (hits > 0) {
                heroBlood = Math.min(heroMaxBlood, heroBlood + hits * 5f);
                hitStop = Math.max(hitStop, 0.045f);
                skillBloom = Math.max(skillBloom, 0.72f);
                cameraZoomPulse = Math.max(cameraZoomPulse, 0.016f);
                audio.playChain();
            }
        } else if (heroAction == ACTION_PILLAR) {
            int hits = 0;
            int damage = RpgRules.crimsonPillarDamage(heroAttackPower, progress.level);
            for (Enemy enemy : enemies) {
                if (enemy.dead || enemy.spawnTimer > 0.18f
                        || Math.abs(enemy.x - hero.x) > 640f) {
                    continue;
                }
                addSkillEffect(FX_PILLAR, enemy.x, GROUND_Y - 44f,
                        enemy.x, GROUND_Y - 300f, CRIMSON,
                        0.68f, 190f, hero.facing);
                damageEnemy(enemy, damage, 150f, true);
                addNovaBurst(enemy.x, GROUND_Y - 60f, 132f);
                hits++;
            }
            if (hits > 0) {
                impactX = hero.x;
                impactY = GROUND_Y - 105f;
                impactFlash = Math.max(impactFlash, 0.82f);
                hitStop = Math.max(hitStop, 0.065f);
                screenShake = Math.max(screenShake, 5.5f);
                cameraZoomPulse = Math.max(cameraZoomPulse, 0.02f);
                skillBloom = Math.max(skillBloom, 0.9f);
                audio.playPillar();
            }
        } else if (heroAction == ACTION_ECLIPSE) {
            int hits = 0;
            int damage = RpgRules.eclipseDamage(heroAttackPower, progress.level);
            for (Enemy enemy : enemies) {
                if (!enemy.dead && enemy.spawnTimer <= 0.18f) {
                    damageEnemy(enemy, damage, 230f, true);
                    addImpactBurst(enemy.x, GROUND_Y - 112f,
                            enemy.x >= hero.x ? 1f : -1f, 22, 350f);
                    hits++;
                }
            }
            addSkillEffect(FX_ECLIPSE, hero.x, GROUND_Y - 300f,
                    hero.x, GROUND_Y - 300f, Color.rgb(245, 53, 104),
                    1.05f, 560f, hero.facing);
            addNovaBurst(hero.x, GROUND_Y - 94f, 430f);
            impactX = hero.x;
            impactY = GROUND_Y - 220f;
            impactDirection = hero.facing;
            impactHeavy = true;
            impactFlash = 1f;
            hitStop = hits > 0 ? 0.11f : 0.045f;
            screenShake = Math.max(screenShake, 6f);
            cameraKickY = -4f;
            cameraZoomPulse = Math.max(cameraZoomPulse, 0.026f);
            skillBloom = 1f;
            audio.playEclipse();
        }
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private void updateWaveSpawning(float dt) {
        if (waveComplete || hero.dead || remainingToSpawn <= 0) {
            return;
        }
        spawnTimer -= dt;
        if (spawnTimer <= 0f && aliveEnemyCount() < MAX_ACTIVE_ENEMIES) {
            int batchSize = RpgRules.reinforcementBatchSize(aliveEnemyCount(),
                    remainingToSpawn, MAX_ACTIVE_ENEMIES);
            for (int index = 0; index < batchSize; index++) {
                spawnEnemy();
            }
            spawnTimer = progress.wave == RpgRules.WAVES_PER_REGION ? 0.26f : 0.16f;
        }
    }

    private void spawnEnemy() {
        int kind;
        if (progress.wave == RpgRules.WAVES_PER_REGION && spawnSerial == 0) {
            kind = RpgRules.ENEMY_BOSS;
        } else {
            int roll = (spawnSerial + progress.wave + progress.region * 2) % 6;
            if (progress.region == 0) {
                kind = roll < 4 ? RpgRules.ENEMY_THRALL : RpgRules.ENEMY_HUNTER;
            } else if (progress.region == 1) {
                kind = roll < 2 ? RpgRules.ENEMY_THRALL
                        : roll < 4 ? RpgRules.ENEMY_HUNTER : RpgRules.ENEMY_WRAITH;
            } else {
                kind = roll < 2 ? RpgRules.ENEMY_HUNTER : RpgRules.ENEMY_WRAITH;
            }
        }

        Enemy enemy = new Enemy();
        enemy.id = nextEnemyId++;
        enemy.kind = kind;
        // This is a side-scrolling hunt: danger always enters from the road ahead.
        enemy.x = 638f - spawnSerial % 3 * 18f;
        enemy.previousX = enemy.x;
        enemy.facing = enemy.x > hero.x ? -1 : 1;
        enemy.maxHealth = RpgRules.enemyMaxHealth(kind, progress.region, progress.wave,
                progress.level, progress.chapterClears);
        enemy.health = enemy.maxHealth;
        enemy.cooldown = 0.65f + random.nextFloat() * 0.7f;
        enemy.spawnTimer = kind == RpgRules.ENEMY_BOSS ? 0.88f : 0.42f;
        enemies.add(enemy);
        remainingToSpawn--;
        spawnSerial++;
        addBurst(enemy.x, GROUND_Y - 55f,
                kind == RpgRules.ENEMY_BOSS ? GOLD : VIOLET,
                kind == RpgRules.ENEMY_BOSS ? 34 : 16, 155f);
        if (kind == RpgRules.ENEMY_BOSS) {
            screenShake = 10f;
            showToast("지역 보스 · " + ENEMY_NAMES[kind], 2.2f);
        }
    }

    private void updateEnemies(float dt) {
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            enemy.animClock += dt;
            enemy.invulnerability = Math.max(0f, enemy.invulnerability - dt);
            if (enemy.dead) {
                enemy.deadTimer -= dt;
                enemy.velocity = approach(enemy.velocity, 0f, 440f * dt);
                enemy.x += enemy.velocity * dt;
                if (enemy.deadTimer <= 0f) {
                    iterator.remove();
                }
                continue;
            }
            if (enemy.spawnTimer > 0f) {
                enemy.spawnTimer = Math.max(0f, enemy.spawnTimer - dt);
                continue;
            }
            enemy.hurtTimer = Math.max(0f, enemy.hurtTimer - dt);
            enemy.facing = hero.x >= enemy.x ? 1 : -1;

            if (enemy.hurtTimer > 0f) {
                enemy.velocity = approach(enemy.velocity, 0f, 520f * dt);
            } else if (enemy.actionTimer > 0f) {
                updateEnemyAction(enemy, dt);
            } else if (!hero.dead && !waveComplete) {
                enemy.cooldown = Math.max(0f, enemy.cooldown - dt);
                updateEnemyIntent(enemy, dt);
            } else {
                enemy.velocity = approach(enemy.velocity, 0f, 500f * dt);
            }

            enemy.x = RpgRules.clamp(enemy.x + enemy.velocity * dt,
                    RpgRules.ARENA_LEFT, RpgRules.ARENA_RIGHT);
            enemy.runDistance += Math.abs(enemy.velocity) * dt;
        }
    }

    private void updateEnemyIntent(Enemy enemy, float dt) {
        float distance = Math.abs(hero.x - enemy.x);
        if (enemy.kind == RpgRules.ENEMY_THRALL) {
            if (distance > 92f) {
                enemy.velocity = approach(enemy.velocity, enemy.facing * 82f, 420f * dt);
            } else {
                enemy.velocity = approach(enemy.velocity, 0f, 640f * dt);
                if (enemy.cooldown <= 0f) {
                    startEnemyAction(enemy, ENEMY_MELEE, 0.68f, 0.38f);
                }
            }
        } else if (enemy.kind == RpgRules.ENEMY_HUNTER) {
            if (distance < 175f) {
                enemy.velocity = approach(enemy.velocity, -enemy.facing * 92f, 470f * dt);
            } else if (distance > 310f) {
                enemy.velocity = approach(enemy.velocity, enemy.facing * 62f, 360f * dt);
            } else {
                enemy.velocity = approach(enemy.velocity, 0f, 510f * dt);
                if (enemy.cooldown <= 0f) {
                    startEnemyAction(enemy, ENEMY_RANGED, 0.88f, 0.53f);
                }
            }
        } else if (enemy.kind == RpgRules.ENEMY_WRAITH) {
            if (distance > 205f) {
                enemy.velocity = approach(enemy.velocity, enemy.facing * 112f, 540f * dt);
            } else {
                enemy.velocity = approach(enemy.velocity, 0f, 600f * dt);
                if (enemy.cooldown <= 0f) {
                    startEnemyAction(enemy, ENEMY_PHASE, 0.78f, 0.46f);
                }
            }
        } else {
            float healthRatio = enemy.health / Math.max(1f, enemy.maxHealth);
            if (distance > 152f) {
                float speed = healthRatio < 0.45f ? 102f : 78f;
                enemy.velocity = approach(enemy.velocity, enemy.facing * speed, 430f * dt);
            } else {
                enemy.velocity = approach(enemy.velocity, 0f, 600f * dt);
            }
            if (enemy.cooldown <= 0f) {
                float roll = random.nextFloat();
                if (healthRatio < 0.62f && roll < 0.28f) {
                    startEnemyAction(enemy, ENEMY_NOVA, 1.26f, 0.88f);
                } else if (distance > 180f || roll < 0.48f) {
                    startEnemyAction(enemy, ENEMY_RANGED, 0.96f, 0.58f);
                } else {
                    startEnemyAction(enemy, ENEMY_HEAVY, 1.02f, 0.66f);
                }
            }
        }
    }

    private void startEnemyAction(Enemy enemy, int type, float duration, float trigger) {
        enemy.actionType = type;
        enemy.actionDuration = duration;
        enemy.actionTimer = duration;
        enemy.actionTrigger = trigger;
        enemy.actionTriggered = false;
        enemy.velocity = 0f;
    }

    private void updateEnemyAction(Enemy enemy, float dt) {
        float elapsed = enemy.actionDuration - enemy.actionTimer;
        enemy.actionTimer = Math.max(0f, enemy.actionTimer - dt);
        if (!enemy.actionTriggered && elapsed >= enemy.actionTrigger) {
            enemy.actionTriggered = true;
            executeEnemyAction(enemy);
        }
        if (enemy.actionTimer <= 0f) {
            float haste = enemy.kind == RpgRules.ENEMY_BOSS
                    && enemy.health < enemy.maxHealth * 0.45f ? 0.72f : 1f;
            enemy.cooldown = (0.72f + random.nextFloat() * 0.62f) * haste;
            enemy.actionType = 0;
        }
    }

    private void executeEnemyAction(Enemy enemy) {
        boolean heavy = enemy.actionType == ENEMY_HEAVY || enemy.actionType == ENEMY_NOVA;
        int damage = RpgRules.enemyDamage(enemy.kind, progress.region, progress.wave,
                progress.level, progress.chapterClears, heavy);
        if (enemy.actionType == ENEMY_MELEE) {
            if (Math.abs(hero.x - enemy.x) <= 116f) {
                damageHero(damage, enemy.x, 128f);
            }
            addSlashArc(enemy.x + enemy.facing * 55f, GROUND_Y - 102f,
                    enemy.facing, VIOLET);
        } else if (enemy.actionType == ENEMY_HEAVY) {
            if (Math.abs(hero.x - enemy.x) <= 164f) {
                damageHero(damage, enemy.x, 190f);
            }
            addNovaBurst(enemy.x, GROUND_Y - 60f, 150f);
            screenShake = Math.max(screenShake, 10f);
        } else if (enemy.actionType == ENEMY_RANGED) {
            Projectile projectile = new Projectile();
            projectile.enemyOwned = true;
            projectile.kind = enemy.kind == RpgRules.ENEMY_BOSS ? 2 : 1;
            projectile.x = enemy.x + enemy.facing * 48f;
            projectile.previousX = projectile.x;
            projectile.y = GROUND_Y - 108f;
            projectile.velocityX = enemy.facing
                    * (enemy.kind == RpgRules.ENEMY_BOSS ? 365f : 315f);
            projectile.damage = damage;
            projectile.life = 2.1f;
            projectile.maxLife = projectile.life;
            projectile.radius = enemy.kind == RpgRules.ENEMY_BOSS ? 34f : 24f;
            projectile.pierce = 1;
            projectile.color = enemy.kind == RpgRules.ENEMY_BOSS ? GOLD : VIOLET;
            projectiles.add(projectile);
            addBurst(projectile.x, projectile.y, projectile.color, 10, 120f);
        } else if (enemy.actionType == ENEMY_PHASE) {
            int side = hero.facing > 0 ? -1 : 1;
            enemy.x = RpgRules.clamp(hero.x + side * 82f,
                    RpgRules.ARENA_LEFT, RpgRules.ARENA_RIGHT);
            enemy.facing = hero.x >= enemy.x ? 1 : -1;
            addBurst(enemy.x, GROUND_Y - 94f, VIOLET, 20, 190f);
            if (Math.abs(hero.x - enemy.x) <= 112f) {
                damageHero(damage, enemy.x, 150f);
            }
        } else if (enemy.actionType == ENEMY_NOVA) {
            if (Math.abs(hero.x - enemy.x) <= 255f) {
                damageHero(damage, enemy.x, 230f);
            }
            addNovaBurst(enemy.x, GROUND_Y - 82f, 255f);
            screenShake = Math.max(screenShake, 15f);
        }
    }

    private void resolveActorSpacing() {
        for (int first = 0; first < enemies.size(); first++) {
            Enemy a = enemies.get(first);
            if (a.dead || a.spawnTimer > 0f) {
                continue;
            }
            for (int second = first + 1; second < enemies.size(); second++) {
                Enemy b = enemies.get(second);
                if (b.dead || b.spawnTimer > 0f) {
                    continue;
                }
                float difference = b.x - a.x;
                float distance = Math.abs(difference);
                if (distance < 44f) {
                    float direction = difference == 0f ? (second % 2 == 0 ? 1f : -1f)
                            : Math.signum(difference);
                    float push = (44f - distance) * 0.5f;
                    a.x -= direction * push;
                    b.x += direction * push;
                }
            }
        }
        if (hero.dashTimer <= 0f && !hero.dead) {
            for (Enemy enemy : enemies) {
                if (enemy.dead || enemy.spawnTimer > 0f) {
                    continue;
                }
                float difference = enemy.x - hero.x;
                float distance = Math.abs(difference);
                if (distance < 58f) {
                    float direction = difference == 0f ? hero.facing : Math.signum(difference);
                    float push = (58f - distance) * 0.5f;
                    hero.x -= direction * push;
                    enemy.x += direction * push;
                }
            }
        }
        hero.x = RpgRules.clamp(hero.x, RpgRules.ARENA_LEFT, RpgRules.ARENA_RIGHT);
        for (Enemy enemy : enemies) {
            enemy.x = RpgRules.clamp(enemy.x, RpgRules.ARENA_LEFT, RpgRules.ARENA_RIGHT);
        }
    }

    private void recenterCombat(float dt) {
        float shift = RpgRules.combatRecenteringShift(hero.x, dt);
        if (shift <= 0f) {
            return;
        }
        hero.x -= shift;
        hero.previousX -= shift;
        for (Enemy enemy : enemies) {
            enemy.x = Math.max(RpgRules.ARENA_LEFT, enemy.x - shift);
            enemy.previousX = Math.max(RpgRules.ARENA_LEFT, enemy.previousX - shift);
        }
        for (Projectile projectile : projectiles) {
            projectile.x -= shift;
            projectile.previousX -= shift;
        }
        for (Particle particle : particles) {
            particle.x -= shift;
        }
        for (FloatingText floatingText : floatingTexts) {
            floatingText.x -= shift;
        }
        for (SkillEffect effect : skillEffects) {
            effect.x -= shift;
            effect.targetX -= shift;
        }
        if (impactX > 0f) {
            impactX -= shift;
        }
    }

    private void updateProjectiles(float dt) {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            projectile.previousX = projectile.x;
            projectile.x += projectile.velocityX * dt;
            projectile.life -= dt;
            projectile.trailTimer -= dt;
            if (!projectile.enemyOwned && projectile.kind == 0 && projectile.trailTimer <= 0f) {
                projectile.trailTimer = 0.018f;
                float direction = Math.signum(projectile.velocityX);
                addStreakParticle(projectile.x - direction * 34f,
                        projectile.y + (random.nextFloat() - 0.5f) * 24f,
                        -direction * (75f + random.nextFloat() * 80f),
                        (random.nextFloat() - 0.5f) * 55f,
                        random.nextInt(5) == 0 ? GOLD : CRIMSON,
                        3f + random.nextFloat() * 3f, 0.24f, 0f);
            }
            boolean remove = projectile.life <= 0f
                    || projectile.x < -90f || projectile.x > LOGICAL_WIDTH + 90f;
            if (!remove && projectile.enemyOwned) {
                if (!hero.dead && Math.abs(projectile.x - hero.x) <= projectile.radius + 31f
                        && Math.abs(projectile.y - (GROUND_Y - 110f)) < 80f) {
                    damageHero(projectile.damage, projectile.x, 118f);
                    addBurst(projectile.x, projectile.y, projectile.color, 14, 180f);
                    remove = true;
                }
            } else if (!remove) {
                for (Enemy enemy : enemies) {
                    if (enemy.dead || projectile.hitIds.contains(enemy.id)) {
                        continue;
                    }
                    if (Math.abs(projectile.x - enemy.x) <= projectile.radius + 34f) {
                        projectile.hitIds.add(enemy.id);
                        damageEnemy(enemy, projectile.damage, 115f, true);
                        if (!projectile.enemyOwned && projectile.kind == 0) {
                            addSkillEffect(FX_SPEAR_IMPACT, projectile.x, projectile.y,
                                    projectile.x, projectile.y, CRIMSON,
                                    0.34f, 92f, Math.signum(projectile.velocityX));
                            skillBloom = Math.max(skillBloom, 0.5f);
                        }
                        projectile.pierce--;
                        if (projectile.pierce <= 0) {
                            remove = true;
                            break;
                        }
                    }
                }
            }
            if (remove) {
                iterator.remove();
            }
        }
    }

    private void damageEnemy(Enemy enemy, int amount, float knockback, boolean heavy) {
        if (enemy.dead || enemy.invulnerability > 0f) {
            return;
        }
        int applied = Math.max(1, amount);
        enemy.health = Math.max(0f, enemy.health - applied);
        boolean killed = enemy.health <= 0f;
        enemy.hurtTimer = heavy || killed ? 0.27f : 0.16f;
        enemy.invulnerability = 0.04f;
        float direction = enemy.x >= hero.x ? 1f : -1f;
        enemy.velocity += direction * knockback * (killed ? 1.32f : heavy ? 1.18f : 1f);
        if (heroAction == ACTION_ATTACK) {
            float followThrough = comboIndex == 2 ? 390f : 325f;
            hero.velocity = direction * Math.max(Math.abs(hero.velocity), followThrough);
        }
        floatingTexts.add(new FloatingText(enemy.x, GROUND_Y - 210f,
                "-" + applied, heavy ? GOLD : Color.WHITE, 0.9f));
        addBurst(enemy.x, GROUND_Y - 106f, CRIMSON, heavy ? 18 : 10, heavy ? 230f : 155f);
        registerEnemyImpact(enemy.x, GROUND_Y - 104f, direction, heavy, killed);
        if (killed) {
            enemy.dead = true;
            enemy.deadTimer = enemy.kind == RpgRules.ENEMY_BOSS ? 1.15f : 0.68f;
            enemy.actionTimer = 0f;
            rewardEnemy(enemy);
        }
    }

    private void damageHero(int rawAmount, float sourceX, float knockback) {
        if (hero.dead || hero.invulnerability > 0f) {
            return;
        }
        int amount = RpgRules.mitigateDamage(rawAmount, progress.armorPower);
        heroHealth = Math.max(0f, heroHealth - amount);
        hero.invulnerability = 0.46f;
        hero.hurtTimer = 0.25f;
        heroAction = ACTION_NONE;
        comboQueued = false;
        float direction = hero.x >= sourceX ? 1f : -1f;
        hero.velocity = direction * knockback;
        damageFlash = Math.min(1f, damageFlash + 0.72f);
        floatingTexts.add(new FloatingText(hero.x, GROUND_Y - 230f,
                "-" + amount, Color.rgb(255, 104, 120), 1f));
        addBurst(hero.x, GROUND_Y - 108f, CRIMSON, 18, 210f);
        registerHeroImpact(hero.x, GROUND_Y - 110f, direction);
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        if (heroHealth <= 0f) {
            hero.dead = true;
            hero.deadTimer = 1.15f;
            hero.velocity = direction * 170f;
            resetPointers();
        }
    }

    private void registerEnemyImpact(float x, float y, float direction,
                                     boolean heavy, boolean killed) {
        boolean major = heavy || killed;
        impactX = x;
        impactY = y;
        impactDirection = direction;
        impactHeavy = major;
        impactFlash = Math.max(impactFlash, killed ? 1f : heavy ? 0.82f : 0.48f);
        cameraKickX = -direction * (killed ? 17f : heavy ? 11f : 5f);
        cameraKickY = major ? -5f : -2f;
        screenShake = Math.max(screenShake, killed ? 16f : heavy ? 11f : 5.5f);
        hitStop = Math.max(hitStop, killed ? 0.072f : heavy ? 0.048f : 0.022f);
        addImpactBurst(x, y, direction, major ? 22 : 12, major ? 320f : 210f);
        audio.playHit(heavy, killed, comboIndex);
        performHapticFeedback(major
                ? HapticFeedbackConstants.LONG_PRESS : HapticFeedbackConstants.VIRTUAL_KEY);
    }

    private void registerHeroImpact(float x, float y, float direction) {
        impactX = x;
        impactY = y;
        impactDirection = direction;
        impactHeavy = true;
        impactFlash = Math.max(impactFlash, 0.88f);
        cameraKickX = -direction * 13f;
        cameraKickY = -5f;
        screenShake = Math.max(screenShake, 13f);
        hitStop = Math.max(hitStop, 0.06f);
        addImpactBurst(x, y, direction, 20, 280f);
        audio.playPlayerHurt();
    }

    private void rewardEnemy(Enemy enemy) {
        if (enemy.rewarded) {
            return;
        }
        enemy.rewarded = true;
        int xp = RpgRules.xpReward(enemy.kind, progress.region, progress.wave,
                progress.chapterClears);
        int gold = RpgRules.goldReward(enemy.kind, progress.region, progress.wave,
                progress.chapterClears);
        progress.xp += xp;
        progress.gold += gold;
        progress.kills++;
        defeatedThisWave++;
        heroBlood = Math.min(heroMaxBlood, heroBlood + 7f);
        floatingTexts.add(new FloatingText(enemy.x, GROUND_Y - 260f,
                "+" + xp + " XP  ·  +" + gold + " G", GOLD, 1.35f));
        gainLevels();
        rollEquipment(enemy.kind == RpgRules.ENEMY_BOSS);
        markSaveDirty();
    }

    private void gainLevels() {
        int previousLevel = progress.level;
        boolean leveled = false;
        while (progress.level < RpgRules.LEVEL_CAP
                && progress.xp >= RpgRules.xpForNextLevel(progress.level)) {
            progress.xp -= RpgRules.xpForNextLevel(progress.level);
            progress.level++;
            leveled = true;
        }
        if (progress.level >= RpgRules.LEVEL_CAP) {
            progress.xp = 0;
        }
        if (leveled) {
            int previousSiphon = progress.siphonLevel;
            int previousNova = progress.novaLevel;
            progress.normalizeUnlocks();
            syncHeroStats(false);
            heroHealth = Math.min(heroMaxHealth, heroHealth + heroMaxHealth * 0.38f);
            heroBlood = heroMaxBlood;
            levelBannerText = "LEVEL UP  ·  " + progress.level;
            if (previousLevel < 15 && progress.level >= 15) {
                levelBannerText = "개기월식 해금  ·  LEVEL " + progress.level;
            } else if (previousLevel < 12 && progress.level >= 12) {
                levelBannerText = "진홍기둥 해금  ·  LEVEL " + progress.level;
            } else if (previousLevel < 9 && progress.level >= 9) {
                levelBannerText = "혈사슬 해금  ·  LEVEL " + progress.level;
            } else if (previousLevel < 6 && progress.level >= 6) {
                levelBannerText = "적월검우 해금  ·  LEVEL " + progress.level;
            } else if (previousLevel < 3 && progress.level >= 3) {
                levelBannerText = "혈영쇄도 해금  ·  LEVEL " + progress.level;
            } else if (previousSiphon == 0 && progress.siphonLevel > 0) {
                levelBannerText = "흡혈 해금  ·  LEVEL " + progress.level;
            } else if (previousNova == 0 && progress.novaLevel > 0) {
                levelBannerText = "혈월 폭발 해금  ·  LEVEL " + progress.level;
            }
            levelBannerTimer = 2.8f;
            addNovaBurst(hero.x, GROUND_Y - 100f, 210f);
            audio.playLevelUp();
            saveNow();
        }
    }

    private void rollEquipment(boolean bossDrop) {
        float chance = bossDrop ? 1f : 0.13f + progress.region * 0.035f;
        if (random.nextFloat() > chance) {
            return;
        }
        float roll = random.nextFloat();
        int rarity = roll > 0.97f ? 3 : roll > 0.84f ? 2 : roll > 0.52f ? 1 : 0;
        if (bossDrop) {
            rarity = Math.max(1, rarity);
        }
        int power = RpgRules.equipmentPower(progress.region, progress.wave, progress.level,
                rarity, progress.chapterClears);
        int slot = random.nextInt(3);
        int current = equippedPower(slot);
        int item = makeItemCode(slot, rarity, power);
        if (power > current) {
            if (current > 0) {
                int replaced = makeItemCode(slot, rarityFromPower(current), current);
                if (!stashItem(replaced)) {
                    progress.gold += salvageValue(replaced);
                }
            }
            setEquippedPower(slot, power);
            syncHeroStats(false);
            showToast(rarityName(rarity) + " " + slotName(slot)
                    + " 자동 장착  ·  +" + power, 2.4f);
            floatingTexts.add(new FloatingText(hero.x, GROUND_Y - 310f,
                    "장비 교체!", rarityColor(rarity), 1.6f));
        } else if (stashItem(item)) {
            showToast(rarityName(rarity) + " " + slotName(slot) + " 가방 보관", 1.8f);
        } else {
            int salvage = salvageValue(item);
            progress.gold += salvage;
            showToast("가방이 가득 차 " + slotName(slot) + " 자동 분해  ·  +"
                    + salvage + " 골드", 2f);
        }
        saveNow();
    }

    private void beginWaveClear() {
        waveComplete = true;
        waveClearTimer = 2.7f;
        heroHealth = Math.min(heroMaxHealth, heroHealth + heroMaxHealth * 0.2f);
        heroBlood = Math.min(heroMaxBlood, heroBlood + heroMaxBlood * 0.35f);
        heroAction = ACTION_NONE;
        projectiles.clear();
        showToast(progress.wave == RpgRules.WAVES_PER_REGION
                ? "지역 정복 · 다음 길이 열립니다" : "웨이브 완료 · 회복 중", 2.3f);
        saveNow();
    }

    private void advanceAdventure() {
        if (progress.wave < RpgRules.WAVES_PER_REGION) {
            progress.wave++;
            saveNow();
            startCurrentWave(false);
        } else {
            progress.bossKills++;
            if (progress.region < RpgRules.REGION_COUNT - 1) {
                progress.region++;
                progress.wave = 1;
                chapterBannerTimer = 3.2f;
                levelBannerText = "새 지역 해금  ·  " + REGION_NAMES[progress.region];
                levelBannerTimer = 3.2f;
                saveNow();
                openStory(progress.region, true, false);
            } else {
                progress.chapterClears++;
                saveNow();
                openStory(3, false, true);
            }
        }
    }

    private void startNewAdventure() {
        audio.playUiTap();
        progressStore.clear();
        progress = RpgProgress.fresh();
        continueAvailable = true;
        beginAdventure();
        saveNow();
    }

    private void continueAdventure() {
        audio.playUiTap();
        long nowEpochSeconds = System.currentTimeMillis() / 1000L;
        long lastActiveEpochSeconds = progressStore.lastActiveEpochSeconds();
        RpgProgress loaded = progressStore.load();
        progress = loaded == null ? RpgProgress.fresh() : loaded;
        progress.normalizeUnlocks();
        beginAdventure();
        if (loaded != null) {
            applyOfflineRewards(RpgRules.offlineElapsedSeconds(
                    lastActiveEpochSeconds, nowEpochSeconds));
        }
    }

    private void applyOfflineRewards(int elapsedSeconds) {
        if (elapsedSeconds < 60) {
            return;
        }
        offlineElapsedSeconds = elapsedSeconds;
        offlineGoldReward = RpgRules.offlineGoldReward(elapsedSeconds,
                progress.level, progress.region, progress.wave);
        offlineXpReward = RpgRules.offlineXpReward(elapsedSeconds,
                progress.level, progress.region, progress.wave);
        int previousLevel = progress.level;
        progress.gold = (int) Math.min(100_000_000L,
                (long) progress.gold + offlineGoldReward);
        progress.xp = (int) Math.min(9_999_999L,
                (long) progress.xp + offlineXpReward);
        gainLevels();
        offlineLevelsGained = progress.level - previousLevel;
        offlineReturnScreen = screen;
        screen = Screen.OFFLINE_REWARD;
        resetPointers();
        saveNow();
    }

    private void claimOfflineRewards() {
        audio.playUiTap();
        screen = offlineReturnScreen;
        offlineElapsedSeconds = 0;
        offlineGoldReward = 0;
        offlineXpReward = 0;
        offlineLevelsGained = 0;
        resetPointers();
        lastFrameNanos = System.nanoTime();
    }

    private void beginAdventure() {
        ensureCombatAtlases();
        hero.reset(190f, 1);
        syncHeroStats(true);
        spearCooldown = 0f;
        siphonCooldown = 0f;
        novaCooldown = 0f;
        rushCooldown = 0f;
        rainCooldown = 0f;
        chainCooldown = 0f;
        pillarCooldown = 0f;
        eclipseCooldown = 0f;
        autoSkillThinkTimer = 0f;
        worldTravel = 0f;
        openStory(progress.region, true, false);
    }

    private void openStory(int chapter, boolean fullRestore, boolean finale) {
        storyChapter = RpgRules.clamp(chapter, 0, STORY_LINES.length - 1);
        storyLine = 0;
        storyStartsWave = !finale;
        storyFullRestore = fullRestore;
        storyFinale = finale;
        screen = Screen.STORY;
        resetPointers();
    }

    private void advanceStory() {
        if (screen != Screen.STORY) {
            return;
        }
        audio.playUiTap();
        storyLine++;
        if (storyLine < STORY_LINES[storyChapter].length) {
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            return;
        }
        if (storyFinale) {
            progress.region = 0;
            progress.wave = 1;
            saveNow();
            screen = Screen.TITLE;
            continueAvailable = true;
            return;
        }
        if (storyStartsWave) {
            screen = Screen.PLAYING;
            lastFrameNanos = System.nanoTime();
            startCurrentWave(storyFullRestore);
        }
    }

    private void startCurrentWave(boolean fullRestore) {
        enemies.clear();
        projectiles.clear();
        particles.clear();
        skillEffects.clear();
        floatingTexts.clear();
        remainingToSpawn = RpgRules.waveEnemyCount(progress.region, progress.wave);
        waveTarget = remainingToSpawn;
        defeatedThisWave = 0;
        spawnSerial = 0;
        nextEnemyId = 1;
        spawnTimer = 0.18f;
        waveComplete = false;
        waveClearTimer = 0f;
        waveBannerTimer = 2.2f;
        hero.dead = false;
        hero.deadTimer = 0f;
        hero.invulnerability = 0.55f;
        hero.x = 190f;
        hero.previousX = hero.x;
        hero.velocity = 0f;
        hero.facing = 1;
        heroAction = ACTION_NONE;
        queuedSkillAction = ACTION_NONE;
        queuedSkillTimer = 0f;
        autoSkillThinkTimer = 0.12f;
        comboQueued = false;
        if (fullRestore) {
            heroHealth = heroMaxHealth;
            heroBlood = heroMaxBlood;
        } else {
            heroHealth = Math.min(heroMaxHealth, heroHealth + heroMaxHealth * 0.28f);
            heroBlood = Math.min(heroMaxBlood, heroBlood + heroMaxBlood * 0.42f);
        }
    }

    private void retryCurrentWave() {
        syncHeroStats(true);
        hero.reset(190f, 1);
        screen = Screen.PLAYING;
        lastFrameNanos = System.nanoTime();
        startCurrentWave(true);
        showToast("성장 기록은 유지됩니다", 1.8f);
    }

    private void syncHeroStats(boolean refill) {
        float oldMaxHealth = heroMaxHealth;
        float oldMaxBlood = heroMaxBlood;
        heroMaxHealth = RpgRules.heroMaxHealth(progress.level, progress.vitalityLevel);
        heroMaxBlood = RpgRules.heroMaxBlood(progress.level, progress.bloodLevel,
                progress.relicPower);
        heroAttackPower = RpgRules.heroAttackPower(progress.level, progress.mightLevel,
                progress.weaponPower);
        if (refill || oldMaxHealth <= 0f) {
            heroHealth = heroMaxHealth;
        } else {
            heroHealth = Math.min(heroMaxHealth,
                    heroHealth + Math.max(0f, heroMaxHealth - oldMaxHealth));
        }
        if (refill || oldMaxBlood <= 0f) {
            heroBlood = heroMaxBlood;
        } else {
            heroBlood = Math.min(heroMaxBlood,
                    heroBlood + Math.max(0f, heroMaxBlood - oldMaxBlood));
        }
    }

    private void purchaseUpgrade(int index) {
        int current;
        int cap;
        int requiredLevel = 1;
        if (index == 0) {
            current = progress.vitalityLevel;
            cap = RpgRules.UPGRADE_CAP;
        } else if (index == 1) {
            current = progress.mightLevel;
            cap = RpgRules.UPGRADE_CAP;
        } else if (index == 2) {
            current = progress.bloodLevel;
            cap = RpgRules.UPGRADE_CAP;
        } else if (index == 3) {
            current = progress.recoveryLevel;
            cap = RpgRules.UPGRADE_CAP;
        } else if (index == 4) {
            current = progress.spearLevel;
            cap = 12;
        } else if (index == 5) {
            current = progress.siphonLevel;
            cap = 12;
            requiredLevel = 4;
        } else {
            current = progress.novaLevel;
            cap = 12;
            requiredLevel = 7;
        }
        if (progress.level < requiredLevel || current <= 0 && requiredLevel > 1) {
            showToast("레벨 " + requiredLevel + "에 해금됩니다", 1.2f);
            return;
        }
        if (current >= cap) {
            showToast("최대 단계입니다", 1.1f);
            return;
        }
        int cost = index < 4 ? RpgRules.statUpgradeCost(current)
                : RpgRules.skillUpgradeCost(current);
        if (progress.gold < cost) {
            showToast("골드가 " + (cost - progress.gold) + " 부족합니다", 1.2f);
            return;
        }
        progress.gold -= cost;
        if (index == 0) {
            progress.vitalityLevel++;
        } else if (index == 1) {
            progress.mightLevel++;
        } else if (index == 2) {
            progress.bloodLevel++;
        } else if (index == 3) {
            progress.recoveryLevel++;
        } else if (index == 4) {
            progress.spearLevel++;
        } else if (index == 5) {
            progress.siphonLevel++;
        } else {
            progress.novaLevel++;
        }
        syncHeroStats(false);
        showToast("강화 완료  ·  전투력이 상승했습니다", 1.5f);
        addBurst(hero.x, GROUND_Y - 110f, GOLD, 20, 175f);
        audio.playUiTap();
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        saveNow();
    }

    private void markSaveDirty() {
        saveDirty = true;
        saveDelay = 0.65f;
    }

    private void saveNow() {
        progressStore.store(progress);
        continueAvailable = true;
        saveDirty = false;
        saveDelay = 0f;
    }

    private int aliveEnemyCount() {
        int result = 0;
        for (Enemy enemy : enemies) {
            if (!enemy.dead) {
                result++;
            }
        }
        return result;
    }

    private Enemy nearestEnemy(float maximumDistance) {
        Enemy nearest = null;
        float best = maximumDistance;
        for (Enemy enemy : enemies) {
            if (enemy.dead || enemy.spawnTimer > 0.18f) {
                continue;
            }
            float distance = Math.abs(enemy.x - hero.x);
            if (distance <= best) {
                best = distance;
                nearest = enemy;
            }
        }
        return nearest;
    }

    private void updateParticles(float dt) {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.life -= dt;
            if (particle.life <= 0f) {
                iterator.remove();
                continue;
            }
            particle.x += particle.velocityX * dt;
            particle.y += particle.velocityY * dt;
            particle.velocityY += particle.gravity * dt;
            particle.velocityX *= Math.max(0f, 1f - dt * 1.7f);
        }
    }

    private void updateSkillEffects(float dt) {
        Iterator<SkillEffect> iterator = skillEffects.iterator();
        while (iterator.hasNext()) {
            SkillEffect effect = iterator.next();
            effect.life -= dt;
            if (effect.life <= 0f) {
                iterator.remove();
            }
        }
    }

    private void updateFloatingTexts(float dt) {
        Iterator<FloatingText> iterator = floatingTexts.iterator();
        while (iterator.hasNext()) {
            FloatingText floatingText = iterator.next();
            floatingText.life -= dt;
            floatingText.y -= dt * 42f;
            if (floatingText.life <= 0f) {
                iterator.remove();
            }
        }
    }

    private void addBurst(float x, float y, int color, int count, float speed) {
        for (int index = 0; index < count && particles.size() < MAX_PARTICLES; index++) {
            float angle = random.nextFloat() * (float) Math.PI * 2f;
            float magnitude = speed * (0.3f + random.nextFloat() * 0.7f);
            addParticle(x, y, (float) Math.cos(angle) * magnitude,
                    (float) Math.sin(angle) * magnitude, color,
                    2.5f + random.nextFloat() * 5f, 0.35f + random.nextFloat() * 0.42f, 170f);
        }
    }

    private void addSlashArc(float x, float y, int direction, int color) {
        for (int index = 0; index < 16 && particles.size() < MAX_PARTICLES; index++) {
            float angle = (direction > 0 ? -1.15f : 1.99f) + index * 0.095f;
            float magnitude = 175f + index * 4f;
            addStreakParticle(x, y, (float) Math.cos(angle) * magnitude,
                    (float) Math.sin(angle) * magnitude, color,
                    3f + index * 0.16f, 0.26f + index * 0.012f, 70f);
        }
    }

    private void addBloodTether(float fromX, float fromY, float toX, float toY) {
        addSkillEffect(FX_TETHER, fromX, fromY, toX, toY,
                CRIMSON, 0.48f, Math.abs(toX - fromX), Math.signum(toX - fromX));
        for (int index = 0; index < 22 && particles.size() < MAX_PARTICLES; index++) {
            float fraction = index / 21f;
            float x = fromX + (toX - fromX) * fraction;
            float y = fromY + (toY - fromY) * fraction
                    + (float) Math.sin(fraction * Math.PI) * -28f;
            addParticle(x, y, (toX - fromX) * 0.18f, -18f,
                    index % 3 == 0 ? CYAN : CRIMSON, 4.5f, 0.65f, -25f);
        }
    }

    private void addNovaBurst(float x, float y, float radius) {
        for (int index = 0; index < 58 && particles.size() < MAX_PARTICLES; index++) {
            float angle = index / 58f * (float) Math.PI * 2f;
            float speed = radius * (0.75f + random.nextFloat() * 0.55f);
            addStreakParticle(x, y, (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed * 0.58f,
                    index % 5 == 0 ? GOLD : CRIMSON,
                    4f + random.nextFloat() * 5f, 0.58f, 55f);
        }
    }

    private void addBladeRainBurst(float x, float y) {
        for (int index = 0; index < 24 && particles.size() < MAX_PARTICLES; index++) {
            float spread = (random.nextFloat() - 0.5f) * 130f;
            float speed = 330f + random.nextFloat() * 260f;
            addStreakParticle(x + spread, y - 120f - random.nextFloat() * 120f,
                    hero.facing * (65f + random.nextFloat() * 120f), speed,
                    index % 5 == 0 ? Color.WHITE : index % 4 == 0 ? GOLD : CRIMSON,
                    3f + random.nextFloat() * 4f, 0.28f + random.nextFloat() * 0.18f, 0f);
        }
    }

    private void addFootstep(float x, float y) {
        int dust = progress.region == 0 ? Color.rgb(124, 166, 190)
                : progress.region == 1 ? Color.rgb(135, 104, 111) : Color.rgb(150, 119, 104);
        for (int index = 0; index < 4; index++) {
            addParticle(x + random.nextFloat() * 18f - 9f, y,
                    -hero.facing * (18f + random.nextFloat() * 34f),
                    -18f - random.nextFloat() * 30f, withAlpha(dust, 150),
                    2f + random.nextFloat() * 2.5f, 0.28f, 58f);
        }
    }

    private void addImpactBurst(float x, float y, float direction, int count, float speed) {
        for (int index = 0; index < count && particles.size() < MAX_PARTICLES; index++) {
            float angle = (random.nextFloat() - 0.5f) * 1.55f;
            float velocity = speed * (0.45f + random.nextFloat() * 0.7f);
            int color = index % 4 == 0 ? Color.WHITE : index % 5 == 0 ? GOLD : CRIMSON;
            addStreakParticle(x, y,
                    direction * (float) Math.cos(angle) * velocity,
                    (float) Math.sin(angle) * velocity * 0.72f,
                    color, 2.5f + random.nextFloat() * 4f,
                    0.2f + random.nextFloat() * 0.24f, 85f);
        }
    }

    private void addParticle(float x, float y, float velocityX, float velocityY,
                             int color, float radius, float life, float gravity) {
        if (particles.size() >= MAX_PARTICLES) {
            return;
        }
        Particle particle = new Particle();
        particle.x = x;
        particle.y = y;
        particle.velocityX = velocityX;
        particle.velocityY = velocityY;
        particle.color = color;
        particle.radius = radius;
        particle.life = life;
        particle.maxLife = life;
        particle.gravity = gravity;
        particles.add(particle);
    }

    private void addStreakParticle(float x, float y, float velocityX, float velocityY,
                                   int color, float radius, float life, float gravity) {
        if (particles.size() >= MAX_PARTICLES) {
            return;
        }
        Particle particle = new Particle();
        particle.x = x;
        particle.y = y;
        particle.velocityX = velocityX;
        particle.velocityY = velocityY;
        particle.color = color;
        particle.radius = radius;
        particle.life = life;
        particle.maxLife = life;
        particle.gravity = gravity;
        particle.style = 1;
        particles.add(particle);
    }

    private void addSkillEffect(int kind, float x, float y, float targetX, float targetY,
                                int color, float life, float radius, float direction) {
        if (skillEffects.size() >= 48) {
            return;
        }
        SkillEffect effect = new SkillEffect();
        effect.kind = kind;
        effect.x = x;
        effect.y = y;
        effect.targetX = targetX;
        effect.targetY = targetY;
        effect.color = color;
        effect.life = life;
        effect.maxLife = life;
        effect.radius = radius;
        effect.direction = direction;
        effect.seed = random.nextFloat() * 10f;
        skillEffects.add(effect);
    }

    private void drawScene(Canvas canvas) {
        float comfortShake = Math.min(5.5f, screenShake * 0.24f);
        float shakeX = cameraKickX * 0.42f + (comfortShake > 0f
                ? (float) Math.sin(ambientClock * 61f) * comfortShake : 0f);
        float shakeY = cameraKickY * 0.34f + (comfortShake > 0f
                ? (float) Math.cos(ambientClock * 47f) * comfortShake * 0.22f : 0f);
        canvas.save();
        canvas.translate(shakeX, shakeY);
        if (cameraZoomPulse > 0f) {
            float pivotX = impactX > 1f ? impactX : LOGICAL_WIDTH * 0.5f;
            float pivotY = impactY > 1f ? impactY : GROUND_Y - 100f;
            float comfortZoom = cameraZoomPulse * 0.36f;
            canvas.scale(1f + comfortZoom, 1f + comfortZoom, pivotX, pivotY);
        }
        drawBackground(canvas);
        if (screen == Screen.STORY || screen == Screen.OFFLINE_REWARD) {
            drawStoryStage(canvas);
        } else if (screen != Screen.TITLE && screen != Screen.NEW_CONFIRM) {
            drawArena(canvas);
        } else {
            drawTitleAtmosphere(canvas);
        }
        canvas.restore();
        drawImpactFlash(canvas);
        drawSkillBloom(canvas);

        if (screen == Screen.TITLE || screen == Screen.NEW_CONFIRM) {
            drawTitle(canvas);
            if (screen == Screen.NEW_CONFIRM) {
                drawNewConfirm(canvas);
            }
        } else if (screen == Screen.STORY) {
            drawStory(canvas);
        } else if (screen == Screen.OFFLINE_REWARD) {
            drawOfflineReward(canvas);
        } else {
            drawHud(canvas);
            if (screen == Screen.PLAYING) {
                drawControls(canvas);
            }
            drawBanners(canvas);
            drawToast(canvas);
            if (screen == Screen.PAUSED) {
                drawPause(canvas);
            } else if (screen == Screen.GROWTH) {
                drawGrowth(canvas);
            } else if (screen == Screen.INVENTORY) {
                drawInventory(canvas);
            } else if (screen == Screen.DEFEAT) {
                drawDefeat(canvas);
            }
        }
        if (damageFlash > 0f) {
            paint.setColor(Color.argb(Math.round(112f * damageFlash), 190, 8, 36));
            canvas.drawRect(0f, 0f, LOGICAL_WIDTH, LOGICAL_HEIGHT, paint);
        }
    }

    private void drawSkillBloom(Canvas canvas) {
        if (skillBloom <= 0f || screen != Screen.PLAYING) {
            return;
        }
        float centerX = heroRenderX();
        float centerY = GROUND_Y - 100f;
        int alpha = Math.round(92f * RpgRules.clamp(skillBloom, 0f, 1f));
        paint.setShader(new RadialGradient(centerX, centerY, 330f,
                new int[]{withAlpha(Color.WHITE, alpha), withAlpha(CRIMSON, alpha / 2),
                        Color.TRANSPARENT},
                new float[]{0f, 0.26f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(centerX, centerY, 330f, paint);
        paint.setShader(null);
    }

    private void drawImpactFlash(Canvas canvas) {
        if (impactFlash <= 0f || screen != Screen.PLAYING) {
            return;
        }
        int alpha = Math.round(impactFlash * (impactHeavy ? 195f : 135f));
        float radius = impactHeavy ? 112f : 72f;
        paint.setShader(new RadialGradient(impactX, impactY, radius,
                new int[]{withAlpha(Color.WHITE, alpha), withAlpha(GOLD, alpha / 2),
                        Color.TRANSPARENT},
                new float[]{0f, 0.2f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(impactX, impactY, radius, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(impactHeavy ? 6f : 3f);
        paint.setColor(withAlpha(Color.WHITE, Math.min(230, alpha)));
        float spread = impactHeavy ? 94f : 58f;
        for (int index = -2; index <= 2; index++) {
            float angle = index * 0.25f;
            float dx = impactDirection * (float) Math.cos(angle) * spread;
            float dy = (float) Math.sin(angle) * spread * 0.65f;
            canvas.drawLine(impactX + dx * 0.3f, impactY + dy * 0.3f,
                    impactX + dx, impactY + dy, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawBackground(Canvas canvas) {
        int region = progress == null ? 0 : RpgRules.clamp(progress.region, 0, 2);
        Bitmap background = obtainBackground(region);
        float parallax = screen == Screen.TITLE ? (float) Math.sin(ambientClock * 0.08f) * 8f
                : -(worldTravel * 0.035f) % 24f;
        if (background != null && !background.isRecycled()) {
            paint.setColorFilter(BACKGROUND_LIFT_FILTER);
            canvas.drawBitmap(background, null,
                    new RectF(-44f - parallax, -32f, LOGICAL_WIDTH + 44f - parallax,
                            LOGICAL_HEIGHT + 32f), paint);
            paint.setColorFilter(null);
        } else {
            int upper = region == 0 ? Color.rgb(10, 28, 55)
                    : region == 1 ? Color.rgb(39, 13, 31) : Color.rgb(31, 14, 24);
            int lower = region == 0 ? Color.rgb(12, 16, 30)
                    : region == 1 ? Color.rgb(23, 13, 25) : Color.rgb(18, 10, 18);
            paint.setShader(new LinearGradient(0f, 0f, 0f, LOGICAL_HEIGHT,
                    upper, lower, Shader.TileMode.CLAMP));
            canvas.drawRect(0f, 0f, LOGICAL_WIDTH, LOGICAL_HEIGHT, paint);
            paint.setShader(null);
            paint.setColor(withAlpha(region == 0 ? CYAN : CRIMSON, 125));
            canvas.drawCircle(525f, 205f, 82f, paint);
        }
        paint.setShader(new LinearGradient(0f, 0f, 0f, LOGICAL_HEIGHT,
                Color.argb(4, 4, 5, 14), Color.argb(42, 3, 4, 10), Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 0f, LOGICAL_WIDTH, LOGICAL_HEIGHT, paint);
        paint.setShader(null);
        drawAmbientMotes(canvas, region);
        if (screen == Screen.PLAYING || screen == Screen.PAUSED || screen == Screen.GROWTH
                || screen == Screen.INVENTORY) {
            drawTravelingForeground(canvas, region);
        }
    }

    private void drawTravelingForeground(Canvas canvas, int region) {
        float roadOffset = -(worldTravel * 0.72f) % 168f;
        int stoneColor = region == 0 ? Color.rgb(52, 70, 86)
                : region == 1 ? Color.rgb(69, 50, 58) : Color.rgb(76, 53, 48);
        paint.setColor(Color.argb(142, 14, 16, 24));
        canvas.drawRect(0f, GROUND_Y + 18f, LOGICAL_WIDTH, 936f, paint);
        for (int index = -1; index < 6; index++) {
            float x = roadOffset + index * 168f;
            paint.setColor(withAlpha(stoneColor, 185));
            canvas.drawOval(new RectF(x, GROUND_Y + 24f, x + 118f, GROUND_Y + 52f), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setColor(Color.argb(90, 205, 220, 226));
            canvas.drawArc(new RectF(x, GROUND_Y + 24f, x + 118f, GROUND_Y + 52f),
                    188f, 148f, false, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawAmbientMotes(Canvas canvas, int region) {
        int color = region == 0 ? CYAN : region == 1 ? CRIMSON : GOLD;
        for (int index = 0; index < 17; index++) {
            float x = (index * 83f + ambientClock * (7f + index % 4) * (index % 2 == 0 ? 1f : -1f))
                    % (LOGICAL_WIDTH + 80f);
            if (x < -40f) {
                x += LOGICAL_WIDTH + 80f;
            }
            float y = 270f + (index * 97f % 535f)
                    + (float) Math.sin(ambientClock * 0.7f + index) * 17f;
            float pulse = 0.45f + 0.35f * (float) Math.sin(ambientClock * 1.2f + index * 0.8f);
            paint.setColor(withAlpha(color, Math.round(66f + pulse * 62f)));
            canvas.drawCircle(x, y, 1.5f + (index % 3), paint);
        }
    }

    private void drawTitleAtmosphere(Canvas canvas) {
        paint.setShader(new LinearGradient(0f, 300f, 0f, 1120f,
                Color.TRANSPARENT, Color.argb(218, 4, 5, 12), Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 240f, LOGICAL_WIDTH, LOGICAL_HEIGHT, paint);
        paint.setShader(null);
        drawVignette(canvas);
    }

    private void drawStoryStage(Canvas canvas) {
        paint.setShader(new LinearGradient(0f, 250f, 0f, LOGICAL_HEIGHT,
                Color.argb(30, 4, 6, 14), Color.argb(232, 3, 4, 10), Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 190f, LOGICAL_WIDTH, LOGICAL_HEIGHT, paint);
        paint.setShader(null);
        drawFighterShadow(canvas, 200f, 0.9f, 48f);
        float oldX = hero.x;
        int oldFacing = hero.facing;
        hero.x = 200f;
        hero.facing = 1;
        drawHero(canvas);
        hero.x = oldX;
        hero.facing = oldFacing;
        drawVignette(canvas);
    }

    private void drawArena(Canvas canvas) {
        drawCombatVisibilityWash(canvas);
        drawGroundGlow(canvas);
        drawCombatSpeedLines(canvas);
        drawSkillEffects(canvas, false);
        for (Enemy enemy : enemies) {
            drawEnemyTelegraph(canvas, enemy);
        }
        for (Enemy enemy : enemies) {
            if (!enemy.dead) {
                drawActorReadabilityGlow(canvas, enemyRenderX(enemy),
                        enemy.kind == RpgRules.ENEMY_BOSS ? GOLD : CYAN,
                        enemy.kind == RpgRules.ENEMY_BOSS ? 104f : 72f);
            }
        }
        drawActorReadabilityGlow(canvas, heroRenderX(), CRIMSON, 82f);
        for (Enemy enemy : enemies) {
            drawFighterShadow(canvas, enemyRenderX(enemy), enemy.dead ? 0.3f : 0.85f,
                    enemy.kind == RpgRules.ENEMY_BOSS ? 50f : 32f);
        }
        drawFighterShadow(canvas, heroRenderX(), hero.dead ? 0.35f : 1f, 35f);
        for (Enemy enemy : enemies) {
            drawEnemy(canvas, enemy);
        }
        drawHero(canvas);
        drawProjectiles(canvas);
        drawSkillEffects(canvas, true);
        drawParticles(canvas);
        drawFloatingTexts(canvas);
        drawVignette(canvas);
    }

    private void drawGroundGlow(Canvas canvas) {
        paint.setShader(new RadialGradient(LOGICAL_WIDTH * 0.5f, GROUND_Y + 18f, 330f,
                Color.argb(92, 112, 144, 168), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawOval(new RectF(18f, GROUND_Y - 55f, LOGICAL_WIDTH - 18f, GROUND_Y + 88f), paint);
        paint.setShader(null);
    }

    private void drawCombatVisibilityWash(Canvas canvas) {
        paint.setShader(new LinearGradient(0f, 300f, 0f, GROUND_Y + 50f,
                Color.argb(8, 88, 112, 142), Color.argb(34, 105, 123, 148),
                Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 270f, LOGICAL_WIDTH, GROUND_Y + 54f, paint);
        paint.setShader(null);
    }

    private void drawActorReadabilityGlow(Canvas canvas, float x, int color, float radius) {
        paint.setColor(withAlpha(color, 18));
        canvas.drawCircle(x, GROUND_Y - 72f, radius, paint);
        paint.setColor(withAlpha(Color.WHITE, 13));
        canvas.drawCircle(x, GROUND_Y - 72f, radius * 0.58f, paint);
    }

    private void drawCombatSpeedLines(Canvas canvas) {
        float velocityIntensity = RpgRules.clamp((Math.abs(hero.velocity) - 150f) / 380f,
                0f, 1f);
        float actionIntensity = heroAction == ACTION_RUSH ? 1f
                : heroAction == ACTION_ATTACK ? 0.48f : 0f;
        float intensity = Math.max(velocityIntensity, actionIntensity);
        if (intensity <= 0.02f || hero.dead) {
            return;
        }
        float direction = Math.abs(hero.velocity) > 20f
                ? Math.signum(hero.velocity) : hero.facing;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int index = 0; index < 18; index++) {
            float phase = (ambientClock * (720f + index * 19f) + index * 83f)
                    % (LOGICAL_WIDTH + 220f);
            float x = direction > 0f ? LOGICAL_WIDTH + 110f - phase : -110f + phase;
            float y = 326f + (index * 67f % 520f);
            float length = (52f + index % 5 * 24f) * intensity;
            paint.setStrokeWidth((index % 4 == 0 ? 4f : 2f) * intensity);
            paint.setColor(withAlpha(index % 6 == 0 ? CYAN : Color.WHITE,
                    Math.round((index % 4 == 0 ? 92f : 48f) * intensity)));
            canvas.drawLine(x - direction * length, y + index % 3 * 3f,
                    x + direction * 20f, y, paint);
        }
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawFighterShadow(Canvas canvas, float x, float alpha, float width) {
        paint.setColor(Color.argb(Math.round(118f * alpha), 0, 0, 0));
        canvas.drawOval(new RectF(x - width, GROUND_Y - 12f, x + width, GROUND_Y + 19f), paint);
    }

    private void drawHero(Canvas canvas) {
        float x = heroRenderX();
        int firstRow = 1;
        int firstColumn = 0;
        int secondRow = firstRow;
        int secondColumn = firstColumn;
        float blend = 0f;
        float lean = 0f;
        float lunge = 0f;
        float scaleX = 1f;
        float scaleY = 1f;
        float actionProgress = heroActionDuration <= 0f ? 0f
                : RpgRules.clamp(1f - heroActionTimer / heroActionDuration, 0f, 1f);
        if (hero.dead) {
            firstRow = secondRow = 3;
            firstColumn = secondColumn = 3;
            lean = hero.facing * 2f;
        } else if (hero.hurtTimer > 0f) {
            firstRow = secondRow = 3;
            firstColumn = secondColumn = 2;
            lean = -hero.facing * 7f * (hero.hurtTimer / 0.25f);
        } else if (heroAction == ACTION_ATTACK) {
            if (comboIndex == 0) {
                float contact = heroActionTrigger / Math.max(0.01f, heroActionDuration);
                if (actionProgress < contact) {
                    firstRow = 1;
                    firstColumn = 0;
                    secondRow = 2;
                    secondColumn = 0;
                    blend = easeOutCubic(actionProgress / Math.max(0.01f, contact));
                } else {
                    firstRow = 2;
                    firstColumn = 0;
                    secondRow = 2;
                    secondColumn = 1;
                    blend = easeOutCubic((actionProgress - contact)
                            / Math.max(0.01f, 1f - contact));
                }
            } else if (comboIndex == 1) {
                firstRow = secondRow = 2;
                firstColumn = 1;
                secondColumn = 2;
                blend = easeOutCubic(actionProgress);
            } else if (actionProgress < 0.72f) {
                firstRow = secondRow = 2;
                firstColumn = 2;
                secondColumn = 3;
                blend = easeOutCubic(actionProgress / 0.72f);
            } else {
                firstRow = 2;
                firstColumn = 3;
                secondRow = 1;
                secondColumn = 0;
                blend = smootherStep((actionProgress - 0.72f) / 0.28f);
            }
            float strikePulse = (float) Math.sin(Math.PI
                    * RpgRules.clamp(actionProgress * 1.24f, 0f, 1f));
            lunge = hero.facing * strikePulse * (comboIndex == 2 ? 31f : 23f);
            lean = -hero.facing * (4f + strikePulse * (comboIndex == 2 ? 8f : 5f));
            scaleX = 1f + strikePulse * 0.035f;
            scaleY = 1f - strikePulse * 0.025f;
        } else if (heroAction == ACTION_RUSH) {
            firstRow = secondRow = 2;
            firstColumn = actionProgress < 0.48f ? 1 : 2;
            secondColumn = actionProgress < 0.48f ? 2 : 3;
            blend = easeOutCubic(actionProgress < 0.48f
                    ? actionProgress / 0.48f : (actionProgress - 0.48f) / 0.52f);
            lunge = hero.facing * (10f + 16f
                    * (float) Math.sin(actionProgress * Math.PI));
            lean = -hero.facing * 10f;
            scaleX = 1.08f;
            scaleY = 0.92f;
        } else if (hero.dashTimer > 0f) {
            float dashProgress = RpgRules.clamp(1f - hero.dashTimer / 0.19f, 0f, 1f);
            firstRow = secondRow = 1;
            if (dashProgress < 0.2f) {
                firstColumn = 0;
                secondColumn = 1;
                blend = smootherStep(dashProgress / 0.2f);
            } else if (dashProgress < 0.72f) {
                firstColumn = 1;
                secondColumn = 2;
                blend = smootherStep((dashProgress - 0.2f) / 0.52f);
            } else {
                firstColumn = 2;
                secondColumn = 3;
                blend = smootherStep((dashProgress - 0.72f) / 0.28f);
            }
            lean = -hero.facing * (8f + 4f * (float) Math.sin(dashProgress * Math.PI));
            scaleX = 1.065f;
            scaleY = 0.94f;
        } else if (heroAction != ACTION_NONE) {
            int skillColumn = heroAction == ACTION_SPEAR || heroAction == ACTION_RAIN ? 0 : 1;
            if (actionProgress < 0.3f) {
                firstRow = 1;
                firstColumn = 0;
                secondRow = 3;
                secondColumn = skillColumn;
                blend = easeOutCubic(actionProgress / 0.3f);
            } else if (actionProgress < 0.8f) {
                firstRow = secondRow = 3;
                firstColumn = secondColumn = skillColumn;
            } else {
                firstRow = 3;
                firstColumn = skillColumn;
                secondRow = 1;
                secondColumn = 0;
                blend = smootherStep((actionProgress - 0.8f) / 0.2f);
            }
            float castPulse = (float) Math.sin(Math.PI * actionProgress);
            lean = hero.facing * (heroAction == ACTION_NOVA ? 1.5f : 3f) * castPulse;
            scaleX = 1f - castPulse * 0.018f;
            scaleY = 1f + castPulse * 0.028f;
        } else if (Math.abs(hero.velocity) > 24f) {
            float framePosition = hero.runDistance / 26f;
            int baseFrame = (int) Math.floor(framePosition);
            firstRow = secondRow = 0;
            firstColumn = Math.floorMod(baseFrame, 4);
            secondColumn = Math.floorMod(baseFrame + 1, 4);
            blend = smootherStep(framePosition - (float) Math.floor(framePosition));
            float gait = framePosition * (float) Math.PI * 0.5f;
            scaleX = 1f + (float) Math.cos(gait * 2f) * 0.012f;
            scaleY = 1f - (float) Math.cos(gait * 2f) * 0.012f;
            lean = -hero.facing * 2.2f;
        } else {
            float breath = (float) Math.sin(hero.animClock * 2.25f);
            scaleX = 1f - breath * 0.006f;
            scaleY = 1f + breath * 0.009f;
        }

        if (heroAction == ACTION_SPEAR || heroAction == ACTION_SIPHON
                || heroAction == ACTION_NOVA || heroAction == ACTION_RAIN
                || heroAction == ACTION_CHAIN || heroAction == ACTION_PILLAR
                || heroAction == ACTION_ECLIPSE) {
            drawHeroSkillCharge(canvas, x, actionProgress);
        }
        float width = 152f;
        float height = 152f;
        float drawX = x + lunge;
        spriteDestination.set(drawX - width * 0.5f,
                GROUND_Y - height + 8f, drawX + width * 0.5f, GROUND_Y + 8f);
        int alpha = hero.invulnerability > 0f
                && ((int) (hero.invulnerability * 32f) & 1) == 0 ? 142 : 255;
        canvas.save();
        canvas.rotate(lean, drawX, GROUND_Y + 5f);
        canvas.scale(scaleX, scaleY, drawX, GROUND_Y + 7f);
        boolean drawMotionTrail = hero.dashTimer > 0f || heroAction == ACTION_RUSH
                || Math.abs(hero.velocity) > 315f;
        if (drawMotionTrail) {
            int trailCount = heroAction == ACTION_RUSH ? 6 : hero.dashTimer > 0f ? 4 : 2;
            float spacing = heroAction == ACTION_RUSH ? 28f : hero.dashTimer > 0f ? 25f : 18f;
            for (int trail = trailCount; trail >= 1; trail--) {
                trailDestination.set(spriteDestination);
                trailDestination.offset(-hero.facing * trail * spacing, 0f);
                drawAtlasBlend(canvas, heroAtlas, 4, 4,
                        firstColumn, firstRow, secondColumn, secondRow, blend,
                        trailDestination, hero.facing > 0,
                        Math.min(92, 12 + trail * 11), 4);
            }
        }
        if (heroAtlas == null || heroAtlas.isRecycled()) {
            drawFallbackFighter(canvas, spriteDestination, CRIMSON, alpha, hero.facing);
        } else {
            drawAtlasBlend(canvas, heroAtlas, 4, 4,
                    firstColumn, firstRow, secondColumn, secondRow, blend,
                    spriteDestination, hero.facing > 0, alpha, 4);
        }
        canvas.restore();
    }

    private void drawHeroSkillCharge(Canvas canvas, float x, float progress) {
        float triggerFraction = heroActionTrigger / Math.max(0.01f, heroActionDuration);
        float charge = smootherStep(RpgRules.clamp(progress
                / Math.max(0.01f, triggerFraction), 0f, 1f));
        float release = progress <= triggerFraction ? 0f
                : smootherStep(RpgRules.clamp((progress - triggerFraction)
                / Math.max(0.01f, 1f - triggerFraction), 0f, 1f));
        int color = heroAction == ACTION_SIPHON || heroAction == ACTION_CHAIN ? CYAN
                : heroAction == ACTION_RAIN ? GOLD
                : heroAction == ACTION_PILLAR ? Color.rgb(255, 74, 76)
                : heroAction == ACTION_ECLIPSE ? Color.rgb(212, 76, 242)
                : heroAction == ACTION_NOVA ? Color.rgb(230, 45, 91) : CRIMSON;
        float centerX = heroAction == ACTION_SPEAR ? x + hero.facing * 74f : x;
        float centerY = heroAction == ACTION_NOVA || heroAction == ACTION_ECLIPSE
                ? GROUND_Y - 102f : GROUND_Y - 116f;

        paint.setShader(new RadialGradient(centerX, centerY,
                42f + charge * (heroAction == ACTION_NOVA || heroAction == ACTION_ECLIPSE
                        ? 132f : 58f),
                withAlpha(Color.WHITE, Math.round(90f * charge)),
                withAlpha(color, Math.round(78f * charge)), Shader.TileMode.CLAMP));
        canvas.drawCircle(centerX, centerY,
                42f + charge * (heroAction == ACTION_NOVA || heroAction == ACTION_ECLIPSE
                        ? 132f : 58f), paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int ring = 0; ring < (heroAction == ACTION_NOVA
                || heroAction == ACTION_ECLIPSE ? 4 : 2); ring++) {
            float radius = 34f + ring * 28f + charge * (24f + ring * 7f) - release * 12f;
            paint.setStrokeWidth(Math.max(2f, 5f - ring));
            paint.setColor(withAlpha(ring == 1 ? GOLD : color,
                    Math.round((155f - ring * 28f) * charge * (1f - release * 0.55f))));
            canvas.drawCircle(centerX, centerY, radius, paint);
        }
        if (heroAction == ACTION_SPEAR) {
            for (int ray = -2; ray <= 2; ray++) {
                float y = centerY + ray * 9f;
                float start = centerX - hero.facing * (24f - Math.abs(ray) * 4f);
                float end = centerX + hero.facing * (58f + charge * 46f - Math.abs(ray) * 7f);
                paint.setStrokeWidth(ray == 0 ? 5f : 2f);
                paint.setColor(withAlpha(ray == 0 ? Color.WHITE : CRIMSON,
                        Math.round((ray == 0 ? 190f : 130f) * charge)));
                canvas.drawLine(start, y, end, y, paint);
            }
        } else if (heroAction == ACTION_SIPHON) {
            Enemy target = nearestEnemy(350f);
            if (target != null) {
                float targetX = enemyRenderX(target);
                effectPath.reset();
                effectPath.moveTo(targetX, GROUND_Y - 116f);
                effectPath.cubicTo(targetX + hero.facing * 45f, GROUND_Y - 200f,
                        x - hero.facing * 52f, GROUND_Y - 190f,
                        x, GROUND_Y - 116f);
                paint.setStrokeWidth(3f + charge * 3f);
                paint.setColor(withAlpha(CRIMSON, Math.round(150f * charge)));
                canvas.drawPath(effectPath, paint);
            }
        } else {
            for (int ray = 0; ray < 12; ray++) {
                float angle = ambientClock * 1.3f + ray * (float) Math.PI / 6f;
                float inner = 74f + charge * 22f;
                float outer = inner + 28f + charge * 34f;
                paint.setStrokeWidth(ray % 3 == 0 ? 4f : 2f);
                paint.setColor(withAlpha(ray % 3 == 0 ? GOLD : color,
                        Math.round((ray % 3 == 0 ? 150f : 92f) * charge)));
                canvas.drawLine(centerX + (float) Math.cos(angle) * inner,
                        centerY + (float) Math.sin(angle) * inner * 0.7f,
                        centerX + (float) Math.cos(angle) * outer,
                        centerY + (float) Math.sin(angle) * outer * 0.7f, paint);
            }
        }
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawEnemy(Canvas canvas, Enemy enemy) {
        float x = enemyRenderX(enemy);
        int alpha = enemy.dead
                ? Math.round(255f * RpgRules.clamp(enemy.deadTimer
                / (enemy.kind == RpgRules.ENEMY_BOSS ? 1.15f : 0.68f), 0f, 1f))
                : enemy.spawnTimer > 0f
                ? Math.round(255f * (1f - enemy.spawnTimer
                / (enemy.kind == RpgRules.ENEMY_BOSS ? 1.1f : 0.58f))) : 255;
        if (enemy.hurtTimer > 0f && ((int) (enemy.hurtTimer * 46f) & 1) == 0) {
            alpha = Math.min(alpha, 125);
        }
        if (enemy.kind == RpgRules.ENEMY_BOSS) {
            ensureBossAtlas();
            int row = 0;
            int firstColumn = 0;
            int secondColumn = 0;
            float blend = 0f;
            if (enemy.dead) {
                row = 2;
                firstColumn = secondColumn = 3;
            } else if (enemy.actionTimer > 0f) {
                row = enemy.actionType == ENEMY_MELEE || enemy.actionType == ENEMY_HEAVY ? 1 : 2;
                float progress = 1f - enemy.actionTimer / Math.max(0.01f, enemy.actionDuration);
                float frame = RpgRules.clamp(progress * 2.25f, 0f, 2f);
                firstColumn = Math.min(2, (int) Math.floor(frame));
                secondColumn = Math.min(2, firstColumn + 1);
                blend = smootherStep(frame - (float) Math.floor(frame));
            } else if (Math.abs(enemy.velocity) > 24f) {
                float frame = enemy.runDistance / 42f;
                firstColumn = Math.floorMod((int) Math.floor(frame), 2);
                secondColumn = 1 - firstColumn;
                blend = smootherStep(frame - (float) Math.floor(frame));
            }
            float width = 220f;
            float height = 186f;
            spriteDestination.set(x - width * 0.5f,
                    GROUND_Y - height + 13f, x + width * 0.5f, GROUND_Y + 13f);
            canvas.save();
            if (enemy.hurtTimer > 0f) {
                canvas.rotate(-enemy.facing * 5.5f, x, GROUND_Y + 5f);
            } else if (Math.abs(enemy.velocity) > 24f) {
                canvas.rotate(-enemy.facing * 1.4f, x, GROUND_Y + 5f);
            }
            if (bossAtlas == null || bossAtlas.isRecycled()) {
                drawFallbackFighter(canvas, spriteDestination, GOLD, alpha, enemy.facing);
            } else {
                drawAtlasBlend(canvas, bossAtlas, 4, 3,
                        firstColumn, row, secondColumn, row, blend,
                        spriteDestination, enemy.facing > 0, alpha, 3);
            }
            canvas.restore();
        } else {
            int row = enemy.kind;
            int firstColumn;
            int secondColumn;
            float blend;
            if (enemy.actionTimer > 0f) {
                float progress = 1f - enemy.actionTimer / Math.max(0.01f, enemy.actionDuration);
                firstColumn = 2;
                secondColumn = 3;
                blend = smootherStep(progress);
            } else if (Math.abs(enemy.velocity) > 20f) {
                float frame = enemy.runDistance / 36f;
                firstColumn = Math.floorMod((int) Math.floor(frame), 2);
                secondColumn = 1 - firstColumn;
                blend = smootherStep(frame - (float) Math.floor(frame));
            } else {
                firstColumn = secondColumn = 0;
                blend = 0f;
            }
            float width = enemy.kind == RpgRules.ENEMY_WRAITH ? 150f : 142f;
            float height = enemy.kind == RpgRules.ENEMY_WRAITH ? 138f : 132f;
            spriteDestination.set(x - width * 0.5f,
                    GROUND_Y - height + 10f, x + width * 0.5f, GROUND_Y + 10f);
            canvas.save();
            if (enemy.hurtTimer > 0f) {
                canvas.rotate(-enemy.facing * 8f, x, GROUND_Y + 4f);
            } else if (Math.abs(enemy.velocity) > 20f) {
                canvas.rotate(-enemy.facing * 1.8f, x, GROUND_Y + 4f);
            }
            if (enemyAtlas == null || enemyAtlas.isRecycled()) {
                drawFallbackFighter(canvas, spriteDestination,
                        enemy.kind == RpgRules.ENEMY_WRAITH ? CYAN : VIOLET,
                        alpha, enemy.facing);
            } else {
                drawAtlasBlend(canvas, enemyAtlas, 4, 3,
                        firstColumn, row, secondColumn, row, blend,
                        spriteDestination, enemy.facing > 0, alpha, 3);
            }
            canvas.restore();
        }
        if (!enemy.dead && enemy.spawnTimer <= 0f
                && (enemy.kind == RpgRules.ENEMY_BOSS || enemy.hurtTimer > 0f)) {
            float width = enemy.kind == RpgRules.ENEMY_BOSS ? 178f : 82f;
            float y = enemy.kind == RpgRules.ENEMY_BOSS ? GROUND_Y - 204f : GROUND_Y - 150f;
            drawMiniHealthBar(canvas, x - width * 0.5f, y, width,
                    enemy.health / Math.max(1f, enemy.maxHealth),
                    enemy.kind == RpgRules.ENEMY_BOSS ? GOLD : CRIMSON);
        }
    }

    private void drawEnemyTelegraph(Canvas canvas, Enemy enemy) {
        if (enemy.dead || enemy.spawnTimer > 0f || enemy.actionTimer <= 0f
                || enemy.actionTriggered) {
            return;
        }
        float elapsed = enemy.actionDuration - enemy.actionTimer;
        float fraction = RpgRules.clamp(elapsed / Math.max(0.01f, enemy.actionTrigger), 0f, 1f);
        int color = enemy.actionType == ENEMY_RANGED ? VIOLET
                : enemy.actionType == ENEMY_NOVA || enemy.kind == RpgRules.ENEMY_BOSS
                ? GOLD : CRIMSON;
        float radius = enemy.actionType == ENEMY_NOVA ? 255f
                : enemy.kind == RpgRules.ENEMY_BOSS ? 88f : 58f;
        float x = enemyRenderX(enemy);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f + fraction * 5f);
        paint.setColor(withAlpha(color, Math.round(72f + fraction * 165f)));
        canvas.drawCircle(x, enemy.actionType == ENEMY_NOVA
                ? GROUND_Y - 78f : GROUND_Y - 142f, radius * (0.78f + fraction * 0.22f), paint);
        if (enemy.actionType == ENEMY_RANGED) {
            paint.setStrokeWidth(2f);
            paint.setColor(withAlpha(color, Math.round(38f + fraction * 82f)));
            canvas.drawLine(x, GROUND_Y - 108f, heroRenderX(), GROUND_Y - 108f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawProjectiles(Canvas canvas) {
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (Projectile projectile : projectiles) {
            float x = lerp(projectile.previousX, projectile.x, renderInterpolation);
            if (projectile.kind == 2) {
                paint.setShader(new RadialGradient(x, projectile.y,
                        projectile.radius * 1.8f, Color.WHITE, projectile.color,
                        Shader.TileMode.CLAMP));
                canvas.drawCircle(x, projectile.y, projectile.radius, paint);
                paint.setShader(null);
            } else if (!projectile.enemyOwned && projectile.kind == 0) {
                float direction = Math.signum(projectile.velocityX);
                if (bloodArtsAtlas != null && !bloodArtsAtlas.isRecycled()) {
                    drawBloodArtAtlasCell(canvas, 0, x, projectile.y,
                            210f, 132f, direction < 0f, 245, -8f * direction);
                    continue;
                }
                float pulse = 0.82f + 0.18f
                        * (float) Math.sin((projectile.maxLife - projectile.life) * 42f);
                paint.setShader(new RadialGradient(x, projectile.y,
                        projectile.radius * 1.9f,
                        withAlpha(Color.WHITE, 220), withAlpha(CRIMSON, 0),
                        Shader.TileMode.CLAMP));
                canvas.drawCircle(x, projectile.y, projectile.radius * 1.9f, paint);
                paint.setShader(null);
                for (int trail = 0; trail < 6; trail++) {
                    float wave = (float) Math.sin(ambientClock * (18f + trail * 2f)
                            + trail * 1.7f) * (5f + trail * 2f);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(Math.max(1.8f, 10f - trail * 1.35f) * pulse);
                    paint.setColor(withAlpha(trail == 0 ? Color.WHITE
                                    : trail == 3 ? GOLD : CRIMSON,
                            218 - trail * 29));
                    canvas.drawLine(x - direction * (48f + trail * 27f),
                            projectile.y + wave,
                            x + direction * (24f - trail * 2f), projectile.y - wave * 0.25f,
                            paint);
                }
                paint.setStyle(Paint.Style.FILL);
                effectPath.reset();
                effectPath.moveTo(x + direction * 52f, projectile.y);
                effectPath.lineTo(x - direction * 18f, projectile.y - 17f * pulse);
                effectPath.lineTo(x - direction * 43f, projectile.y);
                effectPath.lineTo(x - direction * 18f, projectile.y + 17f * pulse);
                effectPath.close();
                paint.setColor(CRIMSON);
                canvas.drawPath(effectPath, paint);
                effectPath.reset();
                effectPath.moveTo(x + direction * 43f, projectile.y);
                effectPath.lineTo(x - direction * 11f, projectile.y - 5f);
                effectPath.lineTo(x - direction * 26f, projectile.y);
                effectPath.lineTo(x - direction * 11f, projectile.y + 5f);
                effectPath.close();
                paint.setColor(Color.WHITE);
                canvas.drawPath(effectPath, paint);
            } else {
                float direction = Math.signum(projectile.velocityX);
                float length = projectile.enemyOwned ? 28f : 64f;
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(projectile.enemyOwned ? 8f : 11f);
                paint.setColor(projectile.color);
                canvas.drawLine(x - direction * length, projectile.y,
                        x + direction * 12f, projectile.y, paint);
                paint.setStrokeWidth(2f);
                paint.setColor(Color.WHITE);
                canvas.drawLine(x - direction * length * 0.45f, projectile.y,
                        x + direction * 8f, projectile.y, paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void drawSkillEffects(Canvas canvas, boolean foreground) {
        for (SkillEffect effect : skillEffects) {
            float progress = RpgRules.clamp(1f - effect.life / effect.maxLife, 0f, 1f);
            float fade = 1f - smootherStep(RpgRules.clamp((progress - 0.52f) / 0.48f, 0f, 1f));
            if (drawBloodArtEffect(canvas, effect, foreground, progress, fade)) {
                continue;
            }
            if (effect.kind == FX_NOVA) {
                float expansion = easeOutCubic(progress);
                float radius = effect.radius * expansion;
                if (!foreground) {
                    paint.setShader(new RadialGradient(effect.x, effect.y,
                            Math.max(1f, radius),
                            new int[]{withAlpha(Color.WHITE, Math.round(95f * fade)),
                                    withAlpha(CRIMSON, Math.round(150f * fade)),
                                    withAlpha(BLOOD, Math.round(55f * fade)),
                                    Color.TRANSPARENT},
                            new float[]{0f, 0.18f, 0.62f, 1f}, Shader.TileMode.CLAMP));
                    canvas.drawCircle(effect.x, effect.y, Math.max(1f, radius), paint);
                    paint.setShader(null);
                    paint.setColor(withAlpha(CRIMSON, Math.round(100f * fade)));
                    effectBounds.set(effect.x - radius, GROUND_Y - 30f,
                            effect.x + radius, GROUND_Y + 34f);
                    canvas.drawOval(effectBounds, paint);
                } else {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    for (int ring = 0; ring < 5; ring++) {
                        float ringRadius = radius * (0.42f + ring * 0.135f);
                        paint.setStrokeWidth(Math.max(2f, 10f - ring * 1.7f));
                        paint.setColor(withAlpha(ring == 1 ? GOLD : CRIMSON,
                                Math.round((215f - ring * 45f) * fade)));
                        canvas.drawCircle(effect.x, effect.y, ringRadius, paint);
                    }
                    for (int ray = 0; ray < 28; ray++) {
                        float angle = effect.seed + progress * 1.8f
                                + ray * (float) Math.PI * 2f / 28f;
                        float inner = radius * (0.58f + (ray % 3) * 0.07f);
                        float outer = inner + 34f + (ray % 4) * 13f;
                        paint.setStrokeWidth(ray % 4 == 0 ? 6f : 3f);
                        paint.setColor(withAlpha(ray % 5 == 0 ? Color.WHITE : CRIMSON,
                                Math.round((ray % 5 == 0 ? 220f : 145f) * fade)));
                        canvas.drawLine(effect.x + (float) Math.cos(angle) * inner,
                                effect.y + (float) Math.sin(angle) * inner * 0.62f,
                                effect.x + (float) Math.cos(angle) * outer,
                                effect.y + (float) Math.sin(angle) * outer * 0.62f, paint);
                    }
                    paint.setStrokeCap(Paint.Cap.BUTT);
                    paint.setStyle(Paint.Style.FILL);
                }
            } else if (effect.kind == FX_RUSH) {
                float dx = effect.targetX - effect.x;
                float coreFade = fade * (1f - progress * 0.35f);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                if (!foreground) {
                    for (int band = 0; band < 5; band++) {
                        float offset = (band - 2) * 12f;
                        paint.setStrokeWidth(22f - band * 3f);
                        paint.setColor(withAlpha(band == 0 ? Color.WHITE
                                        : band == 2 ? GOLD : CRIMSON,
                                Math.round((72f - band * 8f) * coreFade)));
                        canvas.drawLine(effect.x - effect.direction * 44f,
                                effect.y + offset,
                                effect.targetX + effect.direction * 34f,
                                effect.targetY - offset * 0.25f, paint);
                    }
                } else {
                    float impactRadius = 34f + easeOutCubic(progress) * 104f;
                    for (int slash = -4; slash <= 4; slash++) {
                        float offset = slash * 13f;
                        paint.setStrokeWidth(slash == 0 ? 8f : 3f);
                        paint.setColor(withAlpha(slash % 3 == 0 ? Color.WHITE : CRIMSON,
                                Math.round((slash == 0 ? 240f : 155f) * fade)));
                        canvas.drawLine(effect.targetX - effect.direction * impactRadius,
                                effect.targetY + offset - 58f,
                                effect.targetX + effect.direction * impactRadius * 0.7f,
                                effect.targetY - offset + 58f, paint);
                    }
                    paint.setStrokeWidth(6f * fade + 1f);
                    paint.setColor(withAlpha(GOLD, Math.round(210f * fade)));
                    canvas.drawCircle(effect.targetX, effect.targetY,
                            impactRadius * 0.62f, paint);
                }
                paint.setStrokeCap(Paint.Cap.BUTT);
                paint.setStyle(Paint.Style.FILL);
            } else if (effect.kind == FX_RAIN) {
                float strike = easeOutCubic(RpgRules.clamp(progress * 1.5f, 0f, 1f));
                if (!foreground) {
                    float glowRadius = 68f + strike * 88f;
                    paint.setShader(new RadialGradient(effect.x, effect.y, glowRadius,
                            withAlpha(GOLD, Math.round(105f * fade)),
                            withAlpha(CRIMSON, 0), Shader.TileMode.CLAMP));
                    canvas.drawCircle(effect.x, effect.y, glowRadius, paint);
                    paint.setShader(null);
                } else {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    for (int blade = 0; blade < 9; blade++) {
                        float delay = blade * 0.055f;
                        float bladeProgress = easeOutCubic(RpgRules.clamp(
                                (progress - delay) / Math.max(0.01f, 0.62f - delay), 0f, 1f));
                        float offset = (blade - 4) * 24f;
                        float startX = effect.x + offset - effect.direction * 96f;
                        float startY = effect.y - 290f - (blade % 3) * 28f;
                        float endX = effect.x + offset * 0.55f + effect.direction * 38f;
                        float endY = effect.y + 52f;
                        float bladeX = lerp(startX, endX, bladeProgress);
                        float bladeY = lerp(startY, endY, bladeProgress);
                        paint.setStrokeWidth(blade % 3 == 0 ? 7f : 3.5f);
                        paint.setColor(withAlpha(blade % 4 == 0 ? Color.WHITE
                                        : blade % 3 == 0 ? GOLD : CRIMSON,
                                Math.round((blade % 3 == 0 ? 235f : 175f) * fade)));
                        canvas.drawLine(bladeX - effect.direction * 72f, bladeY - 96f,
                                bladeX + effect.direction * 26f, bladeY + 42f, paint);
                    }
                    paint.setStrokeWidth(5f);
                    paint.setColor(withAlpha(CRIMSON, Math.round(190f * fade)));
                    effectBounds.set(effect.x - 108f, effect.y + 28f,
                            effect.x + 108f, effect.y + 68f);
                    canvas.drawOval(effectBounds, paint);
                    paint.setStrokeCap(Paint.Cap.BUTT);
                    paint.setStyle(Paint.Style.FILL);
                }
            } else if (effect.kind == FX_CHAIN) {
                float pulse = 0.72f + (float) Math.sin(progress * 34f + effect.seed) * 0.28f;
                float dx = effect.targetX - effect.x;
                float dy = effect.targetY - effect.y;
                if (!foreground) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    paint.setStrokeWidth(26f * fade);
                    paint.setColor(withAlpha(CYAN, Math.round(42f * fade)));
                    canvas.drawLine(effect.x, effect.y, effect.targetX, effect.targetY, paint);
                    paint.setStrokeCap(Paint.Cap.BUTT);
                    paint.setStyle(Paint.Style.FILL);
                } else {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    for (int strand = 0; strand < 4; strand++) {
                        effectPath.reset();
                        effectPath.moveTo(effect.x, effect.y);
                        for (int segment = 1; segment <= 9; segment++) {
                            float t = segment / 9f;
                            float normalX = -dy;
                            float normalY = dx;
                            float inverseLength = 1f / Math.max(1f,
                                    (float) Math.sqrt(normalX * normalX + normalY * normalY));
                            float jitter = (float) Math.sin(segment * 4.7f
                                    + strand * 2.3f + effect.seed * 5f) * (12f - strand * 2f);
                            effectPath.lineTo(effect.x + dx * t + normalX * inverseLength * jitter,
                                    effect.y + dy * t + normalY * inverseLength * jitter);
                        }
                        paint.setStrokeWidth(strand == 0 ? 7f * pulse : 2.5f);
                        paint.setColor(withAlpha(strand == 0 ? Color.WHITE
                                        : strand == 1 ? CYAN : CRIMSON,
                                Math.round((strand == 0 ? 235f : 165f) * fade)));
                        canvas.drawPath(effectPath, paint);
                    }
                    float nodeRadius = 16f + progress * 38f;
                    paint.setStrokeWidth(4f);
                    paint.setColor(withAlpha(GOLD, Math.round(205f * fade)));
                    canvas.drawCircle(effect.targetX, effect.targetY, nodeRadius, paint);
                    paint.setStrokeCap(Paint.Cap.BUTT);
                    paint.setStyle(Paint.Style.FILL);
                }
            } else if (effect.kind == FX_PILLAR) {
                float rise = easeOutCubic(RpgRules.clamp(progress * 1.8f, 0f, 1f));
                float width = 46f + rise * 86f;
                if (!foreground) {
                    paint.setShader(new RadialGradient(effect.x, GROUND_Y - 54f,
                            170f, withAlpha(Color.WHITE, Math.round(125f * fade)),
                            withAlpha(CRIMSON, 0), Shader.TileMode.CLAMP));
                    canvas.drawCircle(effect.x, GROUND_Y - 54f, 170f, paint);
                    paint.setShader(null);
                    paint.setColor(withAlpha(BLOOD, Math.round(145f * fade)));
                    effectBounds.set(effect.x - width, GROUND_Y - 28f,
                            effect.x + width, GROUND_Y + 34f);
                    canvas.drawOval(effectBounds, paint);
                } else {
                    for (int column = -2; column <= 2; column++) {
                        float columnX = effect.x + column * width * 0.34f;
                        float topY = GROUND_Y - 82f - rise * (330f + Math.abs(column) * 36f);
                        effectPath.reset();
                        effectPath.moveTo(columnX - width * 0.2f, GROUND_Y - 42f);
                        effectPath.lineTo(columnX - width * 0.06f, topY);
                        effectPath.lineTo(columnX + width * 0.18f, GROUND_Y - 42f);
                        effectPath.close();
                        paint.setColor(withAlpha(column == 0 ? Color.WHITE
                                        : column % 2 == 0 ? GOLD : CRIMSON,
                                Math.round((column == 0 ? 205f : 150f) * fade)));
                        canvas.drawPath(effectPath, paint);
                    }
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(5f * fade + 1f);
                    for (int ring = 0; ring < 3; ring++) {
                        float ringWidth = 62f + rise * (76f + ring * 34f);
                        effectBounds.set(effect.x - ringWidth, GROUND_Y - 66f - ring * 7f,
                                effect.x + ringWidth, GROUND_Y + 20f + ring * 7f);
                        paint.setColor(withAlpha(ring == 1 ? GOLD : CRIMSON,
                                Math.round((210f - ring * 48f) * fade)));
                        canvas.drawOval(effectBounds, paint);
                    }
                    paint.setStyle(Paint.Style.FILL);
                }
            } else if (effect.kind == FX_ECLIPSE) {
                float expansion = easeOutCubic(progress);
                float moonRadius = 76f + expansion * 142f;
                if (!foreground) {
                    paint.setShader(new RadialGradient(effect.x, effect.y,
                            Math.max(1f, effect.radius * expansion),
                            new int[]{withAlpha(Color.WHITE, Math.round(90f * fade)),
                                    withAlpha(Color.rgb(231, 32, 83), Math.round(150f * fade)),
                                    withAlpha(Color.rgb(83, 18, 112), Math.round(88f * fade)),
                                    Color.TRANSPARENT},
                            new float[]{0f, 0.12f, 0.48f, 1f}, Shader.TileMode.CLAMP));
                    canvas.drawCircle(effect.x, effect.y,
                            Math.max(1f, effect.radius * expansion), paint);
                    paint.setShader(null);
                } else {
                    paint.setColor(withAlpha(Color.rgb(4, 3, 11), Math.round(245f * fade)));
                    canvas.drawCircle(effect.x, effect.y, moonRadius * 0.72f, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    for (int ring = 0; ring < 6; ring++) {
                        paint.setStrokeWidth(Math.max(2f, 10f - ring * 1.35f));
                        paint.setColor(withAlpha(ring % 3 == 1 ? GOLD
                                        : ring % 2 == 0 ? Color.WHITE : effect.color,
                                Math.round((225f - ring * 27f) * fade)));
                        canvas.drawCircle(effect.x, effect.y,
                                moonRadius * (0.78f + ring * 0.13f), paint);
                    }
                    for (int ray = 0; ray < 36; ray++) {
                        float angle = effect.seed + progress * 2.6f
                                + ray * (float) Math.PI * 2f / 36f;
                        float inner = moonRadius * (1.02f + (ray % 3) * 0.08f);
                        float outer = inner + 58f + (ray % 5) * 18f;
                        paint.setStrokeWidth(ray % 6 == 0 ? 7f : 2.5f);
                        paint.setColor(withAlpha(ray % 6 == 0 ? Color.WHITE
                                        : ray % 4 == 0 ? GOLD : effect.color,
                                Math.round((ray % 6 == 0 ? 225f : 145f) * fade)));
                        canvas.drawLine(effect.x + (float) Math.cos(angle) * inner,
                                effect.y + (float) Math.sin(angle) * inner,
                                effect.x + (float) Math.cos(angle) * outer,
                                effect.y + (float) Math.sin(angle) * outer, paint);
                    }
                    float groundWidth = 190f + expansion * 410f;
                    effectBounds.set(effect.x - groundWidth, GROUND_Y - 76f,
                            effect.x + groundWidth, GROUND_Y + 52f);
                    paint.setStrokeWidth(8f * fade + 1f);
                    paint.setColor(withAlpha(CRIMSON, Math.round(220f * fade)));
                    canvas.drawOval(effectBounds, paint);
                    paint.setStrokeCap(Paint.Cap.BUTT);
                    paint.setStyle(Paint.Style.FILL);
                }
            } else if (foreground && effect.kind == FX_TETHER) {
                float dx = effect.targetX - effect.x;
                float direction = dx == 0f ? 1f : Math.signum(dx);
                for (int strand = 0; strand < 6; strand++) {
                    float wave = (float) Math.sin(progress * 26f + effect.seed + strand * 1.8f)
                            * (16f + strand * 4f);
                    float control1X = effect.x + dx * 0.32f;
                    float control2X = effect.x + dx * 0.7f;
                    float control1Y = effect.y - 45f - wave;
                    float control2Y = effect.targetY - 34f + wave;
                    effectPath.reset();
                    effectPath.moveTo(effect.x, effect.y);
                    effectPath.cubicTo(control1X, control1Y,
                            control2X, control2Y, effect.targetX, effect.targetY);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    paint.setStrokeWidth(strand == 0 ? 8f : 2.5f + strand * 0.55f);
                    paint.setColor(withAlpha(strand == 0 ? Color.WHITE
                                    : strand == 1 ? CYAN : CRIMSON,
                            Math.round((210f - strand * 28f) * fade)));
                    canvas.drawPath(effectPath, paint);
                    for (int drop = 0; drop < 3; drop++) {
                        float t = (progress * (1.5f + strand * 0.12f)
                                + drop * 0.29f + strand * 0.09f) % 1f;
                        float px = cubicBezier(effect.x, control1X, control2X, effect.targetX, t);
                        float py = cubicBezier(effect.y, control1Y, control2Y, effect.targetY, t);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(withAlpha(drop == 0 ? Color.WHITE : CRIMSON,
                                Math.round(220f * fade)));
                        canvas.drawCircle(px + direction * strand, py, 4.5f - drop * 0.7f, paint);
                    }
                }
                paint.setStrokeCap(Paint.Cap.BUTT);
                paint.setStyle(Paint.Style.FILL);
            } else if (foreground && effect.kind == FX_SPEAR_IMPACT) {
                float radius = effect.radius * (0.28f + smootherStep(progress) * 0.72f);
                paint.setShader(new RadialGradient(effect.x, effect.y, radius,
                        withAlpha(Color.WHITE, Math.round(210f * fade)),
                        withAlpha(effect.color, 0), Shader.TileMode.CLAMP));
                canvas.drawCircle(effect.x, effect.y, radius, paint);
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeWidth(7f * fade + 1f);
                paint.setColor(withAlpha(effect.color, Math.round(230f * fade)));
                canvas.drawCircle(effect.x, effect.y, radius * 0.76f, paint);
                for (int ray = -3; ray <= 3; ray++) {
                    float angle = ray * 0.22f;
                    float dx = effect.direction * (float) Math.cos(angle) * radius;
                    float dy = (float) Math.sin(angle) * radius * 0.7f;
                    canvas.drawLine(effect.x + dx * 0.22f, effect.y + dy * 0.22f,
                            effect.x + dx, effect.y + dy, paint);
                }
                paint.setStrokeCap(Paint.Cap.BUTT);
                paint.setStyle(Paint.Style.FILL);
            }
        }
    }

    private boolean drawBloodArtEffect(Canvas canvas, SkillEffect effect,
                                       boolean foreground, float progress, float fade) {
        if (bloodArtsAtlas == null || bloodArtsAtlas.isRecycled()) {
            return false;
        }
        int atlasIndex;
        float centerX = effect.x;
        float centerY = effect.y;
        float width;
        float height;
        float rotation = 0f;
        boolean flip = effect.direction < 0f;
        if (effect.kind == FX_SPEAR_IMPACT) {
            atlasIndex = 0;
            width = 270f;
            height = 196f;
            rotation = -9f * effect.direction;
        } else if (effect.kind == FX_TETHER) {
            atlasIndex = 1;
            centerX = (effect.x + effect.targetX) * 0.5f;
            centerY = (effect.y + effect.targetY) * 0.5f - 12f;
            width = Math.max(250f, Math.abs(effect.targetX - effect.x) + 210f);
            height = 238f;
            flip = effect.targetX < effect.x;
        } else if (effect.kind == FX_NOVA) {
            atlasIndex = 2;
            width = Math.max(350f, effect.radius * 1.95f);
            height = width;
        } else if (effect.kind == FX_RUSH) {
            atlasIndex = 3;
            centerX = (effect.x + effect.targetX) * 0.5f;
            centerY = (effect.y + effect.targetY) * 0.5f;
            width = Math.max(330f, Math.abs(effect.targetX - effect.x) + 250f);
            height = 260f;
            flip = effect.targetX < effect.x;
        } else if (effect.kind == FX_RAIN) {
            atlasIndex = 4;
            centerY = effect.y - 108f;
            width = 330f;
            height = 410f;
        } else if (effect.kind == FX_CHAIN) {
            atlasIndex = 5;
            centerX = (effect.x + effect.targetX) * 0.5f;
            centerY = (effect.y + effect.targetY) * 0.5f - 18f;
            width = Math.max(280f, Math.abs(effect.targetX - effect.x) + 230f);
            height = 245f;
            flip = effect.targetX < effect.x;
        } else if (effect.kind == FX_PILLAR) {
            atlasIndex = 6;
            centerY = GROUND_Y - 220f;
            width = 330f;
            height = 470f;
        } else if (effect.kind == FX_ECLIPSE) {
            atlasIndex = 7;
            width = 680f;
            height = 650f;
        } else {
            return false;
        }

        float appear = easeOutCubic(RpgRules.clamp(progress / 0.24f, 0f, 1f));
        float pulse = 1f + (float) Math.sin(progress * Math.PI) * 0.09f;
        float animatedScale = (0.56f + appear * 0.44f) * pulse;
        if (effect.kind == FX_RUSH || effect.kind == FX_SPEAR_IMPACT) {
            animatedScale = 0.78f + appear * 0.3f;
        } else if (effect.kind == FX_PILLAR || effect.kind == FX_RAIN) {
            animatedScale = 0.48f + appear * 0.58f;
        }
        if (!foreground) {
            drawBloodArtAtlasCell(canvas, atlasIndex, centerX, centerY,
                    width * animatedScale * 1.12f, height * animatedScale * 1.12f,
                    flip, Math.round(72f * fade), rotation);
        } else {
            drawBloodArtAtlasCell(canvas, atlasIndex, centerX, centerY,
                    width * animatedScale, height * animatedScale,
                    flip, Math.round(250f * fade), rotation);
        }
        return true;
    }

    private void drawBloodArtAtlasCell(Canvas canvas, int index, float centerX, float centerY,
                                       float width, float height, boolean flip,
                                       int alpha, float rotation) {
        if (bloodArtsAtlas == null || bloodArtsAtlas.isRecycled()) {
            return;
        }
        int safeIndex = RpgRules.clamp(index, 0, 7);
        int column = safeIndex % 4;
        int row = safeIndex / 4;
        int cellWidth = bloodArtsAtlas.getWidth() / 4;
        int cellHeight = bloodArtsAtlas.getHeight() / 2;
        int left = column * cellWidth;
        int top = row * cellHeight;
        int right = column == 3 ? bloodArtsAtlas.getWidth() : left + cellWidth;
        int bottom = row == 1 ? bloodArtsAtlas.getHeight() : top + cellHeight;
        atlasSource.set(left, top, right, bottom);
        spriteDestination.set(-width * 0.5f, -height * 0.5f,
                width * 0.5f, height * 0.5f);
        canvas.save();
        canvas.translate(centerX, centerY);
        canvas.rotate(rotation);
        canvas.scale(flip ? -1f : 1f, 1f);
        paint.setAlpha(RpgRules.clamp(alpha, 0, 255));
        canvas.drawBitmap(bloodArtsAtlas, atlasSource, spriteDestination, paint);
        paint.setAlpha(255);
        canvas.restore();
    }

    private void drawParticles(Canvas canvas) {
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (Particle particle : particles) {
            float fraction = RpgRules.clamp(particle.life / particle.maxLife, 0f, 1f);
            paint.setColor(withAlpha(particle.color, Math.round(255f * fraction)));
            if (particle.style == 1) {
                float speed = (float) Math.sqrt(particle.velocityX * particle.velocityX
                        + particle.velocityY * particle.velocityY);
                float inverse = speed <= 0.01f ? 0f : 1f / speed;
                float length = Math.min(34f, 7f + speed * 0.055f) * fraction;
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(1.2f, particle.radius * fraction));
                canvas.drawLine(particle.x, particle.y,
                        particle.x - particle.velocityX * inverse * length,
                        particle.y - particle.velocityY * inverse * length, paint);
                paint.setStyle(Paint.Style.FILL);
            } else {
                canvas.drawCircle(particle.x, particle.y,
                        particle.radius * (0.35f + fraction * 0.65f), paint);
            }
        }
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void drawFloatingTexts(Canvas canvas) {
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(24f);
        for (FloatingText floatingText : floatingTexts) {
            float fraction = RpgRules.clamp(floatingText.life / floatingText.maxLife, 0f, 1f);
            textPaint.setColor(withAlpha(floatingText.color, Math.round(255f * fraction)));
            drawTextWithShadow(canvas, floatingText.text, floatingText.x, floatingText.y, textPaint);
        }
    }

    private void drawHud(Canvas canvas) {
        paint.setColor(Color.argb(184, 5, 7, 15));
        canvas.drawRoundRect(new RectF(14f, 12f, LOGICAL_WIDTH - 14f, 218f),
                24f, 24f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(108, 229, 184, 84));
        canvas.drawRoundRect(new RectF(14f, 12f, LOGICAL_WIDTH - 14f, 218f),
                24f, 24f, paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(28f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Lv." + progress.level, 32f, 51f, textPaint);
        textPaint.setTextSize(18f);
        textPaint.setColor(Color.rgb(205, 211, 225));
        canvas.drawText(REGION_NAMES[progress.region] + "  ·  "
                + (progress.wave == 5 ? "보스" : "웨이브 " + progress.wave), 126f, 48f, textPaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(20f);
        textPaint.setColor(GOLD);
        canvas.drawText("◆ " + formatNumber(progress.gold), 614f, 48f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);

        drawBar(canvas, 32f, 68f, 584f, 30f,
                heroHealth / Math.max(1f, heroMaxHealth), BLOOD,
                "생명력  " + Math.round(heroHealth) + " / " + Math.round(heroMaxHealth));
        drawBar(canvas, 32f, 108f, 584f, 24f,
                heroBlood / Math.max(1f, heroMaxBlood), Color.rgb(74, 177, 211),
                "혈기  " + Math.round(heroBlood) + " / " + Math.round(heroMaxBlood));
        float xpFraction = progress.level >= RpgRules.LEVEL_CAP ? 1f
                : progress.xp / (float) RpgRules.xpForNextLevel(progress.level);
        String xpLabel = progress.level >= RpgRules.LEVEL_CAP ? "MAX LEVEL"
                : "경험치  " + progress.xp + " / " + RpgRules.xpForNextLevel(progress.level);
        drawBar(canvas, 32f, 142f, 584f, 20f, xpFraction, VIOLET, xpLabel);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(18f);
        textPaint.setColor(Color.rgb(224, 228, 238));
        String objective = progress.wave == 5 ? "지역 보스 처치"
                : "사냥 목표  " + defeatedThisWave + " / " + waveTarget;
        canvas.drawText("✦  " + objective, 34f, 197f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);

        drawCircleIcon(canvas, 669f, 48f, 29f, "Ⅱ", Color.rgb(50, 55, 72));
        paint.setColor(Color.argb(196, 10, 12, 24));
        canvas.drawRoundRect(new RectF(558f, 232f, 702f, 286f), 18f, 18f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(150, 229, 184, 84));
        canvas.drawRoundRect(new RectF(558f, 232f, 702f, 286f), 18f, 18f, paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(20f);
        textPaint.setColor(GOLD);
        canvas.drawText("성장  ▲", 630f, 266f, textPaint);

        paint.setColor(Color.argb(196, 10, 12, 24));
        canvas.drawRoundRect(inventoryHudButton, 18f, 18f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(150, 103, 218, 235));
        canvas.drawRoundRect(inventoryHudButton, 18f, 18f, paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(19f);
        textPaint.setColor(CYAN);
        canvas.drawText("가방  " + inventoryCount() + "/" + RpgProgress.INVENTORY_SIZE,
                inventoryHudButton.centerX(), 266f, textPaint);

        paint.setColor(Color.argb(165, 8, 10, 19));
        canvas.drawRoundRect(new RectF(18f, 232f, 375f, 286f), 18f, 18f, paint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(17f);
        textPaint.setColor(Color.rgb(210, 215, 226));
        canvas.drawText("전투력 " + combatPower() + "  ·  처치 " + progress.kills,
                38f, 265f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawControls(Canvas canvas) {
        paint.setShader(new LinearGradient(0f, CONTROL_TOP, 0f, LOGICAL_HEIGHT,
                Color.argb(80, 4, 5, 12), Color.argb(238, 4, 5, 12), Shader.TileMode.CLAMP));
        canvas.drawRect(0f, CONTROL_TOP, LOGICAL_WIDTH, LOGICAL_HEIGHT, paint);
        paint.setShader(null);

        paint.setColor(Color.argb(205, 9, 15, 27));
        RectF autoBattleBounds = new RectF(GameUiLayout.AUTO_BATTLE_LEFT,
                GameUiLayout.AUTO_BATTLE_TOP, GameUiLayout.AUTO_BATTLE_RIGHT,
                GameUiLayout.AUTO_BATTLE_BOTTOM);
        canvas.drawRoundRect(autoBattleBounds, 22f, 22f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(175, 93, 218, 235));
        canvas.drawRoundRect(autoBattleBounds, 22f, 22f, paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(20f);
        textPaint.setColor(CYAN);
        canvas.drawText("AUTO BATTLE  ●  자동 추격 · 기본 공격 · 혈술 연계", 360f,
                GameUiLayout.AUTO_BATTLE_TEXT_Y, textPaint);

        drawAutoSkillChip(canvas, 82f, GameUiLayout.AUTO_SKILL_CENTER_Y, ICON_RUSH, "쇄도",
                progress.level >= 3, rushCooldown, Color.rgb(222, 43, 83));
        drawAutoSkillChip(canvas, 221f, GameUiLayout.AUTO_SKILL_CENTER_Y, ICON_RAIN, "검우",
                progress.level >= 6, rainCooldown, GOLD);
        drawAutoSkillChip(canvas, 360f, GameUiLayout.AUTO_SKILL_CENTER_Y, ICON_CHAIN, "사슬",
                progress.level >= 9, chainCooldown, CYAN);
        drawAutoSkillChip(canvas, 499f, GameUiLayout.AUTO_SKILL_CENTER_Y, ICON_PILLAR, "기둥",
                progress.level >= 12, pillarCooldown, Color.rgb(255, 88, 75));
        drawAutoSkillChip(canvas, 638f, GameUiLayout.AUTO_SKILL_CENTER_Y, ICON_ECLIPSE, "월식",
                progress.level >= 15, eclipseCooldown, VIOLET);

        drawControlButton(canvas, 92f, GameUiLayout.MANUAL_SKILL_CENTER_Y, 57f,
                ICON_DASH, "개입 대시", false,
                0f, true, Color.rgb(70, 83, 112));
        drawControlButton(canvas, 292f, GameUiLayout.MANUAL_SKILL_CENTER_Y, 60f,
                ICON_SPEAR, "혈창 20",
                queuedSkillAction == ACTION_SPEAR,
                spearCooldown / 0.9f, true, Color.rgb(154, 24, 58));
        drawControlButton(canvas, 463f, GameUiLayout.MANUAL_SKILL_CENTER_Y, 60f,
                ICON_SIPHON, "흡혈 30",
                queuedSkillAction == ACTION_SIPHON,
                siphonCooldown / 4.8f, progress.level >= 4, Color.rgb(38, 119, 144));
        drawControlButton(canvas, 630f, GameUiLayout.MANUAL_SKILL_CENTER_Y, 60f,
                ICON_NOVA, "폭발 55",
                queuedSkillAction == ACTION_NOVA,
                novaCooldown / 7.5f, progress.level >= 7, Color.rgb(115, 53, 149));

        if (comboDisplayTimer > 0f && comboIndex > 0) {
            textPaint.setTypeface(uiBoldTypeface);
            textPaint.setTextSize(18f);
            textPaint.setColor(GOLD);
            canvas.drawText((comboIndex + 1) + " AUTO COMBO", 360f, 946f, textPaint);
        }
    }

    private void drawControlButton(Canvas canvas, float x, float y, float radius,
                                   int icon, String label, boolean pressed,
                                   float cooldownFraction, boolean enabled, int color) {
        float scale = pressed ? 0.93f : 1f;
        paint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawCircle(x + 3f, y + 8f, radius * scale, paint);
        paint.setShader(new RadialGradient(x - radius * 0.25f, y - radius * 0.28f,
                radius * 1.35f, withAlpha(color, enabled ? 225 : 90),
                withAlpha(darken(color, 0.42f), enabled ? 235 : 120), Shader.TileMode.CLAMP));
        canvas.drawCircle(x, y, radius * scale, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(pressed ? 5f : 3f);
        paint.setColor(enabled ? Color.argb(185, 245, 222, 185) : Color.argb(90, 180, 180, 190));
        canvas.drawCircle(x, y, radius * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        if (cooldownFraction > 0f) {
            paint.setColor(Color.argb(178, 3, 4, 9));
            canvas.drawArc(new RectF(x - radius, y - radius, x + radius, y + radius),
                    -90f, 360f * RpgRules.clamp(cooldownFraction, 0f, 1f), true, paint);
        }
        drawSkillIcon(canvas, icon, x, y, radius * 0.58f,
                enabled ? Color.WHITE : Color.rgb(115, 118, 129));
        textPaint.setTextSize(14f);
        textPaint.setColor(enabled ? Color.rgb(226, 229, 238) : Color.rgb(110, 112, 121));
        canvas.drawText(label, x, y + radius + GameUiLayout.MANUAL_SKILL_LABEL_OFFSET,
                textPaint);
    }

    private void drawAutoSkillChip(Canvas canvas, float x, float y, int icon,
                                   String label, boolean unlocked, float cooldown, int color) {
        RectF bounds = new RectF(x - 62f, y - GameUiLayout.AUTO_SKILL_HALF_HEIGHT,
                x + 62f, y + GameUiLayout.AUTO_SKILL_HALF_HEIGHT);
        paint.setColor(Color.argb(unlocked ? 205 : 145, 10, 14, 25));
        canvas.drawRoundRect(bounds, 14f, 14f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f);
        paint.setColor(withAlpha(unlocked ? color : Color.rgb(86, 91, 105), 165));
        canvas.drawRoundRect(bounds, 14f, 14f, paint);
        paint.setStyle(Paint.Style.FILL);
        drawSkillIcon(canvas, icon, x - 37f, y, 14f,
                unlocked ? color : Color.rgb(92, 97, 109));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(13f);
        textPaint.setColor(unlocked ? Color.rgb(225, 229, 238) : Color.rgb(113, 117, 128));
        String state = !unlocked ? "LOCK" : cooldown <= 0f ? "AUTO" : oneDecimal(cooldown);
        canvas.drawText(label, x - 15f, y - 2f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(9f);
        textPaint.setColor(unlocked ? color : Color.rgb(102, 106, 118));
        canvas.drawText(state, x - 15f, y + 13f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawSkillIcon(Canvas canvas, int icon, float x, float y,
                               float size, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(Math.max(2f, size * 0.13f));
        paint.setColor(color);
        if (icon == ICON_DASH) {
            for (int wing = -1; wing <= 1; wing++) {
                float offset = wing * size * 0.34f;
                canvas.drawLine(x - size * 0.72f, y + offset,
                        x - size * 0.12f, y, paint);
                canvas.drawLine(x - size * 0.12f, y,
                        x + size * 0.72f, y + offset, paint);
            }
        } else if (icon == ICON_SPEAR) {
            canvas.drawLine(x - size * 0.72f, y + size * 0.58f,
                    x + size * 0.64f, y - size * 0.58f, paint);
            effectPath.reset();
            effectPath.moveTo(x + size * 0.64f, y - size * 0.58f);
            effectPath.lineTo(x + size * 0.12f, y - size * 0.43f);
            effectPath.lineTo(x + size * 0.48f, y - size * 0.06f);
            effectPath.close();
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(effectPath, paint);
        } else if (icon == ICON_SIPHON) {
            effectPath.reset();
            effectPath.moveTo(x, y - size * 0.76f);
            effectPath.cubicTo(x - size * 0.62f, y - size * 0.12f,
                    x - size * 0.48f, y + size * 0.7f, x, y + size * 0.72f);
            effectPath.cubicTo(x + size * 0.48f, y + size * 0.7f,
                    x + size * 0.62f, y - size * 0.12f, x, y - size * 0.76f);
            canvas.drawPath(effectPath, paint);
            canvas.drawLine(x - size * 0.62f, y - size * 0.58f,
                    x - size * 0.27f, y - size * 0.18f, paint);
            canvas.drawLine(x + size * 0.62f, y - size * 0.58f,
                    x + size * 0.27f, y - size * 0.18f, paint);
        } else if (icon == ICON_NOVA || icon == ICON_ECLIPSE) {
            canvas.drawCircle(x, y, size * 0.48f, paint);
            int rays = icon == ICON_ECLIPSE ? 10 : 8;
            for (int ray = 0; ray < rays; ray++) {
                float angle = ray * (float) Math.PI * 2f / rays;
                canvas.drawLine(x + (float) Math.cos(angle) * size * 0.66f,
                        y + (float) Math.sin(angle) * size * 0.66f,
                        x + (float) Math.cos(angle) * size * 0.88f,
                        y + (float) Math.sin(angle) * size * 0.88f, paint);
            }
            if (icon == ICON_ECLIPSE) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(darken(color, 0.2f));
                canvas.drawCircle(x + size * 0.18f, y - size * 0.08f, size * 0.31f, paint);
            }
        } else if (icon == ICON_RUSH) {
            for (int slash = -1; slash <= 1; slash++) {
                float offset = slash * size * 0.34f;
                canvas.drawLine(x - size * 0.68f, y + size * 0.52f + offset,
                        x + size * 0.68f, y - size * 0.52f + offset, paint);
            }
        } else if (icon == ICON_RAIN) {
            for (int blade = -1; blade <= 1; blade++) {
                float offset = blade * size * 0.48f;
                canvas.drawLine(x + offset - size * 0.28f, y - size * 0.7f,
                        x + offset + size * 0.18f, y + size * 0.58f, paint);
                canvas.drawLine(x + offset + size * 0.18f, y + size * 0.58f,
                        x + offset - size * 0.08f, y + size * 0.37f, paint);
            }
        } else if (icon == ICON_CHAIN) {
            effectBounds.set(x - size * 0.75f, y - size * 0.42f,
                    x + size * 0.05f, y + size * 0.42f);
            canvas.drawOval(effectBounds, paint);
            effectBounds.set(x - size * 0.05f, y - size * 0.42f,
                    x + size * 0.75f, y + size * 0.42f);
            canvas.drawOval(effectBounds, paint);
        } else if (icon == ICON_PILLAR) {
            for (int flame = -1; flame <= 1; flame++) {
                float offset = flame * size * 0.42f;
                effectPath.reset();
                effectPath.moveTo(x + offset - size * 0.22f, y + size * 0.72f);
                effectPath.lineTo(x + offset, y - size * (flame == 0 ? 0.82f : 0.5f));
                effectPath.lineTo(x + offset + size * 0.22f, y + size * 0.72f);
                canvas.drawPath(effectPath, paint);
            }
        }
        paint.setStrokeJoin(Paint.Join.MITER);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawBar(Canvas canvas, float x, float y, float width, float height,
                         float fraction, int color, String label) {
        paint.setColor(Color.argb(190, 18, 20, 30));
        canvas.drawRoundRect(new RectF(x, y, x + width, y + height),
                height * 0.5f, height * 0.5f, paint);
        float safe = RpgRules.clamp(fraction, 0f, 1f);
        if (safe > 0f) {
            paint.setShader(new LinearGradient(x, y, x + width, y,
                    darken(color, 0.62f), color, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(x + 2f, y + 2f,
                            x + 2f + (width - 4f) * safe, y + height - 2f),
                    Math.max(1f, height * 0.42f), Math.max(1f, height * 0.42f), paint);
            paint.setShader(null);
        }
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(Math.min(16f, height * 0.58f));
        textPaint.setColor(Color.WHITE);
        canvas.drawText(label, x + width * 0.5f, y + height * 0.72f, textPaint);
    }

    private void drawMiniHealthBar(Canvas canvas, float x, float y, float width,
                                   float fraction, int color) {
        paint.setColor(Color.argb(205, 5, 6, 11));
        canvas.drawRoundRect(new RectF(x, y, x + width, y + 9f), 5f, 5f, paint);
        paint.setColor(color);
        canvas.drawRoundRect(new RectF(x + 1f, y + 1f,
                x + 1f + (width - 2f) * RpgRules.clamp(fraction, 0f, 1f), y + 8f),
                4f, 4f, paint);
    }

    private void drawBanners(Canvas canvas) {
        if (waveBannerTimer > 0f) {
            float alpha = Math.min(1f, waveBannerTimer / 0.45f);
            paint.setColor(Color.argb(Math.round(165f * alpha), 3, 4, 10));
            canvas.drawRoundRect(new RectF(92f, 324f, 628f, 430f), 18f, 18f, paint);
            textPaint.setTypeface(titleTypeface);
            textPaint.setTextSize(34f);
            textPaint.setColor(withAlpha(progress.wave == 5 ? GOLD : Color.WHITE,
                    Math.round(255f * alpha)));
            String primary = progress.wave == 5 ? "BOSS WAVE" : "WAVE " + progress.wave;
            drawTextWithShadow(canvas, primary, 360f, 369f, textPaint);
            textPaint.setTypeface(uiTypeface);
            textPaint.setTextSize(17f);
            textPaint.setColor(withAlpha(Color.rgb(207, 211, 224), Math.round(255f * alpha)));
            canvas.drawText(REGION_NAMES[progress.region] + " · "
                    + REGION_SUBTITLES[progress.region], 360f, 402f, textPaint);
        }
        if (levelBannerTimer > 0f) {
            float alpha = Math.min(1f, levelBannerTimer / 0.4f);
            paint.setShader(new LinearGradient(110f, 0f, 610f, 0f,
                    new int[]{Color.TRANSPARENT, withAlpha(GOLD, Math.round(150f * alpha)),
                            Color.TRANSPARENT},
                    new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRect(70f, 455f, 650f, 525f, paint);
            paint.setShader(null);
            textPaint.setTypeface(titleTypeface);
            textPaint.setTextSize(34f);
            textPaint.setColor(withAlpha(Color.WHITE, Math.round(255f * alpha)));
            drawTextWithShadow(canvas, levelBannerText, 360f, 500f, textPaint);
        }
        if (waveComplete && screen == Screen.PLAYING) {
            textPaint.setTypeface(titleTypeface);
            textPaint.setTextSize(30f);
            textPaint.setColor(GOLD);
            drawTextWithShadow(canvas, "AREA CLEAR", 360f, 584f, textPaint);
        }
        if (chapterBannerTimer > 0f && progress.region == 0 && progress.wave == 1
                && progress.chapterClears > 0) {
            textPaint.setTypeface(uiBoldTypeface);
            textPaint.setTextSize(19f);
            textPaint.setColor(GOLD);
            canvas.drawText("더 강해진 밤이 다시 시작됩니다", 360f, 548f, textPaint);
        }
    }

    private void drawToast(Canvas canvas) {
        if (toastTimer <= 0f || toastText.isEmpty()) {
            return;
        }
        float alpha = Math.min(1f, toastTimer / 0.3f);
        paint.setColor(Color.argb(Math.round(220f * alpha), 8, 10, 19));
        canvas.drawRoundRect(new RectF(72f, 878f, 648f, 928f), 20f, 20f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(withAlpha(GOLD, Math.round(125f * alpha)));
        canvas.drawRoundRect(new RectF(72f, 878f, 648f, 928f), 20f, 20f, paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(18f);
        textPaint.setColor(withAlpha(Color.WHITE, Math.round(255f * alpha)));
        canvas.drawText(toastText, 360f, 910f, textPaint);
    }

    private void drawTitle(Canvas canvas) {
        textPaint.setTypeface(titleTypeface);
        textPaint.setTextSize(66f);
        textPaint.setLetterSpacing(0.08f);
        textPaint.setColor(Color.rgb(237, 225, 216));
        drawTextWithShadow(canvas, "VAYLORN", 360f, 378f, textPaint);
        textPaint.setLetterSpacing(0f);
        textPaint.setTextSize(34f);
        textPaint.setColor(CRIMSON);
        drawTextWithShadow(canvas, "IDLE BLOOD RPG", 360f, 430f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(19f);
        textPaint.setColor(Color.rgb(209, 215, 228));
        canvas.drawText("세로형 방치 전투 · 횡스크롤 성장 RPG", 360f, 474f, textPaint);

        paint.setColor(Color.argb(175, 7, 9, 18));
        canvas.drawRoundRect(new RectF(96f, 526f, 624f, 685f), 24f, 24f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(100, 226, 185, 91));
        canvas.drawRoundRect(new RectF(96f, 526f, 624f, 685f), 24f, 24f, paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(21f);
        textPaint.setColor(GOLD);
        canvas.drawText("자동 사냥  ·  오프라인 성장  ·  장비  ·  스토리", 360f, 572f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(17f);
        textPaint.setColor(Color.rgb(194, 200, 214));
        canvas.drawText("카엘은 적에게 달려들며 기본 공격과 혈술을 연계합니다", 360f, 614f, textPaint);
        canvas.drawText("원할 때 버튼을 눌러 자동 전투에 직접 개입하세요", 360f, 649f, textPaint);

        if (continueAvailable) {
            drawMenuButton(canvas, continueButton,
                    "이어하기  ·  Lv." + progress.level + "  " + REGION_NAMES[progress.region], true);
        } else {
            drawMenuButton(canvas, continueButton, "첫 모험 시작", true);
        }
        drawMenuButton(canvas, newGameButton, continueAvailable ? "새 모험" : "새 게임", false);

        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(15f);
        textPaint.setColor(Color.rgb(143, 150, 167));
        canvas.drawText("v4.12.0 DEMO  ·  PORTRAIT RPG", 360f, 1120f, textPaint);
    }

    private void drawOfflineReward(Canvas canvas) {
        drawOverlay(canvas, 210);
        RectF panel = new RectF(54f, 260f, 666f, 1040f);
        paint.setColor(Color.argb(248, 7, 9, 19));
        canvas.drawRoundRect(panel, 32f, 32f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.argb(190, 211, 39, 75));
        canvas.drawRoundRect(panel, 32f, 32f, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setShader(new RadialGradient(360f, 350f, 190f,
                Color.argb(90, 205, 30, 67), Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
        canvas.drawCircle(360f, 350f, 190f, paint);
        paint.setShader(null);

        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(17f);
        textPaint.setColor(CRIMSON);
        canvas.drawText("OFFLINE HUNT REPORT", 360f, 322f, textPaint);
        textPaint.setTypeface(titleTypeface);
        textPaint.setTextSize(39f);
        textPaint.setColor(Color.WHITE);
        drawTextWithShadow(canvas, "밤에도 사냥은 계속됐다", 360f, 383f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(19f);
        textPaint.setColor(Color.rgb(190, 201, 220));
        canvas.drawText("자리를 비운 동안 카엘이 모은 전리품입니다", 360f, 427f, textPaint);

        paint.setColor(Color.argb(185, 16, 20, 34));
        canvas.drawRoundRect(new RectF(100f, 474f, 620f, 548f), 20f, 20f, paint);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(20f);
        textPaint.setColor(CYAN);
        canvas.drawText("사냥 시간  ·  " + formatOfflineDuration(offlineElapsedSeconds),
                360f, 521f, textPaint);

        RectF goldCard = new RectF(100f, 580f, 350f, 754f);
        RectF xpCard = new RectF(370f, 580f, 620f, 754f);
        drawOfflineRewardCard(canvas, goldCard, "◆", "골드",
                "+" + formatNumber(offlineGoldReward), GOLD);
        drawOfflineRewardCard(canvas, xpCard, "✦", "경험치",
                "+" + formatNumber(offlineXpReward), CYAN);

        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(20f);
        textPaint.setColor(offlineLevelsGained > 0 ? GOLD : Color.rgb(183, 191, 207));
        canvas.drawText(offlineLevelsGained > 0
                        ? "레벨 " + offlineLevelsGained + " 상승  ·  현재 Lv." + progress.level
                        : "현재 Lv." + progress.level + "  ·  다음 성장에 반영 완료",
                360f, 813f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(15f);
        textPaint.setColor(Color.rgb(137, 147, 167));
        canvas.drawText("보상은 최대 8시간까지 누적됩니다", 360f, 854f, textPaint);
        drawMenuButton(canvas, offlineClaimButton, "보상 받고 모험 계속", true);
    }

    private void drawOfflineRewardCard(Canvas canvas, RectF bounds, String icon,
                                       String label, String value, int color) {
        paint.setColor(Color.argb(205, 13, 17, 29));
        canvas.drawRoundRect(bounds, 22f, 22f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(withAlpha(color, 155));
        canvas.drawRoundRect(bounds, 22f, 22f, paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(28f);
        textPaint.setColor(color);
        canvas.drawText(icon, bounds.centerX(), bounds.top + 49f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(16f);
        textPaint.setColor(Color.rgb(176, 185, 202));
        canvas.drawText(label, bounds.centerX(), bounds.top + 88f, textPaint);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(25f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(value, bounds.centerX(), bounds.top + 132f, textPaint);
    }

    private void drawStory(Canvas canvas) {
        paint.setColor(Color.argb(205, 4, 6, 14));
        canvas.drawRoundRect(new RectF(34f, 58f, 686f, 176f), 24f, 24f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(155, 105, 221, 240));
        canvas.drawRoundRect(new RectF(34f, 58f, 686f, 176f), 24f, 24f, paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(18f);
        textPaint.setColor(CYAN);
        canvas.drawText(STORY_CHAPTERS[storyChapter], 360f, 103f, textPaint);
        textPaint.setTypeface(titleTypeface);
        textPaint.setTextSize(32f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("카엘 아르덴의 기억", 360f, 148f, textPaint);

        paint.setColor(Color.argb(239, 5, 7, 16));
        canvas.drawRoundRect(new RectF(34f, 828f, 686f, 1186f), 30f, 30f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.argb(175, 197, 31, 70));
        canvas.drawRoundRect(new RectF(34f, 828f, 686f, 1186f), 30f, 30f, paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(24f);
        textPaint.setColor(storyFinale ? GOLD : CRIMSON);
        canvas.drawText(STORY_SPEAKERS[storyChapter][storyLine], 76f, 892f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(25f);
        textPaint.setColor(Color.rgb(231, 234, 242));
        drawWrappedText(canvas, STORY_LINES[storyChapter][storyLine],
                76f, 950f, 568f, 43f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(16f);
        textPaint.setColor(Color.rgb(151, 189, 203));
        canvas.drawText("화면을 눌러 계속  ·  " + (storyLine + 1) + " / "
                + STORY_LINES[storyChapter].length, 360f, 1148f, textPaint);
    }

    private void drawWrappedText(Canvas canvas, String text, float left, float top,
                                 float width, float lineHeight) {
        int start = 0;
        float y = top;
        while (start < text.length() && y < 1115f) {
            int count = textPaint.breakText(text, start, text.length(), true, width, null);
            if (count <= 0) {
                break;
            }
            int end = Math.min(text.length(), start + count);
            if (end < text.length()) {
                int space = text.lastIndexOf(' ', end - 1);
                if (space > start) {
                    end = space;
                }
            }
            canvas.drawText(text.substring(start, end).trim(), left, y, textPaint);
            start = end;
            while (start < text.length() && text.charAt(start) == ' ') {
                start++;
            }
            y += lineHeight;
        }
    }

    private void drawPause(Canvas canvas) {
        drawOverlay(canvas, 190);
        paint.setColor(Color.argb(242, 9, 11, 22));
        canvas.drawRoundRect(new RectF(108f, 346f, 612f, 878f), 30f, 30f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(145, 232, 188, 90));
        canvas.drawRoundRect(new RectF(108f, 346f, 612f, 878f), 30f, 30f, paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(titleTypeface);
        textPaint.setTextSize(42f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("모험 일시정지", 360f, 425f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(18f);
        textPaint.setColor(Color.rgb(188, 195, 210));
        canvas.drawText("현재 진행도는 안전하게 저장되었습니다", 360f, 468f, textPaint);
        drawMenuButton(canvas, pauseResumeButton, "계속 사냥", true);
        drawMenuButton(canvas, pauseGrowthButton, "성장과 장비", false);
        drawMenuButton(canvas, pauseTitleButton, "타이틀로", false);
    }

    private void drawGrowth(Canvas canvas) {
        drawOverlay(canvas, 215);
        paint.setColor(Color.argb(249, 8, 10, 20));
        canvas.drawRoundRect(new RectF(24f, 58f, 696f, 1194f), 30f, 30f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(160, 232, 188, 90));
        canvas.drawRoundRect(new RectF(24f, 58f, 696f, 1194f), 30f, 30f, paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTypeface(titleTypeface);
        textPaint.setTextSize(40f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("혈족의 성장", 360f, 115f, textPaint);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(20f);
        textPaint.setColor(GOLD);
        canvas.drawText("◆ " + formatNumber(progress.gold) + "    전투력 " + combatPower(),
                360f, 154f, textPaint);

        paint.setColor(Color.argb(118, 255, 255, 255));
        canvas.drawRect(54f, 186f, 666f, 188f, paint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(19f);
        textPaint.setColor(Color.rgb(215, 220, 232));
        canvas.drawText("영구 능력", 52f, 232f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);

        drawStatUpgrade(canvas, 0, "생명", "최대 생명력 +24", progress.vitalityLevel, "♥");
        drawStatUpgrade(canvas, 1, "완력", "공격력 +5", progress.mightLevel, "⚔");
        drawStatUpgrade(canvas, 2, "혈기", "최대 혈기 +15", progress.bloodLevel, "滴");
        drawStatUpgrade(canvas, 3, "회복", "초당 회복 +0.22", progress.recoveryLevel, "+");

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(19f);
        textPaint.setColor(Color.rgb(215, 220, 232));
        canvas.drawText("혈술 숙련", 52f, 570f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        drawSkillUpgrade(canvas, 4, "혈창", "관통 투사체 · 피해 증가",
                progress.spearLevel, 1, "Lv.1부터");
        drawSkillUpgrade(canvas, 5, "흡혈", "피해를 주고 생명력 회복",
                progress.siphonLevel, 4, "Lv.4 해금");
        drawSkillUpgrade(canvas, 6, "혈월 폭발", "주변 모든 적에게 큰 피해",
                progress.novaLevel, 7, "Lv.7 해금");

        paint.setColor(Color.argb(165, 15, 18, 31));
        canvas.drawRoundRect(new RectF(50f, 934f, 670f, 1068f), 20f, 20f, paint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(19f);
        textPaint.setColor(GOLD);
        canvas.drawText("장착 장비", 72f, 970f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(17f);
        textPaint.setColor(Color.rgb(210, 215, 228));
        canvas.drawText("무기  +" + progress.weaponPower + " 공격", 72f, 1008f, textPaint);
        canvas.drawText("갑옷  +" + progress.armorPower + " 방어", 270f, 1008f, textPaint);
        canvas.drawText("혈석  +" + progress.relicPower + " 혈기", 468f, 1008f, textPaint);
        textPaint.setTextSize(14f);
        textPaint.setColor(Color.rgb(142, 151, 171));
        canvas.drawText("자동 혈술: 쇄도 · 검우 · 사슬 · 기둥 · 개기월식", 72f, 1045f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        drawMenuButton(canvas, growthCloseButton,
                growthReturnScreen == Screen.PAUSED ? "일시정지로" : "전투로 복귀", true);
    }

    private void drawInventory(Canvas canvas) {
        drawOverlay(canvas, 220);
        paint.setColor(Color.argb(250, 7, 10, 20));
        canvas.drawRoundRect(new RectF(24f, 58f, 696f, 1170f), 30f, 30f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(175, 91, 213, 232));
        canvas.drawRoundRect(new RectF(24f, 58f, 696f, 1170f), 30f, 30f, paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTypeface(titleTypeface);
        textPaint.setTextSize(42f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("혈월 가방", 360f, 116f, textPaint);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(18f);
        textPaint.setColor(GOLD);
        canvas.drawText("◆ " + formatNumber(progress.gold) + "   ·   전투력 " + combatPower(),
                360f, 154f, textPaint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(19f);
        textPaint.setColor(Color.rgb(215, 222, 234));
        canvas.drawText("장착 장비", 52f, 207f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        drawEquippedItem(canvas, 0, new RectF(52f, 228f, 232f, 420f), progress.weaponPower);
        drawEquippedItem(canvas, 1, new RectF(270f, 228f, 450f, 420f), progress.armorPower);
        drawEquippedItem(canvas, 2, new RectF(488f, 228f, 668f, 420f), progress.relicPower);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(19f);
        textPaint.setColor(Color.rgb(215, 222, 234));
        canvas.drawText("전리품  " + inventoryCount() + "/" + RpgProgress.INVENTORY_SIZE,
                52f, 474f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        for (int index = 0; index < RpgProgress.INVENTORY_SIZE; index++) {
            drawInventoryItem(canvas, index, inventorySlotBounds(index),
                    progress.inventory[index], index == selectedInventoryItem);
        }

        paint.setColor(Color.argb(185, 13, 18, 31));
        canvas.drawRoundRect(new RectF(52f, 956f, 668f, 1028f), 18f, 18f, paint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(17f);
        if (selectedInventoryItem >= 0
                && progress.inventory[selectedInventoryItem] != 0) {
            int item = progress.inventory[selectedInventoryItem];
            int slot = itemSlot(item);
            int power = itemPower(item);
            int difference = power - equippedPower(slot);
            textPaint.setColor(rarityColor(itemRarity(item)));
            canvas.drawText(itemName(slot, itemRarity(item)), 72f, 986f, textPaint);
            textPaint.setTypeface(uiTypeface);
            textPaint.setTextSize(15f);
            textPaint.setColor(difference > 0 ? CYAN : Color.rgb(184, 192, 208));
            canvas.drawText(itemEffect(slot, power) + "   ·   현재 장비 대비 "
                    + (difference >= 0 ? "+" : "") + difference, 72f, 1012f, textPaint);
        } else {
            textPaint.setColor(Color.rgb(168, 177, 195));
            canvas.drawText("전리품을 선택하면 능력치 비교와 관리가 표시됩니다", 72f, 998f, textPaint);
        }
        textPaint.setTextAlign(Paint.Align.CENTER);

        String actionLabel = "아이템 선택";
        boolean actionEnabled = selectedInventoryItem >= 0
                && progress.inventory[selectedInventoryItem] != 0;
        if (actionEnabled) {
            int item = progress.inventory[selectedInventoryItem];
            int power = itemPower(item);
            actionLabel = power > equippedPower(itemSlot(item)) ? "선택 장비 착용"
                    : "분해  ·  +" + salvageValue(item) + " 골드";
        }
        drawMenuButton(canvas, inventoryActionButton, actionLabel, actionEnabled);
        drawMenuButton(canvas, inventoryCloseButton,
                inventoryReturnScreen == Screen.PAUSED ? "일시정지로" : "전투로", true);
    }

    private void drawEquippedItem(Canvas canvas, int slot, RectF bounds, int power) {
        int rarity = rarityFromPower(power);
        paint.setColor(Color.argb(205, 13, 17, 29));
        canvas.drawRoundRect(bounds, 20f, 20f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(withAlpha(rarityColor(rarity), 190));
        canvas.drawRoundRect(bounds, 20f, 20f, paint);
        paint.setStyle(Paint.Style.FILL);
        drawItemSymbol(canvas, slot, bounds.centerX(), bounds.top + 55f,
                30f, rarityColor(rarity));
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(16f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(slotName(slot), bounds.centerX(), bounds.top + 105f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(14f);
        textPaint.setColor(rarityColor(rarity));
        canvas.drawText(power > 0 ? itemName(slot, rarity) : "빈 슬롯",
                bounds.centerX(), bounds.top + 134f, textPaint);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(16f);
        textPaint.setColor(Color.rgb(215, 222, 235));
        canvas.drawText(power > 0 ? itemEffect(slot, power) : "효과 없음",
                bounds.centerX(), bounds.top + 166f, textPaint);
    }

    private void drawInventoryItem(Canvas canvas, int index, RectF bounds,
                                   int item, boolean selected) {
        int rarity = item == 0 ? 0 : itemRarity(item);
        paint.setColor(selected ? Color.argb(235, 24, 33, 52) : Color.argb(190, 12, 16, 28));
        canvas.drawRoundRect(bounds, 18f, 18f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(selected ? 4f : 2f);
        paint.setColor(item == 0 ? Color.argb(80, 125, 135, 154)
                : withAlpha(rarityColor(rarity), selected ? 240 : 150));
        canvas.drawRoundRect(bounds, 18f, 18f, paint);
        paint.setStyle(Paint.Style.FILL);
        if (item == 0) {
            textPaint.setTypeface(uiTypeface);
            textPaint.setTextSize(16f);
            textPaint.setColor(Color.rgb(103, 112, 131));
            canvas.drawText("빈 칸", bounds.centerX(), bounds.centerY() + 6f, textPaint);
            return;
        }
        int slot = itemSlot(item);
        drawItemSymbol(canvas, slot, bounds.left + 42f, bounds.centerY(),
                23f, rarityColor(rarity));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(16f);
        textPaint.setColor(rarityColor(rarity));
        canvas.drawText(itemName(slot, rarity), bounds.left + 82f, bounds.top + 42f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(14f);
        textPaint.setColor(Color.rgb(205, 213, 226));
        canvas.drawText(itemEffect(slot, itemPower(item)), bounds.left + 82f,
                bounds.top + 72f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawItemSymbol(Canvas canvas, int slot, float x, float y,
                                float radius, int color) {
        paint.setShader(new RadialGradient(x - radius * 0.25f, y - radius * 0.3f,
                radius * 1.35f, withAlpha(color, 235), darken(color, 0.35f),
                Shader.TileMode.CLAMP));
        canvas.drawCircle(x, y, radius, paint);
        paint.setShader(null);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(radius * 0.92f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(slot == 0 ? "⚔" : slot == 1 ? "◇" : "◆", x, y + radius * 0.32f,
                textPaint);
    }

    private void drawStatUpgrade(Canvas canvas, int index, String title, String effect,
                                 int level, String icon) {
        RectF bounds = upgradeButtons[index];
        int cost = RpgRules.statUpgradeCost(level);
        boolean max = level >= RpgRules.UPGRADE_CAP;
        drawUpgradeBackground(canvas, bounds, progress.gold >= cost && !max);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(25f);
        textPaint.setColor(CRIMSON);
        canvas.drawText(icon, bounds.left + 20f, bounds.top + 40f, textPaint);
        textPaint.setTextSize(19f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(title + "  Lv." + level, bounds.left + 60f, bounds.top + 34f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(14f);
        textPaint.setColor(Color.rgb(171, 180, 198));
        canvas.drawText(effect, bounds.left + 60f, bounds.top + 59f, textPaint);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(15f);
        textPaint.setColor(max ? CYAN : GOLD);
        canvas.drawText(max ? "MAX" : "◆ " + cost, bounds.left + 60f, bounds.top + 84f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawSkillUpgrade(Canvas canvas, int index, String title, String effect,
                                  int level, int requiredLevel, String unlockText) {
        RectF bounds = upgradeButtons[index];
        boolean locked = progress.level < requiredLevel || level <= 0 && requiredLevel > 1;
        boolean max = level >= 12;
        int cost = RpgRules.skillUpgradeCost(Math.max(0, level));
        drawUpgradeBackground(canvas, bounds, !locked && !max && progress.gold >= cost);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(19f);
        textPaint.setColor(locked ? Color.rgb(110, 116, 131) : Color.WHITE);
        canvas.drawText(title + (locked ? "  ·  잠김" : "  Lv." + level),
                bounds.left + 22f, bounds.top + 31f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(14f);
        textPaint.setColor(locked ? Color.rgb(90, 96, 110) : Color.rgb(173, 181, 199));
        canvas.drawText(effect, bounds.left + 22f, bounds.top + 59f, textPaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(16f);
        textPaint.setColor(locked ? VIOLET : max ? CYAN : GOLD);
        canvas.drawText(locked ? unlockText : max ? "MAX" : "강화  ◆ " + cost,
                bounds.right - 22f, bounds.centerY() + 6f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawUpgradeBackground(Canvas canvas, RectF bounds, boolean affordable) {
        paint.setColor(Color.argb(190, affordable ? 30 : 17,
                affordable ? 25 : 19, affordable ? 38 : 30));
        canvas.drawRoundRect(bounds, 18f, 18f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(affordable ? Color.argb(145, 224, 180, 84)
                : Color.argb(75, 137, 143, 160));
        canvas.drawRoundRect(bounds, 18f, 18f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawDefeat(Canvas canvas) {
        drawOverlay(canvas, 205);
        paint.setColor(Color.argb(244, 12, 8, 18));
        canvas.drawRoundRect(new RectF(86f, 350f, 634f, 944f), 32f, 32f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(160, 205, 30, 67));
        canvas.drawRoundRect(new RectF(86f, 350f, 634f, 944f), 32f, 32f, paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(titleTypeface);
        textPaint.setTextSize(44f);
        textPaint.setColor(CRIMSON);
        drawTextWithShadow(canvas, "핏빛 안개", 360f, 470f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(19f);
        textPaint.setColor(Color.rgb(209, 214, 227));
        canvas.drawText("육신은 쓰러졌지만 성장은 사라지지 않습니다", 360f, 520f, textPaint);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(18f);
        textPaint.setColor(GOLD);
        canvas.drawText("Lv." + progress.level + "  ·  " + REGION_NAMES[progress.region]
                + "  " + progress.wave + "웨이브", 360f, 574f, textPaint);
        drawMenuButton(canvas, retryButton, "현재 웨이브 재도전", true);
        drawMenuButton(canvas, defeatTitleButton, "타이틀로", false);
    }

    private void drawNewConfirm(Canvas canvas) {
        drawOverlay(canvas, 218);
        paint.setColor(Color.argb(250, 10, 11, 22));
        canvas.drawRoundRect(new RectF(54f, 454f, 666f, 854f), 30f, 30f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(160, 211, 39, 75));
        canvas.drawRoundRect(new RectF(54f, 454f, 666f, 854f), 30f, 30f, paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(titleTypeface);
        textPaint.setTextSize(38f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("새 모험을 시작할까요?", 360f, 540f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(18f);
        textPaint.setColor(Color.rgb(202, 208, 222));
        canvas.drawText("현재 레벨, 장비, 골드와 지역 진행이 초기화됩니다", 360f, 594f, textPaint);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(18f);
        textPaint.setColor(CRIMSON);
        canvas.drawText("이 작업은 되돌릴 수 없습니다", 360f, 638f, textPaint);
        drawMenuButton(canvas, confirmNewButton, "초기화", false);
        drawMenuButton(canvas, cancelNewButton, "취소", true);
    }

    private void drawOverlay(Canvas canvas, int alpha) {
        paint.setColor(Color.argb(alpha, 2, 3, 8));
        canvas.drawRect(0f, 0f, LOGICAL_WIDTH, LOGICAL_HEIGHT, paint);
    }

    private void drawMenuButton(Canvas canvas, RectF bounds, String label, boolean primary) {
        int color = primary ? CRIMSON : Color.rgb(45, 50, 66);
        paint.setShader(new LinearGradient(bounds.left, bounds.top, bounds.right, bounds.bottom,
                withAlpha(color, 240), darken(color, 0.55f), Shader.TileMode.CLAMP));
        canvas.drawRoundRect(bounds, 21f, 21f, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(primary ? Color.argb(185, 246, 210, 166)
                : Color.argb(110, 188, 194, 209));
        canvas.drawRoundRect(bounds, 21f, 21f, paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(21f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(label, bounds.centerX(), bounds.centerY() + 8f, textPaint);
    }

    private void drawCircleIcon(Canvas canvas, float x, float y, float radius,
                                String text, int color) {
        paint.setColor(Color.argb(220, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(x, y, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(125, 230, 232, 240));
        canvas.drawCircle(x, y, radius, paint);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(18f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(text, x, y + 6f, textPaint);
    }

    private void drawVignette(Canvas canvas) {
        paint.setShader(new RadialGradient(LOGICAL_WIDTH * 0.5f, LOGICAL_HEIGHT * 0.45f,
                760f, Color.TRANSPARENT, Color.argb(88, 0, 0, 4), Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 0f, LOGICAL_WIDTH, LOGICAL_HEIGHT, paint);
        paint.setShader(null);
    }

    private float heroRenderX() {
        return lerp(hero.previousX, hero.x, renderInterpolation);
    }

    private float enemyRenderX(Enemy enemy) {
        return lerp(enemy.previousX, enemy.x, renderInterpolation);
    }

    private void drawAtlasBlend(Canvas canvas, Bitmap atlas, int columns, int rows,
                                int firstColumn, int firstRow,
                                int secondColumn, int secondRow, float blend,
                                RectF destination, boolean flip, int alpha, int inset) {
        float easedBlend = RpgRules.clamp(blend, 0f, 1f);
        if (firstColumn == secondColumn && firstRow == secondRow || easedBlend <= 0.015f) {
            drawAtlasCell(canvas, atlas, columns, rows, firstColumn, firstRow,
                    destination, flip, alpha, inset);
            return;
        }
        if (easedBlend >= 0.985f) {
            drawAtlasCell(canvas, atlas, columns, rows, secondColumn, secondRow,
                    destination, flip, alpha, inset);
            return;
        }
        drawAtlasCell(canvas, atlas, columns, rows, firstColumn, firstRow,
                destination, flip, Math.round(alpha * (1f - easedBlend)), inset);
        drawAtlasCell(canvas, atlas, columns, rows, secondColumn, secondRow,
                destination, flip, Math.round(alpha * easedBlend), inset);
    }

    private void drawAtlasCell(Canvas canvas, Bitmap atlas, int columns, int rows,
                               int column, int row, RectF destination, boolean flip,
                               int alpha, int inset) {
        if (atlas == null || atlas.isRecycled()) {
            return;
        }
        int safeColumn = Math.max(0, Math.min(columns - 1, column));
        int safeRow = Math.max(0, Math.min(rows - 1, row));
        int left = Math.round(safeColumn * atlas.getWidth() / (float) columns) + inset;
        int top = Math.round(safeRow * atlas.getHeight() / (float) rows) + inset;
        int right = Math.round((safeColumn + 1) * atlas.getWidth() / (float) columns) - inset;
        int bottom = Math.round((safeRow + 1) * atlas.getHeight() / (float) rows) - inset;
        atlasSource.set(left, top, right, bottom);
        paint.setAlpha(Math.max(0, Math.min(255, alpha)));
        paint.setColorFilter(ACTOR_LIFT_FILTER);
        if (flip) {
            canvas.save();
            canvas.scale(-1f, 1f, destination.centerX(), destination.centerY());
            canvas.drawBitmap(atlas, atlasSource, destination, paint);
            canvas.restore();
        } else {
            canvas.drawBitmap(atlas, atlasSource, destination, paint);
        }
        paint.setAlpha(255);
        paint.setColorFilter(null);
    }

    private void drawFallbackFighter(Canvas canvas, RectF bounds, int color,
                                     int alpha, int facing) {
        float centerX = bounds.centerX();
        float feetY = bounds.bottom - 20f;
        float bodyHeight = bounds.height() * 0.48f;
        paint.setColor(withAlpha(darken(color, 0.52f), alpha));
        canvas.drawOval(new RectF(centerX - bounds.width() * 0.16f,
                feetY - bodyHeight, centerX + bounds.width() * 0.16f, feetY), paint);
        paint.setColor(withAlpha(color, alpha));
        canvas.drawCircle(centerX, feetY - bodyHeight - bounds.height() * 0.09f,
                bounds.width() * 0.085f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(4f, bounds.width() * 0.025f));
        canvas.drawLine(centerX + facing * bounds.width() * 0.08f,
                feetY - bodyHeight * 0.58f,
                centerX + facing * bounds.width() * 0.34f,
                feetY - bodyHeight * 0.18f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private void drawTextWithShadow(Canvas canvas, String text, float x, float y, Paint sourcePaint) {
        int color = sourcePaint.getColor();
        sourcePaint.setColor(Color.argb(Math.min(190, Color.alpha(color)), 0, 0, 0));
        canvas.drawText(text, x + 3f, y + 4f, sourcePaint);
        sourcePaint.setColor(color);
        canvas.drawText(text, x, y, sourcePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = (event.getX(event.getActionIndex()) - renderOffsetX) / renderScale;
        float y = (event.getY(event.getActionIndex()) - renderOffsetY) / renderScale;
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(event.getActionIndex());

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            handlePointerDown(pointerId, x, y);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            handlePointerUp(pointerId, x, y);
            if (action == MotionEvent.ACTION_UP) {
                performClick();
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            resetPointers();
        }
        return true;
    }

    private void handlePointerDown(int pointerId, float x, float y) {
        if (screen != Screen.PLAYING) {
            return;
        }
        if (insideCircle(x, y, 669f, 48f, 34f)) {
            saveNow();
            screen = Screen.PAUSED;
            resetPointers();
            return;
        }
        if (inventoryHudButton.contains(x, y)) {
            inventoryReturnScreen = Screen.PLAYING;
            selectedInventoryItem = -1;
            screen = Screen.INVENTORY;
            resetPointers();
            audio.playUiTap();
            return;
        }
        if (new RectF(548f, 222f, 710f, 298f).contains(x, y)) {
            growthReturnScreen = Screen.PLAYING;
            screen = Screen.GROWTH;
            resetPointers();
            return;
        }
        if (insideCircle(x, y, 92f, GameUiLayout.MANUAL_SKILL_CENTER_Y, 68f)) {
            dashPressed = true;
        } else if (insideCircle(x, y, 292f, GameUiLayout.MANUAL_SKILL_CENTER_Y, 70f)) {
            spearPressed = true;
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        } else if (insideCircle(x, y, 463f, GameUiLayout.MANUAL_SKILL_CENTER_Y, 70f)) {
            siphonPressed = true;
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        } else if (insideCircle(x, y, 630f, GameUiLayout.MANUAL_SKILL_CENTER_Y, 70f)) {
            novaPressed = true;
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        }
    }

    private void handlePointerUp(int pointerId, float x, float y) {
        if (pointerId == leftPointer) {
            leftPointer = -1;
            leftHeld = false;
        }
        if (pointerId == rightPointer) {
            rightPointer = -1;
            rightHeld = false;
        }

        if (screen == Screen.OFFLINE_REWARD) {
            if (offlineClaimButton.contains(x, y)) {
                claimOfflineRewards();
            }
        } else if (screen == Screen.STORY) {
            advanceStory();
        } else if (screen == Screen.TITLE) {
            if (continueButton.contains(x, y)) {
                if (continueAvailable) {
                    continueAdventure();
                } else {
                    startNewAdventure();
                }
            } else if (newGameButton.contains(x, y)) {
                if (continueAvailable) {
                    screen = Screen.NEW_CONFIRM;
                } else {
                    startNewAdventure();
                }
            }
        } else if (screen == Screen.NEW_CONFIRM) {
            if (confirmNewButton.contains(x, y)) {
                startNewAdventure();
            } else if (cancelNewButton.contains(x, y)) {
                screen = Screen.TITLE;
            }
        } else if (screen == Screen.PAUSED) {
            if (pauseResumeButton.contains(x, y)) {
                screen = Screen.PLAYING;
                lastFrameNanos = System.nanoTime();
            } else if (pauseGrowthButton.contains(x, y)) {
                growthReturnScreen = Screen.PAUSED;
                screen = Screen.GROWTH;
            } else if (pauseTitleButton.contains(x, y)) {
                saveNow();
                screen = Screen.TITLE;
            }
        } else if (screen == Screen.GROWTH) {
            for (int index = 0; index < upgradeButtons.length; index++) {
                if (upgradeButtons[index].contains(x, y)) {
                    purchaseUpgrade(index);
                    return;
                }
            }
            if (growthCloseButton.contains(x, y)) {
                screen = growthReturnScreen;
                if (screen == Screen.PLAYING) {
                    lastFrameNanos = System.nanoTime();
                }
            }
        } else if (screen == Screen.INVENTORY) {
            for (int index = 0; index < RpgProgress.INVENTORY_SIZE; index++) {
                if (inventorySlotBounds(index).contains(x, y)) {
                    selectedInventoryItem = progress.inventory[index] == 0 ? -1 : index;
                    audio.playUiTap();
                    return;
                }
            }
            if (inventoryActionButton.contains(x, y)) {
                manageSelectedInventoryItem();
            } else if (inventoryCloseButton.contains(x, y)) {
                screen = inventoryReturnScreen;
                selectedInventoryItem = -1;
                if (screen == Screen.PLAYING) {
                    lastFrameNanos = System.nanoTime();
                }
            }
        } else if (screen == Screen.DEFEAT) {
            if (retryButton.contains(x, y)) {
                retryCurrentWave();
            } else if (defeatTitleButton.contains(x, y)) {
                screen = Screen.TITLE;
            }
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public void pauseFromSystem() {
        audio.pause();
        if (screen == Screen.PLAYING || screen == Screen.GROWTH
                || screen == Screen.INVENTORY) {
            saveNow();
            screen = Screen.PAUSED;
            resetPointers();
        } else if (screen == Screen.PAUSED || screen == Screen.STORY) {
            saveNow();
        }
    }

    public void resumeFromSystem() {
        audio.resume();
        lastFrameNanos = System.nanoTime();
        if (screen == Screen.PAUSED || screen == Screen.STORY) {
            long nowEpochSeconds = System.currentTimeMillis() / 1000L;
            applyOfflineRewards(RpgRules.offlineElapsedSeconds(
                    progressStore.lastActiveEpochSeconds(), nowEpochSeconds));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        audio.release();
        recycleBitmap(heroAtlas);
        recycleBitmap(enemyAtlas);
        recycleBitmap(bossAtlas);
        recycleBitmap(bloodArtsAtlas);
        recycleBitmap(currentBackground);
        heroAtlas = null;
        enemyAtlas = null;
        bossAtlas = null;
        bloodArtsAtlas = null;
        currentBackground = null;
        super.onDetachedFromWindow();
    }

    public boolean handleBack() {
        if (screen == Screen.OFFLINE_REWARD) {
            claimOfflineRewards();
            return true;
        }
        if (screen == Screen.PLAYING) {
            saveNow();
            screen = Screen.PAUSED;
            resetPointers();
            return true;
        }
        if (screen == Screen.PAUSED) {
            screen = Screen.PLAYING;
            lastFrameNanos = System.nanoTime();
            return true;
        }
        if (screen == Screen.GROWTH) {
            screen = growthReturnScreen;
            if (screen == Screen.PLAYING) {
                lastFrameNanos = System.nanoTime();
            }
            return true;
        }
        if (screen == Screen.INVENTORY) {
            screen = inventoryReturnScreen;
            selectedInventoryItem = -1;
            if (screen == Screen.PLAYING) {
                lastFrameNanos = System.nanoTime();
            }
            return true;
        }
        if (screen == Screen.NEW_CONFIRM) {
            screen = Screen.TITLE;
            return true;
        }
        if (screen == Screen.STORY) {
            screen = Screen.TITLE;
            return true;
        }
        if (screen == Screen.DEFEAT) {
            screen = Screen.TITLE;
            return true;
        }
        return false;
    }

    private void consumeOneShotInput() {
        attackPressed = false;
        dashPressed = false;
        spearPressed = false;
        siphonPressed = false;
        novaPressed = false;
    }

    private void resetPointers() {
        leftPointer = -1;
        rightPointer = -1;
        leftHeld = false;
        rightHeld = false;
        queuedSkillAction = ACTION_NONE;
        queuedSkillTimer = 0f;
        consumeOneShotInput();
    }

    private void showToast(String text, float duration) {
        toastText = text;
        toastTimer = duration;
    }

    private boolean stashItem(int item) {
        if (!RpgProgress.isValidItem(item) || item == 0) {
            return false;
        }
        for (int index = 0; index < progress.inventory.length; index++) {
            if (progress.inventory[index] == 0) {
                progress.inventory[index] = item;
                return true;
            }
        }
        return false;
    }

    private void manageSelectedInventoryItem() {
        if (selectedInventoryItem < 0
                || selectedInventoryItem >= progress.inventory.length) {
            return;
        }
        int item = progress.inventory[selectedInventoryItem];
        if (item == 0 || !RpgProgress.isValidItem(item)) {
            selectedInventoryItem = -1;
            return;
        }
        int slot = itemSlot(item);
        int power = itemPower(item);
        int current = equippedPower(slot);
        progress.inventory[selectedInventoryItem] = 0;
        if (power > current) {
            if (current > 0) {
                stashItem(makeItemCode(slot, rarityFromPower(current), current));
            }
            setEquippedPower(slot, power);
            syncHeroStats(false);
            showToast(itemName(slot, itemRarity(item)) + " 장착 완료", 1.7f);
            addBurst(hero.x, GROUND_Y - 120f, rarityColor(itemRarity(item)), 15, 145f);
        } else {
            int salvage = salvageValue(item);
            progress.gold += salvage;
            showToast(itemName(slot, itemRarity(item)) + " 분해  ·  +"
                    + salvage + " 골드", 1.7f);
        }
        selectedInventoryItem = -1;
        audio.playUiTap();
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        saveNow();
    }

    private int inventoryCount() {
        int count = 0;
        for (int item : progress.inventory) {
            if (item != 0) {
                count++;
            }
        }
        return count;
    }

    private int equippedPower(int slot) {
        return slot == 0 ? progress.weaponPower
                : slot == 1 ? progress.armorPower : progress.relicPower;
    }

    private void setEquippedPower(int slot, int power) {
        if (slot == 0) {
            progress.weaponPower = power;
        } else if (slot == 1) {
            progress.armorPower = power;
        } else {
            progress.relicPower = power;
        }
    }

    private static RectF inventorySlotBounds(int index) {
        int column = index % 2;
        int row = index / 2;
        float left = column == 0 ? 52f : 376f;
        float top = 500f + row * 146f;
        return new RectF(left, top, left + 292f, top + 124f);
    }

    private static int makeItemCode(int slot, int rarity, int power) {
        return 1 + RpgRules.clamp(slot, 0, 2) * 10_000
                + RpgRules.clamp(rarity, 0, 3) * 1_000
                + RpgRules.clamp(power, 1, 300);
    }

    private static int itemSlot(int item) {
        return Math.max(0, item - 1) / 10_000;
    }

    private static int itemRarity(int item) {
        return Math.max(0, item - 1) % 10_000 / 1_000;
    }

    private static int itemPower(int item) {
        return Math.max(0, item - 1) % 1_000;
    }

    private static int rarityFromPower(int power) {
        if (power >= 100) {
            return 3;
        }
        if (power >= 45) {
            return 2;
        }
        return power >= 15 ? 1 : 0;
    }

    private static int salvageValue(int item) {
        return Math.max(8, itemPower(item) * 2 + itemRarity(item) * 12);
    }

    private static String slotName(int slot) {
        return slot == 0 ? "무기" : slot == 1 ? "갑옷" : "혈석";
    }

    private static String itemName(int slot, int rarity) {
        String[][] names = {
                {"낡은 혈검", "월식 혈검", "왕가의 혈검", "시조의 혈검"},
                {"그림자 외투", "밤안개 외투", "혈족 군주복", "불멸의 장막"},
                {"응결 혈석", "푸른 혈석", "혈월의 눈", "녹스의 심장"}
        };
        return names[RpgRules.clamp(slot, 0, 2)][RpgRules.clamp(rarity, 0, 3)];
    }

    private static String itemEffect(int slot, int power) {
        return (slot == 0 ? "공격 +" : slot == 1 ? "방어 +" : "혈기 +") + power;
    }

    private int combatPower() {
        return heroAttackPower * 4 + Math.round(heroMaxHealth * 0.55f)
                + Math.round(heroMaxBlood * 0.45f) + progress.armorPower * 5
                + progress.spearLevel * 8 + progress.siphonLevel * 10
                + progress.novaLevel * 12;
    }

    private static boolean faces(float attackerX, int facing, float targetX) {
        return facing >= 0 ? targetX >= attackerX - 10f : targetX <= attackerX + 10f;
    }

    private static boolean insideCircle(float x, float y, float centerX, float centerY,
                                        float radius) {
        float dx = x - centerX;
        float dy = y - centerY;
        return dx * dx + dy * dy <= radius * radius;
    }

    private static float approach(float value, float target, float amount) {
        if (value < target) {
            return Math.min(target, value + amount);
        }
        return Math.max(target, value - amount);
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * RpgRules.clamp(amount, 0f, 1f);
    }

    private static float smootherStep(float value) {
        float t = RpgRules.clamp(value, 0f, 1f);
        return t * t * t * (t * (t * 6f - 15f) + 10f);
    }

    private static float easeOutCubic(float value) {
        float t = 1f - RpgRules.clamp(value, 0f, 1f);
        return 1f - t * t * t;
    }

    private static float cubicBezier(float start, float control1, float control2,
                                     float end, float value) {
        float t = RpgRules.clamp(value, 0f, 1f);
        float inverse = 1f - t;
        return inverse * inverse * inverse * start
                + 3f * inverse * inverse * t * control1
                + 3f * inverse * t * t * control2
                + t * t * t * end;
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)),
                Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int darken(int color, float factor) {
        return Color.rgb(Math.round(Color.red(color) * factor),
                Math.round(Color.green(color) * factor),
                Math.round(Color.blue(color) * factor));
    }

    private static String oneDecimal(float value) {
        return String.format(Locale.US, "%.1f", Math.max(0f, value));
    }

    private static String formatNumber(int value) {
        if (value >= 1_000_000) {
            return String.format(Locale.US, "%.1fM", value / 1_000_000f);
        }
        if (value >= 10_000) {
            return String.format(Locale.US, "%.1fK", value / 1_000f);
        }
        return String.valueOf(value);
    }

    private static String formatOfflineDuration(int seconds) {
        int safeSeconds = Math.max(0, seconds);
        int hours = safeSeconds / 3600;
        int minutes = safeSeconds % 3600 / 60;
        if (hours > 0) {
            return hours + "시간 " + minutes + "분";
        }
        return Math.max(1, minutes) + "분";
    }

    private static String rarityName(int rarity) {
        switch (rarity) {
            case 3:
                return "신화";
            case 2:
                return "영웅";
            case 1:
                return "희귀";
            default:
                return "일반";
        }
    }

    private static int rarityColor(int rarity) {
        switch (rarity) {
            case 3:
                return GOLD;
            case 2:
                return Color.rgb(195, 90, 232);
            case 1:
                return CYAN;
            default:
                return Color.WHITE;
        }
    }

    private static final class Hero {
        float x;
        float previousX;
        float velocity;
        float invulnerability;
        float hurtTimer;
        float dashTimer;
        float runDistance;
        float animClock;
        float deadTimer;
        int facing;
        int lastFootstep;
        boolean dead;

        void reset(float startX, int startFacing) {
            x = startX;
            previousX = startX;
            velocity = 0f;
            invulnerability = 0f;
            hurtTimer = 0f;
            dashTimer = 0f;
            runDistance = 0f;
            animClock = 0f;
            deadTimer = 0f;
            facing = startFacing;
            lastFootstep = 0;
            dead = false;
        }
    }

    private static final class Enemy {
        int id;
        int kind;
        int facing;
        int actionType;
        float x;
        float previousX;
        float velocity;
        float health;
        float maxHealth;
        float invulnerability;
        float hurtTimer;
        float spawnTimer;
        float deadTimer;
        float cooldown;
        float actionTimer;
        float actionDuration;
        float actionTrigger;
        float animClock;
        float runDistance;
        boolean actionTriggered;
        boolean dead;
        boolean rewarded;
    }

    private static final class Projectile {
        final List<Integer> hitIds = new ArrayList<>();
        float x;
        float previousX;
        float y;
        float velocityX;
        float life;
        float maxLife;
        float trailTimer;
        float radius;
        int kind;
        int damage;
        int pierce;
        int color;
        boolean enemyOwned;
    }

    private static final class Particle {
        float x;
        float y;
        float velocityX;
        float velocityY;
        float gravity;
        float radius;
        float life;
        float maxLife;
        int color;
        int style;
    }

    private static final class SkillEffect {
        int kind;
        int color;
        float x;
        float y;
        float targetX;
        float targetY;
        float radius;
        float direction;
        float life;
        float maxLife;
        float seed;
    }

    private static final class FloatingText {
        float x;
        float y;
        float life;
        float maxLife;
        int color;
        String text;

        FloatingText(float x, float y, String text, int color, float life) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
            this.life = life;
            this.maxLife = life;
        }
    }
}
