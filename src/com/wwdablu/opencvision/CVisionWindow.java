package com.wwdablu.opencvision;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.wwdablu.opencvision.vision.Capture;
import com.wwdablu.opencvision.vision.ICapture;

public class CVisionWindow implements ICapture {
	
	private static CVisionWindow mInstance;
	
	private Capture mCapture;
	
	private JFrame mWindow;
	private JRadioButton mModeTrainUser;
	private JRadioButton mModeDetectUser;
	private JTextField mTrainUsername;
	private JLabel mCaptureImage;
	private JButton mActionButton;
	
	private CVisionWindow() {
		
		mWindow = null;
		mCapture = Capture.getInstance(this);
	}
	
	public static synchronized CVisionWindow getInstance() {
		
		if(null != mInstance) {
			return mInstance;
		}
		
		mInstance = new CVisionWindow();
		return mInstance;
	}
	
	public void launchCVision() {
		
		createWindowFrame();
	}
	
	/*
	 * 
	 * PUBLIC OVERRIDE
	 * 
	 */
	
	@Override
	public void onActionPerformed(CaptureAction action, CaptureStatus status) {
		
		switch(action) {
		
			case CAPTURE_STARTED:
				mActionButton.setText("Cancel Recognition");
				break;
				
			case CAPTURE_STOPPED:
				mActionButton.setText("Start Recognition");
				break;
				
			case CAPTURE_TRAINING_STARTED:
				mActionButton.setText("Training in progress. Face camera.");
				mActionButton.setEnabled(false);
				break;
				
			case CAPTURE_TRAINING_STOPPED:
				mActionButton.setText("Start Recognition");
				mActionButton.setEnabled(true);
				break;
				
			default:
				System.out.println("Action not handled");
		}
	}

	@Override
	public void onFrameCaptured(Image capturedImage) {
		
		mCaptureImage.setIcon(new ImageIcon(capturedImage));
	}
	
	
	/*
	 * 
	 * PRIVATE METHODS
	 * 
	 */
	
	private void createWindowFrame() {
		
		mWindow = new JFrame("CVision");
		mWindow.setSize(600, 400);
		mWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		createModePanelAndControls();
		
		createShowCaptureControl();
		
		createActionButton();
		
		//Pack and show the frame
		mWindow.setVisible(true);
	}
	
	private void createModePanelAndControls() {
		
		JPanel modePanel = new JPanel();
		modePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		mModeTrainUser = new JRadioButton("Train New User");
		mModeTrainUser.setSelected(false);
		mModeTrainUser.addActionListener(mTrainUserClick);
		modePanel.add(mModeTrainUser);
		
		mTrainUsername = new JTextField();
		mTrainUsername.setEnabled(false);
		mTrainUsername.setPreferredSize(new Dimension(180, 35));
		mTrainUsername.setToolTipText("Enter the name of the user being trainned for recognition.");
		modePanel.add(mTrainUsername);
		
		mModeDetectUser = new JRadioButton("Detect User");
		mModeDetectUser.setSelected(true);
		mModeDetectUser.addActionListener(mDetectUserClick);
		modePanel.add(mModeDetectUser);
		
		mWindow.add(modePanel, BorderLayout.NORTH);
	}
	
	private void createShowCaptureControl() {
		
		JPanel capturePanel = new JPanel();
		capturePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		mCaptureImage = new JLabel("Nothing to display");
		capturePanel.add(mCaptureImage);
		
		mWindow.add(capturePanel, BorderLayout.CENTER);
	}
	
	private void createActionButton() {
		
		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		mActionButton = new JButton("Start Recognition");
		mActionButton.addActionListener(actionButtonListener);
		actionPanel.add(mActionButton);
		
		mWindow.add(actionPanel, BorderLayout.SOUTH);
	}
	
	private ActionListener mTrainUserClick = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			mModeTrainUser.setSelected(true);
			mModeDetectUser.setSelected(false);
			mTrainUsername.setEnabled(true);
		}
	};
	
	private ActionListener mDetectUserClick = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			mModeTrainUser.setSelected(false);
			mModeDetectUser.setSelected(true);
			mTrainUsername.setEnabled(false);
			mTrainUsername.setText("");
		}
	};
	
	private ActionListener actionButtonListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if(mCapture.isCaptureActive()) {
				mCapture.stopCapture();
				return;
			}
			
			if(mModeTrainUser.isSelected()) {
				mCapture.startCapture(true, mTrainUsername.getText());
			} else {
				mCapture.startCapture(false, null);
			}
		}
	};
}
