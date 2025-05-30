#!/bin/bash


# Répertoire du script tests.sh
SCRIPT_PATH="./TestsCompil.sh"

# Vérification que le script tests.sh existe
if [[ ! -f "$SCRIPT_PATH" ]]; then
  echo "Le fichier $SCRIPT_PATH n'existe pas."
  exit 1
fi

# Nombre d'itérations
ITERATIONS=50

# Boucle pour appeler le script tests.sh 50 fois
for i in $(seq 1 $ITERATIONS); do
    ARG1="débutant"
    ARG2="intermédiaire"


  # Appel du script avec les arguments
  echo "Exécution de l'itération $i avec les arguments : $ARG1, $ARG2"
  $SCRIPT_PATH "$ARG1" "$ARG2"

done