package pillar.automaticvalidation;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class LaplacePhotoBlurCheck {
	
	File file;
	int threshold;
	boolean pass;
	BufferedImage histogramEQ = null;
	BufferedImage blackAndWhiteImage = null;
	BufferedImage laplaceImage = null;
	BufferedImage strechedLaplaceImage = null;
	
	/**
	 * @author Sam Meek
	 * @param imageFile File of the image
	 * @param threshold A number between 0 and 255, the higher the greater the sharpness requirement
	 */
	
	public LaplacePhotoBlurCheck(File imageFile, int threshold){
		this.file = imageFile;
		this.threshold = threshold;
		BufferedImage original = null;
			
		try {	
			
			original = ImageIO.read(file);
			System.out.println("image size = " + original.getWidth() + " " + original.getHeight());
				
		} catch (IOException e) {
			e.printStackTrace();
		}		
			
		histogramEQ = histogramEqualization(original);
		laplaceImage = getLaplaceImage(histogramEQ);
		blackAndWhiteImage = convertImageToGrey(laplaceImage);
		strechedLaplaceImage = histogramEqualization(blackAndWhiteImage);
		pass = getBlurryImageDecision(blackAndWhiteImage, threshold);

	}
	
/**
 * 
 * @param original The original image
 * @return An image after the Laplace transform
 */
private BufferedImage getLaplaceImage(BufferedImage original){
	
	BufferedImage pic1 = histogramEqualization(original);
	BufferedImage pic2 = new BufferedImage(pic1.getWidth(), pic1.getHeight(), BufferedImage.TYPE_INT_RGB);
	int height = pic1.getHeight();
	int width = pic1.getWidth();
	for (int y = 1; y < height - 1; y++) {
        for (int x = 1; x < width - 1; x++) {
            Color c00 = new Color(pic1.getRGB(x-1, y-1));
            Color c01 = new Color(pic1.getRGB(x-1, y  ));
            Color c02 = new Color(pic1.getRGB(x-1, y+1));
            Color c10 = new Color(pic1.getRGB(x  , y-1));
            Color c11 = new Color(pic1.getRGB(x  , y  ));
            Color c12 = new Color(pic1.getRGB(x  , y+1));
            Color c20 = new Color(pic1.getRGB(x+1, y-1));
            Color c21 = new Color(pic1.getRGB(x+1, y  ));
            Color c22 = new Color(pic1.getRGB(x+1, y+1));
            int r = -c00.getRed() -   c01.getRed() - c02.getRed() +
                    -c10.getRed() + 8*c11.getRed() - c12.getRed() +
                    -c20.getRed() -   c21.getRed() - c22.getRed();
            int g = -c00.getGreen() -   c01.getGreen() - c02.getGreen() +
                    -c10.getGreen() + 8*c11.getGreen() - c12.getGreen() +
                    -c20.getGreen() -   c21.getGreen() - c22.getGreen();
            int b = -c00.getBlue() -   c01.getBlue() - c02.getBlue() +
                    -c10.getBlue() + 8*c11.getBlue() - c12.getBlue() +
                    -c20.getBlue() -   c21.getBlue() - c22.getBlue();
            r = Math.min(255, Math.max(0, r));
            g = Math.min(255, Math.max(0, g));
            b = Math.min(255, Math.max(0, b));
            Color c = new Color(r, g, b);
            
            pic2.setRGB(x, y, c.getRGB());
            
        }
	}
	return pic2;
}

/**
 * 
 * @param original The original image
 * @return a histogram stretched image
 */
private static BufferedImage histogramEqualization(BufferedImage original) {
	 
    int red;
    int green;
    int blue;
    int alpha;
    int newPixel = 0;

    ArrayList<int[]> histLUT = histogramEqualizationLUT(original);

    BufferedImage histogramEQ = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

    for(int i=0; i<original.getWidth(); i++) {
        for(int j=0; j<original.getHeight(); j++) {

            // Get pixels by R, G, B
            alpha = new Color(original.getRGB (i, j)).getAlpha();
            red = new Color(original.getRGB (i, j)).getRed();
            green = new Color(original.getRGB (i, j)).getGreen();
            blue = new Color(original.getRGB (i, j)).getBlue();

            // Set new pixel values using the histogram lookup table
            red = histLUT.get(0)[red];
            green = histLUT.get(1)[green];
            blue = histLUT.get(2)[blue];

            // Return back to original format
            newPixel = colorToRGB(alpha, red, green, blue);

            // Write pixels into image
            histogramEQ.setRGB(i, j, newPixel);

        }
    }
	
   return histogramEQ;

}

/**
 * 
 * @param input
 * @return array of the histogram image
 */
private static ArrayList<int[]> histogramEqualizationLUT(BufferedImage input) {

    // Get an image histogram - calculated values by R, G, B channels
    ArrayList<int[]> imageHist = imageHistogram(input);

    // Create the lookup table
    ArrayList<int[]> imageLUT = new ArrayList<int[]>();

    // Fill the lookup table
    int[] rhistogram = new int[256];
    int[] ghistogram = new int[256];
    int[] bhistogram = new int[256];

    for(int i=0; i<rhistogram.length; i++) rhistogram[i] = 0;
    for(int i=0; i<ghistogram.length; i++) ghistogram[i] = 0;
    for(int i=0; i<bhistogram.length; i++) bhistogram[i] = 0;

    long sumr = 0;
    long sumg = 0;
    long sumb = 0;

    // Calculate the scale factor
    float scale_factor = (float) (255.0 / (input.getWidth() * input.getHeight()));

    for(int i=0; i<rhistogram.length; i++) {
        sumr += imageHist.get(0)[i];
        int valr = (int) (sumr * scale_factor);
        if(valr > 255) {
            rhistogram[i] = 255;
        }
        else rhistogram[i] = valr;

        sumg += imageHist.get(1)[i];
        int valg = (int) (sumg * scale_factor);
        if(valg > 255) {
            ghistogram[i] = 255;
        }
        else ghistogram[i] = valg;

        sumb += imageHist.get(2)[i];
        int valb = (int) (sumb * scale_factor);
        if(valb > 255) {
            bhistogram[i] = 255;
        }
        else bhistogram[i] = valb;
    }

    imageLUT.add(rhistogram);
    imageLUT.add(ghistogram);
    imageLUT.add(bhistogram);

    return imageLUT;

}

/**
 * 
 * @param input produces image histogram
 * @return returns the histogram
 */
public static ArrayList<int[]> imageHistogram(BufferedImage input) {

    int[] rhistogram = new int[256];
    int[] ghistogram = new int[256];
    int[] bhistogram = new int[256];

    for(int i=0; i<rhistogram.length; i++) rhistogram[i] = 0;
    for(int i=0; i<ghistogram.length; i++) ghistogram[i] = 0;
    for(int i=0; i<bhistogram.length; i++) bhistogram[i] = 0;

    for(int i=0; i<input.getWidth(); i++) {
        for(int j=0; j<input.getHeight(); j++) {

            int red = new Color(input.getRGB (i, j)).getRed();
            int green = new Color(input.getRGB (i, j)).getGreen();
            int blue = new Color(input.getRGB (i, j)).getBlue();

            rhistogram[red]++; ghistogram[green]++; bhistogram[blue]++;

        }
    }

    ArrayList<int[]> hist = new ArrayList<int[]>();
    hist.add(rhistogram);
    hist.add(ghistogram);
    hist.add(bhistogram);

    return hist;

}
/** 
 * @param alpha
 * @param red
 * @param green
 * @param blue
 * @return Pixel in RGB
 */
private static int colorToRGB(int alpha, int red, int green, int blue) {

    int newPixel = 0;
    newPixel += alpha; newPixel = newPixel << 8;
    newPixel += red; newPixel = newPixel << 8;
    newPixel += green; newPixel = newPixel << 8;
    newPixel += blue;

    return newPixel;

}

/**
 * @param original the buffered image to convert
 * @return greyImage the image in grey scale
 */
private BufferedImage convertImageToGrey(BufferedImage original){
	
	
	BufferedImage greyImage = new BufferedImage(original.getWidth(), 
			original.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
	
    int  width = original.getWidth();
    int  height = original.getHeight();
     
     for(int i=0; i<height; i++){
     
        for(int j=0; j<width; j++){
        
           Color c = new Color(original.getRGB(j, i));
           int red = (int)(c.getRed() * 0.299);
           int green = (int)(c.getGreen() * 0.587);
           int blue = (int)(c.getBlue() *0.114);
           Color newColor = new Color(red+green+blue,
           
           red+green+blue,red+green+blue);
           
           greyImage.setRGB(j,i,newColor.getRGB());
        }
     }
	return greyImage;
	
}

/**
 * @param image the Laplace converted image
 * @param threshold a number between 0 - 255
 * @return t boolean of whether the image has passed the test or not
 */
public boolean getBlurryImageDecision(BufferedImage image, int threshold){
	boolean t = false;
	int red = 0;
	int green = 0;
	int blue = 0;
	int average = 0;
	int max = 0;
	
	System.out.println("Threshold " + threshold);
     
    for (int i = 0; i < image.getWidth(); i++){
    	for (int j = 0; j < image.getHeight(); j++){

    		Color c = new Color(image.getRGB(i, j));
    		red = c.getRed();
    		green = c.getGreen();
    		blue = c.getBlue();
    		average = (red + blue + green)/3;
    		
    		if(average > max){
    			max = average;
    		}
    		
    		if(average > threshold){
    			t = true;
    		}
    		
    	 }
     }
    
    System.out.println("Max " + max);
    
  return t;
}


public BufferedImage getHistogramStretchedImage(){
	return histogramEQ;
}

public BufferedImage getBlackAndWhiteImage(){
	return blackAndWhiteImage;
}

public BufferedImage getLaplaceImage(){
	return laplaceImage;
}

public boolean getPassDecision(){
	return pass;
}

public BufferedImage getStrechedLaplaceImage(){
	return strechedLaplaceImage;
}

}

