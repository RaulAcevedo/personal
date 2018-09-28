# Setup Serial port parameters to talk to it
stty -f /dev/cu.usbmodem1d1121 115200
sleep 15

#Set ecu to GM
echo -ne "ecu GM\r" >  /dev/cu.usbmodem1d1121
sleep 30

#Turn engine on
echo -ne "rpm 1500\r" >  /dev/cu.usbmodem1d1121
sleep 30

#Set a unit id to "smoke", which matches the unit in the Encompass company
echo -ne "unit smoke\r" >  /dev/cu.usbmodem1d1121
sleep 30

#Drive at least 2 minutes: start moving, wait 2.5 minutes, stop, engine off
echo -ne "vss 97\r" >  /dev/cu.usbmodem1d1121
sleep 150
echo -ne "vss 0\r" >  /dev/cu.usbmodem1d1121
sleep 30
echo -ne "rpm 0\r" >  /dev/cu.usbmodem1d1121
sleep 30
