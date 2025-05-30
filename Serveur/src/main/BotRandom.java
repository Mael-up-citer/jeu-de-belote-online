package src.main;

import src.main.Paquet.Carte;

import java.util.List;
import java.util.Random;



/**
 * Classe représentant un bot intermédiaire.
 */
class BotRandom extends Bot {
    public BotRandom(String nom) {
        super(nom);
    }


    @Override
    public Paquet.Carte jouer(Plis plis) {

        List<Carte> possible = Rules.playable(plis, noPlayer, main);

        Random random = new Random();
        int index = random.nextInt(possible.size()); // Génère un index aléatoire dans la taille de la liste
        Carte carte = possible.get(index); // Récupère la carte à cet index

        System.out.println("carte joué: "+carte);

        // L'ajoute dans le plis
        plis.addCard(this, carte);

        // L'enlève de la main
        removeCarte(carte);

        return carte;
    }
}