package eu.cobwebproject.qa.automaticvalidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public abstract class LaplacePhotoBlurCheck {
	
	File file;
	int threshold;
	boolean pass;
	IImage histogramEQ = null;
	IImage blackAndWhiteImage = null;
	IImage laplaceImage = null;
	IImage strechedLaplaceImage = null;
	
	public abstract IImage read(File imageFile) throws IOException;
	abstract IImage createImage(int width, int height, int type);
	abstract IImage createImage(int width, int height);
	abstract IImage createImageGreyscale(int width, int height);
	abstract Colour createColour(int rgb);
	abstract Colour createColour(int r, int g, int b);
	
	public LaplacePhotoBlurCheck(){
	    
	}
	
	/**
	 * @author Sam Meek
	 * @param imageFile File of the image
	 * @param threshold A number between 0 and 255, the higher the greater the sharpness requirement
	 */
	
	public LaplacePhotoBlurCheck(File imageFile, int threshold){
		this.file = imageFile;
		this.threshold = threshold;
		IImage original = null;
				
		//System.out.println("* read");
		try {	
			
			original = read(file);
			System.out.println("image size = " + original.getWidth() + " " + original.getHeight());
				
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
        //System.out.println("* histogramEqualization");
		histogramEQ = histogramEqualization(original);
		
        //System.out.println("* getLaplaceImage");
		laplaceImage = getLaplaceImage(histogramEQ);
		
        //System.out.println("* convertImageToGrey");
		blackAndWhiteImage = convertImageToGrey(laplaceImage);
		
        //System.out.println("* strechedLaplaceImage");
		strechedLaplaceImage = histogramEqualization(blackAndWhiteImage);
		
        //System.out.println("* blackAndWhiteImage");
		pass = getBlurryImageDecision(blackAndWhiteImage, threshold);
		
		//System.out.println("* finished");
	}
	
/**
 * 
 * @param original The original image
 * @return An image after the Laplace transform
 */
public IImage getLaplaceImage(IImage pic1){
	
    // TODO is this looks like a bug?
	//IImage pic1 = histogramEqualization(original);
    
	IImage pic2 = this.createImage(pic1.getWidth(), pic1.getHeight());
	
	int height = pic1.getHeight();
	int width = pic1.getWidth();
	for (int y = 1; y < height - 1; y++) {
        for (int x = 1; x < width - 1; x++) {
            Colour c00 = this.createColour(pic1.getRGB(x-1, y-1));
            Colour c01 = this.createColour(pic1.getRGB(x-1, y  ));
            Colour c02 = this.createColour(pic1.getRGB(x-1, y+1));
            Colour c10 = this.createColour(pic1.getRGB(x  , y-1));
            Colour c11 = this.createColour(pic1.getRGB(x  , y  ));
            Colour c12 = this.createColour(pic1.getRGB(x  , y+1));
            Colour c20 = this.createColour(pic1.getRGB(x+1, y-1));
            Colour c21 = this.createColour(pic1.getRGB(x+1, y  ));
            Colour c22 = this.createColour(pic1.getRGB(x+1, y+1));
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
            Colour c = this.createColour(r, g, b);
            
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
private IImage histogramEqualization(IImage original) {
	 
    int red;
    int green;
    int blue;
    int alpha;
    int newPixel = 0;

    ArrayList<int[]> histLUT = histogramEqualizationLUT(original);

    IImage histogramEQ = this.createImage(original.getWidth(), original.getHeight(), original.getType());

    for(int i=0; i<original.getWidth(); i++) {
        for(int j=0; j<original.getHeight(); j++) {

            // Get pixels by R, G, B
            alpha = this.createColour(original.getRGB (i, j)).getAlpha();
            red = this.createColour(original.getRGB (i, j)).getRed();
            green = this.createColour(original.getRGB (i, j)).getGreen();
            blue = this.createColour(original.getRGB (i, j)).getBlue();

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
private ArrayList<int[]> histogramEqualizationLUT(IImage input) {

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
public ArrayList<int[]> imageHistogram(IImage input) {

    int[] rhistogram = new int[256];
    int[] ghistogram = new int[256];
    int[] bhistogram = new int[256];

    for(int i=0; i<rhistogram.length; i++) rhistogram[i] = 0;
    for(int i=0; i<ghistogram.length; i++) ghistogram[i] = 0;
    for(int i=0; i<bhistogram.length; i++) bhistogram[i] = 0;

    for(int i=0; i<input.getWidth(); i++) {
        for(int j=0; j<input.getHeight(); j++) {

            int red = this.createColour(input.getRGB (i, j)).getRed();
            int green = this.createColour(input.getRGB (i, j)).getGreen();
            int blue = this.createColour(input.getRGB (i, j)).getBlue();

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
private IImage convertImageToGrey(IImage original){
	
	
	IImage greyImage = this.createImageGreyscale(original.getWidth(), original.getHeight());
	
    int  width = original.getWidth();
    int  height = original.getHeight();
     
     for(int i=0; i<height; i++){
     
        for(int j=0; j<width; j++){
        
           Colour c = this.createColour(original.getRGB(j, i));
           int red = (int)(c.getRed() * 0.299);
           int green = (int)(c.getGreen() * 0.587);
           int blue = (int)(c.getBlue() *0.114);
           Colour newColor = this.createColour(red+green+blue,
           
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
public boolean getBlurryImageDecision(IImage image, int threshold){
	boolean t = false;
	int red = 0;
	int green = 0;
	int blue = 0;
	int average = 0;
	int max = 0;
	
	System.out.println("Threshold " + threshold);
     
    for (int i = 0; i < image.getWidth(); i++){
    	for (int j = 0; j < image.getHeight(); j++){

    		Colour c = this.createColour(image.getRGB(i, j));
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


public IImage getHistogramStretchedImage(){
	return histogramEQ;
}

public IImage getBlackAndWhiteImage(){
	return blackAndWhiteImage;
}

public IImage getLaplaceImage(){
	return laplaceImage;
}

public boolean getPassDecision(){
	return pass;
}

public IImage getStrechedLaplaceImage(){
	return strechedLaplaceImage;
}

}

