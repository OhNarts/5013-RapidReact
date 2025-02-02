// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static frc.robot.Constants.DrivetrainConstants.PIGEON_ID;
import static frc.robot.Constants.DrivetrainConstants.SWERVE_GEAR_RATIO;
import static frc.robot.Constants.DrivetrainConstants.MAX_VOLTAGE;

import com.ctre.phoenix.sensors.WPI_Pigeon2;
import com.swervedrivespecialties.swervelib.Mk4ModuleConfiguration;
import com.swervedrivespecialties.swervelib.Mk4SwerveModuleHelper;
import com.swervedrivespecialties.swervelib.SwerveModule;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.math.MathUtil;
import frc.robot.Constants.DrivetrainConstants.BackLeftSwerveConstants;
import frc.robot.Constants.DrivetrainConstants.BackRightSwerveConstants;
import frc.robot.Constants.DrivetrainConstants.FrontLeftSwerveConstants;
import frc.robot.Constants.DrivetrainConstants.FrontRightSwerveConstants;
import frc.robot.Constants.DrivetrainConstants.TranslationGains;
import frc.robot.Constants.DrivetrainConstants.DrivetrainGeometry;

public class DrivetrainSubsystem extends SubsystemBase {
	private final WPI_Pigeon2 m_pigeon = new WPI_Pigeon2(PIGEON_ID.id,PIGEON_ID.busName);
	private final SwerveDriveKinematics m_kinematics = new SwerveDriveKinematics(
			// Front left
			new Translation2d(DrivetrainGeometry.TRACKWIDTH_METERS / 2.0, DrivetrainGeometry.WHEELBASE_METERS / 2.0),
			// Front right
			new Translation2d(DrivetrainGeometry.TRACKWIDTH_METERS / 2.0, -DrivetrainGeometry.WHEELBASE_METERS / 2.0),
			// Back left
			new Translation2d(-DrivetrainGeometry.TRACKWIDTH_METERS / 2.0, DrivetrainGeometry.WHEELBASE_METERS / 2.0),
			// Back right
			new Translation2d(-DrivetrainGeometry.TRACKWIDTH_METERS / 2.0, -DrivetrainGeometry.WHEELBASE_METERS / 2.0));

	// FIX We need to figure out initial possition.
	private Pose2d m_pose = new Pose2d();
	private SwerveDriveOdometry m_odometry = new SwerveDriveOdometry(m_kinematics, getYawR2d(), m_pose);

	// These are our modules. We initialize them in the constructor.
	private final SwerveModule m_frontLeftModule;
	private final SwerveModule m_frontRightModule;
	private final SwerveModule m_backLeftModule;
	private final SwerveModule m_backRightModule;

	private SwerveModuleState[] m_desiredStates;

	private SimpleMotorFeedforward m_feedForward = new SimpleMotorFeedforward(TranslationGains.kS, TranslationGains.kV,
			TranslationGains.kA);

	public DrivetrainSubsystem() {
		ShuffleboardTab tab = Shuffleboard.getTab("Drivetrain");

		Mk4ModuleConfiguration config = new Mk4ModuleConfiguration();
		config.setDriveCurrentLimit(40);
		config.setDriveCurrentLimit(25);
		// Creating the SwerveModules using SDS factory method.
		m_frontLeftModule = Mk4SwerveModuleHelper.createFalcon500(
				tab.getLayout("Front Left Module", BuiltInLayouts.kList).withSize(2, 4).withPosition(0, 0),
				config,	SWERVE_GEAR_RATIO, FrontLeftSwerveConstants.DRIVE_MOTOR_ID, FrontLeftSwerveConstants.STEER_MOTOR_ID,
				FrontLeftSwerveConstants.ENCODER_ID, FrontLeftSwerveConstants.ENCODER_OFFSET_RADIANS);

		m_frontRightModule = Mk4SwerveModuleHelper.createFalcon500(
				tab.getLayout("Front Right Module", BuiltInLayouts.kList).withSize(2, 4).withPosition(2, 0),
				config,SWERVE_GEAR_RATIO, FrontRightSwerveConstants.DRIVE_MOTOR_ID, FrontRightSwerveConstants.STEER_MOTOR_ID,
				FrontRightSwerveConstants.ENCODER_ID, FrontRightSwerveConstants.ENCODER_OFFSET_RADIANS);

		m_backLeftModule = Mk4SwerveModuleHelper.createFalcon500(
				tab.getLayout("Back Left Module", BuiltInLayouts.kList).withSize(2, 4).withPosition(4, 0),
				config,SWERVE_GEAR_RATIO, BackLeftSwerveConstants.DRIVE_MOTOR_ID, BackLeftSwerveConstants.STEER_MOTOR_ID,
				BackLeftSwerveConstants.ENCODER_ID, BackLeftSwerveConstants.ENCODER_OFFSET_RADIANS);

		m_backRightModule = Mk4SwerveModuleHelper.createFalcon500(
				tab.getLayout("Back Right Module", BuiltInLayouts.kList).withSize(2, 4).withPosition(6, 0),
				config,SWERVE_GEAR_RATIO, BackRightSwerveConstants.DRIVE_MOTOR_ID, BackRightSwerveConstants.STEER_MOTOR_ID,
				BackRightSwerveConstants.ENCODER_ID, BackRightSwerveConstants.ENCODER_OFFSET_RADIANS);

		zeroGyroscope();
	}

