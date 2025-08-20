package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory("Empire")
public class ColorRings extends LXPattern {
    
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
    
    public final ColorParameter innerColor = 
            new ColorParameter("Inner Color", LXColor.RED)
                    .setDescription("Color for points inside the radius");
    
    public final ColorParameter outerColor = 
            new ColorParameter("Outer Color", LXColor.BLUE)
                    .setDescription("Color for points outside the radius");
    
    public final CompoundParameter radius = 
            new CompoundParameter("Radius", 0.3, 0.0, 1.0)
                    .setDescription("Radius threshold for color rings");
    
    public final EnumParameter<Plane> plane =
            new EnumParameter<>("Plane", Plane.XY)
                    .setDescription("Which plane to calculate polar coordinates in");
    
    public ColorRings(LX lx) {
        super(lx);
        addParameter("innerColor", this.innerColor);
        addParameter("outerColor", this.outerColor);
        addParameter("radius", this.radius);
        addParameter("plane", this.plane);
    }
    
    @Override
    protected void run(double deltaMs) {
        double radiusThreshold = this.radius.getValue();
        Plane selectedPlane = this.plane.getEnum();
        
        for (LXPoint point : this.model.points) {
            // Get the two coordinates for the selected plane
            double coord1, coord2;
            
            switch (selectedPlane) {
                case XY:
                    coord1 = point.xn - 0.5; // Center at 0.5
                    coord2 = point.yn - 0.5;
                    break;
                case XZ:
                    coord1 = point.xn - 0.5;
                    coord2 = point.zn - 0.5;
                    break;
                case YZ:
                    coord1 = point.yn - 0.5;
                    coord2 = point.zn - 0.5;
                    break;
                default:
                    coord1 = point.xn - 0.5;
                    coord2 = point.yn - 0.5;
                    break;
            }
            
            // Calculate polar radius from center
            double polarRadius = Math.sqrt(coord1 * coord1 + coord2 * coord2);
            
            // Choose color based on radius threshold
            int color;
            if (polarRadius < radiusThreshold) {
                color = this.innerColor.getColor();
            } else {
                color = this.outerColor.getColor();
            }
            
            this.colors[point.index] = color;
        }
    }
}