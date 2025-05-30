package src.main;

import src.main.Paquet.Carte;
import src.main.Paquet.Carte.*;


import java.util.*;
import java.util.stream.Collectors;



public abstract class Bot extends Joueur {
    // Map qui associe à chaque couleur un score dans le cadre de la prise à l'atout
    protected Map<Paquet.Carte.Couleur, Integer> atoutRate = new HashMap<>();

    // Liste de Map qui associe à chaque cartes sa proba pour tous les joueurs
    public static Map<Integer, Map<Couleur, Map<Carte, Float>>> cardsProbaPerPlayer;



    Bot (String name) {
        super(name);
        hasSayBeloteAndRe = true;

        cardsProbaPerPlayer = new HashMap<>();
    }


    public static void endDistrib() {
        // Remplit les maps en attribuant une probabilité initiale de 1/4
        // à chaque joueur pour posséder chaque carte, en préservant l'ordre d'insertion
        for (int i = 0; i < Game.NB_PLAYERS; i++) {
            cardsProbaPerPlayer.put(i, new LinkedHashMap<>());
            for (Couleur c : Couleur.values()) {
                cardsProbaPerPlayer.get(i).put(c, new LinkedHashMap<>());
                // L'ordre ordinal de l'énumération est conservé par Type.values()
                for (Type t : Type.values()) {
                    Carte carte = new Carte(c, t);
                    cardsProbaPerPlayer.get(i).get(c).put(carte, 0.25f);
                }
            }
        }
    }
    


    /**************************************************************************************************
     * Implémentation de la prise a l'atout
     * ************************************************************************************************
     */


    @Override
    public Couleur parler(int tour) {
        // Si la main est vide, on passe
        if (main == null || main.isEmpty()) return null;

        final int seuil = 85;     // Défini à partir de quelle score on peut prendre
        final int seuilHaut = 115; // Défini à partir de quelle score on peut prendre en fin de partie
        Couleur color = null;   // Défini la couleur de l'atout choisit ou null si on passe

        if (atoutRate.isEmpty()) {
            // On évalue la main pour chaque couleur
            for (Paquet.Carte.Couleur couleur : main.keySet()) {
                System.out.println("score de "+couleur+" = "+evaluerScore(couleur));
                atoutRate.put(couleur, evaluerScore(couleur));
            }

            if (getEquipe().getScore() > 800 && atoutRate.get(colorAtout) > seuilHaut) color = colorAtout;
            else if (atoutRate.get(colorAtout) >= seuil) color = colorAtout;
        }
        else {
            Couleur verif = betterRate();

            if ((getEquipe().getScore() > 800 && atoutRate.get(verif) > seuilHaut)
                        ||
            (atoutRate.get(verif) >= seuil))
                color = verif;
        }
        atoutRate.clear();
        return color;
    }


    private int evaluerScore(Paquet.Carte.Couleur couleur) {
        // On simule que la couleur passée en paramètre est l'atout
        couleur.setIsAtout(true);

        int score = calculerScoreAtout(main) +
                    calculerScoreMaitresses(main) +
                    calculerBonusBelote(main) +
                    calculerBonusLongueEtCoupe(main);
        
        // Rétablir l'état initial de la couleur atout
        couleur.setIsAtout(false);
        return score;
    }


    private int calculerScoreAtout(Map<Couleur, List<Carte>> main) {
        final int POIDS_NB_ATOUT = 5;
        final float POIDS_TOTAL_POWER_ATOUT = 1.66f;

        int nbAtouts = main.get(colorAtout).size();
        int totalPowerAtout = main.get(colorAtout)
                                  .stream()
                                  .mapToInt(Paquet.Carte::getNbPoint)
                                  .sum();
        
        return POIDS_NB_ATOUT * nbAtouts + (int) (POIDS_TOTAL_POWER_ATOUT * totalPowerAtout);
    }


    private int calculerScoreMaitresses(Map<Couleur, List<Carte>> main) {
        final int POIDS_NB_MAITRESSE = 3;
        final int POIDS_TOTAL_POWER_MAITRESSE = 1;

        int nbMaitresses = 0;
        int totalPowerMaitresses = 0;

        // Parcours de toutes les couleurs sauf l'atout
        for (Map.Entry<Paquet.Carte.Couleur, List<Paquet.Carte>> entry : main.entrySet()) {
            Paquet.Carte.Couleur couleur = entry.getKey();
            if (!couleur.equals(colorAtout)) {
                List<Paquet.Carte> cartes = entry.getValue();
                int ordinalGradient = Carte.Type.AS.ordinal();
                int i = cartes.size() - 1;

                while (i >= 0 && ordinalGradient == cartes.get(i).getType().ordinal()) {
                    ordinalGradient--;
                    nbMaitresses++;
                    totalPowerMaitresses += cartes.get(i).getNbPoint();
                    i--;
                }
            }
        }
        return POIDS_NB_MAITRESSE * nbMaitresses + POIDS_TOTAL_POWER_MAITRESSE * totalPowerMaitresses;
    }


