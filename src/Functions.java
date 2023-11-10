import java.awt.Color;
import java.awt.image.BufferedImage;

public class Functions {
	
	public BufferedImage over(BufferedImage foreground, BufferedImage matte, BufferedImage background) {
		
		// Write your code here
		BufferedImage fgm = combineImages(foreground, matte, Operations.multiply);
		BufferedImage bgm = combineImages(background, matte, Operations.multiply);
		BufferedImage invBgm = combineImages(background, bgm, Operations.subtract);
		BufferedImage result = combineImages(fgm, invBgm, Operations.add);
		
		// NOTE: You should change the return statement below to the actual result
		return result;
	}
	
	public BufferedImage invert(BufferedImage src) {
		BufferedImage result = new BufferedImage(src.getWidth(),
				src.getHeight(), src.getType());

    	for(int i = 0; i < result.getWidth(); i++) {
    		for(int j = 0; j < result.getHeight(); j++) {
    			int sCol = src.getRGB(i, j);
    			
    			int newR = 255 - getRed(sCol);
    			int newG = 255 - getGreen(sCol);
    			int newB = 255 - getBlue(sCol);
    			
    			result.setRGB(i, j, new Color(newR, newG, newB).getRGB());
    		}
    	}
		return result;
	}
	
	public BufferedImage combineImages(BufferedImage src1, BufferedImage src2, Operations op) {

		if (src1.getType() != src2.getType()) {
			System.out.println("Source Images should be of the same type");
			return null;
		}
		BufferedImage result = new BufferedImage(src1.getWidth(),
				src1.getHeight(), src1.getType());
		
    	for(int i = 0; i < result.getWidth()-1; i++) {
    		for(int j = 0; j < result.getHeight()-1; j++) {
				int s1c = src1.getRGB(i, j);
				int s2c = src2.getRGB(i, j);
				int newR = 0, newG = 0, newB = 0;
				
    			if(op == Operations.add) {
    				newR = getRed(s1c) + getRed(s2c);
    				newG = getGreen(s1c) + getGreen(s2c);
    				newB = getBlue(s1c) + getBlue(s2c);
    			}
    			if(op == Operations.subtract) {
    				newR = Math.abs(getRed(s1c) - getRed(s2c));
    				newG = Math.abs(getGreen(s1c) - getGreen(s2c));
    				newB = Math.abs(getBlue(s1c) - getBlue(s2c));
    			}
    			if(op == Operations.multiply) {
    				newR = (getRed(s1c) * getRed(s2c)) / 255;
    				newG = (getGreen(s1c) * getGreen(s2c)) / 255;
    				newB = (getBlue(s1c) * getBlue(s2c)) / 255;
    			}
    			
    			newR = clip(newR);
    			newG = clip(newG);
    			newB = clip(newB);
    			
    			result.setRGB(i, j, new Color(newR, newG, newB).getRGB());
    		}
    	}
		
		return result;
	}
	
	private int clip(int v) {
		v = v > 255 ? 255 : v;
		v = v < 0 ? 0 : v;
		return v;
	}

	protected int getRed(int pixel) {
		return (pixel >>> 16) & 0xFF;
	}

	protected int getGreen(int pixel) {
		return (pixel >>> 8) & 0xFF;
	}

	protected int getBlue(int pixel) {
		return pixel & 0xFF;
	}
}
