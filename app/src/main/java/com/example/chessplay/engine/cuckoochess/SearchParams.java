
package com.example.chessplay.engine.cuckoochess;

import java.util.ArrayList;
import java.util.List;

import chess.Move;

/** Store search parameters (times, increments, max depth, etc). */
public class SearchParams {
    List<Move> searchMoves;  // If non-empty, search only these moves
    int wTime;               // White remaining time, ms
    int bTime;               // Black remaining time, ms
    int wInc;                // White increment per move, ms
    int bInc;                // Black increment per move, ms
    int movesToGo;           // Moves to next time control
    int depth;               // If >0, don't search deeper than this
    int nodes;               // If >0, don't search more nodes than this
    int mate;                // If >0, search for mate-in-x
    int moveTime;            // If >0, search for exactly this amount of time, ms
    boolean infinite;

    public SearchParams() {
        searchMoves = new ArrayList<>();
    }
}
