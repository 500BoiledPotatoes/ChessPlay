package com.example.chessplay.pieces;

import com.example.chessplay.Base;
import com.example.chessplay.Board;

import java.util.HashSet;
import java.util.Set;

public class Rook extends Piece {

    private boolean isMoved = false;

    public Rook(boolean isWhie) {
        super(isWhie);
    }

    @Override
    public Set<Base> getAllowedCells(Base base, Board boardClass) {

        int x = base.getX();
        int y = base.getY();

        Set<Base> allowedMoves = new HashSet<>();

        Base[][] board = boardClass.getData();

        for (int i = x + 1; i < 8; i++) {
            Piece curPiece = board[y][i].getPiece();
            if (curPiece == null)
                allowedMoves.add(new Base(i, y));
            else {
                if (curPiece.isWhite() != this.isWhite()) allowedMoves.add(new Base(i, y));
                break;
            }
        }


        for (int i = x - 1; i >= 0; i--) {
            Piece curPiece = board[y][i].getPiece();
            if (curPiece == null) allowedMoves.add(new Base(i, y));
            else {
                if (curPiece.isWhite() != this.isWhite()) allowedMoves.add(new Base(i, y));
                break;
            }
        }


        for (int i = y + 1; i < 8; i++) {
            Piece curPiece = board[i][x].getPiece();
            if (curPiece == null) allowedMoves.add(new Base(x, i));
            else {
                if (curPiece.isWhite() != this.isWhite()) allowedMoves.add(new Base(x, i));
                break;
            }
        }


        for (int i = y - 1; i >= 0; i--) {
            Piece curPiece = board[i][x].getPiece();
            if (curPiece == null) allowedMoves.add(new Base(x, i));
            else {
                if (curPiece.isWhite() != this.isWhite()) allowedMoves.add(new Base(x, i));
                break;
            }
        }

        return allowedMoves;
    }


    @Override
    public String getName() {
        return "Rook";
    }

    public boolean isMoved() {
        return isMoved;
    }

    @Override
    public Piece clone() {
        return new Rook(isWhite());
    }

    public void setMoved() {
        isMoved = true;
    }
}
