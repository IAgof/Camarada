package com.videonasocialmedia.kamarada.model.entities;

/**
 * Created by Veronica Lago Fominaya on 25/11/2015.
 */
public class ShaderEffect extends Effect {

    private final int resourceId;

    public ShaderEffect(String identifier, String name, String iconPath, int resourceId) {
        super(identifier, name, iconPath);
        this.resourceId = resourceId;
    }

    public ShaderEffect(String identifier, String name, int iconId, int resourceId) {
        super(identifier, name, iconId);
        this.resourceId = resourceId;
    }

    public int getResourceId() {
        return resourceId;
    }
}
