package eu.cobwebproject.qa.lbs;

import java.io.IOException;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * Test cases for the line of sight algorithm
 * 
 * @author Sebastian Clarke - Environment Systems - sebastian.clarke@envsys.co.uk
 *
 */
public class LineOfSightTest extends TestCase {
	
	private static final double ACCURACY = 0.05;
	private static final String RASTER1_RESOURCE = "surfaceModel.txt"; 		// this is the original surface model Sam provided
	private static final String RASTER2_RESOURCE = "surfaceModelNRW.asc";	// this is the NRW 1M dataset of the same area
	
	private double easting, northing, bearing, tilt, myHeight;
	private double[] result1, result2;
	private LineOfSightCoordinates loS;
	
    /**
     * Compares the results of running the same tests across different rasters
     * for location in field (flat-ish)
     * @throws IOException 
     */
    @Test
    public void testCompareRastersField() throws IOException {
    	Raster raster1 = new Raster(fileFromResource(RASTER1_RESOURCE));
    	Raster raster2 = new Raster(fileFromResource(RASTER2_RESOURCE));
    	
    	// set up and run test conditions for location in field (flat-ish)
    	easting = 265114.674984; 
        northing = 289276.72543;
        bearing = 0;
        tilt = -1;
        myHeight = 1.5;
       
        // raster 1 (from sam)
        loS = new LineOfSightCoordinates(
                new double[]{easting,northing},
                raster1.getSurfaceModel(),
                raster1.getParams(),
                bearing,
                tilt,
                myHeight);
        result1 = loS.getMyLoSResult();
        
        // raster 2 (nrw data)
        loS = new LineOfSightCoordinates(
                new double[]{easting,northing},
                raster2.getSurfaceModel(),
                raster2.getParams(),
                bearing,
                tilt,
                myHeight);
        result2 = loS.getMyLoSResult();
        
        for(int i = 0; i < 5; i++) {
        	assertAlmostEqual(result1[i], result2[i], ACCURACY);
        }
    }
    
    @Test
    public void testSamsLocation() throws IOException {
     	// set up and run test conditions for sam's location 	
    	Raster raster1 = new Raster(fileFromResource(RASTER1_RESOURCE));
    	
        easting = 265365;
        northing = 289115;
        bearing = 0;
        tilt = -1;
        myHeight = 1.5;
        
        // raster 1 (from sam)
        loS = new LineOfSightCoordinates(
                new double[]{easting,northing},
                raster1.getSurfaceModel(),
                raster1.getParams(),
                bearing,
                tilt,
                myHeight);
        result1 = loS.getMyLoSResult();
           
        assertEquals(8.1, result1[0]);
        assertEquals(49.43426, result1[1]);
        assertEquals(265365.0, result1[2]);
        assertEquals(289123.0, result1[3]);
        assertEquals(45.0, result1[4]);
     
        //System.out.println("result1: " + result1[0] + " " + result1[1] + " " + result1[2] + " " + result1[3] + " " + result1[4]);
        //System.out.println("result2: " + result2[0] + " " + result2[1] + " " + result2[2] + " " + result2[3] + " " + result2[4]);
    }
    
    /**
     * Tests that the line of sight algorithm responds sensibly
     * to changes, e.g., if you look down, does it get shorter?
     * @throws IOException if problem reading raster
     */
    @Test
    public void testShorterLookingDown() throws IOException {
    	Raster nrwHeightMap = new Raster(fileFromResource(RASTER2_RESOURCE));
    	
    	easting = 265114.674984; 					
        northing = 289276.72543;
        bearing = 0;
        tilt = -1;
        myHeight = 1.5;
        
        double lastDistance = 1000000;
        //double lastHeight = 0;
        
        for(int i = 0; i < 5; i++) {
        	tilt = (i*-5)-1;
        	System.out.println("Testing with tilt: " + tilt);
        	loS = new LineOfSightCoordinates(
                    new double[]{easting,northing},
                    nrwHeightMap.getSurfaceModel(),
                    nrwHeightMap.getParams(),
                    bearing,
                    tilt,
                    myHeight);
            
            double[] result = loS.getMyLoSResult();
            System.out.println(LineOfSightCoordinates.resultToString(result));
            assertTrue(result[0] < lastDistance);
        }
    }
    
    
    private String fileFromResource(String resourceName) {
    	return this.getClass().getResource(resourceName).getFile().toString();
	}
    
    private static void assertAlmostEqual(double d1, double d2, double delta) {
    	assertTrue((d1-d2)<delta);
    }
}
