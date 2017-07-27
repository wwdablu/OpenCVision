package com.wwdablu.opencvision.vision;

import java.awt.Image;

public interface ICapture {
	
	enum CaptureAction {
		CAPTURE_STARTED,
		CAPTURE_STOPPED,
		CAPTURE_TRAINING_STARTED,
		CAPTURE_TRAINING_STOPPED,
		CAPTURE_FAILED_NO_CAMERA,
		CAPTURE_FAILED_NO_CAMERA_ACCESS
	}
	
	enum CaptureStatus {
		SUCCESS,
		ERROR_ON_START,
		ERROR_ON_STOP,
		ERROR
	}
	
	void onActionPerformed(CaptureAction action, CaptureStatus status);
	void onFrameCaptured(Image capturedImage);
}
