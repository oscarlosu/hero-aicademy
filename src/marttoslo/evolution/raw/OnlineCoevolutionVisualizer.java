package marttoslo.evolution.raw;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

import action.Action;
import ui.UI;

public class OnlineCoevolutionVisualizer extends JComponent implements KeyListener  {
	static int y = 0;
	
	public UI ui;
	public boolean rendering;
	public boolean p1;
	
	private JFrame frame;
	private int width;
	private int height;
	
	private int div;

	private OnlineCoevolution rolling;
	private List<Point> points;
	private boolean control;
	
	public OnlineCoevolutionVisualizer(UI ui, OnlineCoevolution rolling) {
		super();
		this.ui = ui;
		this.rolling = rolling;
		this.rendering = false;
		frame = new JFrame();
		frame.addKeyListener(this);
		width = 320;
		height = 110;
		frame.setLocation(705, y);
		y += height*1.2;
		div = 10;
		frame.setSize(width, height+32);
		frame.setTitle("Evolution");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(this);
		frame.setVisible(true);
		this.points = new ArrayList<Point>();
		this.control = false;
	}
	
	public void update(){
		rendering = true;
		points.clear();
		
		try {
				
			double xprog;
			int x;
			int y;
			double min = Collections.min(rolling.hostFitnesses.values());
			double max = Collections.max(rolling.hostFitnesses.values());
			double val;
			List<Integer> keys = new ArrayList<Integer>();
			keys.addAll(rolling.hostFitnesses.keySet());
			Collections.sort(keys);
			
			for(int gen : keys){
				xprog =  ((double)gen)/((double)rolling.hostFitnesses.size());
				x = (int) (div + (((double)(width-div-div)) * xprog));
				val = rolling.hostFitnesses.get(gen);
				val = (val - min) / (max -min);
				y = (int) ((height-div) - val * (height-div-div));
				synchronized (this) {
					points.add(new Point(x,y));
				}
				if (rolling.bestHostActions.size() > gen)
				{
					List<Action> bestHost = rolling.bestHostActions.get(gen);
					List<Action> bestParasite = rolling.bestParasiteActions.get(gen);
					ui.setActionLayer(bestHost, bestParasite);
				}
					
				if (!control){
					repaint();
					ui.repaint();
					//Thread.sleep(5);
				}
			}

			repaint();
			ui.repaint();
			Thread.sleep(20);			
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ui.actionLayer.clear();
		rendering = false;
		
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(new Color(220,220,220));
		g.fillRect(0, 0, width*2, height*2);
		
		g.setColor(new Color(20, 20, 20));
		g.drawLine(div, div, div, height-div);
		g.drawLine(div, height-div, width-div, height-div);
		
		g.setColor(Color.RED);
		
		/*
		if (p1)
			g.setColor(Color.RED);
		else
			g.setColor(Color.BLUE);
		*/
		int lastX = -100;
		int lastY = -100;
		int i = 0;
		synchronized (this) {
			for(Point point : points){
				
				if (i > 0)
					g.drawLine(lastX, lastY, point.x, point.y);
				
				lastX = point.x;
				lastY = point.y;
				i++;
				
			}
		}
		
		g.setColor(new Color(200, 200, 200));
		g.drawOval(lastX-5, lastY-5, 10, 10);
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL){
			control = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL){
			control = false;
		}
	}
}
