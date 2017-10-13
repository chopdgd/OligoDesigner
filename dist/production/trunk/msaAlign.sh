#!/bin/bash

SCRIPT_LOC="/data/"
MSA_HOME="/data/"
MSA_INP_FOLDER="msaInp/"
MSA_OP_FOLDER="msaOp/"

MSA_FILE=$1

MSA_FILE_LOC=$MSA_HOME$MSA_INP_FOLDER$MSA_FILE

echo "reblat input file located at.."
echo $MSA_FILE_LOC

cd $MSA_HOME

if [ -e $MSA_INP_FOLDER ]
then

	MSA_OP_FILE=$MSA_HOME$MSA_OP_FOLDER$MSA_FILE".aln"
	MSA_LOG_FILE=$MSA_HOME$MSA_OP_OLDER$MSA_FILE".log"

	echo "<h1>MSA Result</h1><hr>"

	#/usr/local/clustalw/clustalw2 -infile=$MSA_FILE_LOC 
	clustalw2 -infile=$MSA_FILE_LOC -outfile=$MSA_OP_FILE -outorder=input > $MSA_LOG_FILE 


	BODY=`cat $MSA_OP_FILE`

	if [ -n "$BODY" ]
	then
        	echo "$BODY"
	fi

	cat $MSA_LOG_FILE | while read line
	do
		echo "$line"
	done

		echo "Your alignment results are done!<hr>"

else

	echo "NO MSA Alignment file FOUND"

fi
