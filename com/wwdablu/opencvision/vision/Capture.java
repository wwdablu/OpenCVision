package com.wwdablu.opencvision.vision;

import java.awt.Image;
import java.io.File;
import java.io.FilenameFilter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.face.BasicFaceRecognizer;
import org.opencv.face.Face;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import com.wwdablu.opencvision.utils.CVisionUtils;
import com.wwdablu.opencvision.vision.ICapture.CaptureAction;
import com.wwdablu.opencvision.vision.ICapture.CaptureStatus;

public class Capture {
	
	private static int CAPTURE_INTERVAL = 100;
	
	private static Capture mInstance;
	
	private boolean mIsCameraActive;
	private ScheduledExecutorService mRefreshTimer;
	private int absoluteFaceSize;
	private int mTrainCaptureIndex = 0;
	public HashMap<Integer, String> names = new HashMap<Integer, String>();
	
	private VideoCapture mVideoCapture;
	private CascadeClassifier mFaceClassifier;
	private BasicFaceRecognizer mFaceRecognizer;
	
	private ICapture mCallbackInterface;
	
	private Capture(ICapture callbackInterface) {
		
		mIsCameraActive = false;
		mVideoCapture = new VideoCapture();
		mCallbackInterface = callbackInterface;
		
		mFaceClassifier = new CascadeClassifier();
		
		//LBP Cascade (Less accurate but fast)
		mFaceClassifier.load("resources/lbpcascades/lbpcascade_frontalface.xml");
		
		//HAAR Cascade (More accurate but slower)
		//mFaceClassifier.load("resources/haarcascades/haarcascade_frontalface_alt.xml");
		
		mFaceRecognizer = Face.createFisherFaceRecognizer(0, 1500);
		
		//Load the train data if it exists, else ignore
		File trainData = new File("resources/train");
		if(0 != trainData.list().length) {
			trainFaceRecognizer();
		}
	}
	
	public static synchronized Capture getInstance(ICapture callbackInterface) throws InvalidParameterException {
		
		if(null != mInstance) {
			return mInstance;
		}
		
		if(null == callbackInterface) {
			throw new InvalidParameterException("Must implement ICapture interface.");
		}
		
		mInstance = new Capture(callbackInterface);
		return mInstance;
	}
	
	public boolean isCaptureActive() {
		return mIsCameraActive;
	}
	
	public void stopCapture() {
		
		mIsCameraActive = false;
		
		if (null != mRefreshTimer && !mRefreshTimer.isShutdown()) {
			
			try {
				mRefreshTimer.shutdown();
				mRefreshTimer.awaitTermination(CAPTURE_INTERVAL, TimeUnit.MILLISECONDS);
				
			} catch (InterruptedException e) {
				System.out.println("Could not stop the refresh timer. Message: " + e.getMessage());
			}
		}
		
		if (mVideoCapture.isOpened()) {
			mVideoCapture.release();
		}
		
		mCallbackInterface.onActionPerformed(CaptureAction.CAPTURE_STOPPED, CaptureStatus.SUCCESS);
	}
	
	public void startCapture(final boolean isTraining, final String trainingUsername) {
		
		mVideoCapture.open(0);
		
		if (mVideoCapture.isOpened()) {
			
			mIsCameraActive = true;
			
			Runnable frameExtraction = new Runnable() {
				
				@Override
				public void run() {

					Mat frame = extractFrame(isTraining, trainingUsername);
					Image capturedImage = CVisionUtils.matToImage(frame);
					frame.release();
					mCallbackInterface.onFrameCaptured(capturedImage);
				}
			};
			
			if(isTraining) {
				CAPTURE_INTERVAL = 500;
			} else {
				CAPTURE_INTERVAL = 250;
			}
			
			mRefreshTimer = Executors.newSingleThreadScheduledExecutor();
			mRefreshTimer.scheduleAtFixedRate(frameExtraction, 0, CAPTURE_INTERVAL, TimeUnit.MILLISECONDS);
			
			mCallbackInterface.onActionPerformed(CaptureAction.CAPTURE_STARTED, CaptureStatus.SUCCESS);
			
		} else {
			System.out.println("Unable to open the camera");
			mCallbackInterface.onActionPerformed(CaptureAction.CAPTURE_FAILED_NO_CAMERA, CaptureStatus.ERROR);
		}
	}
	
	/*
	 * 
	 * PRIVATE FUNCTIONS
	 * 
	 */
	
	private Mat extractFrame(boolean isTraining, String trainingUsername) {
		
		Mat inputFrame = new Mat();
		
		if (mVideoCapture.isOpened()) {
			
			try {

				mVideoCapture.read(inputFrame);
				
				//Convert it to gray scale. We are going to use gray scale images for functioning.
				if (!inputFrame.empty()) {
					detectAndDrawFaces(inputFrame, isTraining, trainingUsername);
				}
				
			} catch (Exception e) {
				System.out.println("Error while extracting from the frame. Message: " + e.getMessage());
			}
		}
		
		return inputFrame;
	}
	
