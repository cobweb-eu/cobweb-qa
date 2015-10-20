package eu.cobwebproject.qa;

/**
 * 
 * @author Sam Meek
 *
 * TODO
 */
public class LineOfSightCoordinates {

    private double[] myCoords = new double [2];
    private double[][] surfaceModel;
    private double myHeight;
    private Parameters parameters;
    double []myData;
    static double distanceToTarget;
    double []myResult = new double[6];

    /**
     * 
     * @param myCoords a double of coordinates in a planar projection
     * @param surfaceModel an array of the surface model
     * @param parameters a set of parameters that describe the surface model
     * @param bearing the bearing in degrees from North
     * @param tilt the tilt of the device where 0 is perpendicular to the ground
     * @param userHeight the height as a user added as a constant to the height extracted from the surface model
     */

    public LineOfSightCoordinates(double[] myCoords, double[][] surfaceModel, Parameters parameters,
            double bearing, double tilt, double userHeight ){

        this.myCoords = myCoords;
        this.surfaceModel = surfaceModel;
        this.parameters = parameters;

        myResult = GetHeightICanSee(myCoords, surfaceModel, parameters, bearing, tilt, userHeight);

    }	

    /**
     * 
     * @param coords a double of coordinates in a planar projection
     * @param surfaceModel an array of the surface model
     * @param parameters a set of parameters that describe the surface model
     * @param theta the bearing in degrees from North
     * @param elevation the tilt of the device where 0 is perpendicular to the ground
     * @param heightOffset the height as a user added as a constant to the height extracted from the surface model
     * @return the result of the line of sight as an array (distance to target, height of user, x of target, y of target, height of target)
     */
    private double[] GetHeightICanSee(double[] coords, double[][] surfaceModel, Parameters parameters, 
            double theta, double elevation, double heightOffset){

        double myHeight = - 1;

        try{
            myHeight = surfaceModel[(int)getMyy(coords[1],parameters)]
                    [(int)getMyx(coords[0], parameters)] + heightOffset;
            System.out.println("myHeight " + myHeight);

        }
        catch (ArrayIndexOutOfBoundsException e){
            System.out.println("ArrayOutOfBounds");
        }

        dhTuple myResult = heightICanSee(makeAngle(theta), Math.toRadians(elevation), surfaceModel, 
                parameters.cellSize, (int)getMyx(coords[0],parameters), (int)getMyy(coords[1],parameters), myHeight);

        double []myData = new double[3];

        myData = conMyResult(myResult);

        return myData;
    }
    /**
     * @return myResult
     */
    private double[] getMyResult(){
        return myData;
    }

    /**
     * @return coordinates of the target on the array
     */
    public double[] getArrayCoords(){
        double[] arrayCoords = new double[2];
        arrayCoords[0] = (getMyx(getMyResult()[2], parameters))/parameters.getnCols();
        arrayCoords[1] = (getMyy(getMyResult()[3], parameters))/parameters.getnRows();
        return arrayCoords;

    }

    /**
     * @return position as a decimal on the screen to draw on a map
     */
    public double[] getMyPositionDraw(){
        double[] myPos = new double[2];
        myPos [0] = (myCoords[0])/parameters.getnCols();
        myPos [1] = (myCoords[1])/parameters.getnRows();
        return myPos;
    }

    /**
     * @param dh dhTurple
     * @return myResult
     */
    private double [] conMyResult(dhTuple dh){
        double [] myResult = new double[5];
        try{

            myResult[0] = dh.d;
            myResult[1] = dh.h;
            myResult[2] =  (int) ((int) ((dh.x) * parameters.getcellSize()) + parameters.getxlCorner());;
            myResult[3] = (int) ((int) ((int) ((parameters.getnCols() - dh.y - 1)  * parameters.getcellSize())) + parameters.getylCorner());
            myResult[4] = (int) surfaceModel[(int)getMyy(myCoords[1],parameters)][(int)getMyx(myCoords[0], parameters)];
        }
        catch (NullPointerException e){


            for(int i = 0; i < 4; i++){
                myResult[i] = -1;
            }

        }

        return myResult;
    }

    /**
     * @param Easting Easting of the point
     * @param parameters parameters of the surface model
     * @return Easting as an array coordinate
     */
    private static double getMyx(double Easting, Parameters parameters){
        double i = ((Easting - parameters.getxlCorner())) / parameters.getcellSize();
        return i;
    }

