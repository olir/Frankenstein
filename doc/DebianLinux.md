# Debian GNU/Linux Instructions

## Windows 10
To run under Debian under Windows 10 [install WSL and package](https://www.microsoft.com/de-de/store/p/debian-gnu-linux/9msvkqc78pk6?rtc=1).

## Instructions

In Terminal enter:
```
sudo apt-get update
sudo apt-get --yes install ca-certificates
sudo apt-get --yes install unzip
sudo apt-get --yes install git
sudo apt-get --yes install maven
cd opt
sudo tar xzf /mnt/c/Users/User/Downloads/jdk-8u161-linux-x64.tar.gz
echo export JAVA_HOME=/opt/jdk1.8.0_161 >>~/.bashrc
source ~/.bashrc
```

Ready to use git and maven.
