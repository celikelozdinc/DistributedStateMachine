
#!/bin/bash
# $1 : Log file storing sm execution
# Duration in order to up a smoc
if [[ "${HOSTNAME}" == smoc9 ]]; then
  jvm=$(grep "Started" $1 | awk -F 'in | seconds' '{print $2}')
  ckpt=$(grep "Applied" $1 | awk -F 'in | seconds' '{print $2}')
  sum=$(echo "$jvm" + "$ckpt" | bc)

else
  sum=$(grep "Started" $1 | awk -F 'in | seconds' '{print $2}')
fi

# Learn PID
# xargs will do trimming
current_pid=$(grep "PID" $1 | tail -n 1 | awk -F "INFO|---" '{print $2}' | xargs)


#VmSize=$(grep "VmSize" /proc/"$current_pid"/status | awk -F 'VmSize:|kB' '{print $2}' | xargs)
#VmPeak=$(grep "VmPeak" /proc/"$current_pid"/status | awk -F 'VmPeak:|kB' '{print $2}' | xargs)
#VmHWM=$(grep "VmHWM" /proc/"$current_pid"/status | awk -F 'VmHWM:|kB' '{print $2}' | xargs)
#VmRSS=$(grep "VmRSS" /proc/"$current_pid"/status | awk -F 'VmRSS:|kB' '{print $2}' | xargs)
#VmData=$(grep "VmData" /proc/"$current_pid"/status | awk -F 'VmData:|kB' '{print $2}' | xargs)
#VmStk=$(grep "VmStk" /proc/"$current_pid"/status | awk -F 'VmStk:|kB' '{print $2}' | xargs)
#VmExe=$(grep "VmExe" /proc/"$current_pid"/status | awk -F 'VmExe:|kB' '{print $2}' | xargs)
#VmLib=$(grep "VmLib" /proc/"$current_pid"/status | awk -F 'VmLib:|kB' '{print $2}' | xargs)

# Memory Report from /proc/<pid>/status
# xargs will do trimming
# tail : Get last match from $1 file
deltaMemoryFootprint=$(grep "Delta of each memory footprint metric" $1 | tail -n 1 | cut -d'>' -f2 | xargs)

StateMachineObjectSize=$(grep "Current State Machine Object Size" $1 | tail -n 1 | cut -d'=' -f2 | xargs)
CheckpointObjectSize=$(grep  "Current Checkpoint Object Size" $1 | tail -n 1 | cut -d'=' -f2 | xargs)
# for centralized solution, no checkpoint object will be stored
#if [[ -z $CheckCheckpointObjectSize ]]; then
#  CheckpointObjectSize=0
#fi
echo "StateMachineObjectSize = "$StateMachineObjectSize
echo "CheckpointObjectSize = "$CheckpointObjectSize
TotalObjectSize=$(echo "$StateMachineObjectSize" + "$CheckpointObjectSize" | bc)
echo "TotalObjectSize = "$TotalObjectSize


# Memory Report from top command
# -v for awk : define variable
# -n for top : exit after n iteration
from_top=$(top -n 1 | awk -v search="$current_pid" '$1 == search {print $5}' | cut -d'm' -f1)

metadata=$(grep "metadata for reporting" $1 | cut -d'>' -f2 |  xargs)

#echo "Measures in CSV format:"
#echo "$sum","$VmPeak","$VmSize","$VmHWM","$VmRSS","$VmData","$VmStk","$VmExe","$VmLib","$from_top"
echo "$metadata","$sum","$deltaMemoryFootprint","$from_top","$TotalObjectSize"
if [[ "${HOSTNAME}" == smoc9 ]]; then
  # Breakdown of the restore duration of new smoc #
  start_jvm=$(grep "Started" $1 | awk -F 'in | seconds' '{print $2}')
  start_communication=$(grep "start_communication" $1 | awk -F 'in | seconds' '{print $2}')
  prepare_ckpts=$(grep "prepareCkpts" $1 | awk -F 'in | seconds' '{print $2}')
  apply_ckpts=$(grep "applyCktps" $1 | awk -F 'in | seconds' '{print $2}')
  apply=$(grep "Applied" $1 | awk -F 'in | seconds' '{print $2}')
  echo "$start_jvm","$start_communication","$prepare_ckpts","$apply_ckpts","$apply"
fi