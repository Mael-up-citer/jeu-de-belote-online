package main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;



/**
 * Gestionnaire central des événements de l'application.
 * Permet de souscrire, désinscrire et publier des événements.
 * 
 * Type d'event:    server:connected: indique que la connexion est établie
 *                  server:connection_error: indique une erreur dans l'établissement de la connexion
 *                  server:message_received: indique qu'un message a été recu
 *                  server:outOfRange: indique que le serveur est hor de portée
 */
public class EventManager {

    // Map des types d'événements vers leurs abonnés
    private HashMap<String, Set<EventListener>> listenersMap = new HashMap<>();

    // Instance unique du gestionnaire
    private static EventManager instance;



    // Constructeur privé pour empêcher l'instanciation externe
    private EventManager() {
    }


    /**
     * Retourne l'instance unique du gestionnaire d'événements.
     *
     * @return L'instance unique de EventManager.
     */
    public static EventManager getInstance() {
        // Crée une instance si elle n'existe pas encore
        if (instance == null)
            instance = new EventManager();

        return instance;
    }


    /**
     * Interface fonctionnelle pour gérer les événements.
     */
    @FunctionalInterface
    public interface EventListener {
        void onEvent(String eventType, Object data);
    }


    /**
     * Souscrit un écouteur à un type d'événement.
     *
     * @param eventType Le type d'événement à écouter.
     * @param listener  L'écouteur qui réagira à cet événement.
     */
    public void subscribe(String eventType, EventListener listener) {
        listenersMap.computeIfAbsent(eventType, k -> new HashSet<>()).add(listener);
    }


    /**
     * Désinscrit un écouteur d'un type d'événement.
     *
     * @param eventType Le type d'événement.
     * @param listener  L'écouteur à désinscrire.
     */
    public void unsubscribe(String eventType, EventListener listener) {
        Set<EventListener> listeners = listenersMap.get(eventType);

        if (listeners != null) listeners.remove(listener);
    }


    /**
     * Publie un événement à tous les abonnés d'un type donné.
     *
     * @param eventType Le type d'événement.
     * @param data      Les données associées à l'événement.
     */
    public void publish(String eventType, Object data) {
        Set<EventListener> listeners = listenersMap.get(eventType);

        if (listeners != null)
            for (EventListener listener : listeners)
                listener.onEvent(eventType, data);
    }
}