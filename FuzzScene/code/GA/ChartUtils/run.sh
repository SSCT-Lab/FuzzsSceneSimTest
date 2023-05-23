#!/bin/bash
source /home/vangogh/anaconda3/etc/profile.d/conda.sh  # 


# 1: model_num 2: seq_num
for ((i=1; i<=$1; i++))
do
    for ((j=0; j<$2; j++))
    do
        python radar_error_test.py $i $j
    done
done
