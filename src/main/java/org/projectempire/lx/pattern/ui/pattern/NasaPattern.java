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
        for (LXModel model : model.children) {
            for (int i = 0; i < model.points.length; i++) {
                int color = LXColor.BLACK;
                int index = (i / 5) % 2;
                switch (index) {
                    case 0:
                        color = LXColor.BLUE;
                        break;
                    case 1:
                        color = GRAY;
                        break;
                }
                colors[model.points[i].index] = color;
            }
        }
    }
}
