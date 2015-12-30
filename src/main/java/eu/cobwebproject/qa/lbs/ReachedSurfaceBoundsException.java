package eu.cobwebproject.qa.lbs;

/**
 * Exception for when we try and go beyond the extent of the heightmap
 * when performing line of sight calculations
 * 
 * @author Environment Systems - sebastian.clarke@envsys.co.uk
 *
 */
public class ReachedSurfaceBoundsException extends IntersectionException {
	public ReachedSurfaceBoundsException(String message) {
		super(message);
	}
}
