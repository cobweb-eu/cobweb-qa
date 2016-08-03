package eu.cobwebproject.qa.automaticvalidation;

import java.awt.image.BufferedImage;
import java.io.File;

/**
* Class to Check for Blurness of an image. The algorithm is as follows
* 1. Greyscale and resize image
* 2. Convolve around a Laplace kernel defined below
* 3. Calculate variance of result and compare with user defined threshold
*
* Example Usage:
*     // Threshold is concrete-class specfic e.g. android needs threshold of 100 due to differece in Bitmap encodings.
*      test = new BlurCheckAndroid(myimage, 1000, true); 
*      test.run();
*      if (test.pass){ //.. test has passed
*
* 
* @author EDINA
*/
public abstract class BlurCheckRunnable implements Runnable {
        /*********/

        /* the actual Kernel. Any 3x3 kernel should work here for experimentation e.g. {-1,-1,-1,-1,8,-1,-1,-1,-1}  */
        public static final float LAPLACE_KERNEL[]={0,-1,0,-1,4,-1,0,-1,0};
        /* Check this after the test is run for the result. This is null if process has not yet been "run()" */
        public Boolean pass; //Whether the image passed blur check threshold
        public long variance; //Laplace variance value of the image
        
        
        /*********/

        protected File file;
        protected BufferedImage bufImg; 
        protected int threshold;
        protected boolean debug;

        /** Executes the test. Can run as a thread but should be quite fast anyway */
        public abstract void run();

        /** use this for debug messages */
        protected abstract void dbg(String msg);

        /** Theshold is the desired variance i.e. the higher the sharper ( 1500 is a good start) */ 
        public BlurCheckRunnable(File imageFile, int threshold, boolean debug){
            this.pass = null;
            this.variance = 0;
            this.file = imageFile;
            this.threshold = threshold;
            this.debug = debug;
        }

		public BlurCheckRunnable(int threshold, boolean debug) {
			this.pass = null;
			this.variance = 0;
			this.file = null;
			this.threshold = threshold;
            this.debug = debug;
		}
}
