#!/bin/bash

BLUETOOTH_DEVICE=hci0
#sudo hcitool -i hcix cmd <OGF> <OCF> <No. Significant Data Octets> <iBeacon Prefix>    <UUID> <Major> <Minor> <Tx Power> <Placeholder Octets>

#OGF = Operation Group Field = Bluetooth Command Group = 0x08
#OCF = Operation Command Field = HCI_LE_Set_Advertising_Data = 0x0008
#No. Significant Data Octets (Max of 31) = 1E (Decimal 30)
#iBeacon Prefix (Always Fixed) = 02 01 1A 1A FF 4C 00 02 15

OGF="0x08"
OCF="0x0008"
#IBEACONPROFIX="02 01 1A 1A FF 4C 00 02 15"
IBEACONPREFIX="1E 02 01 1A 1A FF 4C 00 02 15"
UUID="15 EA D4 54 C8 58 4A 23 BB 60 19 E5 CF 1B CF 2F"
MAJOR="00 00"
MINOR="00 00"
POWER="C5 00"

sudo hciconfig $BLUETOOTH_DEVICE down
sudo hciconfig $BLUETOOTH_DEVICE up
sudo hciconfig $BLUETOOTH_DEVICE noleadv
sudo hciconfig $BLUETOOTH_DEVICE noscan
sudo hciconfig $BLUETOOTH_DEVICE leadv 3
sudo hcitool -i $BLUETOOTH_DEVICE cmd $OGF $OCF $IBEACONPREFIX $UUID $MAJOR $MINOR $POWER

#sudo hciconfig $BLUETOOTH_DEVICE leadv 3
