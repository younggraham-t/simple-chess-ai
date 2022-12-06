package chess_ai.ai_board;

public class LoadedPositionInfo {
	public int[] squares;
	public boolean whiteCastleKingside;
	public boolean whiteCastleQueenside;
	public boolean blackCastleKingside;
	public boolean blackCastleQueenside;
	public int epFile;
	public boolean whiteToMove;
	public int plyCount;

	public LoadedPositionInfo () {
		squares = new int[64];
	}
}