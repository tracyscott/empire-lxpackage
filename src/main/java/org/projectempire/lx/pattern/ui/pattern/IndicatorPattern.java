package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

@LXCategory("Empire")
public class IndicatorPattern extends LXPattern {
    // TODO: implements UIDeviceControls<IndicatorPattern>
    public final ColorParameter color1 = new ColorParameter("Color1").setDescription("Color of the pattern");
    public final ColorParameter color2 = new ColorParameter("Color2").setDescription("Color of the pattern");
    public final ColorParameter color3 = new ColorParameter("Color3").setDescription("Color of the pattern");

    private double runtime = 0;
    private boolean firstRun = true;

    public IndicatorPattern(LX lx) {
        this(lx, LXColor.RED,
                LXColor.hsb(60, 100, 100), // Yellow
                LXColor.GREEN);
    }

    public IndicatorPattern(LX lx, int color1, int color2, int color3) {
        super(lx);
        this.color1.setColor(color1);
        this.color2.setColor(color2);
        this.color3.setColor(color3);
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
    }

    /**
     * pick one of the 3 colors, weighted towrds the last one.
     */
    private int pickColor() {
        int result = 0;
        int nextColor = (int) (Math.random() * 7);
        switch (nextColor) {
            case 0:
                result = color1.getColor();
                break;
            case 1:
            case 2:
                result = color2.getColor();
                break;
            default:
                result = color3.getColor();
                break;
        }
        return result;
    }

    private void init() {
        for (LXModel fixture : model.children) {
            int color = pickColor();
            for (int i = 0; i < fixture.points.length; i++) {
                colors[fixture.points[i].index] = color;
            }
        }

    }
    @Override
    protected void run(double deltaMs) {
        if (firstRun) {
            init();
            firstRun = false;
        }

        runtime += deltaMs;
        if (runtime >= 5000) {
            // Let's change something every 5 seconds
            runtime = 0;
            for (int i = 0; i < 5; i++) {
                LXModel fixture = model.children[(int)LXUtils.random(0, model.children.length)];
                int color = pickColor();
                for (int index = 0; index < fixture.points.length; index++) {
                    colors[fixture.points[index].index] = color;
                }
            }
        }
    }
}
