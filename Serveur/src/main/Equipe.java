package src.main;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class Equipe {
    private Joueur j1;
    private Joueur j2;
    private int score = 0;
    private boolean aPris = false;
    private ArrayList<Plis> plis = new ArrayList<>();   // Liste des plis de l'equipe pour un tour
    private boolean dixDeDer = false;



    public Equipe() {}


    public Equipe(Joueur j1, Joueur j2) {
        this.j1 = j1;
        this.j2 = j2;
    }


    public void addPlie(Plis p) {
        plis.add(p);
    }
    

    public void addJoueur(Joueur j) throws IllegalStateException {
        if (j1 == null)
            j1 = j;
        else if (j2 == null)
            j2 = j;
        else
            throw new IllegalStateException("L'équipe est pleine !");
    }


    /**
     * Vérifie si cette équipe est égale à une autre.
     * Deux équipes sont considérées comme égales si elles ont les mêmes joueurs,
     * le même score, le même état de prise, le même nombre de belote/rebelote et les mêmes plis.
     *
     * @param obj l'objet à comparer avec cette équipe
     * @return true si les deux équipes sont égales, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        Equipe equipe = (Equipe) obj;
        return score == equipe.score &&
            aPris == equipe.aPris &&
            getBeloteReBelote() == equipe.getBeloteReBelote() &&
            ((j1 == null && equipe.j1 == null) || (j1 != null && j1.equals(equipe.j1))) &&
            ((j2 == null && equipe.j2 == null) || (j2 != null && j2.equals(equipe.j2))) &&
            plis.equals(equipe.plis);
    }


    public Joueur getJ1() {
        return j1;
    }


    public Joueur getJ2() {
        return j2;
    }


    public int getScore() {
        return score;
    }


    public List<Joueur> getJoueurs() {
        return Arrays.asList(j1, j2);
    }

    public ArrayList<Plis> getPlis() {
        return plis;
    }

    public boolean getBeloteReBelote() {
        return ((j1.hasSayBeloteAndRe && j1.hasBeloteAndRe) ||
            (j2.hasSayBeloteAndRe && j2.hasBeloteAndRe));
    }

    public boolean getDixDeDer() {
        return dixDeDer;
    }

    public boolean getAPris() {
        return aPris;
    }

    public void setDixDeDer(boolean dixDeDer) {
        this.dixDeDer = dixDeDer;
    }


    public void setScore(int score) {
        this.score = score;
    }


    public void setAPris(boolean aPris) {
        this.aPris = aPris;
    }
}