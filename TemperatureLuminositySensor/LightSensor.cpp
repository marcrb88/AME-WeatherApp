#include "LightSensor.h"
#include "mbed.h"

const float ADCRES = 65535.0;
const float VREF = 3.3;
const float lUXREL = 500.0;
const float R1 = 10.0;
uint16_t _countsLuminosity;

LightSensor::LightSensor(AnalogIn lightsensor) : _lightsensor(lightsensor) {
 _lightsensor = lightsensor;
}

uint16_t LightSensor::read() {
    _countsLuminosity = _lightsensor.read_u16();

    return _countsLuminosity;
}

float LightSensor::calculate_Vout(uint16_t counts) {
    return (counts*VREF)/ADCRES;
}

float LightSensor::calculate_Lux(float vout) {
   float lux = ((((VREF*lUXREL)*vout) - lUXREL) / R1);
    if (lux < 0) {
        lux = 0;
    }
    return lux;
}