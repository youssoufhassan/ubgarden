    /*
     * Copyright (c) 2020. Laurent Réveillère
     */

    package fr.ubx.poo.ubgarden.game.engine;

    import fr.ubx.poo.ubgarden.game.Direction;
    import fr.ubx.poo.ubgarden.game.Game;
import fr.ubx.poo.ubgarden.game.Level;
import fr.ubx.poo.ubgarden.game.Position;
import fr.ubx.poo.ubgarden.game.World;
import fr.ubx.poo.ubgarden.game.go.bonus.Bonus;
import fr.ubx.poo.ubgarden.game.go.bonus.Carrots;
import fr.ubx.poo.ubgarden.game.go.bonus.Insecticide;
import fr.ubx.poo.ubgarden.game.go.decor.Decor;
import fr.ubx.poo.ubgarden.game.go.decor.DoorNextClosed;
import fr.ubx.poo.ubgarden.game.go.decor.DoorNextOpened;
import fr.ubx.poo.ubgarden.game.go.decor.Flowers;
import fr.ubx.poo.ubgarden.game.go.decor.Hedgehog;
import fr.ubx.poo.ubgarden.game.go.decor.NestHornet;
import fr.ubx.poo.ubgarden.game.go.decor.NestWasp;
import fr.ubx.poo.ubgarden.game.go.decor.Tree;
import fr.ubx.poo.ubgarden.game.go.personage.Gardener;
import fr.ubx.poo.ubgarden.game.go.personage.Hornet;
import fr.ubx.poo.ubgarden.game.go.personage.Wasp;
import fr.ubx.poo.ubgarden.game.launcher.GameLauncher;
import fr.ubx.poo.ubgarden.game.launcher.MapLevel;
import fr.ubx.poo.ubgarden.game.view.ImageResource;
import fr.ubx.poo.ubgarden.game.Map;
    import fr.ubx.poo.ubgarden.game.view.Sprite;
    import fr.ubx.poo.ubgarden.game.view.SpriteFactory;
    import fr.ubx.poo.ubgarden.game.view.SpriteGardener;
import fr.ubx.poo.ubgarden.game.view.SpriteHornet;
import javafx.animation.AnimationTimer;
    import javafx.application.Platform;
    import javafx.scene.Group;
    import javafx.scene.Scene;
    import javafx.scene.layout.Pane;
    import javafx.scene.layout.StackPane;
    import javafx.scene.paint.Color;
    import javafx.scene.text.Font;
    import javafx.scene.text.Text;
    import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.*;


public final class GameEngine {

    private static AnimationTimer gameLoop;
    private final Game game;
    private final Gardener gardener;
    private final List<Sprite> sprites = new LinkedList<>();
    private final Set<Sprite> cleanUpSprites = new HashSet<>();
    public MapLevel[] mapLevels;
    public final List<Hornet> hornets = new LinkedList<>();
    public final List<Wasp> wasps = new LinkedList<>();
    public final List<Insecticide> insecticides = new LinkedList<>();
    public long generate_horn;
    public long generate_wasp;  // Variable pour gérer le temps de génération des guêpes
    public Position nest_position;
    public Position waspNestPosition;
    public Position door_position;
    public Position door_position_prev;
    private final Scene scene; // Final field
    private Stage stage;
    private int totalCarrots = 0;  // Nombre total de carottes dans le niveau
    private Position doorPosition = null; 
    private long lastHornetMoveTime = 0;
private final long hornetMoveFrequency = 500_000_000L; // Tous les 0.5s

     // Position de la porte


    private StatusBar statusBar;

    private final Pane rootPane = new Pane();
    private final Group root = new Group();
    private final Pane layer = new Pane();
    private Input input;

    // Constructor with Scene initialization
    public GameEngine(Game game, Scene scene) {
        this.game = game;
        this.scene = scene;  // Initializing the final field here
        this.gardener = game.getGardener();
        this.generate_horn = System.nanoTime();
        this.generate_wasp = System.nanoTime();
        initialize();
        buildAndSetGameLoop();
    }

