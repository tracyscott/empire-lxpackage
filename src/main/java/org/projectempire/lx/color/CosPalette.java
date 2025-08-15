package org.projectempire.lx.color;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXSerializable;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXSwatch;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.TriggerParameter;
import heronarts.lx.utils.LXUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.projectempire.lx.modulator.CosPaletteModulator;

public class CosPalette extends LXComponent implements LXOscComponent {


    public final StringParameter paletteLabel =
            new StringParameter("Palette Label", "")
                    .setDescription("Label for this palette");

    public final BooleanParameter performanceMode =
            new BooleanParameter("Performance", false)
                    .setDescription("Whether performance mode is enabled");

    
    // Flag to prevent parameter update loops during swatch loading
    private boolean loadingSwatchParameters = false;
    
    // Counter for unique swatch IDs
    private int swatchIdCounter = 0;
    
    // Parameter to trigger UI updates (not persisted)
    public final MutableParameter swatchesChanged = new MutableParameter("SwatchesChanged", 0)
            .setDescription("Parameter to trigger when swatch list changes");

    // Current active swatch index
    public final DiscreteParameter swatchIndex =
            new DiscreteParameter("Swatch", 0, 0, 1)
                    .setDescription("Active swatch index");

    // Swatches
    private final List<CosPaletteSwatch> mutableSwatches = new ArrayList<>();
    private final List<CosPaletteSwatch> swatches = new ArrayList<>();

    // Current color parameters (computed from active swatch)
    public final MutableParameter hue = new MutableParameter("Hue", 0);
    public final MutableParameter saturation = new MutableParameter("Saturation", 100);
    public final MutableParameter brightness = new MutableParameter("Brightness", 100);

    // Vector parameters for current active swatch
    public final CompoundParameter aR = new CompoundParameter("Amp.R", 0.0, -3.13840734641021, 3.13840734641021);
    public final CompoundParameter aG = new CompoundParameter("Amp.G", 0.0, -3.13840734641021, 3.13840734641021);
    public final CompoundParameter aB = new CompoundParameter("Amp.B", 0.0, -3.13840734641021, 3.13840734641021);
    public final CompoundParameter bR = new CompoundParameter("DC.R", 0.0, -3.13840734641021, 3.13840734641021);
    public final CompoundParameter bG = new CompoundParameter("DC.G", 0.0, -3.13840734641021, 3.13840734641021);
    public final CompoundParameter bB = new CompoundParameter("DC.B", 0.0, -3.13840734641021, 3.13840734641021);
    public final CompoundParameter cR = new CompoundParameter("Freq.R", 0.0, -3.13840734641021, 3.13840734641021);
    public final CompoundParameter cG = new CompoundParameter("Freq.G", 0.0, -3.13840734641021, 3.13840734641021);
    public final CompoundParameter cB = new CompoundParameter("Freq.B", 0.0, -3.13840734641021, 3.13840734641021);
    public final CompoundParameter dR = new CompoundParameter("Phase.R", 0.0, -3.13840734641021, 3.13840734641021);
    public final CompoundParameter dG = new CompoundParameter("Phase.G", 0.0, -3.13840734641021, 3.13840734641021);
    public final CompoundParameter dB = new CompoundParameter("Phase.B", 0.0, -3.13840734641021, 3.13840734641021);

