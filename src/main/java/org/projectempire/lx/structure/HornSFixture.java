package org.projectempire.lx.structure;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.structure.LXBasicFixture;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.fixture.UIFixture;
import heronarts.lx.studio.ui.fixture.UIFixtureControls;
import heronarts.lx.transform.LXMatrix;
import heronarts.glx.ui.UI2dComponent;

import java.util.List;
import java.util.Map;

public class HornSFixture extends LXBasicFixture implements UIFixtureControls<HornSFixture> {
    public static final int MAX_POINTS = 4096;
    public static final float CONTROL_WIDTH = 41.0F;
    public static final float INCHES_PER_METER = 39.3701f;

    public final DiscreteParameter ppm = (DiscreteParameter)
            new DiscreteParameter("PPM", 72, 30, 144)
                    .setUnits(LXParameter.Units.INTEGER)
                    .setDescription("Number of pixels per meter");

    public final DiscreteParameter numPoints = (DiscreteParameter)
            new DiscreteParameter("Points", 292, 1, MAX_POINTS + 1)
                    .setUnits(LXParameter.Units.INTEGER)
                    .setDescription("Number of points in the string");

    public final BoundedParameter radius1 = new BoundedParameter("Radius 1", 122.22, 0, 1000000)
            .setDescription("Radius of the first part of horn");

    public final BoundedParameter degrees1 = (BoundedParameter) new BoundedParameter("Degrees 1", 68, 0, 360)
            .setUnits(LXParameter.Units.DEGREES)
            .setDescription("Number of degrees the first arc covers");

    public final BoundedParameter radius2 = new BoundedParameter("Radius 2", 18.9, 0, 1000000)
            .setDescription("Radius of the second part of horn");

    public final BoundedParameter degrees2 = (BoundedParameter) new BoundedParameter("Degrees 2", 50, 0, 360)
            .setUnits(LXParameter.Units.DEGREES)
            .setDescription("Number of degrees the second arc covers");

    public HornSFixture(LX lx) {
        super(lx, "HornS");
        addMetricsParameter("ppm", this.ppm);
        addMetricsParameter("numPoints", this.numPoints);
        addGeometryParameter("radius1", this.radius1);
        addGeometryParameter("degrees1", this.degrees1);
        addGeometryParameter("radius2", this.radius2);
        addGeometryParameter("degrees2", this.degrees2);
    }

    @Override
    protected void computePointGeometry(LXMatrix transform, List<LXPoint> points) {
        // spacing between pixels
        final float pixelSpacing = getPixelSpacing();
        // radius of the first arc
        final float r1 = this.radius1.getValuef();
        // circumference of the first arc
        final float c1 = LX.TWO_PIf * r1;
        // arc length of the first arc
        final float arc1 = c1 * (this.degrees1.getValuef() / 360f);
        // angle between pixels on the first arc
        final float theta1 = LX.TWO_PIf * (pixelSpacing / c1);
        // number of pixels on the first arc
        final int pixelCount1 = (int) (arc1 / pixelSpacing);
        for (int i = 0; i < pixelCount1 && i < points.size(); i++) {
            LXPoint p = points.get(i);
            p.set(transform);
            transform.translateZ(-r1);
            transform.rotateX(-theta1);
            transform.translateZ(r1);
        }
        // radius of the second arc
        final float r2 = this.radius2.getValuef();
        // circumference of the second arc
        final float c2 = LX.TWO_PIf * r2;
        // arc length of the second arc
        final float theta2 = LX.TWO_PIf * (pixelSpacing / c2);
        for (int i = pixelCount1; i < points.size(); i++) {
            LXPoint p = points.get(i);
            p.set(transform);
            transform.translateZ(r2);
            transform.rotateX(theta2);
            transform.translateZ(-r2);
        }
    }

    private float getPixelSpacing() {
        return 1f / (ppm.getValuei() / INCHES_PER_METER);
    }

    @Override
    protected int size() {
        return numPoints.getValuei();
    }

    @Override
    protected String[] getDefaultTags() {
        return new String[]{"horn"};
    }

    @Override
    protected void addModelMetaData(Map<String, String> metaData) {
        metaData.put("ppm", Integer.toString(ppm.getValuei()));
        metaData.put("numPoints", Integer.toString(numPoints.getValuei()));
        metaData.put("radius1", Float.toString(radius1.getValuef()));
        metaData.put("degrees1", Float.toString(degrees1.getValuef()));
        metaData.put("radius2", Float.toString(radius2.getValuef()));
        metaData.put("degrees2", Float.toString(degrees2.getValuef()));
    }

    @Override
    public void buildFixtureControls(LXStudio.UI ui, UIFixture uiFixture, HornSFixture hornSFixture) {
        uiFixture.addTagSection();
        uiFixture.addGeometrySection();
        uiFixture.addSection("Pixels", new UI2dComponent[][]{
                {
                        uiFixture.newControlIntBox(hornSFixture.ppm, CONTROL_WIDTH * 2),
                        uiFixture.newControlIntBox(hornSFixture.numPoints, CONTROL_WIDTH * 2)
                },
                {
                        uiFixture.newControlLabel("PPM", CONTROL_WIDTH * 2),
                        uiFixture.newControlLabel("Points", CONTROL_WIDTH * 2)
                }
        });
        uiFixture.addSection("Curves", new UI2dComponent[][]{
                {uiFixture.newControlLabel("Curve 1", 64),
                        uiFixture.newControlBox(hornSFixture.radius1, 51),
                        uiFixture.newControlBox(hornSFixture.degrees1, 51)
                },
                {uiFixture.newControlLabel("Curve 2", 64),
                        uiFixture.newControlBox(hornSFixture.radius2, 51),
                        uiFixture.newControlBox(hornSFixture.degrees2, 51)
                }
        });
        uiFixture.addProtocolSection(hornSFixture, false);
    }
}
