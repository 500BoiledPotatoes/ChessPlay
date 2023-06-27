package com.example.chessplay;

import com.example.chessplay.pieces.Bishop;
import com.example.chessplay.pieces.King;
import com.example.chessplay.pieces.Knight;
import com.example.chessplay.pieces.Pawn;
import com.example.chessplay.pieces.Piece;
import com.example.chessplay.pieces.Queen;
import com.example.chessplay.pieces.Rook;

import java.util.HashSet;
import java.util.Set;

public class Board {
    private final Base[][] board = new Base[8][8];
    private Base currentBase = null;
    private Set<Base> allowedMoves = null;
    private Base promotedBase = null;


    public Board() {
        Piece[] whitePieces = new Piece[]{
                new Rook(true), new Knight(true), new Bishop(true),
                new Queen(true), new King(true), new Bishop(true),
                new Knight(true), new Rook(true), new Pawn(true)
        };
        Piece[] blackPieces = new Piece[]{
                new Rook(false), new Knight(false), new Bishop(false),
                new Queen(false), new King(false), new Bishop(false),
                new Knight(false), new Rook(false), new Pawn(false)
        };

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                Piece currentPiece;
                switch (y) {
                    case 0:
                        currentPiece = whitePieces[x];
                        break;
                    case 1:
                        currentPiece = whitePieces[8].clone();
                        break;
                    case 6:
                        currentPiece = blackPieces[8].clone();
                        break;
                    case 7:
                        currentPiece = blackPieces[x];
                        break;
                    default:
                        currentPiece = null;
                }
                board[y][x] = new Base(x, y, currentPiece);
            }
        }
    }

    public Board(Base[][] array) {
        for (int i = 0; i < 8; i++) {
            System.arraycopy(array[i], 0, board[i], 0, 8);
        }
    }

    public Base[][] getData() {
        return board;
    }

    public Base getPromotedCell() {
        return promotedBase;
    }

    public void setCurrentCell(Base base) {
        this.currentBase = base;
    }

    public void setAllowedMoves(Set<Base> set) {
        this.allowedMoves = set;
    }

    public Set<Base> capturePiece(Base base) {
        setCurrentCell(base);
        if (base.getPiece() == null) return new HashSet<>();
        Piece capturePiece = base.getPiece();
        Set<Base> allowedMoves = null;
        try {
            allowedMoves = capturePiece.filterAllowedMoves(base, this);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        setAllowedMoves(allowedMoves);
        return allowedMoves;
    }

    public Set<Base> putPiece(Base base) {

        if (!allowedMoves.contains(base)) return null;

        String curPieceName = "";
        if (currentBase.getPiece() != null) curPieceName = currentBase.getPiece().getName();

        Set<Base> res = new HashSet<>();
        res.add(currentBase);
        res.add(base);
        //Pawns moves
        Set<Base> setOfPawns = findAllPawns();
        for (Base pawnBase : setOfPawns) {
            Pawn pawn = (Pawn) pawnBase.getPiece();
            pawn.isPassantAvailable = false;
        }
        if (curPieceName.equals("Pawn")) {
            Pawn curPawn = (Pawn) currentBase.getPiece();
            curPawn.setMoved();
            if (Math.abs(currentBase.getY() - base.getY()) == 2) {
                curPawn.isPassantAvailable = true;
            }
            if (currentBase.getX() != base.getX() && base.getPiece() == null) {
                int i = currentBase.getPiece().isWhite() ? -1 : 1;
                Base attackedBase = board[base.getY() + i][base.getX()];
                res.add(attackedBase);
                attackedBase.removePiece();
            }
            if(base.getY() == curPawn.BORDER_COORDINATE) promotedBase = base;
        }
        if (curPieceName.equals("King")) {
            King curKing = (King) currentBase.getPiece();
            curKing.setMoved();
        }
        if (curPieceName.equals("Rook")) {
            Rook curKing = (Rook) currentBase.getPiece();
            curKing.setMoved();
        }

        if (curPieceName.equals("King") && base.getPiece() != null &&
                base.getPiece().isWhite() == currentBase.getPiece().isWhite()) {
            Piece capturedPiece = currentBase.getPiece();
            currentBase.removePiece();
            Piece swappedRook = base.getPiece();
            base.removePiece();
            int i = base.getX() == 0 ? -1 : 1;
            int y = capturedPiece.isWhite() ? 0 : 7;
            Base newKingBase = board[y][currentBase.getX() + 2 * i];
            Base newRookBase = board[y][currentBase.getX() + i];
            newKingBase.setPiece(capturedPiece);
            newRookBase.setPiece(swappedRook);
            res.add(newKingBase);
            res.add(newRookBase);
        } else {
            //Standard method
            Piece capturedPiece = currentBase.getPiece();
            currentBase.removePiece();
            base.setPiece(capturedPiece);
        }
        return res;
    }

    public boolean isCheckmate(boolean isPlayerWhite) {
        if (isCheck(isPlayerWhite)) {
            Set<Base> allowedMoves = new HashSet<>();
            Set<Base> friendPieceBases = setOfCellsWithPiecesOneColor(isPlayerWhite);
            for (Base base : friendPieceBases) {
                try {
                    allowedMoves.addAll(base.getPiece().filterAllowedMoves(base, this));
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
            return allowedMoves.isEmpty();
        }
        return false;
    }

    public boolean isCheck(boolean isPlayerWhite) {
        Base kingBase = this.findKingCell(isPlayerWhite);

        for (Base[] column : board) {
            for (Base base : column) {
                if (base.getPiece() != null) {
                    Set<Base> allowedMoves = findAllOpponentsMoves(!isPlayerWhite);
                    if (allowedMoves.contains(kingBase)) return true;
                }


            }
        }
        return false;
    }

    private Base findKingCell(boolean isPlayerWhite) {
        Set<Base> pieces = this.setOfCellsWithPiecesOneColor(isPlayerWhite);
        for (Base base : pieces) {
            if (base.getPiece().getName().equals("King")) return base;
        }

        return null;
    }

    public void makePromotion(Piece promotingPiece){
        promotedBase.setPiece(promotingPiece);
        promotedBase = null;
    }

    private Set<Base> setOfCellsWithPiecesOneColor(boolean isPlayerWhite) {
        Set<Base> res = new HashSet<>();

        for (Base[] column : board) {
            for (Base base : column) {
                Piece piece = base.getPiece();
                if (piece != null && piece.isWhite() == isPlayerWhite) res.add(base);
            }
        }
        return res;
    }

    private Set<Base> findAllOpponentsMoves(boolean isOpponentWhite) {
        Set<Base> setOfOpponentsBases = setOfCellsWithPiecesOneColor(isOpponentWhite);
        Set<Base> res = new HashSet<>();
        for (Base base : setOfOpponentsBases) {
            res.addAll(base.getPiece().getAllowedCells(base, this));
        }
        return res;
    }

    private Set<Base> findAllPawns() {
        Set<Base> setOfPiece = setOfCellsWithPiecesOneColor(true);
        setOfPiece.addAll(setOfCellsWithPiecesOneColor(false));
        Set<Base> res = new HashSet<>();
        for (Base base : setOfPiece) {
            if (base.getPiece().getName().equals("Pawn")) res.add(base);
        }
        return res;
    }

    @Override
    public Board clone() throws CloneNotSupportedException {
        Base[][] curBoardData = this.getData();
        Base[][] cloneBoardData = new Base[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                cloneBoardData[i][j] = curBoardData[i][j].clone();
            }
        }
        return new Board(cloneBoardData);
    }

    public void setPromotedCell(Base promotedBase) {
        this.promotedBase = promotedBase;
    }
}
