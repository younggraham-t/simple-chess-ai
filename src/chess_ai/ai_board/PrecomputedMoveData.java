package chess_ai.ai_board;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
/**
 * this class was originally written by SebLague in C# and converted to Java by
 * Graham Young

 * all other methods were written by Graham Young
 * 
 * original file: https://github.com/SebLague/Chess-AI/blob/main/Assets/Scripts/Core/Piece.cs
 * 
 * 
 * @author SebLague, Graham Young
 *
 */
public class PrecomputedMoveData {
	// First 4 are orthogonal, last 4 are diagonals (N, S, W, E, NW, SE, NE, SW)
	public static final int[] directionOffsets = { 8, -8, -1, 1, 7, -7, 9, -9 };

	// Stores number of moves available in each of the 8 directions for every square on the board
	// Order of directions is: N, S, W, E, NW, SE, NE, SW
	// So for example, if availableSquares[0][1] == 7...
	// that means that there are 7 squares to the north of b1 (the square with index 1 in board array)
	public static int[][] numSquaresToEdge;

	// Stores array of indices for each square a knight can land on from any square on the board
	// So for example, knightMoves[0] is equal to {10, 17}, meaning a knight on a1 can jump to c2 and b3
	public static final byte[][] knightMoves;
	public static final byte[][] kingMoves;

	// Pawn attack directions for white and black (NW, NE; SW SE)
	public static final byte[][] pawnAttackDirections = {
		new byte[] { 4, 6 },
		new byte[] { 7, 5 }
	};

	public static final int[][] pawnAttacksWhite;
	public static final int[][] pawnAttacksBlack;
	public static final int[] directionLookup;

	public static final long[] kingAttackBitboards;
	public static final long[] knightAttackBitboards;
	public static final long[][] pawnAttackBitboards;

	public static final long[] rookMoves;
	public static final long[] bishopMoves;
	public static final long[] queenMoves;

	// Aka manhattan distance (answers how many moves for a rook to get from square a to square b)
	public static final int[][] orthogonalDistance;
	// Aka chebyshev distance (answers how many moves for a king to get from square a to square b)
	public static final int[][] kingDistance;
	public static final int[] centreManhattanDistance;

	public static int NumRookMovesToReachSquare (int startSquare, int targetSquare) {
		return orthogonalDistance[startSquare][targetSquare];
	}

	public static int NumKingMovesToReachSquare (int startSquare, int targetSquare) {
		return kingDistance[startSquare][targetSquare];
	}

