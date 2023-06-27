package com.example.chessplay;

import com.example.chessplay.pieces.Piece;

public class Base {
    private final int x;
    private final int y;
    private Piece piece;

    public Base(int x, int y, Piece piece){
        this.x = x;
        this.y = y;
        this.piece = piece;
    }

    public Base(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Piece getPiece(){
        return this.piece;
    }

    public void setPiece(Piece piece){
        this.piece = piece;
    }

    public void removePiece() {
        this.piece = null;
    }

    @Override
    protected Base clone() throws CloneNotSupportedException {

        Piece clonePiece = this.getPiece() == null ? null : this.getPiece().clone();
        return new Base(this.getX(), this.getY(),clonePiece );
    }

    @Override
    public String toString() {
        return "Cell{" +
                "coordinates=" + "(" + x + " ; " + y +
                ") piece=" + piece +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Base base = (Base) o;
        return (this.x == base.getX() && this.y == base.getY());
    }

    @Override
    public int hashCode() {
        return x + 10 * y;
    }
}
