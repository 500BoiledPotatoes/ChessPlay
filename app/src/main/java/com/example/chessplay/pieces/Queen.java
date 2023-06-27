package com.example.chessplay.pieces;

import com.example.chessplay.Board;
import com.example.chessplay.Base;

import java.util.HashSet;
import java.util.Set;

public class Queen extends Piece{
    public Queen(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public Set<Base> getAllowedCells(Base base, Board boardClass) {
        int x = base.getX();
        int y = base.getY();
        Set<Base> res = new HashSet<>();

        Base[][] board = boardClass.getData();

        res.addAll(new Rook(this.isWhite()).getAllowedCells(base,boardClass));
        res.addAll(new Bishop(this.isWhite()).getAllowedCells(base,boardClass));
        return res;
    }
    @Override
    public Piece clone() {
        return new Queen(isWhite());
    }
    @Override
    public String getName() {
        return "Queen";
    }
}
