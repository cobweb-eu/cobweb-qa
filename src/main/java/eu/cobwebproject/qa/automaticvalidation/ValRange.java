package eu.cobwebproject.qa.automaticvalidation;

/**
* Class to Check for whether value falls in a range
* Extremely trivial test, implemented as much as a learning
* excercise for the procedure of calling library functions
* from the COBWEB QA WPS processes. 
*
* Example Usage:
*     if(ValRange.valueInRange(value, lowerBound, upperBound)) { // test passed....
*
* 
* @author Environment Systems
*/
public class ValRange {
	public static boolean valueInRange(double value, double minVal, double maxVal) {
		return value <= maxVal && value >= minVal;
	}
}