    /**
     * 
     * @param Northing Northing of the point
     * @param parameters parameters of the surface model
     * @return Northing as an array coordinate
     */
    private static double getMyy(double Northing, Parameters parameters){
        double i = (parameters.getnCols() - 
                ((Northing - parameters.getylCorner())) / parameters.getcellSize());
        return i;
    }


    /**
     * @author Sam Meek
     * A helper class as Java does not return Tuples
     */
    private static class xyTuple {
        double x, y;
        int xcell, ycell;

        public xyTuple(double x, double y, int xcell, int ycell) {
            this.x = x;
            this.y = y;
            this.xcell = xcell;
            this.ycell = ycell;
        }

        public String toString() {
            return "(" + x + "," + y + "," + xcell + "," + ycell + ")";
        }
    }

    //convert bearing into direction
    //changed to accommodate COBWEB 
    /**
     * @param degrees compass in degress
     * @return radians of real degrees
     */
    private static double makeAngle(double degrees) {
        double realDegrees = 360 - 
                (degrees - 90); // to turn our bearing into a degree from y=0
        //changed here:
        return Math.toRadians(realDegrees);
    }

    /**
     * @param theta compass
     * @param d distance
     * @param cellsize size of the cells in the surface model
     * @return
     */
    private static xyTuple getCellAt(double theta, double d, double cellsize) {
        double x = Math.cos(theta) * d;
        double y = Math.sin(theta) * d;
        int xcell = (int) Math.floor(x / cellsize);
        int ycell = (int) Math.floor(y / cellsize);
        return new xyTuple(x, y, xcell, ycell);
    }


    /**
     * @param elevation tilt
     * @param d distance to check along the array
     * @param myHeight the height of the user
     * @return
     */
    private static double getHeightAt(double elevation, double d, double myHeight) {
        return (d * Math.tan(elevation)) + myHeight;
    }

    private static class dhTuple {
        double d, h, x, y;

        public dhTuple(double d, double h, int y, int x) {
            this.d = d;
            this.h = h;
            this.y = y;
            this.x = x;
        }


    }

    /**
     * 
     * @param theta compass bearing
     * @param elevation tilt
     * @param universe surface model 
     * @param cellsize square metres represented by each cell
     * @param myx user position X
     * @param myy user position Y
     * @param myHeight height
     * @return
     */
    private static dhTuple heightICanSee(double theta, double elevation, double[][] universe, 
            double cellsize, int myx, int myy, double myHeight) {
        double d = cellsize + 0.1; // enter number of cells to ignore here (if any).
        // This is so that we ignore the cell we are 'in'.
        double dincr = cellsize/2;
        xyTuple xy = null;

        try { 

            while (d < (universe[0].length * cellsize)/2) {
                xy = getCellAt(theta, d, cellsize);

                double h = getHeightAt(elevation, d, myHeight);

                //System.out.println(h);

                // The strange -s are because we have to flip from 'geometry' to 'array index'

                if (h < universe[myy-xy.ycell - 1][myx + xy.xcell]){

                    distanceToTarget = d;
                    return new dhTuple(d, (universe[myy-xy.ycell - 1][myx + xy.xcell]),myy-xy.ycell - 1, myx + xy.xcell);
                }

                d += dincr;

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("** The world is flat and we fell off it.");
            return null;
        }
        System.out.println("** Can't see anything at this distance.");
        return null;
    }

    /**
     * @return the height of the user on the surface model
     */
    public double getMyHeight(){
        return myHeight;
    }
    /**
     * 
     * @return coordinates of the user on the array
     */
    public double[] getMyArrayCoords(){
        double[] myArrayCoords = new double[2];

        myArrayCoords[0] = getMyx(getMyResult()[2], parameters);
        myArrayCoords[1] = getMyy(getMyResult()[3], parameters);

        return myArrayCoords;

    }

    /**
     * @return distance from the user to the target
     */
    public double getDistanceToTarget(){
        return distanceToTarget;	
    }

    /**
     * 
     * @return the result of the line of sight as an array (distance to target, height of user, x of target, y of target, height of target)
     */
    public double[] getMyLoSResult(){
        return myResult;
    }

    public double[] getTargetCoordinates(){
        double []coords = new double[2];
        coords[0] = myResult[2];
        coords[1] = myResult[3];
        return coords;
    }

    public double getHeightOfTarget(){
        return myResult[5];
    }


}


