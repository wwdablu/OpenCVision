# OpenCVision
Face detection and recognition using OpenCV

## Purpose of this project
The aim of this project is to use Java AWT and OpenCV on Mac platform for face detection and recognition. The approach for Windows is available almost everywhere but for Mac platform it is a bit difficult to find. For this I will be noting down all the approaches I have taken to build the libraries and the JAR from OpenCV source code which then I used to create the application.

## Ingredients
You would need to first download the following, as required.

1. OpenCV Source (For this we are using version 3.2.0)  
   http://opencv.org/releases.html
2. Ant (Download only if Ant is not present)  
   https://ant.apache.org/bindownload.cgi
3. CMake (Download the DMG)  
   https://cmake.org/download/
4. Java Development Kit (We are using 1.8.0)

## Downloading OpenCV Source
This is a very simple step. Head over to the download page and download the zip file. Once downloaded, extract it to the foldef where you want it to be. Keep the zip file and do not remove it now, just incase you require it in future as fallback.

## Setup Ant
Download the archive and extract it to a folder. Now launch terminal and enter the following commands:
```
export ANT_HOME=/Users/wwdablu/Development/apache-ant
export PATH=$PATH:$ANT_HOME/bin
```  
This change will be valid for this session of the terminal only, hence do not close the terminal. If closed, you need to redo again the above two lines. You can also put them in ~/.bash_profile, but for my case I did not do it. To test if ant has been properly mapped and accessible, on the same terminal window, type:  
```
ant -v
```  
This should show you the ant version which is being used.  

## Using CMake
This is again simple. Double click on the DMG and install CMake on the Applications folder. Once it is done, on the same terminal window (or a new one) type in the following:
```
sudo "/Applications/CMake.app/Contents/bin/cmake-gui" --install
```

