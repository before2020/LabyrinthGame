package labyrinth.Core;

import labyrinth.TileEngine.TETile;
import labyrinth.TileEngine.Tileset;

import java.io.Serializable;

public class Role implements Serializable {
    TETile[][] world;
    int x;
    int y;
    TETile image;

    public Role(){}
    public Role(TETile[][] world, int x, int y, TETile image) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.image = image;
        world[x][y] = image;
    }

    public void move(Direction d) {
        switch(d) {
            case UP:    goUp();    break;
            case DOWN:  goDown();  break;
            case LEFT:  goLeft();  break;
            case RIGHT: goRight(); break;
        }
    }

    private void goUp() {
        if (world[x][y + 1] != Tileset.WALL) {
            world[x][y] = Tileset.FLOOR;
            world[x][++y] = image;
        }
    }

    private void goDown() {
        if (world[x][y - 1] != Tileset.WALL) {
            world[x][y] = Tileset.FLOOR;
            world[x][--y] = image;
        }
    }

    private void goLeft() {
        if (world[x - 1][y] != Tileset.WALL) {
            world[x][y] = Tileset.FLOOR;
            world[--x][y] = image;
        }
    }

    private void goRight() {
        if (world[x + 1][y] != Tileset.WALL) {
            world[x][y] = Tileset.FLOOR;
            world[++x][y] = image;
        }
    }
}
