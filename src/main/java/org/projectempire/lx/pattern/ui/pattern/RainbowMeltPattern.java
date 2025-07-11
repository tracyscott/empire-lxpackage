package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.pattern.LXPattern;
import org.projectempire.lx.utils.Blaze;

@LXCategory("Blaze")
public class RainbowMeltPattern extends LXPattern {
    public RainbowMeltPattern(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        final float t1 = Blaze.time(0.1f);
        final float scale = model.points.length / 2f;
        for (int i = 0; i < model.points.length; i++) {
            float c1 = 1f - Math.abs(i - scale) / scale;
            float c2 = Blaze.wave(c1);
            float c3 = Blaze.wave(c2 + t1);
            float v = Blaze.wave(c3 + t1);
            v = v * v;
            // hsv in pixel blaze h = 0-1
            //     in LX          h = 0-360
            colors[model.points[i].index] = LX.hsb(360f * (c1 + t1), 100f, v * 100f);
        }
    }
}
