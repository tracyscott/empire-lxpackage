package org.projectempire.lx.pattern.ui.pattern;

import heronarts.glx.ui.UI2dComponent;
import heronarts.glx.ui.UI2dContainer;
import heronarts.glx.ui.component.UIColorPicker;
import heronarts.glx.ui.component.UIIntegerBox;
import heronarts.glx.ui.component.UILabel;
import heronarts.glx.ui.component.UISlider;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXDynamicColor;
import heronarts.lx.color.LXSwatch;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;
import heronarts.lx.utils.LXUtils;

@LXCategory("Empire")
public class FillPattern extends LXPattern implements UIDeviceControls<FillPattern> {
    public enum ColorMode {
        FIXED("Fixed"),
        PALETTE("Palette");

        public final String label;

        ColorMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }

    }

    public final EnumParameter<ColorMode> colorMode =
            new EnumParameter<>("Color Mode", ColorMode.FIXED)
                    .setDescription("Which source the gradient selects colors from");

    public final ColorParameter color = new ColorParameter("Color").setDescription("Color of the pattern");
    public final DiscreteParameter paletteIndex =
            new DiscreteParameter("Index", 1, LXSwatch.MAX_COLORS + 1)
                    .setDescription("Which index at the palette to start from");
    public final CompoundParameter speed = new CompoundParameter("Speed", 1000, 500, 5000);
    private double totalTime = 0;

    public FillPattern(LX lx) {
        super(lx);
        this.speed.setUnits(LXParameter.Units.MILLISECONDS);
        this.color.setColor(LXColor.GREEN);
        addParameter("speed", this.speed);
        addParameter("color", this.color);
        addParameter("colorMode", this.colorMode);
        addParameter("paletteIndex", this.paletteIndex);
    }

    public LXDynamicColor getPaletteColor() {
        return this.lx.engine.palette.getSwatchColor(this.paletteIndex.getValuei() - 1);
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        super.onParameterChanged(parameter);
        if (parameter == this.color || parameter == this.paletteIndex) {
            this.totalTime = 0;
        }
    }

    @Override
    public void onActive() {
        super.onActive();
        for (int i = 0; i < this.model.points.length; i++) {
            this.colors[this.model.points[i].index] = LXColor.BLACK;
        }

        totalTime = 0;
    }

    @Override
    protected void run(double deltaMs) {
        totalTime = LXUtils.constrain(totalTime + deltaMs, 0, this.speed.getValue() + 1.0);
        int color;
        if (ColorMode.PALETTE == colorMode.getEnum()) {
            color = getPaletteColor().getColor();
        } else {
            color = this.color.getColor();
        }
        double fillPercent = 1d;
        if (totalTime < speed.getValue()) {
            fillPercent = LXUtils.constrain(totalTime / speed.getValue(), 0d, 1d);
        }
        int fillCount = (int) (fillPercent * this.model.points.length);
        for (int i = 0; i < fillCount; i++) {
            this.colors[this.model.points[i].index] = color;
        }
        for (int i = fillCount; i < this.model.points.length; i++) {
            this.colors[this.model.points[i].index] = LXColor.BLACK;
        }
        //LX.log("FillPattern: fillPercent " + fillPercent + " fillCount " + fillCount + " totalTime " + totalTime);
    }

    @Override
    public void buildDeviceControls(LXStudio.UI ui, UIDevice uiDevice, FillPattern fillPattern) {
        // TODO, this is copied from UISolidPattern.  It is missing speed.
        uiDevice.setLayout(UI2dContainer.Layout.VERTICAL);
        uiDevice.setChildSpacing(6.0F);
        uiDevice.setContentWidth(52.0F);
        UIIntegerBox paletteIndex;
        UILabel indexLabel;
        UIPaletteColor paletteColor;
        UIColorPicker colorPicker;
        UISlider hueSlider;
        UISlider satSlider;
        UISlider brightSlider;
        uiDevice.addChildren(this.newHorizontalSlider(fillPattern.speed),
                this.newDropMenu(fillPattern.colorMode),
                paletteColor = new UIPaletteColor(ui, fillPattern, 52.0F, 14.0F),
                paletteIndex = this.newIntegerBox(fillPattern.paletteIndex),
                indexLabel = this.controlLabel(ui, "Index"),
                colorPicker = (new UIColorPicker(0.0F, 0.0F, 52.0F, 14.0F, fillPattern.color))
                        .setCorner(UIColorPicker.Corner.TOP_RIGHT),
                hueSlider = this.newHorizontalSlider(fillPattern.color.hue),
                satSlider = this.newHorizontalSlider(fillPattern.color.saturation),
                brightSlider = this.newHorizontalSlider(fillPattern.color.brightness));
        LXParameterListener update = (p) -> {
            boolean isFixed = fillPattern.colorMode.getEnum() == ColorMode.FIXED;
            colorPicker.setVisible(isFixed);
            paletteColor.setVisible(!isFixed);
            paletteIndex.setVisible(!isFixed);
            indexLabel.setVisible(!isFixed);
            hueSlider.setVisible(isFixed);
            satSlider.setVisible(isFixed);
            brightSlider.setVisible(isFixed);
        };
        fillPattern.colorMode.addListener(update);
        update.onParameterChanged(null);

    }

    private static class UIPaletteColor extends UI2dComponent {
        private UIPaletteColor(LXStudio.UI ui, FillPattern fillPattern, float w, float h) {
            super(0.0F, 0.0F, w, h);
            this.setBorderColor(ui.theme.controlDisabledColor);
            this.setBackgroundColor(fillPattern.getPaletteColor().getColor());
            this.addLoopTask((deltaMs) -> this.setBackgroundColor(fillPattern.getPaletteColor().getColor()));
        }
    }
}
