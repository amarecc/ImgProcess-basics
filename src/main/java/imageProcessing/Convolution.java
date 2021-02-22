package imageProcessing;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.exception.IncompatibleTypeException;
import java.io.File;

import net.imglib2.view.Views;
import net.imglib2.view.IntervalView;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.util.Intervals;
import net.imglib2.Interval;

public class Convolution {

	/**
	 * Question 1.1
	 */
	public static void meanFilterSimple(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output) {
		final int size = 3;
		final RandomAccess<UnsignedByteType> r = input.randomAccess();
		
		final RandomAccess<UnsignedByteType> rOut = output.randomAccess();

		final int iw = (int) input.max(0);
		final int ih = (int) input.max(1);
		final int padding = size / 2;

		for (int x = padding; x < iw - padding; ++x) {
			for (int y = padding; y < ih - padding; ++y) {
				int moyenne = 0;

				for (int nx = -padding; nx <= padding; nx++) {
					for (int ny = -padding; ny <= padding; ny++) {
						r.setPosition(x + nx, 0);
						r.setPosition(y + ny, 1);
						moyenne += r.get().get();
					}
				}

				moyenne /= size * size;

				rOut.setPosition(x, 0);
				rOut.setPosition(y, 1);
				rOut.get().set(moyenne);
			}
		}
	}

	/**
	 * Question 1.2
	 */
	public static void meanFilterWithBorders(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output,
			int size) {

			//Créer une vue avec size/2 de pixel en plus (prennent la valeur des pixels du bords)
			final IntervalView<UnsignedByteType> view = Views.expandBorder(input, size/2, size/2);
			final RandomAccess<UnsignedByteType> outputAccess = output.randomAccess();
			final int iw = (int) input.max(0);
			final int ih = (int) input.max(1);

			for (int x = 1; x < iw; ++x) {
				for (int y = 1; y < ih; ++y) {
					int moyenne = 0;

					//Calcul de l'interval du voisin
					RandomAccessibleInterval<UnsignedByteType> intervalNeighbord = Views.interval(view,
					new long[] { x-(size/2), y-(size/2) }, new long[] { x+(size/2), y+(size/2) });
					
					for (UnsignedByteType pixel : Views.iterable(intervalNeighbord)) {
						moyenne += pixel.get();
					}

					moyenne /= (size*size);

					outputAccess.setPosition(x, 0);
					outputAccess.setPosition(y, 1);
					outputAccess.get().set(moyenne);
				}
			}
	}

	/**
	 * Question 1.3
	 */
	public static void meanFilterWithNeighborhood(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output,
			int size) {
				final RectangleShape shape = new RectangleShape( size/2, true );
				System.out.println(input.numDimensions());
				//Créer une vue avec size/2 de pixel en plus (prennent la valeur des pixels du bords)
				final IntervalView<UnsignedByteType> view = Views.expandBorder(input, size/2, size/2);
				final RandomAccess<UnsignedByteType> outputAccess = output.randomAccess();

				//On créer un interval qui correspond à notre input original
				Interval interval = Intervals.expand( input, -size/2 );

				RandomAccessibleInterval<UnsignedByteType> source = Views.interval( view, interval );

				final Cursor<UnsignedByteType> center = Views.iterable(source).cursor();				
				
				for ( final Neighborhood<UnsignedByteType> localNeighborhood : shape.neighborhoods( source ) ) {
					center.next();
					int moyenne = 0;
					for ( final UnsignedByteType pixel : localNeighborhood ) {
						moyenne += pixel.get();
					}
					int[] pos = new int[2];
					center.localize(pos);

					moyenne /= (size * size);

					outputAccess.setPosition(pos[0], 0);
					outputAccess.setPosition(pos[1], 1);
					outputAccess.get().set(moyenne);
				}
	}

	/**
	 * Question 2.1
	 */
	public static void convolution(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output,
			int[][] kernel) {
				int size = kernel.length;
				final RectangleShape shape = new RectangleShape( size/2, false );
				//Créer une vue avec size/2 de pixel en plus (prennent la valeur des pixels du bords)
				final IntervalView<UnsignedByteType> view = Views.expandBorder(input, size/2, size/2);
				final RandomAccess<UnsignedByteType> outputAccess = output.randomAccess();

				//On créer un interval qui correspond à notre input original
				Interval interval = Intervals.expand( input, -size/2);

				RandomAccessibleInterval<UnsignedByteType> source = Views.interval( view, interval );

				final Cursor<UnsignedByteType> center = Views.iterable(source).cursor();

				int sumCoef = 0;
				for (int i = 0; i < kernel.length; i++) {
					for (int j = 0; j < kernel[i].length; j++)
						sumCoef += kernel[i][j];
				}
				if (sumCoef == 0)
					System.exit(1);
				
				for ( final Neighborhood<UnsignedByteType> localNeighborhood : shape.neighborhoods( source ) ) {
					center.next();
					int moyenne = 0;
					int i = 0;
					int j = 0;

					for ( final UnsignedByteType pixel : localNeighborhood ) {
						moyenne += pixel.get() * kernel[i][j];
						j++;
						if (j == size) {
							i++;
							j = 0;
						}
					}
					int[] pos = new int[2];
					center.localize(pos);

					moyenne /= sumCoef;

					outputAccess.setPosition(pos[0], 0);
					outputAccess.setPosition(pos[1], 1);
					outputAccess.get().set(moyenne);
				}
	}

	/**
	 * Question 2.3
	 */
	public static void gaussFilterImgLib(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output) {
		Gauss3.gauss(4.0/3.0, Views.extendBorder(input), output);
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

		String functionToTest = "convolution";

		int[][] kernel = {
			{1, 2, 3, 2, 1},
			{2, 6, 8, 6, 2},
			{3, 8, 10, 8, 3},
			{2, 6, 8, 6, 2},
			{1, 2, 3, 2, 1}
		};

		int[][] kernelMoy = {
			{1, 1, 1},
			{1, 1, 1},
			{1, 1, 1}
		};

		switch (functionToTest) {
			case "meanFilterSimple":
				meanFilterSimple(input, output);
				break;
			case "meanFilterWithBorders":
				meanFilterWithBorders(input, output, 5);
				break;
			case "meanFilterWithNeighborhood":
				meanFilterWithNeighborhood(input, output, 5);
				break;
			case "convolution":
				convolution(input, output, kernel);
				break;
			case "gaussFilterImgLib":
				gaussFilterImgLib(input, output);
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
		imgSaver.saveImg(outPath, output);
		imgSaver.context().dispose();
		System.out.println("Image saved in: " + outPath);
	}

}
