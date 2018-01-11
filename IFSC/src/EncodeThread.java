import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
public class EncodeThread extends Thread
{
	private int size;
	private BufferedImage image;
	private OutputStream output;
	private EncodeThread[] threadArray;
	public int threadID;
	
	/**
	 * @param threadIDConstruct - the index of the thread in threadArray. Used to determine range block rows being worked on by thread.
	 * @param sizeConstruct - size of one of the range blocks' width or height in pixels
	 * @param imageConstruct - the input image
	 * @param outputConstruct - the OutputStream used to write the code to the output file
	 */
	public EncodeThread(int threadIDConstruct, int sizeConstruct, BufferedImage imageConstruct, OutputStream outputConstruct, EncodeThread[] threadArrayConstruct)
	{
		threadID = threadIDConstruct;
		size = sizeConstruct;
		image = imageConstruct;
		output = outputConstruct;
		threadArray = threadArrayConstruct;
	}
	
	public void run()
	{
		int totalRows = image.getHeight()/size;
		int startBlockRow = threadID * (totalRows / threadArray.length), endBlockRow;
		if (threadID == threadArray.length - 1)
			endBlockRow = startBlockRow + (totalRows / threadArray.length) + (totalRows % threadArray.length);
		else
			endBlockRow = startBlockRow + (totalRows / threadArray.length);
		double[][][] temp = new double[endBlockRow - startBlockRow][image.getWidth()/size][7];
		byte[] code = new byte[5];
		for (int i = 0; i < temp.length; i++)
		{
			for(int j = 0; j < temp[i].length; j++)
			{
				temp[i][j] = IFSEncoder.selectBestDomain(image, j * size, (startBlockRow + i) * size, size);
			}
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
		for (double[][] rowData : temp)
		{
			for(double[] squareData : rowData)
			{
				int position = (int) squareData[6];
				code[0] = (byte) ((((int) squareData[5]) & 0x7) << 5 + ((Math.round(squareData[2] * 31)) & 0x1F));
				code[1] = (byte) ((((int) squareData[3]) & 0x7F) << 1 + (position & 0x1));
				position >>= 1;
				code[2] = (byte) (position & 0xFF);
				position >>= 8;
				code[3] = (byte) (position & 0xFF);
				position >>= 8;
				code[4] = (byte) (position & 0xFF);
				try
				{
					output.write(code);
				}
				catch(IOException ie)
				{
					ie.printStackTrace();
				}
			}
		}
		System.out.println("Thread " + threadID + " done!");
	}
}