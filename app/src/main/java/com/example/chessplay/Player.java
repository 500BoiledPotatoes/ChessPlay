package com.example.chessplay;

import java.util.Set;

public class Player {
    private boolean isWhite;
    private Board board = new Board();
    private Base currentBase = null;
    private boolean isWhitesTurn = true;


    public Player(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public Board getBoard() {
        return this.board;
    }

    public Set<Base> capturePiece(Base base) {
        return board.capturePiece(base);
    }

    public Set<Base> putPiece(Base base) {
        return board.putPiece(base);
    }

    public void setCurrentCell(Base currentBase) {
        this.currentBase = currentBase;
        board.setCurrentCell(currentBase);
    }

    public Base getCurrentCell() {
        return currentBase;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setColor(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public boolean isWhitesTurn() {
        return isWhitesTurn;
    }

    public boolean isThisPlayersTurn() {
        return isWhitesTurn == isWhite;
    }

    public void changeTurn() {
        isWhitesTurn = !isWhitesTurn;
    }

    public void setWhitesTurn(boolean whitesTurn) {
        this.isWhitesTurn = whitesTurn;
    }

    public boolean isCheck(boolean isWhite) {
        return board.isCheck(isWhite);
    }


    public boolean isCheckmate(boolean isWhite) {
        return board.isCheckmate(isWhite);
    }

}

