import javax.swing.*;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;

public class SudokuPlayer implements Runnable, ActionListener {

    // final values must be assigned in vals[][]
    int[][] vals = new int[9][9];
    Board board = null;



    /// --- AC-3 Constraint Satisfication --- ///


    // Useful but not required Data-Structures;
    ArrayList<Integer>[] globalDomains = new ArrayList[81];
    ArrayList<Integer>[] neighbors = new ArrayList[81];
    Queue<Arc> globalQueue = new LinkedList<Arc>();
        

	/*
	 * This method sets up the data structures and the initial global constraints
	 * (by calling allDiff()) and makes the initial call to backtrack().
	 * You should not change this method header.
 	 */
    private final void AC3Init(){
        //Do NOT remove these lines (required for the GUI)
        board.Clear();
		recursions = 0;
        /**
         *  YOUR CODE HERE:
         *  Create Data structures ( or populate the ones defined above ).
         *  These will be the data structures necessary for AC-3.
         **/

        int index = 0; //this represents the 1D index used in globalDomains: if index=0, we are at [0][0], if index = 1, we are at [0][1], etc.
        for (int row = 0; row < vals.length; row++){ //iterate over every square of the board
            for(int col = 0; col < vals.length; col++){
                if (vals[row][col] != 0){ //if the cell has a predefined value, it can only have its value as its domain
                    globalDomains[index] = new ArrayList<>();
                    globalDomains[index].add(vals[row][col]);
                }
                else{ //if the cell is empty
                    //add the numbers 1-9 into the domain of the cell we are iterating over.
                    //I'm sure there is a better way to do this, but this works fine.
                    globalDomains[index] = new ArrayList<>();
                    globalDomains[index].add(1);
                    globalDomains[index].add(2);
                    globalDomains[index].add(3);
                    globalDomains[index].add(4);
                    globalDomains[index].add(5);
                    globalDomains[index].add(6);
                    globalDomains[index].add(7);
                    globalDomains[index].add(8);
                    globalDomains[index].add(9);
                }
                index++;
            }
        }


        //filling out the row neighbors
        int[] rowNeighbors =new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}; //the first row
        for (int row = 0; row < vals.length; row++){
            for(int i = 0; i < rowNeighbors.length; i++){ //this for loop calculates the integer values of the next row
                if (row!=0) { //we don't want to do this calculation on the first iteration
                    rowNeighbors[i] = rowNeighbors[i] + 9;
                }
            }
            allDiff(rowNeighbors);
        }
        //filling out the column neighbors
        int[] colNeighbors =new int[]{0, 9, 18, 27, 36, 45, 54, 63, 72}; //the first column
        for (int col = 0; col < vals.length; col++){
            for(int i = 0; i < colNeighbors.length; i++){ //this for loop calculates the integer values of the next column
                if(col!=0){
                    colNeighbors[i] = colNeighbors[i] + 1;
                }
            }
            allDiff(colNeighbors);
        }


        //this block calculates the box neighbors. I hardcoded the boxes below
        int[][] boxes = new int[9][];
        //these are the 3x3 squares where each tile is a neighbor of the other tiles in the list
        boxes[0] = new int[]{0, 1, 2, 9, 10, 11, 18, 19, 20};
        boxes[1] = new int[]{3, 4, 5, 12, 13, 14, 21, 22, 23};
        boxes[2] = new int[]{6, 7, 8, 15, 16, 17, 24, 25, 26};
        boxes[3] = new int[]{27, 28, 29, 36, 37, 38, 45, 46, 47};
        boxes[4] = new int[]{30, 31, 32, 39, 40, 41, 48, 49, 50};
        boxes[5] = new int[]{33, 34, 35, 42, 43, 44, 51, 52, 53};
        boxes[6] = new int[]{54, 55, 56, 63, 64, 65, 72, 73, 74};
        boxes[7] = new int[]{57, 58, 59, 66, 67, 68, 75, 76, 77};
        boxes[8] = new int[]{60, 61, 62, 69, 70, 71, 78, 79, 80};

