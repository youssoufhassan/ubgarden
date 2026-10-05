package fr.ubx.poo.ubgarden.game.go.personage;

import fr.ubx.poo.ubgarden.game.Direction;
import fr.ubx.poo.ubgarden.game.Game;
import fr.ubx.poo.ubgarden.game.Position;
import fr.ubx.poo.ubgarden.game.engine.Timer;
import fr.ubx.poo.ubgarden.game.go.GameObject;
import fr.ubx.poo.ubgarden.game.go.Movable;
import fr.ubx.poo.ubgarden.game.go.PickupVisitor;
import fr.ubx.poo.ubgarden.game.go.WalkVisitor;
import fr.ubx.poo.ubgarden.game.go.bonus.Carrots;
import fr.ubx.poo.ubgarden.game.go.bonus.EnergyBoost;
import fr.ubx.poo.ubgarden.game.go.bonus.Insecticide;
import fr.ubx.poo.ubgarden.game.go.bonus.PoisonedApple;
import fr.ubx.poo.ubgarden.game.go.decor.Decor;
import fr.ubx.poo.ubgarden.game.go.decor.DoorNextOpened;
import fr.ubx.poo.ubgarden.game.go.decor.Flowers;
import fr.ubx.poo.ubgarden.game.go.decor.NestHornet;
import fr.ubx.poo.ubgarden.game.go.decor.NestWasp;
import fr.ubx.poo.ubgarden.game.go.decor.Tree;
import fr.ubx.poo.ubgarden.game.go.decor.ground.Grass;
import fr.ubx.poo.ubgarden.game.go.decor.ground.Land;

public class Gardener extends GameObject implements Movable, PickupVisitor, WalkVisitor {

    private int energy;
    private int bombs = 0;
    private Direction direction;
    private int collected_carrot = 0;
    private boolean moveRequested = false;
    private int disease;

    private final Timer recoveryTimer;
    private Timer diseaseTimer;
    private static final long ENERGY_RECOVERY_DELAY = 1_000_000_000L; // 1 seconde en nanosecondes

    public Gardener(Game game, Position position) {
        super(game, position);
        this.direction = Direction.DOWN;
        this.energy = game.configuration().gardenerEnergy();
        this.recoveryTimer = new Timer(1); // Durée ici peu importante, le Timer est réinitialisé manuellement
        this.recoveryTimer.start();
        this.disease = 0;
        this.diseaseTimer = new Timer(game.configuration().diseaseDuration());
        this.diseaseTimer.start();
    }

    @Override
    public void pickUp(EnergyBoost energyBoost) {
        this.energy = Math.min(game.configuration().gardenerEnergy(),
                this.energy + game.configuration().energyBoost());
        System.out.println("🍎 Pomme ramassée ! Énergie = " + this.energy);
        energyBoost.remove();
    }
   
    
    public int getEnergy() {
        return this.energy;
    }

    public void requestMove(Direction direction) {
        if (direction != this.direction) {
            this.direction = direction;
            setModified(true);
        }
        moveRequested = true;
    }

   
    
    @Override
    public final boolean canMove(Direction direction) {
        Position nextPos = direction.nextPosition(getPosition());
        Decor dec = game.world().getGrid().get(nextPos);
        if (dec instanceof Tree || dec instanceof Flowers || 
                game.world().getGrid().width() <= nextPos.x() ||
                game.world().getGrid().height() <= nextPos.y() ||
                nextPos.x() < 0 || nextPos.y() < 0 || dec instanceof NestHornet || dec instanceof NestWasp){
            return false;
        }
        return true;
    }


