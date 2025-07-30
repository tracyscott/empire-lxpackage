package org.projectempire.lx.vstrip;

import org.projectempire.lx.wavetable.Wavetable;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

import java.util.ArrayList;
import java.util.List;

public class Flarelet {
    public VTopology vTop;

    public DStrip dStrip;
    public DStrip initDStrip;
    public float pos = 0f;
    public float initPos = 0f;
    public float speed = 1f;
    public float initSpeed = 1f;
    public List<DStrip> prevDStrips = new ArrayList<>();
    public List<DStrip> nextDStrips = new ArrayList<>();
    public int color = LXColor.rgba(255, 255, 255, 255);
    // Whether the flarelet is enabled or not.  If not enabled, it will not render.
    public boolean enabled = true;
    public boolean cloneAtJoints = false;
    // The intensity of the flarelet.  This is a multiplier that can be used to adjust the intensity of the flarelet.
    public float intensity = 1.0f;
    // The intensity at which we simply skip the rendering.
    public float cutoutIntensity = 0.05f;
    public float flareWidth = -1.0f;
    // Time the flarelet was started.  Used for tracking fade-outs per flarelet.
    public double startTime;
    public double fadeTime; // milliseconds
    // Application of FX
    public int fx;
    public float fxDepth;
    public float fxFreq;
    public Wavetable wavetable;
    public float waveWidth;
    // Which palette swatch to use for the flarelet.
    public int swatch = -1;
    public LX lx;

    static public class CloneRequest {
        // The dStrip that the clone should start on.  Also the position
        public DStrip dStrip;
        // The position on the dStrip that the clone should start at.
        public float pos;
    }

    // In order for flarelets to fan out along all joints, when we reach a joint, we will generate a series of
    // clone requests for all the joints that we can travel to but didn't with the primary Flarelet.
    // The outside rendering loop should look for clone requests each frame and create new flarelets as needed.
    // One the cloned flarelet is no longer visible, we should delete it.
    public List<CloneRequest> cloneRequests = new ArrayList<>();

    public boolean isClone = false;

    // Create a new flarelet as a clone of this one and marked it as cloned.
    public Flarelet cloneFlarelet(DStrip dStrip, float pos) {
        Flarelet f = new Flarelet();
        f.vTop = this.vTop;
        f.dStrip = dStrip;
        f.initDStrip = dStrip;
        f.pos = pos;
        f.initPos = pos;
        f.speed = this.speed;
        f.initSpeed = this.speed;
        f.color = this.color;
        f.intensity = this.intensity;
        f.cutoutIntensity = this.cutoutIntensity;
        f.flareWidth = this.flareWidth;
        f.startTime = this.startTime;
        f.fadeTime = this.fadeTime;
        f.fx = this.fx;
        f.fxDepth = this.fxDepth;
        f.fxFreq = this.fxFreq;
        f.wavetable = this.wavetable;
        f.waveWidth = this.waveWidth;
        f.swatch = this.swatch;
        f.lx = this.lx;
        f.cloneAtJoints = this.cloneAtJoints;
        f.enabled = this.enabled;
        f.startTime = System.currentTimeMillis();

        f.isClone = true;
        return f;
    }

