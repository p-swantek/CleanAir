//Test of the functionality of the CO2 sensor
//Will turn on the sensor board, USB, and real time clock (RTC).
//Program will then loop, reading the value from the CO2 sensor
//every second for 5 seconds, and report the average voltage.


#include <WaspSensorGas_v20.h>
#include <math.h>

//the gain for the CO2 sensor, using the recommended gain
#define CO2_GAIN 7

//the various delay times
#define WARMUP 35000
#define MINUTE 60000

//#define NUM_READS 5


void setup() {
    
  //Turn on USB
  USB.ON();
  USB.println(F("Turning on the USB."));
  delay(100);
  
  //Turn on RTC
  RTC.ON();
  USB.println(F("Turning on the RTC."));
  RTC.setTime("16:02:15:02:03:15:30"); //set the current time to monday 3:15 pm, 2/15/16
  delay(100);
  
  //Turn on the sensor board
  SensorGasv20.setBoardMode(SENS_ON);
  USB.println(F("Turning on the sensor board."));
  delay(100);
  
  //Configure the CO2 sensor with the given gain
  SensorGasv20.configureSensor(SENS_CO2, CO2_GAIN);
  delay(500);

  
}


void loop() {
  
    // Blinking LEDs, visually indicates start of a measurement loop
    Utils.blinkLEDs(1000);
    
    //Hold the value of the voltage from CO2 sensor
    float co2Volts;// = 0.0;
    //float average;
    
    //Turn on the sensor and wait for the required warmup time
    SensorGasv20.setSensorMode(SENS_ON, SENS_CO2);
    delay(WARMUP);
 
     //Read the voltage output from the CO2 sensor every second until desired amount of readings are done
    //for (int i = 0; i < NUM_READS; i++){
        //co2Volts += SensorGasv20.readValue(SENS_CO2);
    co2Volts = SensorGasv20.readValue(SENS_CO2);
    //delay(1000);
     
    //}   
    
    //Calculate average of the amount of reads done
    //average = co2Volts / NUM_READS;
    
    //Print out a timestamp of the results
    USB.println(F("===TIMESTAMP==="));
    USB.println(RTC.getTime());
    delay(100);
    
    //Print out the averages
    //USB.print(F("Average CO2 sensor voltage: "));
    USB.print(F("CO2 sensor voltage: "));
    //USB.println(average);
    USB.println(co2Volts);
    delay(100);
    
    USB.print(F("Current CO2 concentration(ppm): "));
    USB.println(co2_concentration(co2Volts));
    
    //Turn off the sensor and wait for 2 minutes
    SensorGasv20.setSensorMode(SENS_OFF, SENS_CO2);
    delay(2*MINUTE);
    
    
}

//Converts the raw voltage obtained CO2 sensor into a ppm concentration
double co2_concentration(float volts){
  
  //Conversion formula
  return pow(10, ((volts-0.220)+158.631)/62.877);
  
}

