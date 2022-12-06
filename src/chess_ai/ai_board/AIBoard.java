package chess_ai.ai_board;

import java.util.Stack;

import chess_ai.ai_board.Piece;
/**
 * this class was originally written by SebLague in C# and converted to Java by
 * Graham Young
 * 
 * original file: https://github.com/SebLague/Chess-AI/blob/main/Assets/Scripts/Core/Board.cs
 * 
 * 
 * @author SebLague, Graham Young
 *
 */

public class AIBoard {

	public final static int WhiteIndex = 0;
	public final static int BlackIndex = 1;

	// Stores piece code for each square on the board.
	// Piece code is defined as piecetype | colour code
	public int[] Square;

	public boolean WhiteToMove;
	public int ColourToMove;
	public int OpponentColour;
	public int ColourToMoveIndex;

	// Bits 0-3 store white and black kingside/queenside castling legality
	// Bits 4-7 store file of ep square (starting at 1, so 0 = no ep square)
	// Bits 8-13 captured piece
	// Bits 14-... fifty mover counter
	Stack<Integer> gameStateHistory;
	public int currentGameState;

	public int plyCount; // Total plies played in game
	public int fiftyMoveCounter; // Num ply since last pawn move or capture

	public long ZobristKey;
	/// List of zobrist keys 
	public Stack<Long> RepetitionPositionHistory;

	public int[] KingSquare; // index of square of white and black king

	public PieceList[] rooks;
	public PieceList[] bishops;
	public PieceList[] queens;
	public PieceList[] knights;
	public PieceList[] pawns;

	PieceList[] allPieceLists;

	final int whiteCastleKingsideMask = 0b1111111111111110;
	final int whiteCastleQueensideMask = 0b1111111111111101;
	final int blackCastleKingsideMask = 0b1111111111111011;
	final int blackCastleQueensideMask = 0b1111111111110111;

	final int whiteCastleMask = whiteCastleKingsideMask & whiteCastleQueensideMask;
	final int blackCastleMask = blackCastleKingsideMask & blackCastleQueensideMask;
	

	
	

	PieceList GetPieceList (int pieceType, int colourIndex) {
		return allPieceLists[colourIndex * 8 + pieceType];
	}
	
	public static int pawnValue = 100;
	public static int knightValue = 300;
	public static int bishopValue = 300;
	public static int rookValue = 500;
	public static int queenValue = 900;
	
    /**
     * Written by Graham Young on 11/13/22
     * method for determining the current evaluation of the board
     * currently just checks to see if which player has the most material
     * @return int that is positive if white is better negative if black is better and 0
     * if the position is even
     * @TODO make the function better by adding other parameters to check.
     * 
     */
    public int evaluate() {
    	int whiteEval = countMaterial(WhiteIndex);
    	int blackEval = countMaterial(BlackIndex);
    	return whiteEval - blackEval;
    }
    
    
    int countMaterial(int colourIndex) {
		int material = 0;
		material += this.pawns[colourIndex].size() * pawnValue;
		material += this.knights[colourIndex].size() * knightValue;
		material += this.bishops[colourIndex].size() * bishopValue;
		material += this.rooks[colourIndex].size() * rookValue;
		material += this.queens[colourIndex].size() * queenValue;

		return material;
	}

    public void MakeMove(Move move) {
    	MakeMove(move, false);
    }

