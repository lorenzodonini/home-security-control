// Test project

//#include "application.h"
#include "DoorController.h"

/*#define PIN D0
#define DEVICE_BUS 1
#define DEVICE_ADDRESS 0x1D
#define DATA_REG 0x00
#define CTRL_REG 0x2A
#define DATA_CFG_REG 0x0E
#define SLEEP_CNT_REG 0x29*/

void callback(char* topic, uint8_t* payload, unsigned int length) {
    Serial.println("Callback triggered");
}

SerialLogHandler logHandler;
uint8_t brokerIp[] = { 192,168,0,180 };
uint16_t defaultPort = 1883;

MQTT mqttClient(brokerIp, defaultPort, callback);
DoorController doorController(&mqttClient);

void setup() {
  Serial.begin(9600); // start serial for output
  //Setup MQTT
  if (mqttClient.connect(System.deviceID())) {
    Log.info("Connected to MQTT broker");
  } else {
    Log.warn("Failed to connect to MQTT broker");
  }
  doorController.enableMonitoring();
  Log.info("Door monitoring started");
}

void loop() {
  mqttClient.loop();
  doorController.monitor();
  delay(1000);
}
