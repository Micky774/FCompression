import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class CMap {
	public int size;
	public int config;
	public int position;
	public double contrast;
	public double brightness;
	public int dx, dy;
	public int rx, ry;

	public void setRange(int x, int y) {
		rx = x;
		ry = y;
	}

	public static short[][] permute(short[][] domain, int config) {
		int r = domain.length;
		int s = domain[0].length;
		short[][] Range = new short[r][s];
		switch (config) {
		case 0:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[i][j] = domain[i][j];
				}
			}
			break;
		case 1:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[j][s - i - 1] = domain[i][j];
				}
			}
			break;
		case 2:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[s - i - 1][r - j - 1] = domain[i][j];
				}
			}
			break;
		case 3:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[r - j - 1][i] = domain[i][j];
				}
			}
			break;
		case 4:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[j][i] = domain[i][j];
				}
			}
			break;
		case 5:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[s - i - 1][j] = domain[i][j];
				}
			}
			break;
		case 6:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[r - j - 1][s - i - 1] = domain[i][j];
				}
			}
			break;
		case 7:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[i][r - j - 1] = domain[i][j];
				}
			}
			break;
		}
		return Range;
	}

	public void map(BufferedImage domain, BufferedImage range) {

		short[][] Domain = CMap.subsample(CMap.imageToArray(domain, dx, dy, dx + 2 * size, dy + 2 * size));

		for (short[] i : Domain) {
			for (short j : i) {
				j = (short) Math.min(j * contrast + brightness, 255);
			}
		}

		short[][] Range = CMap.permute(Domain, config);

		int r = 0, s = 0, rgb = 0;

		for (short[] i : Range) {
			for (short j : i) {
				rgb = 0xFF;
				rgb <<= 8;
				rgb += Range[r][s];
				rgb <<= 8;
				rgb += Range[r][s];
				rgb <<= 8;
				rgb += Range[r][s];
				range.setRGB(rx + s, ry + r, rgb);
				s++;
			}
			r++;
			s = 0;
		}
	}

	public static short[][] subsample(short[][] array) {
		short[][] result = new short[array.length / 2][array[0].length / 2];
		int r = 0, s = 0;
		for (short[] i : result) {
			for (short j : i) {
				j = (short) ((array[r][s] + array[r][s + 1] + array[r + 1][s] + array[r + 1][s + 1]) / 4);
				s += 2;
			}
			r += 2;
			s = 0;
		}
		return result;
	}

	/**
	 * @return a 2D array of gray values (as RGB ints) coresponding to the given
	 *         rectangle
	 * 
	 * 
	 * 
	 */
	public static short[][] imageToArray(BufferedImage image, int sx, int sy, int tx, int ty) {
		short[][] result = new short[ty - sy][tx - sx];
		// int r = 0, s = 0;
		// System.out.println("Height: " + image.getHeight() + " and Width: " +
		// image.getWidth());

		// System.out.println(sx + " : " + sy);
		for (int i = 0; i < ty - sy; i++) {
			for (int j = 0; j < tx - sx; j++) {
				result[i][j] = (short) (image.getRGB(sx + j, sy + i) & 0xFF);
			}
		} /*
			 * for (short[] i : result) { for (short j : i) { if (sy != 0) {
			 * System.out.println((sx + s) + " , " + (sy + r)); } j = (short)
			 * (image.getRGB(sx + s, sy + r) & 0xFF); s++; } r++; s = 0; }
			 */
		return result;

	}

	public CMap(byte[] code, int _size, BufferedImage domain) {
		ByteBuffer bb = ByteBuffer.wrap(code);
		size = _size;
		short temp = (short) TTP.byteToInt(bb.get());
		config = temp >> 5;
		contrast = ((double) (temp & 0x1F)) / 31;
		temp = (short) TTP.byteToInt(bb.get());
		brightness = temp >> 1;
		position = (temp & 0x1);
		position <<= 1;
		temp = (short) TTP.byteToInt(bb.get());
		position += temp;
		position <<= 8;
		temp = (short) TTP.byteToInt(bb.get());
		position += temp;
		position <<= 8;
		temp = (short) TTP.byteToInt(bb.get());
		position += temp;
		dx = position % (domain.getWidth() - size * 2);
		dy = position / (domain.getWidth() - size * 2);

	}

	public static void main(String[] args) {
	}

}