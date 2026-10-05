package fr.ubx.poo.ubgarden.game.launcher;

import fr.ubx.poo.ubgarden.game.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class GameLauncher {
    private GameLauncher() {
    }

    public static GameLauncher getInstance() {
        return LoadSingleton.INSTANCE;
    }

    private int integerProperty(Properties properties, String name, int defaultValue) {
        return Integer.parseInt(properties.getProperty(name, Integer.toString(defaultValue)));
    }

    private boolean booleanProperty(Properties properties, String name, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(name, Boolean.toString(defaultValue)));
    }

    private Configuration getConfiguration(Properties properties) {
        // Load parameters
        int waspMoveFrequency = integerProperty(properties, "waspMoveFrequency", 1) * 1000;
        int hornetMoveFrequency = integerProperty(properties, "hornetMoveFrequency", 2) * 1000;

        int gardenerEnergy = integerProperty(properties, "gardenerEnergy", 100);
        int energyBoost = integerProperty(properties, "energyBoost", 50);
        long energyRecoverDuration = integerProperty(properties, "energyRecoverDuration", 1_000);
        long diseaseDuration = integerProperty(properties, "diseaseDuration", 5_000);

        return new Configuration(gardenerEnergy, energyBoost, energyRecoverDuration, diseaseDuration, waspMoveFrequency, hornetMoveFrequency);
    }

    public Game load() {
        Properties emptyConfig = new Properties();

        MapLevelDefaultStart mapLevel = new MapLevelDefaultStart();

        Position gardenerPosition = mapLevel.getGardenerPosition();

        if (gardenerPosition == null) {
            throw new RuntimeException("Gardener not found");
        }

        Configuration configuration = getConfiguration(emptyConfig);

        World world = new World(2);
        Game game = new Game(world, configuration, gardenerPosition);

        Map level = new Level(game, 1, mapLevel);

        world.put(1, level);

        return game;
    }

    public MapLevel lireNiveauCarte(String donnees) {
    int largeur = 0;
    int hauteur = 0;
    int tempLargeur = 0;

    for (char caractere : donnees.toCharArray()) {
        if (caractere == 'x') {
            hauteur++;
            largeur = Math.max(largeur, tempLargeur);
            tempLargeur = 0;
        } else {
            tempLargeur++;
        }
    }

    MapEntity[][] carte = new MapEntity[hauteur][largeur];

    int ligne = 0;
    int colonne = 0;

    for (int index = 0; index < donnees.length(); index++) {
        char symbole = donnees.charAt(index);
        if (symbole == 'x') {
            ligne++;
            colonne = 0;
        } else {
            carte[ligne][colonne++] = MapEntity.fromCode(symbole);
        }
    }

    return new MapLevel(largeur, hauteur, carte);
}

public MapLevel lireCarteCompressee(String codeCompresse) {
    StringBuilder reconstruction = new StringBuilder();
    int position = 0;

    while (position < codeCompresse.length()) {
        char caractereActuel = codeCompresse.charAt(position);

        if (Character.isDigit(caractereActuel)) {
            int repetition = Character.getNumericValue(caractereActuel);
            char precedent = reconstruction.charAt(reconstruction.length() - 1);
            reconstruction.append(String.valueOf(precedent).repeat(repetition - 1));
            position++;
        } else {
            reconstruction.append(caractereActuel);
            position++;
        }
    }

    return lireNiveauCarte(reconstruction.toString());
}

public Game load(Reader lecteur) {
    Properties config = new Properties();

    try {
        config.load(lecteur);
    } catch (IOException ex) {
        throw new RuntimeException("Impossible de lire les propriétés", ex);
    }

    boolean estCompresse = booleanProperty(config, "compression", false);
    int totalNiveaux = integerProperty(config, "levels", 1);
    MapLevel[] niveaux = new MapLevel[totalNiveaux];

    for (int indice = 0; indice < totalNiveaux; indice++) {
        String cle = "level" + (indice + 1);
        String chaine = config.getProperty(cle, "x");
        niveaux[indice] = estCompresse ? lireCarteCompressee(chaine) : lireNiveauCarte(chaine);
    }

    Position positionJardinier = niveaux[0].getGardenerPosition();
    if (positionJardinier == null) {
        throw new RuntimeException("Position du jardinier introuvable");
    }

    Configuration cfg = new Configuration(
        integerProperty(config, "gardenerEnergy", 100),
        integerProperty(config, "energyBoost", 50),
        integerProperty(config, "energyRecoverDuration", 1000),
        integerProperty(config, "diseaseDuration", 5000),
        integerProperty(config, "waspMoveFrequency", 1) * 1000,
        integerProperty(config, "hornetMoveFrequency", 2) * 1000
    );

    World monde = new World(totalNiveaux);
    Game jeu = new Game(monde, cfg, positionJardinier);

    for (int i = 0; i < totalNiveaux; i++) {
        monde.put(i + 1, new Level(jeu, i + 1, niveaux[i]));
    }

    return jeu;
}


    private static class LoadSingleton {
        static final GameLauncher INSTANCE = new GameLauncher();
    }
}