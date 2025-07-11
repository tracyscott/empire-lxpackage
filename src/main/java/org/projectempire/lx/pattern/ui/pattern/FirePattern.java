package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

import java.util.*;

@LXCategory("Nozzle")
public class FirePattern extends LXPattern {
    public final ColorParameter color = new ColorParameter("Color").setDescription("Color of the pattern");
    public final CompoundParameter burndown = new CompoundParameter("burndown", 500, 400, 600);

    protected int[] fireColors = new int[100];
    protected int[][] fireIndexes = new int[7][360];

    private double waitMs = 0d;

    protected  FirePattern(LX lx, int color) {
        super(lx);
        this.color.setColor(color);
        addParameter("color", this.color);
        addParameter(burndown.getLabel(), burndown);
        for (int[] fireIndex : fireIndexes) {
            Arrays.fill(fireIndex, 0);
        }
        Arrays.fill(fireColors, LXColor.BLACK);
        setColorLookup();
    }

    public FirePattern(LX lx) {
        this(lx, LXColor.RED);
    }

    @Override
    public void onActive() {
        super.onActive();
        for (int[] fireIndex : fireIndexes) {
            Arrays.fill(fireIndex, 0);
        };
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        super.onParameterChanged(parameter);
        if (color == parameter) {
            setColorLookup();
        }
    }

