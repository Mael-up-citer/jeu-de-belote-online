package GUI.Loby;

import GUI.Gui;
import main.EventManager;
import main.ServerConnection;
import javafx.fxml.FXML;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

/**
 * Contrôleur pour l'interface graphique du lobby.
 * Gère l'affichage des messages du serveur, la connexion et les interactions des boutons.
 */
public class LobyGuiController extends Gui {

    @FXML
    private Label titleLabel; // Le label affichant "Bonjour, choisissez entre :"

    @FXML
    private Label errorLabel; // Le label affichant les erreurs de connexion

    @FXML
    private Button createGameButton; // Le bouton "Créer une partie"

    @FXML
    private Button joinGameButton; // Le bouton "Rejoindre une partie"

    @FXML
    private Button quitButton; // Le bouton "Quitter"



    /**
     * Méthode d'initialisation, appelée automatiquement lorsque le contrôleur est chargé.
     * Initialise l'état des composants graphiques.
     */
    @FXML
    public void initialize() {
        // Associer les actions aux boutons
        createGameButton.setOnAction(event -> startNewGame());
        joinGameButton.setOnAction(event -> joinExistingGame());
        quitButton.setOnAction(event -> quitApplication());

        // Initialiser l'état du label d'erreur : vide et invisible
        errorLabel.setVisible(false); // Caché au départ
    }


    /**
     * Méthode pour démarrer une nouvelle partie.
     */
    @FXML
    private void startNewGame() {
        loadScene("/GUI/Loby/CreateGame.fxml");
    }


    /**
     * Méthode pour rejoindre une partie existante.
     */
    @FXML
    private void joinExistingGame() {
        loadScene("/GUI/Loby/joinGame.fxml");
    }


    /**
     * Méthode pour quitter l'application.
     */
    @FXML
    private void quitApplication() {
        System.out.println("Fermeture de l'application...");
        ServerConnection.getInstance().disconnect();    // Ferme la connexion
        System.exit(0); // Fermer l'application
    }


    /**
     * Affiche un message d'erreur de connexion dans le label et désactive les boutons.
     * 
     * @param error Le message d'erreur à afficher
     */
    public void displayConnectionError(String error) {
        // Afficher le message d'erreur dans le label
        errorLabel.setText(error);
        errorLabel.setVisible(true); // Rendre le label visible

        // Désactiver les boutons pour éviter toute interaction avec le serveur
        createGameButton.setDisable(true);
        joinGameButton.setDisable(true);
    }


    /**
     * Réinitialise l'interface graphique après une connexion réussie.
     * Masque le label d'erreur et réactive les boutons.
     */
    public void resetGui() {
        // Masquer et effacer le label d'erreur
        errorLabel.setText(""); // Réinitialiser le texte
        errorLabel.setVisible(false); // Masquer le label

        // Réactiver les boutons pour permettre à l'utilisateur d'interagir
        createGameButton.setDisable(false);
        joinGameButton.setDisable(false);
    }
}