package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.pattern.LXPattern;
import org.projectempire.lx.color.CosPalette;
import org.projectempire.lx.color.CosPalettePlugin;

@LXCategory("Empire")
public class CosPalettePattern extends LXPattern {
    
    public enum Plane {
        XY("XY Plane"),
        XZ("XZ Plane"),
        YZ("YZ Plane");
        
        public final String label;
        
        Plane(String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return this.label;
        }
    }
    
    public enum Direction {
        HORIZONTAL("Horizontal"),
        VERTICAL("Vertical");
        
        public final String label;
        
        Direction(String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return this.label;
        }
    }
    
    public final DiscreteParameter swatchIndex =
            new DiscreteParameter("Swatch", 0, 0, 30)
                    .setDescription("Which swatch to use for color mapping");
    
    public final EnumParameter<Plane> plane =
            new EnumParameter<>("Plane", Plane.XY)
                    .setDescription("Which plane to operate in (XY, XZ, or YZ)");
    
    public final EnumParameter<Direction> direction =
            new EnumParameter<>("Direction", Direction.HORIZONTAL)
                    .setDescription("Whether to map horizontally or vertically");
    
    public final CompoundParameter pos =
            new CompoundParameter("Pos", 0.0, 0.0, 1.0)
                    .setDescription("Position offset for color mapping");
    
    public final BooleanParameter seamless =
            new BooleanParameter("Seamless", false)
                    .setDescription("When enabled, remaps coordinates for seamless animation");
    
    public final CompoundParameter rotation =
            new CompoundParameter("Rotation", 0.0, 0.0, 360.0)
                    .setDescription("Rotation angle in degrees within the selected plane");
    
    public final CompoundParameter paletteOffset =
            new CompoundParameter("P.Offset", 0.0, 0.0, 1.0)
                    .setDescription("Starting position offset within the palette (0-1)");
    
    public final CompoundParameter paletteScale =
            new CompoundParameter("P.Scale", 1.0, 0.0, 1.0)
                    .setDescription("Scale factor for palette sampling range (0-1)");
    
    private CosPalette cosPalette;
    
    public CosPalettePattern(LX lx) {
        super(lx);
        addParameter("swatchIndex", this.swatchIndex);
        addParameter("plane", this.plane);
        addParameter("direction", this.direction);
        addParameter("pos", this.pos);
        addParameter("seamless", this.seamless);
        addParameter("rotation", this.rotation);
        addParameter("paletteOffset", this.paletteOffset);
        addParameter("paletteScale", this.paletteScale);
        
        // Get the CosPalette component through the static plugin instance
        CosPalettePlugin plugin = CosPalettePlugin.getInstance();
        if (plugin != null) {
            this.cosPalette = plugin.getCosPalette();
        }
        
        // Update swatch range when palette changes
        //if (this.cosPalette != null) {
        //    this.cosPalette.swatchesChanged.addListener((p) -> updateSwatchRange());
        //    updateSwatchRange();
        //}
    }
    
    private void updateSwatchRange() {
        if (this.cosPalette != null) {
            int swatchCount = this.cosPalette.getSwatches().size();
            this.swatchIndex.setRange(0, Math.max(1, swatchCount));
        }
    }

    @Override
    public void onActive() {
        LX.log("Num points: " + this.model.points.length);
    }
    
    @Override
    protected void run(double deltaMs) {
        // If no palette or no swatches, fill with black
        if (this.cosPalette == null || this.cosPalette.getSwatches().isEmpty()) {
            for (LXPoint point : this.model.points) {
                this.colors[point.index] = LXColor.BLACK;
            }
            return;
        }
        
        // Get the selected swatch
        int selectedSwatchIndex = Math.min(this.swatchIndex.getValuei(), this.cosPalette.getSwatches().size() - 1);
        CosPalette.CosPaletteSwatch swatch = this.cosPalette.getSwatches().get(selectedSwatchIndex);
        
        // Map each point's coordinate (with rotation and offset) to a color
        for (LXPoint point : this.model.points) {
            // Get the two coordinates for the selected plane
            double coord1, coord2;
            Plane selectedPlane = this.plane.getEnum();
            
            switch (selectedPlane) {
                case XY:
                    coord1 = point.xn;
                    coord2 = point.yn;
                    break;
                case XZ:
                    coord1 = point.xn;
                    coord2 = point.zn;
                    break;
                case YZ:
                    coord1 = point.yn;
                    coord2 = point.zn;
                    break;
                default:
                    coord1 = point.xn;
                    coord2 = point.yn;
                    break;
            }
            
            // Apply rotation transformation around center point (0.5, 0.5)
            double rotationRadians = Math.toRadians(this.rotation.getValue());
            
            // Translate to origin (center at 0,0)
            double centeredCoord1 = coord1 - 0.5;
            double centeredCoord2 = coord2 - 0.5;
            
            // Apply rotation
            double rotatedCoord1 = centeredCoord1 * Math.cos(rotationRadians) - centeredCoord2 * Math.sin(rotationRadians);
            double rotatedCoord2 = centeredCoord1 * Math.sin(rotationRadians) + centeredCoord2 * Math.cos(rotationRadians);
            
            // Translate back to original position
            rotatedCoord1 += 0.5;
            rotatedCoord2 += 0.5;
            
            // Select the final coordinate based on direction
            Direction selectedDirection = this.direction.getEnum();
            double coord = (selectedDirection == Direction.HORIZONTAL) ? rotatedCoord1 : rotatedCoord2;
            
            // Add the position offset and wrap with modulo 1.0
            double t = (coord + this.pos.getValue()) % 1.0;
            
            // Apply seamless mapping if enabled
            if (this.seamless.isOn()) {
                // Remap 0-1 to 0-1-0 for seamless animation
                // 0 to 0.5 maps to 0 to 1
                // 0.5 to 1 maps to 1 to 0 (reverse)
                if (t <= 0.5) {
                    // First half: map 0-0.5 to 0-1
                    t = t * 2.0;
                } else {
                    // Second half: map 0.5-1 to 1-0
                    t = 2.0 - (t * 2.0);
                }
            }
            
            // Apply palette offset and scale
            double paletteT;
            if (this.seamless.isOn()) {
                // For seamless mode: scale the range and apply offset
                // t is now 0-1, so we scale it to the desired range
                paletteT = this.paletteOffset.getValue() + (t * this.paletteScale.getValue());
            } else {
                // For normal mode: full range scaling with offset
                paletteT = this.paletteOffset.getValue() + (t * this.paletteScale.getValue());
            }
            
            // Ensure we stay within bounds
            paletteT = paletteT % 1.0;
            
            double[] rgb = this.cosPalette.getColor(paletteT, swatch);
            this.colors[point.index] = LXColor.rgb(
                (int)(Math.max(0, Math.min(255, rgb[0] * 255))),
                (int)(Math.max(0, Math.min(255, rgb[1] * 255))),
                (int)(Math.max(0, Math.min(255, rgb[2] * 255)))
            );
        }
    }
}