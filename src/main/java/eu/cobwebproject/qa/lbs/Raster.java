package eu.cobwebproject.qa.lbs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Encapsulates the data about a raster with utility functions to read
 * the raster from an Arc ASCII grid
 * 
 * @author Sebastian Clarke - Environment Systems - sebastian.clarke@envsys.co.uk
 *
 */
public class Raster {
	private final Parameters params;			// The parameters of the data (e.g., rows, cols) 
	private final double[][] surfaceModel;		// The actual surface model data
	private final String fileName;				// the fileName if we did the parseing

	/**
	 * Construct a raster with the parameters and data already parsed.
	 * note: The String file constructor is preferred. 
	 * 
	 * @param p A parsed parsed Parameter object
	 * @param surfaceModel The surface model as a 2d double array
	 */
	public Raster(Parameters p, double[][] surfaceModel) {
		this.fileName = null;
		this.params = p;
		this.surfaceModel = surfaceModel;
	}
	
	/**
	 * Constructor returns a Raster parsed from the file path specified as string
	 * 
	 * @param file A String of the path to the file to parse
	 * @throws IOException If the file can't be found or anything else goes wrong whilst reading
	 */
	public Raster(String file) throws IOException {
		this.fileName = file;	
		// read and parse header from filename to parameters
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			double[] headerData = Raster.consumeRasterHeader(br);	
			this.params = new Parameters(headerData[4], 
										 (int) headerData[0], 
										 (int) headerData[1], 
										 headerData[2], 
										 headerData[3], 
										 headerData[5]);
			this.surfaceModel = Raster.consumeAsciiData(br, params.getnCols(), params.getnRows());
		} finally {
			br.close();
		}
	}
	
	/**
	 * Constructor returns a Raster parsed from the file given by url
	 * 
	 * @param url Url to the ascii heightmap data
	 * @throws IOException 
	 * 
	 */
	public Raster(URL url) throws IOException {
		this.fileName = url.toString();
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		try {
			double[] headerData = Raster.consumeRasterHeader(br);	
			this.params = new Parameters(headerData[4], 
										 (int) headerData[0], 
										 (int) headerData[1], 
										 headerData[2], 
										 headerData[3], 
										 headerData[5]);
			this.surfaceModel = Raster.consumeAsciiData(br, params.getnCols(), params.getnRows());
		} finally {
			br.close();
		}
	}
	
	/**
	 * Getter for the params
	 * @return the parameters
	 */
	public Parameters getParams() {
		return params;
	}
	
	/**
	 * Getter for the surface model
	 * @return the surface model
	 */
	public double[][] getSurfaceModel() {
		return surfaceModel;
	}
	
	/**
	 * Gets the value from the surface model for given x,y cell index coords
	 * 
	 * @param x cell coordinate in rows
	 * @param y cell coordinate in cols
	 * 
	 * @return the value from the surface model raster 
	 */
	public double getXY(int x, int y) {
		if (y >= params.getnCols() || y < 0)
			throw new ArrayIndexOutOfBoundsException("Surface Y out of bounds: " + y);
		if (x >= params.getnRows() || x < 0)
			throw new ArrayIndexOutOfBoundsException("Surface X out of bounds: " + x);
		
		return surfaceModel[y][x];
	}
	
	/**
	 * Private static function to read the raster header data from an open
	 * BufferedReader and return it as an array of doubles. This does
	 * not close the buffered reader stream.
	 *  
	 * @param br The opened buffered reader to consume the header from
	 * @return an array of doubles containing the ascii header data
	 * @throws IOException if there is a problem reading from the bufferedReader 
	 */
	private static double[] consumeRasterHeader(BufferedReader br) throws IOException {
		double[] headerData = new double[6];
			
		for(int i = 0; i < 6; i++) {				
			int skip = 0;	// chars to skip before value
			
			switch (i) {
			case 0:
				skip = 6;
				break;	
			case 1:
				skip = 6;
				break;
			case 2: 	
				skip = 9;
				break;
			case 3:
				skip = 9;
				break;
			case 4: 	
				skip = 8;
				break;
			case 5: 	
				skip = 12;
				break;						
			}
		
			String DSMline = br.readLine();
			char[] buffer = new char[DSMline.length()];
			DSMline.getChars(skip,DSMline.length(), buffer, 0);
			headerData[i] = Double.parseDouble(String.valueOf(buffer));
		}
			
		return headerData;
	}
	
	/**
	 * Private static function to read the surface model raster from an opened
	 * BufferedReader. This does not close the BufferedReader object
	 * 
	 * @param br: The opened BufferedReader
	 * @param cols: The number of columns in the dataset
	 * @param rows: The number of rows in the dataset
	 * @return a 2d array of doubles representing the height field
	 * @throws IOException If there is a problem reading from the file
	 */
	private static double[][] consumeAsciiData(BufferedReader br, int cols, int rows) throws IOException {	
		double[][] ASCIIData = new double[cols][rows];
		
		for(int i = 0;i < cols;i++) {
			String[] temp = br.readLine().trim().split("[ ]+");
			for(int j = 0;j < rows;j++) {
				ASCIIData[i][j] = Double.parseDouble(temp[j]);				
		 	}
		}
		
		return ASCIIData;
	}
	
	public String toString() {
		if(fileName != null)
			return fileName;
		return "Manually parsed raster";
	}
}
