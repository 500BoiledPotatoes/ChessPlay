

package com.example.chessplay.gamelogic;

import android.util.Pair;

import com.example.chessplay.EngineOptions;
import com.example.chessplay.GUIInterface;
import com.example.chessplay.GameMode;
import com.example.chessplay.PGNOptions;
import com.example.chessplay.Util;
import com.example.chessplay.engine.DroidComputerPlayer;
import com.example.chessplay.engine.DroidComputerPlayer.EloData;
import com.example.chessplay.engine.DroidComputerPlayer.SearchRequest;
import com.example.chessplay.engine.DroidComputerPlayer.SearchType;
import com.example.chessplay.gamelogic.Game.CommentInfo;
import com.example.chessplay.gamelogic.Game.GameState;
import com.example.chessplay.gamelogic.GameTree.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** The glue between the chess engine and the GUI. */
public class ChessController {
    private DroidComputerPlayer computerPlayer = null;
    private PgnToken.PgnTokenReceiver gameTextListener;
    private EngineOptions engineOptions = new EngineOptions();
    private Game game = null;
    private Move ponderMove = null;
    private GUIInterface gui;
    private GameMode gameMode;
    private PGNOptions pgnOptions;

    private String engine = "";
    private int numPV = 1;

    private SearchListener listener;
    private boolean guiPaused = false;

    /** Partial move that needs promotion choice to be completed. */
    private Move promoteMove;

    private int searchId;

    /** Constructor. */
    public ChessController(GUIInterface gui, PgnToken.PgnTokenReceiver gameTextListener, PGNOptions options) {
        this.gui = gui;
        this.gameTextListener = gameTextListener;
        gameMode = new GameMode(GameMode.TWO_PLAYERS);
        pgnOptions = options;
        listener = new SearchListener();
        searchId = 0;
    }

    /** Start a new game. */
    public final synchronized void newGame(GameMode gameMode, TimeControlData tcData) {
        boolean updateGui = abortSearch();
        if (updateGui)
            updateGUI();
        this.gameMode = gameMode;
        if (computerPlayer == null) {
            computerPlayer = new DroidComputerPlayer(listener);
            computerPlayer.setEngineOptions(engineOptions);
        }
        computerPlayer.queueStartEngine(searchId, engine);
        searchId++;
        game = new Game(gameTextListener, tcData);
        computerPlayer.uciNewGame();
        setPlayerNames(game);
        updateGameMode();
        game.resetModified(pgnOptions);
    }
    /** Start playing a new game. Should be called after newGame(). */
    public final synchronized void startGame() {
        updateComputeThreads();
        setSelection();
        updateGUI();
        updateGameMode();
    }

    /** @return Array containing time control, moves per session and time increment. */
    public final int[] getTimeLimit() {
        if (game != null)
            return game.timeController.getTimeLimit(game.currPos().whiteMove);
        return new int[]{5*60*1000, 60, 0};
    }

    /** The chess clocks are stopped when the GUI is paused. */
    public final synchronized void setGuiPaused(boolean paused) {
        guiPaused = paused;
        updateGameMode();
    }

    /** Set game mode. */
    public final synchronized void setGameMode(GameMode newMode) {
        if (!gameMode.equals(newMode)) {
            if (newMode.humansTurn(game.currPos().whiteMove))
                searchId++;
            gameMode = newMode;
            if (!gameMode.playerWhite() || !gameMode.playerBlack())
                setPlayerNames(game); // If computer player involved, set player names
            updateGameMode();
            gui.updateEngineTitle(getEloToUse()); // Game mode affects Elo setting
            restartSearch();
        }
    }

    private int getEloToUse() {
        return eloData().getEloToUse();
    }

    public final GameMode getGameMode() {
        return gameMode;
    }


    private void restartSearch() {
        if (game != null) {
            abortSearch();
            updateComputeThreads();
            updateGUI();
        }
    }

    /** Set engine. Restart computer thinking if appropriate. */
    public final synchronized void setEngine(String engine) {
        if (!engine.equals(this.engine)) {
            this.engine = engine;
            restartSearch();
        }
    }

