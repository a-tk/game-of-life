import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Stack;
import javax.swing.JPanel;


/**
 * Class builds a Conways Game of Life simulation, and builds it into a basic GUI JPanel.
 * 
 * @author Andre Keys
 *
 */

public class CellOverlord extends JPanel{

	private int[][][] control;
	private Stack<OrderedPair> changeList, nextList, drawList;
	//GUI
	private int spacing = 1;
	
	
	/**
	 * Builds the basic grid for the simulation. Fills each cell as turned on/off based on the parameter seed.
	 * Grid is of width x and height y
	 * 
	 * 
	 * @param x
	 * @param y
	 * @param seed
	 */
	
	CellOverlord(int x, int y, double seed){
		
		
		/*Instead of using a 2-d array of objects, an 3-d array is used, with the first two dimensions used to locate the cell, and the five positions in the third dimension 
		 * used to store each locations' attributes.
		 *They all take int values, but many only utilize binary values of 0 and 1.
		 *
		 * Attributes for the five positions are listed below:
		 * [0]: Cells status. 0 is considered dead, and 1 is considered alive
		 * [1]: Number of alive neighbors each cell has. Uses the values 0 - 8. 
		 * [2]: A flag that is used when looking for cells to add to the next Iterations checking stack. Basically prevents a single location from being added more than once. 
		 * 		0 signifies that the cell has not yet been added, and 1 signifies the cell has been added
		 * [3]: An interesting defect presented itself when looking for neighbors, and a flag was created that prevents it, but unfortunately prevents the array from overlapping. 
		 * 		Currently a quick fix. 0 means the cell is not on the edge of the 2-d location grid, and 1 does
		 * [4]: When GUI drawing was decided to be implemented as part of this class, this flag indicates which color each cell should be drawn as. 0 for white, 1 for black.
		 * 
		 */
		
		/*
		 * Three stacks are used to hold lists of locations of cells:
		 * An OrderedPair object is an object that holds 2 integer values, x and y, that can be accessed later. It is used as the stacks' object type to identify locations in the 2 grid.
		 * 
		 * changeList: Stores locations of cells that need have their state changed. i.e. locations that need to change based on the simulation rules during the next pass.
		 * nextList: Stores the locations of all adjacent cells to cells that changed during the previous simulation, as they might now be candidates for changing in the next iteration
		 * drawList: Stores locations of cells that need to be redrawn because their state has changed.
		 * 
		 * A note on the stacks:
		 * 		They have been chosen for this implementation to prevent the iteration of every position in the array, and assist in reducing the computational complexity of the simulation
		 * 		They save A LOT of time.
		 * 
		 */
		
		control = new int[x][y][5];
		changeList = new Stack<OrderedPair>();
		nextList = new Stack<OrderedPair>();
		drawList = new Stack<OrderedPair>();
		
		//create fill values of array. third array contains on or off (1 or 0) and neighborCount, and a new flag in the third position
		for (int i = 0; i < control.length; i++){
			for (int k = 0; k < control[i].length; k ++){
				control[i][k][0] = 0; //Initial state = dead
				control[i][k][1] = 0; //Initial neighborCount = 0
				control[i][k][2] = 0; //No cells have been seen
				control[i][k][3] = 0; //Not yet known if a cell is on the edge of the array
				control[i][k][4] = 0; //No drawing is necessary
				
			}

		}
		
		//hard wire overlapping. Currently unnecessary as edge cells are not looked at in the simulation. Flag declaration is necessary //TODO:
		
		for (int i = 1; i < control.length - 1; i ++){
			control[i][0] = control[i][control[i].length - 2];
			control[i][control[i].length - 1] = control[i][1];
			
			control[i][control[i].length - 1][3] = 1;	//set flag for being on the edge of the array
			
		}for (int k = 1; k < control[k].length - 1; k ++){
			control[0][k] = control[control.length - 2][k];
			control[control.length - 1][k] = control[1][k];
			
			control[control.length - 1][k][3] = 1; 		//set flag for being on the edge of the array
		}
		
		//randomly instantiate some cells as true, and add all non-edge cells to the simulation list
		for (int i = 1; i < control.length - 1; i++){
			for (int k = 1; k < control[i].length - 1; k ++){
				
				if (Math.random() <= seed){
					setState(i, k, 1);

				}if (control[i][k][3] == 0){
					nextList.push(new OrderedPair(i, k));
				}
			}
		}
		
		//There are now both alive and dead cells, but non have neighborCounts other than 0, So count the neighbors of each cell in the entire array
		checkEveryNeighbor();
		
		//call the method that controls drawing of the first frame and will start the simulation
		//changeTheList();
		
		//GUI
		this.setPreferredSize(new Dimension(x * spacing, y * spacing));
		
	}
	
	
	/**
	 * This method pops every element out of changeList, adding adjacent locations to nextList, and relevant locations to drawList for redrawing
	 * 
	 */
	
