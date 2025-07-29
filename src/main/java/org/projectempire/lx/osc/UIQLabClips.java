package org.projectempire.lx.osc;

import heronarts.glx.ui.UI;
import heronarts.glx.ui.UI2dComponent;
import heronarts.glx.ui.component.*;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.LX;
import heronarts.lx.parameter.*;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.device.UIControls;

public class UIQLabClips extends UICollapsibleSection implements UIControls {
    private static final int ROW_HEIGHT = 16;
    private static final int INPUT_COL_WIDTH = COL_WIDTH + 30;

    /**
     * Constructs a new collapsible section
     *
     * @param ui UI
     * @param w  Width
     */
    public UIQLabClips(LXStudio lx, UI ui, QLabClips component, float w) {
        super(ui, 0,0, w, 0);
        setTitle("QLab Clips");
        setLayout(Layout.VERTICAL, 4);

        addChildren(
                newParamButton(component.enabled),
                newTextInput("QLab IP", component.qLabIp),
                newTextInput("QLab OSC Port", component.qLabOscPort),
                newTextInput("Receiver OSC Port", component.recvOscPort),
                newParamButton(component.targetClips)
                //newParamButton(component.targetCues)
        );

        addListener(component.enabled, (p) -> {
           // Enable/disable receiver and transmitter.
            LX.log("QLab Clips enabled: " + component.enabled.isOn());
            if (component.enabled.isOn()) {
                // Start the OSC receiver and transmitter.
                component.restartNetwork();
            } else {
                // Stop the OSC receiver and transmitter.
                component.stopNetwork();
            }
        });

        addListener(component.qLabIp, (p) -> {
           // TODO(tracy): Restart networking
            LX.log("QLab Clips IP changed: " + component.qLabIp.getString());
            component.restartNetwork();
        });

        addListener(component.qLabOscPort, (p) -> {
           // TODO(tracy): Restart networking
            LX.log("QLab Clips OSC Port changed: " + Integer.parseInt(component.qLabOscPort.getString()));
            component.restartNetwork();
        });

        addListener(component.recvOscPort, (p) -> {
            // TODO(tracy): Restart networking
            LX.log("QLab Clips Receiver OSC Port changed: " + Integer.parseInt(component.recvOscPort.getString()));
            component.restartNetwork();
        });
    }

    private UI2dComponent newParamButton(BooleanParameter p) {
        return newHorizontalContainer(ROW_HEIGHT, 2,
                new UILabel(getContentWidth() - INPUT_COL_WIDTH,  p.getLabel())
                        .setFont(UI.get().theme.getControlFont())
                        .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE)
                        .setDescription(p.getDescription()),
                newButton(p)
                        .setActiveLabel("Enabled")
                        .setInactiveLabel("Disabled")
                        .setHeight(ROW_HEIGHT)
        );
    }

    private UI2dComponent newTextInput(String label, StringParameter p) {
        return newHorizontalContainer(ROW_HEIGHT, 2,
                new UILabel(getContentWidth() - INPUT_COL_WIDTH,  p.getLabel())
                        .setFont(UI.get().theme.getControlFont())
                        .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE)
                        .setDescription(p.getDescription()),
                        new UITextBox(0, 0, INPUT_COL_WIDTH - 5, ROW_HEIGHT, p)
                        .setHeight(ROW_HEIGHT)
        );
    }
}