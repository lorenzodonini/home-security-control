#include "DoorSensor.h"
#include "math.h"
#include "Wire.h"

DoorSensor::DoorSensor() {
  Wire.begin(); // join i2c bus as master

  //Setup I2C sensor
  Wire.beginTransmission(DEVICE_ADDRESS);
  Wire.write(DATA_CFG_REG);
  Wire.write(0x00);
  if (Wire.endTransmission() > 0) {
    Log.warn("Error setting cfg register!");
  }
}

void DoorSensor::setSensorMode(uint8_t mode) {
  Wire.beginTransmission(DEVICE_ADDRESS);
  Wire.write(CTRL_REG);
  Wire.write(mode);
  if (Wire.endTransmission() > 0) {
    Log.warn("Error setting mode %d!", mode);
  } else {
    Log.info("New Mode: %d", mode);
  }
}

void DoorSensor::updateDoorMovement(SensorData newData) {
  if ((abs(newData.x - referenceValue.x) > ACCEL_DELTA
  || abs(newData.y - referenceValue.y) > ACCEL_DELTA
  || abs(newData.z - referenceValue.z) > ACCEL_DELTA)) {
    if (delegate != nullptr) {
      delegate->onDoorMovement(newData);
    }
  }

  referenceValue = newData;
}

SensorData DoorSensor::readData() {
  SensorData newData;
  int i = 0;

  Wire.requestFrom(DEVICE_ADDRESS, 7);
  while (Wire.available() && i < 7) {
    buffer[i++] = Wire.read();
  }

  newData.x = (buffer[1] * 256 + buffer[2]) / 16;
  if (newData.x > 2047) {
    newData.x -= 4096;
  }

  newData.y = (buffer[3] * 256 + buffer[4]) / 16;
  if (newData.y > 2047) {
    newData.y -= 4096;
  }

  newData.z = (buffer[5] * 256 + buffer[6]) / 16;
  if (newData.z > 2047) {
    newData.z -= 4096;
  }

  Log.info("Parsed data -> x: %f, y: %f, z: %f", newData.x, newData.y, newData.z);

  // Analyzing data
  updateDoorMovement(newData);

  return referenceValue;
}

void DoorSensor::setDelegate(DoorDelegate * delegate) {
  this->delegate = delegate;
}

void DoorSensor::standby() {
  setSensorMode(0x00);
}

void DoorSensor::activate() {
  setSensorMode(0x01);
}

void DoorSensor::sleep() {
  setSensorMode(0x02);
}
