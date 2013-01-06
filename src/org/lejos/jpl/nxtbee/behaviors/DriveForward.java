package org.lejos.jpl.nxtbee.behaviors;

import lejos.robotics.subsumption.Behavior;
import org.lejos.jpl.nxtbee.model.Brick;

/**
 * Lowest priority, basic task: try to drive forward
 * unless the higher behaviors take control, because remote actions or
 * because found obstacles to avoid
 * 
 * @author Jose Pereda Llamas
 * Created on 27-dic-2012 - 11:18:27
 */
public class DriveForward implements Behavior
{

    private boolean _suppressed = false;

    public boolean takeControl()
    {
        if(Brick.getInstance().isStopped()){
            // cancel all behaviors, stop the arbitrator
            return false;
        }
        // this behavior always wants control.
        return true;  
    }

    public void suppress()
    {
        // standard practice for suppress methods
        _suppressed = true;
    }

    public void action()
    {
        _suppressed = false;
        /*
         * Notify robot is moving forward
         */
        Brick.getInstance().setBehState(Brick.state.FORWARD);
        /*
         * perform action till others behaviors take control and suppress this action
         */
        Brick.getInstance().getLeftMotor().forward();
        Brick.getInstance().getRightMotor().forward();
        while (!_suppressed){
            Thread.yield(); //don't exit till suppressed
        }
        /*
         * stop motors just before leaving this action
         */
        Brick.getInstance().getLeftMotor().stop(); 
        Brick.getInstance().getRightMotor().stop();
    }
}
