package pillar.lbs;

public class Main {

	public static void main(String[] args) {
		
		//simple raster reader.
		RasterReader rr = new RasterReader("resources/surfaceModel.txt");
		
		double headerData[] = new double[6];
		
		headerData = rr.getASCIIHeader();
		
		System.out.println("headerData " + headerData[0] + " " + headerData[1] + " " 
		+ headerData[2] + " " + headerData[3]);

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
		
		
		LineOfSightCoordinates LoS = new LineOfSightCoordinates(myCoords,surfaceModel,
				parameters, bearing, tilt,myHeight);
		
		double []result = new double[5];
		
		result = LoS.getMyLoSResult();
		
		System.out.println("result " + result[0] + " " + result[1] + 
				" " + result[2] + " " + result[3] + " " + result[4]);
		
	}

}
