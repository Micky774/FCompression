import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
public class EncodeThread extends Thread
{
	private int threadID, size, rangeBlockCount;
	double threshold = 0;
	private BufferedImage image;
	private OutputStream output;
	private EncodeThread[] threadArray;
	
	/**
	 * @param threadIDConstruct - the index of the thread in threadArray. Used to determine range block rows being worked on by thread.
	 * @param sizeConstruct - size of one of the range blocks' width or height in pixels
	 * @param rangeBlockCountConstruct - number of range blocks in image
	 * @param imageConstruct - the input image
	 * @param outputConstruct - the OutputStream used to write the code to the output file
	 * @param threadArrayConstruct - the array of EncodeThreads being used to encode the image
	 */
	public EncodeThread(int threadIDConstruct, int sizeConstruct, int rangeBlockCountConstruct, BufferedImage imageConstruct, OutputStream outputConstruct, EncodeThread[] threadArrayConstruct)
	{
		threadID = threadIDConstruct;
		size = sizeConstruct;
		rangeBlockCount = rangeBlockCountConstruct;
		image = imageConstruct;
		output = outputConstruct;
		threadArray = threadArrayConstruct;
	}
	
	/**
	 * @param threadIDConstruct - the index of the thread in threadArray. Used to determine range block rows being worked on by thread.
	 * @param sizeConstruct - size of one of the range blocks' width or height in pixels
	 * @param rangeBlockCountConstruct - number of range blocks in image
	 * @param thresholdConstruct - minimum threshold to declare a domain block a "match" for a range block.
	 * @param imageConstruct - the input image
	 * @param outputConstruct - the OutputStream used to write the code to the output file
	 * @param threadArrayConstruct - the array of EncodeThreads being used to encode the image
	 */
	public EncodeThread(int threadIDConstruct, int sizeConstruct, int rangeBlockCountConstruct, double thresholdConstruct, BufferedImage imageConstruct, OutputStream outputConstruct, EncodeThread[] threadArrayConstruct)
	{
		threadID = threadIDConstruct;
		size = sizeConstruct;
		rangeBlockCount = rangeBlockCountConstruct;
		threshold = thresholdConstruct;
		image = imageConstruct;
		output = outputConstruct;
		threadArray = threadArrayConstruct;
	}
	
	public void run()
	{
		int rangeBlocksCovered = rangeBlockCount / threadArray.length;
		int startRangeBlock = Math.min(threadID, rangeBlockCount % threadArray.length) * (rangeBlocksCovered + 1) + Math.max((threadID - (rangeBlockCount % threadArray.length)), 0) * rangeBlocksCovered;
		if(threadID < rangeBlockCount % threadArray.length)
			rangeBlocksCovered++;
		double[] temp = new double[7];
		byte[][] code = new byte[rangeBlocksCovered][5];
		for(int i = 0; i < rangeBlocksCovered; i++)
		{
			int rangeBlockRow = (startRangeBlock + i) / (image.getWidth() / size);
			int rangeBlockColumn = (startRangeBlock + i) % (image.getWidth() / size);
			if(threshold > 0)
				temp = IFSEncoder.selectBestDomain(image, rangeBlockColumn * size, rangeBlockRow * size, size, threshold);
			else
				temp = IFSEncoder.selectBestDomain(image, rangeBlockColumn * size, rangeBlockRow * size, size);
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
			System.out.println((startRangeBlock + i + 1) + "/" + rangeBlockCount + " complete");

			code[i][0] = con;
			code[i][1] = (byte) (brightness & 0xFF);
			code[i][1] <<= 1;
			code[i][1] += p1;
			code[i][2] = (byte) p2;
			code[i][3] = (byte) p3;
			code[i][4] = (byte) p4;
		}
		if(threadID > 0)
		{
			try 
			{
				threadArray[threadID-1].join();
			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}
		for (int i = 0; i < rangeBlocksCovered; i++)
		{
			try
			{
				output.write(code[i]);
			}
			catch(IOException ie)
			{
				ie.printStackTrace();
			}
			System.out.println((startRangeBlock + i + 1) + "/" + rangeBlockCount + " range blocks encoded.");
		}
		System.out.println("Thread " + threadID + " done!");
	}
}