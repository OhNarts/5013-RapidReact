// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.trobot5013lib.LinearInterpolator;

/** Add your docs here. */
public final class ShooterConstants {

    public static final int SHOOTER_TOP_MOTOR = 18;
    public static final int SHOOTER_BOTTOM_MOTOR = 17;
    public static final double SHOOTER_VELOCITY = 4500;
    public static final double TOP_PERCENT_OF_BOTTOM = 1.25;
    
    public static final class FLY_WHEEL_GAINS{
        // (output% * 1023) / (tuner value for velocity at output%)
		public static final double kP = .45;
        //public static final double kF = .049;
        public static final double kF = .055;

        // (throttle% x 1023) / (servo error # after just running w/ F gain)

        public static final double kI = 0.0;
        // 1. find closed loop error units by change in sensor by 100 
        // 2. multiply the units by 2.5 to cover the typical error

        public static final double kD = 0.0;
        // (10 x kP)
    }
	public static final class FendorShotConstants{
		public static final double BOTTOM_WHEEL_SPEED = 3815;
		public static final double  HOOD_HEIGHT = 30;
	}

	public static final class TargetConstants{
		public static final double[][] HOOD_HEIGHT_ARRAY = {
				{-11,1,8},
				{140,140,40}
			};
		public static final double[][] SHOOTER_SPEED_ARRAY = {
				{-11,1,8},
				{5150,4900,4800}
			};
		public static final LinearInterpolator HOOD_INTERPOLATOR = new LinearInterpolator(HOOD_HEIGHT_ARRAY);
		public static final LinearInterpolator SHOOTER_SPEED_INTERPOLATOR = new LinearInterpolator(SHOOTER_SPEED_ARRAY);
		public static final double HOOD_TOLERANCE_MM = 2;
		public static final double SHOOTER_SPEED_PERCENT = .05;
	}
}
