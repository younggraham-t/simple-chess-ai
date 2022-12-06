package chess_ai.ai_board;

import piece.Coordinate;

/**
 * this class was originally written by SebLague in C# and converted to Java by
 * Graham Young
 * 
 * original file: https://github.com/SebLague/Chess-AI/blob/main/Assets/Scripts/Core/Piece.cs
 * 
 * 
 * @author SebLague, Graham Young
 *
 */
 /* 
To preserve memory during search, moves are stored as 16 bit numbers.
The format is as follows:
bit 0-5: from square (0 to 63)
bit 6-11: to square (0 to 63)
bit 12-15: flag
*/

public class Move {

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

	final short moveValue;

	final short startSquareMask = (short) 0b0000000000111111;
	final short targetSquareMask = (short) 0b0000111111000000;
	final short flagMask = (short) 0b1111000000000000;

	public Move(short moveValue) {
		this.moveValue = moveValue;
	}

	public Move(int startSquare, int targetSquare) {
		moveValue = (short) (startSquare | targetSquare << 6);
	}

	public Move(int startSquare, int targetSquare, int flag) {
		moveValue = (short) (startSquare | targetSquare << 6 | flag << 12);
	}

	public int getStartSquare() {

		return moveValue & startSquareMask;

	}

	public int getTargetSquare() {

		return (moveValue & targetSquareMask) >> 6;

	}

	public boolean isPromotion() {

		int flag = getMoveFlag();
		return flag == Flag.PromoteToQueen || flag == Flag.PromoteToRook || flag == Flag.PromoteToKnight
				|| flag == Flag.PromoteToBishop;

	}

	public int getMoveFlag() {

		return moveValue >> 12;

	}

	public int getPromotionPieceType() {
		

		switch (getMoveFlag()) {
		case Flag.PromoteToRook:
			return Piece.Rook;
		case Flag.PromoteToKnight:
			return Piece.Knight;
		case Flag.PromoteToBishop:
			return Piece.Bishop;
		case Flag.PromoteToQueen:
			return Piece.Queen;
		default:
			return Piece.None;
		}

	}
	
	

	public static Move getInvalidMove() {

		return new Move((short) 0);

	}

	public static boolean isSameMove(Move a, Move b) {
		return a.moveValue == b.moveValue;
	}

	public short getValue() {

		return moveValue;

	}
	
	public chessboard.Move getChessBoardMove() {
		if(this.isInvalid()) {
			return chessboard.Move.getInvalidMove();
		}
		
		
		return new chessboard.Move(this);
	}

	public boolean isInvalid() {

		return moveValue == 0;

	}

	public String toString() {

		return BoardRepresentation.SquareNameFromIndex(getStartSquare()) + "-"
				+ BoardRepresentation.SquareNameFromIndex(getTargetSquare()) 
				+ " " + getPromotionPieceType();

	}
}
