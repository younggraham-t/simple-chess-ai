package chessboard;


import java.util.Objects;

import chess_ai.ai_board.BoardRepresentation;
import chess_ai.ai_board.Move.Flag;
import piece.Coordinate;
import piece.Piece;

public class Move implements Comparable<Move>{
	
	public class Flag {
		public static final int None = 0;
		public static final int EnPassantCapture = 1;
		public static final int Castling = 2;
		public static final int PromoteToQueen = 3;
		public static final int PromoteToKnight = 4;
		public static final int PromoteToRook = 5;
		public static final int PromoteToBishop = 6;
		public static final int PawnTwoForward = 7;
	}
	
	public static final String PROMOTE_TO_NOTHING = "";
	public static final String PROMOTE_TO_BISHOP = "bishop";
	public static final String PROMOTE_TO_ROOK = "rook";
	public static final String PROMOTE_TO_Knight = "knight";
	public static final String PROMOTE_TO_Queen = "queen";
	
	private Coordinate piecetoMove;
	private Coordinate squareToMoveTo;

	private int promotionPieceFlag;
	

	public Move(Coordinate piece, Coordinate moveTo) {
		this(piece, moveTo, chess_ai.ai_board.Piece.None);
	}
	
	public Move(Coordinate piece, Coordinate moveTo, int promotionPiece) {
		this.piecetoMove = piece;
		this.squareToMoveTo = moveTo;
		this.promotionPieceFlag = promotionPiece;
	}
	
	public Move(chess_ai.ai_board.Move move) {
		String squareNameMoveFrom = BoardRepresentation.SquareNameFromIndex(move.getStartSquare());
		String squareNameMoveTo = BoardRepresentation.SquareNameFromIndex(move.getTargetSquare());
		piecetoMove = new Coordinate(squareNameMoveFrom);
		squareToMoveTo = new Coordinate(squareNameMoveTo);
		//System.out.println(move.getMoveFlag());
		promotionPieceFlag = move.getMoveFlag();
		
	}
	

	
public int getPromotionPieceType() {
		

		switch (promotionPieceFlag) {
		case Flag.PromoteToRook:
			return chess_ai.ai_board.Piece.Rook;
		case Flag.PromoteToKnight:
			return chess_ai.ai_board.Piece.Knight;
		case Flag.PromoteToBishop:
			return chess_ai.ai_board.Piece.Bishop;
		case Flag.PromoteToQueen:
			return chess_ai.ai_board.Piece.Queen;
		default:
			return chess_ai.ai_board.Piece.None;
		}

	}
	
	public Coordinate getPiece() {
		return this.piecetoMove;
	}
	
	@Override
	public String toString() {
		return "" + piecetoMove + squareToMoveTo + getPromotionPieceType();
	}

	public Piece getPiece(ChessBoard board) {
		return board.getPieceAtCoordinate(piecetoMove);
	}
	
	public Coordinate getMoveTo() {
		return this.squareToMoveTo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(piecetoMove, squareToMoveTo);
	}
	
	public static Move getInvalidMove() {
		return new Move(new Coordinate(-1,-1), new Coordinate(-1,-1));
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Move other = (Move) obj;
		return piecetoMove.equals(other.piecetoMove) && squareToMoveTo.equals(other.squareToMoveTo);
	}


	@Override
	public int compareTo(Move o) {
		if (this.getPiece().compareTo(o.getPiece()) == 0) {
			return this.getMoveTo().compareTo(o.getMoveTo());
		}
		return this.getPiece().compareTo(o.getPiece());

	}


}
