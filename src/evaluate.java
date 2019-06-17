
public class evaluate {
	
	public static void main(String[]args)
	{
		int [] inputBoard = TestState();
		int[][] boardState;
		int stateValue=0;
		
		stateValue = evaluateState(true, inputBoard);
		
		
		System.out.println("State Value= "+stateValue);
	}
	
	//Evaluate board state
	public static int evaluateState(boolean isWhite, int[] inputBoard)
	{
		int[][] boardState = InitBoard(inputBoard);
		print2d(boardState);
		int stateValue=0;
		
		int blackPieces = 0;
		int whitePieces = 0;
		
		//Count number of each color
		//pieces are worth 2, pieces in enemy territory worth 3
		for(int i =1; i<inputBoard.length; i++) 
		{
			//if current piece is white
			if(isWhitePiece(inputBoard[i]))
			{
				whitePieces++;
				//2 points for a piece, 4 for a king
				if(inputBoard[i]==1)
					stateValue+=2;
				else
					stateValue+=4;
				//additional point if pawn is in enemy territory
				if(i<=12 && inputBoard[i]==1)
					stateValue++;
				//another point if it's on an edge(can't be taken)
				if(isEdge(i))
					stateValue++;
				//additional point if one move from king
				if(i<9 && inputBoard[i]==1)
					stateValue++;
			}
			//if current piece is black
			if(inputBoard[i]<0)
			{
				blackPieces++;
				
				if(inputBoard[i]==-1)
					stateValue-=2;
				else
					stateValue-=4;
				
				if(i>=21 && inputBoard[i]==-1)
					stateValue--;
				
				if(isEdge(i))
					stateValue--;
				
				if(i>24 && inputBoard[i]==-1)
					stateValue--;
			}
		}
		stateValue+= capturePotential(boardState);
		
		//No black pieces == Win for White
		if(blackPieces == 0 && whitePieces>0)
			stateValue = 1000000;
		//No white pieces == Win for black
		if(whitePieces == 0 && blackPieces>0)
			stateValue = -1000000;
		
		
		return stateValue;
	}
	
	public static int capturePotential(int[][]boardState)
	{
		int canCapture=0;
		
		
		int row = 0;
		int col=3;
		for(int i=1;i<33;i++)
		{
			//check if current piece is white, check if it can capture anything 
			//if it can capture something recursively check for double jumps
			if(isWhitePiece(boardState[row][col]))
				canCapture += captureCheckWhite(row, col, boardState, boardState[row][col]);	
			if(boardState[row][col]<0)
				canCapture += captureCheckBlack(row, col, boardState, boardState[row][col]);
			
			if(i%4==0)
			{
				switch(i) {
				case 4: row = 0;
						col = 4;
						break;
				case 8: row = 1;
						col = 4;
						break;
				case 12: row = 1;
						col = 5;
						break;
				case 16: row =2;
						 col=5;
						 break;
				case 20: row=2;
						 col=6;
						 break;
				case 24: row=3;
						 col=6;
						 break;
				case 28: row=3;
						 col=7;
						 break;				
				}
			}
			else
			{
				row++;
				col--;
			}
		}
		
		return canCapture;
	}
	
	
	/*
	 * Capture Check for white pieces
	 * takes row and column coordinates of piece being assessed and current board
	 * Checks if it has any available captures from current position
	 * if there are potential captures checks for potential captures from resulting coordinates for double jumps
	 * if in the process of checking captures hits the end of the board "kings" piece for future checks 
	 */
	public static int captureCheckWhite(int row, int col, int[][] boardState, int isKing)
	{
		int vulnerableAdjacentPieces=0;
		
		//recursive checking for pontential captures by a king piece
		if(isKing==2)
		{
			if((row+2)<7 && boardState[row+2][col] == 0 && boardState[row+1][col]<0)
			{
				if(boardState[row+1][col]==-1)
					vulnerableAdjacentPieces+=4;
				else
					vulnerableAdjacentPieces+=6;
				
				//mark square as empty because captured
				boardState[row+1][col]=0;
				
				vulnerableAdjacentPieces+=captureCheckWhite(row+2, col, boardState,isKing);
					
			}
			if((col+2)<8 && boardState[row][col+2] == 0 && boardState[row][col+1]<0)
			{
				if(boardState[row][col+1]==-1)
					vulnerableAdjacentPieces+=4;
				else
					vulnerableAdjacentPieces+=6;
				
				//mark square as empty because captured
				boardState[row][col+1]=0;
				
				vulnerableAdjacentPieces+=captureCheckWhite(row, col+2, boardState, isKing);
			}
		}
		
		//recursive checking for possible captures for both pawns and kings. "Kings" piece if it reaches the end
		if((row-2>0) && boardState[row-2][col]==0 && boardState[row-1][col]<0)
		{
			if(boardState[row-1][col]==-1)
				vulnerableAdjacentPieces+=4;
			else
				vulnerableAdjacentPieces+=6;
			
			//mark square as empty because captured
			boardState[row-1][col]=0;
			
			//if is a pawn and hits the end, king it for future checks
			if(isKing==1 && kingMe(row-2, col, true))
				isKing=2;
			
			vulnerableAdjacentPieces += captureCheckWhite(row-2,col,boardState, isKing);
		}
		
		if((col-2)>0 && boardState[row][col-2]==0 && boardState[row][col-1]<0)
		{
			if(boardState[row][col-1]==-1)
				vulnerableAdjacentPieces+=4;
			else
				vulnerableAdjacentPieces+=6;
			
			//mark square as empty because captured
			boardState[row][col-1]=0;
			
			if(isKing==1 && kingMe(row, col-2, true))
				isKing = 2;
			
			vulnerableAdjacentPieces+=captureCheckWhite(row, col-2, boardState, isKing);
		}
		
		return vulnerableAdjacentPieces;
	}
	
