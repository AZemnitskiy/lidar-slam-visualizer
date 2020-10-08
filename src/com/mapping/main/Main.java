package com.mapping.main;

import com.fazecast.jSerialComm.SerialPort;
import com.mapping.main.lidar.XV11Lidar;

import javax.swing.JFrame;

import java.io.IOException;

public class Main extends JFrame implements XV11Lidar.XV11LidarEventListener
{
	private Map map;
	private RANSAC ransac;
	private int lastAngle;
	private XV11Lidar lidar;

	public Main(int x, int y) {
		ransac = new RANSAC(new LineModel());
		map = new Map(x, y, this);
		this.add(map);
		this.setLocation(0,0);
		this.setResizable(false);
		this.pack();

		SerialPort[] ports = SerialPort.getCommPorts();
		SerialPort comPort = ports[4];

		comPort.setNumDataBits(8);
		comPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
		comPort.setParity(SerialPort.NO_PARITY);
		comPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
		comPort.setBaudRate(115200);
		comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 1000);
		comPort.openPort();

		this.lidar = new XV11Lidar(comPort.getInputStream(), this);
	}

	public static void main(String[] argv) throws IOException {

		Main m = new Main(1280,1920);
		m.setLocationRelativeTo(null);
		m.setExtendedState(JFrame.MAXIMIZED_BOTH);
		m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		m.setVisible(true);
		m.refresh();
		//m.testLines();
	}

	public void valueUpdated(int dist, int angle, int quality) {
		if (dist > 0) {
			this.map.addPoint(dist, angle, quality);
		}
		else {
			this.map.deletePoint(angle);
		}

		if(lastAngle > angle)
		{
			this.repaint();
		}
		lastAngle = angle;
	}

	public void rpmUpdated(double rpm) {
		//System.out.println("RPM: " + motor_rpm);
	}

	public RANSAC getRansac()
	{
		return this.ransac;
	}


	public void refresh() throws IOException {
		ransac.start();
		lidar.read();
	}

//	/*
//	 * The following are just various test functions
//	 */
//	public void testLines()
//	{
//		//this.serial.close();
//		int angle = 0;
//		ransac.start();
//		int points = 181/8;
//		Random r = new Random();
//		r.setSeed(System.currentTimeMillis());
//		int m = r.nextInt(9)+1;
//		int b = r.nextInt(10);
//		for(int j = -points; j < points; j++, angle++)
//		{
//			int y = m*j + b;
//			this.map.addPoint(new Point(j,y), angle);
//			this.repaint();
//		}
//		boolean right = true;
//		int vol = 5;
//		while(true)
//		{
//			for(int i = 0; i < Map.points.length; i++)
//			{
//				if(Map.points[i] == null)
//				{
//					break;
//				}
//				if(Map.points[i].x >= this.map.getW()-500 && right)
//				{
//					vol = -vol;
//					right = false;
//					System.out.println("Direction Switched to Left "+vol);
//				}
//				else if(Map.points[i].x <= -(this.map.getW()-500) && !right)
//				{
//					right = true;
//					vol = -vol;
//					System.out.println("Direction Switched to right "+vol);
//				}
//				Map.points[i].x += vol;
//			}
//			this.repaint();
//			try
//			{
//				Thread.sleep(10);
//			}catch(Exception e){}
//		}
//	}


}
