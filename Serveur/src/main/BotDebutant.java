package src.main;


import src.main.Paquet.Carte;



/**
 * Classe représentant un bot débutant.
 */
class BotDebutant extends Bot {


    public BotDebutant(String nom) {
        super(nom);
    }


    @Override
    public Paquet.Carte jouer(Plis plis) {
        final int DEEPTH = 1;   // Profondeur dans l'arbre de recherche en plis

        // L'IA donne une carte
        Carte carte = exceptedMiniMax(plis, DEEPTH);

        // L'ajoute dans le plis
        plis.addCard(this, carte);

        // L'enlève de la main
        removeCarte(carte);

        return carte;
    }
}