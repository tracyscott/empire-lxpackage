package org.projectempire.lx.vstrip;

import org.projectempire.lx.wavetable.Wavetable;
import heronarts.lx.color.LXColor;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * VStripRender implements a variety of 1D rendering functions that
 * are local to the specified VStrip.
 */
public class VStripRender {
    static public void randomGray(int colors[], VStrip vStrip, LXColor.Blend blend) {
        Random r = new Random();
        for (LVPoint pt : vStrip.points) {
            int randomValue = r.nextInt(256);
            colors[pt.p.index] = LXColor.blend(colors[pt.p.index], LXColor.rgba(randomValue, randomValue, randomValue, 255), blend);
        }
    }

    static public void randomGrayBaseDepth(int colors[], VStrip vStrip, float pos, float width, LXColor.Blend blend, int min, int depth) {
        for (LVPoint pt : vStrip.points) {
            if (depth < 0)
                depth = 0;
            int randomDepth = ThreadLocalRandom.current().nextInt(depth);
            int value = min + randomDepth;
            if (value > 255) {
                value = 255;
            }
            if (pt.xpos > pos - width / 2.0f && pt.xpos < pos + width / 2.0f) {
                colors[pt.p.index] = LXColor.blend(colors[pt.p.index], LXColor.rgba(value, value, value, 255), blend);
            }
        }
    }

    static public void cosine(int colors[], VStrip vStrip, float head, float freq, float phase, float min, float depth, LXColor.Blend blend) {
        for (LVPoint pt : vStrip.points) {
            float value = ((float)Math.cos((double)freq * (head - pt.xpos) + phase) + 1.0f)/2.0f;
            value = min + depth * value;
            int color = (int)(value * 255f);
            colors[pt.p.index] = LXColor.blend(colors[pt.p.index], LXColor.rgba(color, color, color, 255), blend);
        }
    }

    static public void renderWavetable(int colors[], VStrip vStrip, Wavetable wt, float pos, float width, int clr, int swatch, float intensity, LXColor.Blend blend) {
        renderWavetable(colors, vStrip, wt, pos, width, clr, swatch, intensity, blend, false);
    }

    /**
     * Render a wavetable value at the specified position with the specified width.
     */
    static public void renderWavetable(int colors[], VStrip vStrip, Wavetable wt, float pos, float width, int clr, int swatch, float intensity, LXColor.Blend blend, boolean invert) {
        // LXUtil.lx().log("VStripRender renderWavetable pos=" + pos + " width=" + width + " minMax[0]=" + minMax[0] + " minMax[1]=" + minMax[1] + " intensity=" + intensity);
        for (LVPoint pt : vStrip.points) {
            float val = wt.getSample(pt.xpos - pos, width);
            // Palette translation?
            if (swatch != -1) {
                // clr = Colors.getQuantizedPaletteColor(LXUtil.lx(), swatch, val, null);
                clr = Colors.getParameterizedPaletteColor(LXUtil.lx(), swatch, val, null);
            }
            val = val * intensity;
            if (invert) {
                val = 1.0f - val;
            }
            colors[pt.p.index] = LXColor.blend(colors[pt.p.index],
                    LXColor.rgba((int)(((int)Colors.red(clr))*val),
                            (int)(((int)Colors.green(clr))*val),
                            (int)(((int)Colors.blue(clr))*val), 255), blend);
        }
    }


    static public void renderColor(int[] colors, VStrip vStrip, int red, int green, int blue, int alpha) {
        renderColor(colors, vStrip, LXColor.rgba(red, green, blue, alpha));
    }

    static public void renderColor(int[] colors, VStrip vStrip, int color) {
        renderColor(colors, vStrip, color, 1.0f);
    }

    static public void renderColor(int[] colors, VStrip vStrip, int color, float maxValue) {
        for (LVPoint point: vStrip.points) {
            colors[point.p.index] = LXColor.rgba(
                    (int)(Colors.red(color) * maxValue), (int)(Colors.green(color) * maxValue), (int)(Colors.blue(color) * maxValue), 255);
        }
    }
}
