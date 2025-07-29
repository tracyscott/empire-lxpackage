package org.projectempire.lx.osc;

import heronarts.glx.ui.UI2dComponent;
import heronarts.lx.LXComponent;
import heronarts.lx.LXPlugin;
import heronarts.lx.mixer.LXAbstractChannel;
import heronarts.lx.osc.*;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.LX;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.studio.LXStudio;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

@LXPlugin.Name("QLab Show Control")
public class QLabClips extends LXComponent implements LXStudio.Plugin, LXOscListener, LXParameterListener, LX.ProjectListener {

    public static final int RECV_OSC_PORT = 53001;
    public static final int QLAB_OSC_PORT = 53000;
    public static final String QLAB_IP = "127.0.0.1";
    protected LX lx;
    protected LXOscEngine.Transmitter qLabOscTransmitter;
    LXOscEngine.Receiver receiver;
    // Wait until UI is shown before starting the OSC receiver and transmitter since we are initially
    // created with default parameters and then the lxp project file will bind new parameters.  Otherwise
    // we get a situation with the network being started, stopped, and restarted.
    public boolean isReady = false;

    public BooleanParameter enabled =
        new BooleanParameter("Enabled", false)
            .setDescription("Enable or disable the QLab OSC receiver and transmitter");
    public StringParameter qLabIp =
        new StringParameter("QLab IP", QLAB_IP)
            .setDescription("IP address of the QLab OSC receiver");
    public StringParameter qLabOscPort =
        new StringParameter("QLab OSC Port", String.valueOf(QLAB_OSC_PORT))
            .setDescription("Port number for QLab OSC communication");
    public StringParameter recvOscPort =
        new StringParameter("Receive OSC Port", String.valueOf(RECV_OSC_PORT))
            .setDescription("Port number for receiving OSC messages from QLab");
    public BooleanParameter targetClips =
        new BooleanParameter("Clips", true)
            .setDescription("If enabled, the plugin will target clips in the LX engine that match the cue name from QLab");
    // TODO(tracy): Allow this plugin to target cues from cues.lxp
    //public BooleanParameter targetCues =
    //    new BooleanParameter("Cues", true)
    //        .setDescription("If enabled, the plugin will target cues from cues.lxp match the cue name from QLab");

    @Override
    public void initialize(heronarts.lx.LX lx) {
        this.lx = lx;
        addParameter("enabled", enabled);
        addParameter("qLabIp", qLabIp);
        addParameter("qLabOscPort", qLabOscPort);
        addParameter("recvOscPort", recvOscPort);
        addParameter("clips", targetClips);
        //addParameter("cues", targetCues);
        lx.engine.registerComponent("qlabclips", this);
        lx.addProjectListener(this);
    }

    public void restartNetwork() {
        if (!isReady) return;

        if (!enabled.isOn()) {
            LX.log("QLabClips is disabled, not restarting network.");
            return;
        }
        stopNetwork();
        startOscListener();
        startOscTransmitter(qLabIp.getString(), Integer.parseInt(qLabOscPort.getString()), 1024);
    }

    public void stopNetwork() {
        // Stop the OSC receiver and transmitter if they are running.
        if (receiver != null) {
            receiver.removeListener(this);
            receiver.stop();
            receiver = null;
        }
        qLabKeepAlive(false);
        if (qLabOscTransmitter != null) {
            qLabOscTransmitter.dispose();
            qLabOscTransmitter = null;
        }
        LX.log("QLabClips network stopped.");
    }

    public void startOscListener() {
        // If we have an existing receiver, clean it up.
        if (receiver != null) {
            receiver.removeListener(this);
            receiver.stop();
            return;
        }
        try {
            // Register for custom OSC messages on a dedicated port. Unless otherwise, QLabs reponds on
            // it's port + 1 and it's default port is 53000.
            receiver = lx.engine.osc.receiver(RECV_OSC_PORT).addListener(this);
            // Optionally, you can set up a transmitter to send OSC messages back
            LX.log("QLabClips initialized with OSC receiver on port: " + RECV_OSC_PORT);
        } catch (java.net.SocketException sx) {
            throw new RuntimeException(sx);
        }
    }

    public void startOscTransmitter(String address, int port, int bufferSize) {
        if (qLabOscTransmitter != null) {
            // If we have an existing transmitter, clean it up.
            qLabOscTransmitter.dispose();
        }
        try {
            qLabOscTransmitter = lx.engine.osc.transmitter(InetAddress.getByName(address), port, bufferSize);
            LX.log("QLab OSC Sender enabled, destination: " + address + ":" + port);
        } catch (UnknownHostException unhex) {
            LX.log("UnknownHostException creating QLab OSC Transmitter: " + unhex.getMessage());
        } catch (SocketException sex) {
            LX.log("SocketException creating QLab OSC Transmitter: " + sex.getMessage());
        }
        qLabListen();
        qLabKeepAlive(true);
    }

    protected void qLabListen() {
        if (qLabOscTransmitter == null)
            return;
        // Send a /listen command and a /udpKeepAlive message.
        try {
            qLabOscTransmitter.send(new heronarts.lx.osc.OscMessage("/listen"));
        } catch (IOException ioex) {
            LX.log("IOException sending /listen message: " + ioex.getMessage());
        } catch (Exception ex) {
            LX.log("Exception sending /listen message: " + ex.getMessage());
        }
    }

    protected void qLabKeepAlive(boolean b) {
        if (qLabOscTransmitter == null)
            return;
        try {
            heronarts.lx.osc.OscMessage keepAliveMessage = new heronarts.lx.osc.OscMessage("/udpKeepAlive");
            OscArgument arg;
            if (b) arg = new OscTrue();
            else arg = new OscFalse();
            keepAliveMessage.add(arg); // Add a boolean argument to the message
            qLabOscTransmitter.send(keepAliveMessage);
        } catch (IOException ioex) {
            LX.log("IOException sending /udpKeepAlive message: " + ioex.getMessage());
        } catch (Exception ex) {
            LX.log("Exception sending /udpKeepAlive message: " + ex.getMessage());
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

            if (targetClips.isOn()) {
                // Search all channels in the mixer.  Each channel will have a clips member.  The clips member will
                // have a .label() that is the label that should match cueName.
                for (LXAbstractChannel channel : lx.engine.mixer.channels) {
                    if (channel.clips != null) {
                        for (heronarts.lx.clip.LXClip clip : channel.clips) {
                            if (clip == null) continue;
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
    }

    @Override
    public void onParameterChanged(heronarts.lx.parameter.LXParameter parameter) {
        // Handle parameter changes if necessary
    }

    @Override
    public void initializeUI(LXStudio lxStudio, LXStudio.UI ui) {
    }

    @Override
    public void onUIReady(LXStudio lxStudio, LXStudio.UI ui) {
        UIQLabClips uiQLabClips = (UIQLabClips)new UIQLabClips(lxStudio, ui, this, ui.leftPane.global.getContentWidth())
                .addToContainer(ui.leftPane.global);
        uiQLabClips.setExpanded(false);
        // This is not the correct place to start the network.  We are still getting multiple starts
        // and stops.
        LX.log("QLabClips UI initialized.");
    }

    @Override
    public void dispose() {
        // Clean up the OSC receiver and transmitter
        LX.log("Disposing QLabClips plugin.");
        lx.removeProjectListener(this);
        stopNetwork();
        super.dispose();
    }

    @Override
    public void projectChanged(File file, Change change) {
        // Handle project changes if necessary
        if (change == Change.OPEN) {
            // Restart the network when the project is loaded
            isReady = true;
            restartNetwork();
        }
    }
}
