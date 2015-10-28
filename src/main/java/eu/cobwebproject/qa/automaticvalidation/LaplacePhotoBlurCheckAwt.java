package eu.cobwebproject.qa.automaticvalidation;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LaplacePhotoBlurCheckAwt extends LaplacePhotoBlurCheck{
    public LaplacePhotoBlurCheckAwt(File imageFile, int threshold){
        super(imageFile, threshold);
    }
    
    public IImage read(File imageFile) throws IOException{
        return new BImage(ImageIO.read(file));
    }
    
    public IImage createImage(int width, int height, int type){
        return new BImage(width, height, type);
    }
    
    public Colour createColour(int rgb){
        return new AwtColour(rgb); 
    }
        
    public static Colour Colour(int rgb){
        return new AwtColour(rgb); 
    }

    public Colour createColour(int r, int g, int b){
        return new AwtColour(r, g, b); 
    }
}
