package labyrinth.Core;

import edu.princeton.cs.introcs.StdDraw;
import labyrinth.TileEngine.TERenderer;
import labyrinth.TileEngine.TETile;
import labyrinth.TileEngine.Tileset;

import java.awt.*;
import java.io.*;
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
    private Player player;
    private Enemy enemy;
    private Thread enemyThread;
    private boolean running = true;

    public Engine() {
        infoRenderer.initialize(WIDTH, HEIGHT + INFO_HEIGHT, 0, HEIGHT);
        worldRenderer.initialize(WIDTH, HEIGHT + INFO_HEIGHT);
    }

    public void start() {

        showStartMenu();

        // build the labyrinth
        while(true) {
            if(StdDraw.hasNextKeyTyped()) {
                char ch = StdDraw.nextKeyTyped();
                // new labyrinth
                if (ch == 'N' || ch == 'n') {
                    long seed = getSeedFromInput();
                    StdDraw.setFont(new Font("Menlo", Font.BOLD, 16));
                    newGame(seed);
                    break;
                }
                else if (ch == 'L' || ch == 'l') {
                    loadGame();
                    break;
                }
                else if (ch == 'Q' || ch == 'q') {
                    return;
                }
            }
        }

        interactWithKeyboard();
        start();
    }
    private void loadGame() {
        WorldForSave wfs;
        try {
            FileInputStream fileIn = new FileInputStream("/tmp/worldForSave.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            wfs = (WorldForSave) in.readObject();
            in.close();
            fileIn.close();
        } catch(IOException i) {
            i.printStackTrace();
            return;
        } catch(ClassNotFoundException c) {
            System.out.println("WorldForSave class not found");
            c.printStackTrace();
            return;
        }
        if(wfs != null) {
            world = wfs.world;
            info = wfs.info;
            RANDOM = wfs.RANDOM;
            player = wfs.player;
            enemy = wfs.enemy;

            // turn TETile object in world to Tileset.XXX
            for (int i = 0; i < world.length; i++)
                for (int j = 0; j < world[0].length; j++)
                    world[i][j] = Tileset.get(world[i][j].description());

            enemyThread = new Thread(enemy);
            enemyThread.start();
        }
    }
    private void newGame(long seed) {

        System.out.println("seed: " + seed);
        // initialize
        world  = new TETile[WIDTH][HEIGHT];
        info   = new TETile[WIDTH][INFO_HEIGHT];
        RANDOM = new Random(seed);

        for (int i = 0; i < WIDTH; ++i)
            for (int j = 0; j < HEIGHT; j++)
                world[i][j] = Tileset.NOTHING;
        for (int i = 0; i < WIDTH; i++)
            for (int j = 0; j < INFO_HEIGHT; j++)
                info[i][j] = Tileset.SPACE;

        // generate the world and render
        generateRooms();
        generateHallways();
        generateLockedDoor();
        generatePlayer();
        generateEnemy();
        generateKeys();
        render();

        enemyThread = new Thread(enemy);
        enemyThread.start();
    }

    public void interactWithKeyboard() {

        // already built the labyrinth.
        // starts to play.
        long initialTime = System.nanoTime();
        long currentTime;
        running = true;
        while (running) {
            // show what the mouse is pointing at
            int mouseX = (int)StdDraw.mouseX();
            int mouseY = (int)StdDraw.mouseY();
            if(mouseX >= 0 && mouseX < WIDTH && mouseY >= 0 && mouseY < HEIGHT) {
                String pointAt = world[mouseX][mouseY].description();
                writeInfo(WIDTH / 2, 1, 12, pointAt.equals("NOTHING") ? "" : pointAt, Color.yellow);
            }

            // 60 FPS
            currentTime = System.nanoTime();
            if(currentTime - initialTime > 16666666) {
                render();
                initialTime = currentTime;
            }

            // press <W> <A> <S> <D> to move
            if(StdDraw.hasNextKeyTyped()) {
                char ch = StdDraw.nextKeyTyped();
                switch (ch) {
                    case 'w': case 'W': player.move(Direction.UP); break;
                    case 'a': case 'A': player.move(Direction.LEFT); break;
                    case 's': case 'S': player.move(Direction.DOWN); break;
                    case 'd': case 'D': player.move(Direction.RIGHT); break;
                    case 27 : handleSaveAndQuit(); break;   // ESC pressed
                }
            }
        }
    }

    private void handleSaveAndQuit() {
        // pause enemy thread
        enemy.pause();
        showMiddleMenu();
        while(true) {
            if (StdDraw.hasNextKeyTyped()) {
                char ch = StdDraw.nextKeyTyped();
                switch (ch) {
                    case 's': case 'S':
                        saveGame();
                        synchronized (enemy) {
                            enemy.notifyAll();
                        }
                        return;
                    case 'q': case 'Q':
                        // save game and quit to main menu
                        saveGame();
                        synchronized (enemy) {
                            enemy.notifyAll();
                        }
                        running = false;
                        System.out.println(Thread.activeCount());
                        // stop enemy thread
                        try {
                            enemy.stop();
                            enemyThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return;
                    case 27:
                        synchronized (enemy) {
                            enemy.notifyAll();
                        }
                        return;
            }
            }
        }
    }

    private void saveGame() {
        try
        {
            FileOutputStream fileOut =
                    new FileOutputStream("/tmp/worldForSave.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            WorldForSave wfs = new WorldForSave(world, info, RANDOM, player, enemy);
            out.writeObject(wfs);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in /tmp/worldForSave.ser");
        }catch(IOException i)
        {
            i.printStackTrace();
        }
    }

    public TETile[][] interactWithInputString(String input) {
        long seed = 0;
        for (int i = 1; i < input.length() - 1; ++i)
            seed = seed * 10 + input.charAt(i) - '0';
        newGame(seed);
        return world;
    }


    private void generateKeys() {
        for (int i = 0; i < 3; i++) {
            world[20+i*5][10] = Tileset.KEY;
        }
    }

    private void render() {
        worldRenderer.renderFrame(world);

        writeInfo(2, 1, 5, "YOU:@", Color.white);
        writeInfo(9, 1, 7, "HEALTH:", Color.white);
        writeInfo(16, 1, 5, "" + player.health, Color.pink);
        writeInfo(WIDTH - 12, 1, 9, "MENU(ESC)", Color.white);

        infoRenderer.renderFrame(info);
    }

    private void generateEnemy() {
        int x, y;

        while (true) {
            x = RANDOM.nextInt(WIDTH * 2 /3);
            y = RANDOM.nextInt(HEIGHT);
            int count = 0;
            while (world[x][y] != Tileset.FLOOR) {
                y = (y + 1) % HEIGHT;
                if (++count > 6) break; // change x
            }
            if (world[x][y] == Tileset.FLOOR) break;
        }
        enemy = new Enemy(world, x,y, Tileset.ENEMY, player);
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
        player = new Player(world, x, y, INITIAL_HEALTH, Tileset.AVATAR);
    }
    private void generateLockedDoor() {
        int x, y;
        while (true) {
            // make sure the locked door is near right edge
            x = WIDTH - 3 - RANDOM.nextInt(WIDTH / 10);
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
        int leastTotalArea = (int) (WIDTH * HEIGHT * 0.3);
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

    // helper methods
    private void showStartMenu() {
        StdDraw.setPenColor(StdDraw.WHITE);
        //StdDraw.setFont(new Font("Menlo", Font.PLAIN, 20));
        StdDraw.clear(StdDraw.BLACK);

        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.7, "LABYRINTH");
        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.6, "NEW GAME  --- Press N");
        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.5, "LOAD GAME --- Press L");
        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.4, "QUIT      --- Press Q");
        StdDraw.show();
    }
    private void showMiddleMenu() {
        StdDraw.setPenColor(StdDraw.WHITE);
        //StdDraw.setFont(new Font("Menlo", Font.PLAIN, 20));
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.7, "LABYRINTH");

        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.6, "SAVE   --- Press S");
        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.5, "QUIT   --- Press Q");
        StdDraw.text(WIDTH * 0.5, HEIGHT * 0.4, "CANCEL --- Press ESC");
        StdDraw.show();
    }
    private void writeInfo(int x, int y, int totalLength, String s, Color textColor) {

        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            info[x++][y] = ch == '@' ? player.image : new TETile(Tileset.get(ch + ""), textColor);
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
}