    public void updateCurrentDStrip(int dStripSelector) {
        DStrip currentDStrip = dStrip;
        // First, lets transfer the current dStrip into our
        // previous dStrips list.  The list will be trimmed in our draw loop.
        prevDStrips.add(0, dStrip);
        // Next the current dStrip should come from the beginning of nextDStrips
        // The nextDStrips list will be filled out in the draw loop if necessary.
        if (!nextDStrips.isEmpty())
            dStrip = nextDStrips.remove(0);
        else {
            dStrip = dStrip.chooseNextStrip(dStripSelector);
        }

        // No valid joints, just continue along with our current strip.  It will stop rendering
        // once it is no longer visible.
        if (dStrip == null) {
            dStrip = currentDStrip;
            return;
        }

        // If we are cloning a flarelet at each joint, we need to create a list of clone requests
        // here.  In the outer draw loop, we will process the clone requests and create new flarelets
        // as needed.  Cloned flarelets will not be reset at the end of their travel.  They will be
        // deleted.  For each clone, we should set the initial parameters of the target dStrip and
        // position here.  Also verify that there is more than one joint coming up.
        if (cloneAtJoints && currentDStrip.getNextStripJointCount() > 1) {
            int cloneSelector = dStripSelector==0?1:0; // If we are cloning at joints, we will alternate the joint selector to choose the next strip.
            DStrip cloneDStrip = currentDStrip.chooseNextStrip(cloneSelector);
            // If we are cloning at joints, we need to create a clone request for the next dStrip.
            if (cloneDStrip != null) {
                CloneRequest cloneRequest = new CloneRequest();
                cloneRequest.dStrip = cloneDStrip;
                cloneRequest.pos = (cloneDStrip.forward) ? 0.0f : cloneDStrip.vStrip.length();
                cloneRequests.add(cloneRequest);
                //LX.log("Flarelet: adding clone request for dStrip: " + cloneDStrip.vStrip.id + " pos=" + pos);
            }
        }

        // Set or position based on the directionality of the current dStrip.
        if (dStrip.forward) {
            //LX.log("Flarelet: reset current dStrip forward: " + dStrip.vStrip.id + " pos=" + pos);
            pos = 0.0f;
        } else {
            //LX.log("Flarelet: reset current dStrip backward: " + dStrip.vStrip.id + " pos=" + pos);
            pos = dStrip.vStrip.length();
        }

    }

    public void reset(VTopology vTop, int dStripNum, float initialPos, float randomSpeed, boolean forward) {
        this.vTop = vTop;
        pos = initialPos;
        dStrip = new DStrip(vTop, dStripNum, forward);
        speed = randomSpeed * (float)Math.random();
        // This allows us to reset the flarelet to its initial state.
        initSpeed = speed;
        initDStrip = dStrip;
        initPos = pos;
        nextDStrips = new ArrayList<DStrip>();
        prevDStrips = new ArrayList<DStrip>();
    }

    public void resetToInit() {
        dStrip = initDStrip;
        pos = initPos;
        speed = initSpeed;
        nextDStrips.clear();
        prevDStrips.clear();
    }

    public boolean isRenderable() {
        if ((!enabled) || (getFadeLevel() < cutoutIntensity)) return false;
        return true;
    }

    /**
     * For flarelets that will be fading out, compute the intensity level based on the current time, the flare start
     * time, and the flare fade time.
     * @return
     */
    public float getFadeLevel() {
        if (Math.abs(fadeTime) < 0.001f) return intensity;
        float fadeLevel = 1f - (float) ((System.currentTimeMillis() - startTime)/fadeTime);
        if (fadeLevel < 0f)
            fadeLevel = 0f;
        return fadeLevel * intensity;
    }

    public void waveOnTop(int[] colors, LXColor.Blend blend, int whichJoint) {
        waveOnTop(colors, blend, whichJoint, true);
    }

