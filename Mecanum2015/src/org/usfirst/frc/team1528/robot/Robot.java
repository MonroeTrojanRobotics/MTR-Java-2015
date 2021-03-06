
package org.usfirst.frc.team1528.robot;


import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.tables.*;
import edu.wpi.first.wpilibj.smartdashboard.*;

/**
 * The SampleRobot class is the base of a robot application that will automatically call your
 * Autonomous and OperatorControl methods at the right time as controlled by the switches on
 * the driver station or the field controls.
 *
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 *
 * WARNING: While it may look like a good choice to use for your code if you're inexperienced,
 * don't. Unless you know what you are doing, complex code will be much more difficult under
 * this system. Use IterativeRobot or Command-Based instead if you're new.
 */
public class Robot extends SampleRobot {
    
	//Constants for Buttons
    static final int A_BUTTON = 1;
    static final int B_BUTTON = 2;
    static final int X_BUTTON = 3;
    static final int Y_BUTTON = 4;
    static final int LEFT_BUMPER = 5;
    static final int RIGHT_BUMPER = 6;
    static final int BACK_BUTTON = 7;
    static final int START_BUTTON = 8;
    static final int LEFT_JOYSTICK_CLICK = 9;
    static final int RIGHT_JOYSTICK_CLICK = 10;
    
    //Constants for Axes
    static final int LEFT_X_AXIS = 0;
    static final int LEFT_Y_AXIS = 1;
    static final int LEFT_TRIGGER_AXIS = 2;
    static final int RIGHT_TRIGGER_AXIS = 3;
    static final int RIGHT_X_AXIS = 4;
    static final int RIGHT_Y_AXIS = 5;
    static final int D_PAD = 6; 
	
	RobotDrive myDrive;
    Joystick moveStick, shootStick;
    
    ExecutiveOrder control;
    ExecutiveRelease release;
    Thread releaseThread;

    
    Talon liftMotor;
    DigitalInput liftSwitch;
    LiftControl lift;
    Thread liftThread;
    
    DriveState orientationSwitcher;
    Thread orientationThread;
    
    ScaleChanger driveScaler;
    Thread driveScalerThread;
    
    SendableChooser autoChooser; 
    Integer autonomousID;
    SendableChooser teleChooser;
    Integer teleID;
    
    AutonomousManager autoManager;

    public Robot() {
        myDrive = new RobotDrive(4,0,5,2);
        moveStick = new Joystick(0);
        shootStick = new Joystick(1);
        
        control = new ExecutiveOrder(moveStick,shootStick,Y_BUTTON);
        release = new ExecutiveRelease(control);
        
        liftMotor = new Talon(3);
        liftSwitch = new DigitalInput(0);
        
        
        autoChooser = new SendableChooser();
        autoChooser.addDefault("Auto Forward", new Integer(0));
        autoChooser.addObject("Auto Right Sideways", new Integer(1));
        autoChooser.addObject("Auto Left Sideways", new Integer(2));
        autoChooser.addObject("Advanced", new Integer(3));
        
        teleChooser = new SendableChooser();
        teleChooser.addDefault("Default", new Integer(1));
        teleChooser.addObject("Secondary", new Integer(0));
        teleChooser.addObject("Guest Driver", new Integer(2));
        
        autoManager = new AutonomousManager(myDrive, liftMotor);
        
        SmartDashboard.putData("Autonomous Chooser", autoChooser);
        SmartDashboard.putData("TeleOp Chooser", teleChooser);
        SmartDashboard.putNumber("Scale Factor", 0.4);
        SmartDashboard.putNumber("Auto Scale Factor", 1.0);
        SmartDashboard.putNumber("Default Lift Speed", 1.0);
        
        SmartDashboard.putBoolean("Add Step 0", false);
        
        
        myDrive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        myDrive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
        
    }

