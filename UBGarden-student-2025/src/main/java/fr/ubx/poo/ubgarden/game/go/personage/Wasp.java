package fr.ubx.poo.ubgarden.game.go.personage;

import fr.ubx.poo.ubgarden.game.Direction;
import fr.ubx.poo.ubgarden.game.Game;
import fr.ubx.poo.ubgarden.game.Position;
import fr.ubx.poo.ubgarden.game.go.GameObject;
import fr.ubx.poo.ubgarden.game.go.Movable;
import fr.ubx.poo.ubgarden.game.go.decor.Decor;
import fr.ubx.poo.ubgarden.game.go.decor.Flowers;
import fr.ubx.poo.ubgarden.game.go.decor.Tree;

public class Wasp extends GameObject implements Movable {
    private Direction direction;
    private boolean moveRequested = false;
    private boolean alive = true;
    private final long waspMoveFrequency = 5_000_000_000L; // 1 seconde
    private long lastMove = 0;


    public Wasp(Game game, Position position) {
        super(game, position);
        this.direction = Direction.DOWN;
    }

    public void requestMove(Direction direction) {
        this.direction = direction;
        moveRequested = true;
    }

    public boolean isAlive() {
        return alive;
    }

    public void sting(Gardener gardener) {
        gardener.hurt(20);
        alive = false; // meurt après avoir piqué
    }

    
    @Override
    public Position move(Direction direction) {
        if (!canMove(direction)) return getPosition();
        Position nextPos = direction.nextPosition(getPosition());
        setPosition(nextPos);
        return nextPos;
    }

    @Override
    public void update(long now) {
        if (now - lastMove > waspMoveFrequency) {
            lastMove = now;
            Direction dir = Direction.random(); // Méthode aléatoire
            if (canMove(dir)) {
                move(dir);
            }
        }
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean canMove(Direction direction) {
    Position nextPos = direction.nextPosition(getPosition());
    Decor decor = game.world().getGrid().get(nextPos);

    // La guêpe peut se déplacer s’il n’y a rien ou que ce n’est pas un obstacle (comme un arbre ou des fleurs)
    return decor == null || !(decor instanceof Tree || decor instanceof Flowers);
}

}
