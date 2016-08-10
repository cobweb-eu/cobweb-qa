package eu.cobwebproject.qa.lbs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * Test cases for the line of sight algorithm
 * 
 * @author Sebastian Clarke - Environment Systems - sebastian.clarke@envsys.co.uk
 *
 */
public class LineOfSightTest extends TestCase {
	
	private static final boolean DEBUG = true;
	
	private static final double ACCURACY = 0.2;
	private static final String RASTER1_RESOURCE = "surfaceModel.txt"; 		// this is the original surface model Sam provided
	private static final String RASTER2_RESOURCE = "surfaceModelNRW.asc";	// this is the NRW 1M dataset of the same area
	private static final String FLAT_RESOURCE = "surfaceModel_flat_1m.asc";	// sample flat dataset
	private static final String OBSERVATION_AREA_RESOURCE = "surfaceModel_sn7698.txt";
	private static final String SMALL_RESOURCE = "surfaceModel_tiny.asc"; //Small 10x10 file for debugging
	private static final String RASTER3_RESOURCE= "surfaceModelNRW_rectangle_wide.asc"; //wide (100 cols, 900 rows) modification to NRW sample tile 
	private static final String RASTER4_RESOURCE= "surfaceModelNRW_rectangle_gdalclip_narrow.asc"; //Gdal clip (700 cols, 900 rows) of NRW tile to make a tall model
	
	
	private double easting, northing, bearing, tilt, myHeight;				// test conditions
	private LineOfSight los;
	
	/**
	 * Tests the new Line Of Sight implementation using a flat surface
	 * model at 1m height.
	 * 
	 * This will check that the intersection occurs roughly at the
	 * predicted location and that the values returned are correct
	 * 
	 * @throws IOException If there was a problem reading the surface model
	 * @throws IntersectionException If we did not intersect the surface model
	 */
	@Test
	public void testWithFlatSurface() throws IOException, IntersectionException {
		// Surface Model
		Raster flatSurface = new Raster(fileFromResource(FLAT_RESOURCE));
		double flatHeight = 1.0;	// the uniform height of the flat surface
		
		// Test Values
		easting = 265547.050156; 
		northing = 289498.392446;
		bearing = 45;
		tilt = 20;
		myHeight = 2;
		
		printStartingConditions("Testing new LOS Implementation with flat surface model");
		
		// Calculate expected results
		final double horizontalDisplacement = myHeight * Math.tan(Math.toRadians(90-tilt));
		final double expectedXPosition = easting + Math.sin(Math.toRadians(bearing)) * horizontalDisplacement;
		final double expectedYPosition = northing + Math.cos(Math.toRadians(bearing)) * horizontalDisplacement;
		// Perform line of sight estimation - use static method for throw-away call
		double result[] = LineOfSight.Calculate(flatSurface, easting, northing, bearing, tilt, myHeight);
		dbg(LineOfSight.resultAsString(result));
		assertAlmostEqual(expectedXPosition, result[2], 0.1);	// check within 10cm (approximation dependent on LineOfSight.stepSize)
		assertAlmostEqual(expectedYPosition, result[3], 0.1);
		assertEquals(result[4], flatHeight); 					// intersection height
		assertEquals(result[1], flatHeight + myHeight);			// user height 
		assertAlmostEqual(result[0], horizontalDisplacement, ACCURACY);		// horizontal distance
	}
	
