
# Jeu de Belote en Ligne

## Description

Ce projet implémente un jeu de **Belote** en ligne en utilisant **Java** et **JavaFX**. Il permet à deux équipes de s'affronter en temps réel avec des agents IA ou en mode multijoueur local. Le serveur centralise la logique du jeu et la gestion des règles, tandis que le client permet aux joueurs d'interagir avec l'interface graphique.

## Structure du Projet

Le projet est divisé en plusieurs classes principales qui gèrent la logique du jeu, l'IA et l'interface utilisateur :
(dans Serveur)
- **Classe Bot** : Contient les algorithmes d'IA, qui implémentent les stratégies des joueurs contrôlés par l'ordinateur.
- **Classe Rules** : Assure le respect des règles du jeu de la Belote, y compris le comptage des points, la gestion des plis et des enchères, ainsi que les cas spéciaux comme la Belote-Rebelote et le dernier pli.
- **Classe Game** : Centralise la gestion du jeu, y compris la création de la partie, la gestion des joueurs et l'exécution des tests.

## Compilation et Lancement

### 1. Prérequis

Assurez-vous d'avoir **Java 8** ou supérieur installé sur votre machine. Vous pouvez vérifier la version de Java avec la commande suivante :

```bash
java -version
```

Le projet utilise **JavaFX** pour l'interface graphique. JavaFX n'est pas inclus dans ce dépôt pour éviter d’alourdir le versionnage avec des bibliothèques externes.

### Étapes à suivre :
1. Télécharger JavaFX SDK version **21.0.5** depuis le site officiel : https://gluonhq.com/products/javafx/
2. Extraire l’archive téléchargée.
3. Copier le dossier extrait `javafx-sdk-21.0.5` dans :
   - `Client/lib/`
   - `Serveur/lib/`
   

## .gitignore

Le fichier `.gitignore` ignore automatiquement tous les dossiers nommés `javafx-sdk-21.0.5`, où qu’ils se trouvent dans l’arborescence du projet.

### 2. Compilation et Lancement

Pour lancer le jeu, vous devez d'abord compiler et exécuter le projet. Le projet utilise **JavaFX** pour l'interface utilisateur, donc assurez-vous d'avoir les dépendances nécessaires pour JavaFX installées.

#### Compilation du projet

1. **Serveur et Client :**
   - Allez dans les répertoires **Serveur** et **Client** respectivement et exécutez les commandes suivantes pour compiler le code Java :
   ```bash
   cd Serveur
   ./compile
   cd ../Client
   ./compile
   ```

#### Configuration des équipes et lancement d'une partie

1. **Configuration des équipes** : 
   - Dans l'interface **Client**, vous pouvez configurer les équipes en appuyant sur "Créer". Vous devrez choisir le niveau de difficulté des IA ou inviter des joueurs humains.

   **Attention** : Si vous cliquez sur "Retour" après avoir configuré votre équipe, puis appuyez à nouveau sur "Créer", la configuration ne fonctionnera plus correctement. Assurez-vous de bien configurer vos équipes avant de commencer la partie.

2. **Test automatisé avec Serveur/Call.sh** : 
   - Pour exécuter des tests automatisés, vous pouvez utiliser le script `Call.sh`. Ce script lance des parties automatisées et enregistre les résultats dans un fichier. Vous devrez configurer vos équipes dans le fichier de configuration avant de lancer les tests.

   ```bash
   cd Serveur
   ./Call.sh
   ```

## Fonctionnalités

- **IA** : Trois niveaux de difficulté sont disponibles pour les adversaires contrôlés par l'IA : faible, intermédiaire, et expert.
- **Gestion de la partie** : Le serveur gère les plis, les points et l'état du jeu en temps réel.
- **Paramétrage des équipes** : Vous pouvez personnaliser les équipes avant de lancer la partie.

## Règles du Jeu

Les règles du jeu suivent les règles classiques de la Belote. Les principales règles sont les suivantes :

- **Dernier pli** : L’équipe qui fait le dernier pli gagne 10 points supplémentaires.
- **Belote-Rebelote** : Lorsqu’un joueur possède le Roi et la Dame d’atout, il marque 20 points supplémentaires.
- **Litige** : Si une équipe marque exactement la moitié des points du contrat, un litige se produit et les 80 points restants sont remis en jeu.

### Détails des règles spéciales :

- **Comptage des points** : Les points sont calculés en fonction des cartes remportées dans les plis et de l'atout. Les règles spéciales, telles que la Belote-Rebelote et le dernier pli, peuvent modifier le total des points pour chaque équipe.


### Dépendances

Le projet utilise **JavaFX** pour l'interface graphique. Vous devrez peut-être installer **JavaFX** si vous ne l'avez pas déjà fait. Pour télécharger JavaFX, suivez les instructions sur le site officiel : [JavaFX Downloads](https://openjfx.io/).


Si vous rencontrez des problèmes lors de la compilation, assurez-vous que :
- Vous avez correctement configuré JavaFX dans votre environnement de développement.
- Vous utilisez une version de Java compatible (Java 8 ou supérieur).

### Problèmes de Création de Partie

Le processus de création de la partie échoue après avoir cliqué sur "Retour", relancer. Assurez-vous également que toutes les informations nécessaires pour la partie ont été correctement configurées.

### Problèmes d'Interface

Si l'interface graphique ne s'affiche pas correctement, vérifiez que **JavaFX** est correctement installé et configuré.

## Contribution
Maël Lecene

## License

Ce projet est sous la licence **MIT**. Voir le fichier `LICENSE` pour plus de détails.
