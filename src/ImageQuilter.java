import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

class ImageQuilter extends Frame {

	BufferedImage srcImage, patternImage, finalImage;
	int width, height;
	
	public ImageQuilter() {
		//load images
		try {
			srcImage = ImageIO.read(new File("background.jpg")); //fill in file values later
			patternImage = ImageIO.read(new File("statue.jpg"));

		} catch (Exception e) {
			System.out.println("Cannot load the provided image");
		}
		this.setTitle("Image Quilter");
		this.setVisible(true);
		
		width = srcImage.getWidth();
		height = srcImage.getHeight();
		
		//Anonymous inner-class listener to terminate program
		this.addWindowListener(
			new WindowAdapter(){//anonymous class definition
				public void windowClosing(WindowEvent e){
					System.exit(0);//terminate the program
				}//end windowClosing()
			}//end WindowAdapter
		);//end addWindowListener
	}
	
	private BufferedImage createQuiltedImage() {
	    int blockSize = 20;
	    int gridWidth = width / blockSize;
	    int gridHeight = height / blockSize;
	    BufferedImage quiltedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    Graphics g = quiltedImage.getGraphics();

	    for (int x = 0; x < gridWidth; x++) {
	        for (int y = 0; y < gridHeight; y++) {
	            int srcX = (int) (Math.random() * gridWidth) * blockSize;
	            int srcY = (int) (Math.random() * gridHeight) * blockSize;
	            g.drawImage(srcImage, x * blockSize, y * blockSize, x * blockSize + blockSize, y * blockSize + blockSize,
	                        srcX, srcY, srcX + blockSize, srcY + blockSize, null);
	        }
	    }

	    g.dispose();
	    return quiltedImage;
	}
	
	
	
	
	
	public void paint(Graphics g) {
	    int w = width / 2;
	    int h = height / 2;

	    this.setSize(w * 3 + 100, h + 150);

	    g.drawImage(srcImage, 25, 50, w, h, this);
	    BufferedImage quiltedImage = createQuiltedImage();
	    g.drawImage(quiltedImage, 25 + w + 45, 50, w, h, this);

	    g.setColor(Color.BLACK);
	    Font f1 = new Font("Verdana", Font.PLAIN, 13);
	    g.setFont(f1);
	    g.drawString("Original Image", 25, 45);
	    g.drawString("Random Blocks", 25 + w + 45, 45);
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImageQuilter iq = new ImageQuilter();
		iq.repaint();
	}

}