    /**
     * This function is called once each time the robot enters autonomous mode.
     */
    public void autonomous() {
    	autonomousID = (Integer)autoChooser.getSelected();
    	
    	double scale = Math.abs(SmartDashboard.getNumber("Auto Scale Factor", 1.0));
    	
    	if(scale >= 1){
            scale = 1;
        }
    	
        switch(autonomousID.intValue()) {
            case 0:
                autonomous0(scale);
                break;
            case 1:
                autonomous1(scale);
                break;
            case 2:
                autonomous2(scale);
                break;
            case 3:
            	autonomous3();
            	break;
        }
    }
    
    /**
     * Forward driving.
     * @param scale The amount to multiply the speed by.
     */
    public void autonomous0(double scale){
        myDrive.setSafetyEnabled(false);
        
        liftMotor.set(1.0);
        Timer.delay(0.5);
        liftMotor.set(0.0);
        myDrive.mecanumDrive_Cartesian(0.0,1.0*scale,0.0,0.0);
        Timer.delay(0.8);
        myDrive.mecanumDrive_Cartesian(0.0,0.0,0.0,0.0);
        
    }
    
    /**
     * Right Sideways driving.
     * @param scale The amount to multiply the speed by.
     */
    public void autonomous1(double scale){
        myDrive.setSafetyEnabled(false);
       
        liftMotor.set(1.0*scale);
        Timer.delay(0.5);
        liftMotor.set(0.0);
        myDrive.mecanumDrive_Cartesian(1.0*scale,0.0,0.0,0.0);
        Timer.delay(0.8);
        myDrive.mecanumDrive_Cartesian(0.0,0.0,0.0,0.0);
        
    }
    
    /**
     * Left sideways driving.
     * @param scale The amount to multiply the speed by.
     */
    public void autonomous2(double scale){
        myDrive.setSafetyEnabled(false);
       
        liftMotor.set(1.0/scale);
        Timer.delay(0.5);
        liftMotor.set(0.0);
        myDrive.mecanumDrive_Cartesian(-1.0*scale,0.0,0.0,0.0);
        Timer.delay(0.8);
        myDrive.mecanumDrive_Cartesian(0.0,0.0,0.0,0.0);
        
        
    }

    /**
     * Advanced autonomous. Allows user to define steps without needing to recompile code.
     */
    public void autonomous3(){
    	autoManager.performAllActions();
    }
    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl() {
        myDrive.setSafetyEnabled(true);
        teleID = (Integer)teleChooser.getSelected();
        
        double liftSpeed = SmartDashboard.getNumber("Default Lift Speed", 1.0);
        liftSpeed = Math.abs(liftSpeed);
        double scale = Math.abs(SmartDashboard.getNumber("Scale Factor", 0.4));
    	
    	if(scale >= 1){
            scale = 1;
        }
        
        switch(teleID.intValue()) {
            case 0:
                teleOpLoop0(scale,liftSpeed);
                break;
            case 1:
                teleOpLoop1(scale,liftSpeed);
                break;
            case 2:
                teleOpLoop2(scale,liftSpeed);
                break;
        }
    }
    
    /**
     * Normal teleOp, doesn't use an ExecutiveOrder.
     * @param scale The amount to multiply the speed by.
     * @param liftSpeed The speed of the lift if buttons are used to control it.
     */
    public void teleOpLoop0(double scale, double liftSpeed){
    	orientationSwitcher = new DriveState(true,moveStick,A_BUTTON);
        orientationThread = new Thread(orientationSwitcher);
        orientationThread.start();
        
        lift = new LiftControl(shootStick,LEFT_TRIGGER_AXIS,RIGHT_TRIGGER_AXIS,liftMotor,liftSwitch);
        liftThread = new Thread(lift);
        liftThread.start();
        
        driveScaler = new ScaleChanger(moveStick,LEFT_BUMPER,RIGHT_BUMPER,scale,0.10);
        driveScalerThread = new Thread(driveScaler);
        driveScalerThread.start();
        
        while (isOperatorControl() && isEnabled()) {
            myDrive.setSafetyEnabled(true);
            boolean inverted = orientationSwitcher.getOrientation();
            double xMovement = buffer(LEFT_X_AXIS,moveStick,inverted,0.18,-0.18,driveScaler.getScale());
            double yMovement = buffer(LEFT_Y_AXIS,moveStick,inverted,0.18,-0.18,driveScaler.getScale());
            double twist = buffer(RIGHT_X_AXIS,moveStick,true,0.18,-0.18,driveScaler.getScale());
            myDrive.mecanumDrive_Cartesian(xMovement, yMovement, twist, 0.0);
            
            Timer.delay(0.01);
        }
        orientationSwitcher.stop();
        lift.stop();
        driveScaler.stop();
        
    }
    
