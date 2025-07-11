package org.projectempire.lx.pattern.ui.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;
import org.projectempire.lx.utils.Blaze;

@LXCategory("Blaze")
public class SparkFirePattern extends LXPattern {
    private int numSparks;
    private float cooling1;
    private float cooling2;
    private float accel;
    private float speed;
    private float[] sparks;
    private float[] sparkX;
    private float[] pixels;

    public final CompoundParameter cooling = new CompoundParameter("Cooling", 0.95, 0.8, 0.995)
            .setDescription("Coefficient cooling. Lower cools faster.");

    public SparkFirePattern(LX lx) {
        super(lx);
        addParameter("cooling", this.cooling);
        init();
    }

    private void init() {
        int pixelCount = model.points.length;
        // The number of sparks. Try 3-6
        numSparks = 3 + (int) Math.floor(pixelCount / 80f);
        // Subtractive cooling. Try 0.02..0.3. Higher cools faster.
        cooling1 = 0.05f;
        // Coefficient cooling. Try 0.9..0.995. Lower cools faster.
        // This function is a problem, more leds = less cooling to the point that the never cool
        cooling2 = cooling.getValuef(); // 0.8f + 0.2f / (1f + (float) Math.exp(-pixelCount / 80f));
        LX.log("Cooling2: " + cooling2);
        accel = 0.03f; // How fast sparks accelerate as they rise (try 0.02..0.05)
        speed = 0.05f; // Baseline speed of travel for each spark (try 0.03..0.1)
        // Energy of each spark. Affects pixel heating and spark speed.
        sparks = new float[numSparks];
        sparkX = new float[numSparks]; // X position of each spark, in units of pixel index
        pixels = new float[pixelCount]; // Brightness (heat energy) of each pixel
        // Initialize sparks with random position and energy
        for (int i = 0; i < numSparks; i++) {
            sparkX[i] = (int) LXUtils.random(0, pixelCount * 0.75f);
            // Further sparks are older and have less energy
            sparks[i] = 0.2f * (1f - sparkX[i] / (float) pixelCount) + (float) LXUtils.random(0, 0.4);
        }
        // Set up an initial heat value so init doesn't look empty
        pixels[0] = 20;
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        super.onParameterChanged(parameter);
        if (parameter == cooling) {
            cooling2 = (float) cooling.getValue();
        }
    }

    @Override
    protected void onModelChanged(LXModel model) {
        super.onModelChanged(model);
        init();
    }

    public void run(double deltaMs) {
        final int pixelCount = model.points.length;
        float delta = (float) deltaMs * speed;
        for (int i = 0; i < pixelCount; i++) {
            float cooldown = cooling1 * delta;
            if (cooldown > pixels[i]) {
                pixels[i] = 0;
            } else {
                // Coefficient cooling (`cooling2`) makes hotter sparks lose more energy.
                // Subtractive cooling makes all sparks lose some energy.
                float before = pixels[i];
                pixels[i] = pixels[i] * cooling2 - cooldown;
            }
        }
        /*
            Heat rises. Starting at the far end and working towards the first pixel,
            compute a weighted average of the heat from the preceding pixels and apply
            it to this one. Weighting several pixels behind higher creates a nice wispy
            pattern that can be seen more clearly by turning down `speed`.
        */
        for (int k = pixelCount - 1; k >= 4; k--) {
            float before = pixels[k];
            float h1 = pixels[k - 1];
            float h2 = pixels[k - 2];
            float h3 = pixels[k - 3];
            float h4 = pixels[k - 4];
            pixels[k] = (h1 + h2 + h3 * 2f + h4 * 3f) / 7f;
        }
        for (int i = 0; i < numSparks; i++) {
            // Reinitialize a spark that's passed the end and been reset
            if (sparks[i] <= 0) {
                sparks[i] = (float) LXUtils.random(0, 1);
                sparkX[i] = 0;
            }

            // Accelerate (add kinetic energy) to each spark
            sparks[i] += accel * delta;

            // Stash the original x position of this spark
            int ox = (int) sparkX[i];

            // Δd = r·Δt
            // Sparks advance at a rate that's the square of their energy
            sparkX[i] += sparks[i] * sparks[i] * delta;

            // Reset sparks that are past the end
            if (sparkX[i] >= pixelCount) {
                sparkX[i] = 0;
                sparks[i] = 0;
                continue;  // Skip the rest of the for() loop for this iteration
            }

            /*
              For all pixels between the new x position and the original x position, heat the pixels (add brightness)
              . Higher
              energy sparks are travelling faster and thus don't heat each pixel of air as much.
             */
            for (int j = ox; j < sparkX[i]; j++) {
                pixels[j] += LXUtils.clampf(1f - sparks[i] * 0.4f, 0f, 1f) * 0.5f;
            }
        }

        // render
        for (int i = 0; i < pixelCount; i++) {
            float v = pixels[i];
            /*
                v * v is our typical gamma correction, and it's constrained to remain
                between 0 and 1. Multiplying this by 0.1 keeps us in the red-yellow range;
                higher energies will be yellow.
             */
            float h = 0.1f * LXUtils.clampf(v * v, 0f, 1f);

              /*
                Desaturate (to white) any pixel with energy values between 1 and 1.5;
                Saturation will be clamped to 0 for any energy value above 1.5. Without
                this, the hottest pixels are just yellow instead of white hot.
              */
            // This isn't working as expected? v = 3.0636... -> h = 0.1 -> s = -3.1272
            float s = LXUtils.clampf(1f - (v - 1f) * 2f, 0f, 2f);
            colors[model.points[i].index] = Blaze.hsv(h, s, v);
        }
    }
}
