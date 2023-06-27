package com.example.chessplay.pieces;

import com.example.chessplay.Base;
import com.example.chessplay.Board;

import java.util.HashSet;
import java.util.Set;

public class Knight extends Piece {
    public Knight(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public Set<Base> getAllowedCells(Base base, Board boardClass) {
        int x = base.getX();
        int y = base.getY();
        Set<Base> res = new HashSet<>();

        Base[][] board = boardClass.getData();

        for (int i = 1; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                int curX = (int) (x + i * Math.pow(-1.0, j));
                int curY = y + i - 3;
                if (coordinatesAllow(curX, curY, board)) res.add(new Base(curX, curY));
                curY = y - i + 3;
                if (coordinatesAllow(curX, curY, board)) res.add(new Base(curX, curY));
            }
        }

        return res;
    }

    @Override
    public String getName() {
        return "Knight";
    }

    @Override
    public Piece clone() {
        return new Knight(isWhite());
    }

    private boolean coordinatesAllow(int x, int y, Base[][] board) {
        return x >= 0 && x <= 7 && y >= 0 && y <= 7
                && (board[y][x].getPiece() == null
                || board[y][x].getPiece().isWhite() != this.isWhite());
    }
}
