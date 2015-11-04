package eu.cobwebproject.qa.automaticvalidation;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import junit.framework.TestCase;

/**
* Test BlurCheckAwt with sample image.

    Configuration variables:
        int threshold (1500) : The sharpness threshold, the higher the sharper
        bool debug (True) : generate intermediate (greyscale/edge detect) images
*/

public class BlurCheckAwtTest extends TestCase{
    private final int threshold = 1500;
    private final boolean debug = true;

    /**
     * Test a blurred image fails BlurCheck
     */
    @Test
    public void testBlurred() {
        URL url = this.getClass().getResource("flower_blurred.png");

        try{
            File testImage = new File(url.toURI());
            BlurCheckAwt lap = new BlurCheckAwt(testImage, threshold, debug);
            lap.run();
            assertFalse(lap.pass);
        }
        catch(URISyntaxException ex){
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testBlurredSmall() {
        URL url = this.getClass().getResource("flower_blurred_small.png");

        try{
            File testImage = new File(url.toURI());
            
            BlurCheckAwt lap = new BlurCheckAwt(testImage, threshold, debug);
            lap.run();
            assertFalse(lap.pass);
        }
        catch(URISyntaxException ex){
            fail(ex.getMessage());
        }
    }

    @Test
    public void testSharp() {
        URL url = this.getClass().getResource("flower_sharp.jpg");      

        try{
            File testImage = new File(url.toURI());
            BlurCheckAwt lap = new BlurCheckAwt(testImage, threshold, debug);
            lap.run();
            assertTrue(lap.pass);
        }
        catch(URISyntaxException ex){
            fail(ex.getMessage());
        }
    }

    /**
     * sharp
     */
    @Test
    public void testCOBWEBButterfly() {
        URL url = this.getClass().getResource("cobweb/butterfly.jpg");

        try{
            File testImage = new File(url.toURI());
            BlurCheckAwt lap = new BlurCheckAwt(testImage, threshold, debug);
            lap.run();
            assertTrue(lap.pass);
        }
        catch(URISyntaxException ex){
            fail(ex.getMessage());
        }
    }

    /**
     * blur
     */
    @Test
    public void testCOBWEBNatureBlur1() {
        URL url = this.getClass().getResource("cobweb/nature-blur.jpg");

        try{
            File testImage = new File(url.toURI());
            BlurCheckAwt lap = new BlurCheckAwt(testImage, threshold, debug);
            lap.run();
            assertFalse(lap.pass);
        }
        catch(URISyntaxException ex){
            fail(ex.getMessage());
        }
    }

    /**
     * sharp
     */
    @Test
    public void testCOBWEBNature() {
        URL url = this.getClass().getResource("cobweb/nature.jpg");

        try{
            File testImage = new File(url.toURI());
            BlurCheckAwt lap = new BlurCheckAwt(testImage, threshold, debug);
            lap.run();
            assertTrue(lap.pass);
        }
        catch(URISyntaxException ex){
            fail(ex.getMessage());
        }
    }

    /**
     * blur
     */
    @Test
    public void testCOBWEBNatureBlur2() {
        URL url = this.getClass().getResource("cobweb/nature-blur2.jpg");

        try{
            File testImage = new File(url.toURI());
            BlurCheckAwt lap = new BlurCheckAwt(testImage, threshold, debug);
            lap.run();
            assertFalse(lap.pass);
        }
        catch(URISyntaxException ex){
            fail(ex.getMessage());
        }
    }

    /**
     * sharp
     */
    @Test
    public void testCOBWEBNature2() {
        URL url = this.getClass().getResource("cobweb/nature2.jpg");

        try{
            File testImage = new File(url.toURI());
            BlurCheckAwt lap = new BlurCheckAwt(testImage, threshold, debug);
            lap.run();
            assertTrue(lap.pass);
        }
        catch(URISyntaxException ex){
            fail(ex.getMessage());
        }
    }

    /**
     * sharp
     */
    @Test
    public void testCOBWEBWhiteMoth() {
        URL url = this.getClass().getResource("cobweb/white_moth.jpg");

        try{
            File testImage = new File(url.toURI());
            BlurCheckAwt lap = new BlurCheckAwt(testImage, threshold, debug);
            lap.run();
            assertTrue(lap.pass);
        }
        catch(URISyntaxException ex){
            fail(ex.getMessage());
        }
    }

}
