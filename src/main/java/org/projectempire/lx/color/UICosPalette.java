package org.projectempire.lx.color;

import heronarts.glx.event.MouseEvent;
import heronarts.glx.ui.UI;
import heronarts.glx.ui.UI2dComponent;
import heronarts.glx.ui.UI2dContainer;
import heronarts.glx.ui.component.UIButton;
import heronarts.glx.ui.component.UICollapsibleSection;
import heronarts.glx.ui.component.UIKnob;
import heronarts.glx.ui.component.UILabel;
import heronarts.glx.ui.component.UITextBox;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.color.LXColor;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.device.UIControls;

import java.util.ArrayList;
import java.util.List;

public class UICosPalette extends UICollapsibleSection implements UIControls {
    
    private static final int ROW_HEIGHT = 24;
    private static final int KNOB_SIZE = 16;
    private static final int BUTTON_WIDTH = 32;
    private static final int PREVIEW_HEIGHT = 24;
    private static final int COLOR_DOT_SIZE = 16;
    private static final int SWATCH_ROW_HEIGHT = 24;
    
    private final CosPalette palette;
    private final LXStudio.UI lxUI;
    
    private UI2dContainer swatchesContainer;
    private UIPalettePreview previewBar;
    private List<UISwatchControls> swatchControlsList = new ArrayList<>();
    private UI2dContainer controlsSection;
    private boolean editMode = true; // Start in edit mode for testing

    public UICosPalette(LXStudio lx, UI ui, CosPalette palette, float w) {
        super(ui, 0, 0, w, 0);
        this.palette = palette;
        this.lxUI = lx.ui;
        
        setTitle("Cos Palette");
        setLayout(Layout.VERTICAL, 4);
        
        buildUI();
        updateSwatchesUI();
        
        // Listen for swatch changes
        palette.swatchIndex.addListener((p) -> updateSwatchesUI());
        
        // Listen for swatch list changes and rebuild UI
        palette.swatchesChanged.addListener((p) -> updateSwatchesUI());
    }
    
    private void buildUI() {
        // Top header with main palette preview and controls
        UI2dContainer headerRow = UI2dContainer.newHorizontalContainer(PREVIEW_HEIGHT, 2);
        
        // Main palette preview (larger)
        previewBar = new UIPalettePreview(palette, getContentWidth() - 100, PREVIEW_HEIGHT);
        
        // Control buttons in compact layout
        UIButton addButton = new UIButton(0, 0, 20, PREVIEW_HEIGHT) {
            @Override
            protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                super.onMousePressed(mouseEvent, mx, my);
                palette.addSwatch();
            }
        }.setLabel("+");
        
