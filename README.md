# UBGarden

## Présentation

**UBGarden** est un jeu développé en **Java** dans le cadre d’un projet de programmation orientée objet à l’Université de Bordeaux.

Le projet met en œuvre un environnement de jeu composé d’un monde, de niveaux, de personnages, d’objets interactifs et d’un moteur de jeu. L’application dispose également d’une interface graphique avec gestion des sprites, des ressources visuelles et des interactions avec le joueur.

Le projet s’appuie sur une architecture organisée en plusieurs packages afin de séparer la logique du jeu, le moteur, les objets du monde, le lancement des niveaux et l’affichage.

## Fonctionnalités

Le projet intègre notamment :

* Gestion du monde et des niveaux
* Déplacement du jardinier
* Gestion des personnages et des objets du jeu
* Objets récupérables et bonus
* Gestion des déplacements et directions
* Gestion des portes et passages entre niveaux
* Gestion des éléments du décor
* Gestion des insectes et de leurs nids
* Système d'énergie et de bonus
* Gestion du temps et de l'état de la partie
* Chargement de différents mondes et configurations
* Affichage graphique avec sprites et ressources images

## Architecture

Le projet est organisé autour de plusieurs composants.

### `game`

Contient le cœur du modèle du jeu :

* `Game` : gestion générale de la partie
* `World` : représentation du monde
* `Level` : gestion des niveaux
* `Map` : représentation de la carte
* `Position` : gestion des positions
* `Direction` : gestion des directions
* `Configuration` : paramètres de configuration

### `engine`

Contient les éléments nécessaires au fonctionnement de la partie :

* `GameEngine` : moteur principal du jeu
* `Input` : gestion des entrées utilisateur
* `StatusBar` : gestion de la barre d'état
* `Timer` : gestion du temps

### `go`

Regroupe les objets du monde du jeu.

Les objets sont organisés en différentes catégories :

* **Bonus** : carottes, énergie, insecticide, pomme empoisonnée
* **Décor** : arbres, fleurs, hérisson, portes et nids
* **Personnages** : jardinier, guêpes et frelons
* Interfaces pour les objets déplaçables, récupérables et accessibles

Le projet utilise également des interfaces et des visiteurs tels que `WalkVisitor` et `PickupVisitor` pour gérer les interactions avec les objets.

### `launcher`

Gère le lancement et le chargement des mondes et des niveaux :

* `GameLauncher`
* `MapRepo`
* `MapLevel`
* `MapEntity`
* `MapException`

Plusieurs fichiers `.properties` permettent de définir différents mondes :

```text
world/
├── world_1level_clear.properties
├── world_1level_compressed.properties
└── world_3levels_clear.properties
```

### `view`

Gère la représentation graphique du jeu :

* `GameLauncherView`
* `Sprite`
* `SpriteFactory`
* `SpriteGardener`
* `SpriteHornet`
* `SpriteWasp`
* `ImageResource`
* `ImageResourceFactory`

Les ressources graphiques sont stockées dans :

```text
src/main/resources/images/
```

## Structure du projet

```text
UBGarden-student-2025/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── src/
│   └── main/
│       ├── java/
│       │   └── fr/ubx/poo/ubgarden/
│       │       ├── Main.java
│       │       ├── game/
│       │       ├── engine/
│       │       ├── go/
│       │       ├── launcher/
│       │       └── view/
│       └── resources/
│           ├── css/
│           ├── images/
│           └── test.txt/
└── world/
    ├── world_1level_clear.properties
    ├── world_1level_compressed.properties
    └── world_3levels_clear.properties
```

## Technologies

* **Java**
* **Gradle**
* Programmation orientée objet
* Gestion des ressources graphiques
* Architecture modulaire
* Design patterns / interfaces / visiteurs

Le projet utilise **Gradle Wrapper**, ce qui permet de construire et exécuter le projet sans nécessiter une installation globale de Gradle.

## Lancer le projet

Depuis la racine du projet :

### Windows

```powershell
.\gradlew.bat build
```

Puis lancer l'application avec la tâche prévue dans le projet Gradle :

```powershell
.\gradlew.bat run
```

### Linux / macOS

```bash
./gradlew build
./gradlew run
```

> Les tâches Gradle disponibles peuvent être consultées avec `gradlew tasks` ou `./gradlew tasks`.

## Objectifs techniques

Ce projet permet notamment de mettre en pratique :

* Programmation orientée objet en Java
* Encapsulation et séparation des responsabilités
* Interfaces et polymorphisme
* Organisation d'une application Java en packages
* Modélisation d'un environnement de jeu
* Gestion des interactions entre objets
* Gestion des événements et des entrées utilisateur
* Gestion des ressources graphiques
* Construction et exécution avec Gradle
* Conception d'une architecture logicielle maintenable

## Contexte

Projet réalisé dans le cadre de la formation **Licence Informatique — Université de Bordeaux**.

## Auteur

**Youssouf Hassan**

Étudiant en Licence 3 Informatique — Université de Bordeaux.
