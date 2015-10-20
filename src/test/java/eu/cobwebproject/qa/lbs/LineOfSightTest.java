package eu.cobwebproject.qa.lbs;

import java.net.URL;

import org.junit.Test;

import junit.framework.TestCase;

public class LineOfSightTest extends TestCase{
    
    @Test
    public void testLineOfSight() {
        URL url = this.getClass().getResource("surfaceModel.txt");
        RasterReader rr = new RasterReader(url.getFile().toString());
        
        double headerData[] = new double[6];
        
        headerData = rr.getASCIIHeader();
        
        double surfaceModel[][] = new double[(int) headerData[0]][(int) headerData[1]];
        
        surfaceModel = rr.getASCIIData();

        Parameters parameters = new Parameters((int)headerData[4], (int)headerData[0], 
                (int)headerData[1], headerData[2], headerData[3], headerData[5]);
        
        double easting = 265365;
        double northing = 289115;
        double bearing = 0;
        double tilt = -1;
        double myHeight = 1.5;
        double myCoords[] = new double[2];
        
        myCoords[0] = easting;
        myCoords[1] = northing;        
        
        LineOfSightCoordinates loS = new LineOfSightCoordinates(
                myCoords,
                surfaceModel,
                parameters,
                bearing,
                tilt,
                myHeight);
        
        double []result = new double[5];
        
        result = loS.getMyLoSResult();
        
        //System.out.println("result " + result[0] + " " + result[1] + " " + result[2] + " " + result[3] + " " + result[4]);
        assertEquals(8.1, result[0]);
        assertEquals(49.43426, result[1]);
        assertEquals(265365.0, result[2]);
        assertEquals(289123.0, result[3]);
        assertEquals(45.0, result[4]);
    }
}
