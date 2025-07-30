package org.projectempire.lx.pattern.ui.pattern;

import org.projectempire.lx.vstrip.Topology;
import org.projectempire.lx.vstrip.Flarelet;
import org.projectempire.lx.wavetable.SineWavetable;
import org.projectempire.lx.wavetable.StepWavetable;
import org.projectempire.lx.wavetable.TriangleWavetable;
import org.projectempire.lx.wavetable.Wavetable;
import org.projectempire.lx.wavetable.WavetableLib;
import heronarts.lx.LX;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EFlareletsWT extends LXPattern {
  public CompoundParameter slope = new CompoundParameter("slope", 1.0, 0.001, 30.0);
  public CompoundParameter maxValue = new CompoundParameter("maxv", 1.0, 0.0, 1.0);
  public CompoundParameter speed = new CompoundParameter("speed", 40.0, 0.0, 1000.0);
  public CompoundParameter randSpeed = new CompoundParameter("randspd", 1.0, 0.0, 5.0);
  public DiscreteParameter numFlarelets = new DiscreteParameter("flrlets", 4, 1, 5);
  public DiscreteParameter nextStripKnob = new DiscreteParameter("nxtStrip", 0, -1, 2);
  public DiscreteParameter fxKnob = new DiscreteParameter("fx", 0, 0, 3).setDescription("0=none 1=sparkle 2=cosine");
  public CompoundParameter fxDepth = new CompoundParameter("fxDepth", 1.0f, 0.1f, 1.0f);
  public DiscreteParameter waveKnob = new DiscreteParameter("wave", 0, 0, WavetableLib.countWavetables()).setDescription("Waveform type");
  public CompoundParameter widthKnob = new CompoundParameter("width", 0.1f, 0.0f, 120.0f).setDescription("Square wave width");
  public CompoundParameter cosineFreq = new CompoundParameter("cfreq", 1.0, 0.05, 400.0);
  public ColorParameter colorKnob = new ColorParameter("color", 0xffffffff);
  public DiscreteParameter swatch = new DiscreteParameter("swatch", -1, -1, 20);
  public CompoundParameter pulseFreq = new CompoundParameter("pulseFreq", 3.0, 0.1, 10.0)
      .setDescription("Frequency of the pulse effect in seconds");
  public CompoundParameter spineSpeedMod = new CompoundParameter("spineSpdM", 1.2, 0.1, 10.0)
      .setDescription("Speed modifier for the spine effect");


  Wavetable triangleTable = new TriangleWavetable(128);

  public List<Flarelet> flarelets = new ArrayList<Flarelet>();

  double timeSinceLastPulse = 0.0;

  Set<Integer> spineIds = new HashSet<Integer>();
  Set<Integer> buttIds = new HashSet<Integer>();
  Set<Integer> nozzleIds = new HashSet<Integer>();
  Set<Integer> bubbleIds = new HashSet<Integer>();
  Set<Integer> grillIds = new HashSet<Integer>();

  public EFlareletsWT(LX lx) {
    super(lx);
    addParameter("slope", slope);
    addParameter("maxv", maxValue);
    addParameter("speed", speed);
    addParameter("flrlets", numFlarelets);
    addParameter("randspd", randSpeed);
    addParameter("nextStrip", nextStripKnob);
    addParameter("fx", fxKnob);
    addParameter("fxDepth", fxDepth);
    addParameter("wave", waveKnob);
    addParameter("width", widthKnob);
    addParameter("cfreq", cosineFreq);
    addParameter("color", colorKnob);
    addParameter("swatch", swatch);
    addParameter("pulseFreq", pulseFreq);
    addParameter("spineSpd", spineSpeedMod);
    triangleTable.generateWavetable(1f, 0f);
    Topology.getDefaultTopologies(lx).get(0);
    resetFlarelets();
  }

  /**
   * Create a matched set of flarelets on all 4 strips at the start of the center horn, the front horn, and
   * the driver side center horn and passenger side center horn.  Collect all the ids of the strips and then
   * start one on each id.
   */
  public void resetFlarelets() {
    flarelets.clear();
    List<Integer> startPointList = new ArrayList<Integer>();
    startPointList.addAll(Topology.dCenterHornStartStripIds);
    startPointList.addAll(Topology.dFrontHornStripIds);
    startPointList.addAll(Topology.pCenterHornStartStripIds);
    startPointList.addAll(Topology.pFrontHornStripIds);
    startPointList.addAll(Topology.cHornStripIds);
    spineIds.addAll(Topology.cHornStripIds);
    buttIds.addAll(Topology.buttStrips.stream()
            .map(vStrip -> vStrip.id)
            .toList());
    nozzleIds.addAll(Topology.nozzleStrips.stream()
            .map(vStrip -> vStrip.id)
            .toList());
    bubbleIds.addAll(Topology.bubbleStrips.stream()
            .map(vStrip -> vStrip.id)
            .toList());
    grillIds.addAll(Topology.grillStrips.stream()
            .map(vStrip -> vStrip.id)
            .toList());
    startPointList.addAll(buttIds);
    startPointList.addAll(nozzleIds);
    startPointList.addAll(bubbleIds);
    startPointList.addAll(grillIds);
    for (int i = 0; i < startPointList.size(); i++) {
      Flarelet flarelet = new Flarelet();
      flarelets.add(flarelet);
      int stripNum = startPointList.get(i);
      float initPos = 0.01f - widthKnob.getValuef() / 2.0f; // Start at the beginning of the wave.
      flarelet.reset(Topology.getDefaultTopologies(lx).get(0), stripNum,initPos, randSpeed.getValuef(), true);
      flarelet.color = colorKnob.getColor();
      flarelet.cloneAtJoints = true;
      flarelet.enabled = true;
    }
  }

  public void resetFlarelet(Flarelet flarelet) {
    flarelet.resetToInit();
    flarelet.color = colorKnob.getColor();
    flarelet.cloneAtJoints = true;
    flarelet.enabled = true;
  }

  /**
   * onActive is called when the pattern starts playing and becomes the active pattern.  Here we re-assigning
   * our speeds to generate some randomness in the speeds.
   */
  @Override
  public void onActive() {

    resetFlarelets();
  }

  @Override
  public void run(double deltaMs) {
    List<Flarelet> clonesToAdd = new ArrayList<Flarelet>();
    boolean resetFlarelets = false;

    if (timeSinceLastPulse >= pulseFreq.getValuef() * 1000.0) {
      timeSinceLastPulse = 0.0; // Reset the timer.
      resetFlarelets = true;
    }

    for (Flarelet flarelet : flarelets) {
      // Only put flarelets on the p center horn start point for now.
      flarelet.color = colorKnob.getColor();
      flarelet.wavetable = WavetableLib.getLibraryWavetable(waveKnob.getValuei());
      flarelet.fx = fxKnob.getValuei();
      flarelet.fxDepth = fxDepth.getValuef();
      flarelet.fxFreq = cosineFreq.getValuef();
      flarelet.waveWidth = widthKnob.getValuef();
      flarelet.swatch = swatch.getValuei();
      flarelet.speed = speed.getValuef();
      // Allow for the spine waves to be sped up to match the finish times.
      if (spineIds.contains(flarelet.initDStrip.vStrip.id))
        flarelet.speed *= spineSpeedMod.getValuef();
      flarelet.waveOnTopFan(colors, LXColor.Blend.ADD, nextStripKnob.getValuei(), true);
      if (!flarelet.cloneRequests.isEmpty()) {
        for (Flarelet.CloneRequest cloneRequest : flarelet.cloneRequests) {
          Flarelet clone = flarelet.cloneFlarelet(cloneRequest.dStrip, cloneRequest.pos);
          clonesToAdd.add(clone);
        }
        flarelet.cloneRequests.clear();
      }
    }

    // If a flarelet is not enabled, reset it if it is not a clone.  If it is a clone, delete it.
    for (int i = flarelets.size() - 1; i >= 0; i--) {
      Flarelet flarelet = flarelets.get(i);
      if (!flarelet.enabled) {
        if (!flarelet.isClone) {
          // We only want to reset the flarelet after our pulseFreq time has passed.
          if (resetFlarelets) {
            resetFlarelet(flarelet);
          }
        } else {
          flarelets.remove(i);
        }
      }
    }
    // Add all the clones
    flarelets.addAll(clonesToAdd);
    timeSinceLastPulse += deltaMs;
  }
}
