package imageProcessing;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.loops.LoopBuilder;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.exception.IncompatibleTypeException;
import java.io.File;
import java.util.Arrays;

import net.imglib2.view.Views;
import net.imglib2.view.IntervalView;


public class Couleur {

	/**
	 * Question 3
	 */
	public static void convertColor(Img<UnsignedByteType> img) {
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(img, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(img, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(img, 2, 2);

		LoopBuilder.setImages(inputR, inputG, inputB).forEachPixel(
			(r, g, b) -> {
				double pixelValue = r.get() * 0.3 + g.get() * 0.59 + b.get() * 0.11;

				r.set((int)pixelValue);
				g.set((int)pixelValue);
				b.set((int)pixelValue);
			}
		);
	}

	/**
	 * Question 4
	 */
	public static void rgbToHsv(int r, int g, int b, float[] hsv) {
		if (hsv.length != 3)
			System.exit(1);

		int max = 0;
		int min = 255;
		int[] rgb = { r, g, b };
		for (int i = 0 ; i < rgb.length; i++) {
			if (rgb[i] > max)
				max = rgb[i];
			if (rgb[i] < min)
				min = rgb[i];
		}

		if (max == min) 
			hsv[0] = 0;
		else if (max == r)
			hsv[0] = ((g - b) / (float)(max - min) * 60 + 360) % 360;
		else if (max == g)
			hsv[0] = ((b - r) / (float)(max - min) * 60) + 120;
		else
			hsv[0] = ((r - g) / (float)(max - min) * 60) + 240;

		if (max == 0)
			hsv[1] = 0;
		else
			hsv[1] = ((float)1 - (min/(float)max)) * 100;

		hsv[2] = max;
	}

	public static void hsvToRgb(float h, float s, float v, int[] rgb) {
		float ti = (h/60) % 6;
		float f = h/60 - ti;

		float l = v * (1 - s);
		float m = v * (1 - f * s);
		float n = v * (1 - (1 - f) * s);

		switch ((int)ti) {
			case 0:
				rgb[0] = (int)(v * 100);
				rgb[1] = (int)(n * 100);
				rgb[2] = (int)(l * 100);
				break;
			case 1:
				rgb[0] = (int)(m * 100);
				rgb[1] = (int)(v * 100);
				rgb[2] = (int)(l * 100);
				break;
			case 2:
				rgb[0] = (int)(l * 100);
				rgb[1] = (int)(v * 100);
				rgb[2] = (int)(n * 100);
				break;
			case 3:
				rgb[0] = (int)(l * 100);
				rgb[1] = (int)(m * 100);
				rgb[2] = (int)(v * 100);
				break;
			case 4:
				rgb[0] = (int)(n * 100);
				rgb[1] = (int)(l * 100);
				rgb[2] = (int)(v * 100);
				break;
			case 5:
				rgb[0] = (int)(v * 100);
				rgb[1] = (int)(l * 100);
				rgb[2] = (int)(m * 100);
				break;
		
			default:
				break;
		}
	}

	public static void colorFilter(Img<UnsignedByteType> img, float hue) {
		float[] hsv = new float[3];
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(img, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(img, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(img, 2, 2);

		LoopBuilder.setImages(inputR, inputG, inputB).forEachPixel(
			(r, g, b) -> {
				rgbToHsv(r.get(), g.get(), b.get(), hsv);
				int[] rgb = new int[3];

				hsvToRgb(hue, hsv[1]/100, hsv[2]/100, rgb);
				r.set(rgb[0]);
				g.set(rgb[1]);
				b.set(rgb[2]);
			}
		);
	}

	public static int getMinImg(Img<UnsignedByteType> img) {
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(img, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(img, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(img, 2, 2);
		final Cursor<UnsignedByteType> cR = inputR.cursor();
		final Cursor<UnsignedByteType> cG = inputG.cursor();
		final Cursor<UnsignedByteType> cB = inputB.cursor();

		int min = 255;

		while (cR.hasNext() && cG.hasNext() && cB.hasNext()) {
			cR.fwd();
			cG.fwd();
			cB.fwd();

			double pixelValue = cR.get().get() * 0.3 + cG.get().get() * 0.59 + cB.get().get() * 0.11;

			if (pixelValue < min)
				min = (int)pixelValue;
		}

		return min;
	}

	public static int getMaxImg(Img<UnsignedByteType> img) {
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(img, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(img, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(img, 2, 2);
		final Cursor<UnsignedByteType> cR = inputR.cursor();
		final Cursor<UnsignedByteType> cG = inputG.cursor();
		final Cursor<UnsignedByteType> cB = inputB.cursor();

		int max = 0;

		while (cR.hasNext() && cG.hasNext() && cB.hasNext()) {
			cR.fwd();
			cG.fwd();
			cB.fwd();

			double pixelValue = cR.get().get() * 0.3 + cG.get().get() * 0.59 + cB.get().get() * 0.11;

			if (pixelValue > max)
				max = (int)pixelValue;
		}

		return max;
	}

	public static void extendDynamicLUT(Img<UnsignedByteType> img, int min, int max) {
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(img, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(img, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(img, 2, 2);
		int minImg = getMinImg(img);
		int maxImg = getMaxImg(img);
		int LUT[] = new int[256];

		for (int i = 0; i < 256; i++)
			LUT[i] = ( (max * (i - minImg)) / (maxImg - minImg));

		LoopBuilder.setImages(inputR, inputG, inputB).forEachPixel(
			(r, g, b) -> {
				r.set(LUT[r.get()]);
				g.set(LUT[g.get()]);
				b.set(LUT[b.get()]);
			}
		);
	}

	public static void extendDynamicLUTValue(Img<UnsignedByteType> img, int min, int max) {
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(img, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(img, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(img, 2, 2);
		int minImg = getMinImg(img);
		int maxImg = getMaxImg(img);
		int LUT[] = new int[256];

		for (int i = 0; i < 256; i++)
			LUT[i] = ( (max * (i - minImg)) / (maxImg - minImg));

		LoopBuilder.setImages(inputR, inputG, inputB).forEachPixel(
			(r, g, b) -> {
				int[] rgb = new int[3];
				float[] hsv = new float[3];
				rgbToHsv(r.get(), g.get(), b.get(), hsv);
				int lum = (int) hsv[2];

				hsv[2] = LUT[lum];
				hsvToRgb(hsv[0], hsv[1]/100, hsv[2]/100, rgb);

				r.set(rgb[0]);
				g.set(rgb[1]);
				b.set(rgb[2]);
			}
		);
	}

	public static void egalisationHistValue(Img<UnsignedByteType> img) {
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(img, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(img, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(img, 2, 2);
		final Cursor<UnsignedByteType> cR = inputR.cursor();
		final Cursor<UnsignedByteType> cG = inputG.cursor();
		final Cursor<UnsignedByteType> cB = inputB.cursor();

		int hist[] = new int[256];
		int histCumul[] = new int[256];
		int N = (int) img.max(0) * (int) img.max(1) * img.numDimensions();

		while (cR.hasNext() && cG.hasNext() && cB.hasNext()) {
			cR.fwd();
			cG.fwd();
			cB.fwd();
			float[] hsv = new float[3];

			rgbToHsv(cR.get().get(), cG.get().get(), cB.get().get(), hsv);

			hist[(int) hsv[2]]++;
		}


		histCumul[0] = hist[0];
		for (int i = 1; i < 256; i++){
			histCumul[i] = histCumul[i - 1] + hist[i];
		}

		LoopBuilder.setImages(inputR, inputG, inputB).forEachPixel(
			(r, g, b) -> {
				int[] rgb = new int[3];
				float[] hsv = new float[3];
				rgbToHsv(r.get(), g.get(), b.get(), hsv);
				int lum = (int) hsv[2];

				hsv[2] = (255 * (float)histCumul[lum]) / N;
				hsvToRgb(hsv[0], hsv[1]/100, hsv[2]/100, rgb);

				r.set(rgb[0]);
				g.set(rgb[1]);
				b.set(rgb[2]);
			}
		);
	}

	public static void egalisationHistGrey(Img<UnsignedByteType> img, int min, int max) {
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(img, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(img, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(img, 2, 2);
		final Cursor<UnsignedByteType> cR = inputR.cursor();
		final Cursor<UnsignedByteType> cG = inputG.cursor();
		final Cursor<UnsignedByteType> cB = inputB.cursor();

		int hist[] = new int[256];
		int histCumul[] = new int[256];
		int N = (int) img.max(0) * (int) img.max(1) * img.numDimensions();

		while (cR.hasNext() && cG.hasNext() && cB.hasNext()) {
			cR.fwd();
			cG.fwd();
			cB.fwd();

			double greyValue = cR.get().get() * 0.3 + cG.get().get() * 0.59 + cB.get().get() * 0.11;

			hist[(int) greyValue]++;
		}


		histCumul[0] = hist[0];
		for (int i = 1; i < 256; i++){
			histCumul[i] = histCumul[i - 1] + hist[i];
		}

		LoopBuilder.setImages(inputR, inputG, inputB).forEachPixel(
			(r, g, b) -> {
				float value = (255 * (float)histCumul[r.get()]) / N;
				r.set((int) value);

				value = (255 * (float)histCumul[g.get()]) / N;
				g.set((int) value);

				value = (255 * (float)histCumul[b.get()]) / N;
				b.set((int) value);
			}
		);
	}

	public static void main(final String[] args) throws ImgIOException, IncompatibleTypeException {

		// load image
		if (args.length < 2) {
			System.out.println("missing input or output image filename");
			System.exit(-1);
		}
		final String filename = args[0];
		final ArrayImgFactory<UnsignedByteType> factory = new ArrayImgFactory<>(new UnsignedByteType());
		final ImgOpener imgOpener = new ImgOpener();
		final Img<UnsignedByteType> input = (Img<UnsignedByteType>) imgOpener.openImgs(filename, factory).get(0);
		imgOpener.context().dispose();

		// output image of same dimensions
		final Dimensions dim = input;
		final Img<UnsignedByteType> output = factory.create(dim);

		long start = System.nanoTime();

		String functionToTest = "egalisationHistGrey";

		switch (functionToTest) {
			case "convertColor":
				convertColor(input);
				break;
			case "rgbToHsv":
				int r = 59; int g = 20; int b = 64;
				float[] hsv = new float[3];
				rgbToHsv(r, g, b, hsv);
				System.out.println(Arrays.toString(hsv));
				break;
			case "hsvToRgb":
				float h = 89.1f; float s = 46.3f; float v = 67.0f;
				int[] rgb = new int[3];
				hsvToRgb(h, s/100, v/100, rgb);
				System.out.println(Arrays.toString(rgb));
				break;
			case "colorFilter":
				colorFilter(input, 190);
				break;
			case "extendDynamicLUTValue":
				extendDynamicLUTValue(input, 0, 255);
				break;
			case "extendDynamicLUT":
				extendDynamicLUT(input, 0, 255);
				break;
			case "egalisationHist":
				egalisationHistValue(input);
				break;
			case "egalisationHistGrey":
				egalisationHistGrey(input, 0, 255);
				break;
			default:
				break;
		}

		long estimatedTime = System.nanoTime() - start;

		System.out.println("Temps d'exec : " + estimatedTime);

		final String outPath = args[1];
		File path = new File(outPath);
		if (path.exists()) {
			path.delete();
		}
		ImgSaver imgSaver = new ImgSaver();
		imgSaver.saveImg(outPath, input);
		imgSaver.context().dispose();
		System.out.println("Image saved in: " + outPath);
	}

}
