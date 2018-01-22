import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.Timer;

public class IFSDecoder {

	private static BufferedImage[] canvas = new BufferedImage[2];
	private int WIDTH;
	private int HEIGHT;
	private int rangeBlockSize;
	private int rangeBlockCount;
	private int delay = 0;
	private int iterativeCount = 0;
	private int iterativeMax = 100;
	private static int tag = 0;
	private int WRcount;
	private static Random rand = new Random();
	private CMap[] functionList;
	Timer timer = null;
	static JFrame WINDOW1 = new JFrame() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(java.awt.Graphics g) {
			g.drawImage(canvas[tag], 0, 0, null);
		}

	};
	static JFrame WINDOW2 = new JFrame() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(java.awt.Graphics g) {
			g.drawImage(canvas[(tag + 1) % 2], 0, 0, null);
		}

	};

	public static void printBinaries(String inputFile, int until) {
		while (until != 0) {
			until--;
		}

	}

	public int getHeight() {
		return this.HEIGHT;
	}

	public int getWidth() {
		return this.WIDTH;
	}

	private void domainInitialize(int mode) {
		if (mode == 0) {
			for (int i = 0; i < HEIGHT; i++) {
				for (int j = 0; j < WIDTH; j++) {
					if (rand.nextDouble() > .95) {
						byte k = (byte) (rand.nextDouble() * 255);
						int g = k;
						g = (g << 8) + k;
						g = (g << 8) + k;
						canvas[0].setRGB(j, i, g);
					}
				}
			}
		} else if (mode == 1) {
			int g = 255;
			g = (g << 8) + 255;
			g = (g << 8) + 255;
			g = (g << 8) + 255;
			for (int i = 0; i < HEIGHT; i++) {
				for (int j = 0; j < WIDTH; j++) {
					canvas[0].setRGB(j, i, g);

				}
			}
		} else if (mode == 2) {
			int g = 0xFF;
			g <<= 24;
			for (int i = 0; i < HEIGHT; i++) {
				for (int j = 0; j < WIDTH; j++) {
					canvas[0].setRGB(j, i, g);
				}
			}

		}
	}

	private void domainMap() {
		for (CMap cmap : functionList) {
			cmap.map(canvas[tag], canvas[(tag + 1) % 2]);
		}
		tag = (tag + 1) % 2;
	}

	private void run() {
		if (iterativeCount < iterativeMax) {
			domainMap();
			WINDOW1.repaint();
			WINDOW2.repaint();

			iterativeCount++;
		}

	}

	public IFSDecoder(String inputFile, final int _HEIGHT, final int _WIDTH, int size) {
		File f = new File(inputFile);
		HEIGHT = _HEIGHT;
		WIDTH = _WIDTH;

		try {
			InputStream inputStream = new FileInputStream(f);
			rangeBlockSize = size;
			rangeBlockCount = HEIGHT * WIDTH / (rangeBlockSize * rangeBlockSize);
			canvas[0] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			canvas[1] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			WRcount = WIDTH / rangeBlockSize;
			functionList = new CMap[rangeBlockCount];
			byte[] mapBuffer = new byte[5];

			for (int i = 0; i < functionList.length; i++) {
				inputStream.read(mapBuffer);
				functionList[i] = new CMap(mapBuffer, size, canvas[tag]);
				int ry = (i / WRcount) * size;
				int rx = (i % WRcount) * size;
				functionList[i].setRange(rx, ry);
			}
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		WINDOW1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WINDOW1.setSize(WIDTH, HEIGHT);
		WINDOW2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WINDOW2.setSize(WIDTH, HEIGHT);

		domainInitialize(2);
		timer = new Timer(0, event -> this.run());
		WINDOW1.repaint();
		WINDOW1.setVisible(true);
		WINDOW2.repaint();
		WINDOW2.setVisible(true);

		timer.setInitialDelay(delay);
		timer.setDelay(delay);
		timer.start();
	}

	public static void main(String[] args) {
		new IFSDecoder("TestCodeBook2", 600, 800, 1);
	}

}