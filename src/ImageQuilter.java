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
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

class ImageQuilter extends JFrame implements ActionListener {

    BufferedImage patternImage, srcImage, finalImage;
    BufferedImage[][] imageBlocks; // Array to store image blocks
    double screenWidth, screenHeight;
    int srcWidth, srcHeight, patternWidth, patternHeight;
    Dimension screenSize;
    JButton patternButton, srcButton, updateButton;
    JToggleButton blockRotationButton;
    JTextField blockSizeText, overlapText, initialToleranceText, toleranceIncrementText, maxToleranceText, lumaBlendText;

    private double[][] costs;
    private TwoDLoc[][] path;
    boolean rotateBlocks = false;
    
    int blockSize = 20, overlapPercent = 25;
    double initialTolerance = 2;  // Initial tolerance factor 
    double toleranceIncrement = 0.2;  // Reduction factor for each iteration
    double maxTolerance = 6;
    float lumaBlend = 0.5f; //final brightness blend %

	
	public ImageQuilter() {
		//load images
		try {
			patternImage = ImageIO.read(new File("starrynight.jpg")); //fill in file values later
			srcImage = ImageIO.read(new File("statue.jpg"));

		} catch (Exception e) {
			System.out.println("Cannot load the provided image");
		}
		this.setTitle("Image Quilter");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//buttons
		patternButton = new JButton("Upload Pattern");
		srcButton = new JButton("Upload Source");
		updateButton = new JButton("Update Parameters");
        blockRotationButton = new JToggleButton("Rotation for Blocks: OFF");
        blockRotationButton.setBackground(Color.RED);

		blockSizeText = new JTextField(Integer.toString(blockSize));
		overlapText = new JTextField(Integer.toString(overlapPercent));
        initialToleranceText = new JTextField(Double.toString(initialTolerance));
        toleranceIncrementText = new JTextField(Double.toString(toleranceIncrement));
        maxToleranceText = new JTextField(Double.toString(maxTolerance));
        lumaBlendText = new JTextField(Float.toString(lumaBlend*100f));
		
		patternButton.addActionListener(this);
		srcButton.addActionListener(this);
		updateButton.addActionListener(this);
        blockRotationButton.addActionListener(e -> {
            if (blockRotationButton.isSelected()) {
                rotateBlocks = true;
                blockRotationButton.setText("Rotation for Blocks: ON");
                blockRotationButton.setBackground(Color.GREEN);
            } else {
                rotateBlocks = false;
                blockRotationButton.setText("Rotation for Blocks: OFF");
                blockRotationButton.setBackground(Color.RED);
            }
        });
		
		
		
		this.setLayout(null);
		
		this.add(patternButton);
		this.add(srcButton);
		this.add(updateButton);
        this.add(blockRotationButton);
		this.add(blockSizeText);
		this.add(overlapText);
        this.add(initialToleranceText);
        this.add(toleranceIncrementText);
        this.add(maxToleranceText);
        this.add(lumaBlendText);
		
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize);
	}
	
	public void actionPerformed(ActionEvent e) {
		blockSize = Integer.parseInt(blockSizeText.getText());
		overlapPercent = Integer.parseInt(overlapText.getText());
        initialTolerance = Double.parseDouble(initialToleranceText.getText());  // Initial tolerance factor 
        toleranceIncrement = Double.parseDouble(toleranceIncrementText.getText());  // Reduction factor for each iteration
        maxTolerance = Double.parseDouble(maxToleranceText.getText());
        lumaBlend = Float.parseFloat(lumaBlendText.getText()) / 100f; //brightness blend value

        if (overlapPercent < 0) overlapPercent = 0;
        else if(overlapPercent > 100) overlapPercent = 100;

        if (initialTolerance > maxTolerance) {
            initialTolerance = 2;
            initialToleranceText.setText("2");
            maxTolerance = 6;
            maxToleranceText.setText("6");
        }

        if (toleranceIncrement < initialTolerance || toleranceIncrement > maxTolerance) toleranceIncrement = 0.2;
		
		if(e.getSource() == patternButton) {
			File srcFile = uploadFile();
			if(srcFile != null) {
				try {
					patternImage = ImageIO.read(srcFile);
				} catch (Exception ex) {
					System.out.println("Cannot load the provided image");
				}
			}
		}
		if(e.getSource() == srcButton) {
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
	
	
	// Create an image with blocks sorted by brightness
	
	private BufferedImage createQuiltedImage() {
        int gridWidth = patternImage.getWidth() / blockSize;
        int gridHeight = patternImage.getHeight() / blockSize;
        imageBlocks = new BufferedImage[gridWidth][gridHeight]; // Initialize the array

        BufferedImage quiltedImage = new BufferedImage(patternImage.getWidth(), patternImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = quiltedImage.getGraphics();

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                int srcX = (int) (Math.random() * gridWidth) * blockSize;
                int srcY = (int) (Math.random() * gridHeight) * blockSize;

                // Extracting the block and storing it in the array
                imageBlocks[x][y] = patternImage.getSubimage(srcX, srcY, blockSize, blockSize);

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

	
	
	// Create an image with blocks randomly placed. Used as a baseline.
	
	private BufferedImage createRandomQuiltedImage() {
	    int gridWidth = patternImage.getWidth() / blockSize;
	    int gridHeight = patternImage.getHeight() / blockSize;
	    BufferedImage quiltedImage = new BufferedImage(patternImage.getWidth(), patternImage.getHeight(), BufferedImage.TYPE_INT_RGB);
	    Graphics g = quiltedImage.getGraphics();

	    for (int x = 0; x < gridWidth; x++) {
	        for (int y = 0; y < gridHeight; y++) {
	            int srcX = (int) (Math.random() * gridWidth) * blockSize;
	            int srcY = (int) (Math.random() * gridHeight) * blockSize;

	            // Extracting the block and storing it in the array
	            BufferedImage chosenBlock = patternImage.getSubimage(srcX, srcY, blockSize, blockSize);

	            g.drawImage(chosenBlock, x * blockSize, y * blockSize, null);
	        }
	    }

	    g.dispose();
	    return quiltedImage;
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
        BufferedImage newImage = new BufferedImage(patternImage.getWidth(), patternImage.getHeight(), BufferedImage.TYPE_INT_RGB);
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
    
    
    // Instead of sorting by brightness, use a source image and pick the block that closest matches the brightness
    
    private BufferedImage recreatesrcImage() {
        int gridWidth = srcImage.getWidth() / blockSize;
        int gridHeight = srcImage.getHeight() / blockSize;
        
        double brightnessRange = 3.0;
        double brightnessIncrement = 5.0;
        

        BufferedImage newImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = newImage.getGraphics();

        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                BufferedImage chosenBlock = null;
                double brightness = calculateAverageBrightness(srcImage.getSubimage(x * blockSize, y * blockSize, blockSize, blockSize));
                double currentThreshold = brightnessRange;

                while (chosenBlock == null) {
                    List<BufferedImage> suitableBlocks = new ArrayList<>();

                    for (BufferedImage[] row : imageBlocks) {
                        for (BufferedImage block : row) {
                            double blockBrightness = calculateAverageBrightness(block);
                            double diff = Math.abs(blockBrightness - brightness);

                            // Check if the difference is within the current brightness threshold
                            if (diff <= currentThreshold) {
                                suitableBlocks.add(block);
                            }
                        }
                    }

                    if (!suitableBlocks.isEmpty()) {
                        int randomIndex = (int) (Math.random() * suitableBlocks.size());
                        chosenBlock = suitableBlocks.get(randomIndex);
                    } else {
                        currentThreshold += brightnessIncrement;
                    }
                }

                g.drawImage(chosenBlock, x * blockSize, y * blockSize, null);
            }
        }

        g.dispose();
        return newImage;
    }

    
    // Adjust the previous quilted image using the source image's luma so it closer matches.
    
	private BufferedImage lumaCorrect(BufferedImage src, BufferedImage pattern, float blendPercent) {
		BufferedImage result = new BufferedImage(src.getWidth(),
				src.getHeight(), src.getType());

    	for(int i = 0; i < result.getWidth(); i++) {
    		for(int j = 0; j < result.getHeight(); j++) {
    			int sCol = src.getRGB(i, j);
    			int pCol = pattern.getRGB(i, j);
    			
    			float[] sHSB = Color.RGBtoHSB(Functions.getRed(sCol), Functions.getGreen(sCol), Functions.getBlue(sCol), null);
    			float[] pHSB = Color.RGBtoHSB(Functions.getRed(pCol), Functions.getGreen(pCol), Functions.getBlue(pCol), null);
    			
    			float finalBrightness = 255 * (blendPercent * (sHSB[2]/255) + (1-blendPercent) * (pHSB[2]/255));
    			
    			int fCol = Color.HSBtoRGB(pHSB[0], pHSB[1], finalBrightness);
    			result.setRGB(i, j, new Color(Functions.getRed(fCol), Functions.getGreen(fCol), Functions.getBlue(fCol)).getRGB());
    		}
    	}
		return result;
	}
    
	
	// Find overlaps 
    
    private BufferedImage getRightOverlap(BufferedImage block) {
        int overlapWidth = (int) (block.getWidth() * (overlapPercent/100.0));
        return block.getSubimage(block.getWidth() - overlapWidth, 0, overlapWidth, block.getHeight());
    }

    private BufferedImage getLeftOverlap(BufferedImage block) {
        int overlapWidth = (int) (block.getWidth() * (overlapPercent/100.0));
        return block.getSubimage(0, 0, overlapWidth, block.getHeight());
    }

    private BufferedImage getBottomOverlap(BufferedImage block) {
        int overlapHeight = (int) (block.getHeight() * (overlapPercent/100.0));
        return block.getSubimage(0, block.getHeight() - overlapHeight, block.getWidth(), overlapHeight);
    }

    private BufferedImage getTopOverlap(BufferedImage block) {
        int overlapHeight = (int) (block.getHeight() * (overlapPercent/100.0));
        return block.getSubimage(0, 0, block.getWidth(), overlapHeight);
    }
    
    // Create quilted image, using edges of blocks to compare overlap compatability

    private BufferedImage createQuiltedImage2() {
        int gridWidth = patternImage.getWidth() / blockSize;
        int gridHeight = patternImage.getHeight() / blockSize;
        BufferedImage quiltedImage = new BufferedImage(patternImage.getWidth(), patternImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = quiltedImage.getGraphics();
        
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
                            
                            int rotationCount = rotateBlocks ? 4 : 1;

                            for (int rotation = 0; rotation < rotationCount; rotation++) {
                                // Rotate block only if rotation is needed
                                if (rotateBlocks) {
                                    block = rotateClockwise(block);
                                }

                                BufferedImage leftNeighbor = (x > 0) ? selectedBlocks[y][x - 1] : null;
                                BufferedImage topNeighbor = (y > 0) ? selectedBlocks[y - 1][x] : null;

                                double combinedSSD = calculateCombinedSSD(block, leftNeighbor, topNeighbor);

                                if (combinedSSD < minSSD * tolerance) {
                                    if (combinedSSD < minSSD) {
                                        minSSD = combinedSSD;
                                        suitableBlocks.clear();
                                    }
                                    suitableBlocks.add(block);
                                }
                            }
                        }
                    }

                    if (!suitableBlocks.isEmpty()) {
                        int randomIndex = (int) (Math.random() * suitableBlocks.size());
                        chosenBlock = suitableBlocks.get(randomIndex);
                    } else {
                        tolerance += toleranceIncrement;
                        if (tolerance > maxTolerance) {
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

    
    // Calculate overlap
    private double calculateCombinedSSD(BufferedImage block, BufferedImage leftNeighbor, BufferedImage topNeighbor) {
        double ssdLeft = 0.0, ssdTop = 0.0;

        if (leftNeighbor != null) {
            BufferedImage leftOverlap = getRightOverlap(leftNeighbor);
            BufferedImage currentLeftOverlap = getLeftOverlap(block);
            ssdLeft = calculateOverlapSSD(leftOverlap, currentLeftOverlap, leftOverlap.getWidth());
        }

        if (topNeighbor != null) {
            BufferedImage topOverlap = getBottomOverlap(topNeighbor);
            BufferedImage currentTopOverlap = getTopOverlap(block);
            ssdTop = calculateOverlapSSD(topOverlap, currentTopOverlap, topOverlap.getHeight());
        }

        return ssdLeft + ssdTop;
    }
    
    
    
    // Rotate an image 90 degrees clockwise
    private BufferedImage rotateClockwise(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage rotated = new BufferedImage(height, width, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotated.setRGB(y, width - 1 - x, image.getRGB(x, y));
            }
        }

        return rotated;
    }


    // Find Sum of Squared Difference (basically image similarity) between two edges
    
    private double calculateOverlapSSD(BufferedImage block1, BufferedImage block2, int overlapWidth) {
        double ssd = 0.0;

        for (int x = 0; x < overlapWidth; x++) {
            for (int y = 0; y < block1.getHeight(); y++) {
                int rgb1 = block1.getRGB(x, y);
                int rgb2 = block2.getRGB(x, y);

                ssd += Math.pow(Functions.getRed(rgb1) - Functions.getRed(rgb2), 2);
                ssd += Math.pow(Functions.getGreen(rgb1) - Functions.getGreen(rgb2), 2);
                ssd += Math.pow(Functions.getBlue(rgb1) - Functions.getBlue(rgb2), 2);
            }
        }
        return ssd;
    }
    
    
	
	public void paint(Graphics g) {
		super.paint(g);
		
	    g.setColor(Color.BLACK);
	    Font f1 = new Font("Verdana", Font.PLAIN, 13); 
	    g.setFont(f1); 
		
		patternButton.setBounds(25, 25, 125, 25);
		patternButton.setBackground(Color.gray.brighter());
		
		srcButton.setBounds(175, 25, 125, 25);
		srcButton.setBackground(Color.gray.brighter());
		
		g.drawString("Block Size:", 350, 70);
		blockSizeText.setBounds(425, 25, 50, 25);
		
		g.drawString("Overlap %:", 500, 70);
		overlapText.setBounds(575, 25, 50, 25);

        g.drawString("Minimum Error Tolerance:", 35, 120);
		initialToleranceText.setBounds(210, 75, 50, 25);

        g.drawString("Error Tolerance Increment:", 285, 120);
		toleranceIncrementText.setBounds(460, 75, 50, 25);

        g.drawString("Maximum Error Tolerance:", 535, 120);
		maxToleranceText.setBounds(710, 75, 50, 25);
        
		g.drawString("Brightness Blend %:", 785, 120);
		lumaBlendText.setBounds(925, 75, 50, 25);

        blockRotationButton.setBounds(screenSize.width - 550, 25, 250, 25);
		
		updateButton.setBounds(screenSize.width - 250, 25, 200, 25);
		updateButton.setBackground(new Color(164,213,227));
		
		g.setColor(new Color(20,20,20));
		g.drawLine(25, 150, (int)screenSize.getWidth()-25, 150);
		
		//images
		int pw = patternImage.getWidth();
		int ph = patternImage.getHeight();
		
		int sw = srcImage.getWidth();
		int sh = srcImage.getHeight();
		
        g.drawString("Pattern", 25, 225); 
		g.drawImage(patternImage, 25, 240, pw, ph, this);

        g.drawString("Original Image", sw+75, 225); 
	    g.drawImage(srcImage, sw+75, 240, sw, sh, this);

        g.drawString("Quilted Image: Block Sorted By Brightness", sw+pw+125, 225); 
	    g.drawImage(createQuiltedImage(), sw+pw+125, 240, sw, sh, this); 
	    
        g.drawString("Quilted Image: Stitches Random Blocks ", 25, 625);
	    g.drawImage(createRandomQuiltedImage(), 25, 640, sw, sh, this);

        g.drawString("Quilted Image: Uses Steps From Research Paper", sw+75, 625);
	    g.drawImage(createQuiltedImage2(), sw + 75, 640, sw, sh, this); 

        g.drawString("Quilted Image: Recreates Original Image By Comparing Brightness ", sw+pw+125, 625);
	    g.drawImage(lumaCorrect(srcImage, recreatesrcImage(), lumaBlend), sw+pw+125, 640, sw, sh, this);

	     
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImageQuilter iq = new ImageQuilter();
		iq.setVisible(true);
		iq.repaint();
	}
	
	
	
	

    
    // Start step 3
    // Code doesn't work but most of the implementation is accurate.
    
    
    
    private BufferedImage stitchBlocks(BufferedImage block1, BufferedImage block2, boolean horizontal) {
        // Calculate the costs for the overlap region
        double[][] costs = calculateCostsForOverlap(block1, block2, horizontal);

        // Find the minimum path in the overlap region
        findMinPath(costs, horizontal); // Using integrated pathfinding
        TwoDLoc bestSource = findBestSourceLoc(); // Start from the best source location

        // The dimensions of the stitched image depend on how you want to combine the images
        BufferedImage stitched = new BufferedImage(
            block1.getWidth(), // Assuming stitching within the block width
            block1.getHeight(), // Assuming stitching within the block height
            BufferedImage.TYPE_INT_RGB);

        Graphics g = stitched.getGraphics();

        // Draw the first block entirely
        g.drawImage(block1, 0, 0, null);

        // Apply the stitching logic
        TwoDLoc currentLoc = bestSource;
        while (currentLoc != null) {
            int x = currentLoc.getCol();
            int y = currentLoc.getRow();

            // Use the pixel from block2 on the path
            Color color = new Color(block2.getRGB(x, y));
            g.setColor(color);
            g.fillRect(x, y, 1, 1); // Drawing one pixel at a time

            currentLoc = followPath(currentLoc); // Follow the path
        }

        g.dispose();
        return stitched;
    }

    
    
    private void findMinPath(double[][] dists, boolean allowHorizontal) {
        int rows = dists.length;
        int cols = dists[0].length;
        path = new TwoDLoc[rows][cols];
        costs = new double[rows][cols];

        // Initialize costs and path arrays
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                costs[r][c] = dists[r][c];
                path[r][c] = (r == 0) ? null : new TwoDLoc(r - 1, c);
            }
        }

        // Calculate paths based on minimum costs
        for (int r = 1; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c > 0 && costs[r][c - 1] < costs[r][path[r][c].getCol()]) {
                    path[r][c] = new TwoDLoc(r - 1, c - 1);
                }
                if (c < cols - 1 && costs[r][c + 1] < costs[r][path[r][c].getCol()]) {
                    path[r][c] = new TwoDLoc(r - 1, c + 1);
                }
            }
        }

        // Handle horizontal movement if allowed
        if (allowHorizontal) {
            handleHorizontalMovement(dists);
        }
    }

    

    private TwoDLoc followPath(TwoDLoc currentLoc) {
        return path[currentLoc.getRow()][currentLoc.getCol()];
    }

    private TwoDLoc findBestSourceLoc() {
        int lastRow = costs.length - 1;
        TwoDLoc bestLoc = new TwoDLoc(lastRow, 0);
        for (int i = 1; i < costs[0].length; i++) {
            if (costs[lastRow][i] < costs[lastRow][bestLoc.getCol()]) {
                bestLoc = new TwoDLoc(lastRow, i);
            }
        }
        return bestLoc;
    }
    
    
    
    
    private static class TwoDLoc {
        private int row;
        private int col;

        public TwoDLoc(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }
    }
    
    // 
    
    private void handleHorizontalMovement(double[][] dists) {
        int rows = dists.length;
        int cols = dists[0].length;

        for (int r = 1; r < rows; r++) {
            // Update paths for leftward movement
            for (int c = 1; c < cols; c++) {
                double costIfMovedLeft = costs[r][c - 1] + dists[r][c];
                if (costIfMovedLeft < costs[r][c]) {
                    costs[r][c] = costIfMovedLeft;
                    path[r][c] = new TwoDLoc(r, c - 1);
                }
            }

            // Update paths for rightward movement
            for (int c = cols - 2; c >= 0; c--) {
                double costIfMovedRight = costs[r][c + 1] + dists[r][c];
                if (costIfMovedRight < costs[r][c]) {
                    costs[r][c] = costIfMovedRight;
                    path[r][c] = new TwoDLoc(r, c + 1);
                }
            }
        }
    }


    // 
    
    private double[][] calculateCostsForOverlap(BufferedImage block1, BufferedImage block2, boolean horizontal) {
        int overlapSize = horizontal ? block1.getHeight() : block1.getWidth();
        int width = horizontal ? block1.getWidth() : overlapSize;
        int height = horizontal ? overlapSize : block1.getHeight();

        double[][] costs = new double[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color1 = new Color(block1.getRGB(x, y));
                Color color2 = new Color(block2.getRGB(x, y));
                costs[y][x] = calculateColorDifference(color1, color2);
            }
        }

        return costs;
    }

    private double calculateColorDifference(Color c1, Color c2) {
        return Math.pow(c1.getRed() - c2.getRed(), 2) +
               Math.pow(c1.getGreen() - c2.getGreen(), 2) +
               Math.pow(c1.getBlue() - c2.getBlue(), 2);
    }
    
    
    
    // End Step 3


    
	
	public void paint(Graphics g) {
		super.paint(g);
		
	    g.setColor(Color.BLACK);
	    Font f1 = new Font("Verdana", Font.PLAIN, 13); 
	    g.setFont(f1); 
		
		patternButton.setBounds(25, 25, 125, 25);
		patternButton.setBackground(Color.gray.brighter());
		
		srcButton.setBounds(175, 25, 125, 25);
		srcButton.setBackground(Color.gray.brighter());
		
		g.drawString("Block Size:", 350, 70);
		blockSizeText.setBounds(425, 25, 50, 25);
		
		g.drawString("Overlap %:", 500, 70);
		overlapText.setBounds(575, 25, 50, 25);

        g.drawString("Minimum Error Tolerance:", 35, 120);
		initialToleranceText.setBounds(210, 75, 50, 25);

        g.drawString("Error Tolerance Increment:", 285, 120);
		toleranceIncrementText.setBounds(460, 75, 50, 25);

        g.drawString("Maximum Error Tolerance:", 535, 120);
		maxToleranceText.setBounds(710, 75, 50, 25);
        
		g.drawString("Brightness Blend %:", 785, 120);
		lumaBlendText.setBounds(925, 75, 50, 25);

        blockRotationButton.setBounds(screenSize.width - 550, 25, 250, 25);
		
		updateButton.setBounds(screenSize.width - 250, 25, 200, 25);
		updateButton.setBackground(new Color(164,213,227));
		
		g.setColor(new Color(20,20,20));
		g.drawLine(25, 150, (int)screenSize.getWidth()-25, 150);
		
		//images
		int pw = patternImage.getWidth();
		int ph = patternImage.getHeight();
		
		int sw = srcImage.getWidth();
		int sh = srcImage.getHeight();
		
        g.drawString("Pattern", 25, 225); 
		g.drawImage(patternImage, 25, 240, pw, ph, this);

        g.drawString("Original Image", sw+125, 225); 
	    g.drawImage(srcImage, sw+125, 240, sw, sh, this);

        g.drawString("Quilted Image: Block Sorted By Brightness", sw+pw+175, 225); 
	    g.drawImage(createQuiltedImage(), sw+pw+175, 240, pw, ph, this); 
	    
        g.drawString("Quilted Image: Stitches Random Blocks ", 25, 625);
	    g.drawImage(createRandomQuiltedImage(), 25, 640, pw, ph, this);

        g.drawString("Quilted Image: Uses Steps From Research Paper", sw+125, 625);
	    g.drawImage(createQuiltedImage2(), sw + 125, 640, pw, ph, this); 

        g.drawString("Quilted Image: Recreates Original Image By Comparing Brightness ", sw+pw+175, 625);
	    g.drawImage(lumaCorrect(srcImage, recreatesrcImage(), lumaBlend), sw+pw+175, 640, sw, sh, this);

	     
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImageQuilter iq = new ImageQuilter();
		iq.setVisible(true);
		iq.repaint();
	}

}
