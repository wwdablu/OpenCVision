package com.wwdablu.opencvision;

import org.opencv.core.Core;

public class OpenCVision {
	
	public static void main(String[] argv) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		CVisionWindow.getInstance().launchCVision();
	}
}
