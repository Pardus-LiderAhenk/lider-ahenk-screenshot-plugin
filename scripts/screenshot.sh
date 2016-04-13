#!/bin/bash
if [ $# -lt 1 ]; then
	echo "Usage: screenshot.sh <png_file_name> <display_no[0]>";
	echo "Example: screenshot.sh /tmp/scr.png 0"
	exit 1;
fi
display=0;
if [ $# -gt 1 ]; then
	display=$2
fi
if ! which convert > /dev/null; then
	apt-get install -y imagemagick
fi
w -oush | grep tty | awk '{print $1, $3}' | while read x; do 
	array=(${x//:/ })
	if [ $display -eq ${array[1]} ]; then
		echo "Screenshot username:${array[0]} display:${array[1]}"
		su - ${array[0]} -c "xwd -root -display :${array[1]} | convert - png:- > $1"
	fi
done
