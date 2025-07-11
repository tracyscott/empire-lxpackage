package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.LXPattern;

import java.util.List;

@LXCategory("Empire")
public class NasaPattern extends LXPattern {
    public NasaPattern(LX lx) {
        super(lx);
    }

    @Override
    protected void run(double deltaMs) {
        List<LXModel> models = model.children("nasa");
        for (LXModel model: models) {
            for (int i = 0; i < model.points.length; i++) {
                int color = LXColor.BLACK;
                int index = (i / 5) % 2;
                switch (index) {
                    case 0:
                        color = LXColor.BLUE;
                        break;
                    case 1:
                        color = LXColor.gray(75d);
                        break;
                }
                colors[model.points[i].index] = color;
            }
        }
    }
}
