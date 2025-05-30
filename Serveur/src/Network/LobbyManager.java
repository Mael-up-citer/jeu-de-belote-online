package src.Network;

import src.main.*;
import java.util.*;



/**
 * Gère la file d'attente des joueurs et démarre une partie dès qu'un groupe est complet.
 */
public class LobbyManager {
    private final int gameId;   // Id unique de la game
    private final String composition;
    private final int nbHumains;    // Nombre d'humains attendu
    private final List<PlayerConnection> players = new ArrayList<>();
    private final Map<PlayerConnection, Boolean> playerReadyStatus = new HashMap<>();
    private boolean gameStarted = false;    // Défini si la partie est commencée



    public LobbyManager(int gameId, String composition, int nbHumains) {
        this.gameId = gameId;
        this.composition = composition;
        this.nbHumains = nbHumains;
    }


    /**
     * Permet à un joueur de rejoindre la partie.
     * Retourne vrai si le joueur a rejoint avec succès, faux sinon.
     */
    public synchronized boolean joinPlayer(PlayerConnection pc) {
        // Partie déjà commencée ou pleine
        if (players.size() >= nbHumains || gameStarted) return false;

        players.add(pc);
        playerReadyStatus.put(pc, false); // Par défaut, un joueur n'est pas prêt

        // Si la partie est complète, attendre que tous soient prêts
        if (players.size() != nbHumains) System.out.println("Il manque encore: " + (nbHumains - players.size()) + " joueurs");

        return true;
    }


    /**
     * Met à jour l'état "prêt" d'un joueur et démarre la partie si tous les joueurs sont prêts.
     */
    public synchronized boolean setPlayerReady(PlayerConnection pc) {
        if (!playerReadyStatus.containsKey(pc)) return false;

        playerReadyStatus.put(pc, true);

        if (allPlayersReady()) startGame();

        return true;
    }


    /**
     * Vérifie si tous les joueurs sont prêts.
     */
    private boolean allPlayersReady() {
        return players.size() == nbHumains &&
               playerReadyStatus.size() == nbHumains &&
               playerReadyStatus.values().stream().allMatch(status -> status);
    }
    

    /**
     * Démarre la partie dès que tous les joueurs sont connectés et prêts.
     */
    private void startGame() {
        System.out.println("tout le monde est pret... Créer la game !");
        gameStarted = true;

        // Création des équipes
        Paire<Equipe, Equipe> equipes = parseEquipe();

        // Création du jeu et lancement dans un nouveau thread
        Game game = new Game("game_" + gameId, equipes.getFirst(), equipes.getSecond());
        new Thread(game).start();

        // Suppression du LobbyManager après démarrage de la partie pour économiser la mémoire
        removeLobbyManager();

        // Informer tous les joueurs que la partie commence
        notifyPlayers("La partie commence maintenant !");
    }


    /**
     * Construit les équipes à partir de la composition donnée.
     * @return Paire d'équipes prêtes à jouer
     */
    private Paire<Equipe, Equipe> parseEquipe() {
        String[] input = composition.split(";");

        Equipe equipe1 = new Equipe();
        Equipe equipe2 = new Equipe();
        int k = 0;  // Index d'accès dans la liste des sockets
        int j = 0;   // Numéro du bot courant

        // Remplir les équipes 
        for (int i = 0; i < 4; i++) {
            if (input[i].startsWith("humain")) {
                equipe1.addJoueur(new Humain("Joueur" + k, players.get(k)));
                k++;
            }
            else {
                equipe1.addJoueur(BotFactory.creeBot("Bot" + j, input[i]));
                j++;
            }
            i++;

            if (input[i].startsWith("humain")) {
                equipe2.addJoueur(new Humain("Joueur" + k, players.get(k)));
                k++;
            }
            else {
                equipe2.addJoueur(BotFactory.creeBot("Bot" + j, input[i]));
                j++;
            }
        }
        return new Paire<Equipe, Equipe>(equipe1, equipe2);
    }


    /**
     * Supprime ce LobbyManager du registre une fois la partie commencée.
     */
    private void removeLobbyManager() {
        synchronized (Server.getLobbyManagers()) {
            Server.getLobbyManagers().remove(gameId);  // Supprime le lobbyManager pour économiser la mémoire
        }
    }


    /**
     * Envoie un message à tous les joueurs.
     */
    private void notifyPlayers(String message) {
        for (PlayerConnection player : players) player.sendMessage(message);
    }
}