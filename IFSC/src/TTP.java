import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class TTP {
	public static BufferedImage read(final String inputFile) {
		BufferedImage outputImage = null;
		try {
			InputStream inputStream = new FileInputStream(inputFile);
			byte[] temp = new byte[4];
			inputStream.read(temp);
			PTT.printBA(temp);
			final int HEIGHT = TTP.byteToInt(temp);
			System.out.println(HEIGHT);
			inputStream.read(temp);
			final int WIDTH = TTP.byteToInt(temp);
			BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			int rgb = 0;
			byte[] buffer = new byte[3 * WIDTH];
			for (int i = 0; i < HEIGHT; i++) {
				inputStream.read(buffer);
				for (int j = 0; j < WIDTH; j++) {
					rgb = TTP.byteToInt(Arrays.copyOfRange(buffer, j * 3, j * 3 + 3));
					image.setRGB(j, i, rgb);
				}
			}
			outputImage = image;
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputImage;

	}

	public static void writeText(final String inputFile) {

		try {
			InputStream inputStream = new FileInputStream(inputFile);
			byte[] buffer = new byte[1024];
			inputStream.read(buffer);
			PTT.printBA(buffer);
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void save(final String outputFile, final BufferedImage inputImage) {
		File outputfile = new File(outputFile);
		try {
			ImageIO.write(inputImage, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int byteToInt(byte[] ar) {
		byte[] array = new byte[4];
		for (int i = 0; i < 4; i++) {
			if (i < 4 - ar.length) {
				array[i] = 0;
			} else {
				array[i] = ar[i - 4 + ar.length];
			}
		}
		ByteBuffer bb = ByteBuffer.wrap(array);
		int result = bb.getInt(0);

		return result;
	}

	public static void main(String[] args) {
		TTP.save("DiceTest.png", TTP.read("DiceTest"));
		// TTP.writeText("TestOutput");
		System.out.println("Success");
	}

}
