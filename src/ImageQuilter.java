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
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

class ImageQuilter extends JFrame implements ActionListener {

    BufferedImage srcImage, patternImage, finalImage;
    BufferedImage[][] imageBlocks; // Array to store image blocks
    double screenWidth, screenHeight;
    int srcWidth, srcHeight, patternWidth, patternHeight;
    Dimension screenSize;
    JButton srcButton, patternButton, updateButton;
    JTextField blockSizeText, overlapText;
    
    int blockSize = 20, overlapPercent = 10;

	
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
		updateButton = new JButton("Update Parameters");
		blockSizeText = new JTextField(Integer.toString(blockSize));
		overlapText = new JTextField(Integer.toString(overlapPercent));
		
		srcButton.addActionListener(this);
		patternButton.addActionListener(this);
		updateButton.addActionListener(this);
		
		this.setLayout(null);
		
		this.add(srcButton);
		this.add(patternButton);
		this.add(updateButton);
		this.add(blockSizeText);
		this.add(overlapText);
		
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize);
	}
	
	public void actionPerformed(ActionEvent e) {
		blockSize = Integer.parseInt(blockSizeText.getText());
		overlapPercent = Integer.parseInt(overlapText.getText());
		
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
					patternImage = ImageIO.read(patternFile);
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
        int gridWidth = srcImage.getWidth() / blockSize;
        int gridHeight = srcImage.getHeight() / blockSize;
        imageBlocks = new BufferedImage[gridWidth][gridHeight]; // Initialize the array

        BufferedImage quiltedImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = quiltedImage.getGraphics();

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                int srcX = (int) (Math.random() * gridWidth) * blockSize;
                int srcY = (int) (Math.random() * gridHeight) * blockSize;

                // Extracting the block and storing it in the array
                imageBlocks[x][y] = srcImage.getSubimage(srcX, srcY, blockSize, blockSize);

                g.drawImage(imageBlocks[x][y], x * blockSize, y * blockSize, null);
            }
        }

        g.dispose();

        List<BufferedImage> sortedBlocks = sortBlocksByBrightness();
        
        return assembleSortedBlocks(sortedBlocks, gridWidth, gridHeight, blockSize);
        
    }
	
	private List<BufferedImage> sortBlocksByBrightness() {
        List<BufferedImage> blocks = new ArrayList<>();
        for (BufferedImage[] row : imageBlocks) {
            blocks.addAll(Arrays.asList(row));
        }

        blocks.sort(Comparator.comparingDouble(this::calculateAverageBrightness));
        return blocks;
    }

	
	private double calculateAverageBrightness(BufferedImage block) {
        long sum = 0;
        for (int x = 0; x < block.getWidth(); x++) {
            for (int y = 0; y < block.getHeight(); y++) {
                Color color = new Color(block.getRGB(x, y));
                int brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                sum += brightness;
            }
        }
        return sum / (double) (block.getWidth() * block.getHeight());
    }

    private BufferedImage assembleSortedBlocks(List<BufferedImage> blocks, int gridWidth, int gridHeight, int blockSize) {
        BufferedImage newImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = newImage.getGraphics();

        int index = 0;
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                BufferedImage block = blocks.get(index++);
                g.drawImage(block, x * blockSize, y * blockSize, null);
            }
        }

        g.dispose();
        return newImage;
    }
    
    
    
    private BufferedImage recreatePatternImage() {
        int gridWidth = patternImage.getWidth() / blockSize;
        int gridHeight = patternImage.getHeight() / blockSize;

        BufferedImage newImage = new BufferedImage(patternImage.getWidth(), patternImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = newImage.getGraphics();

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                BufferedImage block = patternImage.getSubimage(x * blockSize, y * blockSize, blockSize, blockSize);
                double brightness = calculateAverageBrightness(block);
                BufferedImage closestMatch = findClosestMatch(brightness);
                g.drawImage(closestMatch, x * blockSize, y * blockSize, null);
            }
        }

        g.dispose();
        return newImage;
    }
    
    private BufferedImage findClosestMatch(double targetBrightness) {
        BufferedImage closest = null;
        double minDiff = Double.MAX_VALUE;

        for (BufferedImage[] row : imageBlocks) {
            for (BufferedImage block : row) {
                double blockBrightness = calculateAverageBrightness(block);
                double diff = Math.abs(blockBrightness - targetBrightness);

                if (diff < minDiff) {
                    minDiff = diff;
                    closest = block;
                }
            }
        }

        return closest;
    }
    
    
    private BufferedImage getRightOverlap(BufferedImage block) {
        int overlapWidth = block.getWidth() / 4;
        return block.getSubimage(block.getWidth() - overlapWidth, 0, overlapWidth, block.getHeight());
    }

    private BufferedImage getLeftOverlap(BufferedImage block) {
        int overlapWidth = block.getWidth() / 4;
        return block.getSubimage(0, 0, overlapWidth, block.getHeight());
    }

    private BufferedImage getBottomOverlap(BufferedImage block) {
        int overlapHeight = block.getHeight() / 4;
        return block.getSubimage(0, block.getHeight() - overlapHeight, block.getWidth(), overlapHeight);
    }

    private BufferedImage getTopOverlap(BufferedImage block) {
        int overlapHeight = block.getHeight() / 4;
        return block.getSubimage(0, 0, block.getWidth(), overlapHeight);
    }

    
    private BufferedImage createQuiltedImage2() {
        int gridWidth = srcImage.getWidth() / blockSize;
        int gridHeight = srcImage.getHeight() / blockSize;
        BufferedImage quiltedImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = quiltedImage.getGraphics();
        
        double initialTolerance = 3;  // Initial tolerance factor (e.g., 110% of the best SSD)
        double toleranceReduction = 0.1;  // Reduction factor for each iteration

        BufferedImage[][] selectedBlocks = new BufferedImage[gridHeight][gridWidth];

        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                BufferedImage chosenBlock = null;
                double tolerance = initialTolerance;

                while (chosenBlock == null) {
                    List<BufferedImage> suitableBlocks = new ArrayList<>();
                    double minSSD = Double.MAX_VALUE;

                    for (BufferedImage[] row : imageBlocks) {
                        for (BufferedImage block : row) {
                            double ssdLeft = 0.0, ssdTop = 0.0;

                            if (x > 0) {  // Calculate SSD for left overlap
                                BufferedImage leftOverlap = getRightOverlap(selectedBlocks[y][x - 1]);
                                BufferedImage currentLeftOverlap = getLeftOverlap(block);
                                ssdLeft = calculateOverlapSSD(leftOverlap, currentLeftOverlap, leftOverlap.getWidth());
                            }

                            if (y > 0) {  // Calculate SSD for top overlap
                                BufferedImage topOverlap = getBottomOverlap(selectedBlocks[y - 1][x]);
                                BufferedImage currentTopOverlap = getTopOverlap(block);
                                ssdTop = calculateOverlapSSD(topOverlap, currentTopOverlap, topOverlap.getHeight());
                            }

                            double combinedSSD = ssdLeft + ssdTop;
                            if (combinedSSD < minSSD * tolerance) {
                                if (combinedSSD < minSSD) {
                                    minSSD = combinedSSD;
                                    suitableBlocks.clear();
                                }
                                suitableBlocks.add(block);
                            }
                        }
                    }

                    if (!suitableBlocks.isEmpty()) {
                        int randomIndex = (int) (Math.random() * suitableBlocks.size());
                        chosenBlock = suitableBlocks.get(randomIndex);
                    } else {
                        tolerance -= toleranceReduction;
                        if (tolerance <= 0) {
                            throw new RuntimeException("No suitable block found within tolerance");
                        }
                    }
                }

                selectedBlocks[y][x] = chosenBlock;
                g.drawImage(chosenBlock, x * blockSize - blockSize / 4, y * blockSize, null);
            }
        }

        g.dispose();
        return quiltedImage;
    
    }

    
    private double calculateOverlapSSD(BufferedImage block1, BufferedImage block2, int overlapWidth) {
        double ssd = 0.0;

        for (int x = 0; x < overlapWidth; x++) {
            for (int y = 0; y < block1.getHeight(); y++) {
                int rgb1 = block1.getRGB(x, y);
                int rgb2 = block2.getRGB(x, y);

                int red1 = (rgb1 >> 16) & 0xff;
                int green1 = (rgb1 >> 8) & 0xff;
                int blue1 = (rgb1) & 0xff;

                int red2 = (rgb2 >> 16) & 0xff;
                int green2 = (rgb2 >> 8) & 0xff;
                int blue2 = (rgb2) & 0xff;

                ssd += Math.pow(red1 - red2, 2);
                ssd += Math.pow(green1 - green2, 2);
                ssd += Math.pow(blue1 - blue2, 2);
            }
        }

        return ssd;
    }

    
	
	
	public void paint(Graphics g) {
		super.paint(g);
		
	    g.setColor(Color.BLACK);
	    Font f1 = new Font("Verdana", Font.PLAIN, 13); 
	    g.setFont(f1); 
		
		srcButton.setBounds(25, 25, 125, 25);
		srcButton.setBackground(Color.gray.brighter());
		
		patternButton.setBounds(175, 25, 125, 25);
		patternButton.setBackground(Color.gray.brighter());
		
		g.drawString("Block Size:", 350, 70);
		blockSizeText.setBounds(425, 25, 50, 25);
		
		g.drawString("Overlap %:", 500, 70);
		overlapText.setBounds(575, 25, 50, 25);
		
		updateButton.setBounds(screenSize.width - 250, 25, 200, 25);
		updateButton.setBackground(new Color(164,213,227));
		
		g.setColor(new Color(20,20,20));
		g.drawLine(25, 100, (int)screenSize.getWidth()-25, 100);
		
		//images
		int sw = srcImage.getWidth();
		int sh = srcImage.getHeight();
		
		int pw = patternImage.getWidth();
		int ph = patternImage.getHeight();
		
		g.drawImage(srcImage, 25, 140, sw, sh, this);
	    g.drawImage(patternImage, sw+75, 140, pw, ph, this);
	    g.drawImage(createQuiltedImage(), sw+pw+125, 140, sw, sh, this); 
	    g.drawImage(createQuiltedImage2(), sw, 540, sw, sh, this); 
	    
	    g.drawImage(recreatePatternImage(), sw+pw+125, 540, sw, sh, this);
	    
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
