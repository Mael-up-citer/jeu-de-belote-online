package src.main;


import java.util.*;
import java.util.function.Function;



/*
 * ************************************************************************
 * Partie des bots / IA
 * ************************************************************************
 */

public class BotFactory {
    private static final Map<String, Function<String, Joueur>> BOT_OF = new HashMap<>();

    static {
        BOT_OF.put("random", BotDebutant::new);
        BOT_OF.put("débutant", BotDebutant::new);
        BOT_OF.put("intermédiaire", BotMoyen::new);
        BOT_OF.put("expert", BotExpert::new);
    }


    /**
     * Crée un bot avec un nom personnalisé et un niveau spécifié.
     *
     * @param nom    Le nom du bot.
     * @param niveau Le niveau du bot.
     * @return Une instance du bot correspondant.
     */
    public static Joueur creeBot(String nom, String niveau) {
        // Extrait la fonction associé
        Function<String, Joueur> botSupplier = BOT_OF.get(niveau.toLowerCase());
        if (botSupplier == null)    // Si aucune ne correspond
            throw new IllegalArgumentException("Erreur, la difficulté: " + niveau + " n'est pas connue");

        return botSupplier.apply(nom);  // Retourne une nouvelle instance
    }
}