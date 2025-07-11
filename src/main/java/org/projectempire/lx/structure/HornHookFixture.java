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
import heronarts.lx.transform.LXTransform;
import heronarts.glx.ui.UI2dComponent;

import java.util.List;
import java.util.Map;

public class HornHookFixture extends LXBasicFixture implements UIFixtureControls<HornHookFixture> {
    private static final float SPACING = 0.5468f;
    public static final int MAX_POINTS = 1024;
    public static final float CONTROL_WIDTH = 41.0F;

    public final DiscreteParameter points1 = (DiscreteParameter)
            new DiscreteParameter("LED 1", 150, 1, MAX_POINTS + 1)
                    .setUnits(LXParameter.Units.INTEGER)
                    .setDescription("Number of points in the first string");

    public final DiscreteParameter points2 = (DiscreteParameter)
            new DiscreteParameter("LED 2", 150, 1, MAX_POINTS + 1)
                    .setUnits(LXParameter.Units.INTEGER)
                    .setDescription("Number of points in the second string");

    public final DiscreteParameter points3 = (DiscreteParameter)
            new DiscreteParameter("LED 3", 150, 1, MAX_POINTS + 1)
                    .setUnits(LXParameter.Units.INTEGER)
                    .setDescription("Number of points in the third string");

    public final DiscreteParameter points4 = (DiscreteParameter)
            new DiscreteParameter("LED 4", 150, 1, MAX_POINTS + 1)
                    .setUnits(LXParameter.Units.INTEGER)
                    .setDescription("Number of points in the fourth string");

    public final BoundedParameter length = new BoundedParameter("Length", 24, 0, 1000)
            .setDescription("Length straight part of the horn");

    public final BoundedParameter radius = new BoundedParameter("Radius", 100, 0, 1000000)
            .setDescription("Radius of the horn");

    public final BoundedParameter degrees = (BoundedParameter) new BoundedParameter("Degrees", 90, 0, 360)
            .setUnits(LXParameter.Units.DEGREES)
            .setDescription("Number of degrees the arc covers");

    public HornHookFixture(LX lx) {
        super(lx, "HornHook");
        addMetricsParameter("numPoints1", this.points1);
        addMetricsParameter("numPoints2", this.points2);
        addMetricsParameter("numPoints3", this.points3);
        addMetricsParameter("numPoints4", this.points4);
        addGeometryParameter("length", this.length);
        addGeometryParameter("radius", this.radius);
        addGeometryParameter("degrees", this.degrees);
    }

    @Override
    protected void computePointGeometry(LXMatrix matrix, List<LXPoint> points) {
        LXTransform transform = new LXTransform(matrix);
        final float radius = this.radius.getValuef();
        final float circumference = LX.TWO_PIf * radius;
        final float rotation = LX.TWO_PIf * (SPACING / circumference);
        int startIndex = 0; // inclusive
        int endIndex = 0; // exclusive
        int linePointCount = (int) (length.getValuef() / SPACING);
        for (int segment = 1; segment < 5; ++segment) {
            transform.push();
            switch (segment) {
                case 1:
                    startIndex = 0;
                    endIndex = points1.getValuei();
                    transform.translateY(0.5f);
                    break;
                case 2:
                    startIndex += points1.getValuei();
                    endIndex += points2.getValuei();
                    transform.translateY(-0.5f);
                    break;
                case 3:
                    startIndex += points2.getValuei();
                    endIndex += points3.getValuei();
                    transform.translateZ(0.5f);
                    break;
                case 4:
                    startIndex += points3.getValuei();
                    endIndex += points4.getValuei();
                    transform.translateZ(-0.5f);
                    break;
            }
            int i;
            for (i = startIndex; i < linePointCount + startIndex - 1; ++i) {
                // do the straight part of the horn
                points.get(getPixelIndex(i)).set(transform);
                transform.translateX(SPACING);
            }
            for (; i < endIndex; ++i) {
                // do the curved part of the horn
                points.get(getPixelIndex(i)).set(transform);
                transform.translateZ(radius);
                transform.rotateY(-rotation);
                transform.translateZ(-radius);
            }
            transform.pop();
        }
    }

