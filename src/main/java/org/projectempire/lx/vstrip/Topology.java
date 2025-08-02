package org.projectempire.lx.vstrip;

import com.google.gson.JsonObject;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.util.*;

/**
 * Topology for Interlace.
 *
 * We will create multiple topologies that can be used by patterns.  The most basic
 * topologies will be three topologies where the strips from our models are mapped
 * to virtual strips.
 *
 */
public class Topology {
  static Map<LXModel, List<VTopology>> modelTopologies = new HashMap<>();

  static public List<VStrip> grillStrips = new ArrayList<>(); // only one grill strip
  static public List<VStrip> bubbleStrips = new ArrayList<>();
  static public List<VStrip> pBubbleStrips = new ArrayList<>();
  static public List<VStrip> dBubbleStrips = new ArrayList<>();
  static public List<VStrip> nozzleStrips = new ArrayList<>();
  static public List<VStrip> buttStrips = new ArrayList<>();
  static public List<VStrip> pCenterHornStrips = new ArrayList<>();
  static public List<Integer> pCenterHornStripIds = new ArrayList<>();
  // Since we have split the center horn strips to make the joint with the rear horn,
  // keep track of the strip ids at the beginning of the horn for easy reference
  // when writing patterns.
  static public List<Integer> pCenterHornStartStripIds = new ArrayList<>();
  static public List<Integer> pCenterHornEndStripIds = new ArrayList<>();
  static public List<VStrip> pFrontHornStrips = new ArrayList<>();
  static public List<Integer> pFrontHornStripIds = new ArrayList<>();
  static public List<VStrip> pRearHornStrips = new ArrayList<>();
  static public List<Integer> pRearHornStripIds = new ArrayList<>();
  static public List<VStrip> dCenterHornStrips = new ArrayList<>();
  static public List<Integer> dCenterHornStripIds = new ArrayList<>();
  // Since we have split the center horn strips to make the joint with the rear horn,
  // keep track of the strip ids at the beginning of the horn for easy reference
  // when writing patterns.
  static public List<Integer> dCenterHornStartStripIds = new ArrayList<>();
  static public List<Integer> dCenterHornEndStripIds = new ArrayList<>();
  static public List<VStrip> dFrontHornStrips = new ArrayList<>();
  static public List<Integer> dFrontHornStripIds = new ArrayList<>();
  static public List<VStrip> dRearHornStrips = new ArrayList<>();
  static public List<Integer> dRearHornStripIds = new ArrayList<>();
  static public List<VStrip> cHornStrips = new ArrayList<>();
  static public List<Integer> cHornStripIds = new ArrayList<>();


  static public List<VTopology> getDefaultTopologies(LX lx) {
    LXModel model = lx.getModel();
    List<VTopology> defaultTopologies = modelTopologies.get(model);
    if (defaultTopologies == null) {
      defaultTopologies = createDefaultTopologies(lx);
      modelTopologies.put(model, defaultTopologies);
    }
    return defaultTopologies;
  }

  static public void resetTopologies(LX lx) {
    LXModel model = lx.getModel();
    dispose(lx);
    modelTopologies.remove(model);
  }

  /**
   * When disposing of a topology, remove all the references to LXPoints.
   * @param lx
   */
  static public void dispose(LX lx) {
    for (VTopology vTop : getDefaultTopologies(lx)) {
      for (VStrip vStrip : vTop.strips) {
        for (LVPoint lvPoint : vStrip.points) {
          if (lvPoint != null) lvPoint.p = null;
        }
      }
    }
  }