        for (int i = 0; i < boxes.length; i++) {
            int[] currentBox = boxes[i];
            allDiff(currentBox); //calling allDiff on each box.
        }
        // Initial call to backtrack() on cell 0 (top left)
        boolean success = backtrack(0,globalDomains);

        // Prints evaluation of run
        Finished(success);

    }

    

    /*
     *  This method defines constraints between a set of variables.
     *  Refer to the book for more details. You may change this method header.
     */
    private final void allDiff(int[] all){
        for(int i = 0; i < all.length; i++){
            if(neighbors[all[i]] == null){ //we only want to initialze a new arraylist if we haven't already initialized one
                neighbors[all[i]] = new ArrayList<>(); //initialze the arraylist for the neighbors cell
            }
            for(int j = 0; j < all.length; j++){
                if (all[i] != all[j]){
                    if (!neighbors[all[i]].contains(all[j])){ //only add the neighbor to neighbors and the arc to globalQueue if the neighbor/arc doesn't already exist
                        neighbors[all[i]].add(all[j]); //add j to i's neighbors
                        globalQueue.add(new Arc(all[i], all[j])); //add the arc (i,j) to globalQueue
                    }
                }
            }
        }
    }


    /*
     * This is the backtracking algorithm. If you change this method header, you will have
     * to update the calls to this method. 
     */
    private final boolean backtrack(int cell, ArrayList<Integer>[] Domains) {

    	//Do NOT remove
    	recursions +=1;
    	
    	if (cell > 80){
            return true;
        }
        if (vals[cell/9][cell%9] != 0){ //this math allows us to index the 2d array using the cell integer
            backtrack(cell+1, globalDomains); //this cell has a predetermined value, so we don't need to run AC3 on it
        }

        /*if (recursions == 1){
            ArrayList<Integer> currentDomain = new ArrayList<Integer>();
            for (int i = 0; i < globalDomains[cell].size(); i++){ //copy the domain of the current cell into a separate array, so we don't have to directly modify the master list of that cells domain
                currentDomain.add(globalDomains[cell].get(i)); //I was warned not to use ArrayList.clone()
            }
            for (int i = 0; i < currentDomain.size(); i++){
                globalDomains[cell].clear();
                globalDomains[cell].add(currentDomain.get(i));
                if(backtrack(cell+1, globalDomains)){
                    return true;
                }
                else{
                    continue;
                }
            }
        }*/
        //if((!(AC3(globalDomains))) && recursions != 1){
        if((!AC3(globalDomains))){ //if the AC3 search returns false, then backtrack returns false.
            return false;
        }
        else{
            ArrayList<Integer> currentDomain = new ArrayList<Integer>();
            for (int i = 0; i < globalDomains[cell].size(); i++){ //copy the domain of the current cell into a separate array, so we don't have to directly modify the master list of that cells domain
                currentDomain.add(globalDomains[cell].get(i)); //I was warned not to use ArrayList.clone()
            }
            for (int i = 0; i < currentDomain.size(); i++){
                globalDomains[cell].clear();
                globalDomains[cell].add(currentDomain.get(i));
                if(backtrack(cell+1, globalDomains)){
                    return true;
                }
                else{
                    continue;
                }
            }
        }
        return false;

    }

    
    /*
     * This is the actual AC3 Algorithm. You may change this method header.
     */
    private final boolean AC3(ArrayList<Integer>[] Domains) {
        while(!(globalQueue.isEmpty())){
            Arc curArc = globalQueue.remove();
           /* System.out.println("Current arc is: "+ curArc.toString());
            System.out.println("Current domain is: " + globalDomains[curArc.Xi].toString());*/
            if (Revise(curArc, globalDomains)){
                if(globalDomains[curArc.Xi].isEmpty()){
                    return false;
                }
                for(int neighbor: neighbors[curArc.Xi]){
                    if (neighbor != curArc.Xj){
                        globalQueue.add(new Arc (curArc.Xi,neighbor));
                    }
                }
            }
        }
    	
		return true;
    }
    
    

    /*
     * This is the Revise() procedure. You may change this method header.
     */
     private final boolean Revise(Arc t, ArrayList<Integer>[] Domains){
         boolean revised = false;
         ArrayList<Integer> cell1 = globalDomains[t.Xi];
         ArrayList<Integer> cell2 = globalDomains[t.Xj];
         ArrayList<Integer> toRemove = new ArrayList<Integer>();

         /*if(cell1.equals(cell2)){ //if the domains are the same, we can't revise cell1s domain
             return false;
         }*/


         for (int i = cell1.size() - 1; i >= 0; i--) { // for each vk from Dk
             if (cell2.contains(cell1.get(i))) { // if (vk, Dm) doesnt satisfy the constraint
                 cell1.remove(cell1.get(i)); // delete (vk, Dk)
                 revised = true;
             }
         }

         /*for (Integer val : cell1) {
             if (cell2.contains(val)) {
                 toRemove.add(val);
                 revised = true;
             }
         }
         globalDomains[t.Xi].removeAll(toRemove);*/
         return revised;
 	}

   
     /*
      * This is where you will write your custom solver. 
      * You should not change this method header.
      */
    private final void customSolver(){
    	   
    	   //set 'success' to true if a successful board    
    	   //is found and false otherwise.
    	   boolean success = true; 
		   board.Clear();
	        
	        System.out.println("Running custom algorithm");

	        //-- Your Code Here --
	 
	        
		   Finished(success);
    	       
    	}


    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    public final boolean valid(int x, int y, int val){
        
        if (vals[x][y] == val)
            return true;
        if (rowContains(x,val))
            return false;
        if (colContains(y,val))
            return false;
        if (blockContains(x,y,val))
            return false;
        return true;
    }

    public final boolean blockContains(int x, int y, int val){
        int block_x = x / 3;
        int block_y = y / 3;
        for(int r = (block_x)*3; r < (block_x+1)*3; r++){
            for(int c = (block_y)*3; c < (block_y+1)*3; c++){
                if (vals[r][c] == val)
                    return true;
            }
        }
        return false;
    }

    public final boolean colContains(int c, int val){
        for (int r = 0; r < 9; r++){
            if (vals[r][c] == val)
                return true;
        }
        return false;
    }

    public final boolean rowContains(int r, int val) {
        for (int c = 0; c < 9; c++)
        {
            if(vals[r][c] == val)
                return true;
        }
        return false;
    }

    private void CheckSolution() {
        // If played by hand, need to grab vals
        board.updateVals(vals);

        /*for(int i=0; i<9; i++){
	        for(int j=0; j<9; j++)
	        	System.out.print(vals[i][j]+" ");
	        System.out.println();
        }*/
        
        for (int v = 1; v <= 9; v++){
            // Every row is valid
            for (int r = 0; r < 9; r++)
            {
                if (!rowContains(r,v))
                {
                    board.showMessage("Value "+v+" missing from row: " + (r+1));// + " val: " + v);
                    return;
                }
            }
            // Every column is valid
            for (int c = 0; c < 9; c++)
            {
                if (!colContains(c,v))
                {
                    board.showMessage("Value "+v+" missing from column: " + (c+1));// + " val: " + v);
                    return;
                }
            }
            // Every block is valid
            for (int r = 0; r < 3; r++){
                for (int c = 0; c < 3; c++){
                    if(!blockContains(r, c, v))
                    {
                        return;
                    }
                }
            }
        }
        board.showMessage("Success!");
    }

    

    /// ---- GUI + APP Code --- ////
    /// ----   DO NOT EDIT  --- ////
    enum algorithm {
        AC3, Custom
    }
    class Arc implements Comparable<Object>{
        int Xi, Xj;
        public Arc(int cell_i, int cell_j){
            if (cell_i == cell_j){
                try {
                    throw new Exception(cell_i+ "=" + cell_j);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            Xi = cell_i;      Xj = cell_j;
        }

        public int compareTo(Object o){
            return this.toString().compareTo(o.toString());
        }

        public String toString(){
            return "(" + Xi + "," + Xj + ")";
        }
    }

    enum difficulty {
        easy, medium, hard, random
    }

    public void actionPerformed(ActionEvent e){
        String label = ((JButton)e.getSource()).getText();
        if (label.equals("AC-3"))
        	AC3Init();
        else if (label.equals("Clear"))
            board.Clear();
        else if (label.equals("Check"))
            CheckSolution();
            //added
        else if(label.equals("Custom"))
            customSolver();
    }

    public void run() {
        board = new Board(gui,this);
        
        long start=0, end=0;
       
        while(!initialize());
        if (gui)
            board.initVals(vals);
        else {
            board.writeVals();
            System.out.println("Algorithm: " + alg);
            switch(alg) {
                default:
                case AC3:
                	start = System.currentTimeMillis();
                	AC3Init();
                    end = System.currentTimeMillis();
                    break;
                case Custom: //added
                	start = System.currentTimeMillis();
                	customSolver();
                	end = System.currentTimeMillis();
                    break;
            }
            
            CheckSolution();
            
            if(!gui)
            	System.out.println("time to run: "+(end-start));
        }
    }

    public final boolean initialize(){
        switch(level) {
            case easy:
                vals[0] = new int[] {0,0,0,1,3,0,0,0,0};
                vals[1] = new int[] {7,0,0,0,4,2,0,8,3};
                vals[2] = new int[] {8,0,0,0,0,0,0,4,0};
                vals[3] = new int[] {0,6,0,0,8,4,0,3,9};
                vals[4] = new int[] {0,0,0,0,0,0,0,0,0};
                vals[5] = new int[] {9,8,0,3,6,0,0,5,0};
                vals[6] = new int[] {0,1,0,0,0,0,0,0,4};
                vals[7] = new int[] {3,4,0,5,2,0,0,0,8};
                vals[8] = new int[] {0,0,0,0,7,3,0,0,0};
                break;
            case medium:
                vals[0] = new int[] {0,4,0,0,9,8,0,0,5};
                vals[1] = new int[] {0,0,0,4,0,0,6,0,8};
                vals[2] = new int[] {0,5,0,0,0,0,0,0,0};
                vals[3] = new int[] {7,0,1,0,0,9,0,2,0};
                vals[4] = new int[] {0,0,0,0,8,0,0,0,0};
                vals[5] = new int[] {0,9,0,6,0,0,3,0,1};
                vals[6] = new int[] {0,0,0,0,0,0,0,7,0};
                vals[7] = new int[] {6,0,2,0,0,7,0,0,0};
                vals[8] = new int[] {3,0,0,8,4,0,0,6,0};
                break;
            case hard:
            	vals[0] = new int[] {1,2,0,4,0,0,3,0,0};
            	vals[1] = new int[] {3,0,0,0,1,0,0,5,0};  
            	vals[2] = new int[] {0,0,6,0,0,0,1,0,0};  
            	vals[3] = new int[] {7,0,0,0,9,0,0,0,0};    
            	vals[4] = new int[] {0,4,0,6,0,3,0,0,0};    
            	vals[5] = new int[] {0,0,3,0,0,2,0,0,0};    
            	vals[6] = new int[] {5,0,0,0,8,0,7,0,0};    
            	vals[7] = new int[] {0,0,7,0,0,0,0,0,5};    
            	vals[8] = new int[] {0,0,0,0,0,0,0,9,8};  
                break;
            case random:
            default:
                ArrayList<Integer> preset = new ArrayList<Integer>();
                while (preset.size() < numCells)
                {
                    int r = rand.nextInt(81);
                    if (!preset.contains(r))
                    {
                        preset.add(r);
                        int x = r / 9;
                        int y = r % 9;
                        if (!assignRandomValue(x, y))
                            return false;
                    }
                }
                break;
        }
        return true;
    }

    public final boolean assignRandomValue(int x, int y){
        ArrayList<Integer> pval = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));

        while(!pval.isEmpty()){
            int ind = rand.nextInt(pval.size());
            int i = pval.get(ind);
            if (valid(x,y,i)) {
                vals[x][y] = i;
                return true;
            } else
                pval.remove(ind);
        }
        System.err.println("No valid moves exist.  Recreating board.");
        for (int r = 0; r < 9; r++){
            for(int c=0;c<9;c++){
                vals[r][c] = 0;
            }    }
        return false;
    }

    private void Finished(boolean success){
    	
    	if(success) {
            board.writeVals();
            //board.showMessage("Solved in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
            board.showMessage("Solved in " + myformat.format(recursions) + " recursive ops");

    	} else {
            //board.showMessage("No valid configuration found in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
        	board.showMessage("No valid configuration found");
        }
         recursions = 0;
       
    }
 
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        System.out.println("Gui? y or n ");
        char g=scan.nextLine().charAt(0);

        if (g=='n')
            gui = false;
        else
            gui = true;
        
        if(gui) {
        	System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

	        char c = '*';

	        while (c != 'e' && c != 'm' && c != 'n' && c != 'h' && c != 'r') {
	        	c = scan.nextLine().charAt(0);
	            if(c=='e')
	                level = difficulty.valueOf("easy");
	            else if(c=='m')
	                level = difficulty.valueOf("medium");
	            else if(c=='h')
	                level = difficulty.valueOf("hard");
	            else if(c=='r')
	                level = difficulty.valueOf("random");
	            else{
	                System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
	            }
	        }
	        
	        SudokuPlayer app = new SudokuPlayer();
	        app.run();
	        
        }
        else { //no gui
        	
        	boolean again = true;
        
        	int numiters = 0;
        	long starttime, endtime, totaltime=0;
        
        	while(again) {
        
        		numiters++;
        		System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

        		char c = '*';

		        while (c != 'e' && c != 'm' && c != 'n' && c != 'h' && c != 'r') {
		        	c = scan.nextLine().charAt(0);
		            if(c=='e')
		                level = difficulty.valueOf("easy");
		            else if(c=='m')
		                level = difficulty.valueOf("medium");
		            else if(c=='h')
		                level = difficulty.valueOf("hard");
		            else if(c=='r')
		                level = difficulty.valueOf("random");
		            else{
		                System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
		            }
	            
		        }

	            System.out.println("Algorithm? AC3 (1) or Custom (2)");
	            if(scan.nextInt()==1)
	                alg = algorithm.valueOf("AC3");
	            else
	                alg = algorithm.valueOf("Custom");
	        
	
		        SudokuPlayer app = new SudokuPlayer();
		       
		        starttime = System.currentTimeMillis();
		        
		        app.run();
		        
		        endtime = System.currentTimeMillis();
		        
		        totaltime += (endtime-starttime);
	        
	       
	        	System.out.println("quit(0), run again(1)");
	        	if (scan.nextInt()==1)
	        		again=true;
	        	else
	        		again=false;
	        
	        	scan.nextLine();
	        
        	}
        
        	System.out.println("average time over "+numiters+" iterations: "+(totaltime/numiters));
        }
    
        
        
        scan.close();
    }



    class Board {
        GUI G = null;
        boolean gui = true;

        public Board(boolean X, SudokuPlayer s) {
            gui = X;
            if (gui)
                G = new GUI(s);
        }

        public void initVals(int[][] vals){
            G.initVals(vals);
        }

        public void writeVals(){
            if (gui)
                G.writeVals();
            else {
                for (int r = 0; r < 9; r++) {
                    if (r % 3 == 0)
                        System.out.println(" ----------------------------");
                    for (int c = 0; c < 9; c++) {
                        if (c % 3 == 0)
                            System.out.print (" | ");
                        if (vals[r][c] != 0) {
                            System.out.print(vals[r][c] + " ");
                        } else {
                            System.out.print("_ ");
                        }
                    }
                    System.out.println(" | ");
                }
                System.out.println(" ----------------------------");
            }
        }

        public void Clear(){
            if(gui)
                G.clear();
        }

        public void showMessage(String msg) {
            if (gui)
                G.showMessage(msg);
            System.out.println(msg);
        }

        public void updateVals(int[][] vals){
            if (gui)
                G.updateVals(vals);
        }

    }

    class GUI {
        // ---- Graphics ---- //
        int size = 40;
        JFrame mainFrame = null;
        JTextField[][] cells;
        JPanel[][] blocks;

        public void initVals(int[][] vals){
            // Mark in gray as fixed
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (vals[r][c] != 0) {
                        cells[r][c].setText(vals[r][c] + "");
                        cells[r][c].setEditable(false);
                        cells[r][c].setBackground(Color.lightGray);
                    }
                }
            }
        }

        public void showMessage(String msg){
            JOptionPane.showMessageDialog(null,
                    msg,"Message",JOptionPane.INFORMATION_MESSAGE);
        }

        public void updateVals(int[][] vals) {

           // System.out.println("calling update");
            for (int r = 0; r < 9; r++) {
                for (int c=0; c < 9; c++) {
                    try {
                        vals[r][c] = Integer.parseInt(cells[r][c].getText());
                    } catch (java.lang.NumberFormatException e) {
                        System.out.println("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        showMessage("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        return;
                    }
                }
            }
        }

        public void clear() {
            for (int r = 0; r < 9; r++){
                for (int c = 0; c < 9; c++){
                    if (cells[r][c].isEditable())
                    {
                        cells[r][c].setText("");
                        vals[r][c] = 0;
                    } else {
                        cells[r][c].setText("" + vals[r][c]);
                    }
                }
            }
        }

        public void writeVals(){
            for (int r=0;r<9;r++){
                for(int c=0; c<9; c++){
                    cells[r][c].setText(vals[r][c] + "");
                }   }
        }

        public GUI(SudokuPlayer s){

            mainFrame = new javax.swing.JFrame();
            mainFrame.setLayout(new BorderLayout());
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel gamePanel = new javax.swing.JPanel();
            gamePanel.setBackground(Color.black);
            mainFrame.add(gamePanel, BorderLayout.NORTH);
            gamePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            gamePanel.setLayout(new GridLayout(3,3,3,3));

            blocks = new JPanel[3][3];
            for (int i = 0; i < 3; i++){
                for(int j =2 ;j>=0 ;j--){
                    blocks[i][j] = new JPanel();
                    blocks[i][j].setLayout(new GridLayout(3,3));
                    gamePanel.add(blocks[i][j]);
                }
            }

            cells = new JTextField[9][9];
            for (int cell = 0; cell < 81; cell++){
                int i = cell / 9;
                int j = cell % 9;
                cells[i][j] = new JTextField();
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setSize(new java.awt.Dimension(size, size));
                cells[i][j].setPreferredSize(new java.awt.Dimension(size, size));
                cells[i][j].setMinimumSize(new java.awt.Dimension(size, size));
                blocks[i/3][j/3].add(cells[i][j]);
            }

            JPanel buttonPanel = new JPanel(new FlowLayout());
            mainFrame.add(buttonPanel, BorderLayout.SOUTH);
            //JButton DFS_Button = new JButton("DFS");
            //DFS_Button.addActionListener(s);
            JButton AC3_Button = new JButton("AC-3");
            AC3_Button.addActionListener(s);
            JButton Clear_Button = new JButton("Clear");
            Clear_Button.addActionListener(s);
            JButton Check_Button = new JButton("Check");
            Check_Button.addActionListener(s);
            //buttonPanel.add(DFS_Button);
            JButton Custom_Button = new JButton("Custom");
            Custom_Button.addActionListener(s);
            //added
            buttonPanel.add(AC3_Button);
            buttonPanel.add(Custom_Button);
            buttonPanel.add(Clear_Button);
            buttonPanel.add(Check_Button);






            mainFrame.pack();
            mainFrame.setVisible(true);

        }
    }

    Random rand = new Random();

    // ----- Helper ---- //
    static algorithm alg = algorithm.AC3;
    static difficulty level = difficulty.easy;
    static boolean gui = true;
    static int numCells = 15;
    static DecimalFormat myformat = new DecimalFormat("###,###");
    
    //For printing
	static int recursions;
}




