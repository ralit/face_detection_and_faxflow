package org.ralit.facedetection;

class Rect {
	public int x;
	public int y;
	public int w;
	public int h;
	public Rect(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	public String toString() {
		//		return "(" + x + " - " + (x+w) + ", " + y + " - " + (y+h) + ")";
		return "point (" + x + ", " + y + "); length (" + w + ", " + h + ")";
	}
}