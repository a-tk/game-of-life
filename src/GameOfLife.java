import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.Timer;


public class GameOfLife extends JFrame{

	private static CellOverlord f;
	private static double simNum;
	
	public static void main(String[] args) {

        if (args.length != 3){
            System.out.println("Usage: <x height> <y height> <density> ");
            return;
        }

		simNum = Double.parseDouble(args[2]);
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);

		GameOfLife g = new GameOfLife(x, y, simNum, "Conways Game Of Life");

	}
	
	public GameOfLife(int x, int y, double seed, String title){
		
		super(title);
		f = new CellOverlord(x, y, simNum);
		this.add(f);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		
		startAnimation();
		this.setVisible(true);
	}
	
	private static void startAnimation()
	{
		
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				if(true){
					f.simulate();
					
					//f.repaint();
				
				}
			}
		};
		new Timer(1, taskPerformer).start();
	}
	

}
