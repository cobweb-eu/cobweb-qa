package eu.cobwebproject.qa.lbs;

import org.junit.Test;

import junit.framework.TestCase;


public class SpatialAccuracyTest extends TestCase{
    
    @Test
    public void testSpatialAccuracy() {
        SpatialAccuracy sp = new SpatialAccuracy();
        
        int minSatNum = 5;
        double minAcc = 30.5;
        
        assertTrue(sp.isAccurate(30.4, minAcc));
        assertTrue(sp.isAccurate(30.4, minAcc, 6, minSatNum));
        assertTrue(sp.isAccurate(30.4, minAcc, 5, minSatNum));
        assertFalse(sp.isAccurate(30.4, minAcc, 4, minSatNum));
        
        assertFalse(sp.isAccurate(30.5, minAcc));
        assertFalse(sp.isAccurate(30.5, minAcc, 6, minSatNum));
        
        assertFalse(sp.isAccurate(30.6, minAcc));
        assertFalse(sp.isAccurate(30.5, minAcc, 6, minSatNum));        
    }
}
