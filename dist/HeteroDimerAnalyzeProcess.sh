#!/bin/bash

SCRIPT_LOC="/data/"
DATA_HOME="/data"

OLIGO_HET_FILE1=$1
OLIGO_HET_FILE2=$2

echo $OLIGO_HET_FILE1
echo $OLIGO_HET_FILE2

HET_OUTPUT_PREF=$DATA_HOME"/heterodimerOp/"$OLIGO_HET_FILE1"_"$OLIGO_HET_FILE2
echo $HET_OUTPUT_PREF

cd $DATA_HOME

###run script for heterodimer analysis
###changing temperature to be at 37 and not 25
#hybrid-min -n DNA -t 25 -T 25 -N 0.05 -mfold -o $HET_OUTPUT_PREF $DATA_HOME"/heterodimerInp/"$OLIGO_HET_FILE1 $DATA_HOME"/heterodimerInp/"$OLIGO_HET_FILE2 > /dev/null 
hybrid-min -n DNA -N 0.05 -mfold -o $HET_OUTPUT_PREF $DATA_HOME"/heterodimerInp/"$OLIGO_HET_FILE1 $DATA_HOME"/heterodimerInp/"$OLIGO_HET_FILE2 > /dev/null

cat $HET_OUTPUT_PREF".ct" | while read line
do
	if [[ $line == *dG* ]]
	then
	echo "$line" >> $HET_OUTPUT_PREF".out"
	fi
done

echo "removing large .ct file"

rm $HET_OUTPUT_PREF".ct"

echo "Your het scores are generated!<hr>" 

