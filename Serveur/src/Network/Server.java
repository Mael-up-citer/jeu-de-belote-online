package src.Network;

import java.io.*;
import java.net.*;
import java.util.*;



public class Server {
    private static final int PORT = 12345;
    private static final HashMap<Integer, LobbyManager> lobbyManagers = new HashMap<>();
    private static int gameCounter = 0;



    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Serveur lancé sur le port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(new PlayerConnection(clientSocket))).start();
        }
    }


    public static HashMap<Integer, LobbyManager> getLobbyManagers() {
        return lobbyManagers;
    }



    public static class ClientHandler implements Runnable {
        PlayerConnection playerCo;
        private boolean isRunning = true;
        private int currentGameId = -1;   // Id unique de la game



        public ClientHandler(PlayerConnection playerCo) {
            this.playerCo = playerCo;
        }


        @Override
        public void run() {
            try {
                String input;

                while (isRunning && (input = playerCo.readMessage()) != null) {
                    System.out.println("recu = " + input);

                    if (input.startsWith("create_game")) {
                        String[] parts = input.split("\\s+");

                        if (parts.length == 3) {
                            String composition = parts[1];
                            int numberOfHumans = Integer.parseInt(parts[2]);
                            handleGameCreation(composition, numberOfHumans);
                        }
                        else playerCo.sendMessage("Erreur: Mauvais format de commande.");
                    }
                    else if (input.startsWith("join_game")) {
                        String[] parts = input.split("\\s+");

                        if (parts.length == 2) {
                            String str = parts[1]; // Exemple : "1234"
                            char dernierChar = str.charAt(str.length() - 1); // Récupère le dernier caractère, ici '4'
                            
                            int gameId = -1;
                            try {
                                gameId = Integer.parseInt(String.valueOf(dernierChar)); // Convertit le caractère en entier
                                currentGameId = gameId;
                            } catch (NumberFormatException e) {
                                System.out.println("Le dernier caractère n'est pas un chiffre.");
                            }
                            handleJoinGame(gameId);
                        }
                        else playerCo.sendMessage("Erreur: Mauvais format de commande.");
                    }
                    else if (input.startsWith("RESUME")) handlePlayerReady();
                    else playerCo.sendMessage("Commande inconnue.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void handleGameCreation(String composition, int numberOfHumans) {
            int gameId;
            synchronized (Server.getLobbyManagers()) {
                gameId = ++Server.gameCounter;
                currentGameId = gameId;

                if (Server.getLobbyManagers().containsKey(gameId)) {
                    playerCo.sendMessage("Erreur: La partie existe déjà.");
                    return;
                }

                LobbyManager manager = new LobbyManager(gameId, composition, numberOfHumans);
                Server.getLobbyManagers().put(gameId, manager);
                manager.joinPlayer(playerCo);
            }
            playerCo.sendMessage("game_"+gameId);
        }


        private void handleJoinGame(int gameId) {
            synchronized (Server.getLobbyManagers()) {
                if (!Server.getLobbyManagers().containsKey(gameId)) {
                    playerCo.sendMessage("Erreur: La partie n'existe pas.");
                    return;
                }

                LobbyManager manager = Server.getLobbyManagers().get(gameId);
                boolean result = manager.joinPlayer(playerCo);

                if (!result) playerCo.sendMessage("Erreur: La partie est déjà complète.");
                else playerCo.sendMessage("game_"+gameId);
            }
        }


        // Nouvelle méthode pour gérer l'état "prêt" du joueur
        private void handlePlayerReady() {
            synchronized (Server.getLobbyManagers()) {
                // Récupère le lobby où le joueur est
                if (Server.getLobbyManagers().containsKey(currentGameId)) {
                    System.out.println("le joueur est pret");
                    LobbyManager manager = Server.getLobbyManagers().get(currentGameId);
                    manager.setPlayerReady(playerCo);
                    stopClientHandler();  // Appel de stop une fois que le joueur est prêt
                }
            }
        }


        private void stopClientHandler() {
            isRunning = false;
            Thread.currentThread().interrupt();  // Interrompt le thread pour le stopper proprement
        }
    }
}