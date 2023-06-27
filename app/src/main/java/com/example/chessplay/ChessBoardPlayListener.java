
package com.example.chessplay;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.chessplay.gamelogic.Move;
import com.example.chessplay.gamelogic.Piece;

public class ChessBoardPlayListener implements View.OnTouchListener {
    private ChessPlay cp;
    private ChessBoardPlay cb;

    private boolean pending = false;
    private boolean pendingClick = false;
    private int sq0 = -1;
    private boolean isValidDragSquare; // True if dragging starting at "sq0" is valid
    private int dragSquare = -1;
    private float scrollX = 0;
    private float scrollY = 0;
    private float prevX = 0;
    private float prevY = 0;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {
            pending = false;
            handler.removeCallbacks(runnable);
            cp.reShowDialog(ChessPlay.BOARD_MENU_DIALOG);
        }
    };

    ChessBoardPlayListener(ChessPlay cp, ChessBoardPlay cb) {
        this.cp = cp;
        this.cb = cb;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            handler.postDelayed(runnable, ViewConfiguration.getLongPressTimeout());
            pending = true;
            pendingClick = true;
            sq0 = cb.eventToSquare(event);
            isValidDragSquare = cb.isValidDragSquare(sq0);
            dragSquare = -1;
            scrollX = 0;
            scrollY = 0;
            prevX = event.getX();
            prevY = event.getY();
            break;
        case MotionEvent.ACTION_MOVE:
            if (pending) {
                int sq = cb.eventToSquare(event);
                if (sq != sq0) {
                    handler.removeCallbacks(runnable);
                    pendingClick = false;
                }
                float currX = event.getX();
                float currY = event.getY();
                if (onMove(event)) {
                    handler.removeCallbacks(runnable);
                    pendingClick = false;
                }
                prevX = currX;
                prevY = currY;
            }
            break;
        case MotionEvent.ACTION_UP:
            if (pending) {
                pending = false;
                handler.removeCallbacks(runnable);
                if (cp.ctrl.humansTurn()) {
                    int sq = cb.eventToSquare(event);
                    if (dragSquare != -1) {
                        if (sq != -1 && sq != sq0) {
                            cb.setSelection(cb.highlightLastMove ? sq : -1);
                            cb.userSelectedSquare = false;
                            Move m = new Move(sq0, sq, Piece.EMPTY);
                            cp.ctrl.makeHumanMove(m, false);
                        }
                    } else if (pendingClick && (sq == sq0)) {
                        Move m = cb.mousePressed(sq);
                        if (m != null) {
                            cp.ctrl.makeHumanMove(m, true);
                        }
                    }
                }
                cb.setDragState(-1, 0, 0);
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            pending = false;
            cb.setDragState(-1, 0, 0);
            handler.removeCallbacks(runnable);
            break;
        }
        return true;
    }

    /** Process an ACTION_MOVE event. Return true if a gesture is detected,
     *  which means that a click will not happen when ACTION_UP is received. */
    private boolean onMove(MotionEvent event) {
        if (cp.dragMoveEnabled && isValidDragSquare) {
            return onDrag(event);
        } else {
            return onScroll(event.getX() - prevX, event.getY() - prevY);
        }
    }

    private boolean onDrag(MotionEvent event) {
        if (dragSquare == -1) {
            int sq = cb.eventToSquare(event);
            if (sq != sq0)
                dragSquare = sq0;
        }
        if (dragSquare != -1)
            if (!cb.setDragState(dragSquare, (int)event.getX(), (int)event.getY()))
                dragSquare = -1;
        return false;
    }

    private boolean onScroll(float distanceX, float distanceY) {
        if (cp.invertScrollDirection) {
            distanceX = -distanceX;
            distanceY = -distanceY;
        }
        if ((cp.scrollSensitivity > 0) && (cb.sqSize > 0)) {
            scrollX += distanceX;
            scrollY += distanceY;
            final float scrollUnit = cb.sqSize * cp.scrollSensitivity;
            if (Math.abs(scrollX) >= Math.abs(scrollY)) {
                // Undo/redo
                int nRedo = 0, nUndo = 0;
                while (scrollX > scrollUnit) {
                    nRedo++;
                    scrollX -= scrollUnit;
                }
                while (scrollX < -scrollUnit) {
                    nUndo++;
                    scrollX += scrollUnit;
                }
                if (nRedo + nUndo > 1) {
                    boolean human = cp.gameMode.playerWhite() || cp.gameMode.playerBlack();
                    if (!human)
                        cp.ctrl.setGameMode(new GameMode(GameMode.TWO_PLAYERS));
                }
                if (cp.scrollGames) {
                } else {
                    for (int i = 0; i < nRedo; i++) cp.ctrl.redoMove();
                    for (int i = 0; i < nUndo; i++) cp.ctrl.undoMove();
                }
                cp.ctrl.setGameMode(cp.gameMode);
                return nRedo + nUndo > 0;
            } else {
                // Next/previous variation
                int varDelta = 0;
                while (scrollY > scrollUnit) {
                    varDelta++;
                    scrollY -= scrollUnit;
                }
                while (scrollY < -scrollUnit) {
                    varDelta--;
                    scrollY += scrollUnit;
                }
                if (varDelta != 0) {
                    scrollX = 0;
                    cp.ctrl.changeVariation(varDelta);
                }
                return varDelta != 0;
            }
        }
        return false;
    }
}
