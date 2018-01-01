import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class CMap {
	public int size;
	public int config;
	public short contrast;
	public short brightness;

	public void map(BufferedImage domain, BufferedImage range, int xStart, int yStart, int size) {

	}

	public CMap(byte[] code, int _size) {
		ByteBuffer bb = ByteBuffer.wrap(code);
		size = _size;
		brightness = (short) TTP.byteToInt(bb.get());
		short temp = (short) TTP.byteToInt(bb.get());
		config = temp >> 2;
		contrast = (temp);
	}

	//THIS IS EMPTY, YOU NUMBSKULL
	public static void main(String[] args) {

	}

}