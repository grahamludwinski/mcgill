import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Snakes implements Runnable {
	private static final int NORTH = 0;
	private static final int EAST  = 1;
	private static final int SOUTH = 2;
	private static final int WEST  = 3;
	
	private int snakeLength;
	private int width;
	private int height;
	private int moveTime;
	private SnakeThread[] snakes;
	private Cell[][] grid;
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
		this.snakeLength = n;
		this.width = m;
		this.height = m;
		this.moveTime = t;
		
		this.grid = new Cell[m][m];
		this.snakes = new SnakeThread[p];
		
		for(int i=0; i<m; i++) {
			for(int j=0; j<m; j++) {
				grid[j][i] = new Cell(j, i);
			}
		}
	}
	
	@Override
	public void run() {
		//create threads
		for (int i=0; i< snakes.length; i++) {
			snakes[i] = new SnakeThread(snakeLength, new Cell(0,i));
		}
		
		PaintingAndStroking paint = new PaintingAndStroking();
		paint.repaint();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
		    	paint.repaint();
				Thread.sleep(moveTime);
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
		public Color color;
		
		public SnakeThread(int length, Cell startPosition) {
			numMovements = 0;
			cells = new Cell[length];
			for(int i=0; i<length; i++) {
				//grid[startPosition.getX()+i][startPosition.getY()].lock();
				cells[i] = grid[startPosition.getX()+i][startPosition.getY()];
			}
			
			color = new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
		}
		
		@Override
		public void run() {
			for(Cell cell : cells) {
				if(cell.tryLock() == false) {
					System.out.println("Cell is already locked! Oh noes!");
				}
			}
			//System.out.println("All cells locked");
			
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
				if (next.isHeldByCurrentThread() || !next.tryLock()) {
					//cannot move into newPos
					directions.remove(direction);
				} else {
					isStuck = false;
					//update all cells
					//grid[next.getX()][next.getY()] = next;
					Cell temp = null;
					for(int i=cells.length-1; i>=0; i--) {
						temp = cells[i];
						cells[i] = next;
						next = temp;
					}
					
					Cell old = grid[temp.getX()][temp.getY()];
					if (old.isHeldByCurrentThread()) {
						//System.out.println("Lock held by current thread: " + old);
						old.unlock();
					} else {
						System.out.println("Lock not held by current thread: " + old);
					}
					
					numMovements++;
					
					return;
				}
			}
			
			isStuck = true;
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
			return grid[x][y];
		}
		
		private Cell getHead() {
			return cells[cells.length - 1];
		}
	}
	
	private class PaintingAndStroking extends Frame {
        int FRAME_X = 800;
        int FRAME_Y = 770;
        int OFFSET_X = 10;
        int OFFSET_Y = 30;
        int CELL_SIZE = 5;

        public PaintingAndStroking() {
            setTitle("snakes on a plane");
            setSize(FRAME_X, FRAME_Y);
            setVisible(true);
        }

        public void paint(Graphics g) {
            g.drawRect(OFFSET_X, OFFSET_Y, CELL_SIZE * width, CELL_SIZE * height);
            for (SnakeThread snake : snakes) {
                int count = 0;
                g.setColor(snake.color);
                for (Cell cell : snake.cells) {
                    if (count >= snakeLength - 1) {
                        if(snake.isStuck) {
                            g.setColor(new Color(200, 50, 50));
                        }
                        else {
                            g.setColor(new Color(50, 50, 200));
                        }
                    }
                    g.fillRect(cell.getX() * CELL_SIZE + OFFSET_X, cell.getY() * CELL_SIZE + OFFSET_Y, CELL_SIZE, CELL_SIZE);
                    count++;
                }
            }
        }
	}
}
