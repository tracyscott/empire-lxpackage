package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;
import org.projectempire.lx.utils.Blaze;

@LXCategory("Blaze")
public class EdgeBurstPattern extends LXPattern {
    public EdgeBurstPattern(LX lx) {
        super(lx);
    }

    @Override
    protected void run(double deltaMs) {
        final float t1 = Blaze.triangle(Blaze.time(0.1f));
        for (int index = 0; index < model.points.length; index++) {
            float pct = index / (float) model.points.length;
            float edge = LXUtils.clampf(Blaze.triangle(pct) + t1 * 4f - 2f, 0, 1);
            float h = edge * edge - 0.2f;
            float v = Blaze.triangle(edge);
            colors[model.points[index].index] = Blaze.hsv(h, 1f, v);
        }
    }

// Original code from PixelBlaze:
//    /*
//  Edgeburst
//
//  The triangle() function is simple:
//
//  output:   1  /\    /\
//              /  \  /  \   etc
//           0 /    \/    \/
//  input:    0  .5  1     2
//
//  triangle() is the go-to function when you want to mirror something (space, or
//  time!) This pattern does both.
//
//  Mirroring space is the building block for kaleidoscopes (see 'sound - spectro
//  kalidastrip', 'xorcery', and 'glitch bands'). In this pattern we mirror the
//  pixel's position (expressed as a percentage) around the middle of the strip
//  with `triangle(pct)`.
//
//  Mirroring a 0..1 time sawtooth turns a looping timer into a back-and-forth
//  repetition.
//*/
//
//    export function beforeRender(delta) {
//        t1 = triangle(time(.1))  // Mirror time (bounce)
//    }
//
//    export function render(index) {
//        pct = index / pixelCount
//        edge = clamp(triangle(pct) + t1 * 4 - 2, 0, 1)  // Mirror space
//
//        h = edge * edge - .2  // Expand violets
//
//        v = triangle(edge)    // Doubles the frequency
//
//        hsv(h, 1, v)
//    }
}