	// Initialize lookup data
	static {
		pawnAttacksWhite = new int[64][];
		pawnAttacksBlack = new int[64][];
		numSquaresToEdge = new int[8][];
		knightMoves = new byte[64][];
		kingMoves = new byte[64][];
		numSquaresToEdge = new int[64][];

		rookMoves = new long[64];
		bishopMoves = new long[64];
		queenMoves = new long[64];

		// Calculate knight jumps and available squares for each square on the board.
		// See comments by variable definitions for more info.
		int[] allKnightJumps = { 15, 17, -17, -15, 10, -6, 6, -10 };
		knightAttackBitboards = new long[64];
		kingAttackBitboards = new long[64];
		pawnAttackBitboards = new long[64][];

		for (int squareIndex = 0; squareIndex < 64; squareIndex++) {

			int y = squareIndex / 8;
			int x = squareIndex - y * 8;

			int north = 7 - y;
			int south = y;
			int west = x;
			int east = 7 - x;
			numSquaresToEdge[squareIndex] = new int[8];
			numSquaresToEdge[squareIndex][0] = north;
			numSquaresToEdge[squareIndex][1] = south;
			numSquaresToEdge[squareIndex][2] = west;
			numSquaresToEdge[squareIndex][3] = east;
			numSquaresToEdge[squareIndex][4] = Math.min (north, west);
			numSquaresToEdge[squareIndex][5] = Math.min (south, east);
			numSquaresToEdge[squareIndex][6] = Math.min (north, east);
			numSquaresToEdge[squareIndex][7] = Math.min (south, west);

			// Calculate all squares knight can jump to from current square
			var legalKnightJumps = new ArrayList<Byte> ();
			long knightBitboard = 0;
			for (int knightJumpDelta : allKnightJumps) {
				int knightJumpSquare = squareIndex + knightJumpDelta;
				if (knightJumpSquare >= 0 && knightJumpSquare < 64) {
					int knightSquareY = knightJumpSquare / 8;
					int knightSquareX = knightJumpSquare - knightSquareY * 8;
					// Ensure knight has moved max of 2 squares on x/y axis (to reject indices that have wrapped around side of board)
					int maxCoordMoveDst = Math.max (Math.abs (x - knightSquareX), Math.abs (y - knightSquareY));
					if (maxCoordMoveDst == 2) {
						legalKnightJumps.add ((byte) knightJumpSquare);
						knightBitboard |= Integer.toUnsignedLong(1) << knightJumpSquare;
					}
				}
			}
			
			
			byte[] knightMovesCurrentSquare = new byte[legalKnightJumps.size()];
			for(int i = 0; i < legalKnightJumps.size(); i++) {
				knightMovesCurrentSquare[i] = legalKnightJumps.get(i);
			}
			knightMoves[squareIndex] = knightMovesCurrentSquare;
			knightAttackBitboards[squareIndex] = knightBitboard;

			// Calculate all squares king can move to from current square (not including castling)
			var legalKingMoves = new ArrayList<Byte> ();
			for (int kingMoveDelta : directionOffsets) {
				int kingMoveSquare = squareIndex + kingMoveDelta;
				if (kingMoveSquare >= 0 && kingMoveSquare < 64) {
					int kingSquareY = kingMoveSquare / 8;
					int kingSquareX = kingMoveSquare - kingSquareY * 8;
					// Ensure king has moved max of 1 square on x/y axis (to reject indices that have wrapped around side of board)
					int maxCoordMoveDst = Math.max (Math.abs (x - kingSquareX), Math.abs (y - kingSquareY));
					if (maxCoordMoveDst == 1) {
						legalKingMoves.add ((byte) kingMoveSquare);
						kingAttackBitboards[squareIndex] |= 1L << kingMoveSquare;
					}
				}
			}
			byte[] kingMovesCurrentSquare = new byte[legalKingMoves.size()];
			for(int i = 0; i < legalKingMoves.size(); i++) {
				kingMovesCurrentSquare[i] = legalKingMoves.get(i);
			}
			kingMoves[squareIndex] = kingMovesCurrentSquare;
			// Calculate legal pawn captures for white and black
			List<Integer> pawnCapturesWhite = new ArrayList<>();
			List<Integer> pawnCapturesBlack = new ArrayList<>();
			pawnAttackBitboards[squareIndex] = new long[2];
			if (x > 0) {
				if (y < 7) {
					pawnCapturesWhite.add (squareIndex + 7);
					pawnAttackBitboards[squareIndex][AIBoard.WhiteIndex] |= Integer.toUnsignedLong(1) << (squareIndex + 7);
				}
				if (y > 0) {
					pawnCapturesBlack.add (squareIndex - 9);
					pawnAttackBitboards[squareIndex][AIBoard.BlackIndex] |= Integer.toUnsignedLong(1) << (squareIndex - 9);
				}
			}
			if (x < 7) {
				if (y < 7) {
					pawnCapturesWhite.add (squareIndex + 9);
					pawnAttackBitboards[squareIndex][AIBoard.WhiteIndex] |= Integer.toUnsignedLong(1) << (squareIndex + 9);
				}
				if (y > 0) {
					pawnCapturesBlack.add (squareIndex - 7);
					pawnAttackBitboards[squareIndex][AIBoard.BlackIndex] |= Integer.toUnsignedLong(1) << (squareIndex - 7);
				}
			}
			pawnAttacksWhite[squareIndex] = pawnCapturesWhite.stream().mapToInt(i -> i).toArray();
			pawnAttacksBlack[squareIndex] = pawnCapturesBlack.stream().mapToInt(i -> i).toArray();

			// Rook moves
			for (int directionIndex = 0; directionIndex < 4; directionIndex++) {
				int currentDirOffset = directionOffsets[directionIndex];
				for (int n = 0; n < numSquaresToEdge[squareIndex][directionIndex]; n++) {
					int targetSquare = squareIndex + currentDirOffset * (n + 1);
					rookMoves[squareIndex] |= Integer.toUnsignedLong(1) << targetSquare;
				}
			}
			// Bishop moves
			for (int directionIndex = 4; directionIndex < 8; directionIndex++) {
				int currentDirOffset = directionOffsets[directionIndex];
				for (int n = 0; n < numSquaresToEdge[squareIndex][directionIndex]; n++) {
					int targetSquare = squareIndex + currentDirOffset * (n + 1);
					bishopMoves[squareIndex] |= Integer.toUnsignedLong(1) << targetSquare;
				}
			}
			queenMoves[squareIndex] = rookMoves[squareIndex] | bishopMoves[squareIndex];
		}

		directionLookup = new int[127];
		for (int i = 0; i < 127; i++) {
			int offset = i - 63;
			int absOffset = Math.abs (offset);
			int absDir = 1;
			if (absOffset % 9 == 0) {
				absDir = 9;
			} else if (absOffset % 8 == 0) {
				absDir = 8;
			} else if (absOffset % 7 == 0) {
				absDir = 7;
			}

			directionLookup[i] = absDir * (int) Math.signum (offset);
		}

		// Distance lookup
		orthogonalDistance = new int[64][64];
		kingDistance = new int[64][64];
		centreManhattanDistance = new int[64];
		for (int squareA = 0; squareA < 64; squareA++) {
			Coord coordA = BoardRepresentation.CoordFromIndex (squareA);
			int fileDstFromCentre = Math.max(3 - coordA.fileIndex, coordA.fileIndex - 4);
			int rankDstFromCentre = Math.max(3 - coordA.rankIndex, coordA.rankIndex - 4);
			centreManhattanDistance[squareA] = fileDstFromCentre + rankDstFromCentre;

			for (int squareB = 0; squareB < 64; squareB++) {

				Coord coordB = BoardRepresentation.CoordFromIndex (squareB);
				int rankDistance = Math.abs (coordA.rankIndex - coordB.rankIndex);
				int fileDistance = Math.abs (coordA.fileIndex - coordB.fileIndex);
				orthogonalDistance[squareA][squareB] = fileDistance + rankDistance;
				kingDistance[squareA][squareB] = Math.max(fileDistance, rankDistance);
			}
		}
	}
}
