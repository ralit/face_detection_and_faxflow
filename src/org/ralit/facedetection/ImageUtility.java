package org.ralit.facedetection;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

public class ImageUtility{

	/**
	 * �P�x��\��byte�^�z����g���ăO���[�X�P�[���摜���i�[���Ă���Ƃ��A
	 * byte�^��-128����127�ƈ����Â炢�̂ŁA
	 * ��xint�^�ɃL���X�g���Ă���v�Z�Ɏg���܂��B
	 * ���x�ɂقƂ�ǉe���͂���܂���B
	 */
	public static final int b2i(final byte y) {
		return (int)y + 128;
	}

	/**
	 * �P�x��\��byte�^�z����g���ăO���[�X�P�[���摜���i�[����Ƃ��A
	 * byte�^��-128����127�Ȃ̂ŁAbyte�ɃL���X�g����O��128�������K�v������܂��B
	 * ���x�ɂقƂ�ǉe���͂���܂���B
	 */
	public static final byte i2b(final int y) {
		return (byte)(y - 128);
	}

	// �g���Ă��Ȃ�
	public static int argb(final int a,final int r,final int g,final int b) {
		return a<<24 | r <<16 | g <<8 | b;
	}	

	/**
	 * BufferedImage#setRGB����Ƃ��ɁA1�s�N�Z���̐F����BufferedImage#TYPE_INT_RGB�`���ɂ��邽�߂Ɏg���B
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int rgb(final int r,final int g,final int b) {
		return 0xff000000 | r <<16 | g <<8 | b;
	}

	// �g���Ă��Ȃ�
	public static int a(final int c){
		return c>>>24;
	}

	// �g���Ă��Ȃ�
	public static int r(final int c) {
		return c>>16&0xff;
	}

	// �g���Ă��Ȃ�
	public static int g(final int c) {
		return c>>8&0xff;
	}

	// �g���Ă��Ȃ�
	public static int b(final int c) {
		return c&0xff;
	}

	/**
	 * RGB��YCbCr�`���ɕϊ�
	 * @param rgb
	 * @return
	 */
	public static int rgb2ycbcr(final int rgb) {
		int r = r(rgb);
		int g = g(rgb);
		int b = b(rgb);
		int y = (int)(0.2989 * r + 0.5866 * g + 0.1145 * b);
		int cb = (int)(-0.1687 * r - 0.3312 * g + 0.5000 * b) + 128;
		int cr = (int)(0.5000 * r - 0.4183 * g - 0.0816 * b) + 128;
		return 0xff000000 | y <<16 | cb <<8 | cr;
	}

	// �g���Ă��Ȃ�
	public static int ycbcr2rgb(final int ycbcr) {
		int y = y(ycbcr);
		int cb = cb(ycbcr) - 128;
		int cr = cr(ycbcr) - 128;
		int r = (int)(y               + 1.4022 * cr);
		int g = (int)(y - 0.3456 * cb - 0.7145 * cr);
		int b = (int)(y + 1.7710 * cb              );
		return rgb(r, g, b);
	}

	// �g���Ă��Ȃ�
	public static int ycbcr(final int y, final int cb, final int cr) {
		return 0xff000000 | y <<16 | cb <<8 | cr;
	}

	/**
	 * �P�x���݂̂����o��
	 * @param ycrcb
	 * @return
	 */
	public static int y(final int ycrcb) {
		return ycrcb>>16 & 0xff;
	}

	// �g���Ă��Ȃ�
	public static int cb(final int ycrcb) {
		return ycrcb>>8 & 0xff;
	}