    // Constructor with Stage and MapLevel initialization
    public GameEngine(Game game, Stage stage, MapLevel[] mapLevels) {
        this.stage = stage;  // Initializing the stage field
        this.game = game;
        this.gardener = game.getGardener();
        this.generate_horn = System.nanoTime();
        this.generate_wasp = System.nanoTime();
        this.mapLevels = mapLevels;
        this.scene = new Scene(root);  // Assign a default scene if needed
        initialize();
        buildAndSetGameLoop();
    }


        public Pane getRoot() {
            return rootPane;
        }

        private void initialize() {
            int height = game.world().getGrid().height();
            int width = game.world().getGrid().width();
            int sceneWidth = width * ImageResource.size;
            int sceneHeight = height * ImageResource.size;
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/application.css")).toExternalForm());
            input = new Input(scene);

            root.getChildren().clear();
            root.getChildren().add(layer);
            statusBar = new StatusBar(root, sceneWidth, sceneHeight);

            rootPane.getChildren().clear();
            rootPane.setPrefSize(sceneWidth, sceneHeight + StatusBar.height);
            rootPane.getChildren().add(root);

            // Create sprites
            int currentLevel = game.world().currentLevel();

            for (var decor : game.world().getGrid().values()) {
                sprites.add(SpriteFactory.create(layer, decor));
                decor.setModified(true);
                var bonus = decor.getBonus();
                if (bonus != null) {
                    sprites.add(SpriteFactory.create(layer, bonus));
                    bonus.setModified(true);
                }
            
                if (decor instanceof NestHornet){
                    this.nest_position = decor.getPosition();  // Nouveau
                }
                if (decor instanceof NestWasp){
                    this.waspNestPosition = decor.getPosition();    // Nouveau
                }
                if (decor instanceof DoorNextClosed){
                    this.door_position = decor.getPosition();
                }
                if (decor.getBonus() instanceof Carrots) {
                    totalCarrots++;  // Compter les carottes
                }
            }
            

            sprites.add(new SpriteGardener(layer, gardener));
            resizeScene(sceneWidth, sceneHeight);
        }

        void buildAndSetGameLoop() {
            gameLoop = new AnimationTimer() {
                public void handle(long now) {
                    checkLevel();

                    // Check keyboard actions
                    processInput();

                    // Do actions
                    update(now);
                    checkCollision();

                    // Graphic update
                    cleanupSprites();
                    render();
                    statusBar.update(game);
                }
            };
        }
        private void checkLevel() {
    if (game.isSwitchLevelRequested()) {
        int newLevel = 2;  // Passer au niveau suivant
        cleanupSprites();  // Nettoyer les sprites de l'ancien niveau

        // Charger le nouveau niveau depuis mapLevels
        Map newMap = new Level(game, newLevel, this.mapLevels[newLevel - 1]);
        game.world().put(newLevel, newMap);
        game.world().setCurrentLevel(newLevel);

        // Placer le jardinier à la position d'entrée du nouveau niveau
        Position entryPosition = null;
        for (Decor decor : newMap.values()) {
            if (decor instanceof DoorNextOpened) {
                entryPosition = decor.getPosition();
                break;
            }
        }

        // Si aucune porte ouverte n'a été trouvée, mettre une position par défaut
        if (entryPosition == null) {
            entryPosition = new Position(newLevel, 1, 1);  // Position par défaut
        }

        gardener.setPosition(entryPosition);
        game.clearSwitchLevel();  // Réinitialiser la demande de changement de niveau
        
        // Initialiser les nouveaux éléments du niveau
        initialize();  // Recharger les éléments du niveau
    }
}