  static public List<VTopology> createDefaultTopologies(LX lx) {
    List<VTopology> topologies = new ArrayList<>();
    VTopology vTop = new VTopology();

    // In order to find each fixture, we will need to add specific tags
    // to each one since we can only filter the global model by tags and
    // not names. tag format: P Horn Front -> phornfront

    int stripId = mapHorns(lx, vTop, 0);
    stripId = mapButt(lx, vTop, stripId);
    stripId = mapNozzles(lx, vTop, stripId);
    stripId = mapBubbles(lx, vTop, stripId);
    stripId = mapGrill(lx, vTop, stripId);


    LX.log("Created " + stripId + " virtual strips for the default topology.");


    createDefaultJoints(vTop);
    topologies.add(vTop);

    LX.log("Number of topologies: " + topologies.size());

    // Output pCenterHornStripIds
    // LX.log("pCenterHornStripIds: " + pCenterHornStripIds);
    // pRearHornStripIds
    // LX.log("pRearHornStripIds: " + pRearHornStripIds);
    // dCenterHornStripIds
    // LX.log("dCenterHornStripIds: " + dCenterHornStripIds);
    // dRearHornStripIds
    // LX.log("dRearHornStripIds: " + dRearHornStripIds);

    // Butt strip ids
    // LX.log("Butt vStrip ids: " + buttStrips.stream()
    //  .map(vStrip -> vStrip.id)
    //  .toList());

    // Nozzle strip ids
    // LX.log("Nozzle vStrip ids: " + nozzleStrips.stream()
    //  .map(vStrip -> vStrip.id)
    //  .toList());

    // Bubble strip ids
    // LX.log("Bubble vStrip ids: " + bubbleStrips.stream()
    //  .map(vStrip -> vStrip.id)
    //  .toList());

    // Grill strip id
    // LX.log("Grill vStrip id: " + grillStrips.stream()
    //  .map(vStrip -> vStrip.id)
    //  .toList());

    return topologies;
  }

  /**
   * There is only 1 grill strip.  Just sticking to the standard code structure for consistency.
   *
   * @param lx
   * @param vTop
   * @param stripId
   * @return
   */
  static public int mapGrill(LX lx, VTopology vTop, int stripId) {
    VStrip grillStrip = addTagToVTopology(lx.getModel(), vTop, "grill", stripId++);
    grillStrips.add(grillStrip);
    return stripId;
  }

  /**
   * Bubble tags: pbubblei pubbleo dbubblei dbubbleo
   * @param lx
   * @param vTop
   * @param stripId
   * @return
   */
  static public int mapBubbles(LX lx, VTopology vTop, int stripId) {
    VStrip pbubblei = addTagToVTopology(lx.getModel(), vTop, "pbubblei", stripId++);
    pBubbleStrips.add(pbubblei);
    bubbleStrips.add(pbubblei);
    VStrip pubbleo = addTagToVTopology(lx.getModel(), vTop, "pbubbleo", stripId++);
    pBubbleStrips.add(pubbleo);
    bubbleStrips.add(pubbleo);
    VStrip dbubblei = addTagToVTopology(lx.getModel(), vTop, "dbubblei", stripId++);
    dBubbleStrips.add(dbubblei);
    bubbleStrips.add(dbubblei);
    VStrip dbubbleo = addTagToVTopology(lx.getModel(), vTop, "dbubbleo", stripId++);
    dBubbleStrips.add(dbubbleo);
    bubbleStrips.add(dbubbleo);
    return stripId;
  }

  /**
   * Nozzle tags: nozzle1 nozzle2 nozzle3 nozzle4 nozzle5
   * @param lx
   * @param vTop
   * @param stripId
   * @return
   */
  static public int mapNozzles(LX lx, VTopology vTop, int stripId) {
    nozzleStrips.add(addTagToVTopology(lx.getModel(), vTop, "nozzle1", stripId++));
    nozzleStrips.add(addTagToVTopology(lx.getModel(), vTop, "nozzle2", stripId++));
    nozzleStrips.add(addTagToVTopology(lx.getModel(), vTop, "nozzle3", stripId++));
    nozzleStrips.add(addTagToVTopology(lx.getModel(), vTop, "nozzle4", stripId++));
    nozzleStrips.add(addTagToVTopology(lx.getModel(), vTop, "nozzle5", stripId++));
    return stripId;
  }

