# Block Blast : analyse A → Z

> Analyse faite le 14/11/2026 sur le paquet officiel Play Store
> `com.block.juggle` version 11.0.0 (XAPK arm64-v8a, 239 Mo) et sur la fiche
> Play Store publique.

## Méthode, et ce qui n'a pas pu être fait

L'objectif initial était de lancer le jeu dans un émulateur et de dérouler tous
les menus. **Ce n'est pas possible sur cette machine**, pour trois raisons
vérifiées :

| Blocage | Preuve |
|---|---|
| Pas d'accélération KVM | conteneur LXC, `/dev/kvm` absent et `mknod` refusé par le cgroup |
| APK ARM 64 bits uniquement | le XAPK ne contient que `config.arm64_v8a.apk`, aucun `x86_64` |
| RAM/CPU partagés | 12 Go dont ~10 Go utilisés par les services de la machine |

Un émulateur ARM sous QEMU sans KVM aurait demandé des heures et saturé la
machine. L'analyse ci-dessous s'appuie donc sur des sources vérifiables :
manifeste, permissions, bibliothèques natives, dossiers d'assets, 443
« traits » de configuration, captures officielles et avis Play Store.

## Ce qu'est réellement Block Blast en 2026

Ce n'est plus un simple puzzle de blocs : c'est un **hub de mini-jeux** avec
une couche de rétention et de monétisation très poussée.

### Modes et contenu (dossiers d'assets)

| Dossier | Contenu |
|---|---|
| `chapter` (691 fichiers) | carte d'aventure, chapitres, progression |
| `gl_onet` | mini-jeu « Onet » (relier des paires) |
| `gl_mahjong` | mini-jeu mahjong |
| `gl_watersort` | tri de liquides |
| `gl_sudoku` | sudoku |
| `gl_tictactoe` | morpion |
| `gl_oneline` | puzzle « une ligne » |
| `gl_fruit` | variante fruit |
| `sandcrush` | mini-jeu de sable |
| `gl_hall` | écran d'accueil / hub des jeux |
| `Skin*`, `Collection*`, `Album*` | skins de plateaux, collections, album |

### Monétisation et tracking (preuves dans l'APK)

- Réseaux publicitaires : **AppLovin, AdMob, Facebook Audience Network,
  Moloco, Pangle, Fyber, Vungle, InMobi, Unity Ads, Amazon**.
- Attribution / tracking : **Adjust, AppsFlyer**.
- Crash : **Bugsnag**.
- `assets/RNBundles/ad.bundle` : les publicités ont leur propre bundle React
  Native.
- Traits explicites : `AdSubScribePopModel`, `AdProtectParam`,
  `Ecpm_player_high_death`, `BreakHighScoreRevive`, `CTRefactorAgloRevive`,
  `$28510_f_TravelPassTips` (passe de saison), `GemCollectBoost`,
  `HistoryBoardOfferBlock`.

On y lit aussi une **difficulté pilotée par les données** :
`CTRefactorBAlgorithmDynamicDifficulty`, `AlgoRegretHard`,
`AlgoFillSortReplace`, `HighComboScoringRuleAdjust`,
`ChangeCollectByLevelFailCount`, `CTRefactorLightChurnRate`. Autrement dit, le
tirage des pièces, l'agencement du plateau et les seuils de score sont ajustés
selon le comportement et l'attrition du joueur.

### Perception joueurs (Play Store)

- 1 Md+ d'installations, 5,88 M d'avis, ~4,7/5.
- Avis récurrents : publicités « évitables en hors-ligne », difficulté parfois
  ressentie comme forcée, skins qui changent sans consentement, manque de
  contenu au-delà du mode classique.

## Parcours de menus à vérifier sur un vrai appareil

Liste établie à partir des traits et des assets ; c'est la checklist A → Z que
je déroulerais sur un téléphone :

1. **Écran d'accueil** : bouton Jouer, couronne/meilleur score, réglages.
2. **Mode classique** : plateau 8×8, main de 3 pièces, combos, revive payant
   après un game over.
3. **Aventure** : carte de chapitres, étoiles, déblocage progressif,
   `AddChapterUnlockProcess`, `ChapterSeasonSkin`.
4. **Défi quotidien** : objectif du jour, série de jours (`ShowLoginDays`).
5. **Événements / passe** : `TravelPass`, offres limitées, gemmes.
6. **Boutique** : gemmes, packs, `HistoryBoardOfferBlock`, abonnement
   (`AdSubScribe`).
7. **Skins / collections** : changement de thème du plateau
   (`BoardClearChangeSkin`), album.
8. **Hub mini-jeux** : Onet, mahjong, water sort, sudoku, morpion…
9. **Réglages** : son, musique, vibrations, langue, confidentialité, CGU,
   support, suppression de compte (`HideSettingBeforeChapter`).
10. **Publicités** : interstitielle après game over, rewarded pour un revive,
    bannière, « no ads » par abonnement.

## Comparaison avec Blocksmith

| Sujet | Block Blast | Blocksmith |
|---|---|---|
| Modèle | gratuit + pubs + IAP + abonnement | gratuit, **aucune pub, aucun achat** |
| Réseau | 30+ permissions, 10 réseaux pub | **0 permission**, aucun accès réseau |
| Difficulté | ajustée par algorithme et rétention | **fixe et documentée** (règles publiques) |
| Contenu | hub de 9 mini-jeux | un jeu, 48 niveaux + classique + défi |
| Skins | nombreux, parfois imposés | un thème soigné, pas de boutique |
| Aide | revive payant, indices | indice gratuit et illimité |
| Taille | 239 Mo | **1,1 Mo** |
| Compte | optionnel, cloud | aucun compte, sauvegarde locale |

## Ce que je reprendrais (sans la partie sombre)

- Le **rythme de récompense** : combo visible, phrases d'encouragement,
  particules à l'effacement — c'est ce qui rend le jeu agréable.
- La **lisibilité du plateau** : blocs brillants, contraste fort, cases vides
  discrètes.
- Les **objectifs de chapitre** (lignes, score, gemmes, survie) : c'est ce qui
  transforme un puzzle infini en progression.
- **Pas** la difficulté adaptative cachée, ni les pubs, ni les revive payants.

## Si tu veux un test interactif complet

Il faut un appareil Android réel (ou un cloud phone), puis :

```bash
adb install -r Blocksmith-1.0.0.apk     # pour Blocksmith
adb install-multiple base.apk config.arm64_v8a.apk config.en.apk ...  # Block Blast
adb shell am start -n com.block.juggle/org.cocos2dx.javascript.AppActivity
adb exec-out screencap -p > screen.png
```

Je peux piloter tout ça à distance si un appareil est branché au VPS
(`adb connect <ip>:5555` sur un téléphone en débogage sans fil).
