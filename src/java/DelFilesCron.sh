#!/bin/sh

find /data/heterodimerOp -type f -name "*.txt*" -mmin +3600 -exec rm {} \;
echo "$?"
find /data/heterodimerInp -type f -name "*.txt*" -mmin +3600 -exec rm {} \;
echo "$?"
find /data/homodimerOut -type f -name "*.txt*" -mmin +3600 -exec rm {} \;
echo "$?"
find /data/mfoldInp -type f -name "*.txt*" -mmin +3600 -exec rm {} \;
echo "$?"
find /data/mfoldOut -type f -name "*.txt.*" -mmin +3600 -exec rm {} \;
echo "$?"
find /data/blatOligoOp -type f -name "*.txt" -mtime +3 -exec rm -rf {} \;
echo "$?"
find /data/blatOligoInp -type f -name "*.txt" -mtime +3 -exec rm {} \;
echo "$?"
find /data/oligoOut -type f -name "*.txt" -mmin +3600 -exec rm {} \;
echo "$?"
find /data/oligoInp -type f -name "*.txt" -mmin +3600 -exec rm {} \;
echo "$?"