	/**
	 * Sets the gyroscope angle to zero. This can be used to set the direction the
	 * robot is currently facing to the 'forwards' direction.
	 */
	public void zeroGyroscope() {
		m_pigeon.setYaw(0.0);

	}

	public void setInitialPosition(Pose2d newPosition, Rotation2d newRotation) {
		m_pigeon.setYaw(newRotation.getDegrees());
		m_pose = new Pose2d(
			newPosition.getTranslation(),
			newRotation  );
		m_odometry = new SwerveDriveOdometry(m_kinematics, getYawR2d(), m_pose);
	}

	/*
	 * Return the gyroscope's heading as a Rotation2d object
	 */
	public Rotation2d getYawR2d() {
		return getPose().getRotation();
		//eturn Rotation2d.fromDegrees(m_pigeon.getYaw());
	}

    public Rotation2d getRollR2d() {
		return Rotation2d.fromDegrees(m_pigeon.getRoll());
	}
    public Rotation2d getPitchR2d() {
        return Rotation2d.fromDegrees(m_pigeon.getPitch());
    }

	/*
	 * Return the gyroscope's heading in Radians
	 */
	public double getHeadingRadians() {
		return getYawR2d().getRadians();
	}

	public void drive(ChassisSpeeds chassisSpeeds) {
		if (m_desiredStates != null && chassisSpeeds.vxMetersPerSecond == 0 && chassisSpeeds.vyMetersPerSecond == 0
				&& chassisSpeeds.omegaRadiansPerSecond == 0) {
			m_desiredStates[0].speedMetersPerSecond = 0;
			m_desiredStates[1].speedMetersPerSecond = 0;
			m_desiredStates[2].speedMetersPerSecond = 0;
			m_desiredStates[3].speedMetersPerSecond = 0;
		} else {
			m_desiredStates = m_kinematics.toSwerveModuleStates(chassisSpeeds);
		}
	}

	public void setDesiredStates(SwerveModuleState[] newStates) {
		m_desiredStates = newStates;
	}

	@Override
	public void periodic() {
		updateOdometry();
		updateDriveStates(m_desiredStates);
		SmartDashboard.putNumber("Gyro", getYawR2d().getDegrees());
		SmartDashboard.putNumber("pitch", getPitchR2d().getDegrees());
	}

	private void updateOdometry() {
		m_pose = m_odometry.update(Rotation2d.fromDegrees(m_pigeon.getYaw()), stateFromModule(m_frontLeftModule),
				stateFromModule(m_frontRightModule),
				stateFromModule(m_backLeftModule), stateFromModule(m_backRightModule));
	}

	private SwerveModuleState stateFromModule(SwerveModule swerveModule) {
		return new SwerveModuleState(swerveModule.getDriveVelocity(), new Rotation2d(swerveModule.getSteerAngle()));
	}

	private void updateDriveStates(SwerveModuleState[] desiredStates) {
		if (desiredStates != null) {
			SwerveModuleState frontLeftState = desiredStates[FrontLeftSwerveConstants.STATES_INDEX];
			SwerveModuleState frontRightState = desiredStates[FrontRightSwerveConstants.STATES_INDEX];
			SwerveModuleState backLeftState = desiredStates[BackLeftSwerveConstants.STATES_INDEX];
			SwerveModuleState backRightState = desiredStates[BackRightSwerveConstants.STATES_INDEX];

			SwerveDriveKinematics.desaturateWheelSpeeds(desiredStates,
					DrivetrainGeometry.MAX_VELOCITY_METERS_PER_SECOND);

			m_frontLeftModule.set(velocityToDriveVolts(frontLeftState.speedMetersPerSecond),
					frontLeftState.angle.getRadians());
			m_frontRightModule.set(velocityToDriveVolts(frontRightState.speedMetersPerSecond),
					frontRightState.angle.getRadians());
			m_backLeftModule.set(velocityToDriveVolts(backLeftState.speedMetersPerSecond),
					backLeftState.angle.getRadians());
			m_backRightModule.set(velocityToDriveVolts(backRightState.speedMetersPerSecond),
					backRightState.angle.getRadians());
		}
	}

	private double velocityToDriveVolts(double speedMetersPerSecond) {
		double ff = m_feedForward.calculate(speedMetersPerSecond);
		return MathUtil.clamp(ff, -MAX_VOLTAGE, MAX_VOLTAGE);
	}

	public Pose2d getPose() {
		return m_pose;
	}

	public SwerveDriveKinematics getKinematics() {
		return m_kinematics;
	}

	public static double percentOutputToMetersPerSecond(double percentOutput) {
		return DrivetrainGeometry.MAX_VELOCITY_METERS_PER_SECOND * percentOutput;
	}

	public static double percentOutputToRadiansPerSecond(double percentOutput) {
		return DrivetrainGeometry.MAX_ANGULAR_VELOCITY_RADIANS_PER_SECOND * percentOutput;
	}
}