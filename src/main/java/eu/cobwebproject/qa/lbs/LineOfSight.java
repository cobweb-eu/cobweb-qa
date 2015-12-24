package eu.cobwebproject.qa.lbs;

/**
 * Line of sight algorithm
 * 
 * Re-implemented verion of Sam Meek's Line Of Sight calculation class
 * 
 * @author Sebastian Clarke - Environment Systems - sebastian.clarke@envsys.co.uk	
 *
 */
public class LineOfSight {
	private static final double STEP_SIZE = 0.1; // step size for LOS approximation algorithm
	private static final double DRAW_DISTANCE = 500; // arbitrary limit in vision distance to limit iterations - 0.5km
	
	private Raster heightMap;
	private double userHeight;
	private double bearing;
	private double tilt;
	private double currentNorthing;
	private double currentEasting;
	private double[] currentResult;
	
	/**
	 * Construct a new LineOfSight check object
	 * 
	 * @param heightMap The heightMap to check LOS against
	 * @param easting World easting of eye position
	 * @param northing World northing of eye position
	 * @param bearing Bearing in degrees from phone (heading)
	 * @param tilt Tilt of the phone in degrees, 0 is horizontal
	 * @param userHeight Height of the phone/eye
	 */
	public LineOfSight(Raster heightMap, double easting, double northing, double bearing, double tilt, double userHeight) {
		this.heightMap = heightMap;
		this.userHeight = userHeight;
		this.tilt = tilt;
		this.bearing = bearing;
		this.currentEasting = easting;
		this.currentNorthing = northing;
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
	 * @return the result of LOS calculation (horizontal distance to target, height of user, x of target, y of target, height of target) or null if no intersection
	 * @throws ArrayIndexOutOfBoundsException if we tried to look outside the heightmap coverage
	 */
	public double[] calculateLOS() throws ArrayIndexOutOfBoundsException {
		if(currentResult != null) 
			return currentResult;					// return a cached result if there is one
		
		double distance = userHeight; 				// start the distance down the line at userHeight
		double delta = STEP_SIZE; 					// step size for the algorithm
		double scanLimit = DRAW_DISTANCE; 			// draw distance
		double theta = getBearingAsRadians();		// convert heading to height map radians
		
		double eyeHeight = userHeight + getSurfaceHeightForPoint(currentNorthing, currentEasting);
		
		while (distance < scanLimit) {				// scan down in step size until scan limit
			double x = Math.cos(theta) * distance;	// x displacement
			double y = Math.sin(theta) * distance; 	// y displacement
			
			// use the tilt to calculate ray height at this distance
			double visionHeight = getRayHeight(distance, eyeHeight);
			// get the height from the surface map at this displacement
			double surfaceHeight = getSurfaceHeightForPoint(currentNorthing + y, currentEasting + x);
			
			if(visionHeight <= surfaceHeight) {		// intersection test
				currentResult = new double[]{distance, eyeHeight, currentEasting + x, currentNorthing + y, surfaceHeight}; 
				return currentResult;
			}
			distance += delta;						// step the distance
		}
		
		return null;								// we hit nothing
	}
	
	// Configuration Setters
	
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
	
	// Private utility functions
	
	/**
	 * Gets the Y Cell index for a world northing coordinate
	 * 
	 * @param northing The Northing in world coordinates
	 * @return the col index of the Y cell containing that northing
	 */
	private int getYCell(double northing) {
		double localY = northing - heightMap.getParams().getylCorner();	
		int cellIndex = (int) Math.floor(localY / heightMap.getParams().getcellSize());
		
		return heightMap.getParams().getnCols() - cellIndex; // reverse indexing
	}
	
	/**
	 * Gets the X Cell index for a world easting coordinate
	 *  
	 * @param easting Easting of the point in world coordinates
	 * @return Easting as an array coordinate
	 */
	private int getXCell(double easting){
		double localX = easting - heightMap.getParams().getxlCorner();
		int cellIndex = (int) Math.floor(localX / heightMap.getParams().getcellSize());
		
		return cellIndex; // apparently no reverse indexing..
	}
	
	/**
	 * Reads the surface model and returns the height for a given point in world coordinates
	 * Does not perform any interpolation within the cells
	 * 
	 * @param northing world northing coord
	 * @param easting world easting coord
	 * @return height from the cell for this coord
	 */
	private double getSurfaceHeightForPoint(double northing, double easting) {
		return heightMap.getXY(getYCell(northing), getXCell(easting));
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
	 * @return the vertical height of the line of sight ray
	 */
	private double getRayHeight(double distance, double eyeHeight) {
		return (distance * Math.tan(Math.toRadians(tilt))) + eyeHeight;
	}
 
	// Public static helper convenience functions
	
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
	 */
	public static double[] Calculate(Raster heightMap, double easting, double northing, double bearing, double tilt, double userHeight) {
		LineOfSight los = new LineOfSight(heightMap, easting, northing, bearing, tilt, userHeight);
		return los.calculateLOS();
	}
}