	/**
	 * Tests LOS against the NRW surface model, standing in field, tilted at horizon.
	 * 
	 * Extensive test that tests headings in all quadrants with known
	 * good values and responses as checked in QGIS.
	 * 
	 * @throws IOException if problem reading the raster
	 * @throws IntersectionException if we unexpectedly did not intersect the DTM
	 */
	public void testInFieldWithNRWDTM() throws IOException, IntersectionException {
        double expectedIntersectHeight, expectedX, expectedY, expectedDistance, expectedEyeHeight;
        double[] result;
		
		// Set up initial test conditions
		easting = 265114.674984;	// standing in a field 
        northing = 289276.72543;	// standing in a field
        bearing = 0;				// facing north
        tilt = 0;					// angled at horizon
        myHeight = 2;				// 2m tall
        
        // load raster and setup LineOfSight instance
        Raster raster = new Raster(fileFromResource(RASTER2_RESOURCE));
        los = new LineOfSight(raster, easting, northing, bearing, tilt, myHeight);
        
        expectedEyeHeight = myHeight + 72.42;  			// known for our position
        expectedIntersectHeight = 74.65;				// correct for this orientation
        expectedX = easting;
        expectedY = 289286.02543;						// checked in qgis
        expectedDistance = 9.3;   
        printStartingConditions("Testing NRW DSM - standing in field facing north");
        result = los.calculateLOS();
        dbg(LineOfSight.resultAsString(result));        
        checkResult(result, expectedEyeHeight, expectedIntersectHeight, expectedX, expectedY, expectedDistance);
       
        los.setBearing(bearing = 45); 	
        expectedIntersectHeight = 74.45;
        expectedX = 265120.048995537;
        expectedY = 289282.099441537;
        expectedDistance = 7.6;
        printStartingConditions("Testing NRW DSM - standing in field facing NE");
        result = los.calculateLOS();
        dbg(LineOfSight.resultAsString(result));
        checkResult(result, expectedEyeHeight, expectedIntersectHeight, expectedX, expectedY, expectedDistance);
        
        los.setBearing(bearing = 90); 	
        expectedIntersectHeight = 74.47;
        expectedX = 265128.074984;
        expectedY = northing;
        expectedDistance = 13.4;
        printStartingConditions("Testing NRW DSM - standing in field facing E");
        result = los.calculateLOS();
        dbg(LineOfSight.resultAsString(result));
        checkResult(result, expectedEyeHeight, expectedIntersectHeight, expectedX, expectedY, expectedDistance);
        
        los.setBearing(bearing = 135); 	
        expectedIntersectHeight = 74.58;
        expectedX = 265145.0098649129;
        expectedY = 289246.3905490871;
        expectedDistance = 42.9;  
        printStartingConditions("Testing NRW DSM - standing in field facing SE");
        result = los.calculateLOS();
        dbg(LineOfSight.resultAsString(result));
        checkResult(result, expectedEyeHeight, expectedIntersectHeight, expectedX, expectedY, expectedDistance);
        
        los.setBearing(bearing = 180); 	 
        printStartingConditions("Testing NRW DSM - standing in field facing South");
        try {
        	result = los.calculateLOS();
        	fail("Expected to not intersect in bounds of height map");
        } catch (IntersectionException e) {
        	// assert
        }
    
        los.setBearing(bearing = 225); 	 
        printStartingConditions("Testing NRW DSM - standing in field facing SW");
        try {
        	result = los.calculateLOS();
        	fail("Expected to not intersect in bounds of height map");
        } catch (IntersectionException e) {
        	assertEquals(e.getMessage(), "Surface X out of bounds: -1");
        }
     
        
        los.setBearing(bearing = 270);
        expectedIntersectHeight = 74.57;
        expectedX = 265075.974984;
        expectedY = northing;
        expectedDistance = 38.7;
        printStartingConditions("Testing NRW DSM - standing in field facing West");
        result = los.calculateLOS();
        dbg(LineOfSight.resultAsString(result));
        checkResult(result, expectedEyeHeight, expectedIntersectHeight, expectedX, expectedY, expectedDistance);
        
        los.setBearing(bearing = 315);
        expectedIntersectHeight = 74.55;
        expectedX = 265102.9370114323;
        expectedY = 289288.46340256766;
        expectedDistance = 16.6;
        printStartingConditions("Testing NRW DSM - standing in field facing NW");
        result = los.calculateLOS();
        dbg(LineOfSight.resultAsString(result));
        checkResult(result, expectedEyeHeight, expectedIntersectHeight, expectedX, expectedY, expectedDistance);
        
        los.setBearing(bearing = -45);
        printStartingConditions("Testing NRW DSM - standing in field facing NW (negative bearing value)");
        result = los.calculateLOS();
        checkResult(result, expectedEyeHeight, expectedIntersectHeight, expectedX, expectedY, expectedDistance);
	}
		
    /**
     * Tests the consistency of Line Of Sight on two rasters
     * of the same location. DSM and DTM. Uses a test location
     * in a field so results should be similar for both.
     * 
     * Confirms the results match within +- ACCURACY
     * 
     * @throws IOException If there was a problem reading the rasters 
	 * @throws IntersectionException if we unexpectedly did not intersect the DTM
     */
    @Test
    public void testCompareRastersField() throws IOException, IntersectionException {
    	// Load surface models
    	Raster raster1 = new Raster(fileFromResource(RASTER1_RESOURCE));
    	Raster raster2 = new Raster(fileFromResource(RASTER2_RESOURCE));
    	
    	// Test conditions (starting in a flat-ish field)
    	easting = 265114.674984; 
        northing = 289276.72543;
        bearing = 0;	// facing north
        tilt = 20;
        myHeight = 2;
        
        los = new LineOfSight(raster1, easting, northing, bearing, tilt, myHeight);
        printStartingConditions("Testing Both surface models - standing in field facing north");
        double[] result1 = los.calculateLOS();
        los.setHeightMap(raster2);
        double[] result2 = los.calculateLOS();
        
        for(int i = 0; i < 5; i++) {
        	assertAlmostEqual(result1[i], result2[i], ACCURACY);
        }      
    }
    
