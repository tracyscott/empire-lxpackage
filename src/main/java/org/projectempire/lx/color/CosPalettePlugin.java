package org.projectempire.lx.color;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXPlugin;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.studio.LXStudio;

import java.io.File;

@LXPlugin.Name("Cos Palette")
public class CosPalettePlugin extends LXComponent implements LXStudio.Plugin, LXParameterListener, LX.ProjectListener {

    private static CosPalettePlugin instance;
    
    protected LX lx;
    protected CosPalette cosPalette;
    
    public final BooleanParameter enabled =
        new BooleanParameter("Enabled", true)
            .setDescription("Enable or disable the Cos Palette component");

    @Override
    public void initialize(LX lx) {
        try {
            instance = this;
            this.lx = lx;
            addParameter("enabled", enabled);
            
            // Create the CosPalette component
            this.cosPalette = new CosPalette(lx);
            
            // Register the component with the LX engine
            lx.engine.registerComponent("cospalette", this.cosPalette);
            lx.addProjectListener(this);
            
            // Parameter listener is automatically added since we implement LXParameterListener
            
            LX.log("CosPalettePlugin initialized successfully");
        } catch (Exception e) {
            LX.log("Error initializing CosPalettePlugin: " + e.getMessage());
            e.printStackTrace();
            this.cosPalette = null;
        }
    }

    @Override
    public void onParameterChanged(heronarts.lx.parameter.LXParameter parameter) {
        if (parameter == enabled) {
            if (enabled.isOn()) {
                LX.log("CosPalette enabled");
            } else {
                LX.log("CosPalette disabled");
            }
        }
    }

    @Override
    public void initializeUI(LXStudio lxStudio, LXStudio.UI ui) {
        // UI initialization if needed
    }

    @Override
    public void onUIReady(LXStudio lxStudio, LXStudio.UI ui) {
        // Only create UI if the palette component was successfully initialized
        if (this.cosPalette != null) {
            // Create and add the UI component to the global panel
            UICosPalette uiCosPalette = (UICosPalette) new UICosPalette(lxStudio, ui, this.cosPalette, ui.leftPane.global.getContentWidth())
                    .addToContainer(ui.leftPane.global);
            uiCosPalette.setExpanded(false);
            
            LX.log("CosPalettePlugin UI initialized");
        } else {
            LX.log("CosPalettePlugin: Cannot create UI, palette component is null");
        }
    }

    @Override
    public void dispose() {
        LX.log("Disposing CosPalettePlugin");
        if (lx != null) {
            lx.removeProjectListener(this);
        }
        super.dispose();
    }

    @Override
    public void projectChanged(File file, Change change) {
        // Handle project changes if necessary
        if (change == Change.OPEN) {
            LX.log("Project opened, CosPalette ready");
        }
    }

    /**
     * Get the CosPalette component instance
     * @return The CosPalette component
     */
    public CosPalette getCosPalette() {
        return this.cosPalette;
    }
    
    /**
     * Get the static instance of the CosPalettePlugin
     * @return The plugin instance, or null if not initialized
     */
    public static CosPalettePlugin getInstance() {
        return instance;
    }
}