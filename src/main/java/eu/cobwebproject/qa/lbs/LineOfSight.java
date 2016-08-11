package eu.cobwebproject.qa.lbs;

/**
 * Class giving access to Line of Sight approximation algorithm
 * 
 * This class will return an array of doubles with approximate 
 * ray/surface intersection information given an origin position,
 * orientation and height map.
 * 
 * How To Use The Class:
 * 
 * LineOfSight should be instantiated with a set of parameters 
 * of required input values (see constructor) You can then call
 * the instance.calculateLOS() method to calculate the intersection
 * and return the values.
 * 
 * The instance can be safely updated with new values before 
 * calling instance.calculateLOS() again.
 * 
 * Alternatively, you can call a throw away version using the 
 * static convenience method LineOfSight.Calculate(...) which
 * will return the results directly given the input parameters
 *  
 * Algorithmic Details:
 * 
 * An approximation algorithm is used to incrementally step
 * along a 2d horizontal path in the direction of the heading.
 * At each step, visible height is calculated according to the
 * tilt and the distance of this step from the origin. Surface
 * height is also extracted from the model at this step position.
 * When the visible height is less than or equal to the surface
 * height then we return resulting position information  
 * 
 * This is a re-implemented and slightly modified version of 
 * of Sam Meek's original Line Of Sight calculation class.
 * 
 * @Todo Re-Factor the result object to a class
 * 
 * @author Sebastian Clarke - 12/2015 - Environment Systems - sebastian.clarke@envsys.co.uk
 * 	
 */

public class LineOfSight {
	public static final double VIEW_DISTANCE = 1000; // arbitrary limit in vision distance to limit iterations - 0.5km
	public static final double STEP_SIZE = 0.1; // step size for LOS approximation algorithm
	
	private Raster heightMap;
	private double userHeight;
	private double bearing;
	private double tilt;
	private double currentNorthing;
	private double currentEasting;
	private double[] currentResult;
	private double stepSize;

	/**
	 * Construct a new LineOfSight check object
	 * 
	 * @param heightMap The heightMap to check LOS against
	 * @param easting World easting of eye position
	 * @param northing World northing of eye position
	 * @param bearing Bearing in degrees from device (heading)
	 * @param tilt Tilt of the eye in degrees, 0 is horizontal, 90 is pointing at ground 
	 * @param userHeight Height of the phone/eye
	 */
	public LineOfSight(Raster heightMap, double easting, double northing, double bearing, double tilt, double userHeight) {
		this.heightMap = heightMap;
		this.userHeight = userHeight;
		this.tilt = tilt;
		this.bearing = bearing;
		this.currentEasting = easting;
		this.currentNorthing = northing;
		this.stepSize = STEP_SIZE;
		this.currentResult = null;
	}
	
	/**
	 * Perform the Line of Sight calculation
	 * 
	 * Iterates down the heading, checking a projected ray height from the eye against the surface
	 * If it intersects then we return the results of the calculation. If we do not intersect we return null.
	 * If we try and look outside of the height map coverage, we throw ArrayIndexOutOfBounds
	 * 
	 * Will return a cached result if no attributes have changed since last run
	 * 
	 * @return null if there is no intersection within DRAW_DISTANCE, otherwise the result of LOS calculation 
	 * (horizontal distance to target, world height of user, x of target, y of target, height of target)
	 * If we did not intersect the heightmap within view distance or we went out of bounds
	 * @throws ReachedSurfaceBoundsException If we try to look out of the extent of the raster
	 * @throws NoIntersectionException If we do not intersect the heightmap surface within VIEW_DISTANCE
	 * @throws StartPositionOutOfBoundsException 
	 */
	public double[] calculateLOS() throws ReachedSurfaceBoundsException, NoIntersectionException, StartPositionOutOfBoundsException {
		if(currentResult != null) 
			return currentResult;						// return a cached result if there is one
		
		if(!isCurrentPositionInBounds()) {				// check that we are in the heightmap coverage area
			throw new StartPositionOutOfBoundsException("Position " + currentEasting + "," + currentNorthing + " is out of bounds of the heightmap");
		}
		
		double distance = userHeight; 				// start the distance down the line at userHeight
		double delta = stepSize; 						// step size for the algorithm
		double scanLimit = VIEW_DISTANCE; 			// draw distance
		double theta = getBearingAsRadians();			// convert heading to height map radians
		
		double eyeHeight = userHeight + getSurfaceHeightForPoint(currentNorthing, currentEasting);
		
		while (distance < scanLimit) {					// scan down in step size until scan limit
			double x = Math.cos(theta) * distance;		// x displacement
			double y = Math.sin(theta) * distance; 	// y displacement
						 
			// use the tilt to calculate ray height at this distance
			double visionHeight = getRayHeight(distance, eyeHeight);
			// get the height from the surface map at this displacement
			double surfaceHeight = getSurfaceHeightForPoint(currentNorthing + y, currentEasting + x);
			
			if(visionHeight <= surfaceHeight) {			// intersection test
				currentResult = new double[]{distance, eyeHeight, currentEasting + x, currentNorthing + y, surfaceHeight}; 
				return currentResult;
			}
			distance += delta;							// step the distance
		}
		
		throw new NoIntersectionException("Did not intersect surface within view distance " + VIEW_DISTANCE + "m");
		
	}

	////////////////////////////
	// PRIVATE UTIL FUNCTIONS //
	////////////////////////////
	
