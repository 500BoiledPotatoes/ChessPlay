package com.example.chessplay;

import com.example.chessplay.gamelogic.Game;
import com.example.chessplay.gamelogic.Move;
import com.example.chessplay.gamelogic.Position;

import java.util.ArrayList;

/** Interface between the GUI and the ChessController. */
public interface GUIInterface {

    /** Update the displayed board position. */
    void setPosition(Position pos, String variantInfo, ArrayList<Move> variantMoves);

    /** Mark square sq as selected. Set to -1 to clear selection. */
    void setSelection(int sq);

    final class GameStatus {
        public Game.GameState state = Game.GameState.ALIVE;
        public int moveNr = 0;
        /** Move required to claim draw, or empty string. */
        public String drawInfo = "";
        public boolean white = false;
        public boolean ponder = false;
        public boolean thinking = false;
        public boolean analyzing = false;
    }

    /** Set the status text. */
    void setStatus(GameStatus status);

    /** Update the list of moves. */
    void moveListUpdated();

//    final class ThinkingInfo {
//        public int id;
//        public String pvStr;
//        public String statStr;
//        public String bookInfo;
//        public ArrayList<ArrayList<Move>> pvMoves;
//        public ArrayList<Move> bookMoves;
//        public String eco;
//        public int distToEcoTree;
//    }

//    /** Update the computer thinking information. */
//    void setThinkingInfo(ThinkingInfo ti);

    /** Ask what to promote a pawn to. Should call reportPromotePiece() when done. */
    void requestPromotePiece();

    /** Run code on the GUI thread. */
    void runOnUIThread(Runnable runnable);

    /** Report that user attempted to make an invalid move. */
    void reportInvalidMove(Move m);

    /** Report UCI engine name. */
    void reportEngineName(String engine);

    /** Report UCI engine error message. */
    void reportEngineError(String errMsg);

    /** Called when a move is played. GUI can notify user, for example by playing a sound. */
    void movePlayed(Position pos, Move move, boolean computerMove);

    /** Report remaining thinking time to GUI. */
    void setRemainingTime(int wTime, int bTime, int nextUpdate);

    /** Update engine title text. */
    void updateEngineTitle(int elo);

    /** Update title with the material difference. */
    void updateMaterialDifferenceTitle(Util.MaterialDiff diff);

    /** Update title with time control information. */
    void updateTimeControlTitle();

    /** Report a move made that is a candidate for GUI animation. */
    void setAnimMove(Position sourcePos, Move move, boolean forward);

    /** Return true if positive analysis scores means good for white. */
    boolean whiteBasedScores();

    /** Return true if pondering (permanent brain) is enabled. */
    boolean ponderMode();

    /** Get the default player name. */
    String playerName();

    /** Return true if only main-line moves are to be kept. */
    boolean discardVariations();

//    /** Save the current game to the auto-save file, if storage permission has been granted. */
//    void autoSaveGameIfAllowed(String pgn);
}
