package org.projectempire.lx.osc;

import heronarts.lx.LX;
import heronarts.lx.osc.OscMessage;

import java.util.ArrayList;
import java.util.List;

class EmpireOscCue {
    private String address = "";
    private List<Object> arguments = new ArrayList<>();

    public EmpireOscCue() {
    }

    public EmpireOscCue(EmpireOscCue cue) {
        this.address = cue.address;
        this.arguments.addAll(cue.arguments);
    }

    public EmpireOscCue(OscMessage oscMessage) {
        this.address = oscMessage.getAddressPattern().getValue();
        for (int i = 0; i < oscMessage.size(); i++) {
            arguments.add(oscMessage.get(i).toString());
        }
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public void add(String argument) {
        arguments.add(argument);
    }

    public void add(int argument) {
        arguments.add(argument);
    }

    public void add(double argument) {
        arguments.add(argument);
    }

    public OscMessage toOscMessage() {
        OscMessage message = new OscMessage(address);
        for (Object argument : arguments) {
            if (argument instanceof String) {
                message.add((String) argument);
            } else if (argument instanceof Integer) {
                message.add((Integer) argument);
            } else if (argument instanceof Double) {
                message.add((Double) argument);
            } else {
                message.add(String.valueOf(argument));
                LX.error("Unknown argument type: " + argument.getClass().getName());
            }
//                try {
//                    double d = Double.parseDouble(argument);
//                    message.add(d);
//                } catch (NumberFormatException e) {
//                    message.add(argument);
//                }
        }
        return message;
    }
}
