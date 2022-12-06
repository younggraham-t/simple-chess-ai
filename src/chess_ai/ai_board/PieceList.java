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
public class PieceList {

	// Indices of squares occupied by given piece type (only elements up to Count are valid, the rest are unused/garbage)
	public int[] occupiedSquares;
	// Map to go from index of a square, to the index in the occupiedSquares array where that square is stored
	int[] map;
	int numPieces;

	public PieceList(int maxPieceCount) {
		occupiedSquares = new int[maxPieceCount];
		map = new int[64];
		numPieces = 0;
	}
	
	public PieceList() {
		this(16);
	}

	public int size() {
		return numPieces;
	}
	
	public int get(int position) {
	    return occupiedSquares[position];
	}

	public void set(int position, int value) {
		occupiedSquares[position] = value;
	}

	public void addPieceAtSquare (int square) {
		occupiedSquares[numPieces] = square;
		map[square] = numPieces;
		numPieces++;
	}

	public void removePieceAtSquare (int square) {
		int pieceIndex = map[square]; // get the index of this element in the occupiedSquares array
		occupiedSquares[pieceIndex] = occupiedSquares[numPieces - 1]; // move last element in array to the place of the removed element
		map[occupiedSquares[pieceIndex]] = pieceIndex; // update map to point to the moved element's new location in the array
		numPieces--;
	}

	public void movePiece (int startSquare, int targetSquare) {
		int pieceIndex = map[startSquare]; // get the index of this element in the occupiedSquares array
		occupiedSquares[pieceIndex] = targetSquare;
		map[targetSquare] = pieceIndex;
	}


}
