# Debian GNU/Linux Instructions
(!!! UNDER CONSTRUCTION !!!)

## Windows 10
To run under Debian under Windows 10 ...
* [Install WSL and Debian package](https://www.microsoft.com/de-de/store/p/debian-gnu-linux/9msvkqc78pk6?rtc=1) 
* Install X-Server like [Xming](http://www.straightrunning.com/XmingNotes/). [More information](https://virtualizationreview.com/articles/2017/02/08/graphical-programs-on-windows-subsystem-on-linux.aspx)


## Linux Compilation Instructions
### Preparation
In Terminal enter:
```
sudo apt-get update
sudo apt-get --yes install ca-certificates
sudo apt-get --yes install unzip
sudo apt-get --yes install git
sudo apt-get --yes install maven
sudo apt-get --yes install ffmpeg
sudo tar xzf /mnt/c/Users/User/Downloads/jdk-8u161-linux-x64.tar.gz -C /opt
echo export JAVA_HOME=/opt/jdk1.8.0_161 >>~/.bashrc
export DISPLAY=:0 >>~/.bashrc
source ~/.bashrc
```

### Compile OpenCV
Install OpenCV on Linux: Read [Tutorial](http://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html) or execute:
```
sudo apt-get --yes install cmake
sudo apt-get --yes install cmake-gui
wget https://www.apache.org/dist/ant/binaries/apache-ant-1.10.2-bin.tar.gz ; sudo tar xzf apache-ant-1.10.2-bin.tar.gz -C /opt
export PATH=$PATH:/opt/apache-ant-1.10.2/bin  >>~/.bashrc
source ~/.bashrc
wget https://github.com/opencv/opencv/archive/3.4.1.zip
sudo unzip 3.4.1.zip -d /opt
cd /opt/opencv-3.4.1
sudo mkdir build
sudo cmake-gui
```
and follow the tutorial with ...

* Ungrouped Entries/ANT_EXECUTEABLE: /opt/apache-ant-1.10.2/bin/ant
* ...

### Compile Application

Ready to use git and maven as usual. Read to parent instructions.

