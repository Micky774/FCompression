import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.Timer;

public class ChaosGame {
	private static BufferedImage canvas = null;
	private static final int HEIGHT = 1200;
	private static final int WIDTH = 1200;
	private static int[][] screen;
	private static int delay = 0;
	private static int maxPop = 10;
	private static int[][] operating = new int[maxPop][2];
	private static Random rand = new Random();
	private static int iterativeCount = 0;
	private static int iterativeMax = Integer.MAX_VALUE;
	static JFrame WINDOW = new JFrame() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(java.awt.Graphics g) {
			g.drawImage(canvas, 0, 0, null);
		}

	};
	Timer timer = new Timer(0, event -> ChaosGame.run());

	public static void initialize(double frequency) {
		int k = 0;
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				double random = rand.nextDouble();
				if ((random <= maxPop / ((double) (WIDTH * HEIGHT))) && (k < maxPop)) {
					screen[i][j] = 1;
					operating[k][0] = i;
					operating[k][1] = j;
					k++;
				}
			}
		}
	}

	public static void updateScreen() {
		for (int i = 0; i < maxPop; i++) {
			int temp = (int) (Math.random() * 3);
			int r = operating[i][0] / 2;
			int s = operating[i][1] / 2;
			if (temp == 0) {
				operating[i][0] = r;
				operating[i][1] = s;
				screen[r][s] = 1;
			} else if (temp == 1) {
				operating[i][0] = r + WIDTH / 2;
				operating[i][1] = s;
				screen[operating[i][0]][operating[i][1]] = 1;
			} else {
				operating[i][0] = r + WIDTH / 2;
				operating[i][1] = s + HEIGHT / 2;
				screen[operating[i][0]][operating[i][1]] = 1;
			}
		}
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				if (screen[i][j] == 1)
					canvas.setRGB(i, j, Color.MAGENTA.getRGB());
			}
		}
	}

	private static void run() {
		if (iterativeCount < iterativeMax) {
			updateScreen();
			WINDOW.repaint();
		}

	}

	public ChaosGame(final double freq) {
		canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		screen = new int[WIDTH + 1][HEIGHT + 1];
		WINDOW.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WINDOW.setSize(WIDTH, HEIGHT);
		initialize(freq);
		updateScreen();
		WINDOW.repaint();
		WINDOW.setVisible(true);
		timer.setInitialDelay(delay);
		timer.setDelay(delay);
		timer.start();
	}

	public static void main(String... args) {
		new ChaosGame(.5);
	}

}