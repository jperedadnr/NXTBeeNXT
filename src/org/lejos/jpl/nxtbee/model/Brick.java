package org.lejos.jpl.nxtbee.model;

import java.io.DataOutputStream;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.RegulatedMotor;

/**
 * Singleton class to define the NXT configuration (motors, sensors, initial speed)
 * Provides wrapped access to command motors and read sensors state
 * Writes to PC/Raspi the state of the robot everytime it changes.
 * Modifies motors speed
 * 
 * @author Jose Pereda Llamas
 * Created on 27-dic-2012 - 11:31:15
 */
public class Brick {

    public static final String START    = "S";
    public static final String QUIT     = "Q";
    public static final String STOP     = "P";
    public static final String AUTO     = "A";
    public static final String MANUAL   = "M";
    public static final String LEFT     = "L";
    public static final String RIGHT    = "R";
    public static final String FORWARD  = "F";
    public static final String BACKWARD = "B";
    public static final String SPEED_UP = "V";
    public static final String SPEED_DW = "W";
    
    public static enum state {
        STOPPED(0), FORWARD(1), WALL(2), MANUAL(3);
        
        private final int stateVal;

        state(final int stateVal){
            this.stateVal=stateVal;
        }
        
        public int getStateVal() {
            return this.stateVal;
        }

    }
    
    private static Brick instance=new Brick();
    
    private RegulatedMotor leftMotor;
    private RegulatedMotor rightMotor;
    private TouchSensor touchF;
    private TouchSensor touchB;
    private UltrasonicSensor sonar;
    
    private state behState=null;
    
    private DataOutputStream dos=null;
    
    private Brick(){
        touchF = new TouchSensor(SensorPort.S1);
        touchB = new TouchSensor(SensorPort.S2);
        sonar = new UltrasonicSensor(SensorPort.S3);
        leftMotor = Motor.A;
        rightMotor = Motor.C;
        leftMotor.setSpeed(200);
        rightMotor.setSpeed(200);
    }
    
    public static Brick getInstance() { return instance; }
    
    public void setDataOutputStream(DataOutputStream dos){
        this.dos=dos;
    }
    
    public RegulatedMotor getLeftMotor() { return leftMotor; }
    public RegulatedMotor getRightMotor() { return rightMotor; }
    public TouchSensor getTouchFSensor() { return touchF; }
    public TouchSensor getTouchBSensor() { return touchB; }
    public UltrasonicSensor getUltrasonicSensor() { return sonar; }

    /*
     * if state.STOPPED then stop all behaviors, leave the arbitrator
     */
    public boolean isStopped() {
        return behState==state.STOPPED;
    }

    /*
     * Robot STATE: 
     *  - Stopped, waiting for Start or Quit 
     *  - Auto: Foward or Wall detected, and listening for Stop or Manual
     *  - Manual: F/B,R/L,S+,S-,Auto
     */
    public state getBehState() {
        return behState;
    }

    public void setBehState(state behState) {
        if(this.behState == behState){
            return;
        }
        this.behState = behState;
        try{
            LCD.drawString("State: " + behState.getStateVal(), 0, 2);
            dos.writeBytes(new Integer(behState.getStateVal()).toString()); 
            dos.writeByte(13);
            dos.writeByte(10);
        } catch(Exception e){ }
    }
    
    /*
     * MOTORS SPEED
     */
    public void increaseSpeed(){
        leftMotor.setSpeed(leftMotor.getSpeed()+20);
        rightMotor.setSpeed(leftMotor.getSpeed());
    }
    
    public void decreaseSpeed(){
        leftMotor.setSpeed(leftMotor.getSpeed()-20>0?leftMotor.getSpeed()-20:0);
        rightMotor.setSpeed(leftMotor.getSpeed());
    }
    
    public int getSpeed(){
        return leftMotor.getSpeed(); 
    }
    
}
