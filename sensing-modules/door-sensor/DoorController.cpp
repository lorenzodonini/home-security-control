#include "DoorController.h"
#include <stdio.h>

DoorController::DoorController(MQTT * client): monitoringEnabled(false), client(client) {
    //Initialize sensor
    sensor.setDelegate(this);
    sensor.standby();
    sensor.activate();
}

void DoorController::monitor() {
  if (!monitoringEnabled) {
    return;
  }
  sensor.readData();
}

void DoorController::enableMonitoring() {
  monitoringEnabled = true;
}

void DoorController::disableMonitoring() {
  monitoringEnabled = false;
}

//DoorDelegate callback
void DoorController::onDoorMovement(SensorData data) {
  char payload[256] = {0};
  Log.warn("Detected door movement! MQTT connected: %d", client != nullptr && client->isConnected());
  if (client != nullptr && client->isConnected()) {
    sprintf(payload, "Movement detected -> x: %f, y: %f, z: %f", data.x, data.y, data.z);
    client->publish(TOPIC_ALERT, payload);
  } else {
    Log.info("Not connected to MQTT broker, cannot publish data");
  }
}
