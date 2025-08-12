package org.projectempire.lx.modulator;

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.TriggerParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.glx.ui.UI2dComponent;
import heronarts.glx.ui.UI2dContainer;
import heronarts.glx.ui.component.UIButton;
import heronarts.glx.ui.component.UITextBox;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.studio.ui.modulation.UIModulator;
import heronarts.lx.studio.ui.modulation.UIModulatorControls;
import heronarts.glx.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class UITriggerList implements UIModulatorControls<TriggerList> {

  private final static int LABEL_HEIGHT = 16;
  private final static int TOP_PADDING = 4;
  private final static int TRIG_SIZE = 36;
  private final static int ADD_BUTTON_SIZE = 24;
  private final static int REMOVE_BUTTON_SIZE = 36;
  private final static int ADD_INPUT_WIDTH = 80;
  
  private UIModulator uiModulator;
  private TriggerList triggerList;
  private UITextBox addNameInput;
  
  public void buildModulatorControls(LXStudio.UI ui, UIModulator uiModulator, TriggerList triggerList) {
    this.uiModulator = uiModulator;
    this.triggerList = triggerList;
    
    uiModulator.setLayout(UI2dContainer.Layout.VERTICAL);
    uiModulator.setChildSpacing(4);
    
    // Listen for parameter updates to rebuild UI
    triggerList.onParamUpdate.addListener(p -> rebuildUI());
    
    rebuildUI();
  }
  
  private void rebuildUI() {
    uiModulator.removeAllChildren();
    
    List<BooleanParameter> triggers = triggerList.getTriggerSources();
    List<UI2dComponent> triggerRows = new ArrayList<>();
    List<UI2dComponent> nameRows = new ArrayList<>();
    
    // Create trigger rows in groups of 4
    for (int i = 0; i < triggers.size(); i += 4) {
      List<UI2dComponent> triggerButtons = new ArrayList<>();
      List<UI2dComponent> nameBoxes = new ArrayList<>();
      
      for (int j = i; j < Math.min(i + 4, triggers.size()); j++) {
        BooleanParameter trigger = triggers.get(j);
        
        // Create trigger button
        UIButton triggerButton = (UIButton)new UIButton(0, 0, TRIG_SIZE, TRIG_SIZE)
          .setParameter(trigger)
          .setTriggerable(true)
          .setLabel(trigger.getLabel())
          .setTextAlignment(VGraphics.Align.CENTER, VGraphics.Align.MIDDLE)
          .setBorderRounding(8);
        
        // Create remove button
        UIButton removeButton = (UIButton) new UIButton(0, 0, REMOVE_BUTTON_SIZE, 12) {
          @Override
          protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
            super.onMousePressed(mouseEvent, mx, my);
            triggerList.removeTrigger((TriggerParameter) trigger);
          }
        }
        .setLabel("X")
        .setTextAlignment(VGraphics.Align.CENTER, VGraphics.Align.MIDDLE)
        .setBorderRounding(4);

        // Add components to row lists
        triggerButtons.add(triggerButton);
        nameBoxes.add(removeButton);
      }
      
      // Create horizontal containers for this row
      if (!triggerButtons.isEmpty()) {
        UI2dContainer triggerRow = UI2dContainer.newHorizontalContainer(TRIG_SIZE, 7, 
          triggerButtons.toArray(new UI2dComponent[0]));
        triggerRow.setTopMargin(TOP_PADDING);
        triggerRows.add(triggerRow);
        
        // Center the remove buttons under the trigger buttons by calculating proper spacing
        int buttonSpacing = 7; // Same as trigger button spacing
        int centerOffset = (TRIG_SIZE - REMOVE_BUTTON_SIZE) / 2; // Center remove button under trigger button
        
        // Create centered remove buttons with proper spacing
        UI2dComponent[] centeredRemoveButtons = new UI2dComponent[nameBoxes.size()];
        for (int k = 0; k < nameBoxes.size(); k++) {
          UI2dComponent removeButton = nameBoxes.get(k);
          // Set position to center under corresponding trigger button
          removeButton.setX(centerOffset + k * (TRIG_SIZE + buttonSpacing));
          centeredRemoveButtons[k] = removeButton;
        }
        
        UI2dContainer nameRow = UI2dContainer.newHorizontalContainer(12, buttonSpacing, 
          centeredRemoveButtons);
        nameRows.add(nameRow);
      }
    }
    
    // Create input field and + button for adding new triggers
    addNameInput = new UITextBox(0, 0, ADD_INPUT_WIDTH, ADD_BUTTON_SIZE);
    
    UIButton addButton = (UIButton)new UIButton(0, 0, ADD_BUTTON_SIZE, ADD_BUTTON_SIZE) {
      @Override
      protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        super.onMousePressed(mouseEvent, mx, my);
        String name = addNameInput.getValue();
        if (name != null && !name.trim().isEmpty()) {
          triggerList.addTrigger(name.trim());
          addNameInput.setValue(""); // Clear input after adding
        }
      }
    }
    .setLabel("+")
    .setTextAlignment(VGraphics.Align.CENTER, VGraphics.Align.MIDDLE)
    .setBorderRounding(4);
    
    // Create container for add controls
    UI2dContainer addControls = UI2dContainer.newHorizontalContainer(ADD_BUTTON_SIZE, 4, 
      addNameInput, addButton);
    
    // Add all components to the modulator
    List<UI2dComponent> allComponents = new ArrayList<>();
    for (int i = 0; i < triggerRows.size(); i++) {
      allComponents.add(triggerRows.get(i));
      allComponents.add(nameRows.get(i));
    }
    allComponents.add(addControls);
    
    uiModulator.addChildren(allComponents.toArray(new UI2dComponent[0]));
  }
  

}