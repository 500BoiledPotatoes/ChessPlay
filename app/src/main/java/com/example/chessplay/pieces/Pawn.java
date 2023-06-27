package com.example.chessplay.pieces;

import com.example.chessplay.Base;
import com.example.chessplay.Board;

import java.util.HashSet;
import java.util.Set;

public class Pawn extends Piece {

    public final int BORDER_COORDINATE = this.isWhite() ? 7 : 0;
    private boolean isMoved = false;
    public boolean isPassantAvailable = false;

    public Pawn(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public Set<Base> getAllowedCells(Base base, Board boardClass) {
        int x = base.getX();
        int y = base.getY();
        Set<Base> res = new HashSet<>();

        Base[][] board = boardClass.getData();

        int i = this.isWhite() ? 1 : -1;
        int curY = y + i;
        if (curY >= 0 && curY < 8 && board[curY][x].getPiece() == null) {
            res.add(new Base(x, curY));
            if (!this.isMoved && board[curY + i][x].getPiece() == null)
                res.add(new Base(x, curY + i));
        }
        // check if Pawn can catch opponent piece from RIGHT cell
        int curX = x + 1;
        if (curY >= 0 && curY < 8  && curX < 8) {
            Piece oppositePiece = board[curY][curX].getPiece();
            if (oppositePiece != null) {
                if (oppositePiece.isWhite() != this.isWhite()) res.add(new Base(curX, curY));
            } else {
                Piece checkingPiece = board[y][curX].getPiece();
                if (checkingPiece != null && checkingPiece.getName().equals("Pawn")) {
                    Pawn checkingPawn = (Pawn) checkingPiece;
                    if (checkingPawn.isPassantAvailable && this.isWhite() != checkingPawn.isWhite())
                        res.add(new Base(curX, curY));
                }
            }


        }
        // check if Pawn can catch opponent piece from LEFT cell
        curX = x - 1;
        if (curY >= 0 && curY < 8 && curX >= 0) {
            Piece oppositePiece = board[curY][curX].getPiece();
            if (oppositePiece != null) {
                if (oppositePiece.isWhite() != this.isWhite()) res.add(new Base(curX, curY));
            } else {
                Piece checkingPiece = board[y][curX].getPiece();
                if (checkingPiece != null && checkingPiece.getName().equals("Pawn")) {
                    Pawn checkingPawn = (Pawn) checkingPiece;
                    if (checkingPawn.isPassantAvailable && this.isWhite() != checkingPawn.isWhite())
                        res.add(new Base(curX, curY));
                }
            }

        }
        return res;
    }

    public void setMoved() {
        isMoved = true;
    }

    @Override
    public Piece clone() {
        Pawn newPawn = new Pawn(this.isWhite());
        newPawn.isMoved = this.isMoved;
        newPawn.isPassantAvailable = this.isPassantAvailable;
        return newPawn;
    }
    @Override
    public String getName() {
        return "Pawn";
    }
}
