package main;

import GUI.Gui;
import GUI.Loby.LobyGuiController;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.stage.Stage;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App extends Application {

    private static final int MAX_RETRIES = 15; // Nombre maximal de tentatives de connexion
    private static final int RETRY_INTERVAL_MS = 1000; // Intervalle de réessai (en ms)
    private static int retryCount = 0; // Compteur des tentatives de connexion
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private LobyGuiController lobby;

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        Gui.setStage(primaryStage); // Initialise le Stage pour toutes les GUI

        // Charger et afficher la scène du lobby
        lobby = Gui.loadScene("/GUI/Loby/lobyGui.fxml");

        primaryStage.show(); // Afficher la fenêtre seulement si le chargement a réussi
        attemptConnection(); // Tente de se connecter au serveur
    }


    /**
     * Tente de se connecter au serveur avec des réessais en cas d'échec.
     */
    private void attemptConnection() {
        Platform.runLater(() -> lobby.resetGui()); // S'assurer que resetGui() est exécuté sur le thread JavaFX

        scheduler.execute(() -> {
            if (!ServerConnection.getInstance().connect()) {
                Platform.runLater(() -> lobby.displayConnectionError("Impossible de se connecter au serveur"));
                retryCount++;

                if (retryCount >= MAX_RETRIES)
                    Platform.runLater(this::showConnectionFailureAlert);
                else
                    scheduler.schedule(this::attemptConnection, RETRY_INTERVAL_MS, TimeUnit.MILLISECONDS);
            }
        });
    }


    /**
     * Affiche une alerte si la connexion échoue après plusieurs tentatives.
     */
    private void showConnectionFailureAlert() {
        Alert alert = new Alert(AlertType.ERROR,
                "Le nombre maximum de tentatives de connexion a été atteint. Abandon de la connexion.",
                ButtonType.OK);
        alert.setTitle("Erreur de Connexion");
        alert.setHeaderText(null); // Pas d'en-tête
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK)
                shutdownApp();
        });
    }


    /**
     * Ferme proprement l'application.
     */
    private void shutdownApp() {
        Platform.exit(); // Ferme la GUI
        ServerConnection.getInstance().disconnect(); // Ferme la connexion
        scheduler.shutdown(); // Arrête proprement le scheduler
        System.exit(0); // Ferme le programme
    }


    @Override
    public void stop() {
        shutdownApp(); // Fermer proprement l'application lors de l'arrêt de JavaFX
    }
}