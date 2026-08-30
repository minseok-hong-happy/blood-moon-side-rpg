package com.example.bloodmoonnightfall;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class ImpactAnimationRulesTest {
    @Test
    public void impactAnimationAdvancesMonotonicallyAndHoldsContact() {
        float previous = -1f;
        for (int sample = 0; sample <= 100; sample++) {
            float position = ImpactAnimationRules.framePosition(sample / 100f);
            assertTrue(position >= previous);
            previous = position;
        }
        assertEquals(1f, ImpactAnimationRules.framePosition(0.20f), 0.001f);
        assertEquals(1f, ImpactAnimationRules.framePosition(0.35f), 0.001f);
        assertEquals(3, ImpactAnimationRules.firstFrame(1f));
        assertEquals(3, ImpactAnimationRules.secondFrame(1f));
        assertEquals(0f, ImpactAnimationRules.alpha(1f), 0.001f);
    }

    @Test
    public void finisherOwnsTheLargestAndLongestImpact() {
        assertTrue(ImpactAnimationRules.size(ImpactAnimationRules.STYLE_FINISHER)
                > ImpactAnimationRules.size(ImpactAnimationRules.STYLE_HEAVY));
        assertTrue(ImpactAnimationRules.size(ImpactAnimationRules.STYLE_HEAVY)
                > ImpactAnimationRules.size(ImpactAnimationRules.STYLE_NORMAL));
        assertTrue(ImpactAnimationRules.duration(ImpactAnimationRules.STYLE_FINISHER)
                > ImpactAnimationRules.duration(ImpactAnimationRules.STYLE_HEAVY));
        assertTrue(ImpactAnimationRules.duration(ImpactAnimationRules.STYLE_HEAVY)
                > ImpactAnimationRules.duration(ImpactAnimationRules.STYLE_NORMAL));
    }
}
