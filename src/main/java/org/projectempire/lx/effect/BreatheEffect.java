package org.projectempire.lx.effect;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Empire")
public class BreatheEffect extends LXEffect {
    //private TriangleLFO triangleLFO = new TriangleLFO(0, 50, 8000);
    public BoundedParameter depth = new BoundedParameter("Depth", 90, 0, 100);
    public BoundedParameter speed = new BoundedParameter("Speed", 6000, 1000, 10000);
    private SinLFO sinLFO = new SinLFO(0, depth, speed);

    public BreatheEffect(LX lx) {
        super(lx);
        //startModulator(triangleLFO);
        addParameter("Speed", this.speed);
        addParameter("Depth", this.depth);
        startModulator(sinLFO);
    }

    @Override
    protected void run(double deltaMs, double enabledAmount) {
        if (enabledAmount == 0) {
            return;
        }

        float dim = sinLFO.getValuef();
        //LX.log("dim: " + dim);
        for (int i = 0; i < this.colors.length; i++) {
            this.colors[i] = LXColor.multiply(colors[i], LXColor.gray(100 - dim), 256);
        }

    }
}
