package org.projectempire.lx.effect;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Empire")
public class PulseEffect extends LXEffect {
    private static final LXUtils.LookupTable.Sin SIN_TABLE = new LXUtils.LookupTable.Sin(360 * 2);
    public BoundedParameter width = new BoundedParameter("Width", 1f/3f, 0.2, 5);
    public BoundedParameter depth = new BoundedParameter("Depth", 90, 0, 100);
    public BoundedParameter speed = new BoundedParameter("Speed", 8000, 500, 10000);
    private SawLFO offset = new SawLFO(0, LX.TWO_PI, speed);

    public PulseEffect(LX lx) {
        super(lx);
        addParameter("Speed", this.speed);
        addParameter("Depth", this.depth);
        addParameter("Width", this.width);
        startModulator(offset);
    }

    @Override
    protected void run(double deltaMs, double enabledAmount) {
        final float patternOffset = this.offset.getValuef();
        for (LXPoint p: this.model.points) {
            int i = p.index;
            float radians = patternOffset + (p.zn * LX.TWO_PIf / width.getValuef());
            float b = ((SIN_TABLE.sin(radians) + 1f)/ 2f) * depth.getValuef();
            this.colors[i] = LXColor.multiply(this.colors[i],
                    LXColor.gray(100 - b), 256);
        }
    }
}
