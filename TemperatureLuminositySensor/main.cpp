#include "mbed.h"
#include "LightSensor.h"
#include "BufferedSerial.h"
#include "Thermistor.h"


LightSensor lightsensor (A0);
Thermistor thermistor (A1);

BufferedSerial luminosityBs (D1,D0,9600);

float vOut, lux;

int main()
{
    luminosityBs.set_format(
    /* bits */ 8,
    /* parity */ BufferedSerial::None,
    /* stop bit */ 1
    );

    #define MAX_BUFFER_SIZE 18
    
    uint16_t luminosityCounts;
    uint16_t temperatureCounts;
    float kelvin;
    float resistence;
    float degrees;
    char luminosityBuf[MAX_BUFFER_SIZE];

    while (true) {

            luminosityCounts= lightsensor.read();
            vOut = lightsensor.calculate_Vout(luminosityCounts);
            lux = lightsensor.calculate_Lux(vOut);
            printf("lux: %f \n", lux);
            
            temperatureCounts = thermistor.read();
            resistence = thermistor.calculate_resistance(temperatureCounts);
            kelvin = thermistor.calculate_kelvin(resistence);
            degrees = thermistor.calculate_degrees(kelvin);
            printf("degrees: %f \n",degrees);

            sprintf(luminosityBuf, "%f,%f", lux, degrees);
            luminosityBs.write(luminosityBuf, sizeof(luminosityBuf));
        
            ThisThread::sleep_for(1s);
    }
}