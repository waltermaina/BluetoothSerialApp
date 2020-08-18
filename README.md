# BluetoothSerialApp
A Netbeans 8 JavaFX FXML application that communicates with a Wemos Lolin32 Bluetooth device. The application can turn an LED on/off and receive data from the Bluetooth device.

# Application Requirements
1. The system shall create a Bluetooth connection with the Bluetooth device when the user clicks the connect button.
2. The system shall turn on an LED on the Bluetooth device when the user presses the LED on button.
3. The system shall turn off an LED on the Bluetooth device when the user presses the LED off button.
4. The system shall receive and display data from the Bluetooth device. The data will consist of Voltage, Current, Remaining charge and Temperature. The data will be received every 30 seconds.

# How to use
1. Clone the repository.
2. Program your Wemos Lolin32 Bluetooth device using the Arduino IDE and the firmware in the folder "BluetoothSerialFirmware" of this repository.
3. Enable Bluetooth on your computer.
4. Run the application's jar file "BluetoothSerialApp.jar" which is in the "BluetoothSerialApp\dist" folder.
5. Click the Connect button on the application's user interface to create a connection between the application and the Bluetooth device.
6. Click the LED OFF button to turn the LED on the Bluetooth device off.
7. Click the LED ON button to turn the LED on the Bluetooth device on.
8. Observe the section of the user interface that has a battery icon to see if the voltage, current, charge and temperature values change every 30 seconds.

# An image of the user interface
![Application's User Interface](/images/AppUI.png)