    private int calculerBonusBelote(Map<Couleur, List<Carte>> main) {
        final int BONUS_BELOTE = 20;
        boolean roi = false, dame = false;
 
        for (Paquet.Carte carte : main.get(colorAtout)) {
            if (carte.getType() == Carte.Type.ROI) roi = true;
            else if (carte.getType() == Carte.Type.DAME) dame = true;
        }
        return (roi && dame) ? BONUS_BELOTE : 0;
    }
    
    /**
     * Calcule en une seule passe la somme des bonus :
     * - BONUS_COUPE si une couleur ne contient aucune carte.
     * - BONUS_LONGUE si la couleur contient au moins SEUIL_LONGUE cartes maîtresses,
     *   avec un bonus de base et un bonus incrémental par carte supplémentaire.
     */
    private int calculerBonusLongueEtCoupe(Map<Couleur, List<Carte>> main) {
        final int BONUS_COUPE = 10;
        final int BONUS_LONGUE_DE_BASE = 20;
        final int BONUS_LONGUE_INCREMENT = 10;
        final int SEUIL_LONGUE = 2;
        
        int bonusTotal = 0;
        
        // Parcours unique de toutes les couleurs de la main
        for (List<Paquet.Carte> cartes : main.values()) {
            if (cartes.isEmpty()) bonusTotal += BONUS_COUPE;

            else {
                int ordinalGradient = Carte.Type.AS.ordinal();
                int i = cartes.size() - 1;
                int cptLongue = 0;

                while (i >= 0 && ordinalGradient == cartes.get(i).getType().ordinal()) {
                    ordinalGradient--;
                    cptLongue++;
                    i--;
                }
                if (cptLongue >= SEUIL_LONGUE)
                    bonusTotal += BONUS_LONGUE_DE_BASE + (cptLongue - SEUIL_LONGUE) * BONUS_LONGUE_INCREMENT;
            }
        }
        return bonusTotal;
    }        


    // Retourne la couleur pour laquelle on a le plus de chance de réussir
    private Couleur betterRate() {
        int max = 0;
        Couleur res = null;

        for (Couleur couleur : atoutRate.keySet()) {
            if (couleur != colorAtout && atoutRate.get(couleur) > max) {
                max = atoutRate.get(couleur);
                res = couleur;
            }
        }
        return res;
    }



    /**************************************************************************************************
     * Implémentation de exceptedMiniMax
     * ************************************************************************************************
     */


    // Implémentation de l'algorithme MiniMax pondéré par les probabilités
    protected Carte exceptedMiniMax(Plis plis, int maxDeepth) {
        // Récupère les cartes jouable par le joueur dans ce plis
        List<Carte> playable = Rules.playable(plis, noPlayer, main);
        Carte meilleureCarte = null;    // la carte qu'il faut jouer
        float meilleureValeur = Float.NEGATIVE_INFINITY; // Valeur du coup la meilleur carte

        // Set des cartes déjà jouées
        HashSet<Carte> newCartesJouees = new HashSet<>();

        // On ajoute les cartes déjà jouées
        newCartesJouees.addAll(Game.cartePlay.values()
            .stream()
            .flatMap(List::stream)
            .collect(Collectors.toSet()));

        Map<Couleur, List<Carte>> innerMain = copyMain();

        // Parcourt chaque carte jouable
        for (Carte carte : playable) {
            // Clone le pli donné et simule le coup joué
            Plis p = new Plis(plis);
            p.addCard(this, carte);

            newCartesJouees.add(carte);
            innerMain.get(carte.getCouleur()).remove(carte);

            // Calcul de la valeur de ce coup via le minValue pondéré
            float valeur = minValue(p, (noPlayer + 1) % Game.NB_PLAYERS, 0, maxDeepth, 0, newCartesJouees, innerMain);

            newCartesJouees.remove(carte);
            innerMain.get(carte.getCouleur()).add(carte);

            if (valeur > meilleureValeur) {
                meilleureValeur = valeur;
                meilleureCarte = carte;
            }
        }
        return meilleureCarte;
    }


