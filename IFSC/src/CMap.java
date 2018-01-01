import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class CMap {
	public int size;
	public int config;
	public short contrast;
	public short brightness;
	public int position;

	public void map(BufferedImage domain, BufferedImage range, int rx, int ry, int size) {
		int dx = position % (domain.getWidth() - size * 2);
		int dy = position / (domain.getWidth() - size * 2);
		int[][] Domain = CMap.subsample(CMap.imageToArray(domain, dx, dy, dx + 2 * size, dy + 2 * size));
		int[][] Range = new int[Domain.length][Domain[0].length];
		int r = Domain.length;
		int s = Domain[0].length;
		for (int[] i : Domain) {
			for (int j : i) {
				j = Math.min(j * contrast + brightness, 255);
			}
		}
		switch (config) {
		case 0:
			break;
		case 1:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[j][s - i - 1] = Domain[i][j];
				}
			}
			break;
		case 2:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[s - i - 1][r - j - 1] = Domain[i][j];
				}
			}
			break;
		case 3:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[r - j - 1][i] = Domain[i][j];
				}
			}
			break;
		case 4:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[j][i] = Domain[i][j];
				}
			}
			break;
		case 5:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[s - i - 1][j] = Domain[i][j];
				}
			}
			break;
		case 6:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[r - j - 1][s - i - 1] = Domain[i][j];
				}
			}
			break;
		case 7:
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < s; j++) {
					Range[i][r - j - 1] = Domain[i][j];
				}
			}
			break;
		}
	}

	public static int[][] subsample(int[][] array) {
		int[][] result = new int[array.length / 2][array[0].length];
		int r = 0, s = 0;
		for (int[] i : result) {
			for (int j : i) {
				j = (array[r][s] + array[r][s + 1] + array[r + 1][s] + array[r + 1][s + 1]) / 4;
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
	public static int[][] imageToArray(BufferedImage image, int sx, int sy, int tx, int ty) {
		int[][] result = new int[ty - sy][tx - sx];
		int r = 0, s = 0;
		for (int[] i : result) {
			for (int j : i) {
				j = image.getRGB(sx + s, sy + r);
				s++;
			}
			r++;
			s = 0;
		}
		return result;

	}

	public CMap(byte[] code, int _size) {
		ByteBuffer bb = ByteBuffer.wrap(code);
		size = _size;
		brightness = (short) TTP.byteToInt(bb.get());
		short temp = (short) TTP.byteToInt(bb.get());
		config = temp >> 5;
		contrast = (short) (temp & 0x31);
		temp = (short) TTP.byteToInt(bb.get());
		contrast += (short) (temp >> 3);
		position = (short) (temp & 0x7);
		temp = (short) TTP.byteToInt(bb.get());
		position += (short) temp;
		temp = (short) TTP.byteToInt(bb.get());
		position += (short) temp;
		temp = (short) TTP.byteToInt(bb.get());
		position += (short) temp;

	}

	public static void main(String[] args) {

	}

}