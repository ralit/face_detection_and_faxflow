package org.ralit.facedetection;

public class Graph {
	public int e;
	public int h;
	public int[] c;
	public int to_s, to_t, from_s;
	public Graph(int e, int h, int[] c, int to_s, int to_t, int from_s) {
		this.e = e;
		this.h = h;
		this.c = c;
		this.to_s = to_s;
		this.to_t = to_t;
		this.from_s = from_s;
	}
	public String toString() {
		return "(e:" + e + ", h:" + h + 
				", c0:" + c[0] + ", c1:" + c[1] + ", c2:" + c[2] + ", c3:" + c[3] + ", c4:" + c[4] + ", c5:" + c[5] + ", c6:" + c[6] + ", c7:" + c[7] +  
				", to_s:" + to_s + ", to_t:" + to_t + ", from_s:" + from_s + ")";  
	}
}
