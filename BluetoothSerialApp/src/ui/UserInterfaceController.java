package ui;

import bluetooth.BluetoothConnector;
import bluetooth.ChargerData;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

/**
 * FXML Controller class for the application's user interface
 *
 * @author Walter
 */
public class UserInterfaceController implements Initializable {

    @FXML
    private VBox mainPane;
    @FXML
    private Label lblVoltage;
    @FXML
    private Label lblCurrent;
    @FXML
    private Label lblCharge;
    @FXML
    private Label lblTemperature;
    @FXML
    private Button btnConnect;
    @FXML
    private Button btnLedOn;
    @FXML
    private Button btnLedOff;

    BluetoothConnector btConnector;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btConnector = new BluetoothConnector(this);
    }

    @FXML
    private void handleBtnConnect(ActionEvent event) {
        // Disable button to prevent multiple requests
        btnConnect.setDisable(true);

        // Take action later
        Platform.runLater(new Runnable() {
            public void run() {
                if (btnConnect.getText().equalsIgnoreCase("Connect")) {
                    // Create connection
                    btConnector.createBtConnection();
                } else {
                    // Close connection
                    btConnector.closeConnection();
                    btnConnect.setText("Connect");
                }
            }
        });
    }

    @FXML
    private void handleBtnLedOn(ActionEvent event) {
        // Turn LED on
        btConnector.turnLedOn();
    }

    @FXML
    private void handleBtnLedOff(ActionEvent event) {
        // Turn LED off
        btConnector.turnLedOff();
    }

    /**
     * A method to enable button after a connection is created
     */
    public void handleConnectionCreated() {
        btnConnect.setText("Disconnect");
        btnConnect.setDisable(false);
    }

    /**
     * A method to enable button after a connection attempt fails
     */
    public void handleConnectionFailed() {
        btnConnect.setDisable(false);
        btnConnect.setText("Connect");
    }
    
    /**
     * A method to update charger information on the user interface.
     * @param chargerData is the data to be used.
     */
    public void updateChargerInformation(ChargerData chargerData) {
        Platform.runLater(new Runnable() {
            public void run() {
                try {
                    // Compose voltage string
                    int voltage = chargerData.getVoltage();
                    String voltageString = String.valueOf(voltage) + "V";
                    lblVoltage.setText(voltageString);

                    // Compose current string
                    int current = chargerData.getCurrent();
                    String currentString = String.valueOf(current) + "A";
                    lblCurrent.setText(currentString);

                    // Compose charge string
                    int charge = chargerData.getCharge();
                    String chargeString = "   " + String.valueOf(charge) + "%";
                    lblCharge.setText(chargeString);

                    // Compose temperature string
                    int temperature = chargerData.getTemperature();
                    String temperatureString = String.valueOf(temperature) + "°C";
                    lblTemperature.setText(temperatureString);
                } catch (Exception e) {
                    System.out.println("Data update error: " + e.getMessage());
                }
            }
        });
    }
}
