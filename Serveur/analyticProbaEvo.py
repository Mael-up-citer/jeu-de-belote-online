import re
import ast
import matplotlib.pyplot as plt

def transform_snapshot(snapshot_str):
    """
    Transforme une chaîne représentant un dictionnaire avec des clés non citées en une chaîne
    valide pour ast.literal_eval.
    Exemple : 
      {0={CARREAU={SEPTDeCARREAU=0.25, HUITDeCARREAU=0.25}, ...}, 1={...}}
    devient
      {0:{"CARREAU": {"SEPTDeCARREAU": 0.25, "HUITDeCARREAU": 0.25}, ...}, 1:{...}}
    """
    # On ajoute des guillemets autour des clés alphabétiques après un { ou une virgule
    pattern = r'([{,]\s*)([A-Za-z][A-Za-z0-9]*)(\s*=)'
    transformed = re.sub(pattern, r'\1"\2"\3', snapshot_str)
    # Remplacer ensuite tous les '=' restants par ':'
    transformed = transformed.replace("=", ":")
    return transformed

def load_proba_evo(filepath):
    """
    Ouvre le fichier 'filepath' et extrait une liste de snapshots.
    Les lignes "carte joué: ..." sont ignorées.
    Les autres lignes sont transformées pour être évaluées en structure Python.
    """
    snapshots = []
    with open(filepath, "r", encoding="utf-8") as f:
        lines = f.readlines()
    
    for line in lines:
        line = line.strip()
        if not line or line.startswith("carte joué:"):
            # On ignore les lignes vides et celles indiquant la carte jouée
            continue
        try:
            transformed_line = transform_snapshot(line)
            snapshot = ast.literal_eval(transformed_line)
            snapshots.append(snapshot)
        except Exception as e:
            print(f"Erreur lors du traitement de la ligne : {line}\nErreur : {e}")
    return snapshots

def plot_evolution(probaEvo):
    """
    Affiche l'évolution des probabilités pour chaque joueur (0, 1, 2, 3).
    Si une carte n'est pas présente dans un snapshot, on lui assigne une probabilité 0 pour ce snapshot.
    """
    # 1) Récupérer la liste de toutes les cartes possibles pour chaque joueur
    all_cards_per_player = {player: set() for player in range(4)}
    for snapshot in probaEvo:
        for player, suits in snapshot.items():
            for suit, cards in suits.items():
                for card in cards.keys():
                    all_cards_per_player[player].add(card)

    # 2) Construire la liste d'évolution des probabilités pour chaque joueur et chaque carte
    players_data = {player: {card: [] for card in all_cards_per_player[player]} for player in range(4)}
    
    # Pour chaque snapshot, si une carte est absente, on lui met 0
    for snapshot in probaEvo:
        for player in range(4):
            # Si le joueur n'existe pas dans le snapshot, on considère tout à 0
            suits = snapshot.get(player, {})
            
            # Regrouper toutes les cartes présentes dans ce snapshot pour le joueur
            cards_in_snapshot = {}
            for suit_name, cards_dict in suits.items():
                cards_in_snapshot.update(cards_dict)
            
            # Pour chacune des cartes que le joueur peut avoir
            for card in all_cards_per_player[player]:
                # On récupère la proba si elle existe, sinon 0
                prob = cards_in_snapshot.get(card, 0)
                players_data[player][card].append(prob)

    # 3) Tracer une figure par joueur
    for player in range(4):
        plt.figure(figsize=(12, 6))
        for card, probas in players_data[player].items():
            plt.plot(range(len(probas)), probas, marker='o', label=card)
        plt.title(f"Evolution des probabilités - Joueur {player}")
        plt.xlabel("Nombre de snapshots (cartes jouées)")
        plt.ylabel("Probabilité")
        plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left', fontsize='small')
        plt.tight_layout()
        plt.show()

if __name__ == '__main__':
    filepath = "probaEvo"  # Nom du fichier contenant les données
    probaEvo = load_proba_evo(filepath)
    
    if not probaEvo:
        print("Aucun snapshot n'a été chargé. Vérifiez le contenu du fichier.")
    else:
        plot_evolution(probaEvo)