    // Simule l'équipe adverse ici
    private float minValue(Plis plis, int noCurrentPlayeur, int deepth, int maxDeepth, float globalSum, Set<Carte> cartesJouees, Map<Couleur, List<Carte>> main) {
        Plis modele;
        float localSum = globalSum;

        // Si le pli est fini, on réinitialise le modèle et on ajoute la valeur du pli si l'équipe du bot gagne
        if (plis.getIndex() == Game.NB_PLAYERS) {
            modele = new Plis(); // Nouveau pli

            if (plis.getEquipe().equals(equipe)) localSum += plis.getValue();
            else localSum -= plis.getValue();
        }
        else modele = new Plis(plis);

        // Test terminal
        if (terminalTest(cartesJouees) || deepth == maxDeepth) return utility(localSum, main);

        // Liste de toutes les cartes possibles dans le plis
        List<Carte> playable;

        // Sinon on regarde toutes les possibilitées
        playable = Rules.successeur(plis, main, noCurrentPlayeur, cartesJouees);

        float expectedValue = 0f;  // Calcule l'espérance
        float totalProba = 0f;  // Pour normaliser au cas où les probas ne font pas 1

        // Parcour toutes les cartes possibles
        for (Carte carte : playable) {
            Plis tmp = new Plis(modele);
            tmp.addCard(Game.joueurs[noCurrentPlayeur], carte);

            cartesJouees.add(carte);

            float proba = getProbability(noCurrentPlayeur, carte);
            totalProba += proba;
            expectedValue += proba * maxValue(tmp, (noCurrentPlayeur + 1) % Game.NB_PLAYERS, deepth + 1, maxDeepth, localSum, cartesJouees, main);

            cartesJouees.remove(carte);
        }
        return totalProba > 0 ? expectedValue / totalProba : 0f; // Normalisation
    }


    // Simule le jeu de l'équipe du joueur en cours
    private float maxValue(Plis plis, int noCurrentPlayeur, int deepth, int maxDeepth, float globalSum, Set<Carte> cartesJouees, Map<Couleur, List<Carte>> main) {
        Plis modele;
        float localSum = globalSum;
    
        // Si le pli est fini
        if (plis.getIndex() == Game.NB_PLAYERS) {
            modele = new Plis(); // Nouveau pli

            if (plis.getEquipe().equals(equipe)) localSum += plis.getValue();
            else localSum -= plis.getValue();
        }
        else modele = new Plis(plis);

        if (terminalTest(cartesJouees) || deepth == maxDeepth) return utility(localSum, main);

        // Liste de toutes les cartes possibles dans le plis
        List<Carte> playable;

        // Si c'est le tour du joueur, on joue normalement
        if (noCurrentPlayeur == noPlayer) {
            playable = Rules.playable(plis, noPlayer, main);
            float bestValeur = Float.NEGATIVE_INFINITY;

            for (Carte carte : playable) {
                Plis tmp = new Plis(modele);
                tmp.addCard(Game.joueurs[noCurrentPlayeur], carte);

                cartesJouees.add(carte);
                main.get(carte.getCouleur()).remove(carte);

                float brancheValue = minValue(tmp, (noCurrentPlayeur + 1) % Game.NB_PLAYERS, deepth + 1, maxDeepth, localSum, cartesJouees, main);

                cartesJouees.remove(carte);
                main.get(carte.getCouleur()).add(carte);

                bestValeur = Math.max(bestValeur, brancheValue);
            }
            return bestValeur;
        }
        // Sinon, on prend en compte les probabilités car c'est un joueur dont les cartes ne sont pas connues
        else {
            playable = Rules.successeur(plis, main, noCurrentPlayeur, cartesJouees);

            float expectedValue = 0f;
            float totalProba = 0f;

            for (Carte carte : playable) {
                Plis tmp = new Plis(modele);
                tmp.addCard(Game.joueurs[noCurrentPlayeur], carte);
                cartesJouees.add(carte);

                float proba = getProbability(noCurrentPlayeur, carte);
                totalProba += proba;
                expectedValue += proba * minValue(tmp, (noCurrentPlayeur + 1) % Game.NB_PLAYERS, deepth + 1, maxDeepth, localSum, cartesJouees, main);

                cartesJouees.remove(carte);
            }
            // Normalisation pour éviter un retour à 0 si totalProba == 0
            return totalProba > 0 ? expectedValue / totalProba : 0f;
        }
    }



    /**************************************************************************************************
     * Implémentation de exceptedMiniMaxAlphaBeta
     * ************************************************************************************************
     */