        private void checkCollision() {
            // Collisions avec frelons
            Iterator<Hornet> hornetIterator = hornets.iterator();
            while (hornetIterator.hasNext()) {
                Hornet hor = hornetIterator.next();
        
                // Collision frelon/jardinier
                if (hor.getPosition().equals(gardener.getPosition())) {
                    gardener.hurt(30);
                    hor.remove();
                }
        
                // Collision frelon/bombe
                Iterator<Insecticide> insIt = insecticides.iterator();
                while (insIt.hasNext()) {
                    Insecticide ins = insIt.next();
                    if (hor.getPosition().equals(ins.getPosition())) {
                        hor.remove();
                        ins.remove();
                        hornetIterator.remove();
                        insIt.remove();
                        break;
                    }
                }
        
                // Jardinier ramasse bombe
                for (Insecticide ins : insecticides) {
                    if (gardener.getPosition().equals(ins.getPosition())) {
                        gardener.setBombs(gardener.getBombs() + 1);
                        ins.remove();
                        insecticides.remove(ins);
                        break;
                    }
                }
            }
        
            // Collisions avec guêpes
            Iterator<Wasp> waspIterator = wasps.iterator();
            while (waspIterator.hasNext()) {
                Wasp wasp = waspIterator.next();
        
                // Si la guêpe est sur le jardinier
                if (wasp.getPosition().equals(gardener.getPosition())) {
                    if (gardener.getBombs() > 0) {
                        gardener.setBombs(gardener.getBombs() - 1);
                        wasp.remove();
                        waspIterator.remove();
                        System.out.println("💥 Bombe utilisée automatiquement contre une guêpe !");
                    } else {
                        gardener.hurt(); // Pas de bombe dispo
                    }
                }
        
                // Collision guêpe/bombe
                Iterator<Insecticide> insIt = insecticides.iterator();
                while (insIt.hasNext()) {
                    Insecticide ins = insIt.next();
                    if (wasp.getPosition().equals(ins.getPosition())) {
                        wasp.remove();
                        ins.remove();
                        waspIterator.remove();
                        insIt.remove();
                        break;
                    }
                }
        
                // Jardinier ramasse bombe (si oubli plus haut)
                for (Insecticide ins : insecticides) {
                    if (gardener.getPosition().equals(ins.getPosition())) {
                        gardener.setBombs(gardener.getBombs() + 1);
                        ins.remove();
                        insecticides.remove(ins);
                        break;
                    }
                }
            }
        }
        
        private void processInput() {
            if (input.isExit()) {
                gameLoop.stop();
                Platform.exit();
                System.exit(0);
            } else if (input.isMoveDown()) {
                gardener.requestMove(Direction.DOWN);
            } else if (input.isMoveLeft()) {
                gardener.requestMove(Direction.LEFT);
            } else if (input.isMoveRight()) {
                gardener.requestMove(Direction.RIGHT);
            } else if (input.isMoveUp()) {
                gardener.requestMove(Direction.UP);
            }
            input.clear();
        }

        private void showMessage(String msg, Color color) {
            Text message = new Text(msg);
            message.setTextAlignment(TextAlignment.CENTER);
            message.setFont(new Font(60));
            message.setFill(color);

            StackPane pane = new StackPane(message);
            pane.setPrefSize(rootPane.getWidth(), rootPane.getHeight());
            rootPane.getChildren().clear();
            rootPane.getChildren().add(pane);

            new AnimationTimer() {
                public void handle(long now) {
                    processInput();
                }
            }.start();
        }

        private void update(long now) {
            // Mise à jour des éléments du décor
            if (gardener.getEnergy() <= 0) {
                gameLoop.stop();  // Arrête la boucle de jeu
                showMessage("Perdu!", Color.RED);  // Affiche le message de défaite
                return;  // Ne continue pas l'exécution du reste de la méthode
            }
            game.world().getGrid().values().forEach(decor -> decor.update(now));
        
            gardener.update(now);
            checkCollision();
        
            generateHornetsAndInsecticides(now);
        
            updateHornets(now);
    
            generateWaspsAndInsecticides(now);

        // Mise à jour des guêpes
          updateWasps(now);

            
        
            // Condition de victoire
            Decor dec = game.world().getGrid().get(this.gardener.getPosition());
            if (dec instanceof Hedgehog) {
                gameLoop.stop();
                showMessage("you win!", Color.GREEN);
            }
        
            // Passage au niveau suivant
            // Ouvre la porte quand toutes les carottes sont prises
            if (dec instanceof DoorNextClosed && totalCarrots == gardener.getCollected_carrot()) {
                Decor open = new DoorNextOpened(this.door_position);
                sprites.add(SpriteFactory.create(layer, open));
                open.setModified(true);
            }


            if (dec instanceof DoorNextOpened) {
                game.requestSwitchLevel(2);  // Passe au niveau 2
            }
            
        }
        
