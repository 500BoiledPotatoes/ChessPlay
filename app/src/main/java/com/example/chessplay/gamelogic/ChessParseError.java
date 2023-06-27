
package com.example.chessplay.gamelogic;

/** Exception class to represent parse errors in FEN or algebraic notation. */
public class ChessParseError extends Exception {
    private static final long serialVersionUID = -6051856171275301175L;

    public Position pos;
    public int resourceId = -1;

    public ChessParseError(int resourceId) {
        super("");
        pos = null;
        this.resourceId = resourceId;
    }

    public ChessParseError(int resourceId, Position pos) {
        super("");
        this.pos = pos;
        this.resourceId = resourceId;
    }
}
