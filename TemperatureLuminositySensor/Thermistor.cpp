#include "Thermistor.h"
#include "mbed.h"

uint16_t counts = 0;
const float RB = 100.0;
const float ADCRES = 65535.0;
const float R0 = 100.0;
const float T0 = 298.15;
const float B = 4770.0;
const float K = 273.15;
uint16_t _countsTemperature;

Thermistor::Thermistor(AnalogIn thermistor) : _thermistor(thermistor) {
 _thermistor = thermistor;
}

uint16_t Thermistor::read() {
    _countsTemperature = _thermistor.read_u16();

    return _countsTemperature;
}

float Thermistor::calculate_resistance(uint16_t counts) {
    return RB * ((ADCRES / counts) -1);
}

float Thermistor::calculate_kelvin(float resistance) {
    return 1 / (log2(resistance / R0) / B + (1 / T0));
}

float Thermistor::calculate_degrees(float kelvin){
    return kelvin - K;
}