#!/bin/bash

SCRIPT_LOC="/data/"
PRIMER3_HOME="/data"

PRIMER_FILE=$1

PRIMER_FILE_LOC=`find $PRIMER3_HOME -iname $PRIMER_FILE`

echo "primer input file located at.."
echo $PRIMER_FILE_LOC

cd $PRIMER3_HOME

PRIMERS_OUTPUT_FILE="$PRIMER3_HOME/primer3out/$PRIMER_FILE"

echo "primer output file located at.."
echo $PRIMERS_OUTPUT_FILE


echo "<h1>Primer 3 Output</h1><hr>"

PRIMER_CMD="primer3_core -format_output -strict_tags -default_version=2 -io_version=4 -p3_settings_file=$PRIMER3_HOME/primer3_settings.txt -echo_settings_file -output=$PRIMERS_OUTPUT_FILE $PRIMER3_HOME/primer3Inp/$PRIMER_FILE"

echo $PRIMER_CMD

`$PRIMER_CMD`

#echo $?

BODY=`cat $PRIMERS_OUTPUT_FILE`

if [ -n "$BODY" ]
then
        echo "$BODY"
fi





BLAT_FOLDER="/data"
BLAT_DB="/data/blatDb"
BLAT_INPUT=$BLAT_FOLDER"/blatInp/"$PRIMER_FILE

IS_PCR_INPUT="/data/isPcrInp/"$PRIMER_FILE
IS_PCR_OUTPUT="/data/isPcrOp/"$PRIMER_FILE
IS_PCR_DB="/data/blatDb"


if [ -e $BLAT_INPUT ]
then
rm $BLAT_INPUT
fi

BLAT_OUTPUT_FOLDER=$BLAT_FOLDER"/blatOp/"$PRIMER_FILE

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

if [ -e $IS_PCR_INPUT ]
	then
	rm $IS_PCR_INPUT
	echo "removed existing insilico PCR input file.."
fi	


HEAD_TAG=""
LEFT_PR_TAG=""
RIGHT_PR_TAG=""
NO_PR_TAG=""
COUNT=0


echo " "
echo "<h1>Primer3 Primers</h1><hr>"



cat $PRIMERS_OUTPUT_FILE | \
{
while read regExLine
do

if [[ $regExLine=~NO[:blank:]PRIMERS[:blank:]FOUND ]]
then
NO_PRIMER=`echo $regExLine`
#echo $NO_PRIMER
fi

if [[ $regExLine=~PICKING ]]
then
HEADER=`echo $regExLine | grep -n 'PRIMER PICKING' | awk '{ print $NF }'`
#echo $HEADER
fi

if [[ $regExLine=~LEFT ]]
then
LEFT_PRIMER=`echo $regExLine | grep -n 'LEFT PRIMER' | awk '{ print $NF }'`
#echo $LEFT_PRIMER
fi

if [[ $regExLine=~RIGHT ]]
then
RIGHT_PRIMER=`echo $regExLine | grep -n 'RIGHT PRIMER' | awk '{ print $NF }'`
#echo $RIGHT_PRIMER
fi


if [[ ${#HEADER} > 0 ]]
then
HEAD_TAG=$HEADER
#echo "$HEAD_TAG"
fi

if [[ ${#LEFT_PRIMER} > 0 ]]
then
    LEFT_PR_TAG=$LEFT_PRIMER
    COUNT=`expr $COUNT + 1`
    echo ">"$HEAD_TAG"_L"$COUNT >> $BLAT_INPUT
    echo $LEFT_PR_TAG >> $BLAT_INPUT
    HEADER="o "$HEAD_TAG"_L"$COUNT
    echo "$HEADER"
    echo "  $LEFT_PR_TAG"
fi

if [[ ${#RIGHT_PRIMER} > 0 ]]
then
    RIGHT_PR_TAG=$RIGHT_PRIMER
    echo ">"$HEAD_TAG"_R"$COUNT >> $BLAT_INPUT
    echo $RIGHT_PR_TAG >> $BLAT_INPUT
    HEADER="o "$HEAD_TAG"_R"$COUNT
    echo "$HEADER"
    echo "  $RIGHT_PR_TAG"
    echo $HEAD_TAG"_L"$COUNT"_"$HEAD_TAG"_R"$COUNT" "$LEFT_PR_TAG" "$RIGHT_PR_TAG >> $IS_PCR_INPUT
fi


if [[ ${#NO_PRIMER} > 0 ]]
then
NO_PR_TAG=NO_PRIMER
fi

done
}

#echo "-----------------------------------"



if [ -e $BLAT_INPUT ]
then

echo "<h1>Blat Result</h1><hr>"


	##	Blatting for each chromosome:
#	for n in {1..22} X Y
#	do
	echo "runnig BLAT for query against chr$n"
#	blat -t=dna -q=dna -out=pslx -fine -stepSize=5 -minScore=15 -repMatch=1000000 $BLAT_DB"/hg19.2bit" $BLAT_INPUT $BLAT_OUTPUT_FOLDER"/psl/output.psl" > /dev/null
# 	done
	gfClient localhost 17779 -t=dna -q=dna -out=pslx -minScore=15 $BLAT_DB $BLAT_INPUT $BLAT_OUTPUT_FOLDER"/psl/output.psl" > /dev/null

	##	sorting and merging the psl files(blat output for each chromosome)
	echo "sorting and merging the individual chromosome output psl files"
	pslSort dirs $BLAT_OUTPUT_FOLDER"/out.psl" /tmp $BLAT_OUTPUT_FOLDER"/psl/" > /dev/null

	##pslReps - after pslSort.. haven't tried it yet.. 
	echo "post processing output "
	pslReps -singleHit -ignoreSize $BLAT_OUTPUT_FOLDER"/out.psl" $BLAT_OUTPUT_FOLDER"/FINAL.psl" $BLAT_OUTPUT_FOLDER"/out.psr" > /dev/null


	cat $BLAT_OUTPUT_FOLDER"/FINAL.psl" | while read line
	do
	echo "$line"
	done

	cat $BLAT_OUTPUT_FOLDER"/out.psr" | while read line
	do
	echo "$line"
	done

	echo "<h1>Running insilico PCR</h1><hr>"

	gfPcr localhost 17779 $IS_PCR_DB $IS_PCR_INPUT  $IS_PCR_OUTPUT
	
	cat $IS_PCR_OUTPUT | while read line
	do
	echo "$line"
	done

	echo "Your primers are generated!<hr>" 


else

echo "NO PRIMERS FOUND"

fi

