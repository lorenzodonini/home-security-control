#include "Particle.h"

#define DEVICE_ADDRESS 0x1D
#define DATA_REG 0x00
#define CTRL_REG 0x2A
#define DATA_CFG_REG 0x0E
#define SLEEP_CNT_REG 0x29
#define ACCEL_DELTA 200

struct SensorData {
  float x;
  float y;
  float z;
};

class DoorDelegate {
public:
  virtual void onDoorMovement(SensorData data) = 0;
};

// MA8452 sensor only
class DoorSensor {
private:
  SensorData referenceValue;
  Logger logger;
  DoorDelegate * delegate;
  char buffer[7] = {0};

  void updateDoorMovement(SensorData newData);
  void setSensorMode(uint8_t mode);
public:
  DoorSensor();
  SensorData readData();
  void setDelegate(DoorDelegate * delegate);
  void standby();
  void activate();
  void sleep();
};
