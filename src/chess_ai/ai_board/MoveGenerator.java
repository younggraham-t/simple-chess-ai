package chess_ai.ai_board;

import java.util.ArrayList;
import java.util.List;

import chess_ai.ai_board.PrecomputedMoveData;
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

public class MoveGenerator {
	public enum PromotionMode { All, QueenOnly, QueenAndKnight }

	public PromotionMode promotionsToGenerate = PromotionMode.All;

	// ---- Instance variables ----
	List<Move> moves;
	boolean isWhiteToMove;
	int friendlyColour;
	int opponentColour;
	int friendlyKingSquare;
	int friendlyColourIndex;
	int opponentColourIndex;

	public boolean inCheck;
	boolean inDoubleCheck;
	boolean pinsExistInPosition;
	long checkRayBitmask;
	long pinRayBitmask;
	long opponentKnightAttacks;
	long opponentAttackMapNoPawns;
	public long opponentAttackMap;
	public long opponentPawnAttackMap;
	long opponentSlidingAttackMap;

	boolean genQuiets;
	AIBoard board;
	
	
	static PrecomputedMoveData pmd = new PrecomputedMoveData();
	
	// Generates list of legal moves in current position.
	// Quiet moves (non captures) can optionally be excluded. This is used in quiescence search.
	public List<Move> generateMoves (AIBoard board, boolean includeQuietMoves) {
		this.board = board;
		genQuiets = includeQuietMoves;
		initiateVariables();

		calculateAttackData ();
		generateKingMoves ();

		// Only king moves are valid in a double check position, so can return early.
		if (inDoubleCheck) {
			return moves;
		}

		generateSlidingMoves ();
		generateKnightMoves ();
		generatePawnMoves ();

		return moves;
	}
	
	public List<Move> generateMoves(AIBoard board) {
		return generateMoves(board, true);
	}
	
	public boolean isInCheck(AIBoard board) {
		generateMoves(board);
		return inCheck;
	}
	
	void initiateVariables () {
		moves = new ArrayList<Move>(64);
		inCheck = false;
		inDoubleCheck = false;
		pinsExistInPosition = false;
		checkRayBitmask = 0;
		pinRayBitmask = 0;

		isWhiteToMove = board.ColourToMove == Piece.White;
		friendlyColour = board.ColourToMove;
		opponentColour = board.OpponentColour;
		friendlyKingSquare = board.KingSquare[board.ColourToMoveIndex];
		friendlyColourIndex = (board.WhiteToMove) ? AIBoard.WhiteIndex : AIBoard.BlackIndex;
		opponentColourIndex = 1 - friendlyColourIndex;
	}
	
	void generateSlidingMoves() {
		PieceList rooks = board.rooks[friendlyColourIndex];
		for (int i = 0; i < rooks.size(); i++) {
			generateSlidingPieceMoves(rooks.get(i), 0, 4);
		}

		PieceList bishops = board.bishops[friendlyColourIndex];
		for (int i = 0; i < bishops.size(); i++) {
			generateSlidingPieceMoves(bishops.get(i), 4, 8);
		}

		PieceList queens = board.queens[friendlyColourIndex];
		for (int i = 0; i < queens.size(); i++) {
			generateSlidingPieceMoves(queens.get(i), 0, 8);
		}

	}
	void generateKingMoves() {
		for (int i = 0; i < pmd.kingMoves[friendlyKingSquare].length; i++) {
			int targetSquare = pmd.kingMoves[friendlyKingSquare][i];
			int pieceOnTargetSquare = board.Square[targetSquare];

			// Skip squares occupied by friendly pieces
			if (Piece.IsColour (pieceOnTargetSquare, friendlyColour)) {
				continue;
			}

			boolean isCapture = Piece.IsColour (pieceOnTargetSquare, opponentColour);
			if (!isCapture) {
				// King can't move to square marked as under enemy control, unless he is capturing that piece
				// Also skip if not generating quiet moves
				if (!genQuiets || SquareIsInCheckRay (targetSquare)) {
					continue;
				}
			}

			// Safe for king to move to this square
			if (!SquareIsAttacked (targetSquare)) {
				moves.add(new Move (friendlyKingSquare, targetSquare));

				// Castling:
				if (!inCheck && !isCapture) {
					// Castle kingside
					if ((targetSquare == BoardRepresentation.f1 || targetSquare == BoardRepresentation.f8) && HasKingsideCastleRight()) {
						int castleKingsideSquare = targetSquare + 1;
						if (board.Square[castleKingsideSquare] == Piece.None) {
							if (!SquareIsAttacked (castleKingsideSquare)) {
								moves.add (new Move (friendlyKingSquare, castleKingsideSquare, Move.Flag.Castling));
							}
						}
					}
					// Castle queenside
					else if ((targetSquare == BoardRepresentation.d1 || targetSquare == BoardRepresentation.d8) && HasQueensideCastleRight()) {
						int castleQueensideSquare = targetSquare - 1;
						if (board.Square[castleQueensideSquare] == Piece.None && board.Square[castleQueensideSquare - 1] == Piece.None) {
							if (!SquareIsAttacked (castleQueensideSquare)) {
								moves.add (new Move (friendlyKingSquare, castleQueensideSquare, Move.Flag.Castling));
							}
						}
					}
				}
			}
		}
	}
	



