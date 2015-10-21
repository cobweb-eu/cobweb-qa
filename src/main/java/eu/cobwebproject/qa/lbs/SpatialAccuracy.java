package eu.cobwebproject.qa.lbs;

public class SpatialAccuracy {
    
    /**
     * Assess the spatial accuracy of GPS data.
     * @param acc Accuracy of GPS position.
     * @param minAcc Minimum accuracy threshold.
     * @param numSat Number of satellites.
     * @param minSatNum Minimum Number of satellites threshold.
     * @return True if GPS position is accurate.
     */
    public boolean isAccurate(double acc, double minAcc, int numSat, int minSatNum){
        if(numSat >= minSatNum && isAccurate(acc, minAcc)){
            return true;
        }
        else{
            return false;
        }
    }
    
    /**
     * Assess the spatial accuracy of GPS position.
     * @param acc Accuracy of GPS position.
     * @param minAcc Minimum accuracy threshold.
     * @return True if GPS position is accurate.
     */
    public boolean isAccurate(double acc, double minAcc){
        if(acc >= minAcc){
            return false;
        }
        else{
            return true;
        }
    }    
}
