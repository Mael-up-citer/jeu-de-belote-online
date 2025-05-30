import re
import matplotlib.pyplot as plt



# Nom du fichier contenant les données
def main():
    nom_fichier = 'nullVsMoy.txt'

    # Expressions régulières pour extraire les informations
    gagne_pattern = re.compile(r"Équipe gagnante\s*:\s*(Équipe\s*\d+)")
    scores_pattern = re.compile(r"Scores\s*-\s*Équipe\s*0\s*:\s*(\d+),\s*Équipe\s*1\s*:\s*(\d+)")

    scores = []
    wins = { 'Équipe 0': 0, 'Équipe 1': 0 }

    # Lecture et parsing du fichier
    with open(nom_fichier, 'r', encoding='utf-8') as fichier:
        # Ne garder que les lignes non vides
        lines = [ligne.strip() for ligne in fichier if ligne.strip()]

    # Parcours des paires de lignes (gagnant + scores)
    for i in range(0, len(lines), 2):
        match_gagnant = gagne_pattern.search(lines[i])
        match_scores = scores_pattern.search(lines[i + 1])

        if match_gagnant and match_scores:
            gagnant = match_gagnant.group(1)
            s0 = int(match_scores.group(1))
            s1 = int(match_scores.group(2))

            scores.append((s0, s1))
            wins[gagnant] += 1
        else:
            print(f"Ligne non analysée : '{lines[i]}' / '{lines[i + 1]}'")

    # Vérifier qu'il y a des matchs
    total_matchs = len(scores)
    if total_matchs == 0:
        print("Aucun match valide trouvé dans le fichier.")
        return

    # Calcul des pourcentages de victoire
    pourcentages = { equipe: (wins[equipe] / total_matchs) * 100 for equipe in wins }

    # Tracé des pourcentages de victoire
    plt.figure(figsize=(6, 4))
    plt.bar(list(pourcentages.keys()), list(pourcentages.values()))
    plt.ylabel('Pourcentage de victoires (%)')
    plt.title('Pourcentage de victoires par équipe')
    plt.ylim(0, 100)
    for i, equipe in enumerate(pourcentages):
        pct = pourcentages[equipe]
        plt.text(i, pct + 1, f"{pct:.1f}%", ha='center')
    plt.tight_layout()
    plt.show()

    # Calcul des points totaux marqués par chaque équipe
    points_totaux = {
        'Équipe 0': sum(s0 for s0, _ in scores),
        'Équipe 1': sum(s1 for _, s1 in scores)
    }

    # Tracé des points totaux
    plt.figure(figsize=(6, 4))
    plt.bar(list(points_totaux.keys()), list(points_totaux.values()))
    plt.ylabel('Points totaux')
    plt.title('Points totaux marqués par équipe')
    for i, equipe in enumerate(points_totaux):
        pts = points_totaux[equipe]
        plt.text(i, pts + max(points_totaux.values()) * 0.02, f"{pts}", ha='center')
    plt.tight_layout()
    plt.show()



if __name__ == '__main__':
    main()