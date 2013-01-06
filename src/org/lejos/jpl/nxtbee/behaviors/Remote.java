package org.lejos.jpl.nxtbee.behaviors;

import java.io.DataInputStream;
import lejos.nxt.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.util.Delay;
import org.lejos.jpl.nxtbee.model.Brick;

/**
 * Behavior with higher priority than DriveForward, and lower than DetectWall
 * Stop or Manual mode will stop DriveForward. Orders in manual mode will be cancelled
 * in case osbtacle detection. After the obstacle is avoided, the robot returns 
 * to auto mode
 * 
 * @author Jose Pereda Llamas
 * Created on 27-dic-2012 - 11:27:26
 */
public class Remote implements Behavior
{
    private DataInputStream dis;
    private boolean _suppressed = false;
    private String s;
    
    public Remote(DataInputStream dis){
        this.dis=dis;
    }
  
    public boolean takeControl()
    {
        if(Brick.getInstance().isStopped()){
            // cancel all behaviors, stop the arbitrator
            return false;
        }
      
        if(Brick.getInstance().getBehState()==Brick.state.MANUAL){
            // Take control if we're in Manual mode, don't read remote orders here
            return true;
        }
        
        /*
         * Read remote orders from PC/Raspi 
         */
        byte[] b = new byte[20];
        try {
            if(dis.available() > 0) {
                dis.read(b);
                s = new String(b);
            }
        } catch(Exception e) {}
    
        // Take control if orders are Stop or change from Auto (driving forward) to Manual mode
        return s!=null && (s.startsWith(Brick.STOP) || s.startsWith(Brick.MANUAL));
    }

    public void suppress()
    {
        // standard practice for suppress methods
        _suppressed = true;
    }

    public void action()
    {
        
        _suppressed=false;
        
        LCD.clearDisplay();
        if(s.startsWith(Brick.STOP)){
            /*
            * Notify Stop order
            */
            Brick.getInstance().setBehState(Brick.state.STOPPED);
            LCD.drawString("Bumper Car STOP",0,1);
        } else if(s.startsWith(Brick.MANUAL)){
            /*
            * Notify order to enter in Manual Mode
            */
            Brick.getInstance().setBehState(Brick.state.MANUAL);
            
            /*
            * Start reading serial port and process the orders
            */

            byte[] b = new byte[20];
            try {
                // This action will be suppressed if the robot finds an obstacle
                while(!_suppressed) {
                    
                    if(dis.available() > 0) {
                        dis.read(b);
                        s = new String(b);
                        if(s.startsWith(Brick.LEFT)){
                            LCD.drawString("LEFT    ",0,3);
                            // start Motor.C rotating forward, with A stopped, so 
                            // the robot turns left
                            Brick.getInstance().getLeftMotor().stop();
                            Brick.getInstance().getRightMotor().rotate(360, true);
                            // other orders may be processed before it has finished
                        } else if(s.startsWith(Brick.RIGHT)){
                            LCD.drawString("RIGHT    ",0,3);
                            // start Motor.A rotating forward, with C stopped, so 
                            // the robot turns right
                            Brick.getInstance().getRightMotor().stop();
                            Brick.getInstance().getLeftMotor().rotate(360, true);
                            // other orders may be processed before it has finished
                        } else if(s.startsWith(Brick.FORWARD)){
                            LCD.drawString("FOWARD    ",0,3);
                            // Moves forward
                            Brick.getInstance().getLeftMotor().forward();
                            Brick.getInstance().getRightMotor().forward();
                            // Don't stop moving till other order cancel this one, or the action ends
                            Thread.yield();
                        } else if(s.startsWith(Brick.BACKWARD)){
                            LCD.drawString("BACKWARD    ",0,3);
                            // Moves backward
                            Brick.getInstance().getLeftMotor().backward();
                            Brick.getInstance().getRightMotor().backward();
                            // Don't stop moving till other order cancel this one, or the action ends
                            Thread.yield();
                        } else if(s.startsWith(Brick.SPEED_UP)){
                            // increase speed of motors
                            Brick.getInstance().increaseSpeed();
                            LCD.drawString("VEL+    "+Brick.getInstance().getSpeed(),0,3);
                        } else if(s.startsWith(Brick.SPEED_DW)){
                            // increase speed of motors
                            Brick.getInstance().decreaseSpeed();
                            LCD.drawString("VEL-    "+Brick.getInstance().getSpeed(),0,3);
                        } else if(s.startsWith(Brick.AUTO)){
                            LCD.drawString("AUTO    ",0,3);
                            /*
                             * Return to Auto mode. Motors are stopped
                             */
                            Brick.getInstance().getLeftMotor().stop();
                            Brick.getInstance().getRightMotor().stop();
                            /*
                             * Notify forward (auto) state
                             */
                            Brick.getInstance().setBehState(Brick.state.FORWARD);
                            // ends the action
                            _suppressed=true;
                        }
                    }
                    Delay.msDelay(500);
                }
            } catch(Exception e) { }
        }
        // reset order string
        s="";
    }
}
