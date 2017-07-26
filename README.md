# OpenCVision
Face detection and recognition using OpenCV

## Purpose of this project
The aim of this project is to use Java AWT and OpenCV on Mac platform for face detection and recognition. The approach for Windows is available almost everywhere but for Mac platform it is a bit difficult to find. For this I will be noting down all the approaches I have taken to build the libraries and the JAR from OpenCV source code which then I used to create the application.

## Ingredients
You would need to first download the following, as required.

1. OpenCV Source (For this we are using version 3.2.0)  
   http://opencv.org/releases.html
2. OpenCV Contrib (For this we are using version 3.2.0)  
   https://github.com/opencv/opencv_contrib/releases
3. Ant (Download only if Ant is not present)  
   https://ant.apache.org/bindownload.cgi
4. CMake (Download the DMG)  
   https://cmake.org/download/
5. Java Development Kit (We are using 1.8.0)
6. Eclipse IDE

## Downloading OpenCV Source
This is a very simple step. Head over to the download page and download the zip file. Once downloaded, extract it to the foldef where you want it to be. Keep the zip file and do not remove it now, just incase you require it in future as fallback.

For example in our case we extracted it to:  
```
/Users/wwdablu/Development/OpenCV
```

## Download OpenCV Contrib
This contains various libraries but we are instered on the Face library. Extract this to a folder:  
```
/Users/wwdablu/Development/OpenCV/contrib
```

Once extract head over to the modules/face folder, that is:
```
/Users/wwdablu/Development/OpenCV/contrib/modules/face
```

Now open the file CMakeLists.txt and edit the line to the following:
```
ocv_define_module(face opencv_core opencv_imgproc opencv_objdetect WRAP python java)
```

Note that in here we are adding the java build type too so that during the OpenCV build, the JAR also contains the references to Face library too.

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

## Installing CMake
This is again simple. Double click on the DMG and install CMake on the Applications folder. Once it is done, on the same terminal window (or a new one) type in the following:
```
sudo "/Applications/CMake.app/Contents/bin/cmake-gui" --install
```

## Generate OpenCV JAR
To generate the JAR file from OpenCV, now we need to build the source code. On the same terminal window go to the folder where OpenCV source has been extracted. You can follow along with these commands:  
```
cd /Users/wwdablu/Development/OpenCV
mkdir build
cd build
cmake -D OPENCV_EXTRA_MODULES_PATH=/Users/wwdablu/Development/OpenCV/contrib/modules -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/Users/wwdablu/Development/OpenCV/build
make -j4
```

Once above commands are executed the OpenCV JAR should be present inside the ```lib``` folder in build folder. There you will find the OpenCV JAR which we can now use for our project.
