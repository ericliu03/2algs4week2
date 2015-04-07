import java.awt.Color;


public class SeamCarver {
	private int w;
	private int h;
	private int[][] colorMap;
	// create a seam carver object based on the given picture
	public SeamCarver(Picture picture) { 
//		this.picture = new Picture(picture);
		this.w = picture.width();
		this.h = picture.height();
		colorMap = new int[w][h];
		for (int x = 0; x < w; x ++) {
			for (int y = 0; y < h; y ++) {
				colorMap[x][y] = picture.get(x, y).getRGB();
			}
		}
	}        
	// current picture
	public Picture picture() {
		Picture picture = new Picture(w, h);
		for (int x = 0; x < w; x ++) {
			for (int y = 0; y < h; y ++) {
				picture.set(x, y, new Color(colorMap[x][y]));
			}
		}
		return picture;
	}                    
	// width of current picture
	public int width() {
		return this.w;
	}                            
	// height of current picture
	public int height(){
		return this.h;
	}      
	private double energy(int x, int y, boolean isX) {
		Color leftUpper;
		Color rightLower;
		if (x == 0 || y == 0 || x == width() - 1 || y == height() - 1) {
			return 195075.0/2;
		}
		if (isX) {
			leftUpper = new Color(this.colorMap[x - 1][y]);
			rightLower = new Color(this.colorMap[x + 1][y]);
		} else {
			leftUpper = new Color(this.colorMap[x][y - 1]);
			rightLower = new Color(this.colorMap[x][y + 1]);
		}
		double result = Math.pow(leftUpper.getGreen() - rightLower.getGreen(), 2) + 
						Math.pow(leftUpper.getRed() - rightLower.getRed(), 2) +
						Math.pow(leftUpper.getBlue() - rightLower.getBlue(), 2);
		return result;
	}

	// energy of pixel at column x and row y
	public  double energy(int x, int y) {
		if (x < 0 || x > w - 1 || y < 0 || y > h - 1) throw new IndexOutOfBoundsException();
		return energy(x, y, false) + energy(x, y, true);
	}               
	// sequence of indices for horizontal seam
	public int[] findHorizontalSeam() {
		double[][] energyMat = new double[h][w];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y ++) {
				energyMat[y][x] = energy(x, y);
			}
		}
		return findVerticalSeam(h, w, energyMat);
	}          
	public   int[] findVerticalSeam() { 
		double[][] energyMat = new double[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y ++) {
				energyMat[x][y] = energy(x, y);
			}
		}
		return findVerticalSeam(w, h, energyMat);
	}
	
    // sequence of indices for vertical seam
	private   int[] findVerticalSeam(int w, int h, double[][] energyMat) { 
		double[][] energyAccumulateMat = new double[w][h];
		int[][] indexMat = new int[w][h];

		// initial the first line
		for (int x = 0; x < w; x ++) {
			energyAccumulateMat[x][0] = energyMat[x][0];
		}
		for (int y = 1; y < h; y++) {
			for (int x = 0; x < w; x ++) {
				double max = Double.POSITIVE_INFINITY;
				for (int k = x - 1; k <= x + 1; k ++) {
					if (k < 0 || k > w - 1) {
						continue;
					} else {
						if (energyAccumulateMat[k][y-1] < max) {
							max = energyAccumulateMat[k][y-1];
							indexMat[x][y] = k;
						}
					}
				}
				//this position's accumulate energy is this position's energy plus last line's minimal accuEnergy
				energyAccumulateMat[x][y] =  energyMat[x][y] + energyAccumulateMat[indexMat[x][y]][y-1];
			}
		}
		// find the index of minimal accumulated energy
		int minIndex = 0;
		double min = Double.POSITIVE_INFINITY;
		for (int x = 0; x < w; x ++) {
			if (min > energyAccumulateMat[x][h-1]) {
				min = energyAccumulateMat[x][h-1];
				minIndex = x;
			}
		}
		// back trace
		int[] result = new int[h];
		result[h-1] = minIndex;
		for (int y = h-2; y >= 0; y --) {
			result[y] = indexMat[result[y+1]][y+1];
		}
		return result;
	}      
	// remove horizontal seam from current picture
	public    void removeHorizontalSeam(int[] seam) {
		if (w <= 1 || h <= 1 || seam.length != w) throw new IllegalArgumentException();
		int[][] newColorMap = new int[w][h-1];
		for (int x = 0; x < w; x ++) {
			int passSeam = 0;
			if (x > 0 && Math.abs(seam[x-1] - seam[x]) > 1) throw new IllegalArgumentException();
			for (int y = 0; y < h-1; y ++) {
				if (y == seam[x]) passSeam = 1;
				newColorMap[x][y] = this.colorMap[x][y+passSeam];
			}
		}
		this.h = this.h - 1;
		this.colorMap = newColorMap;
	} 
	// remove vertical seam from current picture
	public    void removeVerticalSeam(int[] seam)  {
		if (w <= 1 || h <= 1 || seam.length != h) throw new IllegalArgumentException();
		int[][] newColorMap = new int[w-1][h];
		for (int y = 0; y < h; y ++) {
			int passSeam = 0;
			if (y > 0 && Math.abs(seam[y-1] - seam[y]) > 1) throw new IllegalArgumentException();
			for (int x = 0; x < w-1; x ++) {
				if (x == seam[y]) passSeam = 1;
				newColorMap[x][y] = this.colorMap[x+passSeam][y];
			}
		}
		this.w = this.w - 1;
		this.colorMap = newColorMap;
	}   
}