    // Implémentation de l'algorithme MiniMax pondéré par les probabilités
    protected Carte exceptedMiniMaxAlphaBeta(Plis plis, int maxDeepth) {
        // Récupère les cartes jouable par le joueur dans ce plis
        List<Carte> playable = Rules.playable(plis, noPlayer, main);
        Carte meilleureCarte = null;    // la carte qu'il faut jouer
        float meilleureValeur = Float.NEGATIVE_INFINITY; // Valeur initiale très basse

        // Le set est initialisé avec la main du joueur
        Set<Carte> newCartesJouees = new HashSet<>();

        // On ajoute aussi les cartes déjà jouées
        newCartesJouees.addAll(Game.cartePlay.values()
            .stream()
            .flatMap(List::stream)
            .collect(Collectors.toSet()));

        Map<Couleur, List<Carte>> innerMain = copyMain();

        // Parcourt chaque carte jouable
        for (Carte carte : playable) {

            // Clone le pli donné et simule le coup joué
            Plis p = new Plis(plis);
            p.addCard(this, carte);

            // Ajoute la carte que l'on vient de jouer
            newCartesJouees.add(carte);
            innerMain.get(carte.getCouleur()).remove(carte);

            // Calcul de la valeur de ce coup via le minValue pondéré
            float valeur = minValueAlphaBeta(p, (noPlayer + 1) % Game.NB_PLAYERS, 0, maxDeepth, 0, newCartesJouees, innerMain, meilleureValeur, Float.MAX_VALUE);

            // Ajoute la carte que l'on vient de jouer
            newCartesJouees.remove(carte);
            innerMain.get(carte.getCouleur()).add(carte);

            if (valeur > meilleureValeur) {
                meilleureValeur = valeur;
                meilleureCarte = carte;
            }
        }
        return meilleureCarte;
    }


    // Simule l'équipe adverse ici
    private float minValueAlphaBeta(Plis plis, int noCurrentPlayeur, int deepth, int maxDeepth, float globalSum, Set<Carte> cartesJouees, Map<Couleur, List<Carte>> main, float alpha, float beta) {
        Plis modele;
        float localSum = globalSum;

        // Si le pli est fini, on réinitialise le modèle et on ajoute la valeur du pli si l'équipe du bot gagne
        if (plis.getIndex() == Game.NB_PLAYERS) {
            modele = new Plis(); // Nouveau pli

            // Si on gagne le plis
            if (plis.getEquipe().equals(equipe))
            {
                localSum += plis.getValue();
                return maxValueAlphaBeta(modele, plis.getWinner(), deepth+1, maxDeepth, localSum, cartesJouees, main, alpha, beta);
            }
            // Si non on le perds
            else {
                localSum -= plis.getValue();
                return minValueAlphaBeta(modele, plis.getWinner(), deepth+1, maxDeepth, localSum, cartesJouees, main, alpha, beta);
            }
        }
        else modele = new Plis(plis);

        // Test terminal
        if (terminalTest(cartesJouees) || deepth == maxDeepth) return utility(localSum, main);

        // Liste de toutes les cartes possibles dans le plis
        List<Carte> playable;

        // Sinon on regarde toutes les possibilitées
        playable = Rules.successeur(plis, main, noCurrentPlayeur, cartesJouees);

        float expectedValue = 0f;  // Calcule l'espérance
        float totalProba = 0f;  // Pour normaliser au cas où les probas ne font pas 1, à cause des arrondis notament

        // Parcour toutes les cartes possibles
        for (Carte carte : playable) {
            Plis tmp = new Plis(modele);
            tmp.addCard(Game.joueurs[noCurrentPlayeur], carte);

            float proba = getProbability(noCurrentPlayeur, carte);
            cartesJouees.add(carte);

            totalProba += proba;
            expectedValue += proba * maxValueAlphaBeta(tmp, (noCurrentPlayeur + 1) % Game.NB_PLAYERS, deepth, maxDeepth, localSum, cartesJouees, main, alpha, beta);
                
            cartesJouees.remove(carte);

            // Calcul le cout réel
            //if ((expectedValue*totalProba) <= alpha) break;

            beta = Math.min(expectedValue, beta);
        }
        return totalProba > 0 ? expectedValue / totalProba : 0f; // Normalisation
    }