	// �g���Ă��Ȃ�
	public static int cr(final int ycrcb) {
		return ycrcb & 0xff;
	}

	
	public static void writeBitmap(final int[][] RGB, final String outputFilePath) throws IOException {
		int h = RGB.length;
		int w = RGB[0].length;
		BufferedImage write = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				write.setRGB(x, y, RGB[y][x]);
			}
		}
		File file = new File(outputFilePath);
		file.mkdirs();
		ImageIO.write(write, "jpg", file);
	}
	
	/**
	 * �P�x���݂̂̔z�񂩂�摜�������o��
	 * @param gray byte[][]�z��ł��邱�Ƃɒ���
	 * @param outputFilePath
	 * @throws IOException
	 */
	public static void writeGrayBitmap(final byte[][] gray, final String outputFilePath) throws IOException {
		int h = gray.length;
		int w = gray[0].length;
		BufferedImage write = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				write.setRGB(x, y, rgb(b2i(gray[y][x]), b2i(gray[y][x]), b2i(gray[y][x])));
			}
		}
		File file = new File(outputFilePath);
		file.mkdirs();
		ImageIO.write(write, "jpg", file);
	}

	/**
	 * 2�l�摜�������o��
	 * @param edged boolean[][]�z��ł��邱�Ƃɒ���
	 * @param outputFilePath
	 * @throws IOException
	 */
	public static void writeBooleanBitmap(final boolean[][] edged, final String outputFilePath) throws IOException {
		int h = edged.length;
		int w = edged[0].length;
		BufferedImage write = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if(edged[y][x]) {
					write.setRGB(x, y, rgb(0xff, 0xff, 0xff));
				} else {
					write.setRGB(x, y, rgb(0x00, 0x00, 0x00));
				}
			}
		}
		File file = new File(outputFilePath);
		file.mkdirs();
		ImageIO.write(write, "jpg", file);
	}

	/**
	 * �摜�ɋ�`��`���ďo�͂���(�摜�̓O���[�X�P�[��)�B
	 * @param gray
	 * @param cols
	 * @param outputFilePath
	 * @throws IOException
	 */
	public static void writeRects(final Image image, final ArrayList<Rect> rects, final String outputFilePath, int color) throws IOException {
		int h = image.h;
		int w = image.w;

		int[][] mutableRGB = new int[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				mutableRGB[y][x] = image.p[y][x];
			}
		}


		for (Rect rect : rects) {
			for (int y = rect.y; y < rect.y + rect.h; y++) {
				for (int x = rect.x; x < rect.x + rect.w; x++) {
					if ((y == rect.y || y == rect.y + rect.h - 1) || (x == rect.x || x == rect.x + rect.w - 1)) {
						mutableRGB[y][x] = color;
					}
				}
			}
		}


		BufferedImage write = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				write.setRGB(x, y, mutableRGB[y][x]);
			}
		}
		File file = new File(outputFilePath);
		file.mkdirs();
		ImageIO.write(write, "jpg", file);
	}

	/**
	 * ���l�̕��z���������邽�߂Ɏg���B
	 * @param connection
	 * @param outputFilePath
	 * @throws IOException
	 */
	public static void writeSizeDistribution(final ArrayList<ArrayList<Rect>> connection, final String outputFilePath) throws IOException {
		int max = 0;
		for (int i = 0; i < connection.size(); i++) {
			if(connection.get(i).size() > max) { max = connection.get(i).size(); }
		}
		BufferedImage connectionImg = new BufferedImage(max+1, connection.size(), BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < connection.size(); i++) {
			connectionImg.setRGB(connection.get(i).size(), i, rgb(0x00, 0xff, 0x00));
		}
		File file = new File(outputFilePath);
		file.mkdirs();
		ImageIO.write(connectionImg, "png", file);
	}

	/**
	 * �摜����e��`�̕�����؂�o��(�A�Ԃ̉摜�ɂȂ�)�B
	 * @param bmp
	 * @param lineRectList
	 * @param outputDirPath
	 * @throws IOException
	 */
	public static void writeEachRect(final byte[][] gray, ArrayList<Rect> lineRectList, String outputDirPath) throws IOException {
		for (int i = 0; i < lineRectList.size(); i++) {
			Rect lineRect = lineRectList.get(i);
			BufferedImage write = new BufferedImage(lineRect.w, lineRect.h, BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < lineRect.h; y++) {
				for (int x = 0; x < lineRect.w; x++) {
					write.setRGB(x, y, rgb(b2i(gray[y + lineRect.y][x + lineRect.x]), b2i(gray[y + lineRect.y][x + lineRect.x]), b2i(gray[y + lineRect.y][x + lineRect.x])));
				}
			}
			File file = new File(outputDirPath + i + ".jpg");
			file.mkdirs();
			ImageIO.write(write, "jpg", file);
		}
	}

	/**
	 * �摜�ɓ_��`���ďo�͂���(�摜�̓O���[�X�P�[��)�B
	 * @param gray
	 * @param cols
	 * @param outputFilePath
	 * @throws IOException
	 */
	public static void writePoints(final byte[][] gray, final Point[] v, final String outputFilePath, int color) throws IOException {
		int h = gray.length;
		int w = gray[0].length;

		int[][] mutableRGB = new int[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				mutableRGB[y][x] = rgb(b2i(gray[y][x]), b2i(gray[y][x]), b2i(gray[y][x]));
			}
		}

		for (Point point : v) {
			mutableRGB[Math.max(0, point.y - 1)][Math.max(0, point.x - 1)] = color;
			mutableRGB[Math.max(0, point.y - 1)][point.x] = color;
			mutableRGB[Math.max(0, point.y - 1)][Math.min(point.x + 1, w-1)] = color;
			mutableRGB[point.y][Math.max(0, point.x - 1)] = color;
			mutableRGB[point.y][point.x] = color;
			mutableRGB[point.y][Math.min(point.x + 1, w-1)] = color;
			mutableRGB[Math.min(h-1, point.y + 1)][Math.max(0, point.x - 1)] = color;
			mutableRGB[Math.min(h-1, point.y + 1)][point.x] = color;
			mutableRGB[Math.min(h-1, point.y + 1)][Math.min(point.x + 1, w-1)] = color;
		}

		BufferedImage write = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				write.setRGB(x, y, mutableRGB[y][x]);
			}
		}
		File file = new File(outputFilePath);
		file.mkdirs();
		ImageIO.write(write, "png", file);
		mutableRGB = null;
		write = null;
		file = null;
	}

	/**
	 * �摜�ɓ_��`���ďo�͂���(�摜�̓O���[�X�P�[��)�B
	 * @param gray
	 * @param cols
	 * @param outputFilePath
	 * @throws IOException
	 */
	public static void writeHistogramPoints(final int[][][] histogram, final String outputDir) throws IOException {

		int max = 0;
		for(int b = 0; b < 32; b++) {
			for(int g = 0; g < 32; g++) {
				for(int r = 0; r < 32; r++) {
					if(max < histogram[b][g][r]) {
						max = histogram[b][g][r];
					}
				}
			}
		}
		System.out.println("max: " + max);

		for(int b = 0; b < 32; b++) {
			int[][] mutableRGB = new int[32][32];
			for (int y = 0; y < 32; y++) {
				for (int x = 0; x < 32; x++) {
					mutableRGB[y][x] = rgb(255, 255, 255);
				}
			}

			ArrayList<Point> v0 = new ArrayList<Point>();
			ArrayList<Integer> intensity = new ArrayList<Integer>(); 
			for(int g = 0; g < 32; g++) {
				for(int r = 0; r < 32; r++) {
					if(histogram[b][g][r] != 0) {
						v0.add(new Point(r, g));
						intensity.add(histogram[b][g][r]);
					}
				}
			}

			Point[] v = new Point[v0.size()];
			for(int i = 0; i < v0.size(); i++) {
				v[i] = v0.get(i);
			}

			//			int color = rgb(intensity.get(index) / 256, 0, 256 - histogram[b][g][r]);
			int w = 32;
			int h = 32;
			//			for (Point point : v) {
			////				mutableRGB[Math.max(0, point.y - 1)][Math.max(0, point.x - 1)] = color;
			////				mutableRGB[Math.max(0, point.y - 1)][point.x] = color;
			////				mutableRGB[Math.max(0, point.y - 1)][Math.min(point.x + 1, w-1)] = color;
			////				mutableRGB[point.y][Math.max(0, point.x - 1)] = color;
			//				mutableRGB[point.y][point.x] = color;
			////				mutableRGB[point.y][Math.min(point.x + 1, w-1)] = color;
			////				mutableRGB[Math.min(h-1, point.y + 1)][Math.max(0, point.x - 1)] = color;
			////				mutableRGB[Math.min(h-1, point.y + 1)][point.x] = color;
			////				mutableRGB[Math.min(h-1, point.y + 1)][Math.min(point.x + 1, w-1)] = color;
			//			}
			for(int i = 0; i < v.length; i++) {
				int color = rgb(intensity.get(i) / 256, 0, 256 - intensity.get(i));
				mutableRGB[v[i].y][v[i].x] = color;
			}

			BufferedImage write = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					write.setRGB(x, y, mutableRGB[y][x]);
				}
			}
			File file = new File(outputDir + b + ".png");
			file.mkdirs();
			ImageIO.write(write, "png", file);
			mutableRGB = null;
			write = null;
			file = null;

		}
	}
}

