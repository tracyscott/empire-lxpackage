package org.projectempire.lx.pattern.ui.pattern;

import org.projectempire.lx.vstrip.LVPoint;
import org.projectempire.lx.vstrip.VStrip;
import org.projectempire.lx.vstrip.VTopology;
import org.projectempire.lx.vstrip.Topology;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

public class VStripSel extends LXPattern {

  DiscreteParameter topology = new DiscreteParameter("top", 0, 0, 2);
  DiscreteParameter strip = new DiscreteParameter("strip", 0, -1, 75);
  DiscreteParameter ledNum = new DiscreteParameter("ledNum", -1, -1, 700);

  VTopology vTop;

  public VStripSel(LX lx) {
    super(lx);
    addParameter("top", topology);
    addParameter("strip", strip);
    addParameter("ledNum", ledNum);
  }

  public void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = LXColor.BLACK;
    }

    vTop = Topology.getDefaultTopologies(lx).get(0);
    if (topology.getValuei() < Topology.getDefaultTopologies(lx).size()) {
      vTop = Topology.getDefaultTopologies(lx).get(topology.getValuei());
    }

    for (VStrip vStrip : vTop.strips) {
      if (vStrip.id == strip.getValuei() || strip.getValuei() == -1) {
        int i = 0;
        for (LVPoint p : vStrip.points) {
          if (ledNum.getValuei() == -1 || i <= ledNum.getValuei())
            colors[p.p.index] = LXColor.WHITE;
          i++;
        }
      }
    }
  }
}