        private void generateHornetsAndInsecticides(long now) {
            if ((now - generate_horn) > 10_000_000_000L) {
                generate_horn = now;
        
                // Crée un nouveau frelon au nid
                Hornet new_h = new Hornet(this.game, this.nest_position);
                hornets.add(new_h);
                sprites.add(new SpriteHornet(layer, new_h));
        
                // Crée 2 insecticides aléatoires
                Random random = new Random();
                for (int i = 0; i < 2; i++) {
                    int x, y;
                    Position p;
                    Decor next;
                    do {
                        x = random.nextInt(game.world().getGrid().width());
                        y = random.nextInt(game.world().getGrid().height());
                        p = new Position(1, x, y);
                        next = game.world().getGrid().get(p);
                    } while (next instanceof Flowers || next instanceof Tree || next.getBonus() != null);
        
                    Insecticide ins = new Insecticide(p, next);
                    insecticides.add(ins);
                    sprites.add(SpriteFactory.create(layer, ins));
                    ins.setModified(true);
                }
            }
        }
        private void generateWaspsAndInsecticides(long now) {
            if ((now - generate_wasp) > 5_000_000_000L) {  // Générer une guêpe toutes les 5 secondes
                generate_wasp = now;
        
                // Crée une nouvelle guêpe au nid de guêpe
                Wasp newWasp = new Wasp(this.game, this.waspNestPosition);
                wasps.add(newWasp);
        
                // Crée un sprite pour la guêpe et l'ajoute à la scène
                sprites.add(SpriteFactory.create(layer, newWasp));
        
                // Crée des insecticides aléatoires
                Random random = new Random();
                for (int i = 0; i < 1; i++) {
                    int x, y;
                    Position p;
                    Decor next;
                    do {
                        x = random.nextInt(game.world().getGrid().width());
                        y = random.nextInt(game.world().getGrid().height());
                        p = new Position(1, x, y);
                        next = game.world().getGrid().get(p);
                    } while (next instanceof Flowers || next instanceof Tree || next.getBonus() != null);
        
                    Insecticide ins = new Insecticide(p, next);
                    insecticides.add(ins);
                    sprites.add(SpriteFactory.create(layer, ins));
                    ins.setModified(true);
                }
            }
        }
        

        private void updateWasps(long now) {
            List<Wasp> waspCopy = new ArrayList<>(wasps); // Copie défensive
            for (Wasp wasp : waspCopy) {
                wasp.update(now);  // Déplacement contrôlé par waspMoveFrequency
            }
        }
    
        
        private void updateHornets(long now) {
            if ((now - lastHornetMoveTime) > hornetMoveFrequency) {
                lastHornetMoveTime = now;
                List<Hornet> hornetCopy = new ArrayList<>(hornets);
                for (Hornet hor : hornetCopy) {
                    hor.update(now);
                }
            }
        }
        
        
        public void cleanupSprites() {
            sprites.forEach(sprite -> {
                if (sprite.getGameObject().isDeleted()) {
                    cleanUpSprites.add(sprite);
                }
            });
            cleanUpSprites.forEach(Sprite::remove);
            sprites.removeAll(cleanUpSprites);
            cleanUpSprites.clear();
        }

        private void render() {
            sprites.forEach(Sprite::render);
        }

        public void start() {
            gameLoop.start();
        }

        private void resizeScene(int width, int height) {
            rootPane.setPrefSize(width, height + StatusBar.height);
            layer.setPrefSize(width, height);
            Platform.runLater(() -> scene.getWindow().sizeToScene());
        }

        public void stop() {
            if(gameLoop!=null){
                gameLoop.stop();
            }
        }
    }