package src.main;

import src.main.Paquet.Carte;
import src.main.Paquet.Carte.Couleur;


import java.util.*;



/**
 * Classe abstraite représentant un joueur, qu'il soit humain ou un bot.
 */
public abstract class Joueur {
    public static Paquet.Carte.Couleur colorAtout;  // Atout de la couleur

    protected Equipe equipe; // L'équipe à laquelle appartient le joueur
    protected String nom; // Le nom du joueur
    protected HashMap<Couleur, List<Carte>> main; // La main du joueur, organisée par couleur
    protected int noPlayer; // Nuémro du jour
    protected boolean hasBeloteAndRe = false;
    protected boolean hasSayBeloteAndRe = false;



    /**
     * Constructeur par défaut de la classe Joueur.
     */
    public Joueur() {
        this.nom = "Joueur inconnu";
        initMain();
    }


    /**
     * Constructeur de la classe Joueur avec un nom.
     *
     * @param nom Le nom du joueur.
     */
    public Joueur(String nom) {
        this.nom = nom;
        initMain();
    }


    /**
     * Initialise la main du joueur avec les couleurs de cartes disponibles.
     */
    private void initMain() {
        this.main = new HashMap<>();
        for (Paquet.Carte.Couleur couleur : Paquet.Carte.Couleur.values())
            main.put(couleur, new ArrayList<>());
    }


    // Vide les listes de cartes
    public void clearMain() {
        for (List<Carte> list : main.values()) list.clear();
    }


    /**
     * Retourne le nom du joueur.
     *
     * @return Le nom du joueur.
     */
    public String getNom() {
        return nom;
    }


    /**
     * Retourne l'équipe du joueur.
     *
     * @return L'équipe du joueur.
     */
    public Equipe getEquipe() {
        return equipe;
    }


    /**
     * Définit l'équipe du joueur.
     *
     * @param equipe L'équipe à assigner au joueur.
     */
    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }


    /**
     * Ajoute une carte à la main du joueur.
     *
     * @param carte La carte à ajouter.
     */
    public void addCard(Paquet.Carte carte) {
        Carte.Couleur key = carte.getCouleur();
        if (main.get(carte.getCouleur()) == null)
            throw new IllegalStateException("Erreur la clef: "+key+"    N'est pas défini ici");

            main.get(key).add(carte);
    }


    /**
     * Trie les cartes de la main du joueur par ordre croissant de valeur.
     */
    public void sortCard() {
        main.values().forEach(cartes -> Collections.sort(cartes));
    }


    /**
     * Méthode abstraite définissant l'action de jouer un tour.
     */
    public abstract Paquet.Carte jouer(Plis plis);


    // Supprime la carte c de la main du joueur
    protected void removeCarte(Carte c) throws IllegalArgumentException {
        if (c == null || main.get(c.getCouleur()) == null)
            throw new IllegalArgumentException("Aucune carte de cette couleur n'hésiste "+c);

        main.get(c.getCouleur()).remove(c);
    }


    /**
     * Méthode définissant l'action à réaliser pour choisir l'atout.
     */
    public abstract Paquet.Carte.Couleur parler(int tour);


    public HashMap<Paquet.Carte.Couleur, List<Paquet.Carte>> getMain() {
        return main;
    }


    public int getNoPlayer() {
        return noPlayer;
    }


    public static Paquet.Carte.Couleur getColorAtout() {
        return colorAtout;
    }


    public boolean getHasBeloteAndRe() {
        return hasBeloteAndRe;
    }


    public void setNoPlayer(int no) {
        noPlayer = no;
    }


    public void setHasBeloteAndRe(boolean hasBeloteAndRe) {
        this.hasBeloteAndRe = hasBeloteAndRe;
    }
}