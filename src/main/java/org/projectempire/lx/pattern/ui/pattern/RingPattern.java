package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXDynamicColor;
import heronarts.lx.color.LXSwatch;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

import java.util.ArrayList;
import java.util.List;

@LXCategory("Nozzle")
public class RingPattern extends LXPattern {
    public enum ColorMode {
        FIXED("Fixed"),
        PALETTE("Palette");

        public final String label;

        private ColorMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    private static final int NUM_BANDS = 5;
    private static final LXUtils.LookupTable.Sin SIN_TABLE = new LXUtils.LookupTable.Sin(500);
    public final EnumParameter<ColorMode> colorMode =
            new EnumParameter<ColorMode>("Color Mode", ColorMode.FIXED)
                    .setDescription("Which source the gradient selects colors from");
    public final ColorParameter color = new ColorParameter("Color").setDescription("Color of the pattern");
    public final CompoundParameter speed = new CompoundParameter("Speed", 1000, 500, 5000);
    public final DiscreteParameter paletteIndex =
            new DiscreteParameter("Index", 1, LXSwatch.MAX_COLORS + 1)
                    .setDescription("Which index at the palette to start from");

    final SawLFO rotation = startModulator(new SawLFO(0, 360, speed));

    public RingPattern(LX lx, int color) {
        super(lx);
        this.color.setColor(color);
        this.speed.setUnits(LXParameter.Units.MILLISECONDS); // TODO: Hertz?
        addParameter("speed", this.speed);
        addParameter("color", this.color);
        addParameter("colorMode", this.colorMode);
        addParameter("paletteIndex", this.paletteIndex);
    }

    public RingPattern(LX lx) {
        this(lx, LXColor.RED);
    }

    public LXDynamicColor getPaletteColor() {
        return this.lx.engine.palette.getSwatchColor(this.paletteIndex.getValuei() - 1);
    }

    @Override
    protected void run(double deltaMs) {
        float h;
        float s;
        switch (this.colorMode.getEnum()) {
            case PALETTE:
                h = (float)getPaletteColor().getHue();
                s = (float)getPaletteColor().getSaturation();
                break;
            case FIXED:
            default:
                h = color.hue.getValuef();
                s = color.saturation.getValuef();
                break;
        }
        double rot = rotation.getValue();
        int sign = 1;
        float offset = (float) (rot * (Math.PI / 180));
        // TODO: access points through model.children of model.childDict so we know which ring we are in
        List<LXModel> models = new ArrayList<>(this.model.children("nozzle"));
        models.addAll(this.model.children("puck"));
        for (LXModel model : models) {
            for (LXPoint p : model.points) {
                //int band = (int)(p.zn / (1f / (float)(NUM_BANDS - 1)));
                //int sign = (band % 2 == 0) ? -1 : 1;
                float b = Math.abs(SIN_TABLE.sin((sign * offset) + p.theta));
                //b = Math.abs(LXUtils.trif((sign * offset) + (p.theta / LX.TWO_PIf)));
                colors[p.index] = LX.hsb(h, s, b * 100);
            }
            sign = -sign;
        }
    }
}
