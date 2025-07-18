package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponent;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.LXPattern;

/**
 * A pattern that alternates between blue and white colors.
 */
@LXCategory("Empire")
@LXComponent.Name("NASA")
public class NasaPattern extends LXPattern {
    private static final int GRAY = LXColor.gray(75d);

    public NasaPattern(LX lx) {
        super(lx);
    }

    @Override
    protected void run(double deltaMs) {
        for (LXModel childModel : model.children) {
            for (int i = 0; i < childModel.points.length; i++) {
                final int index = (i / 5) % 2;
                final int color = (0 == index) ? LXColor.BLUE : GRAY;
                colors[childModel.points[i].index] = color;
            }
        }
    }
}
