import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;

public class PTT {

	@SuppressWarnings("static-access")
	public static void write(final String inputFile, final String outputFile) {
		BufferedImage image;
		try {
			// Create streams/initiate files
			File imageFile = new File(inputFile);
			image = ImageIO.read(imageFile);
			OutputStream outputStream = new FileOutputStream(outputFile);
			int HEIGHT = image.getHeight();
			int WIDTH = image.getWidth();
			System.out.println(WIDTH);
			System.out.println(HEIGHT);
			byte[] HW = new byte[8];
			// ByteBuffer buffer = ByteBuffer.allocate(8);
			ByteBuffer buffer = ByteBuffer.wrap(HW);
			buffer.order(ByteOrder.BIG_ENDIAN);
			buffer.putInt(HEIGHT);
			buffer.putInt(WIDTH);
			buffer.flip();
			// System.out.println(new String(HW));
			outputStream.write(buffer.array());
			// PTT.printBA(buffer.array());
			PTT.printBA(HW);

			// create a byte array to load RGB int and wrap with ByteBuffer
			byte[] rgb = new byte[4];
			ByteBuffer bb = ByteBuffer.wrap(rgb);
			bb.order(ByteOrder.BIG_ENDIAN);
			// iterate thru image assigning RBB ints->byte[4]->Stream(0-2)
			for (int i = 0; i < HEIGHT; i++) {
				for (int j = 0; j < WIDTH; j++) {
					int irgb = image.getRGB(j, i);
					rgb = bb.allocate(4).putInt(irgb).array();
					outputStream.write(rgb, 1, 3);
					// PTT.printBA(rgb);
				}
			}
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	public static void write(BufferedImage img, final String outputFile) {
		BufferedImage image = img;
		try {
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			int HEIGHT = image.getHeight();
			int WIDTH = image.getWidth();
			ByteBuffer buffer = ByteBuffer.allocate(8);
			buffer.order(ByteOrder.BIG_ENDIAN);
			buffer.putInt(HEIGHT);
			buffer.putInt(WIDTH);
			buffer.flip();
			// System.out.println(new String(HW));
			outputStream.write(buffer.array());
			System.out.println(new String(buffer.array()));
			// create a byte array to load RGB int and wrap with ByteBuffer
			byte[] rgb = new byte[4];
			ByteBuffer bb = ByteBuffer.wrap(rgb);
			bb.order(ByteOrder.BIG_ENDIAN);
			// iterate thru image assigning RBB ints->byte[4]->Stream(0-2)
			for (int i = 0; i < HEIGHT; i++) {
				for (int j = 0; j < WIDTH; j++) {
					int irgb = image.getRGB(i, j);
					bb.putInt(irgb);
					outputStream.write(rgb, 1, 3);
				}
			}
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Transcribes a given int value to a byte array
	 * 
	 * @param Int
	 *            The given integer needed to be transcribed
	 * @param size
	 *            How many bytes of the integer to be transcribed. 0<=size<=4
	 * @param LTR
	 *            Boolean determining transcription order: 1=Left to Right, 0=Right
	 *            to Left
	 * @return A byte[] array containing the individual bytes of the given integer
	 *         in the given order.
	 */
	public static byte[] transcribe(int Int, int size, boolean LTR) {
		byte[] result = new byte[size];
		int i = 0;
		if (!LTR) {
			while (i != size) {
				result[i] = (byte) (Int & 0xFF);
				Int >>= 8;
				i++;
			}
		} else {
			while (i != size) {
				result[size - i - 1] = (byte) (Int & 0xFF);
				Int >>= 8;
				i++;
			}
		}
		return result;

	}

	public static void printBA(byte[] ba) {
		for (byte b : ba) {

			System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
		}
	}

	public static void saveAsGray(String inputFile, String outputFile) {
		File imageFile = new File(inputFile);
		try {
			BufferedImage image = ImageIO.read(imageFile);
			for (int i = 0; i < image.getHeight(); i++) {
				for (int j = 0; j < image.getWidth(); j++) {
					int rgb = image.getRGB(j, i);
					int r = (rgb >> 16) & 0xFF;
					int g = (rgb >> 8) & 0xFF;
					int b = (rgb & 0xFF);
					int avg = ((r + g + b) / 3) & 0xFF;
					int k = 0xFF;
					k <<= 8;
					k += avg;
					k <<= 8;
					k += avg;
					k <<= 8;
					k += avg;
					// System.out.println(k);
					image.setRGB(j, i, k);
				}
			}
			File of = new File(outputFile);
			ImageIO.write(image, "png", of);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// PTT.printBA(PTT.transcribe(17, 1, false));

		// PTT.write("Dice.png", "DiceTest");
		PTT.saveAsGray("TestTown.jpg", "TestTownG.png");

		System.out.println("Success!");

	}

}
