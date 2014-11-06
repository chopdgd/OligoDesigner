#!/bin/sh

INPUTFILE=$1

twoBitToFa /data/blatDb/hg19.2bit -seqList="/data/"$INPUTFILE output.fa
echo $?

cat output.fa | while read line
do
	if [[ $line == *chr* ]]
	then
		echo "$line""+"
	else
		echo "$line"
	fi
done

rm output.fa
