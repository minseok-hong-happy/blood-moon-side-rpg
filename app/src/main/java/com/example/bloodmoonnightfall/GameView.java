package com.example.bloodmoonnightfall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
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

/** Portrait, obstacle-free 2D vampire growth action RPG. */
public final class GameView extends View {
    private static final float LOGICAL_WIDTH = 720f;
    private static final float LOGICAL_HEIGHT = 1280f;
    private static final float GROUND_Y = 866f;
    private static final float CONTROL_TOP = 936f;
    private static final float FIXED_STEP = 1f / 60f;
    private static final int MAX_ACTIVE_ENEMIES = 3;
    private static final int MAX_PARTICLES = 260;

    private static final int CRIMSON = Color.rgb(214, 31, 70);
    private static final int BLOOD = Color.rgb(142, 13, 44);
    private static final int GOLD = Color.rgb(236, 193, 91);
    private static final int CYAN = Color.rgb(102, 222, 237);
    private static final int VIOLET = Color.rgb(159, 112, 232);
    private static final int NIGHT = Color.rgb(7, 9, 18);

    private static final int ACTION_NONE = 0;
    private static final int ACTION_ATTACK = 1;
    private static final int ACTION_SPEAR = 2;
    private static final int ACTION_SIPHON = 3;
    private static final int ACTION_NOVA = 4;

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

    private enum Screen {
        TITLE,
        PLAYING,
        PAUSED,
        GROWTH,
        DEFEAT,
        NEW_CONFIRM
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Typeface titleTypeface = Typeface.create("serif", Typeface.BOLD);
    private final Typeface uiTypeface = Typeface.create("sans-serif", Typeface.NORMAL);
    private final Typeface uiBoldTypeface = Typeface.create("sans-serif", Typeface.BOLD);
    private final Random random = new Random(0xB100D00DL);

    private Bitmap heroAtlas;
    private Bitmap enemyAtlas;
    private Bitmap bossAtlas;
    private Bitmap currentBackground;
    private int currentBackgroundRegion = -1;
    private boolean combatAtlasLoadAttempted;
    private boolean bossAtlasLoadAttempted;

    private final RpgProgressStore progressStore;
    private RpgProgress progress;
    private boolean continueAvailable;

    private final Hero hero = new Hero();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    private Screen screen = Screen.TITLE;
    private Screen growthReturnScreen = Screen.PLAYING;
    private long lastFrameNanos;
    private float accumulator;
    private float renderScale = 1f;
    private float renderOffsetX;
    private float renderOffsetY;
    private float ambientClock;
    private float hitStop;
    private float screenShake;
    private float damageFlash;
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

    private boolean saveDirty;
    private float saveDelay;

    private boolean leftHeld;
    private boolean rightHeld;
    private boolean attackPressed;
    private boolean dashPressed;
    private boolean spearPressed;
    private boolean siphonPressed;
    private boolean novaPressed;
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
    private final RectF[] upgradeButtons = new RectF[7];

    public GameView(Context context) {
        super(context);
        setFocusable(true);
        setClickable(true);
        progressStore = new RpgProgressStore(context);
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
        heroAtlas = decodeBitmap(R.drawable.hero_side_atlas, false);
        enemyAtlas = decodeBitmap(R.drawable.enemy_side_atlas, false);
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
        } else {
            updateMenuEffects(frameDelta);
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
        toastTimer = Math.max(0f, toastTimer - dt);
        updateParticles(dt * 0.35f);
        updateFloatingTexts(dt);
    }

