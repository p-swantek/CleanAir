//Test of the functionality of the CO sensor, placed in socket 4
//Will turn on the sensor board, USB, and real time clock (RTC).
//Program will then loop, reading the value from the CO sensor
//and printing out the resulting voltage, resistance, and
//resistance ratio each time.  Pause for 30 seconds and repeat.


#include <WaspSensorGas_v20.h>

//The CO sensor is currently associated with socket 4
#define CO_SENSOR SENS_SOCKET4CO

//the gain and load resistance for the CO sensor, using the recommended values (may need to optimize resistance)
#define CO_GAIN 1
#define CO_RESISTANCE 15




//Hold the value of the voltage, read resistance, and resistance ratio for CO sensor
float coVolts;
float coResistance;
float ratio;

void setup() {
  
  
  //Turn on USB
  USB.ON();
  USB.println(F("Turning on the USB."));
  delay(100);
  
  
  //Turn on the sensor board
  SensorGasv20.setBoardMode(SENS_ON);
  USB.println(F("Turning on the sensor board."));
  delay(100);
  
  //Configure the CO sensor with the given gain and resistance
  SensorGasv20.configureSensor(CO_SENSOR, CO_GAIN, CO_RESISTANCE);
  delay(500);

  
}


void loop() {
    
    // Blinking LEDs, visually indicates start of a measurement loop
    Utils.blinkLEDs(1000);
    
    //Read sensor's voltage output
    coVolts = SensorGasv20.readValue(CO_SENSOR);
    
    //Get the sensor's read resistance using the output voltage
    coResistance = SensorGasv20.calculateResistance(CO_SENSOR, coVolts, CO_GAIN, CO_RESISTANCE);
    
    //Calculate the ratio of the sensor's read resistance and its load resistance
    ratio = coResistance / CO_RESISTANCE;
    
    
    
    //Print out the volts
    USB.print(F("CO sensor voltage: "));
    USB.println(coVolts);
    
    //Print out the read resistance
    USB.print(F("CO sensor read resistance: "));
    USB.println(coResistance);
    
    //Print out the ratio between read and load resistances
    USB.print(F("CO sensor resistance ratio: "));
    USB.println(ratio);
    
    //wait for 10 seconds
    delay(10000);
    
}

