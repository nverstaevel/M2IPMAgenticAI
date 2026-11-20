
## La page du jeu

`http://localhost:8090` montre la clinique en direct : les créatures entrent par la porte et s'installent dans la salle d'attente, chacune avec une jauge de PV et un compte à rebours en **jours restants** (elle perd 15 PV à chaque fin de journée). Dr Gribouille commente chaque action ; la pharmacie, la trésorerie et la réputation se mettent à jour.

Un humain peut aussi **jouer à la main** : clic sur une créature (examiner, soigner), clic sur un flacon (commander), bouton « Fin de journée ». C'est votre référence humaine pour le TD6. Ces actions passent par `/api/examiner`, `/api/soigner`, `/api/commander`, `/api/fin-de-journee` ; un agent, lui, n'a que les outils MCP.
