#!/bin/sh

INPUTFILE=$1
ASSEMBLY=$2

#twoBitToFa /data/blatDb/hg19.2bit -seqList="/data/"$INPUTFILE output.fa
twoBitToFa "/data/blatDb/"$ASSEMBLY".2bit" -seqList="/data/"$INPUTFILE output.fa
echo $?

#cat output.fa | while read line
while read line
do
	if [[ $line == *chr* ]]
	then
		echo "$line""+"
	else
		echo "$line"
	fi
done < output.fa

rm output.fa
