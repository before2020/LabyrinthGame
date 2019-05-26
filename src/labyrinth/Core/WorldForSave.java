package labyrinth.Core;

import labyrinth.TileEngine.TETile;

import java.io.Serializable;
import java.util.Random;

public class WorldForSave implements Serializable {
    TETile[][] world;
    TETile[][] info;
    Random RANDOM;
    Player player;
    Enemy enemy;

    public WorldForSave(TETile[][] world, TETile[][] info, Random RANDOM, Player player, Enemy enemy) {
        this.world = world;
        this.info = info;
        this.RANDOM = RANDOM;
        this.player = player;
        this.enemy = enemy;
    }
}
