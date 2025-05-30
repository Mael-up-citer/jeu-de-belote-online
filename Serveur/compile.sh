#!/bin/bash

# Répertoire des bibliothèques
LIB_DIR="./lib"
# Dossier de compilation
BIN_DIR="./bin"
# Chemin du fichier source principal
JAVA_FILE="src/Network/Server.java"

# Vérification que le fichier existe
if [[ ! -f "$JAVA_FILE" ]]; then
  echo "Le fichier $JAVA_FILE n'existe pas."
  exit 1
fi

# Initialiser les variables pour les librairies et modules JavaFX
JAVAFX_SDK="$LIB_DIR/javafx-sdk-21.0.5/lib"
JAVAFX_MODULES="javafx.controls,javafx.fxml"

# Créer le dossier bin s'il n'existe pas
mkdir -p "$BIN_DIR"

# Compiler le fichier Java en incluant tous les fichiers source dans les sous-dossiers
echo "Compilation du fichier $JAVA_FILE..."
javac -d "$BIN_DIR" --module-path "$JAVAFX_SDK" --add-modules $JAVAFX_MODULES "$JAVA_FILE"

# Vérifier si la compilation a réussi
if [[ $? -eq 0 ]]; then
  echo "Compilation réussie."

  # Récupérer le nom de la classe principale relative au package
  MAIN_CLASS="src.Network.Server"

  # Exécution de la classe JavaFX avec les bibliothèques et les modules nécessaires
  echo "Exécution de l'application..."
  java --module-path "$JAVAFX_SDK" --add-modules $JAVAFX_MODULES -cp "$BIN_DIR" "$MAIN_CLASS"

  # Supprimer les fichiers .class générés après l'exécution (optionnel)
  rm -rf "$BIN_DIR"/* &> /dev/null
else
  echo "Erreur lors de la compilation."
fi