    /**
     * Tests that the line of sight algorithm responds sensibly
     * to changes, e.g., if you look down, does it get shorter?
     * 
     * Also checks that the height for the user stays constant.
     * 
     * @throws IOException if problem reading raster
     * @throws IntersectionException if we unexpectedly did not intersect the DTM
     */
    @Test
    public void testShorterLookingDown() throws IOException, IntersectionException {
    	Raster nrwHeightMap = new Raster(fileFromResource(RASTER2_RESOURCE));
    	
    	easting = 265114.674984; 					
        northing = 289276.72543;
        bearing = 0;
        tilt = 1;
        myHeight = 1.5;
        
        double lastDistance = 1000000;
        double prevHeight = 0;
        los = new LineOfSight(nrwHeightMap, easting, northing, bearing, tilt, myHeight);
        
        for(int i = 0; i < 8; i++) {
        	tilt = (i*10)+1;
        	los.setTilt(tilt);
        	double[] result = los.calculateLOS();
            assertTrue(result[0] <= lastDistance);
            if(i > 0) 
            	assertEquals(result[1], prevHeight);
            lastDistance = result[0];
            prevHeight = result[1];
        }
    }
    
    public void testLookingUp() throws IOException, NoIntersectionException, StartPositionOutOfBoundsException {
    	Raster nrwHeightMap = new Raster(fileFromResource(RASTER2_RESOURCE));
    	
    	// Test conditions (starting in a flat-ish field)
    	easting = 265114.674984; 					
        northing = 289276.72543;
        bearing = 90;
        tilt = -20; 		// Looking up
        myHeight = 1.5;
        
        try {
        	double[] result = LineOfSight.Calculate(nrwHeightMap, easting, northing, bearing, tilt, myHeight);
        	fail("intersected heightmap when looking up");
        } catch (ReachedSurfaceBoundsException e) {
        	assertEquals(e.getMessage(), "Surface X out of bounds: 1000");
        }
    }
        
    /**
     * Test using values directly copied from a real observation (uses custom heightmap location)
     * @throws IOException
     * @throws IntersectionException
     */
    public void testWPSObservations() throws IOException, IntersectionException {
    	Raster observationRaster = new Raster(fileFromResource(OBSERVATION_AREA_RESOURCE));
    	
    	easting = 276283.4833342557; 					
        northing = 298379.6816316855;
        bearing = 182.5;
        tilt = 82.0; 
        myHeight = 1.5;
        
    	double[] result = LineOfSight.Calculate(observationRaster, easting, northing, bearing, tilt, myHeight);
    	checkResult(result, 134.43, 133.03, 276283.41790517466, 298378.18305935315, 1.5);
    }
    
    /**
     * This test shows example usages for the LineOfSight Class
     */
    public void testShowUsage() {
    	Raster heightMap = null;
    	
    	try {
    		heightMap = new Raster(fileFromResource(RASTER2_RESOURCE));
    	} catch (IOException e) {
    		// handle failure reading file
    		fail("Couldn't read file");
    	}
    	
    	double[] result = null;
    	
    	try {
    		result = LineOfSight.Calculate(heightMap, 265114.674984, 289276.72543, 0, 20, 1.5);
    	} catch (NoIntersectionException e) {
    		// handle that we were not pointing at the ground (within 1k)
    	} catch (ReachedSurfaceBoundsException e) {
    		// handle that we tried to look outside the bounds of surface raster extent
    	} catch (StartPositionOutOfBoundsException e) {
			// handle that the starting point is not within the bounds of the raster extent
		}
    	
    	// use result
    	System.out.println(LineOfSight.resultAsString(result));
    	
    }
        
    public void testSmall() throws IOException, NoIntersectionException, StartPositionOutOfBoundsException, ReachedSurfaceBoundsException {
    	Raster nrwHeightMap = new Raster(fileFromResource( SMALL_RESOURCE)); //Small grid for debugging
    	easting = 265003.51847;
        northing =289004.48428;
        bearing = 0;
        tilt = 70; //Lots of tilt so we don'r run out of the model 
        myHeight = 1.5; 
        
        los = new LineOfSight(nrwHeightMap, easting, northing, bearing, tilt, myHeight);
    	double[] result = los.calculateLOS();
        System.out.println("result0 " + result[0]);
        System.out.println("result1 " + result[1]);
        System.out.println("result23 " + result[2] + ", " +result[3]);
        System.out.println("result2 " + result[3]);
    }
    
    
	/**
	 * Test of NRW surface model with fewer rows than cols
	 * 
	 * replicates test position data of testInFieldWithNRWDTM()
	 * 
	 * @throws IOException if problem reading the raster
	 * @throws IntersectionException if we unexpectedly did not intersect the DTM
	 */
    
