package org.lejos.jpl.nxtbee.behaviors;

import lejos.robotics.subsumption.Behavior;
import org.lejos.jpl.nxtbee.model.Brick;

/**
 * Highest priority: avoid always impacts with obstacles from the 
 * front (detected with Ultrasonic and touch) and rear when moving backwards (touch)
 * In case of obstacle, rotate backwards (front) or forwards (rear) and 
 * return to auto mode, driving forward
 * 
 * @author Jose Pereda Llamas
 * Created on 27-dic-2012 - 11:27:26
 */
public class DetectWall implements Behavior
{
    private boolean rear=false;
    private boolean front=false;
  
    public boolean takeControl()
    {
        if(Brick.getInstance().isStopped()){
            // cancel all behaviors, stop the arbitrator
            return false;
        }
      
        Brick.getInstance().getUltrasonicSensor().ping();
    
        if(Brick.getInstance().getTouchBSensor().isPressed()){
            rear=true;
            front=false;
        } else if (Brick.getInstance().getTouchFSensor().isPressed() || 
               Brick.getInstance().getUltrasonicSensor().getDistance() < 25){
            rear=false;
            front=true;
        } else {
            rear=false;
            front=false;
        }         
    
        /*
         * take control only if obstacle detected and robot not stopped
         */
        return (rear || front);
    }

    public void suppress()
    {
        //Since  this is highest priority behavior, suppress will never be called.
    }

    public void action()
    {
        /*
         * Notify Wall has been detected
         */
        Brick.getInstance().setBehState(Brick.state.WALL);  
        
        /*
         * Action: rotate opposite to direction of obstacle
         */
        if(front){
            Brick.getInstance().getLeftMotor().rotate(-180, true);// start Motor.A rotating backward
            Brick.getInstance().getRightMotor().rotate(-360);     // rotate C farther to make the turn
        } else if(rear) {
            Brick.getInstance().getLeftMotor().rotate(180, true);// start Motor.A rotating forward
            Brick.getInstance().getRightMotor().rotate(360);     // rotate C farther to make the turn
        } 
        // the action finishes atfer C stops rotating
        // if there are no more obstacles, DriveForward should take control
    }
}
