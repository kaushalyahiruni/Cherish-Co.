package model;

public class Colors {

    private int resourceId;
    private String color;

    public Colors(int resourceId, String color) {
        this.resourceId = resourceId;
        this.color = color;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
