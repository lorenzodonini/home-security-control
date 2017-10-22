// Test project

//#include "application.h"
#include <stdio.h>
#include "Wire.h"
#include "Particle.h"
#include "MQTT/MQTT.h"

#define PIN D0
#define DEVICE_BUS 1
#define DEVICE_ADDRESS 0x1D
#define DATA_REG 0x00
#define CTRL_REG 0x2A
#define DATA_CFG_REG 0x0E
#define SLEEP_CNT_REG 0x29

struct SensorData {
  float x;
  float y;
  float z;
};

char buffer[7];
char topic[14] = "zone1/door";
char id[8] = "kamino";
uint8_t broker[] = { 192,168,0,180 };
void callback(char* topic, uint8_t* payload, unsigned int length);
MQTT client(broker, 1883, callback);

void callback(char* topic, uint8_t* payload, unsigned int length) {
    Serial.println("Callback triggered");
}

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

  //Setup MQTT
  if (client.connect(id)) {
    Serial.println("Connected to MQTT borker");
  } else {
    Serial.println("Failed to connect to MQTT borker");
  }

  //Setup I2C sensor
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
  if (client.isConnected()) {
    client.publish(topic, output);
  } else {
    Serial.println("Not connected to client, cannot publish data");
  }

  delay(1000);
}