    public void UnmakeMove(Move move) {
    	UnmakeMove(move, false);
    }
	// Make a move on the board
	// The inSearch parameter controls whether this move should be recorded in the game history (for detecting three-fold repetition)
	public void MakeMove (Move move, boolean inSearch) {
		int oldEnPassantFile = (currentGameState >> 4) & 15;
		int originalCastleState = currentGameState & 15;
		int newCastleState = originalCastleState;
		currentGameState = 0;

		int opponentColourIndex = 1 - ColourToMoveIndex;
		int moveFrom = move.getStartSquare();
		int moveTo = move.getTargetSquare();

		int capturedPieceType = Piece.PieceType (Square[moveTo]);
		int movePiece = Square[moveFrom];
		int movePieceType = Piece.PieceType (movePiece);

		int moveFlag = move.getMoveFlag();
		boolean isPromotion = move.isPromotion();
		boolean isEnPassant = moveFlag == Move.Flag.EnPassantCapture;

		// Handle captures
		currentGameState |= (short) (capturedPieceType << 8);
		if (capturedPieceType != 0 && !isEnPassant) {
			ZobristKey ^= Zobrist.piecesArray[capturedPieceType][opponentColourIndex][moveTo];
			GetPieceList (capturedPieceType, opponentColourIndex).removePieceAtSquare (moveTo);
		}

		// Move pieces in piece lists
		if (movePieceType == Piece.King) {
			KingSquare[ColourToMoveIndex] = moveTo;
			newCastleState &= (WhiteToMove) ? whiteCastleMask : blackCastleMask;
		} else {
			GetPieceList (movePieceType, ColourToMoveIndex).movePiece(moveFrom, moveTo);
		}

		int pieceOnTargetSquare = movePiece;

		// Handle promotion
		if (isPromotion) {
			int promoteType = 0;
			switch (moveFlag) {
				case Move.Flag.PromoteToQueen:
					promoteType = Piece.Queen;
					queens[ColourToMoveIndex].addPieceAtSquare(moveTo);
					break;
				case Move.Flag.PromoteToRook:
					promoteType = Piece.Rook;
					rooks[ColourToMoveIndex].addPieceAtSquare (moveTo);
					break;
				case Move.Flag.PromoteToBishop:
					promoteType = Piece.Bishop;
					bishops[ColourToMoveIndex].addPieceAtSquare (moveTo);
					break;
				case Move.Flag.PromoteToKnight:
					promoteType = Piece.Knight;
					knights[ColourToMoveIndex].addPieceAtSquare (moveTo);
					break;

			}
			pieceOnTargetSquare = promoteType | ColourToMove;
			pawns[ColourToMoveIndex].removePieceAtSquare (moveTo);
		} else {
			// Handle other special moves (en-passant, and castling)
			switch (moveFlag) {
				case Move.Flag.EnPassantCapture:
					int epPawnSquare = moveTo + ((ColourToMove == Piece.White) ? -8 : 8);
					currentGameState |= (short) (Square[epPawnSquare] << 8); // add pawn as capture type
					Square[epPawnSquare] = 0; // clear ep capture square
					pawns[opponentColourIndex].removePieceAtSquare (epPawnSquare);
					ZobristKey ^= Zobrist.piecesArray[Piece.Pawn][opponentColourIndex][epPawnSquare];
					break;
				case Move.Flag.Castling:
					boolean kingside = moveTo == BoardRepresentation.g1 || moveTo == BoardRepresentation.g8;
					int castlingRookFromIndex = (kingside) ? moveTo + 1 : moveTo - 2;
					int castlingRookToIndex = (kingside) ? moveTo - 1 : moveTo + 1;

					Square[castlingRookFromIndex] = Piece.None;
					Square[castlingRookToIndex] = Piece.Rook | ColourToMove;

					rooks[ColourToMoveIndex].movePiece (castlingRookFromIndex, castlingRookToIndex);
					ZobristKey ^= Zobrist.piecesArray[Piece.Rook][ColourToMoveIndex][castlingRookFromIndex];
					ZobristKey ^= Zobrist.piecesArray[Piece.Rook][ColourToMoveIndex][castlingRookToIndex];
					break;
			}
		}

		// Update the board representation:
		Square[moveTo] = pieceOnTargetSquare;
		Square[moveFrom] = 0;

		// Pawn has moved two forwards, mark file with en-passant flag
		if (moveFlag == Move.Flag.PawnTwoForward) {
			int file = BoardRepresentation.FileIndex (moveFrom) + 1;
			currentGameState |= (short) (file << 4);
			ZobristKey ^= Zobrist.enPassantFile[file];
		}

		// Piece moving to/from rook square removes castling right for that side
		if (originalCastleState != 0) {
			if (moveTo == BoardRepresentation.h1 || moveFrom == BoardRepresentation.h1) {
				newCastleState &= whiteCastleKingsideMask;
			} else if (moveTo == BoardRepresentation.a1 || moveFrom == BoardRepresentation.a1) {
				newCastleState &= whiteCastleQueensideMask;
			}
			if (moveTo == BoardRepresentation.h8 || moveFrom == BoardRepresentation.h8) {
				newCastleState &= blackCastleKingsideMask;
			} else if (moveTo == BoardRepresentation.a8 || moveFrom == BoardRepresentation.a8) {
				newCastleState &= blackCastleQueensideMask;
			}
		}

		// Update zobrist key with new piece position and side to move
		ZobristKey ^= Zobrist.sideToMove;
		ZobristKey ^= Zobrist.piecesArray[movePieceType][ColourToMoveIndex][moveFrom];
		ZobristKey ^= Zobrist.piecesArray[Piece.PieceType(pieceOnTargetSquare)][ColourToMoveIndex][moveTo];

		if (oldEnPassantFile != 0)
			ZobristKey ^= Zobrist.enPassantFile[oldEnPassantFile];

		if (newCastleState != originalCastleState) {
			ZobristKey ^= Zobrist.castlingRights[originalCastleState]; // remove old castling rights state
			ZobristKey ^= Zobrist.castlingRights[newCastleState]; // add new castling rights state
		}
		currentGameState |= newCastleState;
		currentGameState |= (int) fiftyMoveCounter << 14;
		gameStateHistory.push(currentGameState);

		// Change side to move
		WhiteToMove = !WhiteToMove;
		ColourToMove = (WhiteToMove) ? Piece.White : Piece.Black;
		OpponentColour = (WhiteToMove) ? Piece.Black : Piece.White;
		ColourToMoveIndex = 1 - ColourToMoveIndex;
		plyCount++;
		fiftyMoveCounter++;

		if (!inSearch) {
			if (movePieceType == Piece.Pawn || capturedPieceType != Piece.None) {
				RepetitionPositionHistory.clear ();
				fiftyMoveCounter = 0;
			} else {
				RepetitionPositionHistory.push(ZobristKey);
			}
		}

	}

