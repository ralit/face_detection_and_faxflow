package org.ralit.facedetection;

import static org.ralit.facedetection.ImageUtility.*;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class FaceDetection {
	
	private Image ���͉摜;
	private int[] ���F�̏d�S�z��;
	private int[] ���F�̃s�N�Z�����z��;
	private ArrayList<Rect> rects;
	private ArrayList<Rect> rect;
	private final int residual_capacity = 1000000;
	private Graph[][] graph;
	
	public FaceDetection(String ���͉摜�p�X, int y0, int y1, int u0, int u1, int v0, int v1, String �����O���t�o��, String �����̃q�X�g�O�����o��) throws IOException {
		���͉摜 = readFile(���͉摜�p�X);
		
		long time0, time1;
		
		//**** �猟�o ****//
		time0 = System.currentTimeMillis();
		���F�̏d�S�z�� = new int[���͉摜.h];
		���F�̃s�N�Z�����z�� = new int[���͉摜.h];
		for(int y = 0; y < ���͉摜.h; y++) {
			int ���F���W�̘a = 0;
			int ���F���W�̐� = 0;
			for(int x = 0; x < ���͉摜.w; x++) {
				int YUV = rgb2ycbcr(���͉摜.p[y][x]);
//				System.out.println("(" + x + ", " + y + ") : " + "(" + y(YUV) + ", " + cb(YUV) + ", " + cr(YUV) + ")");
				if(y0 <= y(YUV) && y(YUV) <= y1 && u0 <= cb(YUV) && cb(YUV) <= u1 && v0 <= cr(YUV) && cr(YUV) <= v1) {
					���F���W�̐�++;
					���F���W�̘a += x;
				}
			}
			if(���F���W�̘a != 0) {
				���F�̏d�S�z��[y] = ���F���W�̘a / ���F���W�̐�;
				���F�̃s�N�Z�����z��[y] = ���F���W�̐�;
			}
		}
		
		int �d�ݕt�����C���̘a = 0;
		int �d�݂̘a = 0;
		for(int y = 0; y < ���͉摜.h; y++) {
			�d�ݕt�����C���̘a += ���F�̃s�N�Z�����z��[y] * y;
			�d�݂̘a += ���F�̃s�N�Z�����z��[y];
		}
		int �d�S = �d�ݕt�����C���̘a / �d�݂̘a;
		System.out.println("�d�S: " + �d�S);
		rects = new ArrayList<Rect>();
		for(int y = 0; y < ���͉摜.h; y++) {
			rects.add(new Rect(���F�̏d�S�z��[y] - ���F�̃s�N�Z�����z��[y] / 2, y, ���F�̃s�N�Z�����z��[y], 1));
		}
		
		int ���o�������C���� = 0;
		for(int y = 0; y < ���͉摜.h; y++) {
			if(���F�̃s�N�Z�����z��[y] > 0) {
				���o�������C����++;
			}
		}
		int ���̒����̘a = 0;
		int ���̏d�S�̘a = 0;
		for (int y = �d�S - ���o�������C����/16; y <= �d�S + ���o�������C����/16; y++) {
			���̒����̘a += ���F�̃s�N�Z�����z��[y];
			���̏d�S�̘a += ���F�̏d�S�z��[y];
		}
		int ���̒��� = ���̒����̘a / (���o�������C����/8 + 1);
		int ���̏d�S = ���̏d�S�̘a / (���o�������C����/8 + 1);
		int �c�̒��� = ���̒���;
		rect = new ArrayList<Rect>();
		rect.add(new Rect(���̏d�S - ���̒���/2, �d�S - �c�̒���/2, ���̒���, �c�̒���));
		
		int human_x_min = (���̏d�S - ���̒���/2 > 0) ? ���̏d�S - ���̒���/2 : 0;
		int human_x_MAX = (���̏d�S + ���̒���/2 < ���͉摜.w - 1) ? ���̏d�S + ���̒���/2 : ���͉摜.w - 1;
		int human_y_min = (�d�S - �c�̒���/2 > 0) ? �d�S - �c�̒���/2 : 0;
		int human_y_MAX = ���͉摜.h - 1;
	
		int back0_x_min = 0;
		int back0_x_MAX = ���͉摜.w - 1;
		int back0_y_min = 0;
		int back0_y_MAX = (�d�S - �c�̒���*2 < 0) ? �d�S - �c�̒���*2 : 0;
		
		int back1_x_min = 0;
		int back1_x_MAX = (���̏d�S - ���̒���*2 < 0) ? ���̏d�S - ���̒���*2 : 0;
		int back1_y_min = 0;
		int back1_y_MAX = (�d�S + �c�̒���/2 < ���͉摜.h - 1) ? �d�S + �c�̒���/2 : ���͉摜.h - 1;
		
		int back2_x_min = (���̏d�S + ���̒���*2 < ���͉摜.w - 1) ? ���̏d�S - ���̒���*2 : ���͉摜.w - 1;
		int back2_x_MAX = ���͉摜.w - 1;
		int back2_y_min = 0;
		int back2_y_MAX = (�d�S + �c�̒���/2 < ���͉摜.h - 1) ? �d�S + �c�̒���/2 : ���͉摜.h - 1;
		
		time1 = System.currentTimeMillis();
		System.out.println("�猟�o: " + (time1 - time0) + "ms");
		
		
		//**** MAX,min ****//
		time0 = System.currentTimeMillis();
		long MAX = 0;
		long min = Long.MAX_VALUE;
		for(int y = 1; y < ���͉摜.h - 1; y++) {
			for(int x = 1; x < ���͉摜.w - 1; x++) {
				for(int dy = -1; dy <= 1; dy++) {
					for(int dx = -1; dx <= 1; dx++) {
						if(dx == 0 && dy == 0) { continue; }
						int r = r(���͉摜.p[y][x]);
						int g = g(���͉摜.p[y][x]);
						int b = b(���͉摜.p[y][x]);
						int nr = r(���͉摜.p[y+dy][x+dx]);
						int ng = g(���͉摜.p[y+dy][x+dx]);
						int nb = b(���͉摜.p[y+dy][x+dx]);
						if(MAX < (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb)) {
							MAX = (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb);
						}
						if(min > (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb)) {
							min = (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb);
						}
					}
				}
			}
		}
		long diff = (MAX - min) / 256;
		time1 = System.currentTimeMillis();
		System.out.println("�F��MAX,min: " + (time1 - time0) + "ms");
		
		//**** �O���t������ ****//
		time0 = System.currentTimeMillis();
		graph = new Graph[���͉摜.h][���͉摜.w];
		for(int y = 0; y < ���͉摜.h; y++) {
			for(int x = 0; x < ���͉摜.w; x++) {
				// �c�����̗e�ʌv�Z
				int �l��, �w�i;
				if(human_x_min <= x && x <= human_x_MAX && human_y_min <= y && y <= human_y_MAX) { 
					�l�� = residual_capacity; 
				} else { 
					�l�� = 0; 
				}
				if( back0_x_min <= x && x <= back0_x_MAX && back0_y_min <= y && y <= back0_y_MAX ||
				back1_x_min <= x && x <= back1_x_MAX && back1_y_min <= y && y <= back1_y_MAX ||
				back2_x_min <= x && x <= back2_x_MAX && back2_y_min <= y && y <= back2_y_MAX) {
					�w�i = residual_capacity;
				} else { 
					�w�i = 0; 
				}
				// �������̗e�ʌv�Z�ƃO���t������
				if(y == 0 && x == 0) {
//					graph[y][x] = new Graph(�l��, 1, new int[]{0,0,0,0,0,0,0,0} , �l��, �w�i, �l��);
					graph[y][x] = new Graph(�l��, 1, new int[]{0,0,0,0,255,0,255,255} , �l��, �w�i, �l��);
				} else if(y == 0 && x == ���͉摜.w - 1) {
					graph[y][x] = new Graph(�l��, 1, new int[]{0,0,0,255,0,255,255,0} , �l��, �w�i, �l��);
				} else if(y == ���͉摜.h - 1 && x == 0) {
					graph[y][x] = new Graph(�l��, 1, new int[]{0,255,255,0,255,0,0,0} , �l��, �w�i, �l��);
				} else if(y == ���͉摜.h - 1 && x == ���͉摜.w - 1) {
					graph[y][x] = new Graph(�l��, 1, new int[]{255,255,0,255,0,0,0,0} , �l��, �w�i, �l��);
				} else if(y == 0) {
					graph[y][x] = new Graph(�l��, 1, new int[]{0,0,0,255,255,255,255,255} , �l��, �w�i, �l��);
				} else if(y == ���͉摜.h - 1) {
					graph[y][x] = new Graph(�l��, 1, new int[]{255,255,255,255,255,0,0,0} , �l��, �w�i, �l��);
				} else if(x == 0) {
					graph[y][x] = new Graph(�l��, 1, new int[]{0,255,255,0,255,0,255,255} , �l��, �w�i, �l��);
				} else if(x == ���͉摜.w - 1) {
					graph[y][x] = new Graph(�l��, 1, new int[]{255,255,0,255,0,255,255,0} , �l��, �w�i, �l��);
				} else {
					int[] cf = new int[8];
					int count = 0;
					for(int dy = -1; dy <= 1; dy++) {
						for(int dx = -1; dx <= 1; dx++) {
							if(dx == 0 && dy == 0) { continue; }
							int r = r(���͉摜.p[y][x]);
							int g = g(���͉摜.p[y][x]);
							int b = b(���͉摜.p[y][x]);
							int nr = r(���͉摜.p[y+dy][x+dx]);
							int ng = g(���͉摜.p[y+dy][x+dx]);
							int nb = b(���͉摜.p[y+dy][x+dx]);
							if(min + diff * 1 > (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb)) {
								cf[count] = 255;
							} else if(min + diff * 2 > (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb)) {
								cf[count] = 127;
							} else if(min + diff * 4 > (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb)) {
								cf[count] = 63;
							} else if(min + diff * 8 > (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb)) {
								cf[count] = 31;
							} else if(min + diff * 16 > (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb)) {
								cf[count] = 15;
							} else if(min + diff * 32 > (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb)) {
								cf[count] = 7;
							} else if(min + diff * 64 > (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb)) {
								cf[count] = 3;
							} else if(min + diff * 128 > (r-nr)*(r-nr) + (g-ng)*(g-ng) + (b-nb)*(b-nb)) {
								cf[count] = 1;
							} else {
								cf[count] = 0;
							}
							count++;
						}
					}
					graph[y][x] = new Graph(�l��, 1, cf, �l��, �w�i, �l��);
					cf = null;
				}
			}
		}
		time1 = System.currentTimeMillis();
		System.out.println("�O���t������: " + (time1 - time0) + "ms");
		
//		File file = new File(�����O���t�o��);
//		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//		for(int y = 0; y < ���͉摜.h; y++) {
//			for(int x = 0; x < ���͉摜.w; x++) {
//				writer.write(graph[y][x].toString());
//				writer.newLine();
//			}
//		}
//		writer.close();
	
		
		
		//**** maxflow ****//
		time0 = System.currentTimeMillis();
//		ArrayList<Point> e = new ArrayList<Point>();
		LinkedList<Point> e = new LinkedList<Point>();
		int[] h_histogram = new int[1024];
		for(int k = 0; k < 1024; k++) {
			if(k == 0) {
				h_histogram[k] = 1;
			} else if(k == 800) {
				h_histogram[k] = 1;
			} else if(k == 1) {
				h_histogram[k] = ���͉摜.w * ���͉摜.h;
			} else {
				h_histogram[k] = 0;
			}
		}
		
		do {
			for(int y = 0; y < ���͉摜.h; y++) {
				// 1���C�����̊����_���L���[�ɒǉ�
				for(int x = 0; x < ���͉摜.w; x++) {
//					if(y == 0 || y == ���͉摜.h - 1 || x == 0 || x == ���͉摜.w - 1) { continue; }
					if(graph[y][x].e > 0) {
						if(y != 324) {
							System.out.println("add");
						}
						e.add(new Point(x,y));
					}
				}
//				int test_count = 0;
				while(!e.isEmpty()) {
//					if(y != 324) {
//						System.out.println("while");
//					}
					Point point = e.removeFirst();
					int px = point.x;
					int py = point.y;
					int min_h = Integer.MAX_VALUE;
					int dxx = 0, dyy = 0;
					int count = 0, m = 0;
					for(int dx = -1; dx <= 1; dx++) {
						for(int dy = -1; dy <= 1; dy++) {
							if(dx==0 && dy==0) { continue; }
							if(0 <= py+dy && py+dy <= ���͉摜.h - 1 && 0 <= px+dx && px+dx <= ���͉摜.w - 1) {
								if(graph[py+dy][px+dx].h < min_h) {
									min_h = graph[py+dy][px+dx].h;
									dxx = dx;
									dyy = dy;
									m = count;
								}
							}
							count++;
						}
					}
					if(graph[py][px].h <= graph[py+dyy][px+dxx].h) {
						h_histogram[graph[py][px].h]--;
						graph[py][px].h = graph[py+dyy][px+dxx].h + 1;
						h_histogram[graph[py][px].h]++;
					}
//					int f = Math.min(graph[py][px].e, graph[py][px].c[m]);
					int f = graph[py][px].e;
					graph[py][px].e -= f;
					graph[py][px].c[m] -= f;
					graph[py+dyy][px+dxx].e += f;
					graph[py+dyy][px+dxx].c[7-m] += f;
					int min_zero_h = 0;
					for(int k = 0; k < 1024; k++) {
						if(h_histogram[k] == 0) {
							min_zero_h = k;
							break;
						}
					}
					if(graph[py][px].h > min_zero_h) {
						h_histogram[graph[py][px].h]--;
						graph[py][px].h = Math.min(graph[py][px].h, 801);
						h_histogram[graph[py][px].h]++;
					}
					if(graph[py][px].e > 0) {
						e.add(point);
					}
					if(graph[py+dyy][px+dxx].e > 0) {
						if(0 < px+dxx && px+dxx < ���͉摜.w - 1 && 0 < py+dyy && py+dyy <= ���͉摜.h - 1) {
							e.add(new Point(px+dxx, py+dyy));	
						}
					}
					
					
//					for(int i = 0; i < e.size(); i++) {
//						System.out.print("(" + e.get(i).x + "," + e.get(i).y + ")");
//					}
//					System.out.println("");			
					
			
					
//					System.out.println("test_count: " + test_count);
//					test_count++;
				}
				System.out.println("y = " + y);
			}
			// ���ׂĂ̊����_���Ȃ��Ȃ������ǂ����`�F�b�N
			for(int y = 0; y < ���͉摜.h; y++) {
				for(int x = 0; x < ���͉摜.w; x++) {
					if(y == 0 || y == ���͉摜.h - 1 || x == 0 || y == ���͉摜.w - 1) { continue; }
					if(graph[y][x].e > 0) {
						e.add(new Point(x,y));
					}
				}
			}
		} while(!e.isEmpty());
		
		time1 = System.currentTimeMillis();
		System.out.println("maxflow: " + (time1 - time0) + "ms");
		
		File file = new File(�����O���t�o��);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for(int y = 0; y < ���͉摜.h; y++) {
			for(int x = 0; x < ���͉摜.w; x++) {
				writer.write(graph[y][x].h + ",");
			}
			writer.newLine();
		}
		writer.close();
		
		File file2 = new File(�����̃q�X�g�O�����o��);
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(file2));
		for(int i = 0; i < h_histogram.length; i++) {
			writer2.write(i + ": " + h_histogram[i]);
			writer2.newLine();
		}
		writer2.close();
		
		for(int dy = 0; dy < ���͉摜.h; dy++) {
			for(int dx = 0; dx < ���͉摜.w; dx++) {
				if(graph[dy][dx].h == 1) {
					���͉摜.p[dy][dx] = rgb(0,255,0);
				}
			}
		}
		
		
	}
	
	public Graph[][] getGraph() {
		return graph;
	}
	
	public Image getImage() {
		return ���͉摜;
	}
	
	public ArrayList<Rect> getRects() {
		return rects;
	}
	
	public ArrayList<Rect> getRect() {
		return rect;
	}
	
	private Image readFile(String file) throws IOException {
		BufferedImage read = ImageIO.read(new File(file));
		int w = read.getWidth();
		int h = read.getHeight();
		int[][] RGB = new int[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				RGB[y][x] = read.getRGB(x, y);
			}
		}
		read = null;
		return new Image(w, h, RGB);
	}
	

}
