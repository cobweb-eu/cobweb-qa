package eu.cobwebproject.qa.lbs;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import junit.framework.TestCase;

public class LineOfSightTest extends TestCase{
   
    /**
     * Compares the results of running the same tests across different rasters
     */
    @Test
    public void testCompareRasters() {
    	URL asciiFile1 = this.getClass().getResource("surfaceModel.txt");
    	String testFile1 = asciiFile1.getFile().toString(); 	// this is the original surface model Sam provided
    	
    	URL asciiFile2 = this.getClass().getResource("surfaceModelNRW.asc");
    	String testFile2 = asciiFile2.getFile().toString(); 	// this is the NRW 1M dataset of the same area
    	
    	Raster raster1 = null;
    	Raster raster2 = null;
    	
    	try {
			raster1 = new Raster(testFile1);
			System.out.println("File 1: "+ testFile1);
			raster2 = new Raster(testFile2);
			System.out.println("File 2: "+ testFile2);
		} catch (IOException e) {
			fail("IOException: " + e.getMessage());
		}

    	double easting, northing, bearing, tilt, myHeight;
    	double[] result;
    	LineOfSightCoordinates loS;
   
    	// set up and run test conditions for raster 1
    	/*
    	easting = 265365;
        northing = 289115;
        bearing = 0;
        tilt = -1;
        myHeight = 1.5;*/
    	easting = 265114.674984; 
        northing = 289276.72543;
        bearing = 0;
        tilt = -1;
        myHeight = 1.5;

        loS = new LineOfSightCoordinates(
                new double[]{easting,northing},
                raster1.getSurfaceModel(),
                raster1.getParams(),
                bearing,
                tilt,
                myHeight);
        
        result = loS.getMyLoSResult();
        System.out.println("result " + result[0] + " " + result[1] + " " + result[2] + " " + result[3] + " " + result[4]);
        /*
        assertEquals(8.1, result[0]);
        assertEquals(49.43426, result[1]);
        assertEquals(265365.0, result[2]);
        assertEquals(289123.0, result[3]);
        assertEquals(45.0, result[4]);*/
                
        // set up and run test 2
        easting = 265114.674984; 
        northing = 289276.72543;
        bearing = 0;
        tilt = -1;
        myHeight = 1.5;
        
        loS = new LineOfSightCoordinates(
                new double[]{easting,northing},
                raster2.getSurfaceModel(),
                raster2.getParams(),
                bearing,
                tilt,
                myHeight);
        
        result = loS.getMyLoSResult();
        System.out.println("result " + result[0] + " " + result[1] + " " + result[2] + " " + result[3] + " " + result[4]);
    }
}
