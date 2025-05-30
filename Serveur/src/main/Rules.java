package src.main;

import src.main.Paquet.Carte;
import src.main.Paquet.Carte.*;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;



/**
 * Classe abstraite implémentant les algorithmes de règles du jeu.
 */
public abstract class Rules {

   /**
     * Détermine les cartes jouables par un joueur dans un pli donné en appliquant les règles de la Belote.
     *
     * @param contexte Le pli en cours.
     * @param player   Le joueur dont on vérifie les cartes jouables.
     * @return Une liste des cartes jouables par le joueur.
     */
    public static List<Carte> playable(Plis contexte, int noCurrentPlayer, Map<Couleur, List<Carte>> main) {
        // Si le joueur est le premier à jouer, il peut jouer n’importe quelle carte.
        if (contexte.getPlis()[0] == null) return getAllCards(main);

        Carte carteDemande = contexte.getPlis()[0];
        boolean atoutDejaPresent = contexte.getPowerfullCard().getCouleur().getIsAtout();

        // Si la couleur demandée est un atout
        if (carteDemande.getCouleur().getIsAtout()) return playAtout(contexte, noCurrentPlayer, main);

        // Sinon, la couleur demandée n'est pas un atout
        else return playNonAtout(contexte, noCurrentPlayer, main, carteDemande, atoutDejaPresent);
    }

    /**
     * Gestion du cas où la carte demandée est un atout.
     */
    private static List<Carte> playAtout(Plis contexte, int noCurrentPlayer, Map<Couleur, List<Carte>> main) {
        List<Carte> atoutCards = getCardsOfColor(main, contexte.getPlis()[0].getCouleur());
        if (atoutCards == null || atoutCards.isEmpty())
            // Si le joueur ne possède pas d'atouts, il peut jouer n'importe quelle carte.
            return getAllCards(main);

        // On recherche les atouts qui permettent de surcouper le pli
        List<Carte> overcutCards = getOvercutCards(atoutCards, contexte.getPowerfullCard());
        // S'il en existe, le joueur doit les jouer
        if (!overcutCards.isEmpty()) return overcutCards;

        // Sinon, il doit jouer un atout, même s'il ne peut pas surcouper
        return atoutCards;
    }

    /**
     * Gestion du cas où la carte demandée n'est pas un atout.
     */
    private static List<Carte> playNonAtout(Plis contexte, int noCurrentPlayer, Map<Couleur, List<Carte>> main, Carte carteDemande, boolean atoutDejaPresent) {
        // Si le joueur peut suivre la couleur demandée, il doit le faire.
        List<Carte> cardsOfColor = getCardsOfColor(main, carteDemande.getCouleur());
        if (cardsOfColor != null && !cardsOfColor.isEmpty()) return cardsOfColor;

        // Si l'equipe du jouer a le plis, il peut jouer ce qu'il veut
        if (contexte.isForPlayer(noCurrentPlayer)) return getAllCards(main);

        // Le joueur n'a pas la couleur demandée, il doit alors couper s'il possède des atouts.
        List<Carte> atoutCards = getCardsOfColor(main, Joueur.colorAtout);
        if (atoutCards != null && !atoutCards.isEmpty()) {
            if (atoutDejaPresent) {
                // Si quelqu'un a déjà coupé, il faut tenter de surcouper.
                List<Carte> overcutCards = getOvercutCards(atoutCards, contexte.getPowerfullCard());
                // S'il ne peut pas surcouper, il est quand même obligé de couper.
                if (!overcutCards.isEmpty()) return overcutCards;
                else  return atoutCards;
            }
            // Si personne n'a encore coupé, jouer un atout.
            else return atoutCards;
        }

        // Si le joueur ne possède ni la couleur demandée ni d'atout, il peut jouer n'importe quelle carte.
        return getAllCards(main);
    }


    /**
     * Renvoie toutes les cartes de la main du joueur.
     */
    private static List<Carte> getAllCards(Map<Couleur, List<Carte>> main) {
        return main.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }


    /**
     * Renvoie la liste des cartes de la main du joueur pour une couleur donnée.
     */
    private static List<Carte> getCardsOfColor(Map<Couleur, List<Carte>> main, Couleur couleur) {
        List<Carte> cards = main.get(couleur);
        return cards != null ? new ArrayList<>(cards) : new ArrayList<>();  // Crée une nouvelle liste
    }
    


    /**
     * Renvoie la liste des atouts permettant de surcouper l'atout actuellement le plus fort dans le pli.
     */
    private static List<Carte> getOvercutCards(List<Carte> atoutCards, Carte currentPowerful) {
        List<Carte> res = new ArrayList<>();

        for (Carte carte : atoutCards)
            if (carte.compareTo(currentPowerful) > 0) res.add(carte);

        return res;
    }


    ////////////////////////////////////////////////////////////////////////////////////
    // Méthode pour l'IA : calcul des successeurs (cartes non jouées) en fonction du contexte //
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Retourne l'ensemble des cartes encore jouables dans le jeu en fonction des cartes déjà jouées Pour un joueur donnée.
     * Cette méthode est utile pour les simulations et l'IA afin de déterminer les coups possibles.
     *
     * Règles appliquées :
     * - Si aucun coup n'a été joué dans le pli, toutes les cartes non jouées sont disponibles.
     * - Si la couleur demandée est toujours présente dans le jeu, seules ces cartes sont considérées.
     * - En cas de demande d'atout, et si un atout a déjà été joué, il faut surcouper si possible.
     *
     * @param contexte      Le pli en cours.
     * @param cartesJouees  Les cartes déjà jouées, organisées par couleur.
     * @param main        La main du joueur qui exécute miniMax.
     * @return La liste des cartes non jouées filtrées selon la règle du suivi.
     */
    public static List<Carte> successeur(Plis contexte, Map<Couleur, List<Carte>> main, int noCurrentPlayer, Set<Carte> newCartejouee) {
        // Récupère les probabilités de cartes pour le joueur
        Map<Couleur, Map<Carte, Float>> possibleCardForPlayer = Bot.cardsProbaPerPlayer.get(noCurrentPlayer);
        Map<Couleur, List<Carte>> remainingCards = new HashMap<>();

        // Construction de remainingCards
        // 1. Ajout de possibleCardForPlayer
        for (Map.Entry<Couleur, Map<Carte, Float>> entry : possibleCardForPlayer.entrySet()) {
            Couleur couleur = entry.getKey();
            Map<Carte, Float> cartesAvecProba = entry.getValue();

            if (cartesAvecProba != null) {
                List<Carte> cartesRestantes = new ArrayList<>();

                for (Carte carte : cartesAvecProba.keySet()) {
                    if (carte != null) {
                        List<Carte> cartesEnMain = main.get(couleur);
                        boolean estDansMain = cartesEnMain != null && cartesEnMain.contains(carte);
                        boolean estDejaJouee = newCartejouee.contains(carte);

                        if (!estDansMain && !estDejaJouee) cartesRestantes.add(carte);
                    }
                }
                remainingCards.put(couleur, cartesRestantes);
            }
        }

        // À ce stade, remainingCards contient toutes les cartes encore possibles pour le joueur
        // On retourne le résultat filtré selon les règles du jeu
        return playable(contexte, noCurrentPlayer, remainingCards);
    }
}