	// Undo a move previously made on the board
	public void UnmakeMove(Move move, boolean inSearch) {

		//int opponentColour = ColourToMove;
		int opponentColourIndex = ColourToMoveIndex;
		boolean undoingWhiteMove = OpponentColour == Piece.White;
		ColourToMove = OpponentColour; // side who made the move we are undoing
		OpponentColour = (undoingWhiteMove) ? Piece.Black : Piece.White;
		ColourToMoveIndex = 1 - ColourToMoveIndex;
		WhiteToMove = !WhiteToMove;

		int originalCastleState = currentGameState & 0b1111;

		int capturedPieceType = ((int) currentGameState >> 8) & 63;
		int capturedPiece = (capturedPieceType == 0) ? 0 : capturedPieceType | OpponentColour;

		int movedFrom = move.getStartSquare();
		int movedTo = move.getTargetSquare();
		int moveFlags = move.getMoveFlag();
		boolean isEnPassant = moveFlags == Move.Flag.EnPassantCapture;
		boolean isPromotion = move.isPromotion();

		int toSquarePieceType = Piece.PieceType (Square[movedTo]);
		int movedPieceType = (isPromotion) ? Piece.Pawn : toSquarePieceType;

		// Update zobrist key with new piece position and side to move
		ZobristKey ^= Zobrist.sideToMove;
		ZobristKey ^= Zobrist.piecesArray[movedPieceType][ColourToMoveIndex][movedFrom]; // add piece back to square it moved from
		ZobristKey ^= Zobrist.piecesArray[toSquarePieceType][ColourToMoveIndex][movedTo]; // remove piece from square it moved to

		int oldEnPassantFile = (currentGameState >> 4) & 15;
		if (oldEnPassantFile != 0)
			ZobristKey ^= Zobrist.enPassantFile[oldEnPassantFile];

		// ignore ep captures, handled later
		if (capturedPieceType != 0 && !isEnPassant) {
			ZobristKey ^= Zobrist.piecesArray[capturedPieceType][opponentColourIndex][movedTo];
			GetPieceList (capturedPieceType, opponentColourIndex).addPieceAtSquare (movedTo);
		}

		// Update king index
		if (movedPieceType == Piece.King) {
			KingSquare[ColourToMoveIndex] = movedFrom;
		} else if (!isPromotion) {
			GetPieceList (movedPieceType, ColourToMoveIndex).movePiece (movedTo, movedFrom);
		}

		// put back moved piece
		Square[movedFrom] = movedPieceType | ColourToMove; // note that if move was a pawn promotion, this will put the promoted piece back instead of the pawn. Handled in special move switch
		Square[movedTo] = capturedPiece; // will be 0 if no piece was captured

		if (isPromotion) {
			pawns[ColourToMoveIndex].addPieceAtSquare (movedFrom);
			switch (moveFlags) {
				case Move.Flag.PromoteToQueen:
					queens[ColourToMoveIndex].removePieceAtSquare (movedTo);
					break;
				case Move.Flag.PromoteToKnight:
					knights[ColourToMoveIndex].removePieceAtSquare (movedTo);
					break;
				case Move.Flag.PromoteToRook:
					rooks[ColourToMoveIndex].removePieceAtSquare (movedTo);
					break;
				case Move.Flag.PromoteToBishop:
					bishops[ColourToMoveIndex].removePieceAtSquare (movedTo);
					break;
			}
		} else if (isEnPassant) { // ep cature: put captured pawn back on right square
			int epIndex = movedTo + ((ColourToMove == Piece.White) ? -8 : 8);
			Square[movedTo] = 0;
			Square[epIndex] = (int) capturedPiece;
			pawns[opponentColourIndex].addPieceAtSquare (epIndex);
			ZobristKey ^= Zobrist.piecesArray[Piece.Pawn][opponentColourIndex][epIndex];
		} else if (moveFlags == Move.Flag.Castling) { // castles: move rook back to starting square

			boolean kingside = movedTo == 6 || movedTo == 62;
			int castlingRookFromIndex = (kingside) ? movedTo + 1 : movedTo - 2;
			int castlingRookToIndex = (kingside) ? movedTo - 1 : movedTo + 1;

			Square[castlingRookToIndex] = 0;
			Square[castlingRookFromIndex] = Piece.Rook | ColourToMove;

			rooks[ColourToMoveIndex].movePiece (castlingRookToIndex, castlingRookFromIndex);
			ZobristKey ^= Zobrist.piecesArray[Piece.Rook][ColourToMoveIndex][castlingRookFromIndex];
			ZobristKey ^= Zobrist.piecesArray[Piece.Rook][ColourToMoveIndex][castlingRookToIndex];

		}

		gameStateHistory.pop(); // removes current state from history
		currentGameState = gameStateHistory.peek(); // sets current state to previous state in history

		fiftyMoveCounter = (int) (currentGameState & Integer.parseUnsignedInt("4294950912")) >> 14;
		int newEnPassantFile = (int) (currentGameState >> 4) & 15;
		if (newEnPassantFile != 0)
			ZobristKey ^= Zobrist.enPassantFile[newEnPassantFile];

		int newCastleState = currentGameState & 0b1111;
		if (newCastleState != originalCastleState) {
			ZobristKey ^= Zobrist.castlingRights[originalCastleState]; // remove old castling rights state
			ZobristKey ^= Zobrist.castlingRights[newCastleState]; // add new castling rights state
		}

		plyCount--;

		if (!inSearch && RepetitionPositionHistory.size() > 0) {
			RepetitionPositionHistory.pop();
		}

	}

