package main;

import java.util.function.Consumer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Map;
import java.util.HashMap;



public class GameManager {
    private final String NAMEPUBLISH = "GameManager:message_received";

    // Table de dispatching pour associer les commandes à leurs méthodes pour la com server->GameManager
    private final Map<String, Consumer<String>> COMMANDMAPSERVER = new HashMap<>();
    // Table de dispatching pour associer les commandes à leurs méthodes pour la com GameGUI->GameManager
    private final Map<String, Consumer<String>> COMMANDMAPClIENT = new HashMap<>();
    private int beloteReBelote = 0; // 1 Si il a dit belote et 2 si il a dit les 0



    public GameManager() {
        // S'abonne à l'événement "server_response" pour recevoir la réponse du serveur
        EventManager.getInstance().subscribe("server:message_received", (eventType, data) -> {
            if (data instanceof String) {
                // Diviser la chaîne en un tableau de mots
                String[] serveurResponse = ((String) data).split(":");
                Consumer<String> handler = COMMANDMAPSERVER.get(serveurResponse[0]);

                // Si la commande est trouvée, l'exécuter
                if (handler != null)
                    handler.accept(serveurResponse[1]);  // Appel de la méthode associée à la commande
            }
        });


        // S'abonne à l'événement "Gui_response" pour recevoir la réponse de la GUI
        EventManager.getInstance().subscribe("GameGui:Gui_response", (eventType, data) -> {
            if (data instanceof String) {
                // Diviser la chaîne en un tableau de mots
                String[] guiResponse = ((String) data).split(":");
                Consumer<String> handler = COMMANDMAPClIENT.get(guiResponse[0]);

                // Si la commande est trouvée, l'exécuter
                if (handler != null)
                    handler.accept(guiResponse[1]);  // Appel de la méthode associée à la commande
            }
        });

        initializeCOMMANDMAPServer();
        initializeCOMMANDMAPClient();
    }


    /**
     * Méthode d'initialisation de la table de dispatching. 
     * Elle associe chaque commande à une méthode de traitement correspondante.
     */
    private void initializeCOMMANDMAPServer() {
        // Commande pour la comunication arrivant du serveur
        COMMANDMAPSERVER.put("GameStart", this::onGameStart);
        COMMANDMAPSERVER.put("SetMain", this::onReceiveHand);
        COMMANDMAPSERVER.put("SetMiddleCard", this::SetMiddleCard);
        COMMANDMAPSERVER.put("HasBeloteAndRe", unused -> hasBeloteAndRe());
        COMMANDMAPSERVER.put("GetAtout1", unused -> askAtout1());
        COMMANDMAPSERVER.put("GetAtout2", unused -> askAtout2());
        COMMANDMAPSERVER.put("AtoutIsSet", this::atoutIsSet);
        COMMANDMAPSERVER.put("Play", this::play);
        COMMANDMAPSERVER.put("AddCardOnGame", this::AddCardOnGame);
        COMMANDMAPSERVER.put("SetFirstPlayer", this::setFirstPlayer);
        COMMANDMAPSERVER.put("UpdateScore", this::updateScore);
        COMMANDMAPSERVER.put("End8Plis", unused -> endPlis());
    }


    /**
     * Méthode d'initialisation de la table de dispatching. 
     * Elle associe chaque commande à une méthode de traitement correspondante.
     */
    private void initializeCOMMANDMAPClient() {
        // Commande pour la comunication arrivant de la GUI
        COMMANDMAPClIENT.put("AtoutChoisi", this::setAtout);
        COMMANDMAPClIENT.put("CardPlay", this::onCardPlay);
        COMMANDMAPClIENT.put("RESUME", unused -> resume());
        COMMANDMAPClIENT.put("BeloteHandler",  unsed -> listenerBelote());
    }



    /* #################################
     * Méthodes de COMMANDMAPSERVER
     * ###############################
     */


    // Publie un event pour prévenir la GUI que la partie commence
    private void onGameStart(String noPlayer) {
        System.out.println("je vais dire a la Gui que la game start");
        EventManager.getInstance().publish(NAMEPUBLISH, "GameStart:"+noPlayer);
    }


    // Quand le serveur partage la main du joueur si elle est vide on reset la main
    // Cette méthode est dite cumulative càd la main ce voit ajouter les cartes sans supprimer ce qu'il y avait avant
    // Sauf si message est vide
    // Pour reset la main voir @ref
    private void onReceiveHand(String message) {
        if (message.equals("null"))
            resetPlayerHand();  // Si le message est "null", on réinitialise la main
        else
            // Envoie à la GUI les cartes à afficher sous forme de leur nom (type + "De" + couleur)
            EventManager.getInstance().publish(NAMEPUBLISH, "PlayerHand:"+formatForGui(message));
    }


