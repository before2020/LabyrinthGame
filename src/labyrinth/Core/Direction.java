package labyrinth.Core;

enum Direction {
    UP(0), DOWN(1), LEFT(2), RIGHT(3);

    int index;
    Direction(int i) {
        index = i;
    }

    public int getIndex() {
        return index;
    }
}
