#!/usr/bin/python3

import time
import smbus

DEVICE_BUS = 1
DEVICE_ADDRESS = 0x1D
DATA_REG = 0x00
CTRL_REG = 0x2A
DATA_CFG_REG = 0x0E
SLEEP_CNT_REG = 0x29


class Command:
    def __init__(self, desc, func):
        self.desc = desc
        self.func = func

    def execute(self, bus):
        self.func(bus)


def command_help(commands):
    print("Available commands:")
    for key, val in commands.items():
        print(key + " - " + val.desc)


def standby(bus):
    bus.write_byte_data(DEVICE_ADDRESS, CTRL_REG, 0x00)


def activate(bus):
    bus.write_byte_data(DEVICE_ADDRESS, CTRL_REG, 0x01)


def sleep(bus):
    bus.write_byte_data(DEVICE_ADDRESS, CTRL_REG, 0x02)


def read_data(bus):
    data = bus.read_i2c_block_data(DEVICE_ADDRESS, 0x00, 7)
    # Parse data
    x = (data[1] * 256 + data[2]) / 16
    if x > 2047:
        x -= 4096

    y = (data[3] * 256 + data[4]) / 16
    if y > 2047:
        y -= 4096

    z = (data[5] * 256 + data[6]) / 16
    if z > 2047:
        z -= 4096
    print("X: " + str(x) + ", Y: " + str(y) + ", Z: " + str(z))
    return {'x': x, 'y': y, 'z': z}


def read_loop(bus):
    while True:
        read_data(bus)
        time.sleep(0.1)


def main():
    # Prepare I2C bus
    bus = smbus.SMBus(DEVICE_BUS)
    bus.write_byte_data(DEVICE_ADDRESS, DATA_CFG_REG, 0x00)
    commands = {'r': Command('Read data line', read_data),
                '0': Command('Standby Mode', standby),
                '1': Command('Active Mode', activate),
                '2': Command('Sleep Mode', sleep),
                'l': Command('Read Loop', read_loop),
                'h': Command('Help', command_help)}
    command_help(commands)
    while True:
        cmd = input("Command: ")
        if cmd not in commands:
            print("Invalid command")
            continue
        if cmd == 'h':
            commands[cmd].execute(commands)
        else:
            commands[cmd].execute(bus)


main()
