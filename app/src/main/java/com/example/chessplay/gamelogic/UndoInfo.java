

package com.example.chessplay.gamelogic;

/**
 * Contains enough information to undo a previous move.
 * Set by makeMove(). Used by unMakeMove().
 */
public class UndoInfo {
    int capturedPiece;
    int castleMask;
    int epSquare;
    int halfMoveClock;
}
