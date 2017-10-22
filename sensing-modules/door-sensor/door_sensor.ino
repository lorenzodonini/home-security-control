// Test project

//#include "application.h"
#include <stdio.h>
#include "Wire.h"
#include "Particle.h"

#define PIN D0
#define DEVICE_BUS 1
#define DEVICE_ADDRESS 0x1D
#define DATA_REG 0x00
#define CTRL_REG 0x2A
#define DATA_CFG_REG 0x0E
#define SLEEP_CNT_REG 0x29

char buffer[7];

struct SensorData {
  float x;
  float y;
  float z;
};

SensorData parseData() {
  SensorData result;

  result.x = (buffer[1] * 256 + buffer[2]) / 16;
  if (result.x > 2047) {
    result.x -= 4096;
  }

  result.y = (buffer[3] * 256 + buffer[4]) / 16;
  if (result.y > 2047) {
    result.y -= 4096;
  }

  result.z = (buffer[5] * 256 + buffer[6]) / 16;
  if (result.z > 2047) {
    result.z -= 4096;
  }

  return result;
}

void standby() {
  Wire.beginTransmission(DEVICE_ADDRESS);
  Wire.write(CTRL_REG);
  Wire.write(0x00);
  if (Wire.endTransmission() > 0) {
    Serial.println("Error setting standby mode!");
  } else {
    Serial.println("Mode: STANDBY");
  }
}

void activate() {
  Wire.beginTransmission(DEVICE_ADDRESS);
  Wire.write(CTRL_REG);
  Wire.write(0x01);
  if (Wire.endTransmission() > 0) {
    Serial.println("Error setting active mode!");
  } else {
    Serial.println("Mode: ACTIVE");
  }
}

void sleep() {
  Wire.beginTransmission(DEVICE_ADDRESS);
  Wire.write(CTRL_REG);
  Wire.write(0x02);
  if (Wire.endTransmission() > 0) {
    Serial.println("Error setting sleep mode!");
  } else {
    Serial.println("Mode: SLEEP");
  }
}

void setup() {
  Wire.begin(); // join i2c bus as master
  Serial.begin(9600); // start serial for output
  Wire.beginTransmission(DEVICE_ADDRESS);
  Wire.write(DATA_CFG_REG);
  Wire.write(0x00);
  if (Wire.endTransmission() > 0) {
    Serial.println("Error setting cfg register!");
  }
  standby();
  activate();
}

void loop() {
  int i = 0;
  char output[256];
  SensorData data;

  Wire.requestFrom(DEVICE_ADDRESS, 7);
  while (Wire.available() && i < 7) {
    buffer[i++] = Wire.read();
  }

  data = parseData();
  sprintf(output, "Data -> x: %f, y: %f, z: %f", data.x, data.y, data.z);
  Serial.println(output);        // wait 5 seconds for next scan

  delay(1000);
}
