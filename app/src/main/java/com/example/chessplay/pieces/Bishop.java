package com.example.chessplay.pieces;

import com.example.chessplay.Board;
import com.example.chessplay.Base;

import java.util.HashSet;
import java.util.Set;

public class Bishop extends Piece {

    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public Set<Base> getAllowedCells(Base base, Board boardClass) {
        int x = base.getX();
        int y = base.getY();
        Set<Base> res = new HashSet<>();

        Base[][] board = boardClass.getData();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                int curX = (int) (x + Math.pow(-1.0, i));
                int curY = (int) (y + Math.pow(-1.0, j));
                while (curX >= 0 && curX < 8 && curY >= 0 && curY < 8) {
                    Piece curPiece = board[curY][curX].getPiece();
                    if (curPiece == null) res.add(new Base(curX, curY));
                    else {
                        if (curPiece.isWhite() != this.isWhite()) res.add(new Base(curX, curY));
                        break;
                    }
                    curX = (int) (curX + Math.pow(-1.0, i));
                    curY = (int) (curY + Math.pow(-1.0, j));
                }
            }

        }

        return res;
    }

    @Override
    public Piece clone() {
        return new Bishop(isWhite());
    }

    @Override
    public String getName() {
        return "Bishop";
    }
}
