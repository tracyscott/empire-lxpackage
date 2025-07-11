package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.pattern.LXPattern;
import org.projectempire.lx.utils.Blaze;

@LXCategory("Blaze")
public class SlowColorShiftPattern extends LXPattern {

    public SlowColorShiftPattern(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        final float l4 = model.points.length * 4; // 4 times the strip length
        final float t1 = Blaze.time(0.15f) * LX.TWO_PIf;
        final float t2 = Blaze.time(0.1f);
        for (int index = 0; index < model.points.length; index++) {
            float h = (t2 + 1f + Blaze.sin(index / 2f + 5f * Blaze.sin(t1)) / 5f) + index / l4;
            float v = Blaze.wave((index / 2f + 5f * Blaze.sin(t1)) / LX.TWO_PIf);
            v = (float) Math.pow(v, 4);
            // h in pixel blaze = 0-1
            //   in LX =0-360
            // s, v in pixel blaze = 0-1
            //      in LX = 0-100
            colors[model.points[index].index] = LX.hsb(h * 360f, 100f, v * 100f);
        }
    }

// Original code from PixelBlaze:
//    l4 = pixelCount * 4     // 4 times the strip length
//
//    export function beforeRender(delta) {
//        t1 = time(.15) * PI2
//        t2 = time(.1)
//    }
//
//    export function render(index) {
//        h = (t2 + 1 + sin(index / 2 + 5 * sin(t1)) / 5) + index / l4
//
//        v = wave((index / 2 + 5 * sin(t1)) / PI2)
//        v = v * v * v * v
//
//        hsv(h, 1, v)
//    }
}
