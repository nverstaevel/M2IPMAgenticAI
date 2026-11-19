# ANALYSE (modèle à compléter, ≈ 400 mots)

> Cette page ne peut pas être écrite sans mesures réelles. Le dépôt solution n'a été exécuté qu'avec un modèle simulé ;
> chaque phrase ci-dessous doit être remplacée par une observation tirée de vos traces (`traces/<runId>.jsonl`, `dossiers/<runId>.pdf`).

1. **Ce que l'IA apporte** — Ce qu'un workflow classique (`TraiterConsultationWorkflow`) ne fait pas : demande en langage libre, critique d'un traitement contre des textes. Citez une trace où c'est visible.
2. **Ce qu'elle coûte** — Fiabilité mesurée (`POST /api/agent/mesures`, 20 exécutions à température basse puis habituelle), jetons, latence, difficulté de test (le modèle simulé rend les tests déterministes, pas le vrai modèle).
3. **Risques observés** — Citez la tentative (identifiant d'attaque, trace) et son résultat réel avec votre modèle.
4. **Ce que je laisserais faire, ce que je ne déléguerais jamais** — Par exemple : lecture et proposition, oui ; confirmation, validation du dossier et contre-indications, non.
5. **Ce que mes mesures ne prouvent pas** — Nombre d'exécutions, modèle local de petite taille, jeux de test réduits, simulateur utilisé en CI.
