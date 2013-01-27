import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

public final class PlasmaCalculations {
	int box_w, box_h;
	private BufferedImage image;
	private double factor;
	private byte[][] distTable;
	private byte[][] sinusTable;

	public PlasmaCalculations(int width, int height) {
		super();
		this.box_w = width;
		this.box_h = height;
		init();
	}

	private void init() {
		System.out.println("pre calculate");

		this.image = new BufferedImage(box_w, box_h,
				BufferedImage.TYPE_BYTE_INDEXED);
		double metafactor = 0.4;
		factor = metafactor * 520d / (double) (box_w + box_h);
		this.distTable = calculateDistTable();
		this.sinusTable = calculateSinusTable();

	}

	private byte[][] calculateDistTable() {
		byte[][] tab = new byte[box_h][box_w];
		for (int i = 0; i < box_h; i++) {
			// tab[i] = new byte[box_w];
			for (int j = 0; j < box_w; j++) {
				int v = (int) (dist(factor, 0, 0, j, i));
				tab[i][j] = (byte) v;
			}
		}
		return tab;
	}

	private byte[][] calculateSinusTable() {
		byte[][] tab = new byte[box_h][box_w];
		for (int i = 0; i < box_h; i++) {
			// tab[i] = new byte[box_w];
			for (int j = 0; j < box_w; j++) {
				int v = (int) sindist(factor, 0, 0, j, i);
				tab[i][j] = (byte) v;
			}
		}
		return tab;
	}

	private BufferedImage genFrameImage(long millis) {
		IndexColorModel palette = ColorPalette
				.genPalette((int) (millis / 100d));
		return new BufferedImage(palette, image.getRaster(), false, null);
	}

	public BufferedImage genFrame(long millis) {
		BufferedImage frame = genFrameImage(millis);
		WritableRaster raster = frame.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData(0);
		updateData(data, millis);
		return frame;
	}

	private void updateData(byte[] data, double millis) {
		double tick = (millis / 100.0);

		double circle1 = tick * 0.085 / 6d;
		double circle2 = -tick * 0.1 / 6d;
		double circle3 = tick * .3 / 6d;
		double circle4 = -tick * .2 / 6d;
		double circle5 = tick * .4 / 6d;
		double circle6 = -tick * .15 / 6d;
		double circle7 = tick * .35 / 6d;
		double circle8 = -tick * .05 / 6d;

		double roll = tick * 5;
		double h = box_h;
		double w = box_w;
		int x1 = (int) ((w / 2d) + (w / 2d) * cos(circle3));
		int y1 = (int) ((h / 2d) + (h / 2d) * sin(circle4));
		int x2 = (int) ((w / 2d) + (w / 2d) * sin(circle1));
		int y2 = (int) ((h / 2d) + (h / 2d) * cos(circle2));
		int x3 = (int) ((w / 2d) + (w / 2d) * cos(circle5));
		int y3 = (int) ((h / 2d) + (h / 2d) * sin(circle6));
		int x4 = (int) ((w / 2d) + (w / 2d) * cos(circle7));
		int y4 = (int) ((h / 2d) + (h / 2d) * sin(circle8));

		CalculateBody2(data, x1, y1, x2, y2, x3, y3, x4, y4, (int) roll);
	}

	void CalculateBody2(byte[] body, int x1, int y1, int x2, int y2,
			int x3, int y3, int x4, int y4, int roll) {

		for (int i = 0; i < box_h; i++) {
			int k = i * box_w;
			for (int j = 0; j < box_w; j++) {
				// this is the heart of the plasma
				int d = dist2(x1, y1, j, i) + sindist2(x2, y2, j, i)
						+ sindist2(x3, y3, j, i) + sindist2(x4, y4, j, i);
				body[k + j] = (byte) d;
			}
		}
	}

	void CalculateBody(byte[] body, int x1, int y1, int x2, int y2, int x3,
			int y3, int x4, int y4, int roll) {

		for (int i = 0; i < box_h; i++) {
			int k = i * box_w;
			for (int j = 0; j < box_w; j++) {
				// this is the heart of the plasma
				double d = dist(factor, x1, y1, j, i)
						+ sindist(factor, x2, y2, j, i)
						+ sindist(factor, x3, y3, j, i)
						+ sindist(factor, x4, y4, j, i);
				body[k + j] = (byte) d;
			}
		}
	}

	private final byte sindist2(int x1, int y1, int x2, int y2) {
		return sinusTable[Math.abs(y1 - y2)][Math.abs(x1 - x2)];
	}

	private final byte dist2(int x1, int y1, int x2, int y2) {
		return distTable[Math.abs(y1 - y2)][Math.abs(x1 - x2)];
	}

	private static double sindist(double factor, double x1, double y1,
			double x2, double y2) {
		double d = dist(factor, x1, y1, x2, y2);
		return (sin(d / 9.5) + 1) * 90;
	}

	private static double dist(double factor, double x1, double y1, double x2,
			double y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		double f1 = 20/factor;
		double f2 = f1*f1;
		return factor * (sqrt(f2 + dx * dx + dy * dy) - f1);
	}

}
