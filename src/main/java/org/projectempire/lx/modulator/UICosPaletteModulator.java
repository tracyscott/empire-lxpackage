package org.projectempire.lx.modulator;

import heronarts.glx.event.MouseEvent;
import heronarts.glx.ui.UI;
import heronarts.glx.ui.UI2dComponent;
import heronarts.glx.ui.UI2dContainer;
import heronarts.glx.ui.component.UIButton;
import heronarts.glx.ui.component.UIColorControl;
import heronarts.glx.ui.component.UIKnob;
import heronarts.glx.ui.component.UILabel;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.color.LXColor;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.modulation.UIModulator;
import heronarts.lx.studio.ui.modulation.UIModulatorControls;

public class UICosPaletteModulator implements UIModulatorControls<CosPaletteModulator> {

    public void buildModulatorControls(LXStudio.UI ui, UIModulator uiModulator, final CosPaletteModulator cosColor) {
        final int ROW_HEIGHT = 40;
        final int ROW_SPACING = 4;
        
        uiModulator.setLayout(UI2dContainer.Layout.VERTICAL);
        uiModulator.setChildSpacing(ROW_SPACING);

        // First row: Basic controls
        UI2dContainer basicRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT, 4);
        
        // Create randomize button
        UIButton randomizeButton = new UIButton(0, 0, 50, 16) {
            @Override
            protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                super.onMousePressed(mouseEvent, mx, my);
                // Generate random values for all custom parameters
                cosColor.aR.setValue(Math.random());
                cosColor.aG.setValue(Math.random());
                cosColor.aB.setValue(Math.random());
                cosColor.bR.setValue(Math.random() * 2 - 1); // -1 to 1
                cosColor.bG.setValue(Math.random() * 2 - 1);
                cosColor.bB.setValue(Math.random() * 2 - 1);
                cosColor.cR.setValue(Math.random() * 4); // 0 to 4
                cosColor.cG.setValue(Math.random() * 4);
                cosColor.cB.setValue(Math.random() * 4);
                cosColor.dR.setValue(Math.random() * 2); // 0 to 2
                cosColor.dG.setValue(Math.random() * 2);
                cosColor.dB.setValue(Math.random() * 2);
                // Automatically switch to custom mode
                cosColor.customMode.setValue(true);
            }
        }.setLabel("Random");
        
        basicRow.addChildren(
                new UIKnob(cosColor.input).setLabel("Input"),
                new UIButton(0, 0, 50, 16).setParameter(cosColor.customMode).setLabel("Custom"),
                randomizeButton,
                new UIKnob(cosColor.which).setLabel("Preset")
        );

        // Second row: Output color display
        UI2dContainer colorRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT, 4);
        colorRow.addChildren(
                new UIColorControl(0, 0, cosColor.color),
                new UIKnob(cosColor.red).setLabel("R"),
                new UIKnob(cosColor.green).setLabel("G"),
                new UIKnob(cosColor.blue).setLabel("B")
        );

        // Third row: Palette preview
        UIPalettePreview previewBar = new UIPalettePreview(cosColor, 200, 20);
        UI2dContainer previewRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT, 4);
        previewRow.addChildren(previewBar);

        // Custom palette controls (Vector A - DC Offset)
        UI2dContainer aRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT, 4);
        aRow.addChildren(
                new UILabel(0, 0, 20, 12, "A:"),
                new UIKnob(cosColor.aR).setLabel("R"),
                new UIKnob(cosColor.aG).setLabel("G"),
                new UIKnob(cosColor.aB).setLabel("B")
        );

        // Vector B - Amplitude
        UI2dContainer bRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT, 4);
        bRow.addChildren(
                new UILabel(0, 0, 20, 12, "B:"),
                new UIKnob(cosColor.bR).setLabel("R"),
                new UIKnob(cosColor.bG).setLabel("G"),
                new UIKnob(cosColor.bB).setLabel("B")
        );

        // Vector C - Frequency
        UI2dContainer cRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT, 4);
        cRow.addChildren(
                new UILabel(0, 0, 20, 12, "C:"),
                new UIKnob(cosColor.cR).setLabel("R"),
                new UIKnob(cosColor.cG).setLabel("G"),
                new UIKnob(cosColor.cB).setLabel("B")
        );

        // Vector D - Phase
        UI2dContainer dRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT, 4);
        dRow.addChildren(
                new UILabel(0, 0, 20, 12, "D:"),
                new UIKnob(cosColor.dR).setLabel("R"),
                new UIKnob(cosColor.dG).setLabel("G"),
                new UIKnob(cosColor.dB).setLabel("B")
        );

        uiModulator.addChildren(basicRow, colorRow, previewRow, aRow, bRow, cRow, dRow);

        // Calculate total height: 3 always visible rows + 4 custom rows when visible
        // Height = (num_rows * ROW_HEIGHT) + ((num_rows - 1) * ROW_SPACING)
        final int BASE_ROWS = 3; // basicRow + colorRow + previewRow
        final int CUSTOM_ROWS = 4; // aRow, bRow, cRow, dRow
        
        // Show/hide custom controls based on mode and update height
        Runnable updateLayout = () -> {
            boolean isCustom = cosColor.customMode.getValueb();
            aRow.setVisible(isCustom);
            bRow.setVisible(isCustom);
            cRow.setVisible(isCustom);
            dRow.setVisible(isCustom);
            
            // Calculate height based on visible rows
            int visibleRows = BASE_ROWS + (isCustom ? CUSTOM_ROWS : 0);
            int totalHeight = (visibleRows * ROW_HEIGHT) + ((visibleRows - 1) * ROW_SPACING);
            uiModulator.setContentHeight(totalHeight);
        };
        
        cosColor.customMode.addListener((p) -> updateLayout.run());
        
        // Initialize visibility and height
        updateLayout.run();
        
        // Add listeners to update preview when parameters change
        cosColor.customMode.addListener((p) -> previewBar.redraw());
        cosColor.which.addListener((p) -> previewBar.redraw());
        cosColor.aR.addListener((p) -> previewBar.redraw());
        cosColor.aG.addListener((p) -> previewBar.redraw());
        cosColor.aB.addListener((p) -> previewBar.redraw());
        cosColor.bR.addListener((p) -> previewBar.redraw());
        cosColor.bG.addListener((p) -> previewBar.redraw());
        cosColor.bB.addListener((p) -> previewBar.redraw());
        cosColor.cR.addListener((p) -> previewBar.redraw());
        cosColor.cG.addListener((p) -> previewBar.redraw());
        cosColor.cB.addListener((p) -> previewBar.redraw());
        cosColor.dR.addListener((p) -> previewBar.redraw());
        cosColor.dG.addListener((p) -> previewBar.redraw());
        cosColor.dB.addListener((p) -> previewBar.redraw());
    }

    // Custom component to render palette preview
    private static class UIPalettePreview extends UI2dComponent {
        private final CosPaletteModulator modulator;
        private final int samples;
        
        public UIPalettePreview(CosPaletteModulator modulator, float w, float h) {
            super(0, 0, w, h);
            this.modulator = modulator;
            this.samples = 128;
            this.setBorderColor(0xFF666666);
        }
        
        @Override
        public void onDraw(UI ui, VGraphics vg) {
            // Draw border
            vg.beginPath();
            vg.rect(0, 0, this.width, this.height);
            vg.strokeColor(0xFF666666);
            vg.stroke();
            
            // Draw gradient samples
            float sampleWidth = this.width / samples;
            double[] tempRGB = new double[3];
            
            for (int i = 0; i < samples; i++) {
                double t = (double)i / (samples - 1); // 0 to 1
                
                // Get color at this position using modulator's logic
                if (modulator.customMode.getValueb()) {
                    // Use custom parameters
                    double[] a = {modulator.aR.getValuef(), modulator.aG.getValuef(), modulator.aB.getValuef()};
                    double[] b = {modulator.bR.getValuef(), modulator.bG.getValuef(), modulator.bB.getValuef()};
                    double[] c = {modulator.cR.getValuef(), modulator.cG.getValuef(), modulator.cB.getValuef()};
                    double[] d = {modulator.dR.getValuef(), modulator.dG.getValuef(), modulator.dB.getValuef()};
                    double[] result = CosPaletteModulator.palette(t, a, b, c, d);
                    tempRGB[0] = result[0];
                    tempRGB[1] = result[1];
                    tempRGB[2] = result[2];
                } else {
                    // Use preset palettes
                    CosPaletteModulator.paletteN(t, modulator.which.getValuei(), tempRGB);
                }
                
                // Convert to color and draw
                int color = LXColor.rgb(
                    (int)(tempRGB[0] * 255), 
                    (int)(tempRGB[1] * 255), 
                    (int)(tempRGB[2] * 255)
                );
                
                vg.beginPath();
                vg.rect(i * sampleWidth, 1, sampleWidth + 1, this.height - 2);
                vg.fillColor(color);
                vg.fill();
            }
        }
    }

}