    @Override
public Position move(Direction direction) {
    if (!canMove(direction)) {
        return getPosition();
    }

    Position nextPos = direction.nextPosition(getPosition());
    Decor next = game.world().getGrid().get(nextPos);

    // Consommation d'énergie en fonction du terrain
    if (next instanceof Grass) {
        energy = Math.max(0, energy - 1);
        System.out.println("🌿 Déplacement sur l'herbe : -1 énergie → " + energy);
    } else if (next instanceof Land) {
        energy = Math.max(0, energy - 2);
        System.out.println("🌾 Déplacement sur terre : -2 énergie → " + energy);
    } else {
        energy = Math.max(0, energy - 1); // Par défaut, 1 énergie
        System.out.println("🔋 Déplacement normal : -1 énergie → " + energy);
    }

    setPosition(nextPos);

    if (next != null)
        next.pickUpBy(this);

    // Changement de niveau si porte ouverte
    if (next instanceof DoorNextOpened) {
        game.requestSwitchLevel(game.world().currentLevel() + 1);
        game.world().setCurrentLevel(game.world().currentLevel() + 1);
    }

    return nextPos;
}

    private void restoreEnergy(long now) {
        recoveryTimer.update(now);
        if (recoveryTimer.isTimeElapsed(now, ENERGY_RECOVERY_DELAY)) {
            if (energy < game.configuration().gardenerEnergy()) {
                energy++;
                System.out.println("💤 Récupération d'énergie. Énergie actuelle : " + energy);
            }
            recoveryTimer.start(); // Redémarre le timer
        }
    }
    public void update(long now) {
        // Gestion de la récupération d'énergie
        if (!moveRequested) {
            restoreEnergy(now);
        }
        
        // Gestion de la maladie
        diseaseTimer.update(now);
        if (diseaseTimer.isTimeElapsed(now, game.configuration().diseaseDuration())) {
            if (this.disease > 1) {
                this.disease = 1; // Retour au niveau normal
                System.out.println("😌 Maladie guérie après " + 
                                 game.configuration().diseaseDuration() / 1000 + "s");
            }
            diseaseTimer.start();
        }
        
        // Effet continu de la maladie sur les déplacements
        if (this.disease > 1 && moveRequested) {
            int extraCost = this.disease - 1; // Coût supplémentaire par niveau
            this.energy = Math.max(0, this.energy - extraCost);
        }
    
        // Si l'énergie est insuffisante pour bouger, on empêche le mouvement
        if (this.energy <= 0) {
            System.out.println("🔋 Energie insuffisante pour se déplacer.");
            moveRequested = false; // Ne permet pas de déplacer
            return; // On arrête l'update ici si l'énergie est insuffisante
        }
    
        // Si un mouvement est demandé, procéder au mouvement
        if (moveRequested) {
            // Logique pour déplacer le jardinier
            if (canMove(direction)) {
                move(direction);  // Déplace le jardinier dans la direction demandée
                System.out.println("Déplacement effectué dans la direction : " + direction);
            } else {
                System.out.println("Mouvement impossible vers la direction : " + direction);
            }
            moveRequested = false; // Réinitialiser la demande de mouvement
        }
    }
    
    
    public void hurt(int damage) {
        // Dégâts amplifiés par la maladie
        int actualDamage = damage * this.disease;
        this.energy = Math.max(0, this.energy - actualDamage);
        
        System.out.println("💔 Dégâts " + damage + " x" + this.disease + 
                          " (maladie) = " + actualDamage + 
                          " | Énergie: " + this.energy);
    }

    public void hurt() {
        hurt(1);
    }

    public Direction getDirection() {
        return direction;
    }

    public void pickUp(Carrots carrots) {
        this.collected_carrot += 1;
        carrots.remove();
    }

    

    public void pickUp(PoisonedApple poisonedApple) {
    disease++;
    diseaseTimer.start();
    System.out.println("🤢 Trognon pourri ramassé ! Maladie activée (niveau de maladie : " + disease + ")");
    poisonedApple.remove();
    energy -= 5;
    }

    public void pickUp(Insecticide insecticide) {
        bombs++;
        System.out.println("💣 Bombe ramassée ! Total = " + bombs);
        insecticide.remove();
    }

    public int getBombs() {
        return bombs;
    }

    public int getDisease() {
        return disease;
    }

    public void setBombs(int i) {
        this.bombs = i;
    }

    public int getCollected_carrot() {
        return collected_carrot;
    }

   
}
