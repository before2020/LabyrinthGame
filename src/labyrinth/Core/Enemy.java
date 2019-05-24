package labyrinth.Core;

import labyrinth.TileEngine.TETile;
import labyrinth.TileEngine.Tileset;

import java.util.*;

public class Enemy extends Role implements Runnable {

    private Player player; // enemy will chase the player
    private List<Direction> path = new ArrayList<>(); // a series of actions to reach the player

    public Enemy(TETile[][] world, int x, int y, TETile image, Player player) {
        super(world, x, y, image);
        this.player = player;
    }

    @Override
    public void run() {

        updatePath();
        long initialTime = System.nanoTime();
        int ticks = 0;

        while (true) {

            long currentTime = System.nanoTime();
            if (currentTime - initialTime > 200000000) {
                if (!path.isEmpty())
                    move(path.remove(0));
                if (++ticks > 4) {
                    updatePath();
                    ticks = 0;
                }
                initialTime = currentTime;
            }
        }
    }

    private void updatePath() {
        if (player != null) {
            path = bfs();
            System.out.println(path);
        }
    }

    class State {
        int x;
        int y;

        State(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass())
                return false;
            State other = (State) obj;
            return this.x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return x * 41 + y;
        }
    }

    private List<Direction> bfs() {

        int[] X = new int[]{0, 0, -1, 1};
        int[] Y = new int[]{1, -1, 0, 0};
        List<Direction> solution = new ArrayList<>();

        HashMap<State, State> prevState = new HashMap<>();
        HashMap<State, Direction> prevAction = new HashMap<>();
        boolean[][] visited = new boolean[world.length][world[0].length];

        State startState = new State(x, y);
        State goalState = new State(player.x, player.y);
        if (startState.equals(goalState)) return solution;

        Queue<State> queue = new LinkedList<>();
        queue.add(startState);
        visited[x][y] = true;

        while (!queue.isEmpty()) {
            State nowState = queue.remove();
            for (Direction d : Direction.values()) {
                int newX = nowState.x + X[d.getIndex()];
                int newY = nowState.y + Y[d.getIndex()];
                if (!visited[newX][newY] && world[newX][newY] != Tileset.WALL) {
                    visited[newX][newY] = true;
                    State nextState = new State(newX, newY);
                    queue.add(nextState);
                    prevState.put(nextState, nowState);
                    prevAction.put(nextState, d);
                    if (nextState.equals(goalState)) {
                        System.out.println("Solution appears-----");
                        State s = goalState;
                        while (!s.equals(startState)) {
                            solution.add(prevAction.get(s));
                            s = prevState.get(s);
                        }
                        Collections.reverse(solution);
                        return solution;
                    }
                }
            }
        }
        return solution;
    }

}
