package com.example.chessplay.pieces;

import com.example.chessplay.Base;
import com.example.chessplay.Board;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class King extends Piece {

    private boolean isMoved = false;

    public King(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public Set<Base> getAllowedCells(Base base, Board boardClass) {
        int x = base.getX();
        int y = base.getY();
        Set<Base> res = new HashSet<>();

        Base[][] board = boardClass.getData();

        List<Integer> listOfIterator = new ArrayList<>();
        listOfIterator.add(-1);
        listOfIterator.add(0);
        listOfIterator.add(1);

        for (int iteratorX : listOfIterator) {
            for (int iteratorY : listOfIterator) {
                if (iteratorX == 0 && iteratorY == 0) continue;
                int curX = x + iteratorX;
                int curY = y + iteratorY;
                if (curX >= 0 && curX <= 7 && curY >= 0 && curY <= 7
                        && (board[curY][curX].getPiece() == null
                        || board[curY][curX].getPiece().isWhite() != this.isWhite()))
                    res.add(new Base(curX, curY));
            }
        }

        return res;
    }

    @Override
    public Set<Base> filterAllowedMoves(Base baseOfPiece, Board board) throws CloneNotSupportedException {

        Set<Base> res = new HashSet<>(super.filterAllowedMoves(baseOfPiece, board));
        int y = isWhite() ? 0 : 7;
        int x = baseOfPiece.getX();
        List<Integer> listOfIterator = new ArrayList<>();
        listOfIterator.add(-1);
        listOfIterator.add(1);
        if (!this.isMoved) {
            boolean areAllMovesGood;
            int curX;
            for (int iterator : listOfIterator) {
                curX = x + iterator;
                while (curX >= 0 && curX < 8) {
                    if (curX == 0 || curX == 7) {
                        Piece checkingPiece = board.getData()[y][curX].getPiece();
                        if (checkingPiece != null && checkingPiece.getName().equals("Rook")) {
                            Rook rook = (Rook) board.getData()[y][curX].getPiece();
                            if (!rook.isMoved()) {
                                areAllMovesGood = true;
                                Set<Base> moves = new HashSet<>();
                                moves.add(board.getData()[y][x + iterator]);
                                moves.add(board.getData()[y][x + 2 * iterator]);
                                for (Base base : moves) {
                                    Board newBoard = board.clone();
                                    Base newBase = newBoard.getData()[baseOfPiece.getY()][baseOfPiece.getX()];
                                    Set<Base> newAllowedMoves = new HashSet<>();
                                    newAllowedMoves.add(newBoard.getData()[y][x + iterator]);
                                    newAllowedMoves.add(newBoard.getData()[y][x + 2 * iterator]);
                                    newBoard.setCurrentCell(newBase);
                                    newBoard.setAllowedMoves(newAllowedMoves);
                                    newBoard.putPiece(newBoard.getData()[base.getY()][base.getX()]);
                                    areAllMovesGood = areAllMovesGood & !newBoard.isCheck(this.isWhite());
                                }
                                if (areAllMovesGood) res.add(board.getData()[y][curX]);
                                areAllMovesGood = false;
                            }
                        }
                    } else if (board.getData()[y][curX].getPiece() != null) break;
                    curX = curX + iterator;
                }
            }
        }
        return res;
    }

    @Override
    public String getName() {
        return "King";
    }

    @Override
    public Piece clone() {
        return new King(isWhite());
    }


    public void setMoved() {
        isMoved = true;
    }
}