        UIButton randomButton = new UIButton(0, 0, 20, PREVIEW_HEIGHT) {
            @Override
            protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                super.onMousePressed(mouseEvent, mx, my);
                randomizeActiveSwatch();
            }
        }.setLabel("R").setMomentary(true);
        
        UIButton editButton = new UIButton(0, 0, 20, PREVIEW_HEIGHT) {
            @Override
            protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                super.onMousePressed(mouseEvent, mx, my);
                toggleEditMode();
            }
        }.setLabel("E");
        
        headerRow.addChildren(previewBar, addButton, randomButton, editButton);
        
        // Swatches list container - this will be populated by updateSwatchesUI()
        swatchesContainer = new UI2dContainer(0, 0, getContentWidth(), 0);
        swatchesContainer.setLayout(Layout.VERTICAL, 1);
        
        // Controls section (initially hidden, shown in edit mode)
        UI2dContainer controlsSection = new UI2dContainer(0, 0, getContentWidth(), 200);
        controlsSection.setLayout(Layout.VERTICAL, 2);
        
        // Vector controls in 4 separate rows with better spacing
        UI2dContainer vectorGrid = new UI2dContainer(0, 0, getContentWidth(), 0);
        vectorGrid.setLayout(Layout.VERTICAL, 4);
        
        // Row 1: R (Red component)
        UI2dContainer rRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT + 18, 8);
        rRow.addChildren(
                new UIKnob(palette.aR).setLabel("Amp"),
                new UIKnob(palette.bR).setLabel("DC"),
                new UIKnob(palette.cR).setLabel("Freq"),
                new UIKnob(palette.dR).setLabel("Phase")
        );
        
        // Row 2: G (Green component)
        UI2dContainer gRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT + 18, 8);
        gRow.addChildren(
                new UIKnob(palette.aG).setLabel("Amp"),
                new UIKnob(palette.bG).setLabel("DC"),
                new UIKnob(palette.cG).setLabel("Freq"),
                new UIKnob(palette.dG).setLabel("Phase")
        );
        
        // Row 3: B (Blue component)
        UI2dContainer bRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT + 18, 8);
        bRow.addChildren(
                new UIKnob(palette.aB).setLabel("Amp"),
                new UIKnob(palette.bB).setLabel("DC"),
                new UIKnob(palette.cB).setLabel("Freq"),
                new UIKnob(palette.dB).setLabel("Phase")
        );
        
        vectorGrid.addChildren(rRow, gRow, bRow);
        
        controlsSection.addChildren(vectorGrid);
        controlsSection.setVisible(true); // Start visible for testing
        
        addChildren(headerRow, swatchesContainer, controlsSection);
        
        // Store reference to controls for toggling
        this.controlsSection = controlsSection;
        
        // Add parameter listeners to update preview
        addParameterListeners();
    }
    
    private void addParameterListeners() {
        Runnable updatePreview = () -> {
            previewBar.redraw();
            // Also update individual swatch previews
            for (UISwatchControls swatchControls : swatchControlsList) {
                swatchControls.updatePreview();
            }
        };
        
        palette.swatchIndex.addListener((p) -> updatePreview.run());
        palette.aR.addListener((p) -> updatePreview.run());
        palette.aG.addListener((p) -> updatePreview.run());
        palette.aB.addListener((p) -> updatePreview.run());
        palette.bR.addListener((p) -> updatePreview.run());
        palette.bG.addListener((p) -> updatePreview.run());
        palette.bB.addListener((p) -> updatePreview.run());
        palette.cR.addListener((p) -> updatePreview.run());
        palette.cG.addListener((p) -> updatePreview.run());
        palette.cB.addListener((p) -> updatePreview.run());
        palette.dR.addListener((p) -> updatePreview.run());
        palette.dG.addListener((p) -> updatePreview.run());
        palette.dB.addListener((p) -> updatePreview.run());
        
    }
    
    private void toggleEditMode() {
        editMode = !editMode;
        controlsSection.setVisible(editMode);
        
        // Rebuild swatch UI to show/hide remove buttons
        updateSwatchesUI();
        
        redraw();
    }
    
    private void randomizeActiveSwatch() {
        // Update the top-level palette parameters instead of swatch directly
        // This will trigger the parameter listeners and update both UI and swatch
        double range = 3.13840734641021 * 2; // Full range from -π to +π
        palette.aR.setValue(Math.random() * range - 3.13840734641021);
        palette.aG.setValue(Math.random() * range - 3.13840734641021);
        palette.aB.setValue(Math.random() * range - 3.13840734641021);
        palette.bR.setValue(Math.random() * range - 3.13840734641021);
        palette.bG.setValue(Math.random() * range - 3.13840734641021);
        palette.bB.setValue(Math.random() * range - 3.13840734641021);
        palette.cR.setValue(Math.random() * range - 3.13840734641021);
        palette.cG.setValue(Math.random() * range - 3.13840734641021);
        palette.cB.setValue(Math.random() * range - 3.13840734641021);
        palette.dR.setValue(Math.random() * range - 3.13840734641021);
        palette.dG.setValue(Math.random() * range - 3.13840734641021);
        palette.dB.setValue(Math.random() * range - 3.13840734641021);
    }
    
    
    private void updateSwatchesUI() {
        // Clear existing swatch controls
        swatchesContainer.removeAllChildren();
        swatchControlsList.clear();
        
        // Add UI for each swatch
        List<CosPalette.CosPaletteSwatch> swatches = palette.getSwatches();
        for (int i = 0; i < swatches.size(); i++) {
            CosPalette.CosPaletteSwatch swatch = swatches.get(i);
            UISwatchControls swatchUI = new UISwatchControls(i, swatch);
            swatchControlsList.add(swatchUI);
            swatchesContainer.addChildren(swatchUI);
        }
        
    }
    
    // Individual swatch control UI - compact style like reference
    private class UISwatchControls extends UI2dContainer {
        private final int swatchIndex;
        private final CosPalette.CosPaletteSwatch swatch;
        private UIMiniPalettePreview miniPreview;
        
        public UISwatchControls(int index, CosPalette.CosPaletteSwatch swatch) {
            super(0, 0, UICosPalette.this.getContentWidth() - 8, SWATCH_ROW_HEIGHT);
            this.swatchIndex = index;
            this.swatch = swatch;
            
            setLayout(Layout.HORIZONTAL, 4);
            
            // Swatch label - click to select swatch (40% smaller: 120 * 0.6 = 72)
            UITextBox labelBox = new UITextBox(0, 0, 72, SWATCH_ROW_HEIGHT, swatch.label) {
                @Override
                protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                    super.onMousePressed(mouseEvent, mx, my);
                    palette.setSwatch(swatchIndex);
                }
            };
            
            // Mini palette preview
            miniPreview = new UIMiniPalettePreview(swatch, 100, SWATCH_ROW_HEIGHT - 4);
            
            // Remove button (only visible in edit mode or on hover)
            UIButton removeButton = new UIButton(0, 0, 16, SWATCH_ROW_HEIGHT) {
                @Override
                protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                    super.onMousePressed(mouseEvent, mx, my);
                    if (palette.getSwatches().size() > 1) {
                        palette.removeSwatch(swatch);
                    }
                }
            }.setLabel("×");
            
            addChildren(labelBox, miniPreview);
            if (editMode) {
                addChildren(removeButton);
            }
        }
        
        
        public void updatePreview() {
            miniPreview.redraw();
        }
        
    }
    
    // Mini palette preview showing the full gradient for a swatch
    private static class UIMiniPalettePreview extends UI2dComponent {
        private final CosPalette.CosPaletteSwatch swatch;
        private final int samples = 32; // Fewer samples for smaller preview
        
        public UIMiniPalettePreview(CosPalette.CosPaletteSwatch swatch, float w, float h) {
            super(0, 0, w, h);
            this.swatch = swatch;
            setBorderColor(0xFF666666);
        }
        
        @Override
        public void onDraw(UI ui, VGraphics vg) {
            // Draw border
            vg.beginPath();
            vg.rect(0, 0, this.width, this.height);
            vg.strokeColor(0xFF666666);
            vg.strokeWidth(1);
            vg.stroke();
            
            // Draw gradient samples using THIS swatch's parameters
            float sampleWidth = this.width / samples;
            
            // Get swatch parameters
            double[] a = {swatch.aR.getValue(), swatch.aG.getValue(), swatch.aB.getValue()};
            double[] b = {swatch.bR.getValue(), swatch.bG.getValue(), swatch.bB.getValue()};
            double[] c = {swatch.cR.getValue(), swatch.cG.getValue(), swatch.cB.getValue()};
            double[] d = {swatch.dR.getValue(), swatch.dG.getValue(), swatch.dB.getValue()};
            
            for (int i = 0; i < samples; i++) {
                double t = (double)i / (samples - 1); // 0 to 1
                
                double[] rgb = org.projectempire.lx.modulator.CosPaletteModulator.palette(t, a, b, c, d);
                int color = LXColor.rgb(
                    (int)(Math.max(0, Math.min(255, rgb[0] * 255))), 
                    (int)(Math.max(0, Math.min(255, rgb[1] * 255))), 
                    (int)(Math.max(0, Math.min(255, rgb[2] * 255)))
                );
                
                vg.beginPath();
                vg.rect(i * sampleWidth, 1, sampleWidth + 1, this.height - 2);
                vg.fillColor(color);
                vg.fill();
            }
        }
    }
    
    // Main palette preview component
    private static class UIPalettePreview extends UI2dComponent {
        private final CosPalette palette;
        private final int samples = 128;
        
        public UIPalettePreview(CosPalette palette, float w, float h) {
            super(0, 0, w, h);
            this.palette = palette;
            setBorderColor(0xFF666666);
        }
        
        @Override
        public void onDraw(UI ui, VGraphics vg) {
            // Draw border
            vg.beginPath();
            vg.rect(0, 0, this.width, this.height);
            vg.strokeColor(0xFF666666);
            vg.stroke();
            
            // Draw gradient samples using current active swatch
            float sampleWidth = this.width / samples;
            
            for (int i = 0; i < samples; i++) {
                double t = (double)i / (samples - 1); // 0 to 1
                
                double[] rgb = palette.getColor(t);
                int color = LXColor.rgb(
                    (int)(rgb[0] * 255), 
                    (int)(rgb[1] * 255), 
                    (int)(rgb[2] * 255)
                );
                
                vg.beginPath();
                vg.rect(i * sampleWidth, 1, sampleWidth + 1, this.height - 2);
                vg.fillColor(color);
                vg.fill();
            }
        }
    }
}