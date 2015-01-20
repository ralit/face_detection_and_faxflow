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
	
	private Image 入力画像;
	private int[] 肌色の重心配列;
	private int[] 肌色のピクセル数配列;
	private ArrayList<Rect> rects;
	private ArrayList<Rect> rect;
	private final int residual_capacity = 1000000;
	private Graph[][] graph;
	
	public FaceDetection(String 入力画像パス, int y0, int y1, int u0, int u1, int v0, int v1, String 初期グラフ出力, String 高さのヒストグラム出力) throws IOException {
		入力画像 = readFile(入力画像パス);
		
		long time0, time1;
		
		//**** 顔検出 ****//
		time0 = System.currentTimeMillis();
		肌色の重心配列 = new int[入力画像.h];
		肌色のピクセル数配列 = new int[入力画像.h];
		for(int y = 0; y < 入力画像.h; y++) {
			int 肌色座標の和 = 0;
			int 肌色座標の数 = 0;
			for(int x = 0; x < 入力画像.w; x++) {
				int YUV = rgb2ycbcr(入力画像.p[y][x]);
//				System.out.println("(" + x + ", " + y + ") : " + "(" + y(YUV) + ", " + cb(YUV) + ", " + cr(YUV) + ")");
				if(y0 <= y(YUV) && y(YUV) <= y1 && u0 <= cb(YUV) && cb(YUV) <= u1 && v0 <= cr(YUV) && cr(YUV) <= v1) {
					肌色座標の数++;
					肌色座標の和 += x;
				}
			}
			if(肌色座標の和 != 0) {
				肌色の重心配列[y] = 肌色座標の和 / 肌色座標の数;
				肌色のピクセル数配列[y] = 肌色座標の数;
			}
		}
		
		int 重み付きラインの和 = 0;
		int 重みの和 = 0;
		for(int y = 0; y < 入力画像.h; y++) {
			重み付きラインの和 += 肌色のピクセル数配列[y] * y;
			重みの和 += 肌色のピクセル数配列[y];
		}
		int 重心 = 重み付きラインの和 / 重みの和;
		System.out.println("重心: " + 重心);
		rects = new ArrayList<Rect>();
		for(int y = 0; y < 入力画像.h; y++) {
			rects.add(new Rect(肌色の重心配列[y] - 肌色のピクセル数配列[y] / 2, y, 肌色のピクセル数配列[y], 1));
		}
		
		int 検出したライン数 = 0;
		for(int y = 0; y < 入力画像.h; y++) {
			if(肌色のピクセル数配列[y] > 0) {
				検出したライン数++;
			}
		}
		int 横の長さの和 = 0;
		int 横の重心の和 = 0;
		for (int y = 重心 - 検出したライン数/16; y <= 重心 + 検出したライン数/16; y++) {
			横の長さの和 += 肌色のピクセル数配列[y];
			横の重心の和 += 肌色の重心配列[y];
		}
		int 横の長さ = 横の長さの和 / (検出したライン数/8 + 1);
		int 横の重心 = 横の重心の和 / (検出したライン数/8 + 1);
		int 縦の長さ = 横の長さ;
		rect = new ArrayList<Rect>();
		rect.add(new Rect(横の重心 - 横の長さ/2, 重心 - 縦の長さ/2, 横の長さ, 縦の長さ));
		
		int human_x_min = (横の重心 - 横の長さ/2 > 0) ? 横の重心 - 横の長さ/2 : 0;
		int human_x_MAX = (横の重心 + 横の長さ/2 < 入力画像.w - 1) ? 横の重心 + 横の長さ/2 : 入力画像.w - 1;
		int human_y_min = (重心 - 縦の長さ/2 > 0) ? 重心 - 縦の長さ/2 : 0;
		int human_y_MAX = 入力画像.h - 1;
	
		int back0_x_min = 0;
		int back0_x_MAX = 入力画像.w - 1;
		int back0_y_min = 0;
		int back0_y_MAX = (重心 - 縦の長さ*2 < 0) ? 重心 - 縦の長さ*2 : 0;
		
		int back1_x_min = 0;
		int back1_x_MAX = (横の重心 - 横の長さ*2 < 0) ? 横の重心 - 横の長さ*2 : 0;
		int back1_y_min = 0;
		int back1_y_MAX = (重心 + 縦の長さ/2 < 入力画像.h - 1) ? 重心 + 縦の長さ/2 : 入力画像.h - 1;
		
		int back2_x_min = (横の重心 + 横の長さ*2 < 入力画像.w - 1) ? 横の重心 - 横の長さ*2 : 入力画像.w - 1;
		int back2_x_MAX = 入力画像.w - 1;
		int back2_y_min = 0;
		int back2_y_MAX = (重心 + 縦の長さ/2 < 入力画像.h - 1) ? 重心 + 縦の長さ/2 : 入力画像.h - 1;
		
		time1 = System.currentTimeMillis();
		System.out.println("顔検出: " + (time1 - time0) + "ms");
		
		
		//**** MAX,min ****//
		time0 = System.currentTimeMillis();
		long MAX = 0;
		long min = Long.MAX_VALUE;
		for(int y = 1; y < 入力画像.h - 1; y++) {
			for(int x = 1; x < 入力画像.w - 1; x++) {
				for(int dy = -1; dy <= 1; dy++) {
					for(int dx = -1; dx <= 1; dx++) {
						if(dx == 0 && dy == 0) { continue; }
						int r = r(入力画像.p[y][x]);
						int g = g(入力画像.p[y][x]);
						int b = b(入力画像.p[y][x]);
						int nr = r(入力画像.p[y+dy][x+dx]);
						int ng = g(入力画像.p[y+dy][x+dx]);
						int nb = b(入力画像.p[y+dy][x+dx]);
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
		System.out.println("色差MAX,min: " + (time1 - time0) + "ms");
		
		//**** グラフ初期化 ****//
		time0 = System.currentTimeMillis();
		graph = new Graph[入力画像.h][入力画像.w];
		for(int y = 0; y < 入力画像.h; y++) {
			for(int x = 0; x < 入力画像.w; x++) {
				// 縦方向の容量計算
				int 人物, 背景;
				if(human_x_min <= x && x <= human_x_MAX && human_y_min <= y && y <= human_y_MAX) { 
					人物 = residual_capacity; 
				} else { 
					人物 = 0; 
				}
				if( back0_x_min <= x && x <= back0_x_MAX && back0_y_min <= y && y <= back0_y_MAX ||
				back1_x_min <= x && x <= back1_x_MAX && back1_y_min <= y && y <= back1_y_MAX ||
				back2_x_min <= x && x <= back2_x_MAX && back2_y_min <= y && y <= back2_y_MAX) {
					背景 = residual_capacity;
				} else { 
					背景 = 0; 
				}
				// 横方向の容量計算とグラフ初期化
				if(y == 0 && x == 0) {
//					graph[y][x] = new Graph(人物, 1, new int[]{0,0,0,0,0,0,0,0} , 人物, 背景, 人物);
					graph[y][x] = new Graph(人物, 1, new int[]{0,0,0,0,255,0,255,255} , 人物, 背景, 人物);
				} else if(y == 0 && x == 入力画像.w - 1) {
					graph[y][x] = new Graph(人物, 1, new int[]{0,0,0,255,0,255,255,0} , 人物, 背景, 人物);
				} else if(y == 入力画像.h - 1 && x == 0) {
					graph[y][x] = new Graph(人物, 1, new int[]{0,255,255,0,255,0,0,0} , 人物, 背景, 人物);
				} else if(y == 入力画像.h - 1 && x == 入力画像.w - 1) {
					graph[y][x] = new Graph(人物, 1, new int[]{255,255,0,255,0,0,0,0} , 人物, 背景, 人物);
				} else if(y == 0) {
					graph[y][x] = new Graph(人物, 1, new int[]{0,0,0,255,255,255,255,255} , 人物, 背景, 人物);
				} else if(y == 入力画像.h - 1) {
					graph[y][x] = new Graph(人物, 1, new int[]{255,255,255,255,255,0,0,0} , 人物, 背景, 人物);
				} else if(x == 0) {
					graph[y][x] = new Graph(人物, 1, new int[]{0,255,255,0,255,0,255,255} , 人物, 背景, 人物);
				} else if(x == 入力画像.w - 1) {
					graph[y][x] = new Graph(人物, 1, new int[]{255,255,0,255,0,255,255,0} , 人物, 背景, 人物);
				} else {
					int[] cf = new int[8];
					int count = 0;
					for(int dy = -1; dy <= 1; dy++) {
						for(int dx = -1; dx <= 1; dx++) {
							if(dx == 0 && dy == 0) { continue; }
							int r = r(入力画像.p[y][x]);
							int g = g(入力画像.p[y][x]);
							int b = b(入力画像.p[y][x]);
							int nr = r(入力画像.p[y+dy][x+dx]);
							int ng = g(入力画像.p[y+dy][x+dx]);
							int nb = b(入力画像.p[y+dy][x+dx]);
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
					graph[y][x] = new Graph(人物, 1, cf, 人物, 背景, 人物);
					cf = null;
				}
			}
		}
		time1 = System.currentTimeMillis();
		System.out.println("グラフ初期化: " + (time1 - time0) + "ms");
		
//		File file = new File(初期グラフ出力);
//		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//		for(int y = 0; y < 入力画像.h; y++) {
//			for(int x = 0; x < 入力画像.w; x++) {
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
				h_histogram[k] = 入力画像.w * 入力画像.h;
			} else {
				h_histogram[k] = 0;
			}
		}
		
		do {
			for(int y = 0; y < 入力画像.h; y++) {
				// 1ライン分の活性点をキューに追加
				for(int x = 0; x < 入力画像.w; x++) {
//					if(y == 0 || y == 入力画像.h - 1 || x == 0 || x == 入力画像.w - 1) { continue; }
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
							if(0 <= py+dy && py+dy <= 入力画像.h - 1 && 0 <= px+dx && px+dx <= 入力画像.w - 1) {
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
						if(0 < px+dxx && px+dxx < 入力画像.w - 1 && 0 < py+dyy && py+dyy <= 入力画像.h - 1) {
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
			// すべての活性点がなくなったかどうかチェック
			for(int y = 0; y < 入力画像.h; y++) {
				for(int x = 0; x < 入力画像.w; x++) {
					if(y == 0 || y == 入力画像.h - 1 || x == 0 || y == 入力画像.w - 1) { continue; }
					if(graph[y][x].e > 0) {
						e.add(new Point(x,y));
					}
				}
			}
		} while(!e.isEmpty());
		
		time1 = System.currentTimeMillis();
		System.out.println("maxflow: " + (time1 - time0) + "ms");
		
		File file = new File(初期グラフ出力);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for(int y = 0; y < 入力画像.h; y++) {
			for(int x = 0; x < 入力画像.w; x++) {
				writer.write(graph[y][x].h + ",");
			}
			writer.newLine();
		}
		writer.close();
		
		File file2 = new File(高さのヒストグラム出力);
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(file2));
		for(int i = 0; i < h_histogram.length; i++) {
			writer2.write(i + ": " + h_histogram[i]);
			writer2.newLine();
		}
		writer2.close();
		
		for(int dy = 0; dy < 入力画像.h; dy++) {
			for(int dx = 0; dx < 入力画像.w; dx++) {
				if(graph[dy][dx].h == 1) {
					入力画像.p[dy][dx] = rgb(0,255,0);
				}
			}
		}
		
		
	}
	
	public Graph[][] getGraph() {
		return graph;
	}
	
	public Image getImage() {
		return 入力画像;
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
