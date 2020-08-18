package bluetooth;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Vector;
import javafx.scene.control.Alert;
import javafx.stage.StageStyle;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import ui.FXMLDocumentController;
import ui.UserInterfaceController;

/**
 * BluetoothConnector provides the following services: <p>1. Bluetooth device
 * discovery.</p> <p>2. Connection to a Wemos Lolin32 Bluetooth device using its
 * friendly name.</p> <p>3. Turning ON an LED in the Wemos Lolin32 board.</p> 
 * <p>4. Turning OFF an LED in the Wemos Lolin32 board.</p> <p>5. Receives using 
 * polling, data from the Wemos Lolin32 Bluetooth device.</p>
 *
 * @author waltermaina76@gmail.com
 */
public class BluetoothConnector {

    /**
     * A collection of the devices that have been discovered.
     */
    public static final Vector/*<RemoteDevice>*/ devicesDiscovered = new Vector();

    private static String deviceName = "BT_Charger_Simulator"; // Device friendly name
    private static String deviceAddress = ""; // Bluetooth device address
    private OutputStream os;
    private InputStream is;
    private StreamConnection streamConnection;
    private boolean connected = false;
    private static final int ACK = 0X01;
    private static final int CHARGERDATA = 0X02;
    UserInterfaceController mainController;
    InputStreamRunnable isRunnable;
    
    /**
     * A constructor for the BluetoothConnector class
     * @param controller is the application's main controller
     */
    public BluetoothConnector(UserInterfaceController controller){
        mainController = controller;
    }

    /**
     * After this method is called a Bluetooth connection will be created
     * between the Wemos Lolin32 Bluetooth device and the java application.
     */
    public void createBtConnection() {

        try {
            final Object inquiryCompletedEvent = new Object();

            devicesDiscovered.clear();

            DiscoveryListener listener = new DiscoveryListener() {

                /**
                 * A method used to get the Bluetooth device address using the
                 * friendly name.
                 *
                 * @param btDevice current newly discovered device.
                 * @param cod the device class.
                 */
                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                    devicesDiscovered.addElement(btDevice);
                    try {
                        //System.out.println("     name " + btDevice.getFriendlyName(false));

                        // Get required device address
                        if (btDevice.getFriendlyName(false).equalsIgnoreCase(deviceName)) {
                            deviceAddress = btDevice.getBluetoothAddress();
                        }
                    } catch (IOException cantGetDeviceName) {
                    }
                }

                /**
                 * A method used to create notification that the inquiry is
                 * complete.
                 *
                 * @param discType
                 */
                public void inquiryCompleted(int discType) {
                    //System.out.println("Device Inquiry completed!");
                    synchronized (inquiryCompletedEvent) {
                        inquiryCompletedEvent.notifyAll();
                    }
                }

                public void serviceSearchCompleted(int transID, int respCode) {
                }

                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                }
            };

            synchronized (inquiryCompletedEvent) {
                boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
                if (started) {
                    //System.out.println("wait for device inquiry to complete...");
                    inquiryCompletedEvent.wait();
                    //System.out.println(devicesDiscovered.size() +  " device(s) found");
                    System.out.println(deviceName + " Bluetooth device with address: " + deviceAddress + " found");

                    // Connect to the device
                    String btDevUrl = "btspp://" + deviceAddress + ":2;authenticate=false;encrypt=false;master=false"; //Wemos Lolin32
                    streamConnection = (StreamConnection) Connector.open(btDevUrl);
                    os = streamConnection.openOutputStream();
                    is = streamConnection.openInputStream();
                    connected = true;
                    // Start a thread to poll the input stream
                    isRunnable = new InputStreamRunnable();
                    Thread isPollingThread = new Thread(isRunnable);
                    isPollingThread.start();

                    // Tell user
                    System.out.println("Bluetooth Connection Created");
                    showInformationAlert("Bluetooth Connection Created");
                    mainController.handleConnectionCreated();
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            // Show error alert
            showErrorAlert("Bluetooth Connection Failed: " 
            +"\n1. Try checking if Bluetooth is enabled on this computer."
            +"\n2. Try checking if the Bluetooth device is powered." 
            +"\n3. Then try again.");
            //System.out.println("Bluetooth Connection Failed");
            closeConnection();
        }
    }

