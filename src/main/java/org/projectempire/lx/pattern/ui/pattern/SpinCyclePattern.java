package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.pattern.LXPattern;
import org.projectempire.lx.utils.Blaze;

@LXCategory("Blaze")
public class SpinCyclePattern extends LXPattern {
    public SpinCyclePattern(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        final float t1 = Blaze.time(0.1f);
        for (int index = 0; index < model.points.length; index++) {
            float pct = index / (float) model.points.length;
            float h = pct * (5f * Blaze.wave(t1) + 5f) + 2f * Blaze.wave(t1);
            h = h % 0.5f + t1;
            // I think that our triangle doesn't match the original
            float v = Blaze.triangle(5f * pct + 10f * t1);
            v = v * v * v;
            // h in pixel blaze = 0-1
            //   in LX =0-360
            // s, v in pixel blaze = 0-1
            //      in LX = 0-100
            colors[model.points[index].index] = Blaze.hsv(h, 1f, v);
        }
    }
// Original code from PixelBlaze:
///*
//  Spin cycle
//*/
//
//    export function beforeRender(delta) {
//        t1 = time(.1)
//    }
//
//    export function render(index) {
//        pct = index / pixelCount  // Percent this pixel is into the overall strip
//        h = pct * (5 * wave(t1) + 5) + 2 * wave(t1)
//        h = h % .5 + t1  // Remainder has precedence over addition
//        v = triangle(5 * pct + 10 * t1)
//        v = v * v * v
//        hsv(h, 1, v)
//    }
}
