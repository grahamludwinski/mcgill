import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

public class Snakes implements Runnable {
	private static final int NORTH = 0;
	private static final int EAST  = 1;
	private static final int SOUTH = 2;
	private static final int WEST  = 3;
	
	private int numSnakes;
	private int snakeLength;
	private int width;
	private int height;
	private int moveTime;
	private SnakeThread[] snakes;
	private Cell[][] grid;
	private int stuckSnakes;
	private static final Random RANDOM = new Random();
	
	public static void main(String[] args) {
		int p;
		int n;
		int m;
		int t;
		
        try {
            p = Integer.parseInt(args[0]);
            n = Integer.parseInt(args[1]);
            m = Integer.parseInt(args[2]);
            t = Integer.parseInt(args[3]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Please using the format below :\n"
            		+ "java Factor p n m t");
        }
        
        System.out.println("Running using " + p + " snakes of length " + n + " in a " + m + "x" + m + " grid with a delay of " + t + "ms");
        Snakes snake = new Snakes(p,n,m,t);
        snake.run();
	}
	
	public Snakes(int p, int n, int m, int t) {
		this.numSnakes = p;
		this.snakeLength = n;
		this.width = m;
		this.height = m;
		this.moveTime = t;
		
		this.grid = new Cell[m][m];
		this.snakes = new SnakeThread[p];
		
		this.stuckSnakes = 0;
	}
	
	@Override
	public void run() {
		//create threads
		createThreads();
		
		printGrid(grid);
		
		//start threads running
		for(SnakeThread thread : snakes) {
			thread.start();
		}
		
		//wait for threads to finish
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() < startTime + 60000) {
		    try {
		    	if (allSnakesAreStuck()) {
		    		startTime-= 60000;
		    	}
				Thread.sleep(moveTime);
				printGrid(grid);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for(SnakeThread thread : snakes) {
			thread.interrupt();
		}
		
		if (allSnakesAreStuck()) {
			System.out.println("All snakes are stuck!");
		} else {
			System.out.println("1 minute was reached");
		}
		
		//print number of movements
		for(int i=0; i< snakes.length; i++) {
			System.out.println("Snake " + i + " moved " + snakes[i].numMovements + " times");
		}
	}
	
	public boolean allSnakesAreStuck() {
		boolean allAreStuck = true;
		
		for(SnakeThread snake : snakes) {
    		if (!snake.isStuck) {
    			allAreStuck = false;
    		}
    	}
		
		return allAreStuck;
	}
	public void printGrid(Cell[][] grid) {
		System.out.println("\n");
		for(int y=0; y<grid[0].length; y++) {
			for(int x=0; x<grid.length; x++) {
				if(grid[x][y] == null) {
					System.out.print("+");
				} else {
					System.out.print("o");
				}
				System.out.print(" ");
			}
			System.out.println();
		}
	}
	public void createThreads() {
		for (int i=0; i< snakes.length; i++) {
			snakes[i] = new SnakeThread(snakeLength, new Cell(0,i));
		}
	}
	
	public Set<Integer> newDirections() {
		Set<Integer> directions = new HashSet<Integer>();
		directions.add(0);
		directions.add(1);
		directions.add(2);
		directions.add(3);
		
		return directions;
	}
	
	private class SnakeThread extends Thread {
		private Cell[] cells;
		private int numMovements;
		public boolean isStuck = false;
		public SnakeThread(int length, Cell startPosition) {
			numMovements = 0;
			cells = new Cell[length];
			for(int i=0; i<length; i++) {
				Cell cell = new Cell(startPosition.getX()+i, startPosition.getY());
				grid[cell.getX()][cell.getY()] = cell;
				cells[i] = cell;
			}
		}
		
		@Override
		public void run() {
			while(true) {
				this.move();
				try {
					Thread.sleep(moveTime);
				} catch (InterruptedException e) {
				}
			}
		}
		
		public void move() {
			Set<Integer> directions = newDirections();
			while(directions.size() != 0) {
				Integer[] temporary = new Integer[0];
				Integer[] choices = (Integer[]) directions.toArray(temporary);
				int direction = choices[RANDOM.nextInt(choices.length)];
				
				Cell next = getNewPosition(getHead(), direction);
				if (grid[next.getX()][next.getY()] != null) {
					//cannot move into newPos
					directions.remove(direction);
				} else {
					isStuck = false;
					//update all cells
					grid[next.getX()][next.getY()] = next;
					Cell temp = null;
					for(int i=cells.length-1; i>=0; i--) {
						temp = cells[i];
						cells[i] = next;
						next = temp;
						
					}
					grid[temp.getX()][temp.getY()] = null;
					
					numMovements++;
					return;
				}
			}
			
			isStuck = true;
			//System.out.println("I'm stuck!");
		}
		
		public Cell getNewPosition(Cell current, int direction) {
			int x = current.getX();
			int y = current.getY();
			switch (direction) {
			case NORTH:
				if (y == 0) {
					y = height - 1;
				} else {
					y = y - 1;
				}
				break;
			case EAST:
				if (x == width - 1) {
					x = 0;
				} else {
					x = x + 1;
				}
				break;
			case SOUTH:
				if (y == height - 1) {
					y = 0;
				} else {
					y = y + 1;
				}
				break;
			case WEST:
				if (x == 0) {
					x = width - 1;
				} else {
					x = x - 1;
				}
				break;
			}
			return new Cell(x, y);
		}
		
		private Cell getHead() {
			return cells[cells.length - 1];
		}

		private synchronized boolean canMove(int direction) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
}
