package ui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * The main method of the application is in this class.
 * @author Walter
 */
public class BluetoothSerialApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //remove window decoration
        stage.initStyle(StageStyle.UNDECORATED);
        Platform.setImplicitExit(false);

        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();

        Platform.runLater(new Runnable() {
            public void run() {
                try {
                    System.out.println("Software Version 1.0.0");
                    // Delay for user to view first scene
                    Thread.sleep(10000);
                    stage.hide();
                    // Show the user interface now
                    Stage newStage = new Stage();
                    Parent root = FXMLLoader.load(getClass().getResource("UserInterface.fxml"));
                    Scene scene = new Scene(root);
                    newStage.setScene(scene);
                    newStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent t) {
                            Platform.exit();
                            System.exit(0);
                        }
                    });
                    newStage.setResizable(false);
                    newStage.setTitle("Serial Bluetooth App");
                    Image icon = new Image(FXMLDocumentController.class.getResource("/images/bluetooth_icon.png").toExternalForm(), false);
                    newStage.getIcons().add(icon);
                    newStage.show();
                } catch (InterruptedException ex) {
                    Logger.getLogger(BluetoothSerialApp.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(BluetoothSerialApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
