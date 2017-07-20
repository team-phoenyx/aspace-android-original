package com.aspace.aspace;

/**
 * Created by terrance on 7/19/17.
 */

public class NavigationInstruction {
    private String instruction;
    private String distanceFromInstruction;
    private String iconFileName;

    public NavigationInstruction(String instruction, String distanceFromInstruction, String iconFileName) {
        this.instruction = instruction;
        this.distanceFromInstruction = distanceFromInstruction;
        this.iconFileName = iconFileName;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getDistanceFromInstruction() {
        return distanceFromInstruction;
    }

    public void setDistanceFromInstruction(String distanceFromInstruction) {
        this.distanceFromInstruction = distanceFromInstruction;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public void setIconFileName(String iconFileName) {
        this.iconFileName = iconFileName;
    }
}