	public void changeTheList(){
		/*
		 * Pops every element out of changeList, and withdraws the OrderedPair's stored x and y value. 
		 * Proceeds by adding all adjacent locations as well as itself to the list for next simulation, if it has not yet been added and is not an edge cell.
		 * To finish, The locations state is flipped, and neighborCounts are adjusted accordingly. Also adds cells that changed to the drawList.
		 * 
		 * Possible optimization would be to control drawing of cells in this method, eliminating the need for drawList completely.
		 *
		 */
		
		OrderedPair p;
		int x, y;
		while (changeList.empty() != true){
			
			p = changeList.pop();
			x = p.getX();
			y = p. getY();
			
			for (int i = x - 1; i <= x + 1; i++){ //if the element is changing, add only the surrounding cells that have not been added to the next checking stack
				for (int k = y - 1; k <= y + 1; k ++){

					if(control[i][k][2] == 0 && control [i][k][3] == 0){
						control[i][k][2] = 1;					//set seen flag to 1 (true)
						nextList.push(new OrderedPair(i, k));	//add the cell to the change list
					}
				}
			}
			if (control[x][y][0] == 1){
				
				control[x][y][4] = 0;
				drawList.push(new OrderedPair(x, y));
				setState(x, y , 0);
				decNeighbors(x, y);
			}else if (control [x][y][0] == 0){
				
				control[x][y][4] = 1;
				drawList.push(new OrderedPair(x, y));
				setState(x, y , 1);
				incNeighbors(x, y);
			}
		}
	}
	

	/**
	 * Method Runs through every location in nextList, and applying specific rules based upon current neighborCounts.
	 * if one of these locations needs to change state based upon current neighbors, it is added to the changes list.
	 * 
	 * 
	 */
	
	public void simulate(){

		repaint();
		OrderedPair p;
		int x, y;
		
		while (nextList.empty() != true){
			
			p = nextList.pop();
			x = p.getX();
			y = p.getY();
			
			control[x][y][2] = 0;
			
			if (control[x][y][0] == 1){
				
				//1. Any live cell with fewer than two live neighbors dies, as if caused by under-population.
					if (control[x][y][1] < 2){
						changeList.push(new OrderedPair(x, y));
						
					}
				//2. Any live cell with two or three live neighbors lives on to the next generation. Does not require any code

				//3. Any live cell with more than three live neighbors dies, as if by overcrowding.
					else if (control[x][y][1] > 3){
						changeList.push(new OrderedPair(x, y));
						
					}
			}else if (control[x][y][0] == 0){
				//4. Any dead cell with exactly three live neighbors becomes a live cell, as if by reproduction.
				if (control[x][y][1] == 3){
					changeList.push(new OrderedPair(x, y));
					
				}
				
			}else {System.out.println("ERROR: control[i][k][0] != 0 || 1: control [" + x + "][" + y + "][0] = " + control[x][y][0]);}
			
		}//end while
		changeTheList();
	}

	
	/**
	 * Takes x, and y input and changes the state of cell at x, y to the parameter set
	 * 
	 * Word of caution: Does not check for value of set to be only 0 or 1
	 * 
	 * @param x
	 * @param y
	 * @param set
	 */
	
	public void setState(int x, int y, int set){
		control[x][y][0] = set;
	}
	
	
	/**
	 * Checks all of the neighbors of a particular cell and counts the ones that are alive. 
	 * 
	 * @param x
	 * @param y
	 * @return number of alive neighboring cells
	 */
	
	public int checkNeighbors(int x, int y){
		int neighborCount = 0;
		for (int i = x - 1; i <= x + 1; i ++){
			for (int k = y - 1; k <= y + 1; k++){
				
				if (control[i][k][0] == 1){
					neighborCount++;
				}
			}
		}
		
		//if control[x][y][0] == 1, then above loop counted it as a neighbor, so this needs to be reversed
		if (control[x][y][0] == 1){ 
					neighborCount --;
		}
		
		return neighborCount;
	}
	
	
	/**
	 * Runs checkNeighbors on every location in the grid
	 * 
	 */
	
	public void checkEveryNeighbor(){
		
		for (int i = 1; i < control.length - 1; i ++){
			for (int k = 1; k < control[i].length - 1; k++){
				control[i][k][1] = checkNeighbors(i, k);
			}
		}
	}
	
	
	/**
	 * Takes an inputed location, and increments the neighbor count of the adjacent cells
	 * Will only operate on cells that are alive, and prints error message for invalid input
	 * 
	 * @param x
	 * @param y
	 */
	
	public void incNeighbors(int x, int y){
		if (control[x][y][0] == 1){
			for (int i = x - 1; i < x + 2; i++){
				for (int k = y - 1; k < y + 2; k ++){
					control[i][k][1]++;

					//Reverse the incrementation of its own neighborCount
					if ( (i == x) && (k == y)){
						control[x][y][1]--;
					}
				}
			}
		}else {System.out.println("control[x][y][0] isn't 1: it is = " + control[x][y][0]);}
	}
	
	
	/**
	 * Takes an inputed location, and decrements the neighbor count of the adjacent cells
	 * Will only operate on cells that are dead, and prints error message for invalid input
	 * 
	 * @param x
	 * @param y
	 */
	
	public void decNeighbors(int x, int y){
		if (control[x][y][0]==0){
			for (int i = x - 1; i < x + 2; i++){
				for (int k = y - 1; k < y + 2; k ++){
					control[i][k][1]--;
					
					//Reverse the decrementation of its own neighborCount
					if ((i == x) && (k == y)){
						control[x][y][1]++;
					}
				}
			}
		}else {System.out.println("control[x][y][0] isn't 0: it is = " + control[x][y][0]);}
	}
	
	
	
	/**
	 * repaint() called with every run of simulate(), calling this method and drawing all the cells that need to be updated. 
	 */
	
	public void paintComponent(Graphics g){
		
		OrderedPair p;
		int x, y;
		
		while (drawList.empty() != true){
			
			p = drawList.pop();
			x = p.getX();
			y = p.getY();
			
			if (control[x][y][4] == 1){
				g.setColor(Color.black);
				g.fillRect(x, y, spacing, spacing);
			}else{
				g.setColor(Color.white);
				g.fillRect(x, y, spacing, spacing);
			}
			
		}
	}

	
	
	
	
}