    // Simule le jeu de l'équipe du joueur en cours
    private float maxValueAlphaBeta(Plis plis, int noCurrentPlayeur, int deepth, int maxDeepth, float globalSum, Set<Carte> cartesJouees, Map<Couleur, List<Carte>> main, float alpha, float beta) {
        Plis modele;
        float localSum = globalSum;

        // Si le pli est fini
        if (plis.getIndex() == Game.NB_PLAYERS) {
            modele = new Plis(); // Nouveau pli

            // Si on gagne le plis
            if (plis.getEquipe().equals(equipe))
            {
                localSum += plis.getValue();
                return maxValueAlphaBeta(modele, plis.getWinner(), deepth+1, maxDeepth, localSum, cartesJouees, main, alpha, beta);
            }
            // Si non on le perds
            else {
                localSum -= plis.getValue();
                return minValueAlphaBeta(modele, plis.getWinner(), deepth+1, maxDeepth, localSum, cartesJouees, main, alpha, beta);
            }
        }
        else modele = new Plis(plis);

        if (terminalTest(cartesJouees) || deepth == maxDeepth) return utility(localSum, main);

        // Liste de toutes les cartes possibles dans le plis
        List<Carte> playable;

        // Si c'est le tour du joueur, on joue normalement
        if (noCurrentPlayeur == noPlayer) {
            playable = Rules.playable(plis, noPlayer, main);
            float bestValeur = Float.NEGATIVE_INFINITY;

            for (Carte carte : playable) {
                Plis tmp = new Plis(modele);
                tmp.addCard(Game.joueurs[noCurrentPlayeur], carte);

                cartesJouees.add(carte);
                main.get(carte.getCouleur()).remove(carte);

                float brancheValue = minValueAlphaBeta(tmp, (noCurrentPlayeur + 1) % Game.NB_PLAYERS, deepth, maxDeepth, localSum, cartesJouees, main, alpha, beta);
  
                cartesJouees.remove(carte);
                main.get(carte.getCouleur()).add(carte);

                bestValeur = Math.max(bestValeur, brancheValue);

                //if (bestValeur >= beta) break;

                alpha = Math.max(bestValeur, alpha);
            }
            return bestValeur;
        }
        // Sinon, on prend en compte les probabilités car c'est un joueur dont les cartes ne sont pas connues
        else {
            playable = Rules.successeur(plis, main, noCurrentPlayeur, cartesJouees);

            float expectedValue = 0f;
            float totalProba = 0f;

            for (Carte carte : playable) {
                Plis tmp = new Plis(modele);
                tmp.addCard(Game.joueurs[noCurrentPlayeur], carte);

                float proba = getProbability(noCurrentPlayeur, carte);
                cartesJouees.add(carte);

                totalProba += proba;
                expectedValue += proba * minValueAlphaBeta(tmp, (noCurrentPlayeur + 1) % Game.NB_PLAYERS, deepth, maxDeepth, localSum, cartesJouees, main, alpha, beta);

                cartesJouees.remove(carte);

                // Calcul le cout réel la valeur pour
                if ((expectedValue * proba) >= beta) break;

                alpha = Math.max(expectedValue, alpha);
            }
            // Normalisation pour éviter un retour à 0 si totalProba == 0
            return totalProba > 0 ? expectedValue / totalProba : 0f;
        }
    }



    /**************************************************************************************************
     * MCTS (pas vraiment)
     * ************************************************************************************************
     */


    /**
     * Retourne la meilleure carte à jouer en faisant K simulations sampling + α–β.
     */
    protected Carte samplingMiniMaxAlphaBeta(Plis plis, int maxDepth, int K) {
        // 1) L’ensemble des coups possibles pour ce joueur
        List<Carte> coups = Rules.playable(plis, noPlayer, main);
        int M = coups.size();

        // 2) cumuler les scores sur toutes les simulations
        double[] cumul = new double[M];

        // copie initiale des cartes déjà jouées
        Set<Carte> cartesJoueesBase = Game.cartePlay.values().stream()
            .flatMap(List::stream).collect(Collectors.toSet());

        for (int sim = 0; sim < K; sim++) {
            // 3) tirage d’une distribution complète de mains
            // Pour chaque joueur on lui tire une main
            Map<Integer,Map<Couleur,List<Carte>>> mainsSim = sampleCompleteDeal(cartesJoueesBase);

            // 4) pour chaque coup de départ, on l’évalue
            for (int i = 0; i < M; i++) {
                Carte coup0 = coups.get(i);

                // clone du pli et du set de cartes jouées
                Plis p0 = new Plis(plis);
                p0.addCard(Game.joueurs[noPlayer], coup0);
                cartesJoueesBase.add(coup0);

                // retire la carte de la main simulée du bot
                Map<Couleur,List<Carte>> mainBotSim = mainsSim.get(noPlayer);
                mainBotSim.get(coup0.getCouleur()).remove(coup0);
   
                // 5) on lance l’alpha‑beta pur à partir du joueur suivant
                float v = pureAlphaBeta(
                    p0,
                    (noPlayer + 1) % Game.NB_PLAYERS,
                    0,              // depth
                    maxDepth,
                    0f,             // somme cumulée
                    cartesJoueesBase,
                    mainsSim,
                    Float.NEGATIVE_INFINITY,
                    Float.POSITIVE_INFINITY
                );
                cumul[i] += v;

                cartesJoueesBase.remove(coup0);
                // remettre la carte dans la main simulée
                mainBotSim.get(coup0.getCouleur()).add(coup0);
            }
        }

        // 6) on calcule la moyenne et on choisit le meilleur coup
        double bestAvg = Double.NEGATIVE_INFINITY;
        Carte meilleur = null;

        for (int i = 0; i < M; i++) {
            double avg = cumul[i] / K;

            if (avg > bestAvg) {
                bestAvg = avg;
                meilleur = coups.get(i);
            }
        }
        return meilleur;
    }


