import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public class EncodeThread extends Thread
{
	/**
	 * threadID - index of thread in threadArray
	 * size - the width/height of one range block
	 * rangeBlockCount -  number of range blocks in image
	 * threshold - used by SelectBestDomain method in IFSEncoder to find near-matching domain blocks
	 * image - the input image
	 * output - the OutputStream used to write the code to the output file
	 * threadArray - the array of EncodeThreads employed by IFSEncoder
	 */
	private int threadID, size, rangeBlockCount;
	double threshold = 0;
	private BufferedImage image;
	private OutputStream output;
	private EncodeThread[] threadArray;
	
	/**
	 * Constructs a new EncodeThread WITHOUT a threshold input.
	 * 
	 * @param threadIDConstruct - int input for threadID
	 * @param sizeConstruct - int input for size
	 * @param rangeBlockCountConstruct - int input for rangeBlockCount
	 * @param imageConstruct - BufferedImage input for image
	 * @param outputConstruct - OutputStream input for output
	 * @param threadArrayConstruct - EncodeThread[] input for threadArray
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
	 * Constructs a new EncodeThread WITH a threshold input
	 * 
	 * @param threadIDConstruct - int input for threadID
	 * @param sizeConstruct - int input for size
	 * @param rangeBlockCountConstruct - int input for rangeBlockCount
	 * @param thresholdConstruct - double input for threshold
	 * @param imageConstruct - BufferedImage input for image
	 * @param outputConstruct - OutputStream input for output
	 * @param threadArrayConstruct - EncodeThread[] input for threadArray
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
	
	/**
	 * Runs the EncodeThread in order to encode range blocks and output the byte-data to the output file.
	 */
	public void run()
	{
		//uses number of EncodeThreads, number of range blocks, and position of current thread in
		//threadArray to determine which range blocks should be encoded
		int rangeBlocksCovered = rangeBlockCount / threadArray.length;
		int startRangeBlock = Math.min(threadID, rangeBlockCount % threadArray.length) * (rangeBlocksCovered + 1) + Math.max((threadID - (rangeBlockCount % threadArray.length)), 0) * rangeBlocksCovered;
		if(threadID < rangeBlockCount % threadArray.length)
			rangeBlocksCovered++;
		
		//constructs arrays used to store data for output
		double[] temp = new double[7];
		byte[][] code = new byte[rangeBlocksCovered][5];
		
		//loop for encoding range blocks into byte-data in byte[][] code
		for(int i = 0; i < rangeBlocksCovered; i++)
		{
			//calculates the row and column of the range block currently being encoded
			int rangeBlockRow = (startRangeBlock + i) / (image.getWidth() / size);
			int rangeBlockColumn = (startRangeBlock + i) % (image.getWidth() / size);
			
			//encodes the current range block with method chosen based on EncodeThread constructor used
			if(threshold > 0)
				temp = IFSEncoder.selectBestDomain(image, rangeBlockColumn * size, rangeBlockRow * size, size, threshold);
			else
				temp = IFSEncoder.selectBestDomain(image, rangeBlockColumn * size, rangeBlockRow * size, size);
			
			//configures different variables from the double[] temp into ints for later storage in byte[][] code
			int position = (int) temp[6];
			int config = (int) temp[5];
			int contrast = (int) temp[2];
			int brightness = (int) temp[3];

			//reconfigures position data into 4 shorts
			short p4 = (short) (position & 0xFF);
			position >>= 8;
			short p3 = (short) (position & 0xFF);
			position >>= 8;
			short p2 = (short) (position & 0xFF);
			position >>= 1;
			short p1 = (short) (position & 0x1);

			//reconfigures config and contrast data into a byte
			byte con = (byte) ((config << 5) + contrast);
			System.out.println((startRangeBlock + i + 1) + "/" + rangeBlockCount + " complete");

			//stores encoded range block data in byte form within byte[][] code
			code[i][0] = con;
			code[i][1] = (byte) (brightness & 0xFF);
			code[i][1] <<= 1;
			code[i][1] += p1;
			code[i][2] = (byte) p2;
			code[i][3] = (byte) p3;
			code[i][4] = (byte) p4;
		}
		
		//stops the current thread until the previous thread outputs data
		//this prevents the threads from outputting data out of order and ruining the encoding
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
		
		//outputs the encoded byte-data to the output file
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
		
		System.out.println("Thread " + (threadID + 1) + " done!");
	}
}