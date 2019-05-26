package labyrinth.TileEngine;

import java.awt.*;
import java.util.HashMap;

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
    private static final HashMap<String, TETile> map = new HashMap<>();

    public static final TETile AVATAR = new TETile('I', Color.cyan, Color.black, "AVATAR", "icons/avatar1.png");
    public static final TETile WALL = new TETile('#', new Color(184, 163, 22), new Color(83, 83, 83),
            "WALL", "icons/wall.png");
    public static final TETile FLOOR = new TETile('·', new Color(182, 142, 85), Color.black,
            "FLOOR");
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "NOTHING");
    public static final TETile GRASS = new TETile('"', Color.green, Color.black, "GRASS");
    public static final TETile WATER = new TETile('≈', Color.blue, Color.black, "WATER");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, Color.pink, "FLOWER");
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "LOCKED DOOR", "icons/locked_door.png");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "UNLOCKED DOOR");
    public static final TETile SAND = new TETile('▒', Color.yellow, Color.black, "SAND");
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black, "MOUNTAIN");
    public static final TETile TREE = new TETile('♠', Color.green, Color.black, "TREE");
    public static final TETile ENEMY = new TETile('E', Color.red, Color.black, "ENEMY");
    public static final TETile KEY = new TETile('?', Color.red, Color.black, "KEY", "icons/key.png");

    public static final TETile COLON = new TETile(':', Color.white, Color.black, ":");
    public static final TETile SPACE = new TETile(' ', Color.white, Color.black, "(");
    public static final TETile LEFT_BRACE = new TETile('(', Color.white, Color.black, ")");
    public static final TETile RIGHT_BRACE = new TETile(')', Color.white, Color.black, " ");
    public static final TETile[] NUMBERS = new TETile[10];
    public static final TETile[] LETTERS = new TETile[26];

    static {
        char[] numberArray = "0123456789".toCharArray();
        char[] letterArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        for (int i = 0; i < NUMBERS.length; i++) {
            NUMBERS[i] = new TETile(numberArray[i], Color.cyan, Color.black, "" + i);
            map.put("" + i, NUMBERS[i]);
        }
        for (int i = 0; i < LETTERS.length; i++) {
            LETTERS[i] = new TETile(letterArray[i], Color.white, Color.black, letterArray[i] + "");
            map.put("" + letterArray[i], LETTERS[i]);
        }
        map.put("AVATAR", AVATAR);
        map.put("WALL", WALL);
        map.put("FLOOR", FLOOR);
        map.put("NOTHING", NOTHING);
        map.put("FLOWER", FLOWER);
        map.put("GRASS", GRASS);
        map.put("WATER", WATER);
        map.put("LOCKED DOOR", LOCKED_DOOR);
        map.put("UNLOCKED DOOR", UNLOCKED_DOOR);
        map.put("SAND", SAND);
        map.put("MOUNTAIN", MOUNTAIN);
        map.put("TREE", TREE);
        map.put("ENEMY", ENEMY);
        map.put("KEY", KEY);
        map.put(":", COLON);
        map.put(" ", SPACE);
        map.put("(", LEFT_BRACE);
        map.put(")", RIGHT_BRACE);
    }

    // return TETile according to its description
    public static TETile get(String s) {
        return map.getOrDefault(s, NOTHING);
    }
}


