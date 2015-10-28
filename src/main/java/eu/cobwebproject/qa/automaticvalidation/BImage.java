package eu.cobwebproject.qa.automaticvalidation;

import java.awt.image.BufferedImage;

public class BImage implements IImage{
    
    private BufferedImage image;
    
    public BImage(BufferedImage image){
        this.image = image;
    }
    
    public BImage(int width, int height, int type){
        this.image = new BufferedImage(width, height, type);
    }
    
    public int getWidth(){
        return this.image.getWidth();
    }
    
    public int getHeight(){
        return this.image.getHeight();
    }
    
    public int getRGB(int x, int y){
        return this.image.getRGB(x, y);
    }
    
    public int getType(){
        return this.image.getType();
    }

    public int getTypeIntRgb(){
        return BufferedImage.TYPE_INT_RGB;
    }
    
    public int getTypeByteGrey(){
        return BufferedImage.TYPE_BYTE_GRAY;
    }

    public void setRGB(int x, int y, int rgb) {
        this.image.setRGB(x, y, rgb);
    }
}
