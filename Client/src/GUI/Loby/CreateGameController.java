package GUI.Loby;

import GUI.Gui;
import GUI.Game.WindowsGameController;
import main.ServerConnection;
import main.EventManager;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.input.TransferMode;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.control.ProgressIndicator;


import java.util.ArrayList;



public class CreateGameController extends Gui {
    private static final int NBPLAYER = 4; // Nombre total de joueurs
    private static final int MAX_TEAM_SIZE = 3; // Taille maximale par équipe 2 + 1label

    @FXML
    private AnchorPane mainContenair;  // Conteneur principal
    @FXML
    private ComboBox<String> selectNbHumain;  // ComboBox pour le nombre d'humains
    @FXML
    private FlowPane poolPane;  // Conteneur des joueurs disponibles
    @FXML
    private HBox teamsContainer;  // Conteneur principal des équipes (HBox)
    @FXML
    private VBox Equipe1;  // Équipe 1
    @FXML
    private VBox Equipe2;  // Équipe 2
    @FXML
    private Label errorLabel;  // Label d'erreur
    @FXML
    private Button validateButton;  // Bouton de validation
    @FXML
    private Button retournButton;  // Bouton de retour
    @FXML
    private ProgressIndicator loadIndicator; // Icone de chargement

    private final ArrayList<Button> humainButtons = new ArrayList<>();  // Liste des boutons des joueurs humains
    private final ArrayList<ComboBox<String>> botBoxes = new ArrayList<>();  // Liste des ComboBox des IA
    private int numberOfHumans; // Nombre d'humains de la partie
    // Utilisation du singleton EventManager
    private static EventManager eventManager = EventManager.getInstance();



