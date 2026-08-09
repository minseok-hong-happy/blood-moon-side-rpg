package com.example.bloodmoonnightfall;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public final class CombatStoryTest {
    @Test
    public void everyWaveOwnsReadableOpeningAndMidpointBeats() {
        for (int region = 0; region < CombatStory.REGION_COUNT; region++) {
            for (int wave = 1; wave <= CombatStory.WAVE_COUNT; wave++) {
                assertReadable(CombatStory.opening(region, wave, 0));
                assertReadable(CombatStory.midpoint(region, wave));
            }
            assertTrue(!CombatStory.sealObjective(region).isEmpty());
        }
    }

    @Test
    public void repeatedChapterAcknowledgesTheTimeLoop() {
        for (int region = 0; region < CombatStory.REGION_COUNT; region++) {
            CombatStory.Beat firstRun = CombatStory.opening(region, 1, 0);
            CombatStory.Beat repeatRun = CombatStory.opening(region, 1, 1);
            assertNotEquals(firstRun.line, repeatRun.line);
            assertReadable(repeatRun);
        }
    }

    @Test
    public void combatStoryPanelsDoNotOverlapHudOrControls() {
        assertTrue(GameUiLayout.combatStoryPanelsAreSeparated());
    }

    private static void assertReadable(CombatStory.Beat beat) {
        assertTrue(!beat.speaker.isEmpty());
        assertTrue(!beat.line.isEmpty());
        assertTrue(beat.line.length() <= 80);
    }
}
