package com.keshane.blitz;

class Direction {
    static final Direction NORTH = new Direction(0, 1);
    static final Direction EAST = new Direction(1, 0);
    static final Direction SOUTH = new Direction(0, -1);
    static final Direction WEST = new Direction(-1, 0);

    static final Direction NORTHEAST = new Direction(1, 1);
    static final Direction SOUTHEAST = new Direction(1, -1);
    static final Direction SOUTHWEST = new Direction(-1, -1);
    static final Direction NORTHWEST = new Direction(-1, 1);

    static final Direction NORTH_NORTHEAST = new Direction(1, 2);
    static final Direction EAST_NORTHEAST = new Direction(2, 1);
    static final Direction EAST_SOUTHEAST = new Direction(2, -1);
    static final Direction SOUTH_SOUTHEAST = new Direction(1, -2);
    static final Direction SOUTH_SOUTHWEST = new Direction(-1, -2);
    static final Direction WEST_SOUTHWEST = new Direction(-2, -1);
    static final Direction WEST_NORTHWEST = new Direction(-2, 1);
    static final Direction NORTH_NORTHWEST = new Direction(-1, 2);
    final int file;
    final int rank;

    Direction(int file, int rank) {
        this.file = file;
        this.rank = rank;

    }
}
