package labyrinth.Core;

public class Room {
    int x;
    int y;
    int w;
    int h;

    public Room(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ") " + "w: " + w + " h: " + h;
    }
}