	/*
	 * Capture Check for black pieces
	 * takes row and column coordinates of piece being assessed and current board
	 * Checks if it has any available captures from current position
	 * if there are potential captures checks for potential captures from resulting coordinates for double jumps
	 * if in the process of checking captures hits the end of the board "kings" piece for future checks 
	 */
	public static int captureCheckBlack(int row, int col, int[][] boardState, int isKing)
	{
		int vulnerableAdjacentPieces=0;
		
		//recursive checking for pontential captures by a king piece
		if(isKing==-2)
		{
			if((row-2)>0 && boardState[row-2][col] == 0 && isWhitePiece(boardState[row-1][col]))
			{
				if(boardState[row-1][col]==1)
					vulnerableAdjacentPieces-=4;
				else
					vulnerableAdjacentPieces-=6;
				
				//mark space as empty because captured
				boardState[row-1][col]=0;
				
				vulnerableAdjacentPieces+=captureCheckBlack(row-2, col, boardState,isKing);
					
			}
			if((col-2)>0 && boardState[row][col-2] == 0 && isWhitePiece(boardState[row][col-1]))
			{
				if(boardState[row][col-1]==1)
					vulnerableAdjacentPieces-=4;
				else
					vulnerableAdjacentPieces-=6;
				
				//mark space as empty because captured
				boardState[row][col-1]=0;
				
				vulnerableAdjacentPieces+=captureCheckBlack(row, col-2, boardState, isKing);
			}
		}
		
		//recursive checking for possible captures for both pawns and kings. "Kings" piece if it reaches the end
		if((row+2<7) && boardState[row+2][col]==0 && isWhitePiece(boardState[row+1][col]))
		{
			if(boardState[row+1][col]==1)
				vulnerableAdjacentPieces-=4;
			else
				vulnerableAdjacentPieces-=6;
			
			//mark space as empty because captured
			boardState[row+1][col]=0;
			
			//if is a pawn and hits the end, king it for future checks
			if(isKing==-1 && kingMe(row+2, col, false))
				isKing=-2;
			
			vulnerableAdjacentPieces += captureCheckBlack(row+2,col,boardState, isKing);
		}
		
		if((col+2)<8 && boardState[row][col+2]==0 && isWhitePiece(boardState[row][col+1]))
		{
			if(boardState[row][col+1]==1)
				vulnerableAdjacentPieces-=4;
			else
				vulnerableAdjacentPieces-=6;
			
			//mark space as empty because captured
			boardState[row][col+1]=0;
			
			if(isKing==-1 && kingMe(row, col+2, false))
				isKing = -2;
			
			vulnerableAdjacentPieces+=captureCheckBlack(row, col+2, boardState, isKing);
		}
		
		return vulnerableAdjacentPieces;
	}
	
	
	//check if piece is on a SIDE edge, not ends
	public static boolean isEdge(int position)
	{
		int [] edgeSquares = {5,13,21,12,20,28};
		for(int i=0;i<edgeSquares.length;i++)
		{
			if(edgeSquares[i]==position)
				return true;
		}
		
		return false;
	}
	
	//Check if a piece is white 
	public static boolean isWhitePiece(int i)
	{
		if(i==1 || i==2)
			return true;
		else 
			return false;
	}
	
	public static boolean kingMe(int row, int col, boolean isWhitePiece)
	{
		//white piece
		if(isWhitePiece)
		{
			for(int i=0; i<4; i++)
			{
				for(int j=3; j>=0; j--)
				{
					if(i==row && j==col)
						return true;
				}
			}
		}
		//black piece
		else
		{
			for(int i=6; i>2; i--)
			{
				for(int j=4; j<8; j++)
				{
					if(i==row && j==col)
						return true;
				}
			}
		}
		
		return false;
	}
	
	
	//initialize tilted board
	public static int[][] InitBoard(int[]inputBoard)
	{
		int[][]boardState=new int[7][8];
		
		//initialize board to empty many indeces of array will remain empty
		for(int row=0;row<boardState.length;row++)
		{
			for(int col=0;col<boardState[row].length;col++)
				boardState[row][col]=6;
		}
		
		int row = 0;
		int col=3;
		for(int i=1;i<33;i++)
		{
			boardState[row][col]=inputBoard[i];
			
			if(i%4==0)
			{
				switch(i) {
				case 4: row = 0;
						col = 4;
						break;
				case 8: row = 1;
						col = 4;
						break;
				case 12: row = 1;
						col = 5;
						break;
				case 16: row =2;
						 col=5;
						 break;
				case 20: row=2;
						 col=6;
						 break;
				case 24: row=3;
						 col=6;
						 break;
				case 28: row=3;
						 col=7;
						 break;				
				}
			}
			else
			{
				row++;
				col--;
			}
		}
		
		
		return boardState;
	}
	
	
	public static int[] TestState()
	{
	
		int [] boardState = new int[33];
		for(int i=1;i<boardState.length;i++)
		{
			boardState[i]=0;
			
			if(i==5)
				boardState[i]=-2;
			if(i==9)
				boardState[i]=1;
			if(i==10)
				boardState[i]=1;
			if(i==6)
				boardState[i]=-1;
			if(i==32)
				boardState[i]=-2;
			if(i==27)
				boardState[i]=1;
			if(i==23)
				boardState[i]=1;
		}
		
		return boardState;
	}
	
	public static void print2d(int [][] array)
	{
		for(int i=0;i<array.length;i++)
		{
			for(int j=0;j<array[i].length;j++)
			{
				System.out.print(array[i][j]+ "\t");
			}
			System.out.println();
		}
	}
	
}
