package com.example.bloodmoonnightfall;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import org.godotengine.godot.Godot;
import org.godotengine.godot.GodotActivity;
import org.godotengine.godot.plugin.GodotPlugin;

import java.util.Collections;
import java.util.Set;

/** Android host for the Godot-powered VAYLORN concept demo. */
@SuppressWarnings("deprecation") // Immersive flags keep Android 7-10 behavior consistent.
public final class MainActivity extends GodotActivity {
    private static final String BOOT_TAG = "VaylornBoot";

    @Override
    protected void onCreate(Bundle state) {
        Log.i(BOOT_TAG, "ACTIVITY_CREATE_BEGIN");
        super.onCreate(state);
        Log.i(BOOT_TAG, "ACTIVITY_CREATE_COMPLETE");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUi();
    }

    @Override
    public void onGodotSetupCompleted() {
        super.onGodotSetupCompleted();
        Log.i(BOOT_TAG, "ENGINE_SETUP_COMPLETE");
    }

    @Override
    public void onGodotMainLoopStarted() {
        Log.i(BOOT_TAG, "MAIN_LOOP_STARTED");
    }

    @Override
    public Set<GodotPlugin> getHostPlugins(Godot godot) {
        return Collections.singleton(new GodotProgressPlugin(godot));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUi();
        }
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
