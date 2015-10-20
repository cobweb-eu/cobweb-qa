package pillar.lbs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
/**
 * 
 * @author Sam Meek
 * A class to read in an ESRI grid and extract the header information
 *
 */

public class RasterReader {

	
	String fileName;
	static double [] headerData = new double[6];
	double [][]ASCIIData;
	
	/**
	 * @param string path to the ESRI grid file 
	 */
	public RasterReader(String string){
		
		this.fileName = string;
		
		headerData = getRasterHeader(string);
		ASCIIData = new double[(int)headerData[0]][(int)headerData[1]];
		ASCIIData = inputASCIIData(string);
		
		
	}
	
	/** 
	 * @param string path to the ESRI grid file 
	 * @return an array[5] of the header data (number of columns, number of rows, X lower corner, Y lower corner, the cell size, no data value)
	 */
	private static double[] getRasterHeader(String string){
	BufferedReader br = null;
	
	double[] headerData = new double[6];
	try {
		br = new BufferedReader(new FileReader(string));
		
		String line = null;
		double var;
							
		for(int i = 0; i < 6; i++){
						
			line = br.readLine();
			char[] buffer = new char[line.length()];	
		 	line.getChars(14,line.length(), buffer, 0);
			String s = String.valueOf(buffer);
			var = Double.parseDouble(s);
			headerData[i] = var;
						
		}
		br.close();
		
	
	}
	catch(Exception e){
	}
	
	return headerData;
	}
	
	/**
	 * @param string path to ESRI grid file
	 * @return an array[][] of the surface model values
	 */
	private static double[][] inputASCIIData(String string){
		BufferedReader br = null;
		
		headerData = new double[6];
		
		double[][] ASCIIData = null;
	
		
		int counter = 0;
		try {			
			br = new BufferedReader(new FileReader(string));
			
			String DSMline = null;
			
			double var;

			for(int i = 0; i < 6; i++){
							
				DSMline = br.readLine();
				
				char[] buffer = new char[DSMline.length()];
				String s = null;
				
				switch (i){
				case 0: 
					
					DSMline.getChars(6,DSMline.length(), buffer, 0);
					s = String.valueOf(buffer);
					var = Double.parseDouble(s);
					headerData[i] = var;
					break;
					
				case 1: 
			
					DSMline.getChars(6,DSMline.length(), buffer, 0);
					s = String.valueOf(buffer);
					var = Double.parseDouble(s);
					headerData[i] = var;
					break;
				
				case 2: 
					
					DSMline.getChars(9,DSMline.length(), buffer, 0);
					s = String.valueOf(buffer);
					var = Double.parseDouble(s);
					headerData[i] = var;
					break;
					
				case 3: 
					
					DSMline.getChars(9,DSMline.length(), buffer, 0);
					s = String.valueOf(buffer);
					var = Double.parseDouble(s);
					headerData[i] = var;
					break;
					
				case 4: 
					
					DSMline.getChars(8,DSMline.length(), buffer, 0);
					s = String.valueOf(buffer);
					var = Double.parseDouble(s);
					headerData[i] = var;
					break;
					
				case 5: 
					
					DSMline.getChars(12,DSMline.length(), buffer, 0);
					s = String.valueOf(buffer);
					var = Double.parseDouble(s);
					headerData[i] = var;
					break;					
				
				}
			
				
			} 
					
			ASCIIData = new double[(int) headerData[0]][(int) headerData[1]];
			
			for(int i = 0;i < headerData[0];i++){
				DSMline = br.readLine();
				String [] temp = new String[(int) headerData[1]];
				temp = DSMline.split("[ ]+");
			
				
				for(int j = 0;j < headerData[1];j++){
					counter++;
					ASCIIData[i][j] = Double.parseDouble(temp[j]);
					
				 	}
									
				}
			
			br.close();
		
			
			
							
		}
			
			catch( IOException e ) {
				
			}
			catch(NullPointerException e){
			}
			catch(NumberFormatException e){
			}
			
			
			return ASCIIData;             
		}
	
	/**
	 * @return surface model data
	 */
	public double[][] getASCIIData(){
		return ASCIIData;
	}
	
	/**
	 * @return surface model headers
	 */
	public double[] getASCIIHeader(){
		return headerData;
	}
	
}