package com.example.chessplay.pieces;

import com.example.chessplay.Base;
import com.example.chessplay.Board;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class Piece {
    private final boolean isWhite;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public boolean isWhite() {
        return this.isWhite;
    }

    public abstract Set<Base> getAllowedCells(Base base, Board board);

    public Set<Base> filterAllowedMoves(Base baseOfPiece, Board board) throws CloneNotSupportedException{
        Set<Base> filteredMoves = new HashSet<>();
        Set<Base> allowedMoves = this.getAllowedCells(baseOfPiece, board);
        for (Base base : allowedMoves) {
            Board newBoard = board.clone();
            Base newBase = newBoard.getData()[baseOfPiece.getY()][baseOfPiece.getX()];
            Set<Base> newAllowedMoves = newBase.getPiece().getAllowedCells(newBase, newBoard);
            newBoard.setCurrentCell(newBase);
            newBoard.setAllowedMoves(newAllowedMoves);
            newBoard.putPiece(newBoard.getData()[base.getY()][base.getX()]);
            if(!newBoard.isCheck(this.isWhite())) filteredMoves.add(base);
        }
        return filteredMoves;
    }

    public abstract String getName();


    @Override
    public Piece clone() {
        try {
            return (Piece) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        String pieceColor = isWhite ? "white" : "black";
        return pieceColor + "_" + getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piece)) return false;
        Piece piece = (Piece) o;
        return this.toString().equals(piece.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isWhite) + Objects.hash(getName());
    }
}
