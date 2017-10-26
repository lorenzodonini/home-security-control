#include "DoorSensor.h"
#include "MQTT/MQTT.h"

#define TOPIC_ALERT "zone/1/door/1/alert"
#define TOPIC_INFO "zone/1/door/1/info"

class DoorController: public DoorDelegate {
private:
  DoorSensor sensor;
  MQTT * client;
  bool monitoringEnabled;
public:
  DoorController(MQTT * client);
  void monitor();
  void enableMonitoring();
  void disableMonitoring();

  //DoorDelegate callback
  virtual void onDoorMovement(SensorData data);
};
