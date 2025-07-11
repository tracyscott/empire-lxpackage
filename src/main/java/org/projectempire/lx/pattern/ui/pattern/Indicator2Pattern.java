package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.pattern.LXPattern;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@LXCategory("Empire")
public class Indicator2Pattern extends LXPattern {
    private static final int NUM_CONTROLS = 25;

    /**
     * A triangle wave that goes from 0 to 100 over 10 seconds
     */
    final TriangleLFO triangleLFO = startModulator(new TriangleLFO(0, 100, TimeUnit.SECONDS.toMillis(10)));

    private LXModel[] models = new LXModel[NUM_CONTROLS];
    private int[] hues = new int[NUM_CONTROLS];

    public Indicator2Pattern(LX lx) {
        super(lx);
        initArrays();
    }

    @Override
    protected void onModelChanged(LXModel model) {
        super.onModelChanged(model);
        initArrays();
    }

    public void run(double deltaMs) {
        if (triangleLFO.loop()) {
            initArrays();
        }
        for (int i = 0; i < models.length; i++) {
            LXModel model = models[i];
            int hsb = LX.hsb(hues[i], 100, triangleLFO.getValuef());
            for (int j = 0; j < model.points.length; j++) {
                colors[model.points[j].index] = hsb;
            }
        }
    }

    private void initArrays() {
        Set<Integer> selectedIndexes = new HashSet<>(NUM_CONTROLS);
        while (selectedIndexes.size() < NUM_CONTROLS) {
            int index = (int) (Math.random() * model.children.length);
            if (!selectedIndexes.contains(index)) {
                selectedIndexes.add(index);
            }
        }
        int i = 0;
        for (int index : selectedIndexes) {
            models[i] = model.children[index];
            hues[i++] = pickHue();
        }
    }

    /**
     * pick one of the 3 colors, weighted towards the last one.
     */
    private int pickHue() {
        int result = 0;
        int nextColor = (int) (Math.random() * 8);
        switch (nextColor) {
            case 0:
                result = 0; // Red
                break;
            case 1:
            case 2:
                result = 60; // Yellow
                break;
            default:
                result = 120; // Green
                break;
        }
        return result;
    }

}
