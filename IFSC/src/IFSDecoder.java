import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.Timer;

public class IFSDecoder {

	private static BufferedImage[] canvas = new BufferedImage[2];
	private int WIDTH;
	private int HEIGHT;
	private int rangeBlockSize = 8;
	private int rangeBlockCount;
	private int delay = 0;
	private int iterativeCount = 0;
	private int iterativeMax = 100;
	private int tag = 0;
	private int WRcount = WIDTH / rangeBlockSize;
	private int HRcount = HEIGHT / rangeBlockSize;
	private static Random rand = new Random();
	private CMap[] functionList = new CMap[rangeBlockCount];
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
				functionList[r * WRcount + s].map(canvas[tag], canvas[(tag + 1) % 2], s * rangeBlockSize,
						r * rangeBlockSize, rangeBlockSize);
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

	public IFSDecoder(int _WIDTH, int _HEIGHT) {
		HEIGHT = _HEIGHT;
		WIDTH = _WIDTH;
		rangeBlockCount = HEIGHT * WIDTH / (rangeBlockSize * rangeBlockSize);
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
		new IFSDecoder(1200, 1200);
	}

}