#!/bin/bash
# ./createFile.sh <begin> <end> <gap> <unit>
echo "./createFile.sh <begin> <end> <gap> <unit>"
i=$1
while [ $2 -ge $i ]
do
	dd if=/dev/zero of=/home/donpark/tmp/$i$4.txt bs=$i$4 count=1;
	((i+=$3));
done;
