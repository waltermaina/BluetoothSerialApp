package bluetooth;

/**
 * ChargerData is a class that defines the data type to use for the charger data
 * received from the Bluetooth device.
 * 
 * @author Walter
 */
public class ChargerData {

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the voltage
     */
    public int getVoltage() {
        return voltage;
    }

    /**
     * @return the current
     */
    public int getCurrent() {
        return current;
    }

    /**
     * @return the charge
     */
    public int getCharge() {
        return charge;
    }

    /**
     * @return the temperature
     */
    public int getTemperature() {
        return temperature;
    }
    
    private String message;
    private int voltage;
    private int current;
    private int charge;
    private int temperature;
}
