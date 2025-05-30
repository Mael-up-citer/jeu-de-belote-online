package GUI.Loby;

import GUI.Gui;
import GUI.Game.WindowsGameController;
import main.EventManager;
import main.ServerConnection;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;

import javafx.scene.layout.AnchorPane;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.io.IOException;

public class JoinGameController extends Gui {

    @FXML
    private TextField idGameText; // Zone de saisie de l'id d'une partie

    @FXML
    private Button validateButton;

    @FXML
    private Button returnButton;

    @FXML
    private Label errorLabel;   // Affiche la réponse du serveur en cas de problème

    @FXML
    private ProgressIndicator loadIndicator; // Icone de chargement

    // Utilisation du singleton EventManager
    private static EventManager eventManager = EventManager.getInstance();


    @FXML
    public void initialize() {
        loadIndicator.setVisible(false);

        EventManager.EventListener[] listener = new EventManager.EventListener[1] ;
        // Crée une référence à l'écouteur avant de s'abonner
        listener[0] = (eventType, data) -> {
            if (data instanceof String) {
                String serveurResponse = (String) data;

                // Signaler la réponse dans l'UI
                Platform.runLater(() -> {
                    loadIndicator.setVisible(false);
                    if (serveurResponse.startsWith("Erreur"))
                        errorLabel.setText(serveurResponse);
                    else {
                        WindowsGameController.setIdGame(serveurResponse);
                        // Désabonnement en utilisant la même référence d'écouteur
                        eventManager.unsubscribe("server:message_received", listener[0]);
                        loadScene("/GUI/Game/game.fxml");
                    }
                });
            }
        };
        // S'abonner à l'événement "server:message_received"
        eventManager.subscribe("server:message_received", listener[0]);

        validateButton.setOnAction(e -> {
            if (idGameText.getText() == null || idGameText.getText().isEmpty()) {
                errorLabel.setText("Erreur : le champ est vide");
            } else {
                // Envoie la requête au serveur
                ServerConnection.getInstance().sendToServer("join_game " + idGameText.getText());
                loadIndicator.setVisible(true); // Affiche le chargement
            }
        });
        
        returnButton.setOnAction(e -> goBack());
    }

    private void goBack() {
        loadScene("/GUI/Loby/lobyGui.fxml");
    }
}