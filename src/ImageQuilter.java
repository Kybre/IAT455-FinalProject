import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ImageQuilter extends Frame {

	BufferedImage srcImage, patternImage, finalImage;
	int width, height;
	
	public ImageQuilter() {
		//load images
		try {
			srcImage = ImageIO.read(new File("")); //fill in file values later
			patternImage = ImageIO.read(new File(""));

		} catch (Exception e) {
			System.out.println("Cannot load the provided image");
		}
		
		width = srcImage.getWidth();
		height = srcImage.getHeight();
	}
	
	public void paint(Graphics g) {
		
		//if working with different images, this may need to be adjusted
		int w = width; 
		int h = height;

		this.setSize(w * 5 + 300, h * 3 + 150);

		g.drawImage(srcImage,25,50,w, h,this);
	    g.drawImage(patternImage, 25+w+45, 50, w, h,this);

	    g.setColor(Color.BLACK);
	    Font f1 = new Font("Verdana", Font.PLAIN, 13); 
	    g.setFont(f1); 
	    g.drawString("Original Image", 25, 45); 
	    g.drawString("Pattern Image",25+w+45, 45); 
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImageQuilter iq = new ImageQuilter();
		iq.repaint();
	}

}