    public void testInFieldWithNRWDTMRectangle() throws IOException, IntersectionException{
        double expectedIntersectHeight, expectedX, expectedY, expectedDistance, expectedEyeHeight;
        double[] result;
        
		// Set up initial test conditions
		easting = 265114.674984;	// standing in a field 
	    northing = 289276.72543;	// standing in a field
	    bearing = 0;				// facing north
	    tilt = 0;					// angled at horizon
	    myHeight = 2;				// 2m tall
	    
	    // load raster and setup LineOfSight instance
	    Raster raster = new Raster(fileFromResource(RASTER3_RESOURCE));
	    los = new LineOfSight(raster, easting, northing, bearing, tilt, myHeight);
	    
	    expectedEyeHeight = myHeight + 72.42;  			// known for our position
	    expectedIntersectHeight = 74.65;				// correct for this orientation
	    expectedX = easting;
	    expectedY = 289286.02543;						// checked in qgis
	    expectedDistance = 9.3;   
	    printStartingConditions("Testing NRW DSM - standing in field facing north");
	    result = los.calculateLOS();
        
	    System.out.println("distance: " + result[0]);
        System.out.println("result1 " + result[1]);
        System.out.println("result2&3 " + result[2] + ", " +result[3]);
        
	    dbg(LineOfSight.resultAsString(result));
	    checkResult(result, expectedEyeHeight, expectedIntersectHeight, expectedX, expectedY, expectedDistance);        
    }
    
    
    
    
	/**
	 * Test of NRW surface model clipped using gdal. 
	 * 
	 * replicates test position data of testInFieldWithNRWDTM()
	 * 
	 * @throws IOException if problem reading the raster
	 * @throws IntersectionException if we unexpectedly did not intersect the DTM
	 */
    
    public void testInFieldWithNRWDTMRectangleGDAL() throws IOException, IntersectionException{
        double expectedIntersectHeight, expectedX, expectedY, expectedDistance, expectedEyeHeight;
        double[] result;
        
		// Set up initial test conditions
		easting = 265114.674984;	// standing in a field 
	    northing = 289276.72543;	// standing in a field
	    bearing = 0;				// facing north
	    tilt = 0;					// angled at horizon
	    myHeight = 2;				// 2m tall
	    
	    // load raster and setup LineOfSight instance
	    Raster raster = new Raster(fileFromResource(RASTER4_RESOURCE));
	    los = new LineOfSight(raster, easting, northing, bearing, tilt, myHeight);
	    
	    expectedEyeHeight = myHeight + 72.41999816894531;  			// known for our position
	    expectedIntersectHeight = 74.6500015258789;				// correct for this orientation
	    expectedX = easting;
	    expectedY = 289286.02543;						// checked in qgis
	    expectedDistance = 9.3;   
	    printStartingConditions("Testing NRW DSM - standing in field facing north");
	    result = los.calculateLOS();
        
	    System.out.println("distance: " + result[0]);
        System.out.println("result1 " + result[1]);
        System.out.println("result2&3 " + result[2] + ", " + result[3]);
        
	    dbg(LineOfSight.resultAsString(result));
	    checkResult(result, expectedEyeHeight, expectedIntersectHeight, expectedX, expectedY, expectedDistance);        
    }
    
        
    private void printStartingConditions(String testName) {
    	if(DEBUG) {
	    	System.out.println(testName);
	    	System.out.println("Test Position: " + String.valueOf(easting) + "," + String.valueOf(northing));
	    	System.out.println("Test Heading: " + String.valueOf(bearing));
	    	System.out.println("Test Tilt: " + String.valueOf(tilt));
	    	System.out.println("Test User Height: " + String.valueOf(myHeight));
    	}
    }
    
    private String fileFromResource(String resourceName) {
    	String fileName = this.getClass().getResource(resourceName).getFile();
		try {
			URI uri = new URI(fileName);
			return uri.getPath();
		} catch (URISyntaxException e) {
			System.out.println("Could not parse file name: " + fileName);
			e.printStackTrace();
			return fileName;
		}
	}
    
    private static void checkResult(double[] result, double eyeHeight, double intersectHeight, double xCoord, double yCoord, double distance) {
        assertEquals(result[1], eyeHeight);
        assertEquals(result[4], intersectHeight);
        assertAlmostEqual(result[2], xCoord, ACCURACY);
        assertAlmostEqual(result[3], yCoord, ACCURACY);
        assertAlmostEqual(result[0], distance, ACCURACY);
    }
    
    private static void assertAlmostEqual(double d1, double d2, double delta) {
    	assertTrue((d1-d2)<delta);
    }
    
    private static void dbg(String msg) {
    	if(DEBUG)
    		System.out.println(msg);
    }
}
