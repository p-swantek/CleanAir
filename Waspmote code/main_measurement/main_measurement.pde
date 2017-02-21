#include <WaspBLE.h>
#include <WaspSensorGas_v20.h>
#include <math.h>


//Main board program, reads data from various installed sensors and transmits the data
//Currently reading CO2, CO, O3, and NO2 gasses


//definitions of the sockets for the CO, O3, and NO2 sensors
#define CO_SENSOR SENS_SOCKET4CO
#define O3_SENSOR SENS_SOCKET2B
#define NO2_SENSOR SENS_SOCKET3B

//The gain for the CO2 sensor, using the recommended gain
#define CO2_GAIN 7

//the gain, load resistance, and Ro for the CO sensor, using the recommended values (may need to optimize load resistance and Ro)
#define CO_GAIN 1
#define CO_LOAD_RESISTANCE 100
#define CO_RO 13.3

//the gain, load resistance, and Ro for the O3 sensor, using the recommended values (may need to optimize load resistance and Ro)
#define O3_GAIN 1
#define O3_LOAD_RESISTANCE 20
#define O3_RO 3


//the gain, load resistance, and Ro for the NO2 sensor, using the recommended values (may need to optimize load resistance and Ro)
#define NO2_GAIN 1
#define NO2_LOAD_RESISTANCE 3
#define NO2_RO 1

//Delay time for sensors to warm up before taking measurement
#define WARMUP 35000



//Variables to hold the various sensor outputs
float co2Volts;
float coVolts;
float o3Volts;
float no2Volts;
    
//Variables to hold the read resistances for sensors that use resistance
float coResistance;
float o3Resistance;
float no2Resistance;


//Variables to hold the resistance ratio (read resistance/load resistance) for resistance sensors
float coRatio;
float o3Ratio;
float no2Ratio;

//Variables to hold the final gas concentrations
float co2_PPM;
float co_PPM;
float o3_PPM;
float no2_PPM;


//flag for BLE events
uint8_t flag = 0;




void setup() {
    //Turn on USB
    USB.ON();
    delay(100);
    
    
    BLE.ON(SOCKET0);
    
    BLE.setDiscoverableMode(BLE_GAP_GENERAL_DISCOVERABLE);
    BLE.setConnectableMode(BLE_GAP_UNDIRECTED_CONNECTABLE);
    
    delay(120000); //wait 2 min to allow android app to establish an initial connection to the waspmote's gatt server
    
   

}