    // Parse la String format: {CARREAU=[], TREFLE=[VALLETDeTREFLE, DAMMEDeTREFLE, ROIDeTREFLE], PIQUE=[ROIDePIQUE, ASDePIQUE], COEUR=[]}
    // Vers une string TypeDeCouleur
    private String formatForGui(String message) {
        StringBuilder res = new StringBuilder();

        // Supprimer les accolades
        message = message.substring(1, message.length() - 1);

        // Expression régulière pour capturer "COULEUR=[...]" sans couper l'intérieur des crochets
        Pattern pattern = Pattern.compile("(\\w+)=\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String couleurStr = matcher.group(1);  // Récupère la couleur
            String cartesStr = matcher.group(2);   // Récupère [liste de cartes]
            // Vérifier si la liste de cartes n'est pas vide
            if (!cartesStr.isEmpty())
                // Ajouter la couleur suivie des cartes séparées par un point-virgule
                res.append(cartesStr.replace(", ", ";")).append(";");
        }

        // Retirer le dernier point-virgule si la chaîne n'est pas vide
        if (res.length() > 0)
            res.setLength(res.length() - 1);
        
        return res.toString();
    }

    // Dit à la GUI de retourner la carte du milieu
    private void SetMiddleCard(String carte) {
        EventManager.getInstance().publish(NAMEPUBLISH, "SetMiddleCard:"+carte);
    }


    // Dit à la GUI de retourner la carte du milieu
    private void hasBeloteAndRe() {
        EventManager.getInstance().publish(NAMEPUBLISH, "HasBeloteAndRe:$");
    }


    // Transmet à la GUI qu'on attend un atout
    private void askAtout1() {
        EventManager.getInstance().publish(NAMEPUBLISH, "GetAtout1:$");
    }

    // Transmet à la GUI qu'on attend un atout
    private void askAtout2() {
        EventManager.getInstance().publish(NAMEPUBLISH, "GetAtout2:$");
    }

    // Quand l'atout a été définie
    // arg sous forme: atout+";"+noPlyAPris
    private void atoutIsSet(String arg) {
        EventManager.getInstance().publish(NAMEPUBLISH, "AtoutIsSet:"+arg);
    }

    // Previens la gui que la main a changé
    private void resetPlayerHand() {
        // Envoie un message à la GUI pour désaficher les cartes
        EventManager.getInstance().publish(NAMEPUBLISH, "ClearHand:$");
    }

    // Demande à la Gui de lui retourner une carte
    private void play(String possibiliti) {
        System.out.println("je peux jouer "+possibiliti);
        EventManager.getInstance().publish(NAMEPUBLISH, "Play:"+possibiliti);
    }

    private void AddCardOnGame(String carteJouer) {
        EventManager.getInstance().publish(NAMEPUBLISH, "AddCardOnGame:"+carteJouer.toString());
    }

    private void setFirstPlayer(String noFirstply) {
        EventManager.getInstance().publish(NAMEPUBLISH, "SetFirstPlayer:"+noFirstply);
    }

    private void updateScore(String scores) {
        EventManager.getInstance().publish(NAMEPUBLISH, "UpdateScore:"+scores);
    }


    private void endPlis() {
        EventManager.getInstance().publish(NAMEPUBLISH, "End8Plis:$");

        ServerConnection.getInstance().sendToServer(""+beloteReBelote);

        beloteReBelote = 0; // RAZ
    }


    /* #################################
     * Méthodes de COMMANDMAPClIENT
     * ###############################
     */


    // Transmet au serveur l'atout choisie
    private void setAtout(String atout) {
        ServerConnection.getInstance().sendToServer(atout); // Envoie au serveur sous forme de chaîne
    }

    // Transmet au serveur l'atout choisie
    private void onCardPlay(String carte) {
        System.out.println("je viens de jouer "+carte);
        ServerConnection.getInstance().sendToServer(carte); // Envoie au serveur sous forme de chaîne
    }

    private void resume() {
        ServerConnection.getInstance().sendToServer("RESUME");
    }

    private void listenerBelote() {
        System.out.println("BELOOOOOTEEEEE "+beloteReBelote);
        beloteReBelote++;
    }



    /* #################################
     * Méthodes de auxiliaires
     * ###############################
     */
}