    public void waveOnTop(int[] colors, LXColor.Blend blend, int whichJoint, boolean initialTail) {
        // Render on the current strip.  Compute the amount of the waveform on previous strips and the amount of
        // the waveform on the next strip. This needs to be done iteratively until there is no previous amount
        // or extra end amount. The waveform with have some width.
        float currentStripLength = dStrip.vStrip.length();
        float waveStart = pos - waveWidth/2f;
        float waveEnd = pos + waveWidth/2f;
        float curWtPos = pos;
        boolean needsCurrentBarUpdate = false;

        if (!isRenderable()) return;
        if (waveStart < 0f && initialTail) {
            float prevAmount = -waveStart;
            while (prevAmount > 0f) {
                DStrip prevDStrip = dStrip.choosePrevStrip(whichJoint);
                if (prevDStrip != null) {
                    prevAmount = prevAmount - prevDStrip.vStrip.length();
                    // To render on a previous strip, move the position of the wavetable center to the right by the
                    // length of the strip.
                    curWtPos = curWtPos + prevDStrip.vStrip.length();
                    renderWavetable(colors, prevDStrip, curWtPos, color, blend);

                    if (fx == 1) {
                        VStripRender.randomGrayBaseDepth(colors, prevDStrip.vStrip, pos, waveWidth, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                          (int) (255 * fxDepth));
                    } else if (fx == 2) {
                        VStripRender.cosine(colors, prevDStrip.vStrip, pos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
                    }
                } else {
                    prevAmount = 0f;
                }
            }
        }

        // Render on our current target strip.
        renderWavetable(colors, dStrip, pos, color, blend);

        if (fx == 1) {
            VStripRender.randomGrayBaseDepth(colors, dStrip.vStrip, pos, waveWidth, LXColor.Blend.MULTIPLY, (int)(255*(1f - fxDepth)),
                    (int)(255*fxDepth));
        } else if (fx == 2) {
            VStripRender.cosine(colors, dStrip.vStrip, pos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
        }

        // If the wavetable extends past the current strip, render on the next set of strips.
        if (waveEnd > currentStripLength) {
            //LX.log("waveEnd > currentStripLength: " + waveEnd + " > " + currentStripLength + " waveWidth=" + waveWidth + " pos=" + pos);
            float nextAmount = waveEnd - currentStripLength;
            // To render on a next strip, move the position of the wavetable center to the left by the
            // length of the strip.
            curWtPos = pos - currentStripLength;
            DStrip curDStrip = dStrip;
            while (nextAmount > 0f) {
                DStrip nextDStrip = curDStrip.chooseNextStrip(whichJoint);
                if (nextDStrip != null) {
                    renderWavetable(colors, nextDStrip, curWtPos, color, blend);
                    if (fx == 1) {
                        VStripRender.randomGrayBaseDepth(colors, nextDStrip.vStrip, curWtPos, waveWidth, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                          (int) (255 * fxDepth));
                    } else if (fx == 2) {
                        VStripRender.cosine(colors, nextDStrip.vStrip, curWtPos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
                    }
                    // To render on a next strip, move the position of the wavetable center to the left by the
                    // length of the strip.
                    curWtPos = curWtPos - nextDStrip.vStrip.length();
                    nextAmount = nextAmount - nextDStrip.vStrip.length();
                    curDStrip = nextDStrip;
                } else {
                    nextAmount = 0f;
                }
            }
        }
        if (dStrip.forward) {
            pos += speed / 100f;
        } else {
            pos -= speed / 100f;
        }

        if ((pos < 0.0 && !dStrip.forward) || (pos > currentStripLength && dStrip.forward)) {
            needsCurrentBarUpdate = true;
            // LX.log("needs currentBarUpdate: " + pos + " dStrip=" + dStrip.vStrip.id + " forward=" + dStrip.forward);
        }

        if (needsCurrentBarUpdate) {
            updateCurrentDStrip(whichJoint);
        }
        if (waveStart > currentStripLength) {
            // If the start of the wave has left the end of the current strip then we have moved off the renderable
            // area.  Mark the flarelet as not renderable.
            enabled = false;
        }


    }

    public void waveOnTopFan(int[] colors, LXColor.Blend blend, int whichJoint, boolean initialTail) {
        // Render on the current strip.  Compute the amount of the waveform on previous strips and the amount of
        // the waveform on the next strip. This needs to be done iteratively until there is no previous amount
        // or extra end amount. The waveform with have some width.
        float currentStripLength = dStrip.vStrip.length();
        float waveStart = pos - waveWidth/2f;
        float waveEnd = pos + waveWidth/2f;
        float curWtPos = pos;
        boolean needsCurrentBarUpdate = false;

        if (!isRenderable()) return;
        if (waveStart < 0f && initialTail && !isClone) {
            float prevAmount = -waveStart;
            while (prevAmount > 0f) {
                DStrip prevDStrip = dStrip.choosePrevStrip(whichJoint);
                if (prevDStrip != null) {
                    prevAmount = prevAmount - prevDStrip.vStrip.length();
                    // To render on a previous strip, move the position of the wavetable center to the right by the
                    // length of the strip.
                    curWtPos = curWtPos + prevDStrip.vStrip.length();
                    renderWavetable(colors, prevDStrip, curWtPos, color, blend);

                    if (fx == 1) {
                        VStripRender.randomGrayBaseDepth(colors, prevDStrip.vStrip, pos, waveWidth, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                                (int) (255 * fxDepth));
                    } else if (fx == 2) {
                        VStripRender.cosine(colors, prevDStrip.vStrip, pos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
                    }
                } else {
                    prevAmount = 0f;
                }
            }
        }

        // Render on our current target strip.
        renderWavetable(colors, dStrip, pos, color, blend);

        if (fx == 1) {
            VStripRender.randomGrayBaseDepth(colors, dStrip.vStrip, pos, waveWidth, LXColor.Blend.MULTIPLY, (int)(255*(1f - fxDepth)),
                    (int)(255*fxDepth));
        } else if (fx == 2) {
            VStripRender.cosine(colors, dStrip.vStrip, pos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
        }

        // If the wavetable extends past the current strip, render on the next set of strips.  For the fan out case
        // we want to render on all joints.
        if (waveEnd > currentStripLength) {
            //LX.log("waveEnd > currentStripLength: " + waveEnd + " > " + currentStripLength + " waveWidth=" + waveWidth + " pos=" + pos);
            // We need to render on all the next strips coming out of the joint.  Note that this only renders
            // one segment ahead.  If the width where to span multiple future joints this won't handle it because
            // we are not iterating on nextDStrip.
            // NOTE(tracy): I just added iterating on curDStrip.  It hasn't been tested yet.
            for (int fanJointNum = 0; fanJointNum < 2; fanJointNum++) {
                DStrip curDStrip = dStrip;
                float nextAmount = waveEnd - currentStripLength;
                // To render on a next strip, move the position of the wavetable center to the left by the
                // length of the strip.
                curWtPos = pos - currentStripLength;
                while (nextAmount > 0f) {
                    DStrip nextDStrip = curDStrip.chooseNextStrip(fanJointNum);
                    if (nextDStrip != null) {
                        //LX.log("Rendering on future dStrip: " + nextDStrip.vStrip.id + " at pos: " + curWtPos + " waveWidth=" + waveWidth + " blend=" + blend);
                        renderWavetable(colors, nextDStrip, curWtPos, color, blend);
                        if (fx == 1) {
                            VStripRender.randomGrayBaseDepth(colors, nextDStrip.vStrip, curWtPos, waveWidth, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                                    (int) (255 * fxDepth));
                        } else if (fx == 2) {
                            VStripRender.cosine(colors, nextDStrip.vStrip, curWtPos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
                        }
                        // To render on a next strip, move the position of the wavetable center to the left by the
                        // length of the strip.
                        curWtPos = curWtPos - nextDStrip.vStrip.length();
                        nextAmount = nextAmount - nextDStrip.vStrip.length();
                        curDStrip = nextDStrip;
                    } else {
                        //LX.log("next DStrip was null");
                        nextAmount = 0f;
                    }
                }
            }
        }
        if (dStrip.forward) {
            pos += speed / 100f;
        } else {
            pos -= speed / 100f;
        }

        // Only force the bar update if we were heading past the bar.  For initial starts and things like
        // triangle waves, we would like to start of the edge.
        if ((pos < 0.0 && !dStrip.forward) || (pos > currentStripLength && dStrip.forward)) {
            needsCurrentBarUpdate = true;
            // LX.log("needs currentBarUpdate: " + pos + " dStrip=" + dStrip.vStrip.id + " forward=" + dStrip.forward);
        }

        if (needsCurrentBarUpdate) {
            updateCurrentDStrip(whichJoint);
        }

        if (waveStart > currentStripLength) {
            // If the start of the wave has left the end of the current strip then we have moved off the renderable
            // area.  Mark the flarelet as not renderable.
            enabled = false;
        }
        // If we are rendering on the next strip, we need to update the current dStrip to the next dStrip.

    }

    public void waveOnStrip(int[] colors, int color, LXColor.Blend blend) {
        if (!isRenderable()) return;
        renderWavetable(colors, dStrip, pos, color, blend);
    }

    public void renderWavetable(int[] colors, DStrip targetDStrip, float pos, int color, LXColor.Blend blend) {
        //lx.log("Flarelet renderWavetable at pos: " + pos + " width=" + waveWidth + " color=" + color + " dStrip=" + targetDStrip.vStrip.id);
        VStripRender.renderWavetable(colors, targetDStrip.vStrip, wavetable, pos, waveWidth, color, swatch, getFadeLevel(), blend);
    }
}
