#!/bin/sh

find /data/heterodimerOp -type f -name "*.txt*" -mmin +60 -exec rm  {} \;
find /data/heterodimerInp -type f -name "*.txt*" -mmin +60 -exec rm  {} \;
find /data/homodimerOp -type f -name "*.txt*" -mmin +60 -exec rm  {} \;
find /data/homodimerInp -type f -name "*.txt*" -mmin +60 -exec rm  {} \;
find /data/mfoldInp -type f -name "*.txt*" -mmin +60 -exec rm  {} \;
find /data/mfoldOut -type f -name "*.txt" -mmin +60 -exec rm  {} \;
find /data/blatOligoOp -type f -name "*.txt" -mmin +60 -exec rm -Rf {} \;
find /data/blatOligoInp -type f -name "*.txt" -mmin +60 -exec rm {} \;
find /data/oligoOut -type f -name "*.txt" -mmin +60 -exec rm {} \;
find /data/oligoInp -type f -name "*.txt" -mmin +60 -exec rm {} \;
