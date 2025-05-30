package src.main;

/**
 * Représente un pli dans une partie de Belote.
 * Un pli est une collection de 4 cartes jouées par les joueurs.
 */
public class Plis {
    private Paquet.Carte[] plis = new Paquet.Carte[Game.NB_PLAYERS]; // Tableau des cartes du pli
    private int index = 0;  // Position d'insertion dans le tableau
    private Paquet.Carte powerfullCard = null; // Carte la plus forte du pli
    private Joueur maitre; // Joueur qui possède le pli


    public Plis() {}

    /**
     * Constructeur de copie qui crée un nouveau Plis à partir d'un Plis existant.
     * Ce constructeur réalise une copie profonde pour éviter le partage d'objets.
     * 
     * @param autre Le Plis à copier.
     */
    public Plis(Plis autre) {
        // Copie les cartes
        for (int i = 0; i < autre.index; i++) this.plis[i] = autre.plis[i];

        // Copie les autres attributs
        this.index = autre.index;
        this.powerfullCard = autre.powerfullCard;
        this.maitre = autre.maitre;
    }

    /**
     * Ajoute une carte au pli en respectant les règles de prise de pli.
     * @param j Le joueur qui joue la carte.
     * @param carte La carte jouée.
     * @throws IndexOutOfBoundsException si plus de 4 cartes sont ajoutées.
     */
    public void addCard(Joueur j, Paquet.Carte carte) throws IndexOutOfBoundsException {
        // Si c'est la première carte, elle définit la couleur demandée
        if (index == 0) {
            powerfullCard = carte;
            maitre = j;
        }
        else {
            Paquet.Carte.Couleur couleurDemandee = plis[0].getCouleur(); // Première carte jouée

            boolean carteActuelleEstAtout = carte.getCouleur().getIsAtout();
            boolean cartePowerfullEstAtout = powerfullCard.getCouleur().getIsAtout();

            // Cas 1 : Si la carte actuelle est un atout et que la plus forte n'est pas un atout, elle prend le pli
            if (carteActuelleEstAtout && !cartePowerfullEstAtout) {
                powerfullCard = carte;
                maitre = j;
                //System.out.println("coupe");
            }
            // Cas 2 : Si les deux sont atouts, on garde la plus forte
            else if (carteActuelleEstAtout && cartePowerfullEstAtout) {
                if (carte.compareTo(powerfullCard) > 0) {
                    powerfullCard = carte;
                    maitre = j;
                    //System.out.println("au desuss atout");
                }
            }
            // Cas 3 : Si la carte actuelle est de la couleur demandée et que powerfullCard n'est pas un atout
            else if (carte.getCouleur() == couleurDemandee && !cartePowerfullEstAtout) {
                if (carte.compareTo(powerfullCard) > 0) {
                    powerfullCard = carte;
                    maitre = j;
                    //System.out.println("battu");
                }
            }
        }
        // Ajouter la carte au pli
        plis[index] = carte;
        index++;
    }   


    /**
     * Calcule la valeur totale du pli en fonction des points des cartes.
     * @return La somme des points des cartes dans le pli.
     */
    public int getValue() {
        int sum = 0;

        for(int i = 0; i < plis.length; i++) sum += plis[i].getNbPoint();

        return sum;
    }


    /**
     * Réinitialise le pli pour une nouvelle manche.
     */
    public void reset() {
        for(int i = 0; i < plis.length; i++) plis[i] = null;

        index = 0;
        maitre = null;
        powerfullCard = null;
    }


    // Indique si le plis est pour l'equipe du joueur j
    public boolean isForPlayer(Joueur j) {
        return maitre.getEquipe().equals(j.getEquipe());
    }


    // Indique si le plis est pour l'equipe du joueur j
    public boolean isForPlayer(int noCurrentPlayer) {
        return maitre.getEquipe().equals(Game.joueurs[noCurrentPlayer].getEquipe());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("État du pli (").append(index).append("/").append(Game.NB_PLAYERS).append(" cartes jouées):\n");

        for (int i = 0; i < index; i++)
            sb.append(" - ").append(plis[i]).append("\n");

        sb.append("Carte la plus forte : ").append(powerfullCard != null ? powerfullCard : "Aucune").append("\n");
        sb.append("Joueur maître : ").append(maitre != null ? maitre.getNom() : "Aucun").append("\n");
        sb.append("Valeur totale du pli : ").append(getValue()).append("\n");
        return sb.toString();
    }


    /**
     * Retourne les cartes jouées dans ce pli.
     * @return Un tableau contenant les cartes du pli.
     */
    public Paquet.Carte[] getPlis() {
        return plis;
    }


    /**
     * Retourne la carte la plus forte du pli.
     * @return La carte la plus forte.
     */
    public Paquet.Carte getPowerfullCard() {
        return powerfullCard;
    }


    /**
     * Retourne le joueur maître du pli.
     * @return Le joueur ayant gagné le pli.
     */
    public Joueur getMaitre() {
        return maitre;
    }


    /**
     * Retourne l'index du joueur qui a gagné le pli.
     * @return L'index du joueur gagnant ou -1 si aucun gagnant n'est défini.
     */
    public int getWinner() {
        return maitre.noPlayer;
    }


    /**
     * Retourne l'équipe qui a remporté le pli.
     * @return L'équipe du joueur maître du pli.
     */
    public Equipe getEquipe() {
        return maitre.getEquipe();
    }

    public int getIndex() {
        return index;
    }
}