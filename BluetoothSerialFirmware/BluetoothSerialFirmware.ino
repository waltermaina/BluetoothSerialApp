/**
  A Serial Bluetooth Project
  Name: BluetoothSerialFirmware
  Purpose: 1. Controls an LED (ON/OFF) in an ESP32 board using Serial Bluetooth.
           2. Sends charger data (voltage, current, charge and temperature using 
              serial Bluetooth.
              
  @author Walter
  @version 1.0.0 14/08/2020
  
  @acknowledgement: Thanks to Neil Kolbans for his efforts in adding the support 
  to Arduino IDE Turotial on: www.circuitdigest.com
*/

#include "BluetoothSerial.h"    // Header File for Serial Bluetooth, will be added by default into Arduino

#define ACK 0X01                // Used to indicate the message is of type acknowledgement
#define CHARGER_DATA 0X02       // Used to indicate the message is of type charger data
#define DATA_SEND_DELAY 30000   // Interval at which charger data is sent
#define ACK_SEND_DELAY 5000     // Delay used to allow software to process charger data first

BluetoothSerial ESP_BT;                   // Object for Bluetooth
int incoming;                             // Used to store incoming single character data
int ackSendDelay = 0;                     // Used to determine if a delay before sending an ack is necessary
unsigned long dataSendPreviousMillis = 0; // Will store last time charger data was sent
char payload[1000];                       // Used to concatenate data to send

// Function prototype
void sendChargerData();

void setup() {
  Serial.begin(9600); //Start Serial monitor in 9600
  ESP_BT.begin("BT_Charger_Simulator"); //Name of your Bluetooth Signal
  Serial.println("Bluetooth Device is Ready to Pair");

  pinMode (LED_BUILTIN, OUTPUT);// Specify that LED pin is output
  randomSeed(analogRead(0));    // Initialize random number generator
}

void loop() {

  if (ESP_BT.available()) //Check if we receive anything from Bluetooth
  {
    incoming = ESP_BT.read(); //Read what we recevive
    Serial.print("Received:"); Serial.println(incoming);

    if (incoming == 49)
    {
      digitalWrite(LED_BUILTIN, LOW);
      memset(payload, 0, sizeof(payload)); // Clear the payload array
      payload[0] = ACK;
      char message[100] = "LED turned ON";
      strcpy(payload + 1, message);
      ESP_BT.println(payload);
      // Delay charger data sending for ack to be processed
      ackSendDelay = ACK_SEND_DELAY;
    }

    if (incoming == 48)
    {
      digitalWrite(LED_BUILTIN, HIGH);
      memset(payload, 0, sizeof(payload)); // Clear the payload array
      payload[0] = ACK;
      char message[100] = "LED turned OFF";
      strcpy(payload + 1, message);
      ESP_BT.println(payload);
      // Delay charger data sending for ack to be processed
      ackSendDelay = ACK_SEND_DELAY;
    }
  }

  // Send the charger data
  sendChargerData();
}

/**
  Sends charger data at an interval determined by the DATA_SEND_DELAY
  constant.
  @param no parameter.
  @return no return value.
*/
void sendChargerData()
{
  unsigned long currentMillis = millis();

  if (currentMillis - dataSendPreviousMillis >= (DATA_SEND_DELAY + ackSendDelay))
  {
    //delay(DATA_SEND_DELAY+ackSendDelay);
    // save the last time you sent data
    dataSendPreviousMillis = currentMillis;
    // Compose charger data message
    char message[100] = "\"Charger Data\"";
    int voltage = random(6, 12);
    int current = random(0, 5);
    int charge = random(25, 100);
    int temperature = random(15, 85);

    // Clear the payload array
    memset(payload, 0, sizeof(payload));
    payload[0] = CHARGER_DATA;
    snprintf (payload + 1, 1000, "{ \"message\":%s, \"voltage\":%d, \"current\":%d, \"charge\":%d, \"temperature\":%d }", message, voltage, current, charge, temperature);
    Serial.println(payload);

    ESP_BT.println(payload);
    // Clear delay charger data sending for ack to be processed
    ackSendDelay = 0;
    // Delay ack sending for charger data to be processed
    delay(ACK_SEND_DELAY);
  }
}

