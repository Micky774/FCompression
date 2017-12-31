import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.Timer;

public class IFSDecoder {

	private static BufferedImage[] canvas = new BufferedImage[2];
	private static final int HEIGHT = 1200;
	private static final int WIDTH = 1200;
	private static final int rangeBlockSize = 8;
	private static final int rangeBlockCount = HEIGHT * WIDTH / (rangeBlockSize * rangeBlockSize);
	private static int delay = 0;
	private static int iterativeCount = 0;
	private static int iterativeMax = 100;
	private static int tag = 0;
	private static Random rand = new Random();
	private static CMap[] functionList = new CMap[rangeBlockCount];
	Timer timer = new Timer(0, event -> IFSDecoder.run());
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

	private static void domainInitialize() {
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				byte k = (byte) (rand.nextDouble() * 255);
				int g = k;
				g = (g << 8) + k;
				g = (g << 8) + k;
				canvas[0].setRGB(i, j, g);
			}
		}

	}

	private static void domainMap() {
		for (int r = 0; r < HEIGHT / rangeBlockSize; r++) {
			for (int s = 0; s < WIDTH / rangeBlockSize; s++) {
				functionList[r * WIDTH / rangeBlockSize + s].map(canvas[tag], canvas[(tag + 1) % 2], s * rangeBlockSize,
						r * rangeBlockSize, rangeBlockSize);
			}

		}
		tag = (tag + 1) % 2;
	}

	private static void updateScreen() {
		domainMap();
	}

	private static void run() {
		if (iterativeCount < iterativeMax) {
			updateScreen();
			WINDOW.repaint();
			iterativeCount++;
		}

	}

	public IFSDecoder() {
		canvas[0] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		canvas[1] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		WINDOW.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WINDOW.setSize(WIDTH, HEIGHT);
		domainInitialize();
		WINDOW.repaint();
		WINDOW.setVisible(true);
		timer.setInitialDelay(delay);
		timer.setDelay(delay);
		timer.start();

	}

	public IFSDecoder(File f) {

	}

	public static void main(String[] args) {
		new IFSDecoder();
	}

}