    private void updateGame(float dt) {
        waveBannerTimer = Math.max(0f, waveBannerTimer - dt);
        levelBannerTimer = Math.max(0f, levelBannerTimer - dt);
        chapterBannerTimer = Math.max(0f, chapterBannerTimer - dt);
        toastTimer = Math.max(0f, toastTimer - dt);
        screenShake = Math.max(0f, screenShake - dt * 26f);
        damageFlash = Math.max(0f, damageFlash - dt * 3.2f);
        comboGrace = Math.max(0f, comboGrace - dt);
        comboDisplayTimer = Math.max(0f, comboDisplayTimer - dt);
        spearCooldown = Math.max(0f, spearCooldown - dt);
        siphonCooldown = Math.max(0f, siphonCooldown - dt);
        novaCooldown = Math.max(0f, novaCooldown - dt);

        if (saveDirty) {
            saveDelay -= dt;
            if (saveDelay <= 0f) {
                saveNow();
            }
        }

        if (hitStop > 0f) {
            hitStop = Math.max(0f, hitStop - dt);
            updateParticles(dt * 0.2f);
            return;
        }

        processHeroInput();
        updateHero(dt);
        updateWaveSpawning(dt);
        updateEnemies(dt);
        resolveActorSpacing();
        updateProjectiles(dt);
        updateParticles(dt);
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
        if (dashPressed && (heroAction == ACTION_NONE || heroAction == ACTION_ATTACK)) {
            startDash();
        }
        if (attackPressed) {
            if (heroAction == ACTION_ATTACK) {
                comboQueued = true;
            } else if (heroAction == ACTION_NONE && hero.dashTimer <= 0f) {
                startAttack(comboGrace > 0f ? (comboIndex + 1) % 3 : 0);
            }
        }
        if (spearPressed && heroAction == ACTION_NONE && hero.dashTimer <= 0f) {
            tryStartSpear();
        }
        if (siphonPressed && heroAction == ACTION_NONE && hero.dashTimer <= 0f) {
            tryStartSiphon();
        }
        if (novaPressed && heroAction == ACTION_NONE && hero.dashTimer <= 0f) {
            tryStartNova();
        }
    }

    private void startDash() {
        int direction = rightHeld ? 1 : leftHeld ? -1 : hero.facing;
        if (direction == 0) {
            direction = 1;
        }
        hero.facing = direction;
        hero.dashTimer = 0.19f;
        hero.invulnerability = Math.max(hero.invulnerability, 0.24f);
        hero.velocity = direction * 735f;
        heroAction = ACTION_NONE;
        comboQueued = false;
        addBurst(hero.x, GROUND_Y - 86f, Color.argb(205, 181, 25, 60), 12, 185f);
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
    }

    private void startAttack(int nextCombo) {
        comboIndex = RpgRules.clamp(nextCombo, 0, 2);
        heroAction = ACTION_ATTACK;
        heroActionDuration = comboIndex == 2 ? 0.52f : 0.38f;
        heroActionTimer = heroActionDuration;
        heroActionTrigger = comboIndex == 2 ? 0.27f : 0.18f;
        heroActionTriggered = false;
        comboQueued = false;
        Enemy target = nearestEnemy(230f);
        if (target != null) {
            hero.facing = target.x >= hero.x ? 1 : -1;
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
        startHeroSkill(ACTION_SPEAR, 0.56f, 0.29f);
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
        startHeroSkill(ACTION_SIPHON, 0.72f, 0.37f);
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
        startHeroSkill(ACTION_NOVA, 0.88f, 0.48f);
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
                    comboGrace = 0.3f;
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
            if (hero.dashTimer <= 0f) {
                hero.velocity *= 0.28f;
            }
        } else {
            float movementMultiplier = heroAction == ACTION_NONE ? 1f : 0.32f;
            float desired = (rightHeld ? 1f : 0f) - (leftHeld ? 1f : 0f);
            if (desired != 0f) {
                hero.facing = desired > 0f ? 1 : -1;
            } else if (heroAction == ACTION_NONE) {
                Enemy target = nearestEnemy(360f);
                if (target != null) {
                    hero.facing = target.x >= hero.x ? 1 : -1;
                }
            }
            float targetVelocity = desired * 218f * movementMultiplier;
            hero.velocity = approach(hero.velocity, targetVelocity, 1050f * dt);
            hero.x += hero.velocity * dt;
            hero.runDistance += Math.abs(hero.velocity) * dt;
        }
        hero.x = RpgRules.clamp(hero.x, RpgRules.ARENA_LEFT, RpgRules.ARENA_RIGHT);
    }

