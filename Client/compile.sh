#!/bin/bash

# Répertoire des bibliothèques
LIB_DIR="./lib"
# Dossier de compilation
BIN_DIR="./bin"
# Dossier des ressources
RESOURCES_DIR="./resources"
# Répertoire des sources (racine)
SRC_DIR="./src"
# Classe principale à compiler
MAIN_CLASS="main.App"

# Vérifier si le répertoire JavaFX SDK existe
JAVAFX_SDK="$LIB_DIR/javafx-sdk-21.0.5/lib"
if [[ ! -d "$JAVAFX_SDK" ]]; then
  echo "Erreur : Le répertoire JavaFX SDK n'existe pas à l'emplacement attendu : $JAVAFX_SDK"
  exit 1
fi

# Initialiser les modules JavaFX nécessaires
JAVAFX_MODULES="javafx.controls,javafx.fxml"

# Créer le dossier bin s'il n'existe pas
if [[ ! -d "$BIN_DIR" ]]; then
  echo "Création du dossier bin..."
  mkdir -p "$BIN_DIR"
fi

# Compiler tous les fichiers dans src/
echo "Compilation des fichiers sources dans $SRC_DIR ..."
javac -d "$BIN_DIR" --module-path "$JAVAFX_SDK" --add-modules $JAVAFX_MODULES $(find "$SRC_DIR" -name "*.java")
COMPILE_STATUS=$?

# Vérifier si la compilation a réussi
if [[ $COMPILE_STATUS -eq 0 ]]; then
  echo "Compilation réussie."

  # Exécution de la classe principale
  echo "Exécution de l'application..."
  java --module-path "$JAVAFX_SDK" --add-modules $JAVAFX_MODULES -cp "$BIN_DIR:$RESOURCES_DIR" "$MAIN_CLASS"

    # Suppression des fichiers compilés après exécution
    rm -rf "$BIN_DIR"/*
    echo "Le dossier bin a été nettoyé."
else
  echo "Erreur lors de la compilation."
  exit 1
fi