	void generateSlidingPieceMoves (int startSquare, int startDirIndex, int endDirIndex) {
		boolean isPinned = IsPinned (startSquare);

		// If this piece is pinned, and the king is in check, this piece cannot move
		if (inCheck && isPinned) {
			return;
		}

		for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
			int currentDirOffset = pmd.directionOffsets[directionIndex];

			// If pinned, this piece can only move along the ray towards/away from the friendly king, so skip other directions
			if (isPinned && !IsMovingAlongRay (currentDirOffset, friendlyKingSquare, startSquare)) {
				continue;
			}

			for (int n = 0; n < pmd.numSquaresToEdge[startSquare][directionIndex]; n++) {
				int targetSquare = startSquare + currentDirOffset * (n + 1);
				int targetSquarePiece = board.Square[targetSquare];

				// Blocked by friendly piece, so stop looking in this direction
				if (Piece.IsColour (targetSquarePiece, friendlyColour)) {
					break;
				}
				boolean isCapture = targetSquarePiece != Piece.None;

				boolean movePreventsCheck = SquareIsInCheckRay (targetSquare);
				if (movePreventsCheck || !inCheck) {
					if (genQuiets || isCapture) {
						moves.add (new Move (startSquare, targetSquare));
					}
				}
				// If square not empty, can't move any further in this direction
				// Also, if this move blocked a check, further moves won't block the check
				if (isCapture || movePreventsCheck) {
					break;
				}
			}
		}
	}

	void generateKnightMoves () {
		PieceList myKnights = board.knights[friendlyColourIndex];

		for (int i = 0; i < myKnights.size(); i++) {
			int startSquare = myKnights.get(i);

			// Knight cannot move if it is pinned
			if (IsPinned (startSquare)) {
				continue;
			}

			for (int knightMoveIndex = 0; knightMoveIndex < pmd.knightMoves[startSquare].length; knightMoveIndex++) {
				int targetSquare = pmd.knightMoves[startSquare][knightMoveIndex];
				int targetSquarePiece = board.Square[targetSquare];
				boolean isCapture = Piece.IsColour (targetSquarePiece, opponentColour);
				if (genQuiets || isCapture) {
					// Skip if square contains friendly piece, or if in check and knight is not interposing/capturing checking piece
					if (Piece.IsColour (targetSquarePiece, friendlyColour) || (inCheck && !SquareIsInCheckRay (targetSquare))) {
						continue;
					}
					moves.add (new Move (startSquare, targetSquare));
				}
			}
		}
	}

	void generatePawnMoves () {
		PieceList myPawns = board.pawns[friendlyColourIndex];
		int pawnOffset = (friendlyColour == Piece.White) ? 8 : -8;
		int startRank = (board.WhiteToMove) ? 1 : 6;
		int finalRankBeforePromotion = (board.WhiteToMove) ? 6 : 1;

		int enPassantFile = ((int)(board.currentGameState >> 4) & 15) - 1;
		int enPassantSquare = -1;
		if (enPassantFile != -1) {
			enPassantSquare = 8 * ((board.WhiteToMove) ? 5 : 2) + enPassantFile;
		}

		for (int i = 0; i < myPawns.size(); i++) {
			int startSquare = myPawns.get(i);
			int rank = BoardRepresentation.RankIndex (startSquare);
			boolean oneStepFromPromotion = rank == finalRankBeforePromotion;

			if (genQuiets) {

				int squareOneForward = startSquare + pawnOffset;

				// Square ahead of pawn is empty: forward moves
				if (board.Square[squareOneForward] == Piece.None) {
					// Pawn not pinned, or is moving along line of pin
					if (!IsPinned (startSquare) || IsMovingAlongRay (pawnOffset, startSquare, friendlyKingSquare)) {
						// Not in check, or pawn is interposing checking piece
						if (!inCheck || SquareIsInCheckRay (squareOneForward)) {
							if (oneStepFromPromotion) {
								MakePromotionMoves (startSquare, squareOneForward);
							} else {
								moves.add (new Move (startSquare, squareOneForward));
							}
						}

						// Is on starting square (so can move two forward if not blocked)
						if (rank == startRank) {
							int squareTwoForward = squareOneForward + pawnOffset;
							if (board.Square[squareTwoForward] == Piece.None) {
								// Not in check, or pawn is interposing checking piece
								if (!inCheck || SquareIsInCheckRay (squareTwoForward)) {
									moves.add (new Move (startSquare, squareTwoForward, Move.Flag.PawnTwoForward));
								}
							}
						}
					}
				}
			}

			// Pawn captures.
			for (int j = 0; j < 2; j++) {
				// Check if square exists diagonal to pawn
				if (pmd.numSquaresToEdge[startSquare][pmd.pawnAttackDirections[friendlyColourIndex][j]] > 0) {
					// move in direction friendly pawns attack to get square from which enemy pawn would attack
					int pawnCaptureDir = pmd.directionOffsets[pmd.pawnAttackDirections[friendlyColourIndex][j]];
					int targetSquare = startSquare + pawnCaptureDir;
					int targetPiece = board.Square[targetSquare];

					// If piece is pinned, and the square it wants to move to is not on same line as the pin, then skip this direction
					if (IsPinned (startSquare) && !IsMovingAlongRay (pawnCaptureDir, friendlyKingSquare, startSquare)) {
						continue;
					}

					// Regular capture
					if (Piece.IsColour (targetPiece, opponentColour)) {
						// If in check, and piece is not capturing/interposing the checking piece, then skip to next square
						if (inCheck && !SquareIsInCheckRay (targetSquare)) {
							continue;
						}
						if (oneStepFromPromotion) {
							MakePromotionMoves (startSquare, targetSquare);
						} else {
							moves.add (new Move (startSquare, targetSquare));
						}
					}

					// Capture en-passant
					if (targetSquare == enPassantSquare) {
						int epCapturedPawnSquare = targetSquare + ((board.WhiteToMove) ? -8 : 8);
						if (!InCheckAfterEnPassant (startSquare, targetSquare, epCapturedPawnSquare)) {
							moves.add (new Move (startSquare, targetSquare, Move.Flag.EnPassantCapture));
						}
					}
				}
			}
		}
	}

	void MakePromotionMoves (int fromSquare, int toSquare) {
		moves.add (new Move (fromSquare, toSquare, Move.Flag.PromoteToQueen));
		if (promotionsToGenerate == PromotionMode.All) {
			moves.add (new Move (fromSquare, toSquare, Move.Flag.PromoteToKnight));
			moves.add (new Move (fromSquare, toSquare, Move.Flag.PromoteToRook));
			moves.add (new Move (fromSquare, toSquare, Move.Flag.PromoteToBishop));
		} else if (promotionsToGenerate == PromotionMode.QueenAndKnight) {
			moves.add (new Move (fromSquare, toSquare, Move.Flag.PromoteToKnight));
		}

	}

	boolean IsMovingAlongRay (int rayDir, int startSquare, int targetSquare) {
		int moveDir = pmd.directionLookup[targetSquare - startSquare + 63];
		return (rayDir == moveDir || -rayDir == moveDir);
	}

	//boolean IsMovingAlongRay (int directionOffset, int absRayOffset) {
	//return !((directionOffset == 1 || directionOffset == -1) && absRayOffset >= 7) && absRayOffset % directionOffset == 0;
	//}

	boolean IsPinned (int square) {
		return pinsExistInPosition && ((pinRayBitmask >> square) & 1) != 0;
	}

	boolean SquareIsInCheckRay (int square) {
		return inCheck && ((checkRayBitmask >> square) & 1) != 0;
	}

	boolean HasKingsideCastleRight(){

		int mask = (board.WhiteToMove) ? 1 : 4;
		return (board.currentGameState & mask) != 0;
	}

	boolean HasQueensideCastleRight() {

		int mask = (board.WhiteToMove) ? 2 : 8;
		return (board.currentGameState & mask) != 0;
		
	}

	void GenSlidingAttackMap () {
		opponentSlidingAttackMap = 0;

		PieceList enemyRooks = board.rooks[opponentColourIndex];
		for (int i = 0; i < enemyRooks.size(); i++) {
			UpdateSlidingAttackPiece (enemyRooks.get(i), 0, 4);
		}

		PieceList enemyQueens = board.queens[opponentColourIndex];
		for (int i = 0; i < enemyQueens.size(); i++) {
			UpdateSlidingAttackPiece (enemyQueens.get(i), 0, 8);
		}

		PieceList enemyBishops = board.bishops[opponentColourIndex];
		for (int i = 0; i < enemyBishops.size(); i++) {
			UpdateSlidingAttackPiece (enemyBishops.get(i), 4, 8);
		}
	}

	void UpdateSlidingAttackPiece (int startSquare, int startDirIndex, int endDirIndex) {

		for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
			int currentDirOffset = pmd.directionOffsets[directionIndex];
			for (int n = 0; n < pmd.numSquaresToEdge[startSquare][directionIndex]; n++) {
				int targetSquare = startSquare + currentDirOffset * (n + 1);
				int targetSquarePiece = board.Square[targetSquare];
				opponentSlidingAttackMap |= Integer.toUnsignedLong(1) << targetSquare;
				if (targetSquare != friendlyKingSquare) {
					if (targetSquarePiece != Piece.None) {
						break;
					}
				}
			}
		}
	}

	void calculateAttackData () {
		GenSlidingAttackMap ();
		// Search squares in all directions around friendly king for checks/pins by enemy sliding pieces (queen, rook, bishop)
		int startDirIndex = 0;
		int endDirIndex = 8;

		if (board.queens[opponentColourIndex].size() == 0) {
			startDirIndex = (board.rooks[opponentColourIndex].size() > 0) ? 0 : 4;
			endDirIndex = (board.bishops[opponentColourIndex].size() > 0) ? 8 : 4;
		}

		for (int dir = startDirIndex; dir < endDirIndex; dir++) {
			boolean isDiagonal = dir > 3;

			int n = pmd.numSquaresToEdge[friendlyKingSquare][dir];
			int directionOffset = pmd.directionOffsets[dir];
			boolean isFriendlyPieceAlongRay = false;
			long rayMask = 0;

			for (int i = 0; i < n; i++) {
				int squareIndex = friendlyKingSquare + directionOffset * (i + 1);
				rayMask |= Integer.toUnsignedLong(1) << squareIndex;
				int piece = board.Square[squareIndex];

				// This square contains a piece
				if (piece != Piece.None) {
					if (Piece.IsColour (piece, friendlyColour)) {
						// First friendly piece we have come across in this direction, so it might be pinned
						if (!isFriendlyPieceAlongRay) {
							isFriendlyPieceAlongRay = true;
						}
						// This is the second friendly piece we've found in this direction, therefore pin is not possible
						else {
							break;
						}
					}
					// This square contains an enemy piece
					else {
						int pieceType = Piece.PieceType (piece);

						// Check if piece is in bitmask of pieces able to move in current direction
						if (isDiagonal && Piece.IsBishopOrQueen (pieceType) || !isDiagonal && Piece.IsRookOrQueen (pieceType)) {
							// Friendly piece blocks the check, so this is a pin
							if (isFriendlyPieceAlongRay) {
								pinsExistInPosition = true;
								pinRayBitmask |= rayMask;
							}
							// No friendly piece blocking the attack, so this is a check
							else {
								checkRayBitmask |= rayMask;
								inDoubleCheck = inCheck; // if already in check, then this is double check
								inCheck = true;
							}
							break;
						} else {
							// This enemy piece is not able to move in the current direction, and so is blocking any checks/pins
							break;
						}
					}
				}
			}
			// Stop searching for pins if in double check, as the king is the only piece able to move in that case anyway
			if (inDoubleCheck) {
				break;
			}

		}

		// Knight attacks
		PieceList opponentKnights = board.knights[opponentColourIndex];
		opponentKnightAttacks = 0;
		boolean isKnightCheck = false;

		for (int knightIndex = 0; knightIndex < opponentKnights.size(); knightIndex++) {
			int startSquare = opponentKnights.get(knightIndex);
			opponentKnightAttacks |= pmd.knightAttackBitboards[startSquare];

			if (!isKnightCheck && BitBoardUtility.ContainsSquare (opponentKnightAttacks, friendlyKingSquare)) {
				isKnightCheck = true;
				inDoubleCheck = inCheck; // if already in check, then this is double check
				inCheck = true;
				checkRayBitmask |= Integer.toUnsignedLong(1) << startSquare;
			}
		}

		// Pawn attacks
		PieceList opponentPawns = board.pawns[opponentColourIndex];
		opponentPawnAttackMap = 0;
		boolean isPawnCheck = false;

		for (int pawnIndex = 0; pawnIndex < opponentPawns.size(); pawnIndex++) {
			int pawnSquare = opponentPawns.get(pawnIndex);
			long pawnAttacks = pmd.pawnAttackBitboards[pawnSquare][opponentColourIndex];
			opponentPawnAttackMap |= pawnAttacks;

			if (!isPawnCheck && BitBoardUtility.ContainsSquare (pawnAttacks, friendlyKingSquare)) {
				isPawnCheck = true;
				inDoubleCheck = inCheck; // if already in check, then this is double check
				inCheck = true;
				checkRayBitmask |= Integer.toUnsignedLong(1) << pawnSquare;
			}
		}

		int enemyKingSquare = board.KingSquare[opponentColourIndex];

		opponentAttackMapNoPawns = opponentSlidingAttackMap | opponentKnightAttacks | pmd.kingAttackBitboards[enemyKingSquare];
		opponentAttackMap = opponentAttackMapNoPawns | opponentPawnAttackMap;
	}

	boolean SquareIsAttacked (int square) {
		return BitBoardUtility.ContainsSquare (opponentAttackMap, square);
	}

	boolean InCheckAfterEnPassant (int startSquare, int targetSquare, int epCapturedPawnSquare) {
		// Update board to reflect en-passant capture
		board.Square[targetSquare] = board.Square[startSquare];
		board.Square[startSquare] = Piece.None;
		board.Square[epCapturedPawnSquare] = Piece.None;

		boolean inCheckAfterEpCapture = false;
		if (SquareAttackedAfterEPCapture (epCapturedPawnSquare, startSquare)) {
			inCheckAfterEpCapture = true;
		}

		// Undo change to board
		board.Square[targetSquare] = Piece.None;
		board.Square[startSquare] = Piece.Pawn | friendlyColour;
		board.Square[epCapturedPawnSquare] = Piece.Pawn | opponentColour;
		return inCheckAfterEpCapture;
	}

	boolean SquareAttackedAfterEPCapture (int epCaptureSquare, int capturingPawnStartSquare) {
		if (BitBoardUtility.ContainsSquare (opponentAttackMapNoPawns, friendlyKingSquare)) {
			return true;
		}

		// Loop through the horizontal direction towards ep capture to see if any enemy piece now attacks king
		int dirIndex = (epCaptureSquare < friendlyKingSquare) ? 2 : 3;
		for (int i = 0; i < pmd.numSquaresToEdge[friendlyKingSquare][dirIndex]; i++) {
			int squareIndex = friendlyKingSquare + pmd.directionOffsets[dirIndex] * (i + 1);
			int piece = board.Square[squareIndex];
			if (piece != Piece.None) {
				// Friendly piece is blocking view of this square from the enemy.
				if (Piece.IsColour (piece, friendlyColour)) {
					break;
				}
				// This square contains an enemy piece
				else {
					if (Piece.IsRookOrQueen (piece)) {
						return true;
					} else {
						// This piece is not able to move in the current direction, and is therefore blocking any checks along this line
						break;
					}
				}
			}
		}

		// check if enemy pawn is controlling this square (can't use pawn attack bitboard, because pawn has been captured)
		for (int i = 0; i < 2; i++) {
			// Check if square exists diagonal to friendly king from which enemy pawn could be attacking it
			if (pmd.numSquaresToEdge[friendlyKingSquare][pmd.pawnAttackDirections[friendlyColourIndex][i]] > 0) {
				// move in direction friendly pawns attack to get square from which enemy pawn would attack
				int piece = board.Square[friendlyKingSquare + pmd.directionOffsets[pmd.pawnAttackDirections[friendlyColourIndex][i]]];
				if (piece == (Piece.Pawn | opponentColour)) // is enemy pawn
				{
					return true;
				}
			}
		}

		return false;
	}
}

