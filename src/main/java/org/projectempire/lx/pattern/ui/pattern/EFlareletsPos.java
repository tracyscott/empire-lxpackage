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
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EFlareletsPos extends LXPattern {
  public CompoundParameter maxValue = new CompoundParameter("maxv", 1.0, 0.0, 1.0);
  public CompoundParameter pRearPosition = new CompoundParameter("pRearPos", 0.0, -300.0, 300.0)
      .setDescription("Position for passenger center horn start flarelets");
  public CompoundParameter dRearPosition = new CompoundParameter("dRearPos", 0.0, -300.0, 300.0)
      .setDescription("Position for driver center horn start flarelets");
  public CompoundParameter pCenterEndPosition = new CompoundParameter("pCEndPos", 0.0, -300.0, 300.0)
      .setDescription("Position for passenger center horn end flarelets");
  public CompoundParameter dCenterEndPosition = new CompoundParameter("dCEndPos", 0.0, -300.0, 300.0)
      .setDescription("Position for driver center horn end flarelets");
  public CompoundParameter pFrontHornPosition = new CompoundParameter("pFrontPos", 0.0, -300.0, 300.0)
      .setDescription("Position for passenger front horn flarelets");
  public CompoundParameter dFrontHornPosition = new CompoundParameter("dFrontPos", 0.0, -300.0, 300.0)
      .setDescription("Position for driver front horn flarelets");
  public DiscreteParameter numFlarelets = new DiscreteParameter("flrlets", 4, 1, 5);
  public DiscreteParameter fxKnob = new DiscreteParameter("fx", 0, 0, 3).setDescription("0=none 1=sparkle 2=cosine");
  public CompoundParameter fxDepth = new CompoundParameter("fxDepth", 1.0f, 0.1f, 1.0f);
  public DiscreteParameter waveKnob = new DiscreteParameter("wave", 0, 0, WavetableLib.countWavetables()).setDescription("Waveform type");
  public CompoundParameter widthKnob = new CompoundParameter("width", 0.1f, 0.0f, 120.0f).setDescription("Square wave width");
  public CompoundParameter cosineFreq = new CompoundParameter("cfreq", 1.0, 0.05, 400.0);
  public ColorParameter colorKnob = new ColorParameter("color", 0xffffffff);
  public DiscreteParameter swatch = new DiscreteParameter("swatch", -1, -1, 20);
  public CompoundParameter spinePosition = new CompoundParameter("spinePos", 0.0, -300.0, 300.0)
      .setDescription("Position for spine flarelets");


  Wavetable triangleTable = new TriangleWavetable(128);

  public List<Flarelet> flarelets = new ArrayList<Flarelet>();

  double timeSinceLastPulse = 0.0;

  Set<Integer> spineIds = new HashSet<Integer>();
  Set<Integer> buttIds = new HashSet<Integer>();
  Set<Integer> nozzleIds = new HashSet<Integer>();
  Set<Integer> bubbleIds = new HashSet<Integer>();
  Set<Integer> grillIds = new HashSet<Integer>();
  Set<Integer> pCenterEndIds = new HashSet<Integer>();
  Set<Integer> dCenterEndIds = new HashSet<Integer>();
  Set<Integer> pFrontHornIds = new HashSet<Integer>();
  Set<Integer> dFrontHornIds = new HashSet<Integer>();
  Set<Integer> pRearIds = new HashSet<Integer>();
  Set<Integer> dRearIds = new HashSet<Integer>();

  public EFlareletsPos(LX lx) {
    super(lx);
    addParameter("maxv", maxValue);
    addParameter("pRearPos", pRearPosition);
    addParameter("dRearPos", dRearPosition);
    addParameter("pCEndPos", pCenterEndPosition);
    addParameter("dCEndPos", dCenterEndPosition);
    addParameter("pFrontPos", pFrontHornPosition);
    addParameter("dFrontPos", dFrontHornPosition);
    addParameter("flrlets", numFlarelets);
    addParameter("fx", fxKnob);
    addParameter("fxDepth", fxDepth);
    addParameter("wave", waveKnob);
    addParameter("width", widthKnob);
    addParameter("cfreq", cosineFreq);
    addParameter("color", colorKnob);
    addParameter("swatch", swatch);
    addParameter("spinePos", spinePosition);
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
    startPointList.addAll(Topology.pCenterHornEndStripIds);
    startPointList.addAll(Topology.dCenterHornEndStripIds);
    spineIds.addAll(Topology.cHornStripIds);
    pCenterEndIds.addAll(Topology.pCenterHornEndStripIds);
    dCenterEndIds.addAll(Topology.dCenterHornEndStripIds);
    pFrontHornIds.addAll(Topology.pFrontHornStripIds);
    dFrontHornIds.addAll(Topology.dFrontHornStripIds);
    pRearIds.addAll(Topology.pCenterHornStartStripIds);
    dRearIds.addAll(Topology.dCenterHornStartStripIds);
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
      float initPos = pRearPosition.getValuef() - widthKnob.getValuef() / 2.0f;
      flarelet.reset(Topology.getDefaultTopologies(lx).get(0), stripNum,initPos, 1.0f, true);
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

    for (LXPoint p : model.points) {
      colors[p.index] = LXColor.hsb(0, 0, 0);
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
      flarelet.speed = 0f;
      // Just leave them enabled in case we move them off and the back on.
      flarelet.enabled = true;
      // Set position directly instead of using speed
      if (spineIds.contains(flarelet.initDStrip.vStrip.id)) {
        flarelet.pos = spinePosition.getValuef();
      } else if (pCenterEndIds.contains(flarelet.initDStrip.vStrip.id)) {
        flarelet.pos = pCenterEndPosition.getValuef();
      } else if (dCenterEndIds.contains(flarelet.initDStrip.vStrip.id)) {
        flarelet.pos = dCenterEndPosition.getValuef();
      } else if (pFrontHornIds.contains(flarelet.initDStrip.vStrip.id)) {
        flarelet.pos = pFrontHornPosition.getValuef();
      } else if (dFrontHornIds.contains(flarelet.initDStrip.vStrip.id)) {
        flarelet.pos = dFrontHornPosition.getValuef();
      } else if (pRearIds.contains(flarelet.initDStrip.vStrip.id)) {
        flarelet.pos = pRearPosition.getValuef();
      } else if (dRearIds.contains(flarelet.initDStrip.vStrip.id)) {
        flarelet.pos = dRearPosition.getValuef();
      } else {
        flarelet.pos = pRearPosition.getValuef();
      }
      flarelet.waveOnTop(colors, LXColor.Blend.SCREEN, 0, true);
    }
    timeSinceLastPulse += deltaMs;
  }
}