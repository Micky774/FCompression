import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class IFSEncoder {

	public static void EncodeWithThreads(String inputFile, String outputFile, int size) throws IOException {
		File imageFile = new File(inputFile);
		BufferedImage before = ImageIO.read(imageFile);
		BufferedImage image;
		int w = before.getWidth();
		int h = before.getHeight();
		final int MAX_THREAD = 10;

		if ((w % size == 0) && (h % size == 0)) {
			image = before;
		} else {
			AffineTransform at = new AffineTransform();
			double nx = (Math.ceil(((double) w) / size) * size);
			double ny = (Math.ceil(((double) h) / size) * size);
			double sx = nx / w;
			double sy = ny / h;
			at.scale(sx, sy);
			image = new BufferedImage((int) nx, (int) ny, BufferedImage.TYPE_INT_ARGB);
			System.out.println(sx + ":" + sy);
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			scaleOp.filter(before, image);
		}

		System.out.println(image.getHeight() + ":" + image.getWidth());
		OutputStream outputStream = new FileOutputStream(outputFile);
		int rangeBlockCount = image.getHeight() * image.getWidth() / (size * size);
		int numThreads = Math.min(MAX_THREAD, rangeBlockCount);
		EncodeThread[] threadArray = new EncodeThread[numThreads];
		for (int i = 0; i < threadArray.length; i++)
		{
			threadArray[i] = new EncodeThread(i, size, rangeBlockCount, image, outputStream, threadArray);
			threadArray[i].start();
		}
		while (threadArray[numThreads - 1].isAlive())
		{
			try
			{
				Thread.sleep(10000);
			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}
		outputStream.close();
	}

	public static void Encode(String inputFile, String outputFile, int size, double threshold) throws IOException {
		File imageFile = new File(inputFile);
		BufferedImage before = ImageIO.read(imageFile);
		BufferedImage image;
		int w = before.getWidth();
		int h = before.getHeight();

		if ((w % size == 0) && (h % size == 0)) {
			image = before;
		} else {
			AffineTransform at = new AffineTransform();
			double nx = (Math.ceil(((double) w) / size) * size);
			double ny = (Math.ceil(((double) h) / size) * size);
			double sx = nx / w;
			double sy = ny / h;
			at.scale(sx, sy);
			image = new BufferedImage((int) nx, (int) ny, BufferedImage.TYPE_INT_ARGB);
			System.out.println(sx + ":" + sy);
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			scaleOp.filter(before, image);
		}

		System.out.println(image.getHeight() + ":" + image.getWidth());
		OutputStream outputStream = new FileOutputStream(outputFile);
		int rangeBlockCount = image.getHeight() * image.getWidth() / (size * size);
		double[] temp = new double[7];
		byte[] code = new byte[5];
		int t = 1;
		for (int i = 0; i < image.getHeight() / size; i++) {
			for (int j = 0; j < image.getWidth() / size; j++) {
				temp = IFSEncoder.selectBestDomain(image, j * size, i * size, size, threshold);
				int position = (int) temp[6];
				int config = (int) temp[5];
				int contrast = (int) temp[2];
				int brightness = (int) temp[3];

				short p4 = (short) (position & 0xFF);
				position >>= 8;
				short p3 = (short) (position & 0xFF);
				position >>= 8;
				short p2 = (short) (position & 0xFF);
				position >>= 1;
				short p1 = (short) (position & 0x1);

				byte con = (byte) ((config << 5) + contrast);
				System.out.println(t + "/" + rangeBlockCount + " complete");

				code[0] = con;
				code[1] = (byte) (brightness & 0xFF);
				code[1] <<= 1;
				code[1] += p1;
				code[2] = (byte) p2;
				code[3] = (byte) p3;
				code[4] = (byte) p4;
				outputStream.write(code);
				t++;

			}
		}
		outputStream.close();
	}

	public static void EncodeWithThreads(String inputFile, String outputFile, int size, double threshold) throws IOException {
		File imageFile = new File(inputFile);
		BufferedImage before = ImageIO.read(imageFile);
		BufferedImage image;
		int w = before.getWidth();
		int h = before.getHeight();
		final int MAX_THREAD = 10;

		if ((w % size == 0) && (h % size == 0)) {
			image = before;
		} else {
			AffineTransform at = new AffineTransform();
			double nx = (Math.ceil(((double) w) / size) * size);
			double ny = (Math.ceil(((double) h) / size) * size);
			double sx = nx / w;
			double sy = ny / h;
			at.scale(sx, sy);
			image = new BufferedImage((int) nx, (int) ny, BufferedImage.TYPE_INT_ARGB);
			System.out.println(sx + ":" + sy);
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			scaleOp.filter(before, image);
		}

		System.out.println(image.getHeight() + ":" + image.getWidth());
		OutputStream outputStream = new FileOutputStream(outputFile);
		int rangeBlockCount = image.getHeight() * image.getWidth() / (size * size);
		int numThreads = Math.min(Math.min(MAX_THREAD, Runtime.getRuntime().availableProcessors()), rangeBlockCount);
		EncodeThread[] threadArray = new EncodeThread[numThreads];
		for (int i = 0; i < threadArray.length; i++)
		{
			threadArray[i] = new EncodeThread(i, size, rangeBlockCount, threshold, image, outputStream, threadArray);
			threadArray[i].start();
		}
		while (threadArray[numThreads - 1].isAlive())
		{
			try
			{
				Thread.sleep(5);
			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}
		outputStream.close();
	}
	
	public static void partialEncode(String inputFile, String outputFile, int size, int maxThreads) {

	}

	public static void run(String inputFile, String outputFile, int size, int maxThreads) throws IOException {
		File imageFile = new File(inputFile);
		BufferedImage before = ImageIO.read(imageFile);
		BufferedImage image;
		int w = before.getWidth();
		int h = before.getHeight();

		if ((w % size == 0) && (h % size == 0)) {
			image = before;
		} else {
			AffineTransform at = new AffineTransform();
			double nx = (Math.ceil(((double) w) / size) * size);
			double ny = (Math.ceil(((double) h) / size) * size);
			double sx = nx / w;
			double sy = ny / h;
			at.scale(sx, sy);
			image = new BufferedImage((int) nx, (int) ny, BufferedImage.TYPE_INT_ARGB);
			System.out.println(sx + ":" + sy);
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			scaleOp.filter(before, image);
		}
		System.out.println(image.getHeight() + ":" + image.getWidth());
		OutputStream outputStream = new FileOutputStream(outputFile);
		int rangeBlockCount = image.getHeight() * image.getWidth() / (size * size);
		double[] temp = new double[6];
		byte[] code = new byte[5];
		int t = 1;
		for (int i = 0; i < image.getHeight() / size; i++) {
			for (int j = 0; j < image.getWidth() / size; j++) {
				temp = IFSEncoder.selectBestDomain(image, j * size, i * size, size);
				int position = (int) temp[5];
				System.out.println(t + "/" + rangeBlockCount + " complete");
				code[0] = (byte) ((((int) temp[5]) & 0x7) << 5 + ((Math.round(temp[2] * 31)) & 0x1F));
				code[1] = (byte) ((((int) temp[3]) & 0x7F) << 1 + (position & 0x1));
				position >>= 1;
				code[2] = (byte) (position & 0xFF);
				position >>= 8;
				code[3] = (byte) (position & 0xFF);
				position >>= 8;
				code[4] = (byte) (position & 0xFF);
				outputStream.write(code);
				t++;

			}
		}
		outputStream.close();
	}

	// SCAN DOMAIN-->RANGE NOT THE OTHER WAY AROUND. STORE RANGE CONSTANTS
	public static double[] selectBestRange() {
		double[] result = null;
		return result;
	}

	public static double[] selectBestDomain(BufferedImage image, int rx, int ry, int size) {
		short[][] R = CMap.imageToArray(image, rx, ry, rx + size, ry + size);
		short[][] F;
		short[][] D;
		double[] comparison = new double[] { -1, -1, 0, 0, Double.MAX_VALUE, -1, -1 };
		double[] temp;
		int b = 0, b2 = 0;
		for (short[] u : R) {
			for (short v : u) {
				b += v;
				b2 += v * v;
			}
		}
		long a = 0, a2 = 0;
		for (int i = 0; i < image.getHeight() + 1 - 2 * size; i++) {
			for (int j = 0; j < image.getWidth() + 1 - 2 * size; j++) {
				// System.out.println(j + " , " + i);
				F = CMap.imageToArray(image, j, i, j + 2 * size, i + 2 * size);
				D = CMap.subsample(F);
				for (short[] u : D) {
					for (short v : u) {
						a += v;
						a2 += v * v;
					}
				}
				for (int k = 0; k < 8; k++) {
					D = CMap.permute(D, k);
					temp = IFSEncoder.regression(D, R, a, a2, b, b2);
					if (temp[2] < comparison[4]) {
						comparison[0] = j;
						comparison[1] = i;
						comparison[2] = temp[0];
						comparison[3] = temp[1];
						comparison[4] = temp[2];
						comparison[5] = k;
						comparison[6] = i * (image.getHeight() + 1 - 2 * size) + j;
					}
				}
			}
		}

		return comparison;
	}

	public static double[] selectBestDomain(BufferedImage image, int rx, int ry, int size, double threshold) {
		short[][] R = CMap.imageToArray(image, rx, ry, rx + size, ry + size);
		short[][] F;
		short[][] D;
		double[] comparison = new double[] { -1, -1, 0, 0, Double.MAX_VALUE, -1, -1 };
		double[] temp;
		int b = 0, b2 = 0;
		for (short[] u : R) {
			for (short v : u) {
				b += v;
				b2 += v * v;
			}
		}
		long a = 0, a2 = 0;
		for (int i = 0; i < image.getHeight() + 1 - 2 * size; i++) {
			for (int j = 0; j < image.getWidth() + 1 - 2 * size; j++) {
				// System.out.println(j + " , " + i);
				F = CMap.imageToArray(image, j, i, j + 2 * size, i + 2 * size);
				D = CMap.subsample(F);
				for (short[] u : D) {
					for (short v : u) {
						a += v;
						a2 += v * v;
					}
				}
				for (int k = 0; k < 8; k++) {
					D = CMap.permute(D, k);
					temp = IFSEncoder.regression(D, R, a, a2, b, b2);
					if (temp[2] < threshold) {
						comparison[0] = j;
						comparison[1] = i;
						comparison[2] = temp[0];
						comparison[3] = temp[1];
						comparison[4] = temp[2];
						comparison[5] = k;
						comparison[6] = i * (image.getHeight() + 1 - 2 * size) + j;
						return comparison;
					} else if (temp[2] < comparison[4]) {
						comparison[0] = j;
						comparison[1] = i;
						comparison[2] = temp[0];
						comparison[3] = temp[1];
						comparison[4] = temp[2];
						comparison[5] = k;
						comparison[6] = i * (image.getHeight() + 1 - 2 * size) + j;
					}
				}
			}
		}

		return comparison;
	}

	public static double[] regression(short[][] F, short[][] R, long a, long a2, long b, long b2) {
		long ab = 0;
		int n = F.length * F[0].length;
		int g = 0;
		double s = 0;
		double ms = 0;
		for (int i = 0; i < F.length; i++) {
			for (int j = 0; j < F[0].length; j++) {
				ab += F[i][j] * R[i][j];
			}
		}
		double s1 = (((double) n * ab - a * b) / (n * a2 - a * a));
		double s2 = Math.min(s1, 1.0);
		double s3 = Math.max(s2, 0);
		double s4 = Math.round(s3 * 31);
		s = s4 / 31;
		g = Math.min(Math.max((int) (((double) b - s * a) / (n)), 0) / 2, 255);
		ms = ((double) b2 + s * (s * a2 - 2 * ab + 2 * g * a) + g * (n * g - 2 * b)) / (n);
		double[] result = { s4, g, Math.sqrt(ms) };
		return result;
	}

	public static void main(String[] args) {
		final long startTime = System.currentTimeMillis();
		try {
			IFSEncoder.EncodeWithThreads("TestTownG.png", "TestCodeBook", 8, 125);
		} catch (IOException e) {
			e.printStackTrace();
		}
		final long endTime = System.currentTimeMillis();

		System.out.println("Total execution time: " + (endTime - startTime));

	}

}
