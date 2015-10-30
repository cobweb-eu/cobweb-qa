package eu.cobwebproject.qa.automaticvalidation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LaplacePhotoBlurCheckAwt extends LaplacePhotoBlurCheck{
    public LaplacePhotoBlurCheckAwt(File imageFile, int threshold){
        super(imageFile, threshold);
    }
    
    @Override
    public IImage read(File imageFile) throws IOException{
        return new BImage(ImageIO.read(file));
    }
    
    @Override
    public IImage createImage(int width, int height, int type){
        return new BImage(width, height, type);
    }
    
    @Override
    IImage createImage(int width, int height) {
        return new BImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    IImage createImageGreyscale(int width, int height) {
        return new BImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    }

    @Override
    public Colour createColour(int rgb){
        return new AwtColour(rgb); 
    }
    
    @Override
    public Colour createColour(int r, int g, int b){
        return new AwtColour(r, g, b); 
    }
}
