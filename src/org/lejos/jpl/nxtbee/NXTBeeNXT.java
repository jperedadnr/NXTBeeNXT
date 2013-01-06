package org.lejos.jpl.nxtbee;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.addon.NXTBee;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.util.Delay;
import org.lejos.jpl.nxtbee.behaviors.DetectWall;
import org.lejos.jpl.nxtbee.behaviors.DriveForward;
import org.lejos.jpl.nxtbee.behaviors.Remote;
import org.lejos.jpl.nxtbee.model.Brick;


/**
 * Demonstration of the Behavior subsumption classes.
 * 
 * Requires a wheeled vehicle with two independently controlled
 * motors connected to motor ports A and C, and 
 * a touch sensor connected to sensor  port 1 and
 * an ultrasonic sensor connected to port 3;
 * 
 * @author Brian Bagnall and Lawrie Griffiths, modified by Roger Glassey
 * 
 * Modified by Jose Pereda on 27-dic-2012
 *
 */
public class NXTBeeNXT
{
  private Arbitrator arbitrator;
  
  public NXTBeeNXT(DataInputStream dis)
  {
    
    Behavior b1 = (Behavior) new DriveForward(); // Low priority
    Behavior b2 = (Behavior) new Remote(dis); // Middle priority
    Behavior b3 = (Behavior) new DetectWall();   // High priority
    Behavior[] behaviorList ={b1, b2, b3};
    
    /*
     * calls takeControl() from each behavior. Only the action of the highest priority 
     * behavior which wants to take control is called. The rest are suppressed
     * If no one wants control it stops.
     * It will stop also if STOP (P) is sent from PC/Raspi
     */
    arbitrator = new Arbitrator(behaviorList,true);
    
  }
  
  public void start(){
    LCD.clearDisplay();
    LCD.drawString("Bumper Car START",0,1);
    arbitrator.start();
  }
  
  public static void main(String[] args) {
        
        NXTBee nb = new NXTBee(9600, true, true);

        /*
         * Start polling the RS485 port
         */
        Thread t = new Thread(nb);
        t.setDaemon(true);
        t.start();

        /*
         * Set the DataOutputStream for writting from the NXT via NXTBee to the PC/Raspi
         */
        Brick.getInstance().setDataOutputStream(new DataOutputStream(nb.getOutputStream()));

        
        /*
         * Set the DataInputStream for reading from the PC/Raspi via NXTBee
         */
        InputStream is = nb.getInputStream();		
        DataInputStream dis = new DataInputStream(is);
        
        Delay.msDelay(1000);

        LCD.clear();
        LCD.drawString("NXTBee waiting...", 0, 0);
        
        /*
         * Wait for orders of START (S) or QUIT (Q)
         */
        byte[] b = new byte[20];
        try {
            while(Button.ENTER.isUp()){

                if(dis.available() > 0) {
                    dis.read(b);
                    String s = new String(b);
                    if(s.startsWith(Brick.START)){
                        /*
                         * Define behaviors and arbitrator
                         */
                        NXTBeeNXT bumper=new NXTBeeNXT(dis);
                        /* 
                         * START the arbitrator, in AUTO mode
                         */
                        Brick.getInstance().setBehState(Brick.state.FORWARD);
                        bumper.start();
                    } else if(s.startsWith(Brick.QUIT)){
                        /*
                         * Quit the application
                         */
                        System.exit(1);
                    }
                }
                Delay.msDelay(1000);
            }
        } catch(Exception e) {  
        } finally {
            try {
                if(dis!=null){
                    dis.close();
                }
                if(is!=null){
                    is.close();
                }
            } catch (IOException ex) {}
        }
    }
}



