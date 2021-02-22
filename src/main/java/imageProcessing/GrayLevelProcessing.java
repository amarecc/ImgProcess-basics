package imageProcessing;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.Cursor;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.exception.IncompatibleTypeException;
import java.io.File;


public class GrayLevelProcessing{

	public static Img<UnsignedByteType> createImg(String filename) throws ImgIOException, IncompatibleTypeException{
            if (!new File(filename).exists()) {
                System.err.println("File '" + filename + "' does not exist");
                System.exit(-1);
            }

            final ArrayImgFactory<UnsignedByteType> factory = new ArrayImgFactory<>(new UnsignedByteType());
            final ImgOpener imgOpener = new ImgOpener();
            final Img<UnsignedByteType> input = (Img<UnsignedByteType>) imgOpener.openImgs(filename, factory).get(0);
            imgOpener.context().dispose();

			return input;
	}
	public static void setPixelValue(UnsignedByteType pixel, int value) {
		if (value <= 255 && value >= 0)
			pixel.set(value);
		else if (value < 0) 
			pixel.set(0);
		else {
			pixel.set(255);
			System.out.println(value);
		}
	}

	public static void threshold(Img<UnsignedByteType> img, int t) {
		final RandomAccess<UnsignedByteType> r = img.randomAccess();

		final int iw = (int) img.max(0);
		final int ih = (int) img.max(1);

		for (int x = 0; x <= iw; ++x) {
			for (int y = 0; y <= ih; ++y) {
				r.setPosition(x, 0);
				r.setPosition(y, 1);
				final UnsignedByteType val = r.get();
				if (val.get() < t)
				    val.set(0);
				else
				    val.set(255);
			}
		}

	}

	public static void addLumen(Img<UnsignedByteType> img, int delta) {
		final RandomAccess<UnsignedByteType> r = img.randomAccess();

		final int iw = (int) img.max(0);
		final int ih = (int) img.max(1);

		for (int x = 0; x <= iw; ++x) {
			for (int y = 0; y <= ih; ++y) {
				r.setPosition(x, 0);
				r.setPosition(y, 1);
				
				final UnsignedByteType pixel = r.get();
				final int pixelValue = pixel.get();
				
				if (pixelValue + delta <= 255 && pixelValue + delta >= 0)
					pixel.set(pixelValue + delta);
				else if (pixelValue + delta < 0)
					pixel.set(0);
				else
					pixel.set(255);
			}
		}
	}

	public static void addLumenCursor(Img<UnsignedByteType> img, int delta) {
		final Cursor<UnsignedByteType> cursor = img.cursor();

		while (cursor.hasNext()) {
			cursor.fwd();

			final UnsignedByteType pixel = cursor.get();
			final int pixelValue = pixel.get();

			if (pixelValue + delta <= 255 && pixelValue + delta >= 0)
				pixel.set(pixelValue + delta);
			else if (pixelValue + delta < 0)
				pixel.set(0);
			else
				pixel.set(255);

		}
	}

	public static int getMinImg(Img<UnsignedByteType> img) {
		final Cursor<UnsignedByteType> cursor = img.cursor();

		int min = 255;
		while (cursor.hasNext()) {
			cursor.fwd();
			if (cursor.get().get() < min)
				min = cursor.get().get();
		}

		return min;
	}

	public static int getMaxImg(Img<UnsignedByteType> img) {
		final Cursor<UnsignedByteType> cursor = img.cursor();

		int max = 0;
		while (cursor.hasNext()) {
			cursor.fwd();
			if (cursor.get().get() > max)
				max = cursor.get().get();
		}

		return max;
	}

	public static void extendDynamic(Img<UnsignedByteType> img, int min, int max) {
		final Cursor<UnsignedByteType> cursor = img.cursor();
		int minImg = getMinImg(img);
		int maxImg = getMaxImg(img);


		while(cursor.hasNext()) {
			cursor.fwd();

			final UnsignedByteType pixel = cursor.get();
			final int newPixelValue = (max * (pixel.get() - minImg)) / (maxImg - minImg);
			
			pixel.set(newPixelValue);
		}
	}

	public static void extendDynamicLUT(Img<UnsignedByteType> img, int min, int max) {
		final Cursor<UnsignedByteType> cursor = img.cursor();
		int minImg = getMinImg(img);
		int maxImg = getMaxImg(img);
		int LUT[] = new int[256];

		for (int i = 0; i < 256; i++)
			LUT[i] = ( (max * (i - minImg)) / (maxImg - minImg));

		while (cursor.hasNext()) {
			cursor.fwd();

			final UnsignedByteType pixel = cursor.get();

			pixel.set(LUT[pixel.get()]);
		}
	}

	public static void egalisationHist(Img<UnsignedByteType> img) {
		final Cursor<UnsignedByteType> cursor = img.cursor();
		int hist[] = new int[256];
		int histCumul[] = new int[256];
		int N = (int) img.max(0) * (int) img.max(1);

		while (cursor.hasNext()) {
			cursor.fwd();
			final UnsignedByteType pixel = cursor.get();
			final int pixelValue = pixel.get();
			
			hist[pixelValue]++;
		}

		histCumul[0] = hist[0];
		for (int i = 1; i < 256; i++){
			histCumul[i] = histCumul[i - 1] + hist[i];
		}

		for (UnsignedByteType p : img) {
			if (N > 0)
				p.set( (255 * histCumul[p.get()]) / N);
		}
			
	}
	
	public static void main(final String[] args) throws ImgIOException, IncompatibleTypeException {
		// load image
		if (args.length < 2) {
			System.out.println("missing input and/or output image filenames");
			System.exit(-1);
		} 
		final String filename = args[0];
		if (!new File(filename).exists()) {
			System.err.println("File '" + filename + "' does not exist");
			System.exit(-1);
		}

		final ArrayImgFactory<UnsignedByteType> factory = new ArrayImgFactory<>(new UnsignedByteType());
		final ImgOpener imgOpener = new ImgOpener();
		final Img<UnsignedByteType> input = (Img<UnsignedByteType>) imgOpener.openImgs(filename, factory).get(0);
		imgOpener.context().dispose();

		long start = System.nanoTime();

		String functionToTest = "extendDynamicLUT";

		switch (functionToTest) {
			case "threshold":
				threshold(input, 128);
				break;
			case "addLumen":
				addLumen(input, 50);
				break;
			case "addLumenCursor":
				addLumenCursor(input, 50);
				break;
			case "extendDynamic":
				extendDynamic(input, 0, 255);
				break;
			case "extendDynamicLUT":
				extendDynamicLUT(input, 0, 255);
				break;
			case "egalisationHist":
				egalisationHist(input);
				break;
		
			default:
				break;
		}

		long estimatedTime = System.nanoTime() - start;

		System.out.println("Temps d'exec : " + estimatedTime);
		
		// save output image
		final String outPath = args[1];
		File path = new File(outPath);
		// clear the file if it already exists.
		if (path.exists()) {
			path.delete();
		}
		ImgSaver imgSaver = new ImgSaver();
		imgSaver.saveImg(outPath, input);
		imgSaver.context().dispose();
		System.out.println("Image saved in: " + outPath);		
	}

}
