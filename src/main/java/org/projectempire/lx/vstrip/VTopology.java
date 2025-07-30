package org.projectempire.lx.vstrip;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonObject;
import heronarts.lx.LX;

/**
 * Class for managing a virtual topology of strips.  The topology will track the joints between strips and allow for
 * rendering across multiple strips.
 */
public class VTopology {

    public List<VStrip> strips;

    public VTopology() {
        strips = new ArrayList<VStrip>();
    }

    public void addStrip(VStrip strip) {
        strips.add(strip);
    }

    public VStrip getVStrip(int id) {
        for (VStrip s : strips) {
            if (s.id == id) {
                return s;
            }
        }
        return null;
    }


    public void buildJoints(JsonObject startJoints, JsonObject endJoints) {
        //LX.log("startJoints: " + startJoints);
        //LX.log("endJoints: " + endJoints);
        for (VStrip thisVStrip : strips) {
            for (VStrip otherVStrip : strips) {
                if (startJoints.get(thisVStrip.id + "-" + otherVStrip.id) != null) {
                    int adjacentValue = startJoints.get(thisVStrip.id + "-" + otherVStrip.id).getAsInt();
                    if (adjacentValue == 1) {
                        //LX.log("Adding joint to start point: " + thisVStrip.id + " -> " + otherVStrip.id + " as adjacentValue: " + adjacentValue);
                        thisVStrip.myStartPointJoints.add(new VJoint(otherVStrip, true));
                    } else if (adjacentValue == 2) {
                        //LX.log("Adding joint to start point: " + thisVStrip.id + " -> " + otherVStrip.id + " as adjacentValue: " + adjacentValue);
                        thisVStrip.myStartPointJoints.add(new VJoint(otherVStrip, false));
                    }
                }
                if (endJoints.get(thisVStrip.id + "-" + otherVStrip.id) != null) {
                    int adjacentValue = endJoints.get(thisVStrip.id + "-" + otherVStrip.id).getAsInt();
                    if (adjacentValue == 1) {
                        //LX.log("Adding joint to end point: " + thisVStrip.id + " -> " + otherVStrip.id + " as adjacentValue: " + adjacentValue);
                        thisVStrip.myEndPointJoints.add(new VJoint(otherVStrip, true));
                    } else if (adjacentValue == 2) {
                        //LX.log("Adding joint to end point: " + thisVStrip.id + " -> " + otherVStrip.id + " as adjacentValue: " + adjacentValue);
                        thisVStrip.myEndPointJoints.add(new VJoint(otherVStrip, false));
                    }
                }
            }
        }
    }
}
