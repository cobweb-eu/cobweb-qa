package eu.cobwebproject.qa.automaticvalidation;

public interface IImage {
    int getWidth();
    int getHeight();
    int getRGB(int x, int y);
    int getType();
        
    void setRGB(int x, int y, int rgb);
}