    /**
     * Échantillonne une main complètes cohérentes avec les cartes déjà jouées et les proba.
     * @return map playerIndex -> main simulée (Map<Couleur,List<Carte>>)
     */
    private Map<Integer,Map<Couleur,List<Carte>>> sampleCompleteDeal(
        Set<Carte> cartesJouees
    ) {

        // 1) initialise pour chaque joueur une copie vide
        Map<Integer,Map<Couleur,List<Carte>>> res = new HashMap<>();

        for (int p = 0; p < Game.NB_PLAYERS; p++) {
            // Crée une nouvelle map vide avec une liste vide pour chaque couleur
            Map<Couleur, List<Carte>> emptyHand = new HashMap<>();

            for (Couleur couleur : main.keySet())
                emptyHand.put(couleur, new ArrayList<>());

            res.put(p, emptyHand);
        }

        // 2) le bot reçoit sa main réelle
        res.get(noPlayer).clear();
        res.get(noPlayer).putAll(copyMain());

        // 3) collecte de toutes les cartes non encore jouées ni en main du bot
        List<Carte> nonjouee = new ArrayList<>();

        // Construit toutes les cartes encore jouable
        for (Couleur coul : Couleur.values()) {
            for (Type type : Type.values()) {
                Carte carte = new Carte(coul, type);

                if (!Game.cartePlay.get(carte.getCouleur()).contains(carte) && !main.get(carte.getCouleur()).contains(carte))
                    nonjouee.add(carte);
            }
        }

        // 4) tirage aléatoire pondéré pour affecter chaque carte aux joueurs adverses
        //    en respectant Bot.cardsProbaPerPlayer.get(player).get(carte) comme poids

        // Map du nombre de carte requis par joueur
        Map<Integer, Integer> nbCardsRequired = new HashMap<>();

        for (int i  = 0; i < Game.NB_PLAYERS; i++)
            nbCardsRequired.put(i, Game.joueurs[i].main.values().stream().mapToInt(List::size).sum());

        // Mélange des cartes à distribuer
        Collections.shuffle(nonjouee);

        // Liste des joueurs éligibles (pas encore remplis)
        List<Integer> eligibles = new ArrayList<>();

        // Pour tout joueur différent de bot et pas full
        for (int i = 0; i < Game.NB_PLAYERS; i++)
            if ((i != noPlayer) && (nbCardsRequired.get(i) > 0)) eligibles.add(i);

        for (Carte c : nonjouee) {
            // Tirage pondéré restreint aux joueurs éligibles
            int tirage = weightedRandomAmong(eligibles, c);

            // Ajout de la carte
            res.get(tirage).get(c.getCouleur()).add(c);
            nbCardsRequired.put(tirage, nbCardsRequired.get(tirage) - 1);

            if (nbCardsRequired.get(tirage) <= 0)
                eligibles.remove(Integer.valueOf(tirage));
        }
        return res;
    }


    /**
     * Minimax + alpha‑beta pur sur jeu complet.
     */
    private float pureAlphaBeta(
        Plis plis,
        int currentPlayer,
        int depth,
        int maxDepth,
        float sumSoFar,
        Set<Carte> cartesJouees,
        Map<Integer,Map<Couleur,List<Carte>>> mains,
        float alpha,
        float beta
    ) {

        // 1) fin de pli
        if (plis.getIndex() == Game.NB_PLAYERS) {
            depth++;
            int winner = plis.getWinner();
            sumSoFar += (plis.getEquipe().equals(equipe) ? +plis.getValue() : -plis.getValue());

            // 2) terminal / profondeur max
            if (depth == maxDepth || terminalTest(cartesJouees)) {
                // Calcul qui a le dix de der
                sumSoFar += (plis.getEquipe().equals(equipe)) ? 10 : -10;

                return utility(sumSoFar, mains.get(noPlayer));
            }
            else {
                return pureAlphaBeta(
                    new Plis(),
                    winner,
                    depth,
                    maxDepth,
                    sumSoFar,
                    cartesJouees,
                    mains,
                    alpha,
                    beta
                );
            }
        }

        boolean isMaxNode = (currentPlayer == noPlayer || currentPlayer == (noPlayer+2) % Game.NB_PLAYERS);
        List<Carte> coups = Rules.playable(plis, currentPlayer, mains.get(currentPlayer));

        if (isMaxNode) {
            float best = Float.NEGATIVE_INFINITY;

            for (Carte c : coups) {
                Plis next = new Plis(plis);
                next.addCard(Game.joueurs[currentPlayer], c);

                cartesJouees.add(c);

                // retire de la main
                Map<Couleur,List<Carte>> mainP = mains.get(currentPlayer);
                mainP.get(c.getCouleur()).remove(c);

                best = Math.max(best, pureAlphaBeta(
                    next,
                    (currentPlayer + 1) % Game.NB_PLAYERS,
                    depth,
                    maxDepth,
                    sumSoFar,
                    cartesJouees,
                    mains,
                    alpha,
                    beta
                ));

                // restore
                mainP.get(c.getCouleur()).add(c);
                cartesJouees.remove(c);

                alpha = Math.max(alpha, best);

                if (alpha >= beta) break;
            }
            return best;
        }
        else {
            float best = Float.POSITIVE_INFINITY;

            for (Carte c : coups) {
                Plis next = new Plis(plis);
                next.addCard(Game.joueurs[currentPlayer], c);

                cartesJouees.add(c);

                Map<Couleur,List<Carte>> mainP = mains.get(currentPlayer);
                mainP.get(c.getCouleur()).remove(c);

                best = Math.min(best, pureAlphaBeta(
                    next,
                    (currentPlayer + 1) % Game.NB_PLAYERS,
                    depth,
                    maxDepth,
                    sumSoFar,
                    cartesJouees,
                    mains,
                    alpha,
                    beta
                ));
                mainP.get(c.getCouleur()).add(c);
                cartesJouees.remove(c);

                beta = Math.min(beta, best);
                if (beta <= alpha) break;
            }
            return best;
        }
    }

    