	private void detectAndDrawFaces(Mat frame, boolean isTraining, String trainingUsername) {
	
		MatOfRect faces = new MatOfRect();
		Mat greyFrame = new Mat();
		Mat greyEqualizedFrame = new Mat();
		
		Imgproc.cvtColor(frame, greyFrame, Imgproc.COLOR_BGR2GRAY);
		
		Imgproc.equalizeHist(greyFrame, greyEqualizedFrame);
		
		if (absoluteFaceSize == 0) {
			
			int height = greyEqualizedFrame.rows();
			if (Math.round(height * 0.2f) > 0) {
				absoluteFaceSize = Math.round(height * 0.2f);
			}
		}
		
		//This will try to detect all the faces found in the frame
		mFaceClassifier.detectMultiScale(greyEqualizedFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
				
		//Draw a rectangular frame on all the faces
		Rect[] facesArray = faces.toArray();
		for (int i = 0; i < facesArray.length; i++) {
			
			Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
			
			if(isTraining) {
				saveTrainImages(
						frame,
						new Rect(facesArray[i].tl(), facesArray[i].br()),
						trainingUsername
				);
				
			} else {
				
				Mat croppedImage = new Mat(frame, new Rect(facesArray[i].tl(), facesArray[i].br()));

				Imgproc.cvtColor(croppedImage, croppedImage, Imgproc.COLOR_BGR2GRAY);

				Imgproc.equalizeHist(croppedImage, croppedImage);

				Mat resizeImage = new Mat();
				Size size = new Size(250,250);
				Imgproc.resize(croppedImage, resizeImage, size);
				
				double[] returnedResults = faceRecognition(resizeImage);
				double prediction = returnedResults[0];
				//double confidence = returnedResults[1];
				
				System.out.println("Prediction is: " + prediction);
				int label = (int) prediction;
				String name = "";
				if (names.containsKey(label)) {
					name = names.get(label);
				} else {
					name = "Unidentified";
				}
				
				String box_text = "Prediction = " + name;

	            double pos_x = Math.max(facesArray[i].tl().x - 10, 0);
	            double pos_y = Math.max(facesArray[i].tl().y - 10, 0);

	            Imgproc.putText(frame, box_text, new Point(pos_x, pos_y), 
	            		Core.FONT_HERSHEY_COMPLEX, 1.0, new Scalar(0, 0, 255, 2.0));
			}
		}
		
		faces.release();
		greyFrame.release();
		greyEqualizedFrame.release();
	}
	
	private double[] faceRecognition(Mat currentFace) {
    	
    	int[] predLabel = new int[1];
        double[] confidence = new double[1];
        int result = -1;
        
    	mFaceRecognizer.predict(currentFace,predLabel,confidence);
    	result = mFaceRecognizer.predict_label(currentFace);
    	result = predLabel[0];
    	
    	return new double[] {result,confidence[0]};
	}
	
	private void saveTrainImages(Mat frame, Rect faceRect, String trainingUsername) {
		
		Mat croppedImage = new Mat(frame, faceRect);

		Imgproc.cvtColor(croppedImage, croppedImage, Imgproc.COLOR_BGR2GRAY);

		Imgproc.equalizeHist(croppedImage, croppedImage);

		Mat resizeImage = new Mat();
		Size size = new Size(250,250);
		Imgproc.resize(croppedImage, resizeImage, size);
		
		if(mTrainCaptureIndex <= 20) {
			
			Imgcodecs.imwrite("resources/train/" +
					0 + "-" + trainingUsername + "_" + (mTrainCaptureIndex++) + ".png", resizeImage);
			
		} else if (mTrainCaptureIndex >= 21) {
			stopCapture();
			trainFaceRecognizer();
		}
	}
	
	private void trainFaceRecognizer() {
		
		File root = new File("resources/train/");
		
		FilenameFilter imgFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
                return name.endsWith(".png");
			}
        };
        
        File[] imageFiles = root.listFiles(imgFilter);
        
        //If no training images are found, then return
        if(0 == imageFiles.length) {
        	return;
        }
        
        ArrayList<Mat> images = new ArrayList<Mat>();
        Mat labels = new Mat(imageFiles.length, 1, CvType.CV_32SC1);
        
        int counter = 0;
        
        for (File image : imageFiles) {

        	Mat img = Imgcodecs.imread(image.getAbsolutePath());

        	Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        	Imgproc.equalizeHist(img, img);

        	int label = Integer.parseInt(image.getName().split("\\-")[0]);

        	String labnname = image.getName().split("\\_")[0];
        	String name = labnname.split("\\-")[1];
        	names.put(label, name);

        	images.add(img);

        	labels.put(counter++, 0, label);
        }
        
        mFaceRecognizer.train(images, labels);
        
        /*
        for (File image : imageFiles) {
        	image.delete();
        }
        */
	}
}
