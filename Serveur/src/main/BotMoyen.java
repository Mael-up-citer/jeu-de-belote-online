package src.main;

import src.main.Paquet.Carte;



/**
 * Classe représentant un bot intermédiaire.
 */
class BotMoyen extends Bot {
    public BotMoyen(String nom) {
        super(nom);
    }


    @Override
    public Paquet.Carte jouer(Plis plis) {
        final int DEEPTH = 2;   // Profondeur dans l'arbre de recherche en nombre de plis

        // L'IA donne une carte
        Carte carte = samplingMiniMaxAlphaBeta(plis, DEEPTH, 10);

        // L'ajoute dans le plis
        plis.addCard(this, carte);

        // L'enlève de la main
        removeCarte(carte);

        return carte;
    }
}