package eu.cobwebproject.qa.automaticvalidation;

import java.io.File;
import java.io.IOException;

import java.util.Vector;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.imageio.ImageIO;


/**
* Class to Check for Blurness of an image. The algorithm is as follows
* 1. Greyscale and resize image
* 2. Convolve around a Laplace kernel defined below
* 3. Calculate variance of result and compare with user defined threshold

* Example Usage:
*     test = new BlurCheckAwt(myimage, 1000, true);
*      test.run();
*      if (test.pass){ //.. test has passed
*
* 
* @author EDINA
*/
public class BlurCheckAwt extends BlurCheckRunnable{
        /*********/
        private BufferedImage original;

        /** use this for debug messages for now */
        @Override
        protected void dbg(String msg){
            System.out.println(msg);
        }

        /** Theshold is the desired variance i.e. the higher the sharper ( 1500 is a good start) */ 
        public BlurCheckAwt(File imageFile, int threshold, boolean debug){
            super(imageFile, threshold, debug);
            try {                
                this.original=ImageIO.read(imageFile);
                dbg("image size = " + original.getWidth() + " " + original.getHeight());
            } catch (IOException e) {
                e.printStackTrace();
            }               
        }
        
        /**
         * Constructor with BufferedImage (for when it has already been read, i.e., not from a file)
         * 
         * (Added by Seb - 15/12/2015)
         * @param image: The buffered image to operate on
         * @param threshold: The threshold value for sharpness (1500 is sensible)
         * @param debug: Whether to produce debugging outputs
         */
        public BlurCheckAwt(BufferedImage image, int threshold, boolean debug) {
            super(image, threshold, debug);
            this.original = image; 
        }
    
        /** Executes the test. Can run as a thread but should be quite fast anyway */
        @Override
        public void run() {
            BufferedImage blackAndWhiteImage = convertImageToGrey(original);
            BufferedImage laplaceImage = convolve(blackAndWhiteImage, LAPLACE_KERNEL);
            if (this.debug){
                dump(blackAndWhiteImage, file.getName() + "-grey.jpg");
                dump(laplaceImage, file.getName() + "-laplace.jpg");
            }
            this.pass = getPassDecision(laplaceImage);
        }

        /** 
        * Preprocess image by 1. resizing it to 500x500 and 2. greyscaling it
        *
        * @param img the input image
        * @return the output image
        */        

        public BufferedImage convertImageToGrey(BufferedImage img){
            int newWidth = 500; // or img.getWidth()
            int newHeight = 500; // or img.getHeight()
            BufferedImage outImage = new BufferedImage(newHeight, newWidth, BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = outImage.getGraphics();
            // resize: 
            g.drawImage(img, 0, 0, newHeight, newWidth, null);
            g.dispose();
            return outImage;
        }

        /** 
        * Apply a 3x3 kernel on image
        *
        * @param src The image
        * @param k the 3x3 kernel
        */
        public BufferedImage convolve(BufferedImage src, final float[] k){
            ConvolveOp op=new ConvolveOp(new Kernel(3,3,k),ConvolveOp.EDGE_NO_OP,null);
            return op.filter(src, null);  //operating on image
        }

        private boolean getPassDecision( BufferedImage img ){
            long variance = getVariance(img);
            dbg("Variance is : " + variance);
            return variance > threshold;
        }

        /** 
        * Write the image to disc
        *
        * @param img The image
        * @param fname the filename eg. "foo.jpg"
        */
        private void dump( BufferedImage img, String fname){
            dbg("Dumping: " + fname);
            try{
                ImageIO.write(img, "jpeg",new File(fname));
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        /** 
        * Calculare variance of all pixels. Assumes greyscale i.e. R=G=B 
        *
        * @param img The greyscaled image
        * @return the variance of the image
        */
        private long getVariance ( BufferedImage img ){
            Vector<Integer> data = new Vector<Integer>(20000,5000);
            long sum = 0;
            long mean = 0;
            long variance = 0;
            // Populate vector<int> from image data -- greyscale implies R=G=B;
            for (int i = 0; i < img.getWidth(); i++){
                for (int j = 0; j < img.getHeight(); j++){
                    Color c = new Color(img.getRGB(i, j));
                    data.add( c.getRed() );
                }
            }

            //find mean
            for ( int pix : data ){
                sum += pix;
            }
            mean = sum  / data.size();

            //find variance
            int temp = 0;
            for(int pix :data)
                temp += (mean-pix)*(mean-pix);
            variance = temp/data.size();
            
            dbg("Sum: " + sum);
            dbg("Size: " + data.size());
            dbg("Mean: " + mean);
            dbg("Variance: " + variance);

            return variance;
        }
}

