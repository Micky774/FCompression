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
	private int tag = 0;
	private int WRcount;
	private static Random rand = new Random();
	private CMap[] functionList;
	Timer timer = null;
	static JFrame WINDOW = new JFrame() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(java.awt.Graphics g) {
			g.drawImage(canvas[0], 0, 0, null);
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

	private void domainInitialize() {
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				byte k = (byte) (rand.nextDouble() * 255);
				int g = k;
				g = (g << 8) + k;
				g = (g << 8) + k;
				canvas[0].setRGB(j, i, g);
			}
		}

	}

	private void domainMap() {
		for (CMap cmap : functionList) {
			cmap.map(canvas[tag], canvas[(tag + 1) % 2]);
		}
		tag = (tag + 1) % 2;
	}

	private void updateScreen() {
		domainMap();
	}

	private void run() {
		if (iterativeCount < iterativeMax) {
			updateScreen();
			WINDOW.repaint();
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
		WINDOW.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WINDOW.setSize(WIDTH, HEIGHT);
		domainInitialize();
		timer = new Timer(0, event -> this.run());
		WINDOW.repaint();
		WINDOW.setVisible(true);
		timer.setInitialDelay(delay);
		timer.setDelay(delay);
		timer.start();
	}

	public static void main(String[] args) {
		new IFSDecoder("TestCodeBook1", 640, 896, 128);
	}

}