  /**
   * Butt tags: buttbottom1 butttop1 buttbottom2 butttop2 buttbottom3 butttop3 buttbottom4 butttop4
   * buttbottom5 butttop5 butt6 butt7 butt8 butt9 butt10
   * @param lx
   * @param vTop
   * @param stripId
   * @return
   */
  static public int mapButt(LX lx, VTopology vTop, int stripId) {
    VStrip butt1 = addTagToVTopology(lx.getModel(), vTop, "buttbottom1", stripId++);
    addTagToVStripReverse(lx.getModel(), butt1, "butttop1");
    buttStrips.add(butt1);
    VStrip butt2 = addTagToVTopology(lx.getModel(), vTop, "buttbottom2", stripId++);
    addTagToVStripReverse(lx.getModel(), butt2, "butttop2");
    buttStrips.add(butt2);
    VStrip butt3 = addTagToVTopology(lx.getModel(), vTop, "buttbottom3", stripId++);
    addTagToVStripReverse(lx.getModel(), butt3, "butttop3");
    buttStrips.add(butt3);
    VStrip butt4 = addTagToVTopology(lx.getModel(), vTop, "buttbottom4", stripId++);
    addTagToVStripReverse(lx.getModel(), butt4, "butttop4");
    buttStrips.add(butt4);
    VStrip butt5 = addTagToVTopology(lx.getModel(), vTop, "buttbottom5", stripId++);
    addTagToVStripReverse(lx.getModel(), butt5, "butttop5");
    buttStrips.add(butt5);

    // Butt 6, 7, 8, 9, 10 are just single strips.
    buttStrips.add(addTagToVTopologyReverse(lx.getModel(), vTop, "butt6", stripId++));
    buttStrips.add(addTagToVTopologyReverse(lx.getModel(), vTop, "butt7", stripId++));
    buttStrips.add(addTagToVTopologyReverse(lx.getModel(), vTop, "butt8", stripId++));
    buttStrips.add(addTagToVTopologyReverse(lx.getModel(), vTop, "butt9", stripId++));
    buttStrips.add(addTagToVTopologyReverse(lx.getModel(), vTop, "butt10", stripId++));

    return stripId;
  }

