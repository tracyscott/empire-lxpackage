package org.projectempire.lx.osc;

import heronarts.lx.LXPlugin;
import heronarts.lx.mixer.LXAbstractChannel;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.LX;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

@LXPlugin.Name("QLab OSC Listener")
public class QLabClips implements LXPlugin, LXOscListener, LXParameterListener {

    public static final int RECV_OSC_PORT = 53001;
    public static final int QLAB_OSC_PORT = 53000;
    public static final String OSC_IP = "192.168.4.41";
    protected LX lx;
    protected LXOscEngine.Transmitter qLabOscTransmitter;

    @Override
    public void initialize(heronarts.lx.LX lx) {
        this.lx = lx;
        // Initialization logic for QLabClips
        try {
            // Register for custom OSC messages on a dedicated port. Unless otherwise, QLabs reponds on
            // it's port + 1 and it's default port is 53000.
            LXOscEngine.Receiver r = lx.engine.osc.receiver(RECV_OSC_PORT).addListener(this);
            // Optionally, you can set up a transmitter to send OSC messages back
            LX.log("QLabClips initialized with OSC receiver on port: " + RECV_OSC_PORT);
        } catch (java.net.SocketException sx) {
            throw new RuntimeException(sx);
        }
        startOscTransmitter(OSC_IP, QLAB_OSC_PORT, 1024);
    }

    public void startOscTransmitter(String address, int port, int bufferSize) {
        try {
            qLabOscTransmitter = lx.engine.osc.transmitter(InetAddress.getByName(address), port, bufferSize);
            LX.log("OSC Sender enabled, destination: " + address + ":" + port);
        } catch (UnknownHostException unhex) {
            LX.log("UnknownHostException creating OSC Transmitter: " + unhex.getMessage());
        } catch (SocketException sex) {
            LX.log("SocketException creating OSC Transmitter: " + sex.getMessage());
        }
        // Send a /listen command
        // Send a text message of /listen to QLab
        try {
            qLabOscTransmitter.send(new heronarts.lx.osc.OscMessage("/listen"));
        } catch (IOException ioex) {
            LX.log("IOException sending /listen message: " + ioex.getMessage());
        } catch (Exception ex) {
            LX.log("Exception sending /listen message: " + ex.getMessage());
        }
    }

    @Override
    public void oscMessage(heronarts.lx.osc.OscMessage message) {
        // Handle incoming OSC messages related to QLab clips
        // This could include parsing the message and executing corresponding actions
        // Just print out the message and the arguments to the log.
        LX.log("Received OSC message: " + message.getAddressPattern() + " with arguments: " );
        for (int i = 0; i < message.size(); i++) {
            LX.log("Argument " + i + ": " + message.get(i));
        }
        if ("/qlab/event/workspace/go".equalsIgnoreCase(message.getAddressPattern().toString())) {
            // Extract the cue name, search for a matching clip, and trigger it.
            String cueName = message.get(1).toString();
            LX.log("Triggering QLab cue: " + cueName);
            // Search all channels in the mixer.  Each channel will have a clips member.  The clips member will
            // have a .label() that is the label that should match cueName.
            for (LXAbstractChannel channel : lx.engine.mixer.channels) {
                LX.log("Found channel: " + channel.label.getString());
                if (channel.clips != null) {
                    for (heronarts.lx.clip.LXClip clip : channel.clips) {
                        if (clip == null) continue;
                        LX.log("Found clip: " + clip.label.toString());
                        if (clip.label.getString().equalsIgnoreCase(cueName)) {
                            LX.log("Found matching clip: " + clip.label);
                            // Trigger the clip
                            clip.trigger();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onParameterChanged(heronarts.lx.parameter.LXParameter parameter) {
        // Handle parameter changes if necessary
    }

}
