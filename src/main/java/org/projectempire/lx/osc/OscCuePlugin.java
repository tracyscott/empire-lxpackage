package org.projectempire.lx.osc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import heronarts.lx.LX;
import heronarts.lx.LXPlugin;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscArgument;
import heronarts.lx.osc.OscBool;
import heronarts.lx.osc.OscDouble;
import heronarts.lx.osc.OscFloat;
import heronarts.lx.osc.OscInt;
import heronarts.lx.osc.OscLong;
import heronarts.lx.osc.OscMessage;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import org.projectempire.lx.log.LXLogWrapper;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Empire OSC Plugin for handling OSC messages and executing cues.
 * <p>
 * This plugin listens for OSC messages with the pattern "/empire/cue/<cueName>"
 * and executes the corresponding cues defined in a JSON file.
 */
@LXPlugin.Name("Empire OSC Listener")
public class OscCuePlugin implements LXPlugin, LXOscListener, LXParameterListener {
    /** Prefix for log messages from this plugin */
    private static final String LOG_PREFIX = "[Empire OSC] ";
    private static final LXLogWrapper logger = new LXLogWrapper(LOG_PREFIX);
    private static final byte[] LOCALHOST_IP = {127, 0, 0, 1};
    private LX lx;
    /** Transmitter for sending OSC messages to localhost (127.0.0.1) */
    private LXOscEngine.Transmitter transmitter;
    private AtomicInteger cueCounter = new AtomicInteger(0);
    /** Cue data from json file */
    private Map<String, List<EmpireOscCue>> cueMap = Collections.emptyMap();

    @Override
    public void initialize(LX _lx) {
        this.lx = _lx;

        loadCueJson();
        // Create a transmitter to send OSC messages back to the LX engine on localhost. Since this seems to be
        // called before the receivePort is read from the project/config, we add a listener to properly set the
        // transmitter port when the receivePort changes.
        this.lx.engine.osc.receivePort.addListener(this);
        try {
            // Get the port the LX is listening on for OSC messages on
            final int oscPort = this.lx.engine.osc.receivePort.getValuei();
            transmitter = this.lx.engine.osc.transmitter(InetAddress.getByAddress(LOCALHOST_IP), oscPort);
        } catch (SocketException | UnknownHostException e) {
            logger.error(e, LOG_PREFIX + "Failed to create OSC transmitter");
        }
        this.lx.engine.osc.addListener(this);
    }

    @Override
    public void dispose() {
        this.lx.engine.osc.removeListener(this);
        this.transmitter.dispose();
    }

    @Override
    public void oscMessage(OscMessage oscMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug(cueCounter.incrementAndGet() + " " + oscMessage.toString());
        }
        try {
            String address = oscMessage.getAddressPattern().toString();

            String[] parts = address.split("/");
            // Note: parts[0] is expected to be empty due to leading slash. Should we enforce this?
            if (parts.length > 1 && "empire".equals(parts[1])) {
                if (parts.length > 2 && "cue".equals(parts[2])) {
                    if (oscMessage.size() > 0) {
                        String cueName = oscMessage.get(0).toString();
                        List<EmpireOscCue> cues = getCue(cueName);
                        copyIncomingArguments(oscMessage, cues);
                        if (cues != null && !cues.isEmpty()) {
                            runCues(cues);
                        } else {
                            logger.error("Cue not found: '" + cueName + "'");
                        }
                    } else {
                        logger.error("Cue name not specified");
                    }
                } else {
                    logger.error("Unknown empire command: '" + parts[2] + "'");
                }
            }
        } catch (Exception e) {
            logger.error(e, "Error processing OSC message: " + oscMessage.toString());
        }
    }

    /**
     * Runs the cues by sending OSC messages to the LX engine.
     *
     * @param cues List of EmpireOscCue objects to run
     */
    private void runCues(List<EmpireOscCue> cues) {
        for (EmpireOscCue cue : cues) {
            OscMessage oscMessage = cue.toOscMessage();
            if (oscMessage.hasPrefix("/lx")) {
                try {
                    transmitter.send(oscMessage);
                } catch (IOException e) {
                    logger.error(e, "Error sending OSC message");
                }
            }
        }
    }

    /**
     * Copies incoming OSC message arguments to the cues.
     *
     * @param oscMessage The incoming OSC message
     * @param cues       List of EmpireOscCue objects to copy arguments to
     */
    private void copyIncomingArguments(OscMessage oscMessage, List<EmpireOscCue> cues) {
        // copy arguments from incoming message to cues
        if (oscMessage.size() > 1) {
            List<Object> arguments = new ArrayList<>();
            for (int i = 1; i < oscMessage.size(); i++) {
                OscArgument argument = oscMessage.get(i);
                if (argument instanceof OscInt || argument instanceof OscLong) {
                    arguments.add(argument.toInt());
                } else if (argument instanceof OscFloat || argument instanceof OscDouble) {
                    arguments.add(argument.toDouble());
                } else if (argument instanceof OscBool) {
                    arguments.add(argument.toBoolean() ? 1 : 0);
                } else {
                    arguments.add(argument.toString());
                }
            }
            for (EmpireOscCue cue : cues) {
                cue.getArguments().addAll(arguments);
            }
        }
    }

    private List<EmpireOscCue> getCue(String name) {
        List<EmpireOscCue> original = cueMap.get(name);
        if (original == null || original.isEmpty()) {
            return Collections.emptyList();
        }
        List<EmpireOscCue> copy = new ArrayList<>(original.size());
        for (EmpireOscCue cue : original) {
            copy.add(new EmpireOscCue(cue));
        }
        return copy;
    }

    private void loadCueJson() {
        logger.log(lx.getMediaPath(LX.Media.PROJECTS, Path.of("cues.json").toFile()));
        Path jsonPath = Path.of(System.getProperty("user.home"), "Chromatik/Projects/Empire/cues.json").normalize();
        //File f = lx.getMediaFile("cues.json"); // TODO: how to get project root?
        if (!Files.exists(jsonPath)) {
            logger.error("Cues file not found: " + jsonPath);
            return;
        }
        Gson gson = new GsonBuilder().create();
        Map<String, List<EmpireOscCue>> map = Collections.emptyMap();
        final Type typeOf = new TypeToken<Map<String, List<EmpireOscCue>>>() {
        }.getType();
        try (Reader reader = Files.newBufferedReader(jsonPath)) {
            map = gson.fromJson(reader, typeOf);
        } catch (Exception e) {
            logger.error(e, "Error reading cues from JSON file: " + jsonPath);
        }

        this.cueMap = map;
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        if (null != transmitter) {
            if (parameter == lx.engine.osc.receivePort) {
                final int newPort = lx.engine.osc.receivePort.getValuei();
                logger.log("Changing OSC destination port to " + newPort);
                transmitter.setPort(newPort);
            }
        }
    }
}