  static public int mapHorns(LX lx, VTopology vTop, int stripId) {
    // Create 4 virtual strips that are the center/top/back
    //  C Horn Top, C Horn Bottom, C Horn Pass, C Horn Drive
    //
    cHornStripIds.add(stripId);
    cHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "chorntop", stripId++));
    cHornStripIds.add(stripId);
    cHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "chornbottom", stripId++));
    cHornStripIds.add(stripId);
    cHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "chornpass", stripId++));
    cHornStripIds.add(stripId);
    cHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "chorndrive", stripId++));


    // Create 3 virtual strips for the passenger side horn
    // P Horn Front, P Horn Rear, P Horn Center
    // Each horn model part is one set of points that ends up being 4
    // strips arranged in top, bottom, left, and right.  So we need to
    // create 4 virtual strips for each horn part.  We should specify
    // the start index and end index inclusive, and also whether it is
    // forwards or backwards.
    pFrontHornStripIds.add(stripId);
    pFrontHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phornfront", stripId++, 0, 121, true));
    pFrontHornStripIds.add(stripId);
    pFrontHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phornfront", stripId++, 122, 243, false));
    pFrontHornStripIds.add(stripId);
    pFrontHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phornfront", stripId++, 244, 364, true));
    pFrontHornStripIds.add(stripId);
    pFrontHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phornfront", stripId++, 365, 486, false));

    pRearHornStripIds.add(stripId);
    pRearHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phornrear", stripId++, 0, 101, true));
    pRearHornStripIds.add(stripId);
    pRearHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phornrear", stripId++, 102, 202, false));
    pRearHornStripIds.add(stripId);
    pRearHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phornrear", stripId++, 203, 304, true));
    pRearHornStripIds.add(stripId);
    pRearHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phornrear", stripId++, 305, 405, false));

    // For each of the 4 horn strips, we need to split them into virtual strips where the rear horn strips
    // meet up.
    pCenterHornStripIds.add(stripId);
    pCenterHornStartStripIds.add(stripId);
    pCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phorncenter", stripId++, 0, 37, true));

    pCenterHornStripIds.add(stripId);
    pCenterHornEndStripIds.add(stripId);
    pCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phorncenter", stripId++, 38, 145, true));

    // 291 - 38 = 253
    pCenterHornStripIds.add(stripId);
    pCenterHornEndStripIds.add(stripId);
    pCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phorncenter", stripId++, 146, 253, false));

    pCenterHornStartStripIds.add(stripId);
    pCenterHornStripIds.add(stripId);
    pCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phorncenter", stripId++, 254, 291, false));

    // 292 + 37 = 329
    pCenterHornStripIds.add(stripId);
    pCenterHornStartStripIds.add(stripId);
    pCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phorncenter", stripId++, 292, 329, true));

    pCenterHornEndStripIds.add(stripId);
    pCenterHornStripIds.add(stripId);
    pCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phorncenter", stripId++, 330, 438, true));

    // 584 - 38 = 546
    pCenterHornEndStripIds.add(stripId);
    pCenterHornStripIds.add(stripId);
    pCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phorncenter", stripId++, 439, 546, false));

    pCenterHornStartStripIds.add(stripId);
    pCenterHornStripIds.add(stripId);
    pCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "phorncenter", stripId++, 547, 584, false));

    // Create 3 virtual strips for the driver side horn
    // D Horn Front, D Horn Rear, D Horn Center
    dFrontHornStripIds.add(stripId);
    dFrontHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhornfront", stripId++, 0, 115, true));
    dFrontHornStripIds.add(stripId);
    dFrontHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhornfront", stripId++, 116, 230, false));
    dFrontHornStripIds.add(stripId);
    dFrontHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhornfront", stripId++, 231, 346, true));
    dFrontHornStripIds.add(stripId);
    dFrontHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhornfront", stripId++, 347, 462, false));

    dRearHornStripIds.add(stripId);
    dRearHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhornrear", stripId++, 0, 95, true));
    dRearHornStripIds.add(stripId);
    dRearHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhornrear", stripId++, 96, 192, false));
    dRearHornStripIds.add(stripId);
    dRearHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhornrear", stripId++, 193, 289, true));
    dRearHornStripIds.add(stripId);
    dRearHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhornrear", stripId++, 290, 385, false));

    // Split dhorncenter into 2 partial strips each so that we can create a joint.  Intersection at 37 leds.
    dCenterHornStartStripIds.add(stripId);
    dCenterHornStripIds.add(stripId);
    dCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhorncenter", stripId++, 0, 37, true));

    dCenterHornEndStripIds.add(stripId);
    dCenterHornStripIds.add(stripId);
    dCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhorncenter", stripId++, 38, 151, true));

    // 297 - 38 = 259
    dCenterHornEndStripIds.add(stripId);
    dCenterHornStripIds.add(stripId);
    dCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhorncenter", stripId++, 152, 259, false));

    dCenterHornStartStripIds.add(stripId);
    dCenterHornStripIds.add(stripId);
    dCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhorncenter", stripId++, 260, 297, false));

    // 298 + 37 = 335
    dCenterHornStartStripIds.add(stripId);
    dCenterHornStripIds.add(stripId);
    dCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhorncenter", stripId++, 298, 335, true));

    dCenterHornEndStripIds.add(stripId);
    dCenterHornStripIds.add(stripId);
    dCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhorncenter", stripId++, 336, 446, true));

    // 593 - 38 = 555
    dCenterHornEndStripIds.add(stripId);
    dCenterHornStripIds.add(stripId);
    dCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhorncenter", stripId++, 447, 555, false));

    dCenterHornStartStripIds.add(stripId);
    dCenterHornStripIds.add(stripId);
    dCenterHornStrips.add(addTagToVTopology(lx.getModel(), vTop, "dhorncenter", stripId++, 556, 593, false));


    return stripId;
  }

  static public VStrip addTagToVTopology(LXModel model, VTopology vTop, String tag, int stripId,
                                       int startIndex, int endIndex, boolean forward) {
    LXModel stripToAdd = model.sub(tag, 0);
    if (stripToAdd == null) {
      LX.error("Could not find strip with tag: " + tag);
      return null;
    }
    VStrip newVStrip = new VStrip(stripId);
    addStripToVStrip(newVStrip, stripToAdd, startIndex, endIndex, forward);
    newVStrip.normalize();
    vTop.addStrip(newVStrip);
    //LX.log("Added points to " + tag + " vStrip from index " +
    //       startIndex + " to " + endIndex + " forward: " + forward + " stripId: " + stripId);
    return newVStrip;
  }


  static public VStrip addTagToVTopology(LXModel model, VTopology vTop, String tag, int stripId) {
    LXModel stripToAdd = model.sub(tag, 0);
    if (stripToAdd == null) {
      LX.error("Could not find strip with tag: " + tag);
      return null;
    }
    VStrip newVStrip = new VStrip(stripId);
    addStripToVStrip(newVStrip, stripToAdd);
    newVStrip.normalize();
    vTop.addStrip(newVStrip);
    //LX.log("Added " + stripToAdd.points.length + " points to " + tag + " vStrip stripId: " + stripId);
    return newVStrip;
  }

  static public VStrip addTagToVTopologyReverse(LXModel model, VTopology vTop, String tag, int stripId) {
    LXModel stripToAdd = model.sub(tag, 0);
    if (stripToAdd == null) {
      LX.error("Could not find strip with tag: " + tag);
      return null;
    }
    VStrip newVStrip = new VStrip(stripId);
    addStripToVStripReverse(newVStrip, stripToAdd);
    newVStrip.normalize();
    vTop.addStrip(newVStrip);
    //LX.log("Added " + stripToAdd.points.length + " points to " + tag + " vStrip stripId: " + stripId);
    return newVStrip;
  }

  /**
   * This is for taking multiple fixtures and adding them to a single vStrip.  This is useful for butt half
   * sections.
   */
  static public VStrip addTagToVStrip(LXModel model, VStrip targetStrip, String tag) {
    LXModel stripToAdd = model.sub(tag, 0);
    if (stripToAdd == null) {
      LX.error("Could not find strip with tag: " + tag);
      return null;
    }
    addStripToVStrip(targetStrip, stripToAdd);
    targetStrip.normalize();
    return targetStrip;
  }

  static public VStrip addTagToVStripReverse(LXModel model, VStrip targetStrip, String tag) {
    LXModel stripToAdd = model.sub(tag, 0);
    if (stripToAdd == null) {
      LX.error("Could not find strip with tag: " + tag);
      return null;
    }
    addStripToVStripReverse(targetStrip, stripToAdd);
    targetStrip.normalize();
    return targetStrip;
  }

  /**
   * Given a LXModel that is a strip, compute the point spacing.  We will need this to
   * build our virtual strips.
   *
   * @param strip
   * @return
   */
  static public float computeSpacing(LXModel strip) {
    if (strip.points.length < 2) {
      return 1f;
    }
    // Just use the first 2 points since it is expected to be consistent between all points.
    float dist = (float)Math.sqrt(
      (strip.points[0].x - strip.points[1].x) * (strip.points[0].x - strip.points[1].x) +
      (strip.points[0].y - strip.points[1].y) * (strip.points[0].y - strip.points[1].y) +
      (strip.points[0].z - strip.points[1].z) * (strip.points[0].z - strip.points[1].z)
    );

    return dist;
  }

  static public void addStripToVStrip(VStrip vStrip, LXModel strip, int startIndex,
                                      int endIndex, boolean forward) {
    float spacing = computeSpacing(strip);
    if (forward) {
      for (int i = startIndex; i <= endIndex && i < strip.points.length; i++) {
        vStrip.addPoint(strip.points[i], spacing);
      }
    } else {
      for (int i = endIndex; i >= startIndex && i >= 0; i--) {
        vStrip.addPoint(strip.points[i], spacing);
      }
    }
  }

  static public void addStripToVStrip(VStrip vStrip, LXModel strip) {
    float spacing = computeSpacing(strip);
    for (LXPoint p : strip.points) {
      vStrip.addPoint(p, spacing);
    }
  }

  static public void addStripToVStripReverse(VStrip vStrip, LXModel strip) {
    float spacing = computeSpacing(strip);
    for (int i = strip.points.length - 1; i >= 0; i--) {
      vStrip.addPoint(strip.points[i], spacing);
    }
  }

  // Mapping between vstrip id's and their joints to other vstrips.  This is a mapping of the form:
  // vstripId1, vstripId2, *ADJACENCY* where ADJACENCY is:
  // 1 if the adjacent vstrip is adjacent at a start point.
  // 2 if the adjacent vstrip is adjacent at an end point.
  // Make the rear horn strips connect to the center horn strips.
  // Passenger Center Inner most horn strip ids are 12, 15, 16, 19
  // Passenger Center Outer horn strips ids are 13, 14, 17, 18
  // Passenger Rear horn strip ids are 8, 9, 10, 11
  // Driver Center Inner most horn strip ids are 28, 31, 32, 35
  // Driver Center Outer horn strips ids are 29, 30, 33, 34
  // Driver Rear horn strip ids are 24, 25, 26, 27
  // NOTE(tracy): We currently have no joint between the rear horns and the outer center horns as there
  // is no pattern that requires it.  If we do need to add a joint, we can add it later.  For example,
  // if we want to start at the rear horn and fan out to both sides of the passenger/driver center horn.
  static public int[] hornStartPointJoints = {
          // Passenger side rear horn
          8, 12, 2,
          9, 15, 2,
          10, 16, 2,
          11, 19, 2,
          // Passenger side center horn outer
          13, 12, 2,
          14, 15, 2,
          17, 16, 2,
          18, 19, 2,
          // Driver side rear horn
          24, 28, 2,
          25, 31, 2,
          26, 32, 2,
          27, 35, 2,
          // Driver side center horn outer
          29, 28, 2,
          30, 31, 2,
          33, 32, 2,
          34, 35, 2
  };

  // For each strip's end point, match the strip to connect to.  Since we don't have any complex wrap-around scenarios
  // here currently, the end point of one strip is always connected to the start of another strip.
  static public int[] hornEndPointJoints = {
          // Passenger side center horn inner
          12, 8, 1,
          15, 9, 1,
          16, 10, 1,
          19, 11, 1,
          12, 13, 1,
          15, 14, 1,
          16, 17, 1,
          19, 18, 1,
          // Driver side center horn inner
          28, 24, 1,
          31, 25, 1,
          32, 26, 1,
          35, 27, 1,
          28, 29, 1,
          31, 30, 1,
          32, 33, 1,
          35, 34, 1
  };

  static public List<Integer> mapSelfConnectedStartJoints(List<VStrip> vStrips) {
    List<Integer> selfConnectedStartJoints = new ArrayList<>();
    for (VStrip vStrip : vStrips) {
      // Each strip is a single strip, so we can just map it to itself.
      selfConnectedStartJoints.add(vStrip.id);
      selfConnectedStartJoints.add(vStrip.id);
      selfConnectedStartJoints.add(2); // 2 means that the start of the strip is connected to the end of the strip.
    }
    return selfConnectedStartJoints;
  }

    static public List<Integer> mapSelfConnectedEndJoints(List<VStrip> vStrips) {
        List<Integer> selfConnectedEndJoints = new ArrayList<>();
        for (VStrip vStrip : vStrips) {
        // Each strip is a single strip, so we can just map it to itself.
        selfConnectedEndJoints.add(vStrip.id);
        selfConnectedEndJoints.add(vStrip.id);
        selfConnectedEndJoints.add(1);  // 1 means the end of the strip is connected to the start of the strip.
        }
        return selfConnectedEndJoints;
    }

  static public JsonObject createStartPointJoints(int[] startPointJoints) {
    JsonObject startJoints = new JsonObject();
    for (int i = 0; i < startPointJoints.length; i+=3) {
      startJoints.addProperty(startPointJoints[i] + "-" + startPointJoints[i+1], startPointJoints[i+2]);
    }
    return startJoints;
  }

  static public JsonObject createEndPointJoints(int[] endPointJoints) {
    JsonObject endJoints = new JsonObject();
    for (int i = 0; i < endPointJoints.length; i+=3) {
      endJoints.addProperty(endPointJoints[i] + "-" + endPointJoints[i+1], endPointJoints[i+2]);
    }
    return endJoints;
  }

  static public void createDefaultJoints(VTopology vTop) {
    // LX.log("Creating default joints for topology: " + vTop);
    List<Integer> buttStartJoints = mapSelfConnectedStartJoints(buttStrips);
    List<Integer> buttEndJoints = mapSelfConnectedEndJoints(buttStrips);
    // Nozzle joints
    List<Integer> nozzleStartJoints = mapSelfConnectedStartJoints(nozzleStrips);
    List<Integer> nozzleEndJoints = mapSelfConnectedEndJoints(nozzleStrips);
    List<Integer> bubbleStartJoints = mapSelfConnectedStartJoints(bubbleStrips);
    List<Integer> bubbleEndJoints = mapSelfConnectedEndJoints(bubbleStrips);
    List<Integer> grillStartJoints = mapSelfConnectedStartJoints(grillStrips);
    List<Integer> grillEndJoints = mapSelfConnectedEndJoints(grillStrips);
    List<Integer> hornStartJoints = new ArrayList<>();
    List<Integer> hornEndJoints = new ArrayList<>();
    for (int i = 0; i < hornStartPointJoints.length; i+=3) {
      hornStartJoints.add(hornStartPointJoints[i]);
      hornStartJoints.add(hornStartPointJoints[i+1]);
      hornStartJoints.add(hornStartPointJoints[i+2]);
    }
    for (int i = 0; i < hornEndPointJoints.length; i+=3) {
      hornEndJoints.add(hornEndPointJoints[i]);
      hornEndJoints.add(hornEndPointJoints[i+1]);
      hornEndJoints.add(hornEndPointJoints[i+2]);
    }
    List<Integer> startPointJoints = new ArrayList<>();
    List<Integer> endPointJoints = new ArrayList<>();
    startPointJoints.addAll(buttStartJoints);
    startPointJoints.addAll(nozzleStartJoints);
    startPointJoints.addAll(bubbleStartJoints);
    startPointJoints.addAll(hornStartJoints);
    startPointJoints.addAll(grillStartJoints);
    endPointJoints.addAll(buttEndJoints);
    endPointJoints.addAll(nozzleEndJoints);
    endPointJoints.addAll(bubbleEndJoints);
    endPointJoints.addAll(hornEndJoints);
    endPointJoints.addAll(grillEndJoints);

    int[] startJointsArray = new int[startPointJoints.size()];
    for (int i = 0; i < startPointJoints.size(); i++) {
      startJointsArray[i] = startPointJoints.get(i);
    }
    int[] endJointsArray = new int[endPointJoints.size()];
    for (int i = 0; i < endPointJoints.size(); i++) {
      endJointsArray[i] = endPointJoints.get(i);
    }
    vTop.buildJoints(createStartPointJoints(startJointsArray), createEndPointJoints(endJointsArray));
  }
}