    @FXML
    public void initialize() {
        EventManager.EventListener[] listener = new EventManager.EventListener[1] ;
        // Crée une référence à l'écouteur avant de s'abonner
        listener[0] = (eventType, data) -> {
            if (data instanceof String) {
                String serveurResponse = (String) data;
    
                // Signaler la réponse dans l'UI (appeler un update UI via Platform.runLater)
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

        // Ajout des choix à la ComboBox
        selectNbHumain.getItems().addAll("0", "1", "2", "3", "4");
        selectNbHumain.setOnAction(event -> displayAll());
        retournButton.setOnAction(event -> goBack());
    
        teamsContainer.setVisible(false);  // Cache le conteneur des équipes
        validateButton.setOnAction(e -> askToServer());

        // Activation du drag & drop pour les équipes
        setupDragAndDrop(Equipe1);
        setupDragAndDrop(Equipe2);
    
        loadIndicator.setVisible(false);
    }
    

    /**
     * Met à jour l'affichage des joueurs et bots en fonction du nombre d'humains sélectionné.
     */
    private void displayAll() {
        try {
            numberOfHumans = Integer.parseInt(selectNbHumain.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        teamsContainer.setVisible(true);

        poolPane.getChildren().clear();  // Réinitialiser le pool
        // Sauvegarde des labels des équipes
        Label labelEquipe1 = (Label) Equipe1.getChildren().get(0); // Supposons que le premier enfant est le label de l'équipe 1
        Label labelEquipe2 = (Label) Equipe2.getChildren().get(0); // Supposons que le premier enfant est le label de l'équipe 2

        // Réinitialiser les équipes (en supprimant tous les autres enfants)
        Equipe1.getChildren().clear();
        Equipe2.getChildren().clear();

        // Ajouter les labels de nouveau après avoir vidé les conteneurs
        Equipe1.getChildren().add(labelEquipe1);
        Equipe2.getChildren().add(labelEquipe2);
        humainButtons.clear();
        botBoxes.clear();

        // Ajout du joueur principal "moi"
        if (numberOfHumans > 0) {
            Button btn = createDraggableButton("Moi");
            humainButtons.add(btn);
            poolPane.getChildren().add(btn);
        }
        // Ajout des autres joueurs humains
        for (int i = 1; i < numberOfHumans; i++) {
            Button btn = createDraggableButton("Joueur " + i);
            humainButtons.add(btn);
            poolPane.getChildren().add(btn);
        }
        // Ajout des bots
        for (int i = numberOfHumans; i < NBPLAYER; i++) {
            ComboBox<String> botBox = new ComboBox<>();
            botBox.getItems().addAll("Débutant", "Intermédiaire", "Expert");
            botBox.setPromptText("Bot"+i); // Texte affiché par défaut
            makeDraggable(botBox);
            botBoxes.add(botBox);
            poolPane.getChildren().add(botBox);
        }
    }

    /**
     * Crée un bouton draggable représentant un joueur.
     */
    private Button createDraggableButton(String name) {
        Button button = new Button(name);
        makeDraggable(button);
        return button;
    }

    /**
     * Configure le drag & drop pour une équipe.
     */
    private void setupDragAndDrop(VBox equipe) {
        equipe.setOnDragOver(event -> {
            // Vérifier que l'élément est déplacé dans une équipe et qu'il y a encore de la place
            if (event.getGestureSource() != equipe && equipe.getChildren().size() < MAX_TEAM_SIZE) {
                event.acceptTransferModes(TransferMode.MOVE);  // Accepte le transfert du mouvement
                equipe.setStyle("-fx-border-color: blue; -fx-border-width: 2;");  // Met une bordure bleue pour l'effet visuel
                equipe.setEffect(new DropShadow(10, Color.BLUE));  // Effet d'ombre
            }
            event.consume();
        });

        equipe.setOnDragExited(event -> {
            // Réinitialiser l'effet visuel lorsque l'élément quitte la zone de drop
            equipe.setStyle("-fx-border-color: transparent;");
            equipe.setEffect(null);
        });

        equipe.setOnDragDropped(event -> {
            // Lorsque l'élément est déposé dans l'équipe
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                // Récupérer l'élément déplacé
                Node draggedNode = (Node) event.getGestureSource();
                movePlayerToTeam(draggedNode, equipe);  // Déplacer l'élément dans l'équipe
            }
            event.setDropCompleted(true);
            event.consume();
        });
    }

    /**
     * Rend un élément draggable (déplaçable).
     */
    private void makeDraggable(Node node) {
        double[] x = new double[1];
        double[] y = new double[1];

        node.setOnMousePressed(e -> {
            // Sauvegarder la position initiale de la souris
            x[0] = node.localToScene(e.getX(), e.getY()).getX();
            y[0] = node.localToScene(e.getX(), e.getY()).getY();
            
            // Ajouter l'effet de survol pour améliorer l'interaction
            node.setEffect(new DropShadow(10, Color.GRAY));  // Ombre lorsque l'élément est sélectionné
        });

        node.setOnMouseDragged(e -> {
            // Déplacer l'élément en fonction des mouvements de la souris
            node.setLayoutX(node.localToScene(e.getX(), e.getY()).getX() - x[0]);
            node.setLayoutY(node.localToScene(e.getX(), e.getY()).getY() - y[0]);
        });

        node.setOnMouseReleased(e -> {
            // Retirer l'effet d'ombre lorsque l'élément est relâché
            node.setEffect(null);
        });

        node.setOnDragDetected(e -> {
            // Démarre le drag and drop
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("Player");
            db.setContent(content);
            e.consume();
        });
    }

    /**
     * Déplace un joueur vers une équipe et équilibre les équipes.
     */
    private void movePlayerToTeam(Node player, VBox targetEquipe) {
        if (targetEquipe.getChildren().size() < MAX_TEAM_SIZE) {
            // Retirer l'élément de son parent actuel (FlowPane ou autre)
            mainContenair.getChildren().remove(player);

            // Ajouter l'élément à l'équipe cible (VBox)
            targetEquipe.getChildren().add(player);

            // Si nécessaire, équilibrer les équipes
            balanceTeams();
        }
    }

    /**
     * Équilibre les équipes automatiquement.
     */
    private void balanceTeams() {
        if (Equipe1.getChildren().size() == MAX_TEAM_SIZE)
            autoFillTeam(Equipe2);
        else if (Equipe2.getChildren().size() == MAX_TEAM_SIZE)
            autoFillTeam(Equipe1);
    }

    /**
     * Remplit automatiquement une équipe si l'autre est pleine.
     */
    private void autoFillTeam(VBox targetEquipe) {
        while (targetEquipe.getChildren().size() < MAX_TEAM_SIZE && !poolPane.getChildren().isEmpty()) {
            Node player = poolPane.getChildren().get(0);
            poolPane.getChildren().remove(player);
            targetEquipe.getChildren().add(player);
        }
    }

    // Envoie au serveur une demande de création de partie
    private void askToServer() {
        // Si la saisie est correcte
        if (Equipe1.getChildren().size() == MAX_TEAM_SIZE && Equipe2.getChildren().size() == MAX_TEAM_SIZE) {

            // Vérifie que toutes les ComboBox des bots sont bien renseignées
            for (ComboBox<String> botBox : botBoxes) {
                if (botBox.getValue() == null || botBox.getValue().startsWith("Sélectionnez")) {
                    errorLabel.setText("Erreur : Veuillez choisir un niveau pour tous les bots.");
                    return; // Annule l'envoi si une ComboBox est mal renseignée
                }
            }

            StringBuilder message = new StringBuilder();
            message.append("create_game ");

            for (int i = 1; i <= 2; i++) { // On commence à 1 pour éviter le label
                Node nodeEquipe1 = Equipe1.getChildren().get(i);
                Node nodeEquipe2 = Equipe2.getChildren().get(i);

                message.append(getPlayerInfo(nodeEquipe1)).append(";");
                message.append(getPlayerInfo(nodeEquipe2)).append(";");
            }
            message.append(" ");
            message.append(numberOfHumans);

            System.out.println(message);
            ServerConnection.getInstance().sendToServer(message.toString());
        }
        else errorLabel.setText("Erreur les équipes ne sont pas remplie");
    }

    private String getPlayerInfo(Node node) {
        try {
            // Si c'est un joueur humain
            if (node instanceof Button) return "humain";
            else return ""+((ComboBox<?>) node).getValue();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }  

    /**
     * Retourne à l'écran du lobby.
     */
    private void goBack() {
        loadScene("/GUI/Loby/lobyGui.fxml");
    }
}