/**
 * 
 */
package eu.cobwebproject.qa.lbs;

/**
 * @author Sebastian Clarke - Environment Systems - sebastian.clarke@envsys.co.uk
 *
 */
public abstract class IntersectionException extends Exception {
	private String message = null;
	
	public IntersectionException() {
		super();
	}
	
	public IntersectionException(String message) {
		super();
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}

}
