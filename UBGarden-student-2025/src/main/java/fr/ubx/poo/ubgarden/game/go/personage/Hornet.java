package fr.ubx.poo.ubgarden.game.go.personage;


import java.util.Random;

import fr.ubx.poo.ubgarden.game.Direction;
import fr.ubx.poo.ubgarden.game.Game;
import fr.ubx.poo.ubgarden.game.Position;
import fr.ubx.poo.ubgarden.game.engine.Timer;
import fr.ubx.poo.ubgarden.game.go.GameObject;
import fr.ubx.poo.ubgarden.game.go.Movable;
import fr.ubx.poo.ubgarden.game.go.bonus.Insecticide;
import fr.ubx.poo.ubgarden.game.go.decor.Decor;

public class Hornet extends GameObject implements Movable {
    private int energy;
    private Direction direction;
    private boolean moveRequested = false;
    private int stingsLeft = 2;
    public long time_to_move;

    public Timer moved;
    
    public Hornet(Game game, Position position) {

        super(game, position);
        this.direction = Direction.DOWN;
        this.energy = game.configuration().gardenerEnergy();
        this.moved = new Timer(1);
        moved.start();
        time_to_move = System.nanoTime();
    }
     public void take(Insecticide ins) {
        ins.remove();
        this.remove();

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

    public boolean isAlive() {
        return stingsLeft > 0;
    }

    public void sting(Gardener gardener) {
        gardener.hurt(1); // Réduit l'énergie du jardinier en cas de piqûre
        stingsLeft--;
    }

    @Override
    public final boolean canMove(Direction direction) {
        Position nextPos = direction.nextPosition(getPosition());
        if (game.world().getGrid().width() <= nextPos.x() ||
                game.world().getGrid().height() <= nextPos.y() ||
                nextPos.x() < 0 || nextPos.y() < 0){
            return false;
        }
        return true;
    }
    @Override
    public Position move(Direction direction) {
      Position nextPos = direction.nextPosition(getPosition());
        Decor next = game.world().getGrid().get(nextPos);
        setPosition(nextPos);
        if (next != null)
            next.pickUpBy(this);
        return nextPos;
    }

   public void update(long now) {

            if (now - time_to_move > 1000000000 / game.configuration().hornetMoveFrequency()) {
                time_to_move = now;
                Random random = new Random();
                do {
                    int r1 = random.nextInt(4);
                    switch (r1) {
                        case 0:
                            this.direction = Direction.UP;
                            break;
                        case 1:
                            this.direction = Direction.DOWN;
                            break;
                        case 2:
                            this.direction = Direction.LEFT;
                            break;
                        case 3:
                            this.direction = Direction.RIGHT;
                            break;
                        default:
                            throw new RuntimeException("le probleme se trouve dans la update de random de hornet ");
                    }
                }while(!canMove(this.direction));

                move(direction);

            }
        /*Position nextPos = direction.nextPosition(getPosition());
        if (nextPos.x() == game.getGardener().getPosition().x() &&
                nextPos.y() == game.getGardener().getPosition().y() ) {
            this.energy = 0;
        }*/



    }

    public Direction getDirection() {
        return direction;
    }

   
}