    private void executeHeroAction() {
        if (heroAction == ACTION_ATTACK) {
            Enemy target = nearestEnemy(132f + comboIndex * 8f);
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
            projectile.y = GROUND_Y - 112f;
            projectile.velocityX = hero.facing * 570f;
            projectile.damage = RpgRules.spearDamage(heroAttackPower, progress.spearLevel);
            projectile.life = 1.35f;
            projectile.radius = 42f;
            projectile.pierce = 3;
            projectile.color = CRIMSON;
            projectiles.add(projectile);
            addBurst(projectile.x, projectile.y, CRIMSON, 16, 210f);
        } else if (heroAction == ACTION_SIPHON) {
            Enemy target = nearestEnemy(350f);
            if (target != null) {
                int damage = RpgRules.siphonDamage(heroAttackPower, progress.siphonLevel);
                damageEnemy(target, damage, 48f, true);
                float healed = Math.min(heroMaxHealth - heroHealth, damage * 0.62f);
                heroHealth += healed;
                heroBlood = Math.min(heroMaxBlood, heroBlood + damage * 0.22f);
                addBloodTether(target.x, GROUND_Y - 112f, hero.x, GROUND_Y - 112f);
                floatingTexts.add(new FloatingText(hero.x, GROUND_Y - 230f,
                        "+" + Math.round(healed), CYAN, 1.05f));
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
            addNovaBurst(hero.x, GROUND_Y - 90f, 255f);
            screenShake = Math.max(screenShake, 13f);
            hitStop = hits > 0 ? 0.075f : 0.025f;
        }
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private void updateWaveSpawning(float dt) {
        if (waveComplete || hero.dead || remainingToSpawn <= 0) {
            return;
        }
        spawnTimer -= dt;
        if (spawnTimer <= 0f && aliveEnemyCount() < MAX_ACTIVE_ENEMIES) {
            spawnEnemy();
            spawnTimer = progress.wave == RpgRules.WAVES_PER_REGION ? 1f : 0.72f;
        }
    }

    private void spawnEnemy() {
        int kind;
        if (progress.wave == RpgRules.WAVES_PER_REGION) {
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
        boolean spawnRight = (spawnSerial & 1) == 0;
        if (spawnRight && hero.x > 510f) {
            spawnRight = false;
        } else if (!spawnRight && hero.x < 210f) {
            spawnRight = true;
        }
        enemy.x = spawnRight ? 638f : 82f;
        enemy.facing = enemy.x > hero.x ? -1 : 1;
        enemy.maxHealth = RpgRules.enemyMaxHealth(kind, progress.region, progress.wave,
                progress.level, progress.chapterClears);
        enemy.health = enemy.maxHealth;
        enemy.cooldown = 0.65f + random.nextFloat() * 0.7f;
        enemy.spawnTimer = kind == RpgRules.ENEMY_BOSS ? 1.1f : 0.58f;
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
            projectile.y = GROUND_Y - 108f;
            projectile.velocityX = enemy.facing
                    * (enemy.kind == RpgRules.ENEMY_BOSS ? 365f : 315f);
            projectile.damage = damage;
            projectile.life = 2.1f;
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
                if (distance < 58f) {
                    float direction = difference == 0f ? (second % 2 == 0 ? 1f : -1f)
                            : Math.signum(difference);
                    float push = (58f - distance) * 0.5f;
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

    private void updateProjectiles(float dt) {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            projectile.x += projectile.velocityX * dt;
            projectile.life -= dt;
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
        enemy.hurtTimer = heavy ? 0.23f : 0.14f;
        enemy.invulnerability = 0.04f;
        float direction = enemy.x >= hero.x ? 1f : -1f;
        enemy.velocity += direction * knockback;
        floatingTexts.add(new FloatingText(enemy.x, GROUND_Y - 210f,
                "-" + applied, heavy ? GOLD : Color.WHITE, 0.9f));
        addBurst(enemy.x, GROUND_Y - 106f, CRIMSON, heavy ? 18 : 10, heavy ? 230f : 155f);
        screenShake = Math.max(screenShake, heavy ? 9f : 4f);
        hitStop = Math.max(hitStop, heavy ? 0.055f : 0.025f);
        if (enemy.health <= 0f) {
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
        screenShake = Math.max(screenShake, 11f);
        hitStop = Math.max(hitStop, 0.06f);
        floatingTexts.add(new FloatingText(hero.x, GROUND_Y - 230f,
                "-" + amount, Color.rgb(255, 104, 120), 1f));
        addBurst(hero.x, GROUND_Y - 108f, CRIMSON, 18, 210f);
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        if (heroHealth <= 0f) {
            hero.dead = true;
            hero.deadTimer = 1.15f;
            hero.velocity = direction * 170f;
            resetPointers();
        }
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
            if (previousSiphon == 0 && progress.siphonLevel > 0) {
                levelBannerText = "흡혈 해금  ·  LEVEL " + progress.level;
            } else if (previousNova == 0 && progress.novaLevel > 0) {
                levelBannerText = "혈월 폭발 해금  ·  LEVEL " + progress.level;
            }
            levelBannerTimer = 2.8f;
            addNovaBurst(hero.x, GROUND_Y - 100f, 210f);
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
        int current = slot == 0 ? progress.weaponPower
                : slot == 1 ? progress.armorPower : progress.relicPower;
        String slotName = slot == 0 ? "무기" : slot == 1 ? "갑옷" : "혈석";
        if (power > current) {
            if (slot == 0) {
                progress.weaponPower = power;
            } else if (slot == 1) {
                progress.armorPower = power;
            } else {
                progress.relicPower = power;
            }
            syncHeroStats(false);
            showToast(rarityName(rarity) + " " + slotName + " 획득  ·  전투력 +" + power, 2.4f);
            floatingTexts.add(new FloatingText(hero.x, GROUND_Y - 310f,
                    "장비 교체!", rarityColor(rarity), 1.6f));
        } else {
            int salvage = Math.max(8, power * 2);
            progress.gold += salvage;
            showToast(slotName + " 분해  ·  +" + salvage + " 골드", 1.6f);
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
        } else {
            progress.bossKills++;
            if (progress.region < RpgRules.REGION_COUNT - 1) {
                progress.region++;
                progress.wave = 1;
                chapterBannerTimer = 3.2f;
                levelBannerText = "새 지역 해금  ·  " + REGION_NAMES[progress.region];
                levelBannerTimer = 3.2f;
            } else {
                progress.chapterClears++;
                progress.region = 0;
                progress.wave = 1;
                chapterBannerTimer = 4f;
                levelBannerText = "밤의 순환 " + (progress.chapterClears + 1) + "단계";
                levelBannerTimer = 4f;
            }
        }
        saveNow();
        startCurrentWave(false);
    }

    private void startNewAdventure() {
        progressStore.clear();
        progress = RpgProgress.fresh();
        continueAvailable = true;
        beginAdventure();
        saveNow();
    }

    private void continueAdventure() {
        RpgProgress loaded = progressStore.load();
        progress = loaded == null ? RpgProgress.fresh() : loaded;
        progress.normalizeUnlocks();
        beginAdventure();
    }

    private void beginAdventure() {
        ensureCombatAtlases();
        hero.reset(275f, 1);
        syncHeroStats(true);
        spearCooldown = 0f;
        siphonCooldown = 0f;
        novaCooldown = 0f;
        screen = Screen.PLAYING;
        lastFrameNanos = System.nanoTime();
        startCurrentWave(true);
    }

    private void startCurrentWave(boolean fullRestore) {
        enemies.clear();
        projectiles.clear();
        floatingTexts.clear();
        remainingToSpawn = RpgRules.waveEnemyCount(progress.region, progress.wave);
        waveTarget = remainingToSpawn;
        defeatedThisWave = 0;
        spawnSerial = 0;
        nextEnemyId = 1;
        spawnTimer = 0.55f;
        waveComplete = false;
        waveClearTimer = 0f;
        waveBannerTimer = 2.2f;
        hero.dead = false;
        hero.deadTimer = 0f;
        hero.invulnerability = 0.55f;
        heroAction = ACTION_NONE;
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
        hero.reset(275f, 1);
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
            addParticle(x, y, (float) Math.cos(angle) * magnitude,
                    (float) Math.sin(angle) * magnitude, color,
                    3f + index * 0.16f, 0.26f + index * 0.012f, 70f);
        }
    }

    private void addBloodTether(float fromX, float fromY, float toX, float toY) {
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
        for (int index = 0; index < 40 && particles.size() < MAX_PARTICLES; index++) {
            float angle = index / 40f * (float) Math.PI * 2f;
            float speed = radius * (0.75f + random.nextFloat() * 0.55f);
            addParticle(x, y, (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed * 0.58f,
                    index % 5 == 0 ? GOLD : CRIMSON,
                    4f + random.nextFloat() * 5f, 0.58f, 55f);
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

    private void drawScene(Canvas canvas) {
        float shakeX = screenShake > 0f
                ? (float) Math.sin(ambientClock * 84f) * screenShake : 0f;
        float shakeY = screenShake > 0f
                ? (float) Math.cos(ambientClock * 67f) * screenShake * 0.45f : 0f;
        canvas.save();
        canvas.translate(shakeX, shakeY);
        drawBackground(canvas);
        if (screen != Screen.TITLE && screen != Screen.NEW_CONFIRM) {
            drawArena(canvas);
        } else {
            drawTitleAtmosphere(canvas);
        }
        canvas.restore();

        if (screen == Screen.TITLE || screen == Screen.NEW_CONFIRM) {
            drawTitle(canvas);
            if (screen == Screen.NEW_CONFIRM) {
                drawNewConfirm(canvas);
            }
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
            } else if (screen == Screen.DEFEAT) {
                drawDefeat(canvas);
            }
        }
        if (damageFlash > 0f) {
            paint.setColor(Color.argb(Math.round(112f * damageFlash), 190, 8, 36));
            canvas.drawRect(0f, 0f, LOGICAL_WIDTH, LOGICAL_HEIGHT, paint);
        }
    }

    private void drawBackground(Canvas canvas) {
        int region = progress == null ? 0 : RpgRules.clamp(progress.region, 0, 2);
        Bitmap background = obtainBackground(region);
        float parallax = screen == Screen.TITLE ? (float) Math.sin(ambientClock * 0.08f) * 8f
                : (hero.x - LOGICAL_WIDTH * 0.5f) * 0.025f;
        if (background != null && !background.isRecycled()) {
            canvas.drawBitmap(background, null,
                    new RectF(-12f - parallax, -2f, LOGICAL_WIDTH + 12f - parallax,
                            LOGICAL_HEIGHT + 2f), paint);
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
                Color.argb(18, 4, 5, 14), Color.argb(100, 3, 4, 10), Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 0f, LOGICAL_WIDTH, LOGICAL_HEIGHT, paint);
        paint.setShader(null);
        drawAmbientMotes(canvas, region);
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
            paint.setColor(withAlpha(color, Math.round(42f + pulse * 48f)));
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

    private void drawArena(Canvas canvas) {
        drawGroundGlow(canvas);
        for (Enemy enemy : enemies) {
            drawEnemyTelegraph(canvas, enemy);
        }
        for (Enemy enemy : enemies) {
            drawFighterShadow(canvas, enemy.x, enemy.dead ? 0.3f : 0.85f,
                    enemy.kind == RpgRules.ENEMY_BOSS ? 76f : 50f);
        }
        drawFighterShadow(canvas, hero.x, hero.dead ? 0.35f : 1f, 58f);
        for (Enemy enemy : enemies) {
            drawEnemy(canvas, enemy);
        }
        drawHero(canvas);
        drawProjectiles(canvas);
        drawParticles(canvas);
        drawFloatingTexts(canvas);
        drawVignette(canvas);
    }

    private void drawGroundGlow(Canvas canvas) {
        paint.setShader(new RadialGradient(LOGICAL_WIDTH * 0.5f, GROUND_Y + 18f, 330f,
                Color.argb(62, 196, 24, 62), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawOval(new RectF(18f, GROUND_Y - 55f, LOGICAL_WIDTH - 18f, GROUND_Y + 88f), paint);
        paint.setShader(null);
    }

    private void drawFighterShadow(Canvas canvas, float x, float alpha, float width) {
        paint.setColor(Color.argb(Math.round(118f * alpha), 0, 0, 0));
        canvas.drawOval(new RectF(x - width, GROUND_Y - 12f, x + width, GROUND_Y + 19f), paint);
    }

    private void drawHero(Canvas canvas) {
        int row;
        int column;
        if (hero.dead) {
            row = 3;
            column = 3;
        } else if (hero.hurtTimer > 0f) {
            row = 3;
            column = 2;
        } else if (heroAction != ACTION_NONE && heroAction != ACTION_ATTACK) {
            row = 3;
            column = heroAction == ACTION_SIPHON ? 1 : 0;
        } else if (heroAction == ACTION_ATTACK) {
            row = 2;
            column = comboIndex + 1;
        } else if (hero.dashTimer > 0f) {
            row = 1;
            column = 3;
        } else if (Math.abs(hero.velocity) > 24f) {
            row = 0;
            column = ((int) (hero.runDistance / 32f)) % 4;
        } else {
            row = 1;
            column = 0;
        }
        float bob = hero.dead ? 0f : (float) Math.sin(ambientClock * 3.2f) * 2.2f;
        float width = 248f;
        float height = 248f;
        RectF destination = new RectF(hero.x - width * 0.5f,
                GROUND_Y - height + 21f + bob, hero.x + width * 0.5f, GROUND_Y + 21f + bob);
        int alpha = hero.invulnerability > 0f
                && ((int) (hero.invulnerability * 32f) & 1) == 0 ? 142 : 255;
        if (hero.dashTimer > 0f) {
            for (int trail = 4; trail >= 1; trail--) {
                RectF trailRect = new RectF(destination);
                trailRect.offset(-hero.facing * trail * 31f, 0f);
                drawAtlasCell(canvas, heroAtlas, 4, 4, column, row, trailRect,
                        hero.facing < 0, 16 + trail * 12, 4);
            }
        }
        if (heroAction == ACTION_NOVA && heroActionTimer > 0f) {
            float fraction = 1f - heroActionTimer / heroActionDuration;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5f);
            paint.setColor(withAlpha(CRIMSON, Math.round(80f + fraction * 130f)));
            canvas.drawCircle(hero.x, GROUND_Y - 104f, 70f + fraction * 85f, paint);
            paint.setStyle(Paint.Style.FILL);
        }
        if (heroAtlas == null || heroAtlas.isRecycled()) {
            drawFallbackFighter(canvas, destination, CRIMSON, alpha, hero.facing);
        } else {
            drawAtlasCell(canvas, heroAtlas, 4, 4, column, row, destination,
                    hero.facing < 0, alpha, 4);
        }
    }

    private void drawEnemy(Canvas canvas, Enemy enemy) {
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
            int column = 0;
            if (enemy.dead) {
                row = 2;
                column = 3;
            } else if (enemy.actionTimer > 0f) {
                row = enemy.actionType == ENEMY_MELEE || enemy.actionType == ENEMY_HEAVY ? 1 : 2;
                column = enemy.actionTriggered ? 2 : 0;
            } else if (Math.abs(enemy.velocity) > 24f) {
                column = ((int) (enemy.runDistance / 38f)) & 1;
            }
            float width = 330f;
            float height = 278f;
            RectF destination = new RectF(enemy.x - width * 0.5f,
                    GROUND_Y - height + 22f, enemy.x + width * 0.5f, GROUND_Y + 22f);
            if (bossAtlas == null || bossAtlas.isRecycled()) {
                drawFallbackFighter(canvas, destination, GOLD, alpha, enemy.facing);
            } else {
                drawAtlasCell(canvas, bossAtlas, 4, 3, column, row, destination,
                        enemy.facing > 0, alpha, 3);
            }
        } else {
            int row = enemy.kind;
            int column;
            if (enemy.actionTimer > 0f) {
                column = enemy.actionTriggered ? 3 : 2;
            } else if (Math.abs(enemy.velocity) > 20f) {
                column = ((int) (enemy.runDistance / 31f)) & 1;
            } else {
                column = 0;
            }
            float width = enemy.kind == RpgRules.ENEMY_WRAITH ? 224f : 210f;
            float height = enemy.kind == RpgRules.ENEMY_WRAITH ? 204f : 196f;
            RectF destination = new RectF(enemy.x - width * 0.5f,
                    GROUND_Y - height + 18f, enemy.x + width * 0.5f, GROUND_Y + 18f);
            if (enemyAtlas == null || enemyAtlas.isRecycled()) {
                drawFallbackFighter(canvas, destination,
                        enemy.kind == RpgRules.ENEMY_WRAITH ? CYAN : VIOLET,
                        alpha, enemy.facing);
            } else {
                drawAtlasCell(canvas, enemyAtlas, 4, 3, column, row, destination,
                        enemy.facing > 0, alpha, 3);
            }
        }
        if (!enemy.dead && enemy.spawnTimer <= 0f
                && (enemy.kind == RpgRules.ENEMY_BOSS || enemy.hurtTimer > 0f)) {
            float width = enemy.kind == RpgRules.ENEMY_BOSS ? 260f : 112f;
            float y = enemy.kind == RpgRules.ENEMY_BOSS ? GROUND_Y - 288f : GROUND_Y - 210f;
            drawMiniHealthBar(canvas, enemy.x - width * 0.5f, y, width,
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
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f + fraction * 5f);
        paint.setColor(withAlpha(color, Math.round(72f + fraction * 165f)));
        canvas.drawCircle(enemy.x, enemy.actionType == ENEMY_NOVA
                ? GROUND_Y - 78f : GROUND_Y - 142f, radius * (0.78f + fraction * 0.22f), paint);
        if (enemy.actionType == ENEMY_RANGED) {
            paint.setStrokeWidth(2f);
            paint.setColor(withAlpha(color, Math.round(38f + fraction * 82f)));
            canvas.drawLine(enemy.x, GROUND_Y - 108f, hero.x, GROUND_Y - 108f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawProjectiles(Canvas canvas) {
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (Projectile projectile : projectiles) {
            if (projectile.kind == 2) {
                paint.setShader(new RadialGradient(projectile.x, projectile.y,
                        projectile.radius * 1.8f, Color.WHITE, projectile.color,
                        Shader.TileMode.CLAMP));
                canvas.drawCircle(projectile.x, projectile.y, projectile.radius, paint);
                paint.setShader(null);
            } else {
                float direction = Math.signum(projectile.velocityX);
                float length = projectile.enemyOwned ? 28f : 64f;
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(projectile.enemyOwned ? 8f : 11f);
                paint.setColor(projectile.color);
                canvas.drawLine(projectile.x - direction * length, projectile.y,
                        projectile.x + direction * 12f, projectile.y, paint);
                paint.setStrokeWidth(2f);
                paint.setColor(Color.WHITE);
                canvas.drawLine(projectile.x - direction * length * 0.45f, projectile.y,
                        projectile.x + direction * 8f, projectile.y, paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void drawParticles(Canvas canvas) {
        for (Particle particle : particles) {
            float fraction = RpgRules.clamp(particle.life / particle.maxLife, 0f, 1f);
            paint.setColor(withAlpha(particle.color, Math.round(255f * fraction)));
            canvas.drawCircle(particle.x, particle.y,
                    particle.radius * (0.35f + fraction * 0.65f), paint);
        }
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

        drawControlButton(canvas, 88f, 1110f, 64f, "◀", "이동", leftHeld,
                0f, true, Color.rgb(57, 65, 86));
        drawControlButton(canvas, 218f, 1110f, 64f, "▶", "이동", rightHeld,
                0f, true, Color.rgb(57, 65, 86));
        drawControlButton(canvas, 153f, 1212f, 47f, "✦", "대시 ∞", false,
                0f, true, Color.rgb(103, 40, 70));

        drawControlButton(canvas, 618f, 1084f, 72f, "A", "공격", false,
                0f, true, CRIMSON);
        drawControlButton(canvas, 505f, 1190f, 53f, "血", "혈창 20", false,
                spearCooldown / 0.9f, true, Color.rgb(154, 24, 58));
        drawControlButton(canvas, 400f, 1088f, 51f, "吸", "흡혈 30", false,
                siphonCooldown / 4.8f, progress.level >= 4, Color.rgb(38, 119, 144));
        drawControlButton(canvas, 618f, 1222f, 47f, "月", "폭발 55", false,
                novaCooldown / 7.5f, progress.level >= 7, Color.rgb(115, 53, 149));

        if (comboDisplayTimer > 0f && comboIndex > 0) {
            textPaint.setTypeface(uiBoldTypeface);
            textPaint.setTextSize(18f);
            textPaint.setColor(GOLD);
            canvas.drawText((comboIndex + 1) + " COMBO", 618f, 974f, textPaint);
        }
    }

    private void drawControlButton(Canvas canvas, float x, float y, float radius,
                                   String symbol, String label, boolean pressed,
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
        textPaint.setTypeface(uiBoldTypeface);
        textPaint.setTextSize(radius > 60f ? 31f : 25f);
        textPaint.setColor(enabled ? Color.WHITE : Color.rgb(115, 118, 129));
        canvas.drawText(symbol, x, y + 8f, textPaint);
        textPaint.setTextSize(14f);
        textPaint.setColor(enabled ? Color.rgb(226, 229, 238) : Color.rgb(110, 112, 121));
        canvas.drawText(label, x, y + radius + 19f, textPaint);
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
        drawTextWithShadow(canvas, "BLOOD MOON", 360f, 378f, textPaint);
        textPaint.setLetterSpacing(0f);
        textPaint.setTextSize(34f);
        textPaint.setColor(CRIMSON);
        drawTextWithShadow(canvas, "밤의 계승자", 360f, 430f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(19f);
        textPaint.setColor(Color.rgb(209, 215, 228));
        canvas.drawText("세로형 2D 뱀파이어 성장 액션 RPG", 360f, 474f, textPaint);

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
        canvas.drawText("사냥  ·  성장  ·  장비  ·  지역 해금", 360f, 572f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(17f);
        textPaint.setColor(Color.rgb(194, 200, 214));
        canvas.drawText("몬스터 무리를 사냥하고 영구 능력과 장비를 키우세요", 360f, 614f, textPaint);
        canvas.drawText("장애물 없는 전장 · 무제한 대시 · 3개 지역 보스", 360f, 649f, textPaint);

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
        canvas.drawText("v3.0.1  ·  진행도 자동 저장", 360f, 1120f, textPaint);
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
        canvas.drawText("자동 장착 장비", 72f, 970f, textPaint);
        textPaint.setTypeface(uiTypeface);
        textPaint.setTextSize(17f);
        textPaint.setColor(Color.rgb(210, 215, 228));
        canvas.drawText("무기  +" + progress.weaponPower + " 공격", 72f, 1008f, textPaint);
        canvas.drawText("갑옷  +" + progress.armorPower + " 방어", 270f, 1008f, textPaint);
        canvas.drawText("혈석  +" + progress.relicPower + " 혈기", 468f, 1008f, textPaint);
        textPaint.setTextSize(14f);
        textPaint.setColor(Color.rgb(142, 151, 171));
        canvas.drawText("더 강한 전리품은 즉시 장착되고, 낮은 장비는 골드로 분해됩니다", 72f, 1045f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        drawMenuButton(canvas, growthCloseButton,
                growthReturnScreen == Screen.PAUSED ? "일시정지로" : "전투로 복귀", true);
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
                690f, Color.TRANSPARENT, Color.argb(175, 0, 0, 4), Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 0f, LOGICAL_WIDTH, LOGICAL_HEIGHT, paint);
        paint.setShader(null);
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
        Rect source = new Rect(left, top, right, bottom);
        paint.setAlpha(Math.max(0, Math.min(255, alpha)));
        paint.setColorFilter(null);
        if (flip) {
            canvas.save();
            canvas.scale(-1f, 1f, destination.centerX(), destination.centerY());
            canvas.drawBitmap(atlas, source, destination, paint);
            canvas.restore();
        } else {
            canvas.drawBitmap(atlas, source, destination, paint);
        }
        paint.setAlpha(255);
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
        if (new RectF(548f, 222f, 710f, 298f).contains(x, y)) {
            growthReturnScreen = Screen.PLAYING;
            screen = Screen.GROWTH;
            resetPointers();
            return;
        }
        if (insideCircle(x, y, 88f, 1110f, 70f) && leftPointer < 0) {
            leftPointer = pointerId;
            leftHeld = true;
        } else if (insideCircle(x, y, 218f, 1110f, 70f) && rightPointer < 0) {
            rightPointer = pointerId;
            rightHeld = true;
        } else if (insideCircle(x, y, 153f, 1212f, 56f)) {
            dashPressed = true;
        } else if (insideCircle(x, y, 618f, 1084f, 82f)) {
            attackPressed = true;
        } else if (insideCircle(x, y, 505f, 1190f, 62f)) {
            spearPressed = true;
        } else if (insideCircle(x, y, 400f, 1088f, 61f)) {
            siphonPressed = true;
        } else if (insideCircle(x, y, 618f, 1222f, 56f)) {
            novaPressed = true;
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

        if (screen == Screen.TITLE) {
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
        if (screen == Screen.PLAYING || screen == Screen.GROWTH) {
            saveNow();
            screen = Screen.PAUSED;
            resetPointers();
        }
    }

    public boolean handleBack() {
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
        if (screen == Screen.NEW_CONFIRM) {
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
        consumeOneShotInput();
    }

    private void showToast(String text, float duration) {
        toastText = text;
        toastTimer = duration;
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
        float velocity;
        float invulnerability;
        float hurtTimer;
        float dashTimer;
        float runDistance;
        float deadTimer;
        int facing;
        boolean dead;

        void reset(float startX, int startFacing) {
            x = startX;
            velocity = 0f;
            invulnerability = 0f;
            hurtTimer = 0f;
            dashTimer = 0f;
            runDistance = 0f;
            deadTimer = 0f;
            facing = startFacing;
            dead = false;
        }
    }

    private static final class Enemy {
        int id;
        int kind;
        int facing;
        int actionType;
        float x;
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
        float y;
        float velocityX;
        float life;
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
