#!/bin/bash
# Duration in order to up a smoc
if [[ "${HOSTNAME}" == smoc11 ]]; then
  jvm=$(grep "Started" log | awk -F 'in | seconds' '{print $2}')
  ckpt=$(grep "Applied" log | awk -F 'in | seconds' '{print $2}')
  sum=$(echo "$jvm" + "$ckpt" | bc)
else
  sum=$(grep "Started" log | awk -F 'in | seconds' '{print $2}')
fi

# Learn PID
# xargs will do trimming
current_pid=$(grep "PID" log | tail -n 1 | awk -F "INFO|---" '{print $2}' | xargs)


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
# tail : Get last match from log file
deltaMemoryFootprint=$(grep "Delta of each memory footprint metric" log | tail -n 1 | cut -d'>' -f2 | xargs)

# Memory Report from top command
# -v for awk : define variable
# -n for top : exit after n iteration
from_top=$(top -n 1 | awk -v search="$current_pid" '$1 == search {print $5}' | cut -d'm' -f1)

#echo "Measures in CSV format:"
#echo "$sum","$VmPeak","$VmSize","$VmHWM","$VmRSS","$VmData","$VmStk","$VmExe","$VmLib","$from_top"
echo "$sum","$deltaMemoryFootprint","$from_top"