    /**
     * Restricted teleOp, only uses ExecutiveOrder to override accessories.
     * @param scale The amount to multiply the speed by.
     * @param liftSpeed The speed of the lift if buttons are used to control it.
     */
    public void teleOpLoop1(double scale, double liftSpeed) {
    	releaseThread = new Thread(release);
        releaseThread.start();
        
        orientationSwitcher = new DriveState(true,moveStick,A_BUTTON);
        orientationThread = new Thread(orientationSwitcher);
        orientationThread.start();
        
        lift = new LiftControl(control,LEFT_TRIGGER_AXIS,RIGHT_TRIGGER_AXIS,liftMotor,liftSwitch);
        liftThread = new Thread(lift);
        liftThread.start();
        
        driveScaler = new ScaleChanger(moveStick,LEFT_BUMPER,RIGHT_BUMPER,scale,0.10);
        driveScalerThread = new Thread(driveScaler);
        driveScalerThread.start();
        
        while (isOperatorControl() && isEnabled()) {
            myDrive.setSafetyEnabled(true);
            if(control.president.getRawButton(B_BUTTON)){
               control.trap();
            }
            boolean inverted = orientationSwitcher.getOrientation();
            double xMovement = buffer(LEFT_X_AXIS,moveStick,inverted,0.18,-0.18,driveScaler.getScale());
            double yMovement = buffer(LEFT_Y_AXIS,moveStick,inverted,0.18,-0.18,driveScaler.getScale());
            double twist = buffer(RIGHT_X_AXIS,moveStick,true,0.18,-0.18,driveScaler.getScale());
            myDrive.mecanumDrive_Cartesian(xMovement, yMovement, twist, 0.0);
            
            Timer.delay(0.01);
        }
        
        release.stop();
        orientationSwitcher.stop();
        lift.stop();
        driveScaler.stop();
    }
    
    /**
     * Guest teleOp, uses ExecutiveOrder for full system.
     * @param scale The amount to multiply the speed by.
     * @param liftSpeed The speed of the lift if buttons are used to control it.
     */
    public void teleOpLoop2(double scale, double liftSpeed) { 
    	releaseThread = new Thread(release);
        releaseThread.start();
        
        orientationSwitcher = new DriveState(true,control,A_BUTTON);
        orientationThread = new Thread(orientationSwitcher);
        orientationThread.start();
        
        lift = new LiftControl(control,LEFT_TRIGGER_AXIS,RIGHT_TRIGGER_AXIS,liftMotor,liftSwitch);
        liftThread = new Thread(lift);
        liftThread.start();
        
        driveScaler = new ScaleChanger(moveStick,LEFT_BUMPER,RIGHT_BUMPER,scale,0.10);
        driveScalerThread = new Thread(driveScaler);
        driveScalerThread.start();
        
        while (isOperatorControl() && isEnabled()) {
            myDrive.setSafetyEnabled(true); 
            Joystick currentDriver;
            if(control.president.getRawButton(B_BUTTON)){
               control.trap();
            }
            if(control.getReleaseState()){
                currentDriver = control.congress;
            }
            else {
                currentDriver = control.president;
            }
            boolean inverted = orientationSwitcher.getOrientation();
            double xMovement = buffer(LEFT_X_AXIS,currentDriver,inverted,0.18,-0.18,driveScaler.getScale());
            double yMovement = buffer(LEFT_Y_AXIS,currentDriver,inverted,0.18,-0.18,driveScaler.getScale());
            double twist = buffer(RIGHT_X_AXIS,currentDriver,true,0.18,-0.18,driveScaler.getScale());
            myDrive.mecanumDrive_Cartesian(xMovement, yMovement, twist, 0.0);
            
            Timer.delay(0.01);
        }
        
        release.stop();
        orientationSwitcher.stop();
        lift.stop();
        driveScaler.stop();
    }

