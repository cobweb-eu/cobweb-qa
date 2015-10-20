package pillar.automaticvalidation;
import java.io.File;


public class Main {

	public static void main(String[] args) {
		
		File testImage = new File("resources/flower_blurry.jpg");
		
		int threshold = 200;
		
		LaplacePhotoBlurCheck lap = new LaplacePhotoBlurCheck(testImage, threshold);
		
		boolean out = lap.getPassDecision();
		
		System.out.println(out);

	}

}
