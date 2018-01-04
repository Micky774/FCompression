import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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
	private int HRcount;
	private static Random rand = new Random();
	private CMap[] functionList;
	Timer timer = new Timer(0, event -> this.run());
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
		for (int r = 0; r < HRcount; r++) {
			for (int s = 0; s < WRcount; s++) {
				int fn = r * WRcount + s;
				functionList[fn].map(canvas[tag], canvas[(tag + 1) % 2]);
				//
				// int dx=functionList[fn].dx; int dy=functionList[fn].dy;
				// int[][]
				// F=CMap.imageToArray(canvas[tag], dx, dy,
				// dx+2*rangeBlockSize,dy+2*rangeBlockSize); int[][]
				// R=CMap.imageToArray(canvas[(tag + 1) % 2], s *
				// rangeBlockSize, r *
				// rangeBlockSize, s * rangeBlockSize+rangeBlockSize, r *
				// rangeBlockSize+rangeBlockSize);
			}

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

	public IFSDecoder(String inputFile, int size) {
		File f = new File(inputFile);

		try {
			InputStream inputStream = new FileInputStream(f);
			byte[] buffer = new byte[4];
			ByteBuffer bb = ByteBuffer.wrap(buffer);
			inputStream.read(buffer);
			HEIGHT = bb.getInt();
			inputStream.read(buffer);
			bb.clear();
			WIDTH = bb.getInt();
			rangeBlockSize = size;
			rangeBlockCount = HEIGHT * WIDTH / (rangeBlockSize * rangeBlockSize);
			canvas[0] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			canvas[1] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			WRcount = WIDTH / rangeBlockSize;
			HRcount = HEIGHT / rangeBlockSize;
			CMap[] functionList = new CMap[rangeBlockCount];
			byte[] mapBuffer = new byte[5];
			int i = 0;
			for (CMap map : functionList) {
				inputStream.read(mapBuffer);
				map = new CMap(mapBuffer, size, canvas[tag]);
				int ry = i / WRcount;
				int rx = i % WRcount;
				map.setRange(rx, ry);
				i++;
			}

			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		WINDOW.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WINDOW.setSize(WIDTH, HEIGHT);
		domainInitialize();
		WINDOW.repaint();
		WINDOW.setVisible(true);
		timer.setInitialDelay(delay);
		timer.setDelay(delay);
		timer.start();
	}

	public static void main(String[] args) {
		// new IFSDecoder("TestCodeBook", 32);
	}

}