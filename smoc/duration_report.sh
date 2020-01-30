#!/bin/bash
jvm=$(grep "Started" log | awk -F 'in | seconds' '{print $2}')
#ckpt=$(grep "Applied" log | awk -F 'in | seconds' '{print $2}')
#sum=$(echo "$jvm" + "$ckpt" | bc)
echo "Sum for duration is ""$jvm"