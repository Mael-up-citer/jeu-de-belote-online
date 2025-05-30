package src.main;

import src.Network.*;

import java.net.Socket;
import java.io.*;



/**
 * Classe représentant un joueur humain.
 * Utilise l'abstraction PlayerConnection pour la communication réseau.
 */
public class Humain extends Joueur {
    private final PlayerConnection connection;
    public boolean hasSayBelote = false;
    public boolean hasSayReBelote = false;



    /**
     * Constructeur d'un joueur humain avec un nom et une connexion réseau.
     *
     * @param nom        Le nom du joueur.
     * @param connection La connexion réseau du joueur.
     */
    public Humain(String nom, PlayerConnection connection) {
        super(nom);
        this.connection = connection;
    }


    @Override
    public Paquet.Carte.Couleur parler(int tour) {
        System.out.println("attend une réponse de " + nom);
        notifier("GetAtout" + tour + ":$");

        String atout = waitForClient();

        if (atout == null || atout.equalsIgnoreCase("Passer")) return null;
        try {
            return Paquet.Carte.Couleur.valueOf(atout.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Paquet.Carte jouer(Plis plis) {
        notifier("Play:" + Rules.playable(plis, noPlayer, main));
        String input = waitForClient();

        if (input == null || input.isEmpty()) return null;

        String[] parts = input.split(";");
        Paquet.Carte carte = Paquet.Carte.parseCarte(parts[0]);

        removeCarte(carte);

        plis.addCard(this, carte);

        return carte;
    }


    /**
     * Envoie un message au client.
     */
    public void notifier(String message) {
        connection.sendMessage(message);
    }


    /**
     * Attend un message de façon bloquante.
     */
    public String waitForClient() {
        try {
            return connection.readMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Ferme proprement la connexion.
     */
    public void endConnection() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Pour accéder à la socket si besoin.
     */
    public Socket getSocket() {
        return connection.getSocket();
    }
}