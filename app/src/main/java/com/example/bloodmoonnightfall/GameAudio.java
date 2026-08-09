package com.example.bloodmoonnightfall;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.Random;

/** Low-latency original SFX plus a looping AI-composed background track. */
final class GameAudio {
    private final SoundPool soundPool;
    private final Random random = new Random(0xA0D10L);
    private final int hitLight;
    private final int hitHeavy;
    private final int dash;
    private final int bloodSpear;
    private final int siphon;
    private final int nova;
    private final int uiTap;
    private final int playerHurt;
    private final int levelUp;
    private MediaPlayer music;
    private boolean released;

    GameAudio(Context context) {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attributes)
                .build();
        hitLight = soundPool.load(context, R.raw.sfx_hit_light, 1);
        hitHeavy = soundPool.load(context, R.raw.sfx_hit_heavy, 1);
        dash = soundPool.load(context, R.raw.sfx_dash, 1);
        bloodSpear = soundPool.load(context, R.raw.sfx_blood_spear, 1);
        siphon = soundPool.load(context, R.raw.sfx_siphon, 1);
        nova = soundPool.load(context, R.raw.sfx_blood_nova, 1);
        uiTap = soundPool.load(context, R.raw.sfx_ui_tap, 1);
        playerHurt = soundPool.load(context, R.raw.sfx_player_hurt, 1);
        levelUp = soundPool.load(context, R.raw.sfx_level_up, 1);
        try {
            music = MediaPlayer.create(context, R.raw.bgm_blood_road);
            if (music != null) {
                music.setLooping(true);
                music.setVolume(0.32f, 0.32f);
                music.start();
            }
        } catch (RuntimeException ignored) {
            music = null;
        }
    }

    void playHit(boolean heavy, boolean killed, int combo) {
        float variation = (random.nextFloat() - 0.5f) * 0.08f;
        float comboLift = Math.min(2, Math.max(0, combo)) * 0.025f;
        float rate = Math.max(0.78f, Math.min(1.18f,
                (heavy ? 0.91f : 1.02f) + variation + comboLift));
        play(heavy || killed ? hitHeavy : hitLight, heavy || killed ? 0.92f : 0.72f,
                heavy || killed ? 5 : 3, rate);
    }

    void playDash() {
        play(dash, 0.62f, 2, 0.96f + random.nextFloat() * 0.06f);
    }

    void playBloodSpear() {
        play(bloodSpear, 0.78f, 4, 0.97f + random.nextFloat() * 0.05f);
    }

    void playSiphon() {
        play(siphon, 0.76f, 4, 1f);
    }

    void playNova() {
        play(nova, 0.94f, 7, 0.92f);
    }

    void playPlayerHurt() {
        play(playerHurt, 0.84f, 6, 0.94f + random.nextFloat() * 0.05f);
    }

    void playUiTap() {
        play(uiTap, 0.46f, 1, 0.98f + random.nextFloat() * 0.08f);
    }

    void playLevelUp() {
        play(levelUp, 0.74f, 5, 1f);
    }

    private void play(int soundId, float volume, int priority, float rate) {
        if (!released && soundId != 0) {
            soundPool.play(soundId, volume, volume, priority, 0, rate);
        }
    }

    void pause() {
        if (music != null && music.isPlaying()) {
            music.pause();
        }
        if (!released) {
            soundPool.autoPause();
        }
    }

    void resume() {
        if (released) {
            return;
        }
        soundPool.autoResume();
        if (music != null && !music.isPlaying()) {
            try {
                music.start();
            } catch (IllegalStateException ignored) {
                // The game remains playable if the device audio service was reclaimed.
            }
        }
    }

    void release() {
        if (released) {
            return;
        }
        released = true;
        soundPool.release();
        if (music != null) {
            music.release();
            music = null;
        }
    }
}
