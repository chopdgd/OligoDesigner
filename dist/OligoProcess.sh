#!/bin/bash

SCRIPT_LOC="/data/"
PRIMER3_HOME="/data"

OLIGO_FILE=$1

OLIGO_FILE_LOC=`find $PRIMER3_HOME -iname $OLIGO_FILE`

echo "primer input file located at.."
echo $OLIGO_FILE_LOC

cd $PRIMER3_HOME

OLIGO_OUTPUT_FILE="$PRIMER3_HOME/oligoOut/$OLIGO_FILE"

echo "primer output file located at.."
echo $OLIGO_OUTPUT_FILE


echo "<h1>Primer 3 Output</h1><hr>"

PRIMER_CMD="primer3_core -format_output -strict_tags -default_version=2 -io_version=4 -p3_settings_file=$PRIMER3_HOME/oligo_settings.txt -echo_settings_file -output=$OLIGO_OUTPUT_FILE $PRIMER3_HOME/oligoInp/$OLIGO_FILE"

echo $PRIMER_CMD

`$PRIMER_CMD`

#echo $?

BODY=`cat $OLIGO_OUTPUT_FILE`

if [ -n "$BODY" ]
then
   echo "$BODY"
fi



BLAT_FOLDER="/data"
BLAT_DB="/data/blatDb"
BLAT_INPUT=$BLAT_FOLDER"/blatOligoInp/"$OLIGO_FILE
MFOLD_INPUT=$BLAT_FOLDER"/mfoldInp/"$OLIGO_FILE
MFOLD_OUTPUT="/data/mfoldOut/"$OLIGO_FILE
HOMODIMER_OUTPUT="/data/homodimerOut/"$OLIGO_FILE"_"$OLIGO_FILE
GFPCR_CMD="/usr/local/isPcr/gfPcr"


if [ -e $BLAT_INPUT ]
then
rm $BLAT_INPUT
fi

BLAT_OUTPUT_FOLDER=$BLAT_FOLDER"/blatOligoOp/"$OLIGO_FILE

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


HEAD_TAG=""
OLIGO_TAG=""
NO_PR_TAG=""
COUNT=0


echo " "
echo "<h1>Primer3 Primers</h1><hr>"



cat $OLIGO_OUTPUT_FILE | \
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


if [[ $regExLine=~INTERNAL ]]
then
OLIGO=`echo $regExLine | grep -n 'INTERNAL_OLIGO' | awk '{ print $NF }'`
#echo $OLIGO
fi

if [[ ${#HEADER} > 0 ]]
then
HEAD_TAG=$HEADER
#echo "$HEAD_TAG"
fi


if [[ ${#OLIGO} > 0 ]]
then
    OLIGO_TAG=$OLIGO
    COUNT=`expr $COUNT + 1`
    echo ">"$HEAD_TAG"_O"$COUNT >> $BLAT_INPUT
    echo $OLIGO_TAG >> $BLAT_INPUT
    echo ">"$HEAD_TAG"_O"$COUNT >> $MFOLD_INPUT
    echo $OLIGO_TAG >> $MFOLD_INPUT 
    HEADER="o "$HEAD_TAG"_O"$COUNT
    echo "$HEADER"
    echo "  $OLIGO_TAG"
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
	echo "runnig BLAT for query"
	gfClient localhost 17779 -t=dna -q=dna -out=pslx -minScore=15 $BLAT_DB $BLAT_INPUT $BLAT_OUTPUT_FOLDER"/psl/output.psl" > /dev/null

	
	##	sorting and merging the psl files(blat output for each chromosome)
	echo "sorting and merging the individual chromosome output psl files"
	pslSort dirs $BLAT_OUTPUT_FOLDER"/out.psl" /tmp $BLAT_OUTPUT_FOLDER"/psl/" > /dev/null

	##	pslReps - after pslSort.. haven't tried it yet.. 
	echo "post processing output"
	pslReps -singleHit -ignoreSize $BLAT_OUTPUT_FOLDER"/out.psl" $BLAT_OUTPUT_FOLDER"/FINAL.psl" $BLAT_OUTPUT_FOLDER"/out.psr" > /dev/null


	###	run script for hairpin..
	echo "processing hairpin analysis script"
#	cat $MFOLD_INPUT | hybrid-ss-min -n DNA -t 25 -T 25 -N 0.05 -E -mfold --stream > $MFOLD_OUTPUT
	####changing melting temperature to be default at 37
	###hybrid-ss-min -n DNA -t 25 -T 25 -N 0.05 -mfold -o $MFOLD_OUTPUT $MFOLD_INPUT
	hybrid-ss-min -n DNA -N 0.05 -mfold -o $MFOLD_OUTPUT $MFOLD_INPUT

	###	run script for homodimer analysis

	hybrid-min -n DNA -t 25 -T 25 -N 0.05 -mfold -o $HOMODIMER_OUTPUT $MFOLD_INPUT $MFOLD_INPUT	


	###	run script for heterodimer analysis



	cat $BLAT_OUTPUT_FOLDER"/FINAL.psl" | while read line
	do
	echo "$line"
	done

	cat $BLAT_OUTPUT_FOLDER"/out.psr" | while read line
	do
	echo "$line"
	done
	
	cat $MFOLD_OUTPUT".ct" | while read line
	do
		if [[ $line == *dG* ]]
		then
		echo "$line"
		fi
	done	

	cat $HOMODIMER_OUTPUT".ct" | while read line
	do
		if [[ $line == *dG* ]]
		then 
		echo "$line"
		fi
	done


	echo "Your oligos are generated!<hr>" 


else

echo "NO PRIMERS FOUND"

fi

