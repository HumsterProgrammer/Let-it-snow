/*
@author DenisKochetkov
@ver 1.0
@link https://github.com/HumsterProgrammer/Let-it-snow
*/
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.RepaintManager;
import javax.swing.JOptionPane;

import java.util.ArrayList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

class Snow extends Component{
	private static int fps = 100; // maxFPS. don't change
	private static String path = "./"; // path to images
	
	private ArrayList<Particle> spisok = new ArrayList<Particle>(); // massive of particles
	private int[][] map; // mask with particles
	private int[][] map_mask; // mask
	
	private BufferedImage mask; // images
	private BufferedImage back;
	
	private int maxSize = -1;
	private int counter = 0;
	
	
	Snow(BufferedImage b, BufferedImage m){
		this.back = b;
		
		map_mask = new int[m.getHeight()][m.getWidth()];
		map = new int[m.getHeight()][m.getWidth()];
		for(int i = 0; i< m.getHeight(); i++){
			map_mask[i] = new int[m.getWidth()];
			map[i] = new int[m.getWidth()];
			for(int q = 0; q< m.getWidth();q++){
				if(m.getRGB(q,i) != -16777216){
					map_mask[i][q] = 1;
				}else{
					map_mask[i][q] = 0;
				}
			}
		}
		
	}
	public void setMaxCount(int m){this.maxSize = m;}
	public int getMaxCount(){return this.maxSize;}
	
	public void update(long dt){
		double delt = dt/1000.0;
		for(Particle i: spisok){
			i.update(delt, this.getHeight(), this.getWidth(), this.map);
		}
		for(int i =0; i< map.length; i++){
			map[i] = Arrays.copyOf(map_mask[i], map_mask[i].length);
		}
		for(Particle i: spisok){
			try{
				int x = (int)(i.getX());
				int y = (int)(i.getY());
				this.map[y][x] = 1;
				this.map[y][x+1] = 1;
				this.map[y+1][x] = 1;
				this.map[y+1][x+1] = 1;
			}catch(Exception e){}
		}
		if(Math.random()> 0.9){
			if(this.spisok.size() == maxSize && this.maxSize != -1){
				this.spisok.set(this.counter, new Particle(this.getWidth(), this.getHeight()+10));
				this.counter++;
				if(this.counter == maxSize) this.counter = 0;
			}else this.spisok.add(new Particle(this.getWidth(), -10));
		}
	}
	
	@Override
	public void paint(Graphics g){ // method of painting
		g.setColor(Color.black);
		g.fillRect(0,0,this.getWidth(), this.getHeight());
		g.drawImage(this.back, 0,0,null);
		g.setColor(Color.white);
		for(Particle i: spisok){
			g.fillRect((int)i.getX(), (int)i.getY(), 2,2);
		}
	}
	
	public static void main(String[] args){
		BufferedImage back;
		BufferedImage mask;
		try{
			back = ImageIO.read(new File(path+"back.png"));
			mask = ImageIO.read(new File(path+"mask.png"));
		}catch(Exception e){JOptionPane.showMessageDialog(null, "Sorry. ImageNotFound: back.png, mask.png", "ErrorMessage", JOptionPane.INFORMATION_MESSAGE); return;}
		
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(mask.getWidth()+10,mask.getHeight()+40);
		f.setResizable(false);
		
		Snow g = new Snow(back, mask);
		g.setBounds(0,0, mask.getWidth(), mask.getHeight());
		g.setMaxCount(10000); // setting maxCount
		f.add(g);
		
		f.setLayout(null);
		f.setVisible(true);
		
		long delt = 0; // main loop
		RepaintManager rm = new RepaintManager(); 
		while(true){
			long t1 = System.currentTimeMillis();
			g.update(delt);
			
			f.repaint();
			rm.paintDirtyRegions();
			delt = System.currentTimeMillis() - t1;
			try{
				Thread.currentThread().sleep(1000/fps - delt);
				delt = 1000/fps;
			}catch(Exception e){}
		}
	}
}
class Particle{ // class of snow particle
	
	private static double k = 1; // air resistance
	private static double g = 10; // free fall acceleration
	
	private static Vector v_wind = new Vector(5,0); // wind params
	private static double k_wind = 1;
	private static double time = 0;
	
	private Vector cord; // coordinate {x,y}
	private Vector v; // velocity {vx, vy}
	
	Particle( double width, double h){
		
		this.cord = new Vector(Math.random()*width, h + Math.random()*10);
		this.v = new Vector();
	}
	
	public void update(double delt, double h_floor, double w_floor, int[][] map){
		Vector f = new Vector(0,0);
		f.add(new Vector(0,g));
		f.add_with_mul(this.v, -k);
		double pos = this.cord.y/10*Math.PI;
		Vector t = new Vector(this.v_wind);
		t.mul(Math.sin(pos));
		f.add_with_mul(t, k_wind);
		
		
		this.v.add_with_mul(f, delt); // update velocity
		
		try{ // find collision
			boolean teta = false;
			if(v.x > 0){
				int sost = map[(int)(this.cord.y)][(int)(this.cord.x)+2];
				if(sost == 1) this.v.x = 0;
			}else{
				int sost = map[(int)(this.cord.y)][(int)(this.cord.x)-1];
				if(sost == 1) this.v.x = 0;
			}
			if(v.y > 0){
				int sost = map[(int)(this.cord.y)+2][(int)(this.cord.x)];
				if(sost == 1){ this.v.y = 0; teta = true;}
			}else{
				int sost = map[(int)(this.cord.y)-1][(int)(this.cord.x)];
				if(sost == 1){ this.v.y = 0; teta = true;}
			}
			if(teta && this.v.x == 0){
				int sost = map[(int)(this.cord.y)+2][(int)(this.cord.x-1)];
				if(sost == 0){
					this.cord.x--;
					this.v.x = 0;
				}else{
					sost = map[(int)(this.cord.y)+2][(int)(this.cord.x+2)];
					if(sost == 0){
						this.cord.x += 2;
						this.v.x = 0;
					}
				}
			}
		}catch(Exception e){}
		this.cord.add_with_mul(this.v, delt);
	}
	public static void updateWind(double delt){
		time += delt;
		k_wind = Math.cos(time);
	}
	public double getX(){return this.cord.x;}
	public double getY(){return this.cord.y;}
}

class Vector{ // class of Vector
	public double x = 0;
	public double y = 0;
	
	Vector(double x, double y){
		this.x = x;
		this.y = y;
	}
	Vector(){this.x = 0; this. y = 0;}
	Vector(Vector v){this.x = v.x; this.y = v.y;}
	
	public void add(Vector v){
		this.x += v.x;
		this.y += v.y;
	}
	public void add_with_mul(Vector v, double k){
		this.x += v.x*k;
		this.y += v.y*k;
	}
	public void mul(double k){
		this.x *= k;
		this.y *= k;
	}
}
