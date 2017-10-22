// Test project

//#include "application.h"
#include <stdio.h>
#include "DoorSensor.h"
#include "MQTT/MQTT.h"

/*#define PIN D0
#define DEVICE_BUS 1
#define DEVICE_ADDRESS 0x1D
#define DATA_REG 0x00
#define CTRL_REG 0x2A
#define DATA_CFG_REG 0x0E
#define SLEEP_CNT_REG 0x29*/

SerialLogHandler logHandler;
DoorSensor doorSensor;
char topic[14] = "zone1/door";
char id[8] = "kamino";
uint8_t broker[] = { 192,168,0,180 };
void callback(char* topic, uint8_t* payload, unsigned int length);
MQTT client(broker, 1883, callback);

void callback(char* topic, uint8_t* payload, unsigned int length) {
    Serial.println("Callback triggered");
}

void setup() {
  Serial.begin(9600); // start serial for output

  //Setup MQTT
  if (client.connect(id)) {
    Serial.println("Connected to MQTT borker");
  } else {
    Serial.println("Failed to connect to MQTT borker");
  }

  doorSensor.standby();
  doorSensor.activate();
}

void loop() {
  /*char output[256];*/

  doorSensor.readData();

  /*data = parseData();
  sprintf(output, "Data -> x: %f, y: %f, z: %f", data.x, data.y, data.z);
  Serial.println(output);        // wait 5 seconds for next scan*/
  if (client.isConnected()) {
    client.publish(topic, "New data");
  } else {
    Serial.println("Not connected to client, cannot publish data");
  }

  delay(1000);
}
