package org.projectempire.lx.wavetable;

public abstract class Wavetable {

    public float[] samples;
    int numSamples;

    // The x position of the wavetable.
    public float pos;


    public Wavetable(int numSamples) {
        samples = new float[numSamples+1];
        this.numSamples = numSamples;
    }
    abstract public void generateWavetable(float max, float offset);


    public void ease(Ease ease) {
        for (int i = 0; i < numSamples; i++) {
            samples[i] = ease.ease(samples[i]);
        }
    }

    public float getSample(float position, float physicalWidth) {
        int index = Math.round((position + physicalWidth / 2f - this.pos) * numSamples / physicalWidth);
        if (index < 0 || index >= numSamples)
            return 0;
        return samples[index];
    }

    public void multiply(Wavetable other) {
        for (int i = 0; i < numSamples; i++) {
            samples[i] *= other.samples[i];
        }
    }
}
