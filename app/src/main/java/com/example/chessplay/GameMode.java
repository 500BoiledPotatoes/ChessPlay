package com.example.chessplay;

public class GameMode {
    private final int modeNr;

    public static final int PLAYER_WHITE  = 1;
    public static final int PLAYER_BLACK  = 2;
    public static final int TWO_PLAYERS   = 3;

    public GameMode(int modeNr) {
        this.modeNr = modeNr;
    }


    /** Return true if white side is controlled by a human. */
    public final boolean playerWhite() {
        switch (modeNr) {
        case PLAYER_WHITE:
        case TWO_PLAYERS:
            return true;
        default:
            return false;
        }
    }

    /** Return true if black side is controlled by a human. */
    public final boolean playerBlack() {
        switch (modeNr) {
        case PLAYER_BLACK:
        case TWO_PLAYERS:
            return true;
        default:
            return false;
        }
    }


    /** Return true if it is a humans turn to move. */
    public final boolean humansTurn(boolean whiteMove) {
        return whiteMove ? playerWhite() : playerBlack();
    }

    /** Return true if the clocks are running. */
    public final boolean clocksActive() {
        switch (modeNr) {
        case PLAYER_WHITE:
        case PLAYER_BLACK:
        case TWO_PLAYERS:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (o.getClass() != this.getClass()))
            return false;
        GameMode other = (GameMode)o;
        return modeNr == other.modeNr;
    }

    @Override
    public int hashCode() {
        return modeNr;
    }
}
