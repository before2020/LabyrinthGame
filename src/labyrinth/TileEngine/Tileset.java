package labyrinth.TileEngine;

import java.awt.*;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 *
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 *
 * Ex:
 *      world[x][y] = Tileset.FLOOR;
 *
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */

public class Tileset {
    public static final TETile AVATAR = new TETile('Ǿ', Color.cyan, Color.black, "YOU");
    public static final TETile WALL = new TETile('#', new Color(216, 128, 128), Color.darkGray,
            "WALL");
    public static final TETile FLOOR = new TETile('·', new Color(128, 192, 128), Color.black,
            "FLOOR");
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "NOTHING");
    public static final TETile GRASS = new TETile('"', Color.green, Color.black, "GRASS");
    public static final TETile WATER = new TETile('≈', Color.blue, Color.black, "WATER");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, Color.pink, "FLOWER");
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "LOCKED DOOR");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "UNLOCKED DOOR");
    public static final TETile SAND = new TETile('▒', Color.yellow, Color.black, "SAND");
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black, "MOUNTAIN");
    public static final TETile TREE = new TETile('♠', Color.green, Color.black, "TREE");
    public static final TETile ENEMY = new TETile('Ѿ', Color.red, Color.black, "ENEMY");
    public static final TETile COLON = new TETile(':', Color.white, Color.black, "COLON");
    public static final TETile SPACE = new TETile(' ', Color.white, Color.black, "SPACE");

    public static final TETile[] NUMBERS = new TETile[10];
    public static final TETile[] LETTERS = new TETile[26];

    static {
        char[] numberArray = "0123456789".toCharArray();
        char[] letterArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        for (int i = 0; i < NUMBERS.length; i++) {
            NUMBERS[i] = new TETile(numberArray[i], Color.white, Color.black, "" + i);
        }
        for (int i = 0; i < LETTERS.length; i++) {
            LETTERS[i] = new TETile(letterArray[i], Color.yellow, Color.black, letterArray[i] + "");
        }
    }
}

