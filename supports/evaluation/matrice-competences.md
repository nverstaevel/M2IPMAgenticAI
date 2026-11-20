# Matrice de compétences — IA agentiques avec Spring AI (M2 MIAGE IPM)

Export texte de la matrice distribuée aux étudiants (classeur à trois feuilles : Matrice, Échelle, Synthèse). L'étudiant remplit « Début » avant le CM1, « Fin » après le TD6 et cite une preuve par compétence ; le progrès se calcule dans le classeur.

## Échelle

| Niveau | Ce que cela veut dire | Exemple |
|---|---|---|
| 0 | Je n'ai jamais vu cela | — |
| 1 | Je reconnais le terme et je sais où chercher | Je sais qu'un outil est une méthode annotée |
| 2 | Je sais le faire avec un exemple sous les yeux | J'adapte le code d'un TD |
| 3 | Je sais le faire seul sur un cas nouveau | Je décris et je teste un outil pour une autre application |
| 4 | Je peux l'expliquer à un autre et en discuter les limites | Je justifie mon choix et je cite une trace à l'appui |

## Mode d'emploi

1. Avant le CM1, remplissez la colonne « Début » de la feuille Matrice.
2. Après le TD6, remplissez la colonne « Fin ». Le progrès se calcule tout seul.
3. Pour chaque compétence, citez une preuve dans « Ma preuve » : un livrable, un test, une ligne du journal.
4. Les compétences marquées Outil, Trace ou Analyse critique sont celles que vous retrouverez à l'évaluation de TD.
5. Une compétence marquée « non pratiqué en TD » est à travailler vous-même, par exemple dans un projet.

## Matrice

| Domaine | Compétence | Séances | Preuve attendue | Évaluation de TD |
|---|---|---|---|---|
| Fondements | Expliquer ce qu'un LLM fait et ne fait pas : état, déterminisme, contexte | CM1 | Journal du TD2, analyse critique | Analyse critique |
| Fondements | Appeler un modèle avec ChatClient et gérer la mémoire de conversation | CM1, TD2 | Un chat qui fonctionne, testé avec le modèle simulé | — |
| Fondements | Obtenir une sortie structurée et la valider | CM1, TD2 | Un triage typé, avec ses cas limites testés | — |
| Outils | Concevoir et décrire un outil exploitable par un modèle | CM1, TD2, TD3 | Des outils aux descriptions précises | Outil |
| Outils | Séparer proposer et confirmer toute écriture | CM1, TD2, TD3 | Une confirmation hors modèle, testée | Outil |
| Outils | Brancher une source externe : derrière un port (TD3) ou par MCP (TD6) | CM2, CM3, TD3, TD6 | Un port avec son adaptateur simulé (l'adaptateur réel est fourni) ; la liste des outils du jeu découverte par MCP | — |
| Outils | Concevoir le retour d'un outil : peu, structuré, erreur lisible | CM2, TD3 | Des outils qui renvoient peu de données | Outil |
| Contexte et mesure | Mesurer l'effet du contexte : taille, position, bruit | CM2, TD3 | Un tableau de mesures (stratégies A et B ; position et bruit en extension) | — |
| Contexte et mesure | Rendre compte honnêtement d'une mesure | CM2, CM3, TD3, TD5, TD6 | Un journal et des comparatifs sincères, dont le tableau de scores du TD6 | Analyse critique |
| Agents | Écrire une boucle ReAct avec des plafonds et des motifs d'arrêt | CM2, TD4 | Une boucle dont les arrêts sur plafond et sur boucle détectée sont testés | — |
| Agents | Lire une trace d'agent et situer l'erreur | CM2, TD4, TD5 | Des traces commentées, un catalogue d'échecs | Trace |
| Agents | Mettre en œuvre un duo acteur et critique, et mesurer ses faux rejets | CM2, TD5 | Un duo qui cite ses sources, un taux de faux rejets | — |
| Agents | Orchestrer des travailleurs spécialisés | CM2, TD5 | Un superviseur et trois travailleurs | — |
| Agents | Choisir entre un workflow et un agent, et le justifier | CM1, CM2, TD5, TD6 | Un comparatif sincère, puis des architectures comparées par leurs scores | Analyse critique |
| Sécurité et esprit critique | Reconnaître les risques d'un serveur d'outils tiers : injection, droits, outils trop puissants | CM3, TD6 | Une trace de partie où l'injection est repérée | Trace |
| Sécurité et esprit critique | Limiter les outils de chaque rôle et valider côté serveur | CM3, TD6 | Un scénario .md à rôles aux outils restreints | Trace |
| Sécurité et esprit critique | Rédiger une analyse critique appuyée sur des traces et des scores | CM1, TD6 | L'analyse chiffrée de vos parties comparées | Analyse critique |
| Pratique de projet | Configurer et déployer sans fuite de secrets, avec des plafonds | CM1, TD2 à TD6 | Des profils de configuration, aucun secret dans Git | — |
| Pratique de projet | Spécifier le comportement d'un agent pour un client | CM1 seulement : non pratiqué en TD | À travailler dans votre projet | — |
