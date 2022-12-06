package chess_ai.ai_board;
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
public class BoardRepresentation {
	public final static String[] fileNames = {"a","b","c","d","e","f","g","h"};
	public final static String[] rankNames = {"1","2","3","4","5","6","7","8"};

	public final static int a1 = 0;
	public final static int b1 = 1;
	public final static int c1 = 2;
	public final static int d1 = 3;
	public final static int e1 = 4;
	public final static int f1 = 5;
	public final static int g1 = 6;
	public final static int h1 = 7;

	public final static int a8 = 56;
	public final static int b8 = 57;
	public final static int c8 = 58;
	public final static int d8 = 59;
	public final static int e8 = 60;
	public final static int f8 = 61;
	public final static int g8 = 62;
	public final static int h8 = 63;

	// Rank (0 to 7) of square 
	public static int RankIndex (int squareIndex) {
		return squareIndex >> 3;
	}

	// File (0 to 7) of square 
	public static int FileIndex (int squareIndex) {
		return squareIndex & 0b000111;
	}

	public static int IndexFromCoord (int fileIndex, int rankIndex) {
		return rankIndex * 8 + fileIndex;
	}

	public static int IndexFromCoord (Coord coord) {
		return IndexFromCoord(coord.fileIndex, coord.rankIndex);
	}

	public static Coord CoordFromIndex (int squareIndex) {
		return new Coord(FileIndex(squareIndex), RankIndex(squareIndex));
	}

	public static boolean LightSquare (int fileIndex, int rankIndex) {
		return (fileIndex + rankIndex) % 2 != 0;
	}

	public static String SquareNameFromCoordinate (int fileIndex, int rankIndex) {
		return fileNames[fileIndex] + "" + (rankIndex + 1);
	}

	public static String SquareNameFromIndex (int squareIndex) {
		return SquareNameFromCoordinate(CoordFromIndex(squareIndex));
	}

	public static String SquareNameFromCoordinate (Coord coord) {
		return SquareNameFromCoordinate(coord.fileIndex, coord.rankIndex);
	}
}