    public CosPalette(LX lx) {
        super(lx, "CosPalette");
        addParameter("paletteLabel", this.paletteLabel);
        addParameter("performanceMode", this.performanceMode);
        addParameter("swatchIndex", this.swatchIndex);

        // Add mutable parameters for current swatch values
        addParameter("hue", this.hue);
        addParameter("saturation", this.saturation);
        addParameter("brightness", this.brightness);
        addParameter("aR", this.aR);
        addParameter("aG", this.aG);
        addParameter("aB", this.aB);
        addParameter("bR", this.bR);
        addParameter("bG", this.bG);
        addParameter("bB", this.bB);
        addParameter("cR", this.cR);
        addParameter("cG", this.cG);
        addParameter("cB", this.cB);
        addParameter("dR", this.dR);
        addParameter("dG", this.dG);
        addParameter("dB", this.dB);

        // Add default swatch
        addSwatch(new CosPaletteSwatch(lx, "Default", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        // Listen for swatch index changes
        this.swatchIndex.addListener(this::onSwatchChanged);
        
        // Add parameter listeners
        addParameterListeners();
    }
    
    private void addParameterListeners() {
        // Listen for parameter changes and update active swatch
        this.aR.addListener(this::onParameterChanged);
        this.aG.addListener(this::onParameterChanged);
        this.aB.addListener(this::onParameterChanged);
        this.bR.addListener(this::onParameterChanged);
        this.bG.addListener(this::onParameterChanged);
        this.bB.addListener(this::onParameterChanged);
        this.cR.addListener(this::onParameterChanged);
        this.cG.addListener(this::onParameterChanged);
        this.cB.addListener(this::onParameterChanged);
        this.dR.addListener(this::onParameterChanged);
        this.dG.addListener(this::onParameterChanged);
        this.dB.addListener(this::onParameterChanged);
    }
    
    private void removeParameterListeners() {
        this.aR.removeListener(this::onParameterChanged);
        this.aG.removeListener(this::onParameterChanged);
        this.aB.removeListener(this::onParameterChanged);
        this.bR.removeListener(this::onParameterChanged);
        this.bG.removeListener(this::onParameterChanged);
        this.bB.removeListener(this::onParameterChanged);
        this.cR.removeListener(this::onParameterChanged);
        this.cG.removeListener(this::onParameterChanged);
        this.cB.removeListener(this::onParameterChanged);
        this.dR.removeListener(this::onParameterChanged);
        this.dG.removeListener(this::onParameterChanged);
        this.dB.removeListener(this::onParameterChanged);
    }

    private void onSwatchChanged(LXParameter parameter) {
        updateCurrentSwatchParameters();
    }
    
    public void onParameterChanged(LXParameter parameter) {
        if (!loadingSwatchParameters) {
            updateActiveSwatchFromParameters();
        }
    }

    private void updateCurrentSwatchParameters() {
        if (!this.swatches.isEmpty()) {
            int index = LXUtils.constrain(this.swatchIndex.getValuei(), 0, this.swatches.size() - 1);
            CosPaletteSwatch swatch = this.swatches.get(index);
            
            // Temporarily remove parameter listeners to prevent feedback loops
            removeParameterListeners();
            try {
                // Update mutable parameters to reflect current swatch
                this.aR.setValue(swatch.aR.getValue());
                this.aG.setValue(swatch.aG.getValue());
                this.aB.setValue(swatch.aB.getValue());
                this.bR.setValue(swatch.bR.getValue());
                this.bG.setValue(swatch.bG.getValue());
                this.bB.setValue(swatch.bB.getValue());
                this.cR.setValue(swatch.cR.getValue());
                this.cG.setValue(swatch.cG.getValue());
                this.cB.setValue(swatch.cB.getValue());
                this.dR.setValue(swatch.dR.getValue());
                this.dG.setValue(swatch.dG.getValue());
                this.dB.setValue(swatch.dB.getValue());

                // Update derived color parameters
                // TODO(tracy): Disable dfor debugging.  updateColorParameters(swatch);
            } finally {
                // Re-add parameter listeners
                addParameterListeners();
            }
        }
    }
    
    private void updateActiveSwatchFromParameters() {
        if (!this.swatches.isEmpty()) {
            int index = LXUtils.constrain(this.swatchIndex.getValuei(), 0, this.swatches.size() - 1);
            CosPaletteSwatch swatch = this.swatches.get(index);
            
            // Update swatch parameters from mutable parameters
            /*
            NOTE(tracy): This code causes the swatch values to be overwritten
            with the previous palette colors when changing which swatch is
            selected.
            swatch.aR.setValue(this.aR.getValue());
            swatch.aG.setValue(this.aG.getValue());
            swatch.aB.setValue(this.aB.getValue());
            swatch.bR.setValue(this.bR.getValue());
            swatch.bG.setValue(this.bG.getValue());
            swatch.bB.setValue(this.bB.getValue());
            swatch.cR.setValue(this.cR.getValue());
            swatch.cG.setValue(this.cG.getValue());
            swatch.cB.setValue(this.cB.getValue());
            swatch.dR.setValue(this.dR.getValue());
            swatch.dG.setValue(this.dG.getValue());
            swatch.dB.setValue(this.dB.getValue());
            */

            // Update derived color parameters
            updateColorParameters(swatch);
        }
    }

    private void updateColorParameters(CosPaletteSwatch swatch) {
        // Sample the palette at t=0 to get a representative color for HSB
        double[] rgb = getColor(0, swatch);
        float[] hsb = java.awt.Color.RGBtoHSB((int)(rgb[0] * 255), (int)(rgb[1] * 255), (int)(rgb[2] * 255), null);
        
        this.hue.setValue(hsb[0] * 360);
        this.saturation.setValue(hsb[1] * 100);
        this.brightness.setValue(hsb[2] * 100);
    }

    public List<CosPaletteSwatch> getSwatches() {
        return this.swatches;
    }

    public CosPaletteSwatch addSwatch(CosPaletteSwatch swatch) {
        Objects.requireNonNull(swatch, "Cannot add null swatch to CosPalette");
        
        // Register as child component for proper LX tracking
        // Use a unique counter to ensure no duplicate keys
        String key = "swatch-" + (this.swatchIdCounter++);
        addChild(key, swatch);
        
        this.mutableSwatches.add(swatch);
        this.swatches.clear();
        this.swatches.addAll(this.mutableSwatches);
        
        // Update swatch index range
        this.swatchIndex.setRange(0, Math.max(1, this.swatches.size()));
        
        // Notify UI of swatch list change
        this.swatchesChanged.bang();
        
        return swatch;
    }

    public CosPaletteSwatch addSwatch() {
        // Create new swatch with current parameter values
        CosPaletteSwatch newSwatch = new CosPaletteSwatch(this.lx, 
            "Swatch " + (this.swatches.size() + 1),
            this.aR.getValue(), this.aG.getValue(), this.aB.getValue(),
            this.bR.getValue(), this.bG.getValue(), this.bB.getValue(),
            this.cR.getValue(), this.cG.getValue(), this.cB.getValue(),
            this.dR.getValue(), this.dG.getValue(), this.dB.getValue());
        return addSwatch(newSwatch);
    }

    public CosPaletteSwatch addSwatch(String label, double aR, double aG, double aB, 
                                     double bR, double bG, double bB,
                                     double cR, double cG, double cB,
                                     double dR, double dG, double dB) {
        return addSwatch(new CosPaletteSwatch(this.lx, label, aR, aG, aB, bR, bG, bB, cR, cG, cB, dR, dG, dB));
    }

    public CosPaletteSwatch removeSwatch(CosPaletteSwatch swatch) {
        if (this.mutableSwatches.remove(swatch)) {
            this.swatches.clear();
            this.swatches.addAll(this.mutableSwatches);
            
            // Update swatch index range
            this.swatchIndex.setRange(0, Math.max(1, this.swatches.size()));
            
            // Ensure current index is still valid
            if (this.swatchIndex.getValuei() >= this.swatches.size()) {
                this.swatchIndex.setValue(Math.max(0, this.swatches.size() - 1));
            }
            
            // Dispose the swatch (this will also remove it from parent)
            swatch.dispose();
            
            // Notify UI of swatch list change
            this.swatchesChanged.bang();
            
            updateCurrentSwatchParameters();
        }
        return swatch;
    }

    public CosPaletteSwatch getActiveSwatch() {
        if (this.swatches.isEmpty()) {
            return null;
        }
        int index = LXUtils.constrain(this.swatchIndex.getValuei(), 0, this.swatches.size() - 1);
        return this.swatches.get(index);
    }

    public void setSwatch(int index) {
        if (index >= 0 && index < this.swatches.size()) {
            this.swatchIndex.setValue(index);
            updateCurrentSwatchParameters();
        }
    }




    public double[] getColor(double t) {
        // Use current palette parameters (from knobs) for live preview
        double[] a = {this.aR.getValue(), this.aG.getValue(), this.aB.getValue()};
        double[] b = {this.bR.getValue(), this.bG.getValue(), this.bB.getValue()};
        double[] c = {this.cR.getValue(), this.cG.getValue(), this.cB.getValue()};
        double[] d = {this.dR.getValue(), this.dG.getValue(), this.dB.getValue()};
        return CosPaletteModulator.palette(t, a, b, c, d);
    }

    public double[] getColor(double t, CosPaletteSwatch swatch) {
        if (swatch == null) {
            return new double[]{0, 0, 0};
        }
        return getSwatchColor(t, swatch);
    }

    private double[] getSwatchColor(double t, CosPaletteSwatch swatch) {
        double[] a = {swatch.aR.getValue(), swatch.aG.getValue(), swatch.aB.getValue()};
        double[] b = {swatch.bR.getValue(), swatch.bG.getValue(), swatch.bB.getValue()};
        double[] c = {swatch.cR.getValue(), swatch.cG.getValue(), swatch.cB.getValue()};
        double[] d = {swatch.dR.getValue(), swatch.dG.getValue(), swatch.dB.getValue()};
        return CosPaletteModulator.palette(t, a, b, c, d);
    }

    public int getColor(double t, int defaultColor) {
        double[] rgb = getColor(t);
        return LXColor.rgb((int)(rgb[0] * 255), (int)(rgb[1] * 255), (int)(rgb[2] * 255));
    }

    private static final String KEY_SWATCHES = "swatches";

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.add(KEY_SWATCHES, LXSerializable.Utils.toArray(lx, this.swatches));
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        // Load new swatches from JSON first
        if (obj.has(KEY_SWATCHES)) {
            // Clear existing swatches
            while (!this.mutableSwatches.isEmpty()) {
                removeSwatch(this.mutableSwatches.get(this.mutableSwatches.size() - 1));
            }
            
            JsonArray swatchArr = obj.get(KEY_SWATCHES).getAsJsonArray();
            for (JsonElement swatchElem : swatchArr) {
                JsonObject swatchObj = swatchElem.getAsJsonObject();
                CosPaletteSwatch swatch = new CosPaletteSwatch(lx);
                // Add swatch first (which registers it as a child component)
                addSwatch(swatch);
                // Then load its parameters
                swatch.load(lx, swatchObj);
            }
        }
        
        // Call super to load basic parameters AFTER swatches are loaded
        super.load(lx, obj);
        
        // Update current swatch parameters after loading
        updateCurrentSwatchParameters();
        
        // Notify UI that swatches have been loaded
        this.swatchesChanged.bang();
    }

    // Swatch class
    public static class CosPaletteSwatch extends LXComponent {
        
        public final StringParameter label;
        
        // Vector A parameters (DC offset)
        public final CompoundParameter aR;
        public final CompoundParameter aG;
        public final CompoundParameter aB;
        
        // Vector B parameters (amplitude)
        public final CompoundParameter bR;
        public final CompoundParameter bG;
        public final CompoundParameter bB;
        
        // Vector C parameters (frequency)
        public final CompoundParameter cR;
        public final CompoundParameter cG;
        public final CompoundParameter cB;
        
        // Vector D parameters (phase)
        public final CompoundParameter dR;
        public final CompoundParameter dG;
        public final CompoundParameter dB;

        // Default constructor needed for LX serialization
        public CosPaletteSwatch(LX lx) {
            this(lx, "Swatch", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        public CosPaletteSwatch(LX lx, String label) {
            this(lx, label, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        public CosPaletteSwatch(LX lx, String label, double aR, double aG, double aB,
                               double bR, double bG, double bB,
                               double cR, double cG, double cB,
                               double dR, double dG, double dB) {
            super(lx, label);
            
            this.label = new StringParameter("Label", label);
            
            // Vector A parameters (amplitude)
            this.aR = new CompoundParameter("Amp.R", aR, -3.13840734641021, 3.13840734641021);
            this.aG = new CompoundParameter("Amp.G", aG, -3.13840734641021, 3.13840734641021);
            this.aB = new CompoundParameter("Amp.B", aB, -3.13840734641021, 3.13840734641021);
            
            // Vector B parameters (DC offset)
            this.bR = new CompoundParameter("DC.R", bR, -3.13840734641021, 3.13840734641021);
            this.bG = new CompoundParameter("DC.G", bG, -3.13840734641021, 3.13840734641021);
            this.bB = new CompoundParameter("DC.B", bB, -3.13840734641021, 3.13840734641021);
            
            // Vector C parameters (frequency)
            this.cR = new CompoundParameter("Freq.R", cR, -3.13840734641021, 3.13840734641021);
            this.cG = new CompoundParameter("Freq.G", cG, -3.13840734641021, 3.13840734641021);
            this.cB = new CompoundParameter("Freq.B", cB, -3.13840734641021, 3.13840734641021);
            
            // Vector D parameters (phase)
            this.dR = new CompoundParameter("Phase.R", dR, -3.13840734641021, 3.13840734641021);
            this.dG = new CompoundParameter("Phase.G", dG, -3.13840734641021, 3.13840734641021);
            this.dB = new CompoundParameter("Phase.B", dB, -3.13840734641021, 3.13840734641021);

            addParameter("swatchLabel", this.label);
            addParameter("aR", this.aR);
            addParameter("aG", this.aG);
            addParameter("aB", this.aB);
            addParameter("bR", this.bR);
            addParameter("bG", this.bG);
            addParameter("bB", this.bB);
            addParameter("cR", this.cR);
            addParameter("cG", this.cG);
            addParameter("cB", this.cB);
            addParameter("dR", this.dR);
            addParameter("dG", this.dG);
            addParameter("dB", this.dB);
        }

        public String getLabel() {
            return this.label.getString();
        }

        public void setLabel(String label) {
            this.label.setValue(label);
        }
    }
}