    /**
     * A class that has a thread to poll the input stream
     */
    private class InputStreamRunnable implements Runnable {

        private boolean doStop = false;

        public synchronized void doStop() {
            this.doStop = true;
        }

        private synchronized boolean keepRunning() {
            return this.doStop == false;
        }

        @Override
        public void run() {
            while (keepRunning()) {
                // keep doing what this thread should do.
                try {
                    //System.out.println("PollingThread running");
                    Thread.sleep(1000);
                    byte[] b = new byte[200];

                    // Check if we received something
                    if (is.available() > 0) {
                        is.read(b);

                        // Separate the command and data parts of the message
                        int from = 1;
                        int to = b.length - 1;
                        byte[] messageArray = Arrays.copyOfRange(b, from, to);
                        String messageString = new String(messageArray).trim();

                        // Take action based on type of received message
                        if (b[0] == CHARGERDATA) {
                            System.out.println("CHARGERDATA received " + messageString);
                            handleInData(messageString);
                        } else if (b[0] == ACK) {
                            System.out.println("ACK received " + messageString);
                        } else {
                            System.out.println("Undefined message received");
                        }
                    }
                } catch (InterruptedException ex) {
                    System.out.println("Error: " + ex.getMessage());
                } catch (IOException ex) {
                    System.out.println("Error: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * A method to close the Bluetooth connection
     */
    public void closeConnection() {
        try {
            mainController.handleConnectionFailed();
            isRunnable.doStop();
            connected = false;
            os.close();
            is.close();
            streamConnection.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * A method to turn the led on
     */
    public void turnLedOn() {
        // Check if connection exists
        if (connected == true) {
            try {
                os.write("1".getBytes()); //'1' means ON and '0' means OFF
            } catch (Exception e) {
                System.out.println("turnLedOn Error: " + e.getMessage());
            }
        } else {
            // Show error alert
            showErrorAlert("Try creating the bluetooth connection first");
        }
    }

    /**
     * A method to turn the led off
     */
    public void turnLedOff() {
        // Check if connection exists
        if (connected == true) {
            try {
                os.write("0".getBytes()); //'1' means ON and '0' means OFF
            } catch (Exception e) {
                System.out.println("turnLedOff Error: " + e.getMessage());
            }
        } else {
            showErrorAlert("Try creating the bluetooth connection first");
        }
    }

    /**
     * A method to receive and display charger data
     *
     * @param chargerDataJsonString the json string containing the data
     */
    private void handleInData(String chargerDataJsonString) {

        try {
            // Convert json string to ChargerData object
            ObjectMapper mapper = new ObjectMapper();
            ChargerData chargerData = mapper.readValue(chargerDataJsonString, ChargerData.class);
            // Update the user interface
            mainController.updateChargerInformation(chargerData);

            // Print the received data
            System.out.println("Message: " + chargerData.getMessage());
            System.out.println("Voltage: " + chargerData.getVoltage());
            System.out.println("Current: " + chargerData.getCurrent());
            System.out.println("Charge: " + chargerData.getCharge());
            System.out.println("Temperature: " + chargerData.getTemperature());
        } catch (Exception e) {
            System.out.println("Error while handling charger data");
        }
    }
    
    /**
     * A method to show an error message.
     * @param errorMessage is the error message
     */
    public void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.initStyle(StageStyle.UTILITY);

        alert.showAndWait();
    }
    
    /**
     * A method to show information to the user
     * @param information is the information to show
     */
    public void showInformationAlert(String information) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText(information);
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }
}
