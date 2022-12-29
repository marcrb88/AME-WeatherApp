#include "mbed.h"

class Thermistor {
public:
  Thermistor(AnalogIn pin);
  uint16_t read();
  float calculate_resistance(uint16_t counts);
  float calculate_kelvin(float resistence);
  float calculate_degrees(float kelvin);

private:
  AnalogIn _thermistor;
};