	/**
	 * Gets the Y Cell index for a world northing coordinate
	 * 
	 * @param northing The Northing in world coordinates
	 * @return the col index of the Y cell containing that northing
	 */
	private int getYCell(double northing) {
		double localY = northing - heightMap.getParams().getylCorner();	
		int cellIndex = (int) Math.ceil(localY / heightMap.getParams().getcellSize());
		return heightMap.getParams().getnRows() - cellIndex; // reverse indexing, need to use nrows for the reversing of a northing value. 
	}
	
	/**
	 * Gets the X Cell index for a world easting coordinate
	 *  
	 * @param easting Easting of the point in world coordinates
	 * @return Easting as an array coordinate
	 */
	private int getXCell(double easting){
		double localX = easting - heightMap.getParams().getxlCorner();
		int cellIndex = (int) Math.floor((localX / heightMap.getParams().getcellSize()));
		return cellIndex; // apparently no reverse indexing.. (it's because ascii index from lower left corner)
	}
	
	/**
	 * Reads the surface model and returns the height for a given point in world coordinates
	 * Does not perform any interpolation within the cells
	 * 
	 * @param northing world northing coord
	 * @param easting world easting coord
	 * @return height from the cell for this coord
	 * @throws ReachedSurfaceBoundsException If the world point is beyond the bounds of the raster extent
	 */
	private double getSurfaceHeightForPoint(double northing, double easting) throws ReachedSurfaceBoundsException {
		try {	
			return heightMap.getXY(getXCell(easting),getYCell(northing));
		} catch (ArrayIndexOutOfBoundsException e) {
			// Tried to look outside height map extent
			throw new ReachedSurfaceBoundsException(e.getMessage());
		}
	}
	
	/**
	 * Convert the current compass bearing to an angle from y = 0 to index the height map

	 * @return the angle to index the heightmap in radians as a double
	 */
	private double getBearingAsRadians() {
		return Math.toRadians(360.0 - (bearing-90.0));
	}
	
	/**
	 * Gets the height of a ray from the eye at current tilt and given height and distance
	 * @param distance the horizontal distance down the ray
	 * @param eyeHeight the starting height of the ray origin
	 * @return the vertical height of the line of sight ray
	 */
	private double getRayHeight(double distance, double eyeHeight) {
		return (distance * Math.tan(Math.toRadians(-tilt))) + eyeHeight;
	}
	
	private boolean isCurrentPositionInBounds() {
		return heightMap.isPointInBounds(currentEasting, currentNorthing);
	}
 
	//////////////////////////////////
	// STATIC CONVENIENCE FUNCTIONS //
	//////////////////////////////////
	
	/**
	 * Does a one off calculation of Line of Sight
	 *  
	 * @param heightMap The heightMap to check LOS against
	 * @param easting World easting of eye position
	 * @param northing World northing of eye position
	 * @param bearing Bearing in degrees from phone (heading)
	 * @param tilt Tilt of the phone in degrees, 0 is horizontal
	 * @param userHeight Height of the phone/eye
	 * @return the result of LOS calculation (horizontal distance to target, height of user, x of target, y of target, height of target) or null if no intersection
	 * @throws NoIntersectionException If not pointing at surface within LineOfSight.VIEW_DISTANCE
	 * @throws ReachedSurfaceBoundsException If tried to look beyond the bounds of the raster extent
	 * @throws StartPositionOutOfBoundsException If starting position not covered by raster  
	 */
	public static double[] Calculate(Raster heightMap, double easting, double northing, double bearing, double tilt, double userHeight) throws ReachedSurfaceBoundsException, NoIntersectionException, StartPositionOutOfBoundsException {
		LineOfSight los = new LineOfSight(heightMap, easting, northing, bearing, tilt, userHeight);
		return los.calculateLOS();
	}
	
	/**
	 * Gets the result array as a nice string
	 *
	 * @param result The result double array from LOS calculation
	 * @return The result summarised as a string\
	 */
	public static String resultAsString(double[] result) {
		assert result.length == 5;
		String resultString = "Distance:" + result[0];
		resultString += " My Height:" + result[1];
		resultString += " Intersect Point:" + result[2] + "," + result[3];
		resultString += " Intersect Height:" + result[4];
		return resultString;
	}
	
	/////////////////////////
	// GETTERS AND SETTERS //
	/////////////////////////
	
	public void setHeightMap(Raster heightMap) {
		this.heightMap = heightMap;
		this.currentResult = null;
	}
	
	public void setUserHeight(double userHeight) {
		this.userHeight = userHeight;
		this.currentResult = null;
	}
	
	public void setBearing(double bearing) {
		this.bearing = bearing;
		this.currentResult = null;
	}
	
	/**
	 * Set Tilt
	 * @param tilt Tilt of the eye in degrees, 0 is horizontal, 90 is pointing at ground
	 */
	public void setTilt(double tilt) {
		this.tilt = tilt;
		this.currentResult = null;
	}
	
	public void setCurrentNorthing(double currentNorthing) {
		this.currentNorthing = currentNorthing;
		this.currentResult = null;
	}
	
	public void setCurrentEasting(double currentEasting) {
		this.currentEasting = currentEasting;
		this.currentResult = null;
	}
	
	/**
	 * Set the step size for the LOS approximation algorithm
	 * 
	 * @param newStepSize The new step size (in metres)
	 */
	public void setStepSize(double newStepSize) {
		this.stepSize = newStepSize;
		this.currentResult = null;
	}
	
	/**
	 * Gets the current step size for the LOS approximation algorithm
	 * 
	 * @return the current step size (in metres)
	 */
	public double getStepSize() {
		return this.stepSize;
	}
}