	// Load the starting position
	public void LoadStartPosition () {
		LoadPosition (FenUtility.startFen);
	}

	// Load custom position from fen string
	public void LoadPosition (String fen) {
		Initialize ();
		var loadedPosition = FenUtility.PositionFromFen (fen);

		// Load pieces into board array and piece lists
		for (int squareIndex = 0; squareIndex < 64; squareIndex++) {
			int piece = loadedPosition.squares[squareIndex];
			Square[squareIndex] = piece;

			if (piece != Piece.None) {
				int pieceType = Piece.PieceType (piece);
				int pieceColourIndex = (Piece.IsColour (piece, Piece.White)) ? WhiteIndex : BlackIndex;
				if (Piece.IsSlidingPiece (piece)) {
					if (pieceType == Piece.Queen) {
						queens[pieceColourIndex].addPieceAtSquare (squareIndex);
					} else if (pieceType == Piece.Rook) {
						rooks[pieceColourIndex].addPieceAtSquare (squareIndex);
					} else if (pieceType == Piece.Bishop) {
						bishops[pieceColourIndex].addPieceAtSquare (squareIndex);
					}
				} else if (pieceType == Piece.Knight) {
					knights[pieceColourIndex].addPieceAtSquare (squareIndex);
				} else if (pieceType == Piece.Pawn) {
					pawns[pieceColourIndex].addPieceAtSquare (squareIndex);
				} else if (pieceType == Piece.King) {
					KingSquare[pieceColourIndex] = squareIndex;
				}
			}
		}

		// Side to move
		WhiteToMove = loadedPosition.whiteToMove;
		ColourToMove = (WhiteToMove) ? Piece.White : Piece.Black;
		OpponentColour = (WhiteToMove) ? Piece.Black : Piece.White;
		ColourToMoveIndex = (WhiteToMove) ? 0 : 1;

		// Create gamestate
		int whiteCastle = ((loadedPosition.whiteCastleKingside) ? 1 << 0 : 0) | ((loadedPosition.whiteCastleQueenside) ? 1 << 1 : 0);
		int blackCastle = ((loadedPosition.blackCastleKingside) ? 1 << 2 : 0) | ((loadedPosition.blackCastleQueenside) ? 1 << 3 : 0);
		int epState = loadedPosition.epFile << 4;
		short initialGameState = (short) (whiteCastle | blackCastle | epState);
		gameStateHistory.push(Short.toUnsignedInt(initialGameState));
		currentGameState = initialGameState;
		plyCount = loadedPosition.plyCount;

		// Initialize zobrist key
		ZobristKey = Zobrist.CalculateZobristKey (this);
	}

	void Initialize () {
		Square = new int[64];
		KingSquare = new int[2];

		gameStateHistory = new Stack<Integer> ();
		ZobristKey = 0;
		RepetitionPositionHistory = new Stack<Long> ();
		plyCount = 0;
		fiftyMoveCounter = 0;

		knights = new PieceList[] { new PieceList(10), new PieceList(10) };
		pawns = new PieceList[] { new PieceList(8), new PieceList(8) };
		rooks = new PieceList[] { new PieceList(10), new PieceList(10) };
		bishops = new PieceList[] { new PieceList(10), new PieceList(10) };
		queens = new PieceList[] { new PieceList(9), new PieceList(9) };
		PieceList emptyList = new PieceList (0);
		allPieceLists = new PieceList[] {
			emptyList,
			emptyList,
			pawns[WhiteIndex],
			knights[WhiteIndex],
			emptyList,
			bishops[WhiteIndex],
			rooks[WhiteIndex],
			queens[WhiteIndex],
			emptyList,
			emptyList,
			pawns[BlackIndex],
			knights[BlackIndex],
			emptyList,
			bishops[BlackIndex],
			rooks[BlackIndex],
			queens[BlackIndex],
		};
	}
}
