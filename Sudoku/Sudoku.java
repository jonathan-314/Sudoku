package Sudoku;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Sudoku extends JPanel implements MouseListener, KeyListener {
	/**
	 * serial version id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * sudoku instance
	 */
	static Sudoku test;

	/**
	 * the current state
	 */
	int[][] puzzle = new int[9][9];

	/**
	 * the original puzzle
	 */
	int[][] game = new int[9][9];

	/**
	 * the solution
	 */
	int[][] solution = new int[9][9];

	/**
	 * JFrame
	 */
	JFrame jf;

	/**
	 * the x location that the user has selected
	 */
	int selectx = -1;

	/**
	 * the y location that the user has selected
	 */
	int selecty = -1;

	/**
	 * start time, used for timing
	 */
	long startTime = 0;

	/**
	 * elapsed time, used for timing
	 */
	long time = 0;

	/**
	 * difficulty
	 */
	int difficulty = 0;

	/**
	 * is the game over
	 */
	boolean gameOver = false;

	/**
	 * check answers?
	 */
	boolean checkAnswers = false;

	/**
	 * Random instance
	 */
	Random random = new Random();

	/**
	 * width of screen
	 */
	int screenWidth = getToolkit().getScreenSize().width;

	/**
	 * height of screen
	 */
	int screenHeight = getToolkit().getScreenSize().height;

	/**
	 * Sudoku constructor
	 */
	public Sudoku() {
		solution = generateSolution();
		game = generatePuzzle(solution, difficulty);
		reset();
		addMouseListener(this);
		addKeyListener(this);
		setFocusable(true);
		this.requestFocus();

		jf = new JFrame("sudoku");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setSize(getToolkit().getScreenSize());
		jf.setVisible(true);
		jf.add(this);
		startTime = System.currentTimeMillis();
		try {
			int temp = 0;
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					if (game[i][j] != 0) {
						temp++;
					}
				}
			}
			System.out.println("not blank: " + temp);
			while (true) {
				if (!gameOver)
					time = System.currentTimeMillis() - startTime;
				jf.repaint();
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted Exception");
		}
	}

	/**
	 * Paint function, called each frame
	 */
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenWidth, screenHeight);
		
		g.setColor(Color.YELLOW);
		g.drawOval(screenWidth / 2 - 50, 8, 15, 15);
		g.drawString("C", screenWidth / 2 - 50 + 3, 20);
		g.drawString("Jonathan Guo", screenWidth / 2 - 50 + 20, 20);
		
		Font f = new Font("Helvetica", 20, 30);
		g.setFont(f);
		g.setColor(Color.BLUE);
		if (selectx != -1 && selecty != -1)
			g.fillRect(60 * selectx + 370, 60 * selecty + 90, 60, 60);

		g.setColor(Color.YELLOW);
		for (int i = 0; i < 9; i++) {
			g.drawRect(980, 60 * i + 90, 60, 60);
			g.drawString("" + (i + 1), 980 + 20, 60 * i + 130);
		}
		// time stuff
		long seconds = (time / 1000) % 60;
		long minutes = (time / 1000) / 60;
		String timeDisplay = Long.toString(seconds);
		if (timeDisplay.length() == 1)
			timeDisplay = "0" + timeDisplay;
		timeDisplay = minutes + ":" + timeDisplay;
		g.drawString(timeDisplay, 1080, 130);

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				g.setColor(Color.YELLOW);
				g.drawRect(60 * i + 370, 60 * j + 90, 60, 60);
				if (puzzle[i][j] == 0) // blank square
					continue;
				if (game[i][j] == 0) // user entered number
					g.setColor(Color.CYAN);
				if (checkAnswers)
					if (game[i][j] == 0 && solution[i][j] != puzzle[i][j]) // incorrect value entered!
						g.setColor(Color.RED);
				g.drawString("" + puzzle[i][j], 60 * i + 390, 60 * j + 130);
			}
		}
		g.setColor(Color.YELLOW);
		for (int i = 0; i < 4; i++) {
			g.fillRect(180 * i + 370 - 2, 90, 4, 540);
			g.fillRect(370, 180 * i + 90 - 2, 540, 4);
		}
	}

	/**
	 * restart (same puzzle though)
	 */
	public void reset() {
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++)
				puzzle[i][j] = game[i][j];
	}

	/**
	 * generate a puzzle
	 * 
	 * @param sol        solution
	 * @param difficulty difficulty
	 * @return a puzzle with solution sol of the specified difficulty
	 */
	public int[][] generatePuzzle(int[][] sol, int difficulty) {
		int[][] answer = new int[9][9];
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++)
				answer[i][j] = sol[i][j];

		int row = -1;
		int col = -1;
		for (int k = 0; k < 500; k++) {
			while (true) { // make sure it isn't filled in
				row = random.nextInt(9);
				col = random.nextInt(9);
				if (answer[row][col] != 0)
					break;
			}
			int previousNumber = answer[row][col];
			answer[row][col] = 0;
			int[][] copy = new int[9][9];
			for (int i = 0; i < 9; i++)
				for (int j = 0; j < 9; j++)
					copy[i][j] = answer[i][j];
			int[][] solvedCopy = solve(copy);
			boolean good = true;
			o: for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					if (solvedCopy[i][j] == 0) {
						good = false;
						break o;
					}
				}
			}
			if (!good)
				answer[row][col] = previousNumber;
		}
		// matching difficulty
		int tick = 0;
		while (tick < difficulty) {
			row = random.nextInt(9);
			col = random.nextInt(9);
			if (answer[row][col] == 0) {
				answer[row][col] = sol[row][col];
				tick++;
			}
			boolean allFilledIn = true;
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					if (answer[i][j] == 0) {
						allFilledIn = false;
						break;
					}
				}
			}
			if (allFilledIn)
				break;
		}
		return answer;
	}

	private boolean[][][] generatePossibilitiesArray() {
		boolean[][][] answer = new boolean[9][9][10];
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				Arrays.fill(answer[i][j], true);
				answer[i][j][0] = false;
			}
		}
		return answer;
	}

	/**
	 * generate a solution
	 * 
	 * @return a solution
	 */
	public int[][] generateSolution() {
		int[][] puzzle = new int[9][9];
//		boolean[][][] rowArray = new boolean[9][9][10];
//		boolean[][][] colArray = new boolean[9][9][10];
//		boolean[][][] sqArray = new boolean[9][9][10];
//		for (int i = 0; i < 9; i++) {
//			for (int j = 0; j < 9; j++) {
//				Arrays.fill(rowArray[i][j], true);
//				rowArray[i][j][0] = false;
//				Arrays.fill(colArray[i][j], true);
//				colArray[i][j][0] = false;
//				Arrays.fill(sqArray[i][j], true);
//				sqArray[i][j][0] = false;
//			}
//		}
		boolean[][][] rowArray = generatePossibilitiesArray();
		boolean[][][] colArray = generatePossibilitiesArray();
		boolean[][][] sqArray = generatePossibilitiesArray();
		puzzle = dfs(0, puzzle, rowArray, colArray, sqArray);
		return puzzle;
	}

	/**
	 * depth first search, creates a puzzle
	 * 
	 * @param current  current index that is being looked at
	 * @param puzzle   the puzzle
	 * @param rowArray which values are possible, based on rows
	 * @param colArray which values are possible, based on columns
	 * @param sqArray  which values are possible, based on squares
	 * @return a randomly generated puzzle
	 */
	public int[][] dfs(int current, int[][] puzzle, boolean[][][] rowArray, boolean[][][] colArray,
			boolean[][][] sqArray) {
		int[][] falseArray = new int[9][9]; // no solutions found
		falseArray[0][0] = -1;
		if (current == 81) // dfs done!! made it to the end
			return puzzle;
		int row = current / 9;
		int col = current % 9;
		int available = 0;
		boolean[] possible = new boolean[10];
		for (int i = 0; i < 10; i++) {
			possible[i] = rowArray[row][col][i] && colArray[row][col][i] && sqArray[row][col][i];
			if (i >= 1 && possible[i])
				available++;
		}
		if (available == 0)
			return falseArray;
		int permutation = random.nextInt(factorial(available));
		for (int i = available - 1; i >= 0; i--) {
			int value = permutation / factorial(i);
			permutation %= factorial(i);
			for (int j = 1; j <= 9; j++) {
				if (!possible[j])
					continue;
				if (value == 0) {
					possible[j] = false;
					// int[][] newPuzzle = new int[9][9];
					// boolean[][][] newArray = new boolean[9][9][10];
//					for (int k = 0; k < 9; k++) {
//						for (int l = 0; l < 9; l++) {
//							newPuzzle[k][l] = puzzle[k][l];
//							for (int m = 0; m < 10; m++)
//								newArray[k][l][m] = arr[k][l][m];
//						}
//					}

					// newPuzzle[row][col] = j;
					puzzle[row][col] = j;

					for (int k = 0; k < 9; k++) {
						// newArray[row][k][j] = false;
						// newArray[k][col][j] = false;
						rowArray[row][k][j] = false;
						colArray[k][col][j] = false;
					}
					int squareRow = row / 3;
					int squareCol = col / 3;
					for (int k = 0; k < 3; k++)
						for (int l = 0; l < 3; l++)
							// newArray[3 * squareRow + k][3 * squareCol + l][j] = false;
							sqArray[3 * squareRow + k][3 * squareCol + l][j] = false;
					int[][] result = dfs(current + 1, puzzle, rowArray, colArray, sqArray);
					// int[][] result = dfs(current + 1, newPuzzle, newArray);
					if (result[0][0] > 0)
						return result;
					puzzle[row][col] = 0;
					for (int k = 0; k < 9; k++) {
						// newArray[row][k][j] = false;
						// newArray[k][col][j] = false;
						rowArray[row][k][j] = true;
						colArray[k][col][j] = true;
					}
					for (int k = 0; k < 3; k++)
						for (int l = 0; l < 3; l++)
							// newArray[3 * squareRow + k][3 * squareCol + l][j] = false;
							sqArray[3 * squareRow + k][3 * squareCol + l][j] = true;
					break;
				}
				value--;
			}
		}
		return falseArray;
	}

	/**
	 * factorial function
	 * 
	 * @param n number
	 * @return n!
	 */
	public int factorial(int n) {
		if (n <= 9)
			return new int[] { 1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880 }[n];
		return n * factorial(n - 1);
	}

	/**
	 * solve this puzzle
	 * 
	 * @param puzz puzzle to be solved
	 * @return solved puzzle
	 */
	public int[][] solve(int[][] puzz) {
//		boolean[][][] rowArray = new boolean[9][9][10];
//		boolean[][][] colArray = new boolean[9][9][10];
//		boolean[][][] sqArray = new boolean[9][9][10];
//		for (int i = 0; i < 9; i++) {
//			for (int j = 0; j < 9; j++) {
//				Arrays.fill(rowArray[i][j], true);
//				rowArray[i][j][0] = false;
//				Arrays.fill(colArray[i][j], true);
//				colArray[i][j][0] = false;
//				Arrays.fill(sqArray[i][j], true);
//				sqArray[i][j][0] = false;
//			}
//		}
		boolean[][][] rowArray = generatePossibilitiesArray();
		boolean[][][] colArray = generatePossibilitiesArray();
		boolean[][][] sqArray = generatePossibilitiesArray();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (puzz[i][j] != 0) {
					fillIn(i, j, puzz[i][j], puzz, rowArray, colArray, sqArray);
				}
			}
		}
		for (int i = 0; i < 81; i++) {
			if (!solveOne(puzz, rowArray, colArray, sqArray)) {
				break;
			}
		}
		return puzz;
	}

	/**
	 * is it possible to solve one square?
	 * 
	 * @param puzz     the puzzle
	 * @param rowArray possibilities array, based on rows
	 * @param colArray possibilities array, based on columns
	 * @param sqArray  possibilities array, based on squares
	 * @return whether it is possible to solve on square or not
	 */
	public boolean solveOne(int[][] puzz, boolean[][][] rowArray, boolean[][][] colArray, boolean[][][] sqArray) {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (game[i][j] != 0)
					continue;
				if (puzz[i][j] != 0) {
					continue;
				}
				int possibilities = 0;
				int num = 0;
				for (int k = 1; k <= 9; k++) {
					if (rowArray[i][j][k] && colArray[i][j][k] && sqArray[i][j][k]) {
						possibilities++;
						num = k;
					}
				}
				if (possibilities != 1) // only one possibility!
					continue;
				fillIn(i, j, num, puzz, rowArray, colArray, sqArray);
				return true;
			}
		}
		return false;
	}

	/**
	 * fills in one square
	 * 
	 * @param r        row
	 * @param c        column
	 * @param value    value to be filled in
	 * @param puzz     the puzzle
	 * @param rowArray possibility array, based on rows
	 * @param colArray possibility array, based on columns
	 * @param sqArray  possibility array, based on squares
	 */
	public void fillIn(int r, int c, int value, int[][] puzz, boolean[][][] rowArray, boolean[][][] colArray,
			boolean[][][] sqArray) {
		puzz[r][c] = value;
		for (int i = 0; i < 9; i++) {
			// array[r][i][value] = false;
			// array[i][c][value] = false;
			// array[r][c][i + 1] = false;

			rowArray[r][i][value] = false;
			colArray[i][c][value] = false;
		}
		int squareRow = r / 3;
		int squareCol = c / 3;
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				// array[3 * squareRow + i][3 * squareCol + j][value] = false;
				sqArray[3 * squareRow + i][3 * squareCol + j][value] = false;
	}

	/**
	 * user fill in
	 * 
	 * @param val value (between 0 and 9)
	 */
	public void userFillIn(int val) {
		if (selectx == -1 || selecty == -1)
			return;
		if (game[selectx][selecty] != 0)
			return;
		puzzle[selectx][selecty] = val;
		boolean allCorrect = true;
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++)
				if (solution[i][j] != puzzle[i][j])
					allCorrect = false;
		if (allCorrect) {
			selectx = -1;
			selecty = -1;
			gameOver = true;
			JOptionPane.showMessageDialog(this, "Game Over! You win!");
			System.exit(ABORT);
		}
	}

	/**
	 * main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		test = new Sudoku();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int mousex = e.getX();
		int mousey = e.getY();
		if (mousey >= 90 && mousey <= 90 + 540) {
			if (mousex >= 370 && mousex <= 370 + 540) {
				selectx = (mousex - 370) / 60;
				selecty = (mousey - 90) / 60;
				if (selectx >= 9 || selecty >= 9 || game[selectx][selecty] != 0) {
					selectx = -1;
					selecty = -1;
					return;
				}
			} else if (mousex >= 980 && mousex <= 1040) {
				userFillIn((mousey - 90) / 60 + 1);
			} else {
				selectx = -1;
				selecty = -1;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		String key = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();
		char c = key.charAt(0);
		if (selectx == -1 || selecty == -1)
			return;
		if (c <= '9' && c >= '0') {
			userFillIn(c - '0');
		} else {
			if (c == 'W')
				selecty--;
			else if (c == 'S')
				selecty++;
			else if (c == 'A')
				selectx--;
			else if (c == 'D')
				selectx++;
			if (selectx == 9 || selectx == -1 || selecty == 9 || selecty == -1) { // out of bounds
				selectx = -1;
				selecty = -1;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
}
