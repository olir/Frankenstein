#!/bin/sh
if [ $# -eq 0 ]
then
  echo "No arguments supplied. Usage: createvideo MP4FILENAME"
  exit -1
fi
ffmpeg -t 5 -f lavfi -i color=c=black:s=1920x1200 -c:v libx264 -tune stillimage -pix_fmt yuv420p -y .tmp_$1
ffmpeg -f lavfi -i anullsrc -i .tmp_$1 -shortest -c:v copy -c:a aac -map 0:a -map 1:v -y $1
rm .tmp_$1

