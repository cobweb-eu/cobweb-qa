package eu.cobwebproject.qa.lbs;

public class Parameters {
	
	private double cellSize;
	private int nCols;
	private int nRows;
	private double xlCorner;
	private double ylCorner;
	private double noData;
	
	
	/**
	 * @param cellSize size of the cells
	 * @param nCols the number of columns
	 * @param nRows the number of rows
	 * @param xlCorner the bottom corner of the X of the array
	 * @param ylCorner the bottom corner of the Y of the array
	 * @param noData the no data value
	 */
	public Parameters(double cellSize, int nCols, int nRows, double xlCorner, double ylCorner, double noData) {
		
		this.cellSize = cellSize;
		this.nCols = nCols;
		this.nRows = nRows;
		this.xlCorner = xlCorner;
		this.ylCorner = ylCorner;
		this.noData = noData;
		
	}
	
	/**
	 * @param cellSize sets the size of the cells
	 */
	public void setcellSize(double cellSize){
		
		this.cellSize = cellSize;
		
	}
	
	/**
	 * 
	 * @return the size of the cell
	 */
	public double getcellSize(){
		return cellSize;
	}
	
	/**
	 * 
	 * @param nCols sets the number of columns
	 */
	public void setnCols(int nCols){
		
		this.nCols = nCols;
		
	}
	
	/**
	 * 
	 * @return the number of columns
	 */
	public int getnCols(){
		return nCols;
	}
	
	/**
	 * 
	 * @param nRows sets the number of rows
	 */
	public void setnRows(int nRows){
	
		this.nRows = nRows;
		
	}
	
	/**
	 * 
	 * @return number of rows
	 */
	public int getnRows(){
		return nRows;
	}
	
	/**
	 * 
	 * @param xlCorner sets the Easting corner coordinate
	 */
	public void setxlCorner(double xlCorner){
		
		this.xlCorner = xlCorner;
		
	}
	/**
	 * 
	 * @return the Easting corner coordinate 
	 */
	public double getxlCorner(){
		return xlCorner;
	}
	
	/**
	 * 
	 * @param sets the Northing coordinate corner
	 */
	public void setylCorner(double ylCorner){
		
		this.ylCorner = ylCorner;
		
	}
	
	/**
	 * 
	 * @return the Northing corner coordinate
	 */
	public double getylCorner(){
		return ylCorner;
	}
	
	/**
	 * 
	 * @param noData set the no data value
	 */
	public void setnoData(double noData){
		
		this.noData = noData;
		
	}
	
	/**
	 * 
	 * @return the no data value
	 */
	public double getnoData(){
		return noData;
	}
	
	
	

}