void loop() {
  
    //Turn on RTC after waking up
    RTC.ON();
    
    //Turn BLE module ON
    BLE.ON(SOCKET0); 
    
    /******** Gas board code ********/
    
    //Turn on the sensor board after waking up
    SensorGasv20.setBoardMode(SENS_ON);
    
    
    //Configure the various sensors each time board is turned on
    SensorGasv20.configureSensor(SENS_CO2, CO2_GAIN);
    SensorGasv20.configureSensor(CO_SENSOR, CO_GAIN, CO_LOAD_RESISTANCE);
    SensorGasv20.configureSensor(O3_SENSOR, O3_GAIN, O3_LOAD_RESISTANCE);
    SensorGasv20.configureSensor(NO2_SENSOR, NO2_GAIN, NO2_LOAD_RESISTANCE);
    
    
    USB.println(F("Measurement loop starting...."));
    
    //Blinking LEDs, visually indicates start of a measurement loop
    Utils.blinkLEDs(3000);
    
  
    //Turn on sensors that need to be warmed up prior to making a reading (CO2, O3, and NO2 sensors need a warmup. CO sensor doesn't need a warmup before taking a reading)
    SensorGasv20.setSensorMode(SENS_ON, SENS_CO2);
    SensorGasv20.setSensorMode(SENS_ON, O3_SENSOR);
    SensorGasv20.setSensorMode(SENS_ON, NO2_SENSOR);
    delay(WARMUP);
 
    //Read voltage output from sensors
    co2Volts = SensorGasv20.readValue(SENS_CO2);
    coVolts = SensorGasv20.readValue(CO_SENSOR);
    o3Volts = SensorGasv20.readValue(O3_SENSOR);
    no2Volts = SensorGasv20.readValue(NO2_SENSOR);
    
    //Get the read resistance based on the output voltage from the resistance sensors (CO, O3, and NO2 sensors are all resistance based sensors)
    coResistance = SensorGasv20.calculateResistance(CO_SENSOR, coVolts, CO_GAIN, CO_LOAD_RESISTANCE);
    o3Resistance = SensorGasv20.calculateResistance(O3_SENSOR, o3Volts, O3_GAIN, O3_LOAD_RESISTANCE);
    no2Resistance = SensorGasv20.calculateResistance(NO2_SENSOR, no2Volts, NO2_GAIN, NO2_LOAD_RESISTANCE);
    
    //Calculate the ratio of the read resistance and the load resistance for resistance sensors
    coRatio = coResistance / CO_RO;
    o3Ratio = o3Resistance / O3_RO;
    no2Ratio = no2Resistance / NO2_RO;

    
    /*** Print results to console ***/
 
    //Print out the percentage of battery remaining
    USB.print(F("The current % battery remaining: "));
    USB.println(PWR.getBatteryLevel(),DEC);
    
     /*** CO2 Results ***/
    USB.print(F("CO2 sensor voltage: "));
    USB.println(co2Volts);
    delay(100);
    
    USB.print(F("Current CO2 concentration(ppm): "));
    co2_PPM = co2_concentration(co2Volts);
    USB.println(co2_PPM);
    delay(100);
    
    
    /*** CO Results ***/
    USB.print(F("CO sensor voltage: "));
    USB.println(coVolts);
    delay(100);
    
    //Print out the read resistance
    //USB.print(F("CO sensor read resistance: "));
    //USB.println(coResistance);
    //delay(100);
    
    //Print out the ratio between read and load resistances
    //USB.print(F("CO sensor resistance ratio: "));
    //USB.println(coRatio);
    //delay(100);
    
    //Print out the calculated CO ppm concentration
    USB.print(F("Current CO concentration(ppm): "));
    co_PPM = co_concentration(coRatio);
    USB.println(co_PPM);
    delay(100);
    
    /*** O3 Results ***/
     USB.print(F("O3 sensor voltage: "));
    USB.println(o3Volts);
    delay(100);
    
    //Print out the read resistance
    //USB.print(F("O3 sensor read resistance: "));
    //USB.println(o3Resistance);
    //delay(100);
    
    //Print out the ratio between read and load resistances
    //USB.print(F("O3 sensor resistance ratio: "));
    //USB.println(o3Ratio);
    //delay(100);
    
    //Print out the calculated O3 ppm concentration
    USB.print(F("Current O3 concentration(ppm): "));
    o3_PPM = o3_concentration(o3Ratio);
    USB.println(o3_PPM);
    delay(100);
    
    /*** NO2 Results ***/
     USB.print(F("NO2 sensor voltage: "));
    USB.println(no2Volts);
    delay(100);
    
    //Print out the read resistance
    //USB.print(F("NO2 sensor read resistance: "));
    //USB.println(no2Resistance);
    //delay(100);
    
    //Print out the ratio between read and load resistances
    //USB.print(F("NO2 sensor resistance ratio: "));
    //USB.println(no2Ratio);
    //delay(100);
    
    //Print out the calculated O3 ppm concentration
    USB.print(F("Current NO2 concentration(ppm): "));
    no2_PPM = no2_concentration(no2Ratio);
    USB.println(no2_PPM);
    delay(100);
    
    USB.println();
    //Turn off gas board
    SensorGasv20.setBoardMode(SENS_OFF);
    
    //Turn off RTC
    RTC.OFF();
    
     /******** Gas board code end ********/


    /***** Bluetooth code *****/
    
  USB.println(F("Starting bluetooth transmission...")); 
  delay(5000);
    
     // 1. Make Waspmote visible to other BLE modules
  BLE.setDiscoverableMode(BLE_GAP_GENERAL_DISCOVERABLE);

  // 2. Make Waspmote connectable to any other BLE device
  BLE.setConnectableMode(BLE_GAP_UNDIRECTED_CONNECTABLE);
  USB.println(F("Waiting for incoming connections..."));

  // 3. Wait for connection status event during 30 seconds. 

  flag = BLE.waitEvent(30000);
  if (flag == BLE_EVENT_CONNECTION_STATUS)
  {
    USB.println(F("Connected!"));
    USB.println(F("Now Waspmote is connected as slave.")); 

    // 3.1 Parse the status event to find MAC of master device who initiated the connection.
    // NOTE: The event captured is stored in BLE.event array.
    uint8_t b = 5;
    for(uint8_t a = 0; a < 6 ; a++)
    {
      BLE.BLEDev.mac[a] = BLE.event[b+6];
      b--;
    }

    // 3.2 Print MAC of the Master device.
    USB.print(F("MASTER MAC Address: "));
    for(uint8_t i =0; i<6; i++)
    {
      USB.printHex(BLE.BLEDev.mac[i]);
    }
    USB.println();

    // 3.3 Parse connection handler. other information about status not used in this example.
    BLE.connection_handle = BLE.event[4];



    // 4 Now wait to other events forever. If disconnection is detected, exit loop and start again
    //loop counter
    int counter = 0; //avoid infinite loops

    while(flag != BLE_EVENT_CONNECTION_DISCONNECTED && counter < 20)
    {

      // 4.1 Wait for indication event 
      USB.println(F("Waiting events..."));
      flag = BLE.waitEvent(5000);

      if (flag == BLE_EVENT_ATTRIBUTES_STATUS)
      {
        USB.println(F("Status event found"));

        // 4.2 look if one attribute has been indicated. flag = 1 means that notifications are enabled.
        /* status event structure:
         Field:   | Message type | Payload| Msg Class | Method |  Handle | Flags |
         Length:  |       1      |    1   |     1     |    1   |    2    |    1  |
         Example: |      80      |   10   |     02    |   02   |    00   |   01  |
         */

        if(BLE.event[6] == 1)
        {
          // 4.3 Subscription received.
          uint16_t handler = ((uint16_t)BLE.event[5] << 8) | BLE.event[4];
          USB.print(F("The master has subscribed to notifications of the attribute with handle: "));
          USB.println(handler, DEC);

          // 4.4 Change the attribute value 5 times each 5 seconds 
          USB.println(F("Change the attribute 5 times each five seconds"));        
          for (uint8_t a = 1; a <= 100; a++)
          {
            // 4.4.1 Write the local attribute which is indicated
            
            char message[10];
            
            Utils.float2String(co2_PPM, message, 3);
            flag = BLE.writeLocalAttribute(44, message);
            delay(500);
            
            Utils.float2String(co_PPM, message, 3);
            flag = BLE.writeLocalAttribute(44, message);
            delay(500);
            
            Utils.float2String(o3_PPM, message, 3);
            flag = BLE.writeLocalAttribute(44, message);
            delay(500);
            
            Utils.float2String(no2_PPM, message, 3);
            flag = BLE.writeLocalAttribute(44, message);
            delay(500);
            
            Utils.float2String(PWR.getBatteryLevel(), message, 3);
            flag = BLE.writeLocalAttribute(44, message);

            if (flag == 0)
            {
              
              USB.println("Attribute changed!");
            }
            else
            {
              USB.print("Error writing. flag =");
              USB.println(flag, DEC);
            }

            // 4.4.2 Wait 5 seconds till change the attribute value
            delay(5000);
            break;
          }

          flag = BLE_EVENT_CONNECTION_DISCONNECTED;
       
        }
        else 
        {
          // 4.2.1 Notify subscription not received
          USB.println(F("Master not subscribed"));
        }
      }
      else
      {
        // 4.1.1 Maybe Other event found
        if (flag != 0)
        {          
          // Other event received from BLE module
          USB.print(F("Event found. "));
          USB.print("flag = ");
          USB.println(flag, DEC);
        }
        else 
        {
          // no event received. 
          USB.println(F("No event received"));
          counter++; //counter to make sure no infinite loops

        }
      }

      //4.5 get status. If not connected, exit.
      if (BLE.getStatus(BLE.connection_handle) == 0)
      {        
        BLE.disconnect(BLE.connection_handle);
        break;
      }
      

    } // end while

    // 4. if here, disconnected.
    USB.println(F("Disconnected"));    

  }
  else 
  {
    if (flag == 0)
    {
      // If there are no events, then no one tried to connect Waspmote
      USB.println(F("No events found. No devices tried to connect Waspmote."));
    }
    else
    {
      // Other event received from BLE module
      USB.print(F("Other event found. "));
      USB.print("flag = ");
      USB.println(flag, DEC);
    }
  }

  USB.println(); 
    

    /***** Bluetooth code end *****/
    
    //Go to deepsleep	
    //After 5 minutes, Waspmote wakes up thanks to the RTC Alarm
    USB.println(F("Waspmote entering sleep mode for 5 min"));
    PWR.deepSleep("00:00:05:00", RTC_OFFSET, RTC_ALM1_MODE1, ALL_OFF);
    
    
    
}

//Converts the raw voltage obtained CO2 sensor into a ppm concentration
float co2_concentration(float volts){
  
  //Conversion formula
  return pow(10, ((volts-0.220)+158.631)/62.877);
  
}

//Converts the resistance from the CO sensor into a ppm concentration
float co_concentration(float ratio){
  
  return pow(10, (log10(ratio)-2.302)/(-1.151));
  
}

//Converts the resistance from the O3 sensor into a ppm concentration
float o3_concentration(float ratio){
  
  return pow(10, (log10(ratio)-2.302)/(-1.151)) / 1000;
  
}

//Converts the resistance from the O3 sensor into a ppm concentration
float no2_concentration(float ratio){
  
  return pow(10, (log10(ratio)-2.302)/(-1.151)) / 1000;
  
}



