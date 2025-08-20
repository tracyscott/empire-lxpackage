package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory("Empire")
public class ColorNormalVector extends LXPattern {
    
    public final ColorParameter belowColor = 
            new ColorParameter("Below Color", LXColor.RED)
                    .setDescription("Color for points below the plane");
    
    public final ColorParameter aboveColor = 
            new ColorParameter("Above Color", LXColor.BLUE)
                    .setDescription("Color for points above the plane");
    
    public final CompoundParameter normalX = 
            new CompoundParameter("Normal X", 0.0, -1.0, 1.0)
                    .setDescription("X component of the plane normal vector");
    
    public final CompoundParameter normalY = 
            new CompoundParameter("Normal Y", 0.0, -1.0, 1.0)
                    .setDescription("Y component of the plane normal vector");
    
    public final CompoundParameter normalZ = 
            new CompoundParameter("Normal Z", 1.0, -1.0, 1.0)
                    .setDescription("Z component of the plane normal vector");
    
    public final CompoundParameter dist = 
            new CompoundParameter("Distance", 0.5, -0.1, 1.1)
                    .setDescription("Distance along normal vector where color separation occurs");
    
    public ColorNormalVector(LX lx) {
        super(lx);
        addParameter("belowColor", this.belowColor);
        addParameter("aboveColor", this.aboveColor);
        addParameter("normalX", this.normalX);
        addParameter("normalY", this.normalY);
        addParameter("normalZ", this.normalZ);
        addParameter("dist", this.dist);
    }
    
    @Override
    protected void run(double deltaMs) {
        // Get normal vector components
        double nx = this.normalX.getValue();
        double ny = this.normalY.getValue();
        double nz = this.normalZ.getValue();
        
        // Normalize the normal vector to unit length
        double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 0) {
            nx /= length;
            ny /= length;
            nz /= length;
        } else {
            // Default to Z-axis if zero vector
            nx = 0.0;
            ny = 0.0;
            nz = 1.0;
        }
        
        double threshold = this.dist.getValue();
        
        for (LXPoint point : this.model.points) {
            // Calculate the dot product of point position with normal vector
            // This gives the distance along the normal vector from origin
            double dotProduct = point.xn * nx + point.yn * ny + point.zn * nz;
            
            // Choose color based on whether point is above or below the threshold distance
            int color;
            if (dotProduct < threshold) {
                color = this.belowColor.getColor();
            } else {
                color = this.aboveColor.getColor();
            }
            
            this.colors[point.index] = color;
        }
    }
}