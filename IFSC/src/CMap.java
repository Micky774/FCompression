import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class CMap {
	public int size;
	public int config;
	public short contrast;
	public short brightness;
	public int position;

	public void map(BufferedImage domain, BufferedImage range, int xStart, int yStart, int size) {

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