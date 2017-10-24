#!/bin/bash

SCRIPT_LOC="/data/"
REBLAT_HOME="/data"

REBLAT_FILE=$1

REBLAT_FILE_LOC=`find $REBLAT_HOME -iname $REBLAT_FILE`

echo "reblat input file located at.."
echo $REBLAT_FILE_LOC

cd $REBLAT_HOME

BLAT_FOLDER="/data"
BLAT_DB="/data/blatDb"
BLAT_INPUT=$BLAT_FOLDER"/blatInp/"$REBLAT_FILE

BLAT_OUTPUT_FOLDER=$BLAT_FOLDER"/blatOp/"$REBLAT_FILE

if [ -e $BLAT_OUTPUT_FOLDER ]
	then
	echo "blat op folder exists.."
else
	mkdir $BLAT_OUTPUT_FOLDER
fi



if [ -e $BLAT_OUTPUT_FOLDER"/psl/" ]
	then
	echo "psl folder exists"
else
	mkdir $BLAT_OUTPUT_FOLDER"/psl/"
fi


if [ -e $BLAT_INPUT ]
then

echo "<h1>Blat Result</h1><hr>"


	##	Blatting for each chromosome:
#	for n in {1..22} X Y
#	do
#	echo "runnig BLAT for query against chr$n"
#	blat -t=dna -q=dna -out=pslx -fine -stepSize=5 -minScore=15 -repMatch=1000000 $BLAT_DB"/hg19.2bit" $BLAT_INPUT $BLAT_OUTPUT_FOLDER"/psl/output.psl" > /dev/null
# 	done

	#gfClient localhost 17779 -t=dna -q=dna -out=pslx -minScore=5 $BLAT_DB $BLAT_INPUT $BLAT_OUTPUT_FOLDER"/psl/output.psl" > /dev/null
	gfClient localhost 17779 -t=dna -q=dna -out=pslx -minScore=15 $BLAT_DB $BLAT_INPUT $BLAT_OUTPUT_FOLDER"/psl/output.psl" > /dev/null

	##	sorting and merging the psl files(blat output for each chromosome)
	echo "sorting and merging the individual chromosome output psl files"
	pslSort dirs $BLAT_OUTPUT_FOLDER"/out.psl" /tmp $BLAT_OUTPUT_FOLDER"/psl/" > /dev/null

	##pslReps - after pslSort.. haven't tried it yet.. 
	##echo "post processing output "
	##pslReps $BLAT_OUTPUT_FOLDER"/out.psl" $BLAT_OUTPUT_FOLDER"/FINAL.psl" $BLAT_OUTPUT_FOLDER"/out.psr" > /dev/null
	cp $BLAT_OUTPUT_FOLDER"/out.psl" $BLAT_OUTPUT_FOLDER/"FINAL.psl"

	cat $BLAT_OUTPUT_FOLDER"/out.psl" | while read line
	do
	echo "$line"
	done

	echo "Your blat results are done!<hr>"

else

echo "NO PRIMERS FOUND"

fi

