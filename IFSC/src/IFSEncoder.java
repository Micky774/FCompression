import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class IFSEncoder {

	public static void Encode(String inputFile, String outputFile, int size) throws IOException {
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
		int t = 0;
		for (int i = 0; i < image.getHeight() / size; i++) {
			for (int j = 0; j < image.getWidth() / size; j++) {
				temp = IFSEncoder.selectBestDomain(image, j * size, i * size, size);
				int position = t;
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
		int t = 0;
		for (int i = 0; i < image.getHeight() / size; i++) {
			for (int j = 0; j < image.getWidth() / size; j++) {
				temp = IFSEncoder.selectBestDomain(image, j * size, i * size, size);
				int position = t;
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

	public static double[] selectBestDomain(BufferedImage image, int rx, int ry, int size) {
		short[][] R = CMap.imageToArray(image, rx, ry, rx + size, ry + size);
		short[][] D;
		double[] comparison = new double[] { -1, -1, 0, 0, Double.MAX_VALUE, -1 };
		double[] temp;
		for (int i = 0; i < image.getHeight() + 1 - 2 * size; i++) {
			for (int j = 0; j < image.getWidth() + 1 - 2 * size; j++) {
				// System.out.println(j + " , " + i);
				D = CMap.subsample(CMap.imageToArray(image, j, i, j + 2 * size, i + 2 * size));
				for (int k = 0; k < 8; k++) {
					D = CMap.permute(D, k);
					temp = IFSEncoder.regression(D, R);
					if (temp[2] < comparison[4]) {
						comparison[0] = j;
						comparison[1] = i;
						comparison[2] = temp[0];
						comparison[3] = temp[1];
						comparison[4] = temp[2];
						comparison[5] = k;
					}
				}
			}
		}

		return comparison;
	}

	public static double[] regression(short[][] F, short[][] R) {
		int a = 0, a2 = 0, b = 0, b2 = 0, ab = 0;
		int n = F.length * F[0].length;
		int g = 0;
		double s = 0;
		double ms = 0;
		for (int i = 0; i < F.length; i++) {
			for (int j = 0; j < F[0].length; j++) {
				a += F[i][j];
				b += R[i][j];
				ab += F[i][j] * R[i][j];
				a2 += F[i][j] * F[i][j];
				b2 += R[i][j] * R[i][j];
			}
		}
		s = (((double) n * ab - a * b) / (n * a2 - a * a));
		g = (int) (((double) b - s * a) / (n));
		ms = ((double) b2 + s * (s * a2 - 2 * ab + 2 * g * a) + g * (n * g - 2 * b)) / (n);
		double[] result = { s, g, Math.sqrt(ms) };
		return result;
	}

	public static void main(String[] args) {
		final long startTime = System.currentTimeMillis();
		try {
			IFSEncoder.Encode("TestTownG.png", "TestCodeBook", 128);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final long endTime = System.currentTimeMillis();

		System.out.println("Total execution time: " + (endTime - startTime));

	}

}