    /** Set engine strength. Restart computer thinking if appropriate. */
    public final synchronized void setStrength(boolean limitStrength, int elo) {
        EloData d = eloData();
        int oldElo = d.getEloToUse();
        d.limitStrength = limitStrength;
        d.elo = elo;
        int newElo = d.getEloToUse();
        if (oldElo != newElo) {
            if (computerPlayer != null)
                computerPlayer.setStrength(newElo);
            restartSearch();
            gui.updateEngineTitle(newElo);
        }
    }

    /** Return engine Elo strength data. */
    public final synchronized EloData eloData() {
        if (computerPlayer == null)
            return new EloData();
        return computerPlayer.getEloData();
    }

    /** Return current engine identifier. */
    public final synchronized String getEngine() {
        return engine;
    }

    /** Notify controller that preferences has changed. */
    public final synchronized void prefsChanged(boolean translateMoves) {
        if (game == null)
            translateMoves = false;
        if (translateMoves)
            game.tree.translateMoves();
//        updateBookHints();
        updateMoveList();
        listener.prefsChanged(searchId, translateMoves);
        if (translateMoves)
            updateGUI();
    }

    /** De-serialize from byte array. */
    public final synchronized void fromByteArray(byte[] data, int version) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {
            game.readFromStream(dis, version);
            game.tree.translateMoves();
        } catch (IOException|ChessParseError ignore) {
        }
    }

    /** Serialize to byte array. */
    public final synchronized byte[] toByteArray() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(32768);
             DataOutputStream dos = new DataOutputStream(baos)) {
            game.writeToStream(dos);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }


    /** True if human's turn to make a move. (True in analysis mode.) */
    public final synchronized boolean humansTurn() {
        if (game == null)
            return false;
        return gameMode.humansTurn(game.currPos().whiteMove);
    }

    /** Make a move for a human player. */
    public final synchronized void makeHumanMove(Move m, boolean animate) {
        if (!humansTurn())
            return;
        Position oldPos = new Position(game.currPos());
        if (game.pendingDrawOffer) {
            ArrayList<Move> moves = new MoveGen().legalMoves(oldPos);
            for (Move m2 : moves) {
                if (m2.equals(m)) {
                    if (findValidDrawClaim(TextIO.moveToUCIString(m))) {
                        stopPonder();
                        updateGUI();
                        gui.setSelection(-1);
                        return;
                    }
                    break;
                }
            }
        }
        if (doMove(m)) {
            if (m.equals(ponderMove) &&
                    (computerPlayer.getSearchType() == SearchType.PONDER)) {
                computerPlayer.ponderHit(searchId);
                ponderMove = null;
            } else {
                abortSearch();
                updateComputeThreads();
            }
            if (animate)
                setAnimMove(oldPos, m, true);
            updateGUI();
        } else {
            gui.setSelection(-1);
        }
    }

    /** Report promotion choice for incomplete move.
     * @param choice 0=queen, 1=rook, 2=bishop, 3=knight. */
    public final synchronized void reportPromotePiece(int choice) {
        if (promoteMove == null)
            return;
        final boolean white = game.currPos().whiteMove;
        int promoteTo;
        switch (choice) {
            case 1:
                promoteTo = white ? Piece.WROOK : Piece.BROOK;
                break;
            case 2:
                promoteTo = white ? Piece.WBISHOP : Piece.BBISHOP;
                break;
            case 3:
                promoteTo = white ? Piece.WKNIGHT : Piece.BKNIGHT;
                break;
            default:
                promoteTo = white ? Piece.WQUEEN : Piece.BQUEEN;
                break;
        }
        promoteMove.promoteTo = promoteTo;
        Move m = promoteMove;
        promoteMove = null;
        makeHumanMove(m, true);
    }


    /** Resign game for current player. */
    public final synchronized void resignGame() {
        if (game.getGameState() == GameState.ALIVE) {
            game.processString("resign");
            updateGUI();
        }
    }

    /** Undo last move. Does not truncate game tree. */
    public final synchronized void undoMove() {
        if (game.getLastMove() != null) {
            abortSearch();
            boolean didUndo = undoMoveNoUpdate();
            updateComputeThreads();
            setSelection();
            if (didUndo)
                setAnimMove(game.currPos(), game.getNextMove(), false);
            updateGUI();
        }
    }

    /** Redo last move. Follows default variation. */
    public final synchronized void redoMove() {
        if (game.canRedoMove()) {
            abortSearch();
            redoMoveNoUpdate();
            updateComputeThreads();
            setSelection();
            setAnimMove(game.prevPos(), game.getLastMove(), true);
            updateGUI();
        }
    }


    /** Go to given node in game tree. */
    public final synchronized void goNode(Node node) {
        if (node == null)
            return;
        if (!game.goNode(node))
            return;
        if (!humansTurn()) {
            if (game.getLastMove() != null) {
                game.undoMove();
                if (!humansTurn())
                    game.redoMove();
            }
        }
        abortSearch();
        updateComputeThreads();
        setSelection();
        updateGUI();
    }

    /** Return true if the current variation can be moved closer to the main-line. */
    public final synchronized boolean canMoveVariationUp() {
        return game.canMoveVariation(-1);
    }

    /** Return true if the current variation can be moved farther away from the main-line. */
    public final synchronized boolean canMoveVariationDown() {
        return game.canMoveVariation(1);
    }

    /** Go to a new variation in the game tree. */
    public final synchronized void changeVariation(int delta) {
        if (game.numVariations() > 1) {
            abortSearch();
            game.changeVariation(delta);
            updateComputeThreads();
            setSelection();
            updateGUI();
        }
    }


    /** Update remaining time and trigger GUI update of clocks. */
    public final synchronized void updateRemainingTime() {
        long now = System.currentTimeMillis();
        int wTime = game.timeController.getRemainingTime(true, now);
        int bTime = game.timeController.getRemainingTime(false, now);
        int nextUpdate = 0;
        if (game.timeController.clockRunning()) {
            int t = game.currPos().whiteMove ? wTime : bTime;
            nextUpdate = t % 1000;
            if (nextUpdate < 0) nextUpdate += 1000;
            nextUpdate += 1;
        }
        gui.setRemainingTime(wTime, bTime, nextUpdate);
    }

    /** Return maximum number of PVs supported by engine. */
    public final synchronized int maxPV() {
        if (computerPlayer == null)
            return 1;
        return computerPlayer.getMaxPV();
    }

    /** Set multi-PV mode. */
    public final synchronized void setMultiPVMode(int numPV) {
        int clampedNumPV = Math.min(numPV, maxPV());
        clampedNumPV = Math.max(clampedNumPV, 1);
        boolean modified = clampedNumPV != this.numPV;
        this.numPV = numPV;
        if (modified)
            restartSearch();
    }

    /** Request computer player to make a move immediately. */
    public final synchronized void stopSearch() {
        if (!humansTurn() && (computerPlayer != null))
            computerPlayer.moveNow();
    }

    /** Stop ponder search. */
    public final synchronized void stopPonder() {
        if (humansTurn() && (computerPlayer != null)) {
            if (computerPlayer.getSearchType() == SearchType.PONDER) {
                boolean updateGui = abortSearch();
                if (updateGui)
                    updateGUI();
            }
        }
    }

    /** Shut down chess engine process. */
    public final synchronized void shutdownEngine() {
        gameMode = new GameMode(GameMode.TWO_PLAYERS);
        abortSearch();
        computerPlayer.shutdownEngine();
    }

    /** Get PGN header tags and values. */
    public final synchronized void getHeaders(Map<String,String> headers) {
        if (game != null)
            game.tree.getHeaders(headers);
    }

    /** Set PGN header tags and values. */
    public final synchronized void setHeaders(Map<String,String> headers) {
        boolean resultChanged = game.tree.setHeaders(headers);
        gameTextListener.clear();
        if (resultChanged) {
            abortSearch();
            updateComputeThreads();
            setSelection();
        }
        updateGUI();
    }

    /** Get comments associated with current position. */
    public final synchronized CommentInfo getComments() {
        Pair<CommentInfo,Boolean> p = game.getComments();
        if (p.second) {
            gameTextListener.clear();
            updateGUI();
        }
        return p.first;
    }

    /** Set comments associated with current position. "commInfo" must be an object
     *  (possibly modified) previously returned from getComments(). */
    public final synchronized void setComments(CommentInfo commInfo) {
        game.setComments(commInfo);
        gameTextListener.clear();
        updateGUI();
    }

    /** Return true if localized piece names should be used. */
    private boolean localPt() {
        switch (pgnOptions.view.pieceType) {
        case PGNOptions.PT_ENGLISH:
            return false;
        case PGNOptions.PT_LOCAL:
        case PGNOptions.PT_FIGURINE:
        default:
            return true;
        }
    }

    /** Engine search information receiver. */
    private final class SearchListener implements com.example.chessplay.gamelogic.SearchListener {
        private int currDepth = 0;
        private int currMoveNr = 0;
        private Move currMove = null;
        private String currMoveStr = "";
        private long currNodes = 0;
        private int currNps = 0;
        private long currTBHits = 0;
        private int currHash = 0;
        private int currTime = 0;
        private int currSelDepth = 0;

        private boolean whiteMove = true;

        private Move ponderMove = null;
        private ArrayList<PvInfo> pvInfoV = new ArrayList<>();
        private int pvInfoSearchId = -1; // Search ID corresponding to pvInfoV

        public final void clearSearchInfo(int id) {
            pvInfoSearchId = -1;
            ponderMove = null;
            pvInfoV.clear();
            currDepth = 0;
            setSearchInfo(id);
        }

        private void setSearchInfo(final int id) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < pvInfoV.size(); i++) {
                PvInfo pvi = pvInfoV.get(i);
                if (pvi.depth <= 0)
                    continue;
                if (i > 0)
                    buf.append('\n');
                buf.append(String.format(Locale.US, "[%d] ", pvi.depth));
                boolean negateScore = !whiteMove && gui.whiteBasedScores();
                if (pvi.upperBound || pvi.lowerBound) {
                    boolean upper = pvi.upperBound ^ negateScore;
                    buf.append(upper ? "<=" : ">=");
                }
                int score = negateScore ? -pvi.score : pvi.score;
                if (pvi.isMate) {
                    buf.append(String.format(Locale.US, "m%d", score));
                } else {
                    buf.append(String.format(Locale.US, "%.2f", score / 100.0));
                }

                buf.append(pvi.pvStr);
            }
            StringBuilder statStrTmp = new StringBuilder();
            if (currDepth > 0) {
                statStrTmp.append(String.format(Locale.US, "d:%d", currDepth));
                if (currSelDepth > 0)
                    statStrTmp.append(String.format(Locale.US, "/%d", currSelDepth));
                if (currMoveNr > 0)
                    statStrTmp.append(String.format(Locale.US, " %d:%s", currMoveNr, currMoveStr));
                if (currTime < 99995) {
                    statStrTmp.append(String.format(Locale.US, " t:%.2f", currTime / 1000.0));
                } else if (currTime < 999950) {
                    statStrTmp.append(String.format(Locale.US, " t:%.1f", currTime / 1000.0));
                } else {
                    statStrTmp.append(String.format(Locale.US, " t:%d", (currTime + 500) / 1000));
                }
                statStrTmp.append(" n:");
                appendWithPrefix(statStrTmp, currNodes);
                statStrTmp.append(" nps:");
                appendWithPrefix(statStrTmp, currNps);
                if (currTBHits > 0) {
                    statStrTmp.append(" tb:");
                    appendWithPrefix(statStrTmp, currTBHits);
                }
                if (currHash > 0)
                    statStrTmp.append(String.format(Locale.US, " h:%d", currHash / 10));
            }
            final String statStr = statStrTmp.toString();
            final String newPV = buf.toString();
            final ArrayList<ArrayList<Move>> pvMoves = new ArrayList<>();
            for (int i = 0; i < pvInfoV.size(); i++) {
                if (ponderMove != null) {
                    ArrayList<Move> tmp = new ArrayList<>();
                    tmp.add(ponderMove);
                    tmp.addAll(pvInfoV.get(i).pv);
                    pvMoves.add(tmp);
                } else {
                    pvMoves.add(pvInfoV.get(i).pv);
                }
            }
        }

        private void appendWithPrefix(StringBuilder sb, long value) {
            if (value > 100000000000L) {
                value /= 1000000000;
                sb.append(value);
                sb.append('G');
            } else if (value > 100000000) {
                value /= 1000000;
                sb.append(value);
                sb.append('M');
            } else if (value > 100000) {
                value /= 1000;
                sb.append(value);
                sb.append('k');
            } else {
                sb.append(value);
            }
        }

        @Override
        public void notifyDepth(int id, int depth) {
            currDepth = depth;
            setSearchInfo(id);
        }

        @Override
        public void notifyCurrMove(int id, Position pos, Move m, int moveNr) {
            Position tmpPos = new Position(pos);
            if (!TextIO.isValid(tmpPos, m))
                m = new Move(0, 0, 0);
            currMove = m;
            currMoveStr = TextIO.moveToString(tmpPos, m, false, localPt());
            currMoveNr = moveNr;
            setSearchInfo(id);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void notifyPV(int id, Position pos, ArrayList<PvInfo> pvInfo, Move ponderMove) {
            this.ponderMove = ponderMove;
            pvInfoSearchId = id;
            pvInfoV = (ArrayList<PvInfo>) pvInfo.clone();
            for (PvInfo pv : pvInfo) {
                currTime = pv.time;
                currNodes = pv.nodes;
                currNps = pv.nps;
                currTBHits = pv.tbHits;
                currHash = pv.hash;

                StringBuilder buf = new StringBuilder();
                Position tmpPos = new Position(pos);
                UndoInfo ui = new UndoInfo();
                if (ponderMove != null) {
                    String moveStr = TextIO.moveToString(tmpPos, ponderMove, false, localPt());
                    buf.append(String.format(Locale.US, " [%s]", moveStr));
                    tmpPos.makeMove(ponderMove, ui);
                }
                for (int i = 0; i < pv.pv.size(); i++) {
                    Move m = pv.pv.get(i);
                    if (!TextIO.isValid(tmpPos, m)) {
                        while (pv.pv.size() > i)
                            pv.pv.remove(pv.pv.size() - 1);
                        break;
                    }
                    String moveStr = TextIO.moveToString(tmpPos, m, false, localPt());
                    buf.append(String.format(Locale.US, " %s", moveStr));
                    tmpPos.makeMove(m, ui);
                }
                pv.pvStr = buf.toString();
            }
            whiteMove = pos.whiteMove ^ (ponderMove != null);

            setSearchInfo(id);
        }

        @Override
        public void notifyStats(int id, long nodes, int nps, long tbHits, int hash, int time, int seldepth) {
            currNodes = nodes;
            currNps = nps;
            currTBHits = tbHits;
            currHash = hash;
            currTime = time;
            currSelDepth = seldepth;
            setSearchInfo(id);
        }

        public void prefsChanged(int id, boolean translateMoves) {
            if (translateMoves && (id == pvInfoSearchId)) {
                Position pos = game.currPos();
                if (currMove != null)
                    notifyCurrMove(id, pos, currMove, currMoveNr);
                notifyPV(id, pos, pvInfoV, ponderMove);
            } else {
                setSearchInfo(id);
            }
        }

        @Override
        public void notifySearchResult(int id, String cmd, Move ponder) {
            new Thread(() -> gui.runOnUIThread(() -> makeComputerMove(id, cmd, ponder))).start();
        }

        @Override
        public void notifyEngineName(String engineName) {
            gui.runOnUIThread(() -> {
                updatePlayerNames(engineName);
                gui.reportEngineName(engineName);
            });
        }

        @Override
        public void notifyEngineInitialized() {
            gui.runOnUIThread(() -> {
                gui.updateEngineTitle(eloData().getEloToUse());
            });
        }

        @Override
        public void reportEngineError(final String errMsg) {
            gui.runOnUIThread(() -> gui.reportEngineError(errMsg));
        }
    }

    /** Discard current search. Return true if GUI update needed. */
    private boolean abortSearch() {
        ponderMove = null;
        searchId++;
        if (computerPlayer == null)
            return false;
        if (computerPlayer.stopSearch()) {
            listener.clearSearchInfo(searchId);
            return true;
        }
        return false;
    }

    private void updateGameMode() {
        if (game != null) {
            boolean gamePaused = !gameMode.clocksActive() || (humansTurn() && guiPaused);
            game.setGamePaused(gamePaused);
            updateRemainingTime();
            Game.AddMoveBehavior amb;
            if (gui.discardVariations())
                amb = Game.AddMoveBehavior.REPLACE;
            else if (gameMode.clocksActive())
                amb = Game.AddMoveBehavior.ADD_FIRST;
            else
                amb = Game.AddMoveBehavior.ADD_LAST;
            game.setAddFirst(amb);
        }
    }

    /** Start/stop computer thinking/analysis as appropriate. */
    private void updateComputeThreads() {
        boolean alive = game.tree.getGameState() == GameState.ALIVE;
        boolean computersTurn = !humansTurn() && alive;
        boolean ponder =  !computersTurn && (ponderMove != null) && alive;
        if (!(computersTurn || ponder))
            computerPlayer.stopSearch();
        listener.clearSearchInfo(searchId);
        if (!computerPlayer.sameSearchId(searchId)) {
             if (computersTurn || ponder) {
                listener.clearSearchInfo(searchId);
                final Pair<Position, ArrayList<Move>> ph = game.getUCIHistory();
                Position currPos = new Position(game.currPos());
                long now = System.currentTimeMillis();
                if (ponder)
                    game.timeController.advanceMove(1);
                int wTime = game.timeController.getRemainingTime(true, now);
                int bTime = game.timeController.getRemainingTime(false, now);
                int wInc = game.timeController.getIncrement(true);
                int bInc = game.timeController.getIncrement(false);
                boolean wtm = currPos.whiteMove;
                int movesToGo = game.timeController.getMovesToTC(wtm ^ ponder);
                if (ponder)
                    game.timeController.advanceMove(-1);
                final Move fPonderMove = ponder ? ponderMove : null;
                SearchRequest sr = SearchRequest.searchRequest(
                        searchId, now, ph.first, ph.second, currPos,
                        game.haveDrawOffer(),
                        wTime, bTime, wInc, bInc, movesToGo, fPonderMove,
                        engine, getEloToUse());
                computerPlayer.queueSearchRequest(sr);
            } else {
                computerPlayer.queueStartEngine(searchId, engine);
            }
        }
    }

    private synchronized void makeComputerMove(int id, final String cmd, final Move ponder) {
        if (searchId != id)
            return;
        searchId++;
        Position oldPos = new Position(game.currPos());
        Pair<Boolean,Move> res = game.processString(cmd);
        ponderMove = ponder;
        updateGameMode();
        gui.movePlayed(game.prevPos(), res.second, true);
        listener.clearSearchInfo(searchId);
        if (res.first) {
            updateComputeThreads();
            setSelection();
            setAnimMove(oldPos, game.getLastMove(), true);
            updateGUI();
        }
    }

    private void setPlayerNames(Game game) {
        if (game != null) {
            String engine = "Computer";
            if (computerPlayer != null) {
                engine = computerPlayer.getEngineName();
                int elo = getEloToUse();
                if (elo != Integer.MAX_VALUE)
                    engine += String.format(Locale.US, " (%d)", elo);
            }
            String player = gui.playerName();
            String white = gameMode.playerWhite() ? player : engine;
            String black = gameMode.playerBlack() ? player : engine;
            game.tree.setPlayerNames(white, black);
        }
    }

    private synchronized void updatePlayerNames(String engineName) {
        if (game != null) {
            int elo = getEloToUse();
            if (elo != Integer.MAX_VALUE)
                engineName += String.format(Locale.US, " (%d)", elo);
            String white = gameMode.playerWhite() ? game.tree.white : engineName;
            String black = gameMode.playerBlack() ? game.tree.black : engineName;
            game.tree.setPlayerNames(white, black);
            updateMoveList();
        }
    }

    private boolean undoMoveNoUpdate() {
        if (game.getLastMove() == null)
            return false;
        searchId++;
        game.undoMove();
        if (!humansTurn()) {
            if (game.getLastMove() != null) {
                game.undoMove();
                if (!humansTurn()) {
                    game.redoMove();
                }
            } else {
                // Don't undo first white move if playing black vs computer,
                // because that would cause computer to immediately make
                // a new move.
                if (gameMode.playerWhite() || gameMode.playerBlack()) {
                    game.redoMove();
                    return false;
                }
            }
        }
        return true;
    }

    private void redoMoveNoUpdate() {
        if (game.canRedoMove()) {
            searchId++;
            game.redoMove();
            if (!humansTurn() && game.canRedoMove()) {
                game.redoMove();
                if (!humansTurn())
                    game.undoMove();
            }
        }
    }

    /**
     * Move a piece from one square to another.
     * @return True if the move was legal, false otherwise.
     */
    private boolean doMove(Move move) {
        Position pos = game.currPos();
        ArrayList<Move> moves = new MoveGen().legalMoves(pos);
        int promoteTo = move.promoteTo;
        for (Move m : moves) {
            if ((m.from == move.from) && (m.to == move.to)) {
                if ((m.promoteTo != Piece.EMPTY) && (promoteTo == Piece.EMPTY)) {
                    promoteMove = m;
                    gui.requestPromotePiece();
                    return false;
                }
                if (m.promoteTo == promoteTo) {
                    String strMove = TextIO.moveToString(pos, m, false, false, moves);
                    Pair<Boolean,Move> res = game.processString(strMove);
                    gui.movePlayed(game.prevPos(), res.second, false);
                    return true;
                }
            }
        }
        gui.reportInvalidMove(move);
        return false;
    }

    private void updateGUI() {
        GUIInterface.GameStatus s = new GUIInterface.GameStatus();
        s.state = game.getGameState();
        if (s.state == GameState.ALIVE) {
            s.moveNr = game.currPos().fullMoveCounter;
            s.white = game.currPos().whiteMove;
            SearchType st = SearchType.NONE;
            if (computerPlayer != null)
                st = computerPlayer.getSearchType();
            switch (st) {
            case SEARCH:  s.thinking  = true; break;
            case PONDER:  s.ponder    = true; break;
            case ANALYZE: s.analyzing = true; break;
            case NONE: break;
            }
        } else {
            if ((s.state == GameState.DRAW_REP) || (s.state == GameState.DRAW_50))
                s.drawInfo = game.getDrawInfo(localPt());
        }
        gui.setStatus(s);
        updateMoveList();

        StringBuilder sb = new StringBuilder();
        if (game.tree.currentNode != game.tree.rootNode) {
            game.tree.goBack();
            Position pos = game.currPos();
            List<Move> prevVarList = game.tree.variations();
            for (int i = 0; i < prevVarList.size(); i++) {
                if (i > 0) sb.append(' ');
                if (i == game.tree.currentNode.defaultChild)
                    sb.append(Util.boldStart);
                sb.append(TextIO.moveToString(pos, prevVarList.get(i), false, localPt()));
                if (i == game.tree.currentNode.defaultChild)
                    sb.append(Util.boldStop);
            }
            game.tree.goForward(-1);
        }
        gui.setPosition(game.currPos(), sb.toString(), game.tree.variations());

        updateRemainingTime();
        updateMaterialDiffList();
        gui.updateTimeControlTitle();
    }

    public final void updateMaterialDiffList() {
        gui.updateMaterialDifferenceTitle(Util.getMaterialDiff(game.currPos()));
    }
    private void updateMoveList() {
        if (game == null)
            return;
        if (!gameTextListener.isUpToDate()) {
            PGNOptions tmpOptions = new PGNOptions();
            tmpOptions.exp.variations     = pgnOptions.view.variations;
            tmpOptions.exp.comments       = pgnOptions.view.comments;
            tmpOptions.exp.nag            = pgnOptions.view.nag;
            tmpOptions.exp.playerAction   = false;
            tmpOptions.exp.clockInfo      = false;
            tmpOptions.exp.moveNrAfterNag = false;
            tmpOptions.exp.pieceType      = pgnOptions.view.pieceType;
            gameTextListener.clear();
            game.tree.pgnTreeWalker(tmpOptions, gameTextListener);
        }
        gameTextListener.setCurrent(game.tree.currentNode);
        gui.moveListUpdated();
    }

    /** Mark last played move in the GUI. */
    private void setSelection() {
        Move m = game.getLastMove();
        int sq = ((m != null) && (m.from != m.to)) ? m.to : -1;
        gui.setSelection(sq);
    }

    private void setAnimMove(Position sourcePos, Move move, boolean forward) {
        gui.setAnimMove(sourcePos, move, forward);
    }

    private boolean findValidDrawClaim(String ms) {
        if (!ms.isEmpty())
            ms = " " + ms;
        if (game.getGameState() != GameState.ALIVE) return true;
        game.tryClaimDraw("draw accept");
        if (game.getGameState() != GameState.ALIVE) return true;
        game.tryClaimDraw("draw rep" + ms);
        if (game.getGameState() != GameState.ALIVE) return true;
        game.tryClaimDraw("draw 50" + ms);
        if (game.getGameState() != GameState.ALIVE) return true;
        return false;
    }
}