    /**
     * Runs while disabled
     */
    
    @Override
    public void disabled(){	
    }
    
    /**
     * Runs during test mode
     */
    public void test() {
    }  
    
    /**
     * This function buffers Joystick.getRawAxis() input.
     * @param axisNum The ID for the axis of a Joystick.
     * @param joystickName The Joystick that input is coming from. 
     * @param inverted Is it flipped?
     * @param highMargin The high margin of the buffer.
     * @param lowMargin The low margin of the buffer.
     * @return moveOut - The buffered axis data from joystickName.getRawAxis().
     **/
    public double buffer(int axisNum, Joystick joystickName, boolean inverted, double highMargin, double lowMargin) {
        double moveIn = joystickName.getRawAxis(axisNum);
        double moveOut;
        moveOut = 0.0;
        
        if(moveIn >= lowMargin && moveIn <= highMargin ) {
            moveOut = 0.0;
        }
        else{
            if(inverted){
                moveOut = -moveIn;
            }
            else if(!inverted){ 
                moveOut = moveIn;
            }    
        }
	
        return moveOut;
   }
   
    
    /**
     * This function buffers Joystick.getRawAxis() input.
     * @param axisNum The ID for the axis of a Joystick.
     * @param joystickName The Joystick that input is coming from. 
     * @param inverted Is it flipped?
     * @param highMargin The high margin of the buffer.
     * @param lowMargin The low margin of the buffer.
     * @param scale The amount you want to multiply the output by.
     * @return moveOut - The buffered axis data from joystickName.getRawAxis().
     **/
    public double buffer(int axisNum, Joystick joystickName, boolean inverted, double highMargin, double lowMargin, double scale) {
        double moveIn = joystickName.getRawAxis(axisNum);
        double moveOut;
        moveOut = 0.0;
        
        if(moveIn >= lowMargin && moveIn <= highMargin ) {
            moveOut = 0.0;
        }
        else{
            if(inverted){
                moveOut = -moveIn;
            }
            else if(!inverted){ 
                moveOut = moveIn;
            }    
        }
        
        scale = Math.abs(scale);
        
        if(scale >= 1){
            scale = 1;
        }
        
        moveOut = moveOut*scale;
        
        return moveOut;
   }
    
    /**
     * This function toggles the solenoids with two buttons.
     * @param offButton ID of button to deactivate 
     * @param onButton ID of button to activate
     * @param joystickName Name of Joystick input is coming from
     * @param solenoid1 The first solenoid
     * @param solenoid2 The second solenoid
     */
   
    public void solenoidToggle(int offButton, int onButton, Joystick joystickName, Solenoid solenoid1, Solenoid solenoid2 ) {
       boolean pressedOn = joystickName.getRawButton(onButton);
       boolean pressedOff = joystickName.getRawButton(offButton);
       
       if (pressedOn) {
        solenoid1.set(true);
        solenoid2.set(false);
       }
       else if (pressedOff) {
        solenoid1.set(false);
        solenoid2.set(true);
       }
       
    }
    
    /**
     * This function controls operation of a relay with a switch.
     * @param relayName The Relay object.
     * @param switchName1 The switch for forward motion.
     * @param switchName2 The switch for backward motion.
     */
    