    private int getPixelIndex(int pixelNum) {
        final int segment1Start = 0;
        final int segment1End = points1.getValuei() - 1;
        final int segment2Start = segment1End + 1;
        final int segment2End = segment1End + points2.getValuei();
        final int segment3Start = segment2End + 1;
        final int segment3End = segment2End + points3.getValuei();
        final int segment4Start = segment3End + 1;
        final int segment4End = segment3End + points4.getValuei();

        // Every other segment is reverse order:
        // 0 1 2 3 4 | 5 6 7 8 9 | 10 11 12 13 14 | 15 16 17 18 19
        // 0 1 2 3 4 | 9 8 7 6 5 | 10 11 12 13 14 | 19 18 17 16 15
        int result = -1;
        if (pixelNum <= segment1End) {
            result = pixelNum;
        } else if (pixelNum <= segment2End) {
            result = segment2Start + segment2End - pixelNum;
        } else if (pixelNum <= segment3End) {
            result = pixelNum;
        } else if (pixelNum <= segment4End) {
            result = segment4Start + segment4End - pixelNum;
        }
        return result;
    }

    @Override
    protected int size() {
        return points1.getValuei() + points2.getValuei() + points3.getValuei() + points4.getValuei();
    }

    @Override
    protected String[] getDefaultTags() {
        return new String[]{"horn"};
    }

    @Override
    protected void addModelMetaData(Map<String, String> metaData) {
        metaData.put("numPoints1", String.valueOf(this.points1.getValuei()));
        metaData.put("numPoints2", String.valueOf(this.points2.getValuei()));
        metaData.put("numPoints3", String.valueOf(this.points3.getValuei()));
        metaData.put("numPoints4", String.valueOf(this.points4.getValuei()));
        metaData.put("length", String.valueOf(this.length.getValuef()));
        metaData.put("radius", String.valueOf(this.radius.getValuef()));
        metaData.put("degrees", String.valueOf(this.degrees.getValuef()));
    }

    @Override
    public void buildFixtureControls(LXStudio.UI ui, UIFixture uiFixture, HornHookFixture hornHookFixture) {
        uiFixture.addTagSection();
        uiFixture.addGeometrySection();
        uiFixture.addSection("Horn Hook", new UI2dComponent[][]{
                {uiFixture.newControlIntBox(hornHookFixture.points1, CONTROL_WIDTH),
                        uiFixture.newControlIntBox(hornHookFixture.points2, CONTROL_WIDTH),
                        uiFixture.newControlIntBox(hornHookFixture.points3, CONTROL_WIDTH),
                        uiFixture.newControlIntBox(hornHookFixture.points4, CONTROL_WIDTH)
                },
                {uiFixture.newControlLabel("LEDs", CONTROL_WIDTH * 4),
//                        uiFixture.newControlLabel("LEDs 2", CONTROL_WIDTH),
//                        uiFixture.newControlLabel("LEDs 3", CONTROL_WIDTH),
//                        uiFixture.newControlLabel("LEDs 4", CONTROL_WIDTH)
                },
                {
                        uiFixture.newControlBox(hornHookFixture.length, CONTROL_WIDTH),
                        uiFixture.newControlBox(hornHookFixture.radius, CONTROL_WIDTH),
                        uiFixture.newControlBox(hornHookFixture.degrees, CONTROL_WIDTH)

                },
                {
                        uiFixture.newControlLabel("Length", CONTROL_WIDTH),
                        uiFixture.newControlLabel("Radius", CONTROL_WIDTH),
                        uiFixture.newControlLabel("Degrees", CONTROL_WIDTH)

                }
        });
        uiFixture.addProtocolSection(hornHookFixture, false);
    }
}
