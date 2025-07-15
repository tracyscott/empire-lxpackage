package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponent;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

import java.util.ArrayList;

@LXCategory("Empire")
@LXComponent.Name("Indicator")
public class IndicatorPattern extends LXPattern {
    public final ColorParameter color1 = new ColorParameter("Color1").setDescription("Color of the pattern");
    public final ColorParameter color2 = new ColorParameter("Color2").setDescription("Color of the pattern");
    public final ColorParameter color3 = new ColorParameter("Color3").setDescription("Color of the pattern");

    private double runtime = 0;
    private boolean firstRun = true;
    /** A list of colors for each fixture in the model. */
    private ArrayList<ColorParameter> colorList = new ArrayList<>();

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
     * pick one of the 3 colors, weighted towards the last one.
     */
    private ColorParameter pickColor() {
        ColorParameter result;
        int nextColor = (int) (Math.random() * 7);
        switch (nextColor) {
            case 0:
                result = color1;
                break;
            case 1:
            case 2:
                result = color2;
                break;
            default:
                result = color3;
                break;
        }
        return result;
    }

    /**
     * Initialize the color list with a random color for each fixture in the model.
     */
    private void init() {
        colorList.clear();
        colorList.ensureCapacity(model.children.length);
        for (int i = 0; i < model.children.length; i++) {
            colorList.add(pickColor());
        }
    }

    @Override
    protected void run(double deltaMs) {
        if (firstRun) {
            init();
            firstRun = false;
        }

        // Update random button colors every 5 seconds
        runtime += deltaMs;
        if (runtime >= 5000) {
            // Let's change something every 5 seconds
            runtime = 0;
            for (int i = 0; i < 5; i++) {
                // Pick a random fixture and set its color
                int fixtureIndex = (int) LXUtils.random(0, model.children.length);
                if (fixtureIndex < colorList.size()) {
                    colorList.set(fixtureIndex, pickColor());
                }
            }
        }

        // Chromatik seems to want all pixes set on every call to run()
        for (int i = 0; i < model.children.length; i++) {
            if (i < colorList.size()) { // always true?
                LXModel fixture = model.children[i];
                int color = colorList.get(i).getColor();
                for (int index = 0; index < fixture.points.length; index++) {
                    colors[fixture.points[index].index] = color;
                }
            }
        }
    }

    @Override
    protected void onModelChanged(LXModel model) {
        super.onModelChanged(model);
        init();
    }
}
