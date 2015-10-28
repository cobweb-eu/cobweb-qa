package eu.cobwebproject.qa.automaticvalidation;

import java.awt.Color;

public class AwtColour implements Colour{
    private Color color;
    
    public AwtColour(int rgb){
        this.color = new Color(rgb);
    }
    
    public AwtColour(int r, int g, int b){
        this.color = new Color(r, g, b);
    }
    
    public int getRed() {
        return this.color.getRed();
    }
    
    public int getGreen() {
        return this.color.getGreen();
    }
    
    public int getBlue() {
        return this.color.getBlue();
    }

    public int getRGB() {
        return this.color.getRGB();
    }

    public int getAlpha() {
        return this.color.getAlpha();
    }
}
