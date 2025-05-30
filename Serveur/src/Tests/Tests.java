package src.Tests;

import src.main.*;



/**
 * Classe de test permettant de lancer une partie entre deux équipes de bots
 * avec des niveaux spécifiés en ligne de commande.
 *
 * Usage : java Tests <niveauJoueur1> <niveauJoueur2>
 *
 * Exemple : java Tests débutant expert
 */
public class Tests {

    /**
     * Point d'entrée du programme. Attend deux arguments représentant les niveaux
     * des joueurs de chaque équipe. Crée les bots, les équipes et démarre une partie.
     *
     * @param args les niveaux des deux équipes (ex : "débutant", "expert")
     */
    public static void main(String[] args) {
        // Vérifie qu'il y a bien deux arguments fournis
        if (args.length < 2) {
            System.out.println("Usage : java Tests <niveauJoueur1> <niveauJoueur2>");
            return;
        }

        // Récupération des niveaux à partir des arguments
        String niveau1 = args[0];
        String niveau2 = args[1];

        // Création des joueurs de la première équipe avec le niveau 1
        Joueur j1 = BotFactory.creeBot("Bot1", niveau1);
        Joueur j2 = BotFactory.creeBot("Bot2", niveau1);

        // Création des joueurs de la deuxième équipe avec le niveau 2
        Joueur j3 = BotFactory.creeBot("Bot3", niveau2);
        Joueur j4 = BotFactory.creeBot("Bot4", niveau2);

        // Création de la partie avec deux équipes de deux joueurs
        Game game = new Game("PartieTest", new Equipe(j1, j2), new Equipe(j3, j4));

        // Lancement de la partie dans un nouveau thread
        new Thread(game).start();
    }
}