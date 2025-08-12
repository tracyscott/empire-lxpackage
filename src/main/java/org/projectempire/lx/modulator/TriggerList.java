package org.projectempire.lx.modulator;

import heronarts.lx.modulator.LXMacroModulator;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LXTriggerSource;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.TriggerParameter;

/**
 * Modulator that allows for adding Boolean parameters as triggers.
 */
@LXModulator.Global("TriggerList")
@LXModulator.Device("TriggerList")
@LXCategory(LXCategory.MACRO)
public class TriggerList extends LXMacroModulator implements LXTriggerSource {
  
  // Hidden parameter that stores trigger names for persistence
  public final StringParameter triggerNames = new StringParameter("triggerNames", "")
    .setDescription("Comma-separated list of trigger parameter names for persistence");
  
  private boolean isLoadingFromProject = false;
  public final MutableParameter onParamUpdate = new MutableParameter("Update", 0)
    .setDescription("Parameter to trigger when a new trigger is added or removed");

  public TriggerList() {
    this("TriggerList");
  }

  public TriggerList(String label) {
    super(label);
    this.midiFilter.enabled.setValue(false);
    
    // Add the persistence parameter (not shown in UI)
    addParameter("triggerNames", this.triggerNames);
    
    // Listen for changes to triggerNames parameter (happens during project load)
    this.triggerNames.addListener(p -> {
      if (!isLoadingFromProject) {
        recreateTriggersFromString();
      }
    });
    
    setMappingSource(false);
  }

  public List<BooleanParameter> getTriggerSources() {
    List<BooleanParameter> triggers = new ArrayList<>();
    
    // Get the list of trigger names from the StringParameter
    String namesString = this.triggerNames.getString();
    if (namesString == null || namesString.trim().isEmpty()) {
      return triggers; // Return empty list if no trigger names are stored
    }
    
    // Parse the trigger names
    String[] names = namesString.split(",");
    
    // Only return TriggerParameters that are in the triggerNames list
    for (String name : names) {
      String trimmedName = name.trim();
      if (!trimmedName.isEmpty()) {
        LXParameter param = getParameter(trimmedName);
        if (param instanceof TriggerParameter) {
          triggers.add((TriggerParameter) param);
        }
      }
    }
    return triggers;
  }

  public void removeTrigger(TriggerParameter trigger) {
    if (trigger != null) {
      removeParameter(trigger);
      updateTriggerNamesParameter();
      onParamUpdate.bang();
    }
  }

  public void addTrigger(String label) {
    TriggerParameter trigger = new TriggerParameter(label);
    addParameter(label, trigger);
    updateTriggerNamesParameter();
    onParamUpdate.bang();
  }
  
  /**
   * Updates the triggerNames parameter with current trigger names for persistence
   */
  private void updateTriggerNamesParameter() {
    isLoadingFromProject = true; // Prevent recursion
    try {
      List<String> names = new ArrayList<>();
      
      // Collect all TriggerParameter names (except built-in ones like triggerNames)
      for (LXParameter param : getParameters()) {
        if (param instanceof TriggerParameter && !param.getLabel().equals("triggerNames")) {
          names.add(param.getLabel());
        }
      }
      
      String namesString = String.join(",", names);
      this.triggerNames.setValue(namesString);
    } finally {
      isLoadingFromProject = false;
    }
  }
  
  /**
   * Recreates BooleanParameters from the triggerNames string (called during project load)
   */
  private void recreateTriggersFromString() {
    isLoadingFromProject = true;
    try {
      // Remove all existing trigger parameters (except built-in ones)
      List<LXParameter> parametersToRemove = new ArrayList<>();
      for (LXParameter param : getParameters()) {
        if (param instanceof TriggerParameter && !param.getLabel().equals("triggerNames")) {
          parametersToRemove.add(param);
        }
      }
      for (LXParameter param : parametersToRemove) {
        removeParameter(param);
      }
      
      // Create new parameters from the names string
      String namesString = this.triggerNames.getString();
      if (namesString != null && !namesString.trim().isEmpty()) {
        String[] names = namesString.split(",");
        for (String name : names) {
          String trimmedName = name.trim();
          if (!trimmedName.isEmpty()) {
            TriggerParameter trigger = new TriggerParameter(trimmedName);
            addParameter(trimmedName, trigger);
          }
        }
      }
      
      // Notify UI that parameters have been recreated
      onParamUpdate.bang();
    } finally {
      isLoadingFromProject = false;
    }
  }

  @Override
  protected double computeValue(double deltaMs) {
    // Not relevant
    return 0;
  }

  @Override
  public BooleanParameter getTriggerSource() {
    return null;
  }

}