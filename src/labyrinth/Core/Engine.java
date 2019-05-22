package labyrinth.Core;

import labyrinth.TileEngine.TERenderer;
import labyrinth.TileEngine.TETile;
import labyrinth.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Engine {

    public static final int WIDTH = 80;         // actual world: WIDTH * HEIGHT
    public static final int HEIGHT = 30;
    private static final int INFO_HEIGHT = 3;   // info area on the top: WIDTH * INFO_HEIGHT
    private static final int INITIAL_HEALTH = 100;

    private TERenderer worldRenderer = new TERenderer(); // render actual world
    private TERenderer infoRenderer = new TERenderer();  // render info area
    private TETile[][] world;
    private TETile[][] info;
    private Random RANDOM;  // seed is provided by user
    private int filled = 0; // approximate number of grids filled with wall or floor
    private ArrayList<Room> rooms = new ArrayList<>();
    private int playerX, playerY; // coordinate of the player
    private int health;

    public Engine() {
        infoRenderer.initialize(WIDTH, HEIGHT + INFO_HEIGHT, 0, HEIGHT);
        worldRenderer.initialize(WIDTH, HEIGHT + INFO_HEIGHT);
    }

    public void interactWithKeyboard() {

        showMenu();

        // build the labyrinth
        while(true) {
            if(StdDraw.hasNextKeyTyped()) {

                char ch = StdDraw.nextKeyTyped();

                // new labyrinth
                if (ch == 'N' || ch == 'n') {
                    long seed = getSeedFromInput();
                    StdDraw.setFont(new Font("Menlo", Font.BOLD, 16));
                    generateWorld(seed);
                    break;
                }
                else if (ch == 'L' || ch == 'l') {
                    // loadGame();
                    break;
                }
            }
        }

        // already built the labyrinth. starts to play.
        while (true) {
            // show what the mouse is pointing at
            int mouseX = (int)StdDraw.mouseX();
            int mouseY = (int)StdDraw.mouseY();
            if(mouseX >= 0 && mouseX < WIDTH && mouseY >= 0 && mouseY < HEIGHT) {
                String pointAt = world[mouseX][mouseY].description();
                writeInfo(WIDTH - 12, 1, 12, pointAt.equals("NOTHING") ? "" : pointAt);
                infoRenderer.renderFrame(info);
            }

            // press <W> <A> <S> <D> to move
            if(StdDraw.hasNextKeyTyped()) {
                char ch = StdDraw.nextKeyTyped();
                switch (ch) {
                    case 'w': case 'W': goUp(); break;
                    case 'a': case 'A': goLeft(); break;
                    case 's': case 'S': goDown(); break;
                    case 'd': case 'D': goRight(); break;
                }
            }
        }
    }
    public TETile[][] interactWithInputString(String input) {
        long seed = 0;
        for (int i = 1; i < input.length() - 1; ++i)
            seed = seed * 10 + input.charAt(i) - '0';
        generateWorld(seed);
        return world;
    }

    private void generateWorld(long seed) {

        // initialize
        world  = new TETile[WIDTH][HEIGHT];
        info   = new TETile[WIDTH][INFO_HEIGHT];
        RANDOM = new Random(seed);
        health = INITIAL_HEALTH;

        for (int i = 0; i < WIDTH; ++i)
            for (int j = 0; j < HEIGHT; j++)
                world[i][j] = Tileset.NOTHING;
        for (int i = 0; i < WIDTH; i++)
            for (int j = 0; j < INFO_HEIGHT; j++)
                info[i][j] = Tileset.SPACE;

        // write info at top info area and render
        writeInfo(2, 1, 5, "YOU:@");
        writeInfo(9, 1, 12, "HEALTH:" + health);
        infoRenderer.renderFrame(info);

        // generate the world and render
        generateRooms();
        generateHallways();
        generateLockedDoor();
        generatePlayer();
        generateEnemy();
        worldRenderer.renderFrame(world);
    }

    private void generateEnemy() {
        world[50][20] = Tileset.ENEMY;
    }

    private void generatePlayer() {
        int x, y;

        while (true) {
            // make sure player is near left edge
            x = RANDOM.nextInt(WIDTH / 8) + 1;
            y = RANDOM.nextInt(HEIGHT);
            int count = 0;
            while (world[x][y] != Tileset.FLOOR) {
                y = (y + 2) % HEIGHT;
                if (++count > 15) break; // change x
            }
            if (world[x][y] == Tileset.FLOOR) break;
        }
        world[x][y] = Tileset.AVATAR;
        playerX = x;
        playerY = y;
    }
    private void generateLockedDoor() {
        int x, y;
        while (true) {
            // make sure the locked door is near right edge
            x = WIDTH - 1 - RANDOM.nextInt(WIDTH / 10);
            y = RANDOM.nextInt(HEIGHT);
            int count = 0;
            while (world[x][y] != Tileset.WALL) {
                y = (y + 2) % HEIGHT;
                if (++count > 15) break;
            }
            // DOOR must be next to FLOOR
            if (world[x][y] == Tileset.WALL) {
                if (world[x - 1][y] == Tileset.FLOOR
                        || x < WIDTH - 1 && world[x + 1][y] == Tileset.FLOOR
                        || y > 0 && world[x][y - 1] == Tileset.FLOOR
                        || y < HEIGHT - 1 && world[x][y + 1] == Tileset.FLOOR)
                    break;
            }
        }
        world[x][y] = Tileset.LOCKED_DOOR;
    }
    private void generateRooms() {
        int leastTotalArea = (int) (WIDTH * HEIGHT * 0.4);
        int maxRoomArea = (int) (WIDTH * HEIGHT * 0.15);
        int count = 0;
        while (filled < leastTotalArea) {
            int X = RANDOM.nextInt(WIDTH);
            int Y = randomYForSomeX(X);
            if (Y < 0) continue;
            int[] wh = randomWidthAndHeight(X, Y);

            // keep one space away from other rooms on all directions of the room
            if (wh != null && wh[0] * wh[1] <= maxRoomArea) {
                generateOneRoom(X + 1, Y + 1, wh[0], wh[1]);
                rooms.add(new Room(X + 1, Y + 1, wh[0], wh[1]));
                filled += wh[0] * wh[1];
            }

            if(++count > 2000) return;
        }
    }
    private void generateOneRoom(int startX, int startY, int w, int h) {
        for (int y = startY; y < startY + h; ++y) {
            world[startX][y] = Tileset.WALL;
            world[startX + w - 1][y] = Tileset.WALL;
        }
        for (int x = startX + 1; x <= startX + w - 2; ++x) {
            world[x][startY] = Tileset.WALL;
            world[x][startY + h - 1] = Tileset.WALL;
            for (int y = startY + 1; y <= startY + h - 2; ++y)
                world[x][y] = Tileset.FLOOR;
        }

    }
    private void generateHallways() {
        for (Room room : rooms) {
            int deltaX = room.w == 3 ? 0 : RANDOM.nextInt(room.w - 3);
            generateHallwayUp(room.x + deltaX, room.y + room.h - 1);
        }
        for (Room room : rooms) {
            int deltaY = room.h == 3 ? 0 : RANDOM.nextInt(room.h - 3);
            generateHallwayRight(room.x + room.w - 1, room.y + deltaY);
        }
        checkClosure();
    }
    private void generateHallwayRight(int startX, int startY) {
        world[startX][startY + 1] = Tileset.FLOOR;
        int x = startX + 1, y = startY;
        while(x < WIDTH - 1) {
            world[x][y + 1] = Tileset.FLOOR;
            if(world[x][y + 2] != Tileset.NOTHING) {
                world[x][y + 2] = Tileset.FLOOR;
                if (y + 3 < HEIGHT - 1) world[x][y + 3] = Tileset.FLOOR;
                break;
            }
            else if(world[x][y] != Tileset.NOTHING) {
                world[x][y] = Tileset.FLOOR;
                if(y > 1) world[x][y - 1] = Tileset.FLOOR;
                break;
            }
            x++;
        }
    }
    private void generateHallwayUp(int startX, int startY) {
        world[startX + 1][startY] = Tileset.FLOOR;
        int x = startX, y = startY + 1;
        while (y < HEIGHT - 1) {
            world[x + 1][y] = Tileset.FLOOR;
            if (world[x][y] != Tileset.NOTHING) {
                world[x][y] = Tileset.FLOOR;
                if(x > 1) world[x - 1][y] = Tileset.FLOOR;
                break;
            }
            else if (world[x + 2][y] != Tileset.NOTHING) {
                world[x + 2][y] = Tileset.FLOOR;
                if(x + 3 < WIDTH - 1) world[x + 3][y] = Tileset.FLOOR;
                break;
            }
            y++;
        }
    }

    // make sure everything is inside walls
    private void checkClosure() {
        int[] X = new int[] {-1, -1, -1, 0, 0, 1,  1, 1};
        int[] Y = new int[] {-1, 0,  1, -1, 1, -1, 0, 1};
        for (int i = 1; i < WIDTH - 1; ++i) {
            for (int j = 1; j < HEIGHT - 1; ++j) {
                if(world[i][j] == Tileset.FLOOR) {
                    for (int k = 0; k < 8; ++k) {
                        if(world[i + X[k]][j + Y[k]] == Tileset.NOTHING)
                            world[i + X[k]][j + Y[k]] = Tileset.WALL;
                    }
                }
            }
        }
    }

    // player movement
    private void goUp() {
        if(world[playerX][playerY + 1] != Tileset.WALL) {
            world[playerX][playerY] = Tileset.FLOOR;
            world[playerX][++playerY] = Tileset.AVATAR;
        }
        worldRenderer.renderFrame(world);
    }
    private void goDown() {
        if(world[playerX][playerY - 1] != Tileset.WALL) {
            world[playerX][playerY] = Tileset.FLOOR;
            world[playerX][--playerY] = Tileset.AVATAR;
        }
        worldRenderer.renderFrame(world);
    }
    private void goLeft() {
        if(world[playerX - 1][playerY] != Tileset.WALL) {
            world[playerX][playerY] = Tileset.FLOOR;
            world[--playerX][playerY] = Tileset.AVATAR;
        }
        worldRenderer.renderFrame(world);
    }
    private void goRight() {
        if(world[playerX + 1][playerY] != Tileset.WALL) {
            world[playerX][playerY] = Tileset.FLOOR;
            world[++playerX][playerY] = Tileset.AVATAR;
        }
        worldRenderer.renderFrame(world);
    }

    // helper methods
    private void showMenu() {
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Menlo", Font.PLAIN, 20));
        StdDraw.clear(StdDraw.BLACK);

        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.7, "LABYRINTH");
        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.6, "NEW GAME  --- Press N");
        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.5, "LOAD GAME --- Press L");
        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.4, "QUIT      --- Press Q");
        StdDraw.show();
    }
    private void writeInfo(int x, int y, int totalLength, String s) {

        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if (ch >= 'A' && ch <= 'Z')
                info[x++][y] = Tileset.LETTERS[ch - 'A'];
            else if (ch >= '0' && ch <= '9')
                info[x++][y] = Tileset.NUMBERS[ch - '0'];
            else if (ch == ' ')
                info[x++][y] = Tileset.SPACE;
            else if (ch == ':')
                info[x++][y] = Tileset.COLON;
            else if (ch == '@')
                info[x++][y] = Tileset.AVATAR;
        }

        // fill the rest with SPACE
        for (int i = 0; i < totalLength - s.length(); ++i)
            info[x++][y] = Tileset.SPACE;
    }
    private long getSeedFromInput() {
        String hint = "Please enter a positive number to generate a random world: ";
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.text(WIDTH*0.5, HEIGHT*0.7, hint);
        StdDraw.show();

        String input = "";

        while(true)
        {
            if(StdDraw.hasNextKeyTyped())
            {
                StdDraw.clear(StdDraw.BLACK);
                StdDraw.text(WIDTH*0.5, HEIGHT*0.7, hint);

                char ch = StdDraw.nextKeyTyped();

                if(ch == '\n') {
                    try {
                        long seed = Long.parseLong(input.substring(0, input.length() - 1));
                        if(seed > 0) return seed;
                    } catch (Exception e) {
                        StdDraw.text(WIDTH* 0.5, HEIGHT*0.3, "Invalid number!");
                        StdDraw.show();
                    }
                }
                else if(ch == '\b') {
                    if(input.length() > 0) input = input.substring(0, input.length() - 1);
                } else {
                    input += ch;
                }
                StdDraw.text(WIDTH*0.5, HEIGHT * 0.5, input);
                StdDraw.show();
            }
        }
    }
    private int randomYForSomeX(int X) {
        int Y = RANDOM.nextInt(HEIGHT);
        int count = 0;
        while (world[X][Y] != Tileset.NOTHING) {
            Y = (Y + 2) % HEIGHT;
            if (++count > 15) return -1;
        }
        return Y;
    }
    private int[] randomWidthAndHeight(int X, int Y) {
        int[] freeSpace = calcFreeSpace(X, Y);
        int maxWidth = freeSpace[0], maxHeight = freeSpace[1];

        // keep one space away from other rooms on all directions of the room
        if (maxWidth < 5 || maxHeight < 5) return null;
        int w = (maxWidth == 5)
                ? 3
                : 3 + RANDOM.nextInt(maxWidth - 5);
        w = Math.min(w, (int) (WIDTH * 0.2));
        int h = (maxHeight == 5)
                ? 3
                : 3 + RANDOM.nextInt(maxHeight - 5);

        return new int[]{w, h};
    }
    private int[] calcFreeSpace(int startX, int startY) {
        int x = startX, y = startY;
        int endX = startX, endY = startY;
        while (x < WIDTH) {
            y = startY;
            while (y < HEIGHT && world[x][y] == Tileset.NOTHING)
                y++;
            if (y - startY < 4) break;
            endX = x;
            if (endY == startY || y < endY) endY = y;
            x++;
        }
        return new int[]{endX - startX, endY - startY};
    }

    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.interactWithInputString("N63456572S");
    }
}

