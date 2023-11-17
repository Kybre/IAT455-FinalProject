import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

class ImageQuilter extends JFrame implements ActionListener {

	BufferedImage srcImage, patternImage, finalImage;
	double screenWidth, screenHeight;
	int srcWidth, srcHeight, patternWidth, patternHeight;
	Dimension screenSize;
	JButton srcButton, patternButton;
	
	public ImageQuilter() {
		//load images
		try {
			srcImage = ImageIO.read(new File("background.jpg")); //fill in file values later
			patternImage = ImageIO.read(new File("statue.jpg"));

		} catch (Exception e) {
			System.out.println("Cannot load the provided image");
		}
		this.setTitle("Image Quilter");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//buttons
		srcButton = new JButton("Upload Source");
		patternButton = new JButton("Upload Pattern");
		
		srcButton.addActionListener(this);
		patternButton.addActionListener(this);
		
		this.setLayout(null);
		
		this.add(srcButton);
		this.add(patternButton);
		
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == srcButton) {
			File srcFile = uploadFile();
			if(srcFile != null) {
				try {
					srcImage = ImageIO.read(srcFile);
				} catch (Exception ex) {
					System.out.println("Cannot load the provided image");
				}
			}
		}
		if(e.getSource() == patternButton) {
			File patternFile = uploadFile();
			if(patternFile != null) {
				try {
					srcImage = ImageIO.read(patternFile);
				} catch (Exception ex) {
					System.out.println("Cannot load the provided image");
				}
			}
		}
		repaint();
	}
	
	public File uploadFile() {
		JFileChooser jfc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png");
		
		int returnVal = jfc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	return jfc.getSelectedFile();
        }
        else return null;
	}
	
	private BufferedImage createQuiltedImage() {
	    int blockSize = 20;
	    int gridWidth = srcImage.getWidth() / blockSize;
	    int gridHeight = srcImage.getHeight() / blockSize;
	    BufferedImage quiltedImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
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
		super.paint(g);
		
		srcButton.setBounds(25, 25, 125, 25);
		patternButton.setBounds(175, 25, 125, 25);
		
		g.setColor(new Color(20,20,20));
		g.drawLine(25, 100, (int)screenSize.getWidth()-25, 100);
		
		//images
		int sw = srcImage.getWidth();
		int sh = srcImage.getHeight();
		
		int pw = patternImage.getWidth();
		int ph = patternImage.getHeight();
		
		g.drawImage(srcImage, 25, 140, sw, sh, this);
	    g.drawImage(patternImage, sw+75, 140, pw, ph, this);
	    g.drawImage(createQuiltedImage(), sw+pw+125, 140, sw, sh, this); //replace with final image

	    g.setColor(Color.BLACK);
	    Font f1 = new Font("Verdana", Font.PLAIN, 13); 
	    g.setFont(f1); 
	    g.drawString("Original Image", 25, 125); 
	    g.drawString("Pattern", sw+75, 125); 
	    g.drawString("Result", sw+pw+125, 125); 
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImageQuilter iq = new ImageQuilter();
		iq.setVisible(true);
		iq.repaint();
	}

}
