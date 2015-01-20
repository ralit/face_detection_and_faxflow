package org.ralit.facedetection;

import static org.ralit.facedetection.ImageUtility.*;

import java.io.IOException;
import java.io.ObjectInputStream.GetField;

public class Main {

	private static String HOME = System.getProperty("user.home");
	
	public static void main(String[] args) throws IOException {
//		FaceDetection face = new FaceDetection(HOME + "/Desktop/Snakes/p388-nemu-yumemi.jpg",
//				48, 138, -22, -14, 11, 24);
//		
//		FaceDetection face = new FaceDetection(HOME + "/Desktop/Snakes/p388-nemu-yumemi.jpg",
//				48, 138, 106, 114, 139, 152,
//				HOME + "/Desktop/Snakes/�����O���t1.txt",
//				HOME + "/Desktop/Snakes/�����̃q�X�g�O����1.txt");
//		writeBitmap(face.getImage().p, HOME + "/Desktop/Snakes/maxflowed1.jpg");
		
		FaceDetection face = new FaceDetection(HOME + "/Desktop/Snakes/DSC_0027.JPG",
				48, 138, 106, 114, 139, 152,
				HOME + "/Desktop/Snakes/�����O���t.txt",
				HOME + "/Desktop/Snakes/�����̃q�X�g�O����.txt");
		writeBitmap(face.getImage().p, HOME + "/Desktop/Snakes/maxflowed.jpg");
		
//		FaceDetection face = new FaceDetection(HOME + "/Desktop/Snakes/p388-nemu-yumemi.jpg",
//				0, 255, 106, 114, 139, 152);
		
//		writeRects(face.getImage(), face.getRects(), HOME + "/Desktop/Snakes/face0.jpg", rgb(255, 0, 0));
//		writeRects(face.getImage(), face.getRect(), HOME + "/Desktop/Snakes/face1.jpg", rgb(0, 255, 0));
		
		System.out.println("�����");
	}
}


