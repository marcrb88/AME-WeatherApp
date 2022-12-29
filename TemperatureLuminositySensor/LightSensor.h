#include "mbed.h"

class LightSensor {
public:
  LightSensor(AnalogIn pin);
  uint16_t read();
  float calculate_Vout(uint16_t counts);
  float calculate_Lux(float vout);

private:
  AnalogIn _lightsensor;
};