    public void relayControl(Relay relayName, DigitalInput switchName1, DigitalInput switchName2){
        
        if(switchName1.get() && !switchName2.get()) {
            relayName.set(Relay.Value.kForward);
        }
        else if(!switchName1.get() && switchName2.get()) {
            relayName.set(Relay.Value.kReverse);
        }
        else{
            relayName.set(Relay.Value.kOff);
        }
    }
    
    /**
     * This runs the winch with an AnalogChannel senor.
     * @param relayName The relay spike.
     * @param sonicPing The ultrasonic sensor.
     * @param pullBack The distance to pull back.
     */
    public void relayControl(Relay relayName, AnalogInput sonicPing, double pullBack) {
        
        
        double pulledBack = (sonicPing.getVoltage()/0.0048828125);
        
        if(pulledBack != pullBack){
            relayName.set(Relay.Value.kForward);
        }
        else if(pulledBack == pullBack){
            relayName.set(Relay.Value.kOff);
        }
    }
    
    /**
     * This controls a relay with either axis input or two buttons.
     * When using an axis, forward and back should be the same value.
     * @param relayName The Relay that is being controlled.
     * @param joystickName The joystick for input.
     * @param forward The id for the forward button or one half of an axis.
     * @param back The id for the back button or one half of an axis.
     * @param type Is the input from a button or axis?
     * @exception IllegalArgumentException() If type is invalid. 
     */
    public void relayControl(Relay relayName, Joystick joystickName, int forward, int back, String type) {
        boolean pressedForward = false;
        boolean pressedBack = false;
        
        if(type.equalsIgnoreCase("button")) {
           pressedForward = joystickName.getRawButton(forward);
           pressedBack = joystickName.getRawButton(back);
        }
        else if(type.equalsIgnoreCase("axis")) {
           pressedForward = joystickName.getRawAxis(forward) <= -0.40;
           pressedBack = joystickName.getRawAxis(back) >= 0.40;
        }
        else {
            throw new IllegalArgumentException(type + " is not a valid type of input.");
        }
        
        if(pressedForward && !pressedBack) {
            relayName.set(Relay.Value.kForward);
        }
        else if(!pressedForward && pressedBack) {
            relayName.set(Relay.Value.kReverse);
        }
        else {
            relayName.set(Relay.Value.kOff);
        }
    }
    
    
     /**
     * Relay control with buttons and limit switches.
     * @param relayName The relay under control.
     * @param joystickName The joystick controlling it.
     * @param forward The id for the forward button or one half of an axis.
     * @param back The id for the back button or one half of an axis.
     * @param type Is the input from a button or axis?
     * @param inside The switch at the inside limit.
     * @param outside The switch on the outside limit.
     * @exception IllegalArgumentException If type is invalid.
     */
    public void relayControl(Relay relayName, Joystick joystickName, int forward, 
             int back, String type , DigitalInput inside, DigitalInput outside) {
        boolean pressedForward = false;
        boolean pressedBack = false;
        
        if(type.equalsIgnoreCase("button")) {
           pressedForward = joystickName.getRawButton(forward);
           pressedBack = joystickName.getRawButton(back);
        }
        else if(type.equalsIgnoreCase("axis")) {
           pressedForward = joystickName.getRawAxis(forward) <= -0.40;
           pressedBack = joystickName.getRawAxis(back) >= 0.40;
        }
        else {
            throw new IllegalArgumentException(type + " is not a valid type of input.");
        }
        
        if(pressedForward && !pressedBack && !outside.get()) {
            relayName.set(Relay.Value.kForward);
        }
        else if(pressedForward && !pressedBack && outside.get()) {
            relayName.set(Relay.Value.kOff);
        }
        else if(!pressedForward && pressedBack && !inside.get()) {
            relayName.set(Relay.Value.kReverse);
        }
        else if(!pressedForward && pressedBack && inside.get()) {
            relayName.set(Relay.Value.kOff);
        }
        else {
            relayName.set(Relay.Value.kOff);
        }
    }
   
}