    protected void setColorLookup() {
        List<Integer> fireColorList = new ArrayList<>(256);
        float h1 = color.hue.getValuef();
        float h2 = h1 + 30;
        float h3 = h1 + 60;
        for (int i = 0; i < 64; i++) {
          float h = LXUtils.lerpf(h1, h2, i/63f);
          float s = 100;
          float l = LXUtils.lerpf(0, 100, i/63f);
          fireColorList.add(LXColor.hsb(h, s, l));
        }
        for (int i = 0; i < 64; i++) {
          float h = LXUtils.lerpf(h2, h3, i/63f);
          float s = 100;
          float l = 100;
          fireColorList.add(LXColor.hsb(h, s, l));
        }
        for (int i = 0; i < 64; i++) {
            float h = h3;
            float s = LXUtils.lerpf(100, 50, i/63f);
            float l = 100;
            fireColorList.add(LXColor.hsb(h,s,l));
        }
//        ColorParameter p = new ColorParameter("");
//        float h1 = this.color.hue.getValuef();
//        float h2 = h1 + 60;
//        // The idea: Black to Red (hue) to yellow (hue+60) to white
//        // Brightness from 0 t0 40%
//        for (int i = 0; i < 50; i++) {
//            float b = LXUtils.lerpf(0, 40, i/49f);
//            fireColors[i] = LXColor.hsb(h1, 100, b);
//            p.setColor(fireColors[i]);
//            LX.log(String.format("%.3f, %.3f, %.3f, %s", h1, 100.0, b, p.getHexString()));
//        }
//        // Hue from h1 to h2, brightness 40 to 60 %
//        for (int i = 0 ; i < 50; i++) {
//            float h = LXUtils.lerpf(h1, h2, (i+1)/50f);
//            float b = LXUtils.lerpf(40, 60, (i+1)/50f);
//            fireColors[50+i] = LXColor.hsb(h, 100, b);
//            p.setColor(fireColors[50+i]);
//            LX.log(String.format("%.3f, %.3f, %.3f, %s", h1, 100.0, b, p.getHexString()));
//        }
        // TODO: better pallet
        int c;
        int i = 0;
        c = LXColor.rgb(0,0,0); //=> 0,0,0
        fireColors[i++] = c;
        c = LXColor.rgb(5,0,0); //=> 0,100,2
        fireColors[i++] = c;
        c = LXColor.rgb(10,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(14,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(19,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(23,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(27,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(31,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(35,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(39,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(43,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(47,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(51,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(55,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(59,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(63,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(67,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(71,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(75,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(79,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(83,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(88,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(92,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(96,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(100,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(104,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(108,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(112,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(116,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(121,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(125,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(129,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(133,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(137,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(141,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(145,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(149,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(153,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(157,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(161,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(165,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(169,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(173,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(177,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(181,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(185,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(190,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(194,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(198,0,0);
        fireColors[i++] = c;
        c = LXColor.rgb(202,0,0); //=> 0, 100, 79.2
        fireColors[i++] = c;
        c = LXColor.rgb(205,2,0); //=> 1, 100, 80.4
        fireColors[i++] = c;
        c = LXColor.rgb(207,6,0);
        fireColors[i++] = c;
        c = LXColor.rgb(209,10,0);
        fireColors[i++] = c;
        c = LXColor.rgb(211,14,0);
        fireColors[i++] = c;
        c = LXColor.rgb(213,18,0);
        fireColors[i++] = c;
        c = LXColor.rgb(215,22,0);
        fireColors[i++] = c;
        c = LXColor.rgb(217,26,0);
        fireColors[i++] = c;
        c = LXColor.rgb(219,30,0);
        fireColors[i++] = c;
        c = LXColor.rgb(221,35,0);
        fireColors[i++] = c;
        c = LXColor.rgb(223,39,0);
        fireColors[i++] = c;
        c = LXColor.rgb(225,43,0);
        fireColors[i++] = c;
        c = LXColor.rgb(227,47,0);
        fireColors[i++] = c;
        c = LXColor.rgb(229,51,0);
        fireColors[i++] = c;
        c = LXColor.rgb(231,55,0);
        fireColors[i++] = c;
        c = LXColor.rgb(233,59,0);
        fireColors[i++] = c;
        c = LXColor.rgb(235,63,0);
        fireColors[i++] = c;
        c = LXColor.rgb(237,67,0);
        fireColors[i++] = c;
        c = LXColor.rgb(239,71,0);
        fireColors[i++] = c;
        c = LXColor.rgb(241,75,0);
        fireColors[i++] = c;
        c = LXColor.rgb(243,79,0);
        fireColors[i++] = c;
        c = LXColor.rgb(245,83,0);
        fireColors[i++] = c;
        c = LXColor.rgb(248,88,0);
        fireColors[i++] = c;
        c = LXColor.rgb(250,92,0); //=> 22, 100, 98
        fireColors[i++] = c;
        c = LXColor.rgb(252,96,0);
        fireColors[i++] = c;
        c = LXColor.rgb(254,100,0); //=> 24, 100, 99.6
        fireColors[i++] = c;
        c = LXColor.rgb(255,105,1); //=> 25, 99.6	100
        fireColors[i++] = c;
        c = LXColor.rgb(255,111,3); //=> 26, 98.8, 100
        fireColors[i++] = c;
        c = LXColor.rgb(255,117,5);
        fireColors[i++] = c;
        c = LXColor.rgb(255,123,7);
        fireColors[i++] = c;
        c = LXColor.rgb(255,130,9);
        fireColors[i++] = c;
        c = LXColor.rgb(255,136,11);
        fireColors[i++] = c;
        c = LXColor.rgb(255,142,13);
        fireColors[i++] = c;
        c = LXColor.rgb(255,148,15);
        fireColors[i++] = c;
        c = LXColor.rgb(255,154,17);
        fireColors[i++] = c;
        c = LXColor.rgb(255,160,19);
        fireColors[i++] = c;
        c = LXColor.rgb(255,166,21);
        fireColors[i++] = c;
        c = LXColor.rgb(255,172,23);
        fireColors[i++] = c;
        c = LXColor.rgb(255,179,25);
        fireColors[i++] = c;
        c = LXColor.rgb(255,185,27);
        fireColors[i++] = c;
        c = LXColor.rgb(255,191,29);
        fireColors[i++] = c;
        c = LXColor.rgb(255,197,31);
        fireColors[i++] = c;
        c = LXColor.rgb(255,203,33);
        fireColors[i++] = c;
        c = LXColor.rgb(255,209,35);
        fireColors[i++] = c;
        c = LXColor.rgb(255,215,37);
        fireColors[i++] = c;
        c = LXColor.rgb(255,221,39);
        fireColors[i++] = c;
        c = LXColor.rgb(255,227,41);
        fireColors[i++] = c;
        c = LXColor.rgb(255,234,44);
        fireColors[i++] = c;
        c = LXColor.rgb(255,240,46);
        fireColors[i++] = c;
        c = LXColor.rgb(255,246,48);
        fireColors[i++] = c;
        c = LXColor.rgb(255,252,50); // => 59, 80.4, 100
        fireColors[i++] = c;

        fireColors = fireColorList.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    protected void run(double deltaMs) {
        waitMs += deltaMs;
        if (waitMs < (1000.0/15.0)) {
            // 15 fps
            return;
        }
        waitMs = 0;

        Set<Integer> hVals = new TreeSet<>();
        final int h = fireIndexes.length;
        final int w = 360;
        // Random assign first row
        for (int z = 0; z < w; z++) {
            fireIndexes[h-1][z] = (int)LXUtils.random(0, fireColors.length-1);
        }
        // average existing
        for (int y = 0; y < h - 1; y++) {
            for (int z = 0; z < w; z++) {
                int c1 = fireIndexes[(y + 1) % h][(z - 1 + w) % w];
                int c2 = fireIndexes[(y + 1) % h][(z) % w];
                int c3 = fireIndexes[(y + 1) % h][(z + 1) % w];
                int c4 = fireIndexes[(y + 2) % h][(z) % w];
                fireIndexes[y][z] = ((c1 + c2 + c3 + c4)
                                * 100) / (int)burndown.getValuef();  // TODO Change cooling
            }
        }
        for (LXPoint p: model.points) {
            int z = LXUtils.lerpi(0, h-2, p.zn);
            int x = (int)Math.toDegrees(p.theta)/2;
            colors[p.index] = fireColors[fireIndexes[z][x]];
            hVals.add(z);
        }
        //LX.log(hVals.toString());
    }
}
