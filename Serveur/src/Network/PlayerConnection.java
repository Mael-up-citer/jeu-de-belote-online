package src.Network;

import java.io.*;
import java.net.*;



/**
 * Classe représentant une connexion réseau avec un joueur.
 * Elle encapsule les flux d'entrée et de sortie associés à un socket client,
 * permettant ainsi une communication simplifiée entre le serveur et le joueur.
 */
public class PlayerConnection {
    // Socket réseau connecté au joueur
    private Socket socket;

    // Flux d'entrée permettant de lire les messages envoyés par le joueur
    private BufferedReader in;

    // Flux de sortie permettant d'envoyer des messages au joueur
    private PrintWriter out;



    /**
     * Constructeur de la connexion d'un joueur.
     * Initialise les flux d'entrée et de sortie à partir du socket fourni.
     *
     * @param socket Le socket connecté au joueur.
     * @throws IOException Si une erreur survient lors de l'ouverture des flux.
     */
    public PlayerConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }


    /**
     * Envoie un message au joueur.
     *
     * @param message Le message à envoyer.
     */
    public void sendMessage(String message) {
        out.println(message);
    }


    /**
     * Lit un message envoyé par le joueur.
     *
     * @return Le message reçu, ou null si la connexion est coupée.
     * @throws IOException En cas d'erreur de lecture.
     */
    public String readMessage() throws IOException {
        return in.readLine();
    }


    /**
     * Vérifie si la connexion est encore active.
     *
     * @return true si la connexion est ouverte, false sinon.
     */
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }


    /**
     * Retourne le flux d'entrée pour lire les messages du joueur.
     *
     * @return BufferedReader permettant la lecture des données entrantes.
     */
    public BufferedReader getInput() {
        return in;
    }


    /**
     * Retourne le flux de sortie pour envoyer des messages au joueur.
     *
     * @return PrintWriter permettant l'écriture des données sortantes.
     */
    public PrintWriter getOutput() {
        return out;
    }


    /**
     * Retourne le socket associé à la connexion du joueur.
     *
     * @return Le socket de communication.
     */
    public Socket getSocket() {
        return socket;
    }


    /**
     * Ferme proprement la connexion avec le joueur,
     * y compris les flux d'entrée, de sortie et le socket lui-même.
     *
     * @throws IOException Si une erreur survient lors de la fermeture.
     */
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}