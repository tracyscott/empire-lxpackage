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
import heronarts.lx.parameter.LXParameterListener;

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
    private int selectedSwatchIndex = -1; // -1 means no swatch selected
    
    // Dynamic knob management
    private List<UIKnob> currentKnobs = new ArrayList<>();
    private UI2dContainer vectorGrid;
    private CosPalette.CosPaletteSwatch currentListeningSwatch = null;
    private final LXParameterListener swatchPreviewUpdater = (parameter) -> updateSelectedSwatchPreview();
    
    // Fixed knob references for reuse
    private UIKnob[] fixedKnobs = new UIKnob[12]; // 12 knobs total (3 colors x 4 parameters)

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
        vectorGrid = new UI2dContainer(0, 0, getContentWidth(), 0);
        vectorGrid.setLayout(Layout.VERTICAL, 4);
        
        // Initialize with global palette knobs
        createKnobsForGlobalPalette();
        
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
    
    private void disposeCurrentKnobs() {
        // Remove parameter listeners from current swatch if any
        if (currentListeningSwatch != null) {
            currentListeningSwatch.aR.removeListener(swatchPreviewUpdater);
            currentListeningSwatch.aG.removeListener(swatchPreviewUpdater);
            currentListeningSwatch.aB.removeListener(swatchPreviewUpdater);
            currentListeningSwatch.bR.removeListener(swatchPreviewUpdater);
            currentListeningSwatch.bG.removeListener(swatchPreviewUpdater);
            currentListeningSwatch.bB.removeListener(swatchPreviewUpdater);
            currentListeningSwatch.cR.removeListener(swatchPreviewUpdater);
            currentListeningSwatch.cG.removeListener(swatchPreviewUpdater);
            currentListeningSwatch.cB.removeListener(swatchPreviewUpdater);
            currentListeningSwatch.dR.removeListener(swatchPreviewUpdater);
            currentListeningSwatch.dG.removeListener(swatchPreviewUpdater);
            currentListeningSwatch.dB.removeListener(swatchPreviewUpdater);
            currentListeningSwatch = null;
        }
        
        // Remove all current knobs from the vector grid
        vectorGrid.removeAllChildren();
        // Clear the knobs list
        currentKnobs.clear();
    }
    
    private void createKnobsForGlobalPalette() {
        disposeCurrentKnobs();
        
        // Row 1: R (Red component)
        UI2dContainer rRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT + 18, 4);
        UIKnob aRKnob = (UIKnob) new UIKnob(palette.aR).setLabel("Amp");
        UIKnob bRKnob = (UIKnob) new UIKnob(palette.bR).setLabel("DC");
        UIKnob cRKnob = (UIKnob) new UIKnob(palette.cR).setLabel("Freq");
        UIKnob dRKnob = (UIKnob) new UIKnob(palette.dR).setLabel("Phase");
        rRow.addChildren(aRKnob, bRKnob, cRKnob, dRKnob);
        currentKnobs.addAll(List.of(aRKnob, bRKnob, cRKnob, dRKnob));
        
        // Row 2: G (Green component)
        UI2dContainer gRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT + 18, 4);
        UIKnob aGKnob = (UIKnob) new UIKnob(palette.aG).setLabel("Amp");
        UIKnob bGKnob = (UIKnob) new UIKnob(palette.bG).setLabel("DC");
        UIKnob cGKnob = (UIKnob) new UIKnob(palette.cG).setLabel("Freq");
        UIKnob dGKnob = (UIKnob) new UIKnob(palette.dG).setLabel("Phase");
        gRow.addChildren(aGKnob, bGKnob, cGKnob, dGKnob);
        currentKnobs.addAll(List.of(aGKnob, bGKnob, cGKnob, dGKnob));
        
        // Row 3: B (Blue component)
        UI2dContainer bRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT + 18, 4);
        UIKnob aBKnob = (UIKnob) new UIKnob(palette.aB).setLabel("Amp");
        UIKnob bBKnob = (UIKnob) new UIKnob(palette.bB).setLabel("DC");
        UIKnob cBKnob = (UIKnob) new UIKnob(palette.cB).setLabel("Freq");
        UIKnob dBKnob = (UIKnob) new UIKnob(palette.dB).setLabel("Phase");
        bRow.addChildren(aBKnob, bBKnob, cBKnob, dBKnob);
        currentKnobs.addAll(List.of(aBKnob, bBKnob, cBKnob, dBKnob));
        
        vectorGrid.addChildren(rRow, gRow, bRow);
    }
    
    private void createKnobsForSwatch(CosPalette.CosPaletteSwatch swatch) {
        disposeCurrentKnobs();
        
        // Row 1: R (Red component)
        UI2dContainer rRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT + 18, 4);
        UIKnob aRKnob = (UIKnob) new UIKnob(swatch.aR).setLabel("Amp");
        UIKnob bRKnob = (UIKnob) new UIKnob(swatch.bR).setLabel("DC");
        UIKnob cRKnob = (UIKnob) new UIKnob(swatch.cR).setLabel("Freq");
        UIKnob dRKnob = (UIKnob) new UIKnob(swatch.dR).setLabel("Phase");
        rRow.addChildren(aRKnob, bRKnob, cRKnob, dRKnob);
        currentKnobs.addAll(List.of(aRKnob, bRKnob, cRKnob, dRKnob));
        
        // Row 2: G (Green component)
        UI2dContainer gRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT + 18, 4);
        UIKnob aGKnob = (UIKnob) new UIKnob(swatch.aG).setLabel("Amp");
        UIKnob bGKnob = (UIKnob) new UIKnob(swatch.bG).setLabel("DC");
        UIKnob cGKnob = (UIKnob) new UIKnob(swatch.cG).setLabel("Freq");
        UIKnob dGKnob = (UIKnob) new UIKnob(swatch.dG).setLabel("Phase");
        gRow.addChildren(aGKnob, bGKnob, cGKnob, dGKnob);
        currentKnobs.addAll(List.of(aGKnob, bGKnob, cGKnob, dGKnob));
        
        // Row 3: B (Blue component)
        UI2dContainer bRow = UI2dContainer.newHorizontalContainer(ROW_HEIGHT + 18, 4);
        UIKnob aBKnob = (UIKnob) new UIKnob(swatch.aB).setLabel("Amp");
        UIKnob bBKnob = (UIKnob) new UIKnob(swatch.bB).setLabel("DC");
        UIKnob cBKnob = (UIKnob) new UIKnob(swatch.cB).setLabel("Freq");
        UIKnob dBKnob = (UIKnob) new UIKnob(swatch.dB).setLabel("Phase");
        bRow.addChildren(aBKnob, bBKnob, cBKnob, dBKnob);
        currentKnobs.addAll(List.of(aBKnob, bBKnob, cBKnob, dBKnob));
        
        vectorGrid.addChildren(rRow, gRow, bRow);
        
        // Add parameter listeners to update swatch preview when knobs are adjusted
        currentListeningSwatch = swatch;
        swatch.aR.addListener(swatchPreviewUpdater);
        swatch.aG.addListener(swatchPreviewUpdater);
        swatch.aB.addListener(swatchPreviewUpdater);
        swatch.bR.addListener(swatchPreviewUpdater);
        swatch.bG.addListener(swatchPreviewUpdater);
        swatch.bB.addListener(swatchPreviewUpdater);
        swatch.cR.addListener(swatchPreviewUpdater);
        swatch.cG.addListener(swatchPreviewUpdater);
        swatch.cB.addListener(swatchPreviewUpdater);
        swatch.dR.addListener(swatchPreviewUpdater);
        swatch.dG.addListener(swatchPreviewUpdater);
        swatch.dB.addListener(swatchPreviewUpdater);
    }
    
    private void updateSelectedSwatchPreview() {
        if (selectedSwatchIndex >= 0 && selectedSwatchIndex < swatchControlsList.size()) {
            swatchControlsList.get(selectedSwatchIndex).updatePreview();
        }
        // Also update the main preview bar
        previewBar.redraw();
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
            
            // Swatch label - click to select swatch (10% smaller than 65: 65 * 0.9 = 58)
            UITextBox labelBox = new UITextBox(0, 0, 58, SWATCH_ROW_HEIGHT, swatch.label) {
                @Override
                protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                    super.onMousePressed(mouseEvent, mx, my);
                    palette.setSwatch(swatchIndex);
                }
            };
            
            // Mini palette preview (15% smaller: 100 * 0.85 = 85)
            miniPreview = new UIMiniPalettePreview(swatch, swatchIndex, 85, SWATCH_ROW_HEIGHT - 4);
            
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
                // Add 3px spacer before remove button (moved left 1px from previous 4px)
                UI2dComponent spacer = new UI2dComponent(0, 0, 3, SWATCH_ROW_HEIGHT) {};
                addChildren(spacer, removeButton);
            }
        }
        
        
        public void updatePreview() {
            miniPreview.redraw();
        }
        
    }
    
    // Mini palette preview showing the full gradient for a swatch
    private class UIMiniPalettePreview extends UI2dComponent {
        private final CosPalette.CosPaletteSwatch swatch;
        private final int swatchIndex;
        private final int samples = 32; // Fewer samples for smaller preview
        
        public UIMiniPalettePreview(CosPalette.CosPaletteSwatch swatch, int swatchIndex, float w, float h) {
            super(0, 0, w, h);
            this.swatch = swatch;
            this.swatchIndex = swatchIndex;
            setBorderColor(0xFF666666);
        }
        
        @Override
        protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
            super.onMousePressed(mouseEvent, mx, my);
            // Toggle selection: if this swatch is already selected, unselect it
            // Otherwise, select this swatch
            if (selectedSwatchIndex == swatchIndex) {
                selectedSwatchIndex = -1; // Unselect
                // Switch back to global palette knobs
                createKnobsForGlobalPalette();
            } else {
                selectedSwatchIndex = swatchIndex; // Select this swatch
                // Switch to swatch-specific knobs
                createKnobsForSwatch(swatch);
            }
            // Force complete redraw of all swatch previews to update selection outlines
            for (UISwatchControls swatchControls : swatchControlsList) {
                swatchControls.miniPreview.redraw();
            }
        }
        
        @Override
        public void onDraw(UI ui, VGraphics vg) {
            // Draw gradient samples first (before border to avoid lines through gradient)
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
                vg.rect(i * sampleWidth, 0, sampleWidth + 1, this.height);
                vg.fillColor(color);
                vg.fill();
            }
            
            // Draw border on top of gradient to ensure it covers the full area
            vg.beginPath();
            vg.rect(0, 0, this.width, this.height);
            if (selectedSwatchIndex == swatchIndex) {
                vg.strokeColor(0xFF00FF00); // Green outline when selected
                vg.strokeWidth(2);
            } else {
                vg.strokeColor(0xFF666666); // Gray outline when not selected  
                vg.strokeWidth(2); // Use same width to ensure complete coverage
            }
            vg.stroke();
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