    /**************************************************************************************************
     * Méthode utilitaire pour les algo d'IA
     * ************************************************************************************************
     */

    // Test terminal : si toutes les cartes ont été jouées (par exemple, dans un jeu à 32 cartes)
    private boolean terminalTest(Set<Carte> cartesJouees) {
        //System.out.println("terminal test: "+cartesJouees.size());
        return cartesJouees.size() == 32;
    }


    // Notation des états
    // la valeur du jeu + le nombre de pts accumulé, la valeur du jeu est null si on est dans le cas terminal
    protected float utility(float globalSum, Map<Couleur, List<Carte>> mainTmp) {
        // Calcul le score de la main
        float mainScore = calculerScoreAtout(mainTmp) +
        calculerScoreMaitresses(mainTmp) +
        calculerBonusLongueEtCoupe(mainTmp);

        //System.out.println("le score de la main est de: "+mainScore);
        //System.out.println("le score du jeu est de: "+globalSum);

        return mainScore + globalSum;
    }


    private HashMap<Couleur, List<Carte>> copyMain()
    {
        // Copie complète de main avant les modifications
        HashMap<Couleur, List<Carte>> mainTmp = new HashMap<>(main.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> new ArrayList<>(e.getValue()) // Copie indépendante de chaque liste
        )));

        return mainTmp;
    }


    /**
     * Choisit aléatoirement un joueur (!= noPlayer) selon la proba que chaque
     * adversaire détienne la carte `carte`, d’après
     * cardsProbaPerPlayer : Map<playerIndex, Map<Couleur, Map<Carte,Float>>>
     */
    private int weightedRandomAmong(List<Integer> among, Carte carte) {
        final Random RNG = new Random();

        // 1) Calcule un poids pour chaque joueur de la liste among
        Map<Integer, Float> poids = new HashMap<>();
        float somme = 0f;

        for (int p : among) {
            float w = getProbability(p, carte);
            poids.put(p, w);
            somme += w;
        }

        // 2) Tirage pondéré parmi les "among"
        float r = RNG.nextFloat() * somme;
        float cumul = 0f;

        for (int p : among) {
            cumul += poids.get(p);
            if (r <= cumul) return p;
        }

        // 3) Sécurité : retourne un joueur valide au hasard dans "among"
        return among.get(RNG.nextInt(among.size()));
    }
    


    private float getProbability(int player, Carte carte) {
        Map<Couleur, Map<Carte, Float>> probaParCouleur = cardsProbaPerPlayer.get(player);

        if (probaParCouleur != null) {
            Map<Carte, Float> probaCarte = probaParCouleur.getOrDefault(carte.getCouleur(), Collections.emptyMap());
            return probaCarte.getOrDefault(carte, 0f);
        }
        return 0f;
    }
    


    /**************************************************************************************************
     * Méthode utilitaire pour les algos d'IA Partie 2 inférence et recalcule des probas
     * ************************************************************************************************
     */


    // Méthode d'inferance qui selon un plis va esayer de recalculer la main d'un jouer
    // argument 1 le plis avant que le joueur ne joue
    // argument 2 la carte joué par le joueur
    public static void inference(Plis before, Carte carte, Joueur j) {
        Carte asked = before.getPlis()[0];

        // Si le joueur est le 1er a jouer on ne déduis rien des règles
        if (asked != null) {
            // Si le joueur ne joue pas la couleur demandé c'est qu'il n'en a pas
            if (! asked.getCouleur().equals(carte.getCouleur())) {
                removeAllCardsOfColor(asked.getCouleur(), j.noPlayer);

                // Si le joueur coupe
                if (carte.getCouleur().getIsAtout())
                    // Si il sous coupe -> il a pas mieux que l'atout demandé
                    if (before.getPowerfullCard().compareTo(carte) > 0)
                        cuteHigherCards(asked, j.noPlayer);

                // Si le joueur ne coupe pas et n'a pas le pli -> pas d'atout en plus
                else if (!before.isForPlayer(j)) removeAllCardsOfColor(colorAtout, j.noPlayer);
            }
        }
        // On enlève la proba de jouer la carte qui vient d'etre joué de tout les joueurs
        for (int i = 0; i < Game.NB_PLAYERS; i++) {
            Map<Couleur, Map<Carte, Float>> proba = cardsProbaPerPlayer.get(i);
            if (proba != null)
                proba.getOrDefault(carte.getCouleur(), Collections.emptyMap()).remove(carte);
        }
    }


    // Enlève toutes les cartes d'une couleur pour un jouer donné
    private static void removeAllCardsOfColor(Couleur couleur, int noPlayer) {
        Map<Couleur, Map<Carte, Float>> proba = cardsProbaPerPlayer.get(noPlayer);

        if (proba != null) {
            Map<Carte, Float> cartes = proba.get(couleur);
            if (cartes != null) {
                // Crée une copie de l'ensemble des clés
                List<Carte> copieCartes = new ArrayList<>(cartes.keySet());
                for (Carte c : copieCartes) distributeProba(c, noPlayer);
            }
        }        
        // On coupe la map de cette couleur pour le joueur noPlayer car il n'en a plus
        cardsProbaPerPlayer.get(noPlayer).remove(couleur);
    }


    // Pour une couleur et un joueur données:
    // enlève les carte qui sont au dessus d'une certaine carte
    // Dans le proba d'un joueur donné
    private static void cuteHigherCards(Carte asked, int noPlayer) {
        Map<Carte, Float> cards = cardsProbaPerPlayer.get(noPlayer).get(asked.getCouleur());

        if (cards == null) return;

        for (Type t : Type.values()) {
            Carte c = new Carte(asked.getCouleur(), t);
            if (asked.compareTo(c) <= 0) distributeProba(c, noPlayer);
        }
    }


    // On enlève une carte du jeu d'un joueur et on distribut cette proba équitablement
    // Avec les joueurs qui ont encore des chances d'avoir cette carte
    private static void distributeProba(Carte carte, int noPlayer) {
        // On doit répartir les proba avec les autres joueurs !
        // 1. Récupère la proba de la carte à enlever
        Float cardProba = cardsProbaPerPlayer.get(noPlayer).get(carte.getCouleur()).get(carte);

        // 2. Calcul la proba a add à chaque joueur qui possède cette carte
        Float additionalProba = cardProba / nbPossibleCardHowner(carte, noPlayer);

        // 3. On l'ajoute a chaque carte joueur
        for (int i = 0; i < Game.NB_PLAYERS; i++) {
            if (i != noPlayer) {
                Map<Couleur, Map<Carte, Float>> proba = cardsProbaPerPlayer.get(i);

                if (proba != null) {
                    Map<Carte, Float> probaColor = proba.get(carte.getCouleur());

                    if (probaColor != null) {
                        Float initialProba = probaColor.get(carte);
                        if (initialProba != null)
                            probaColor.put(carte, initialProba+additionalProba);
                    }
                }
            }
        }
        // Enlève la carte dans les proba du joueur qui vient de la jouer
        cardsProbaPerPlayer.get(noPlayer).get(carte.getCouleur()).remove(carte);
    }


    // Donne le nombre de joueur qui on encore une proba d'avoir la carte
       private static int nbPossibleCardHowner(Carte carte, int noPlayer) {
        int cpt = 0;

        for (int i = 0; i < Game.NB_PLAYERS; i++) {
            if (i != noPlayer) {
                Map<Couleur, Map<Carte, Float>> proba = cardsProbaPerPlayer.get(i);
                if (proba != null) {
                    Map<Carte, Float> probaColor = proba.get(carte.getCouleur());
                    if ((probaColor != null) && (probaColor.get(carte) != null)) cpt++;
                }
            }
        }
        return cpt;
    }


    // Le joueur noPlayerAPris à pris à l'atout et possède donc la carte du milieu
    // Les autres joueurs ne peuvent plus la posséder
    public static void onAtoutSet(Carte carte, int noPlayerAPris) {
        // On marque que ce joueur à forcement la carte du milieu
        cardsProbaPerPlayer.get(noPlayerAPris).get(carte.getCouleur()).put(carte, 1f);

        // Et on enlève la carte du milieu aux autres joueurs
        for (int i = 0; i < Game.NB_PLAYERS; i++) {
            if (i != noPlayerAPris) {
                Map<Couleur, Map<Carte, Float>> proba = cardsProbaPerPlayer.get(i);
                if (proba != null) {
                    Map<Carte, Float> probaColor = proba.get(carte.getCouleur());
                    if ((probaColor != null) && (probaColor.get(carte) != null))
                        probaColor.remove(carte);
                }
            }
        }
    }
}