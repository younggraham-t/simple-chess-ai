package chess_ai.ai_board;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.AbstractMap.*;
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
public class FenUtility {





	public final static String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

	// Load position from fen string
	public static LoadedPositionInfo PositionFromFen (String fen) {
		Map<Character, Integer> pieceTypeFromSymbol = Stream.of(
	            new SimpleEntry<>('k', Piece.King),
	            new SimpleEntry<>('p', Piece.Pawn),
	            new SimpleEntry<>('n', Piece.Knight),
	            new SimpleEntry<>('b', Piece.Bishop),
	            new SimpleEntry<>('r', Piece.Rook),
	            new SimpleEntry<>('q', Piece.Queen))
	            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

		LoadedPositionInfo loadedPositionInfo = new LoadedPositionInfo();
		String[] sections = fen.split(" ");
		//for(String s : sections) 
			//System.out.println(s);

		int file = 0;
		int rank = 7;

		for(char symbol : sections[0].toCharArray()) {
			
			if (symbol == '/') {
				file = 0;
				rank--;
			} else {
				if (Character.isDigit(symbol)) {
					file += (int) Character.getNumericValue(symbol);
				} else {
					int pieceColour = (Character.isUpperCase(symbol)) ? Piece.White : Piece.Black;
					int pieceType = pieceTypeFromSymbol.get(Character.toLowerCase(symbol));
					loadedPositionInfo.squares[rank * 8 + file] = pieceType | pieceColour;
					file++;
				}
			}
		}

		loadedPositionInfo.whiteToMove = (sections[1].equals("w"));

		String castlingRights = (sections.length > 2) ? "" + sections[2] : "KQkq";
		loadedPositionInfo.whiteCastleKingside = castlingRights.contains ("K");
		loadedPositionInfo.whiteCastleQueenside = castlingRights.contains ("Q");
		loadedPositionInfo.blackCastleKingside = castlingRights.contains ("k");
		loadedPositionInfo.blackCastleQueenside = castlingRights.contains ("q");

		if (sections.length > 3) {
			String enPassantFileName = "" + sections[3].substring(0,1);
			
			if (Arrays.asList(BoardRepresentation.fileNames).contains(enPassantFileName)) {
				loadedPositionInfo.epFile = Arrays.asList(BoardRepresentation.fileNames).indexOf(enPassantFileName) + 1;
				
			}
		}

		// Half-move clock
		if (sections.length > 4) {
			Integer.parseInt ("" + sections[4]);
		}
		return loadedPositionInfo;
	}

	// Get the fen string of the current position
	public static String currentFen(AIBoard board) {
		String fen = "";
		for (int rank = 7; rank >= 0; rank--) {
			int numEmptyFiles = 0;
			for (int file = 0; file < 8; file++) {
				int i = rank * 8 + file;
				int piece = board.Square[i];
				if (piece != 0) {
					if (numEmptyFiles != 0) {
						fen += numEmptyFiles;
						numEmptyFiles = 0;
					}
					boolean isBlack = Piece.IsColour (piece, Piece.Black);
					int pieceType = Piece.PieceType (piece);
					char pieceChar = ' ';
					switch (pieceType) {
						case Piece.Rook:
							pieceChar = 'R';
							break;
						case Piece.Knight:
							pieceChar = 'N';
							break;
						case Piece.Bishop:
							pieceChar = 'B';
							break;
						case Piece.Queen:
							pieceChar = 'Q';
							break;
						case Piece.King:
							pieceChar = 'K';
							break;
						case Piece.Pawn:
							pieceChar = 'P';
							break;
					}
					fen += (isBlack) ? Character.toString(pieceChar).toLowerCase() : Character.toString(pieceChar);
				} else {
					numEmptyFiles++;
				}

			}
			if (numEmptyFiles != 0) {
				fen += numEmptyFiles;
			}
			if (rank != 0) {
				fen += '/';
			}
		}

		// Side to move
		fen += ' ';
		fen += (board.WhiteToMove) ? 'w' : 'b';

		// Castling
		boolean whiteKingside = (board.currentGameState & 1) == 1;
		boolean whiteQueenside = (board.currentGameState >> 1 & 1) == 1;
		boolean blackKingside = (board.currentGameState >> 2 & 1) == 1;
		boolean blackQueenside = (board.currentGameState >> 3 & 1) == 1;
		fen += ' ';
		fen += (whiteKingside) ? "K" : "";
		fen += (whiteQueenside) ? "Q" : "";
		fen += (blackKingside) ? "k" : "";
		fen += (blackQueenside) ? "q" : "";
		fen += ((board.currentGameState & 15) == 0) ? "-" : "";

		// En-passant
		fen += ' ';
		int epFile = (int) (board.currentGameState >> 4) & 15;
		if (epFile == 0) {
			fen += '-';
		} else {
			String fileName = BoardRepresentation.fileNames[epFile - 1].toString();
			int epRank = (board.WhiteToMove) ? 6 : 3;
			fen += fileName + epRank;
		}

		// 50 move counter
		fen += ' ';
		fen += board.fiftyMoveCounter;

		// Full-move count (should be one at start, and increase after each move by black)
		fen += ' ';
		fen += (board.plyCount / 2) + 1;

		return fen;
	}

}


