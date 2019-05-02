package br.unifor.si.rosos.Fuzzdeu;

import java.awt.Robot;
import java.util.Random;

import br.unifor.si.rosos.GameSimulator;
import br.unifor.si.rosos.GoalWall;
import br.unifor.si.rosos.Judge;
import br.unifor.si.rosos.RobotBasic;
import br.unifor.si.rosos.Sensor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import processing.core.PApplet;

public class Fuzzdeu extends RobotBasic {
	private float velocidade;
	private Sensor sensorBall;
	private Sensor sensorDistanceLeft, sensorDistanceRight, sensorDistanceFront, sensorDistanceBack;
	private Sensor sensorCompass;

	public Fuzzdeu(GameSimulator g) {
		super(g);
		this.sensorBall = getSensor("BALL");
		this.sensorDistanceLeft = getSensor("ULTRASONIC_LEFT");
		this.sensorDistanceRight = getSensor("ULTRASONIC_RIGHT");
		this.sensorDistanceFront = getSensor("ULTRASONIC_FRONT");
		this.sensorDistanceBack = getSensor("ULTRASONIC_BACK");
		this.sensorCompass = getSensor("COMPASS");
	}

	public void setup() {
		velocidade = 0.5f;
		/*
		 * You should use this method to initialize your code, setup Sensors and
		 * variables.
		 * 
		 * It will be runned once.
		 */
	}

	public void loop() {
		double y = 0;
		caminhar(velocidade);
		velocidade += 0.5f;
		System.out.println("OI0");
		float[] valuesBola = sensorBall.readValues();
		float[] valuesCompass = sensorCompass.readValues();
		float[] valuesPosFront = sensorDistanceFront.readValues();
		float[] valuesPosBack = sensorDistanceBack.readValues();
		float[] valuesPosRight = sensorDistanceRight.readValues();
		float[] valuesPosLeft = sensorDistanceLeft.readValues();
		System.out.println("POSIÇÂO Bola: " + valuesBola[0] + " " + valuesBola[1]);
		System.out.println("POSIÇÂO Comp: " + valuesCompass[0]);
		System.out.println("POSIÇÂO Pos Back: " + valuesPosBack[0]);
		System.out.println("POSIÇÂO Pos Front: " + valuesPosFront[0]);
		System.out.println("POSIÇÂO Pos Right: " + valuesPosRight[0]);
		System.out.println("POSIÇÂO Pos Left: " + valuesPosLeft[0]);
		System.out.println(this.orientation);
		System.out.println(this.position);
		System.out.println(this.sensorBall.readValues()[1]);
		this.fuzzyLogic();
	}

	void caminhar(float distancia) {

		setSpeed(distancia);
		delay(300);
		stopMotors();
		if (sensorBall.readValues()[0] <= 0f) {
			if (sensorBall.readValues()[0] >= -45f) {
				//				setSpeed(distancia);
				setRotation(-90);
			} else {
				//				setSpeed(distancia);
				stopMotors();
				setRotation(-90);
				delay(750);
				setSpeed(distancia);
			}
		} else if (sensorBall.readValues()[0] >= 0f) {
			if (sensorBall.readValues()[0] <= 45f) {
				//				setSpeed(distancia);
				setRotation(90);
			} else {
				//				setSpeed(distancia);
				stopMotors();
				setRotation(90);
				delay(750);
				setSpeed(distancia);
				
			}
		}
		//		0.4, 0.8 lim sup
		//		0.35, 1.3 lim inf
		//		if(sensorBall.readValues()[1] <= 1f) {
		//			stopMotors();
		//			if(this.getOrientation() >= 3)
		//			setRotation(90);
		//			delay(750);
		//			setSpeed(distancia);
		//		}

		// delay(1000);
		// stopMotors();
		/*
		 * setSpeed(-distancia); delay(1000); stopMotors();
		 */
	}

	private void fuzzyLogic() {
		double y = 0;
		caminhar(velocidade);
		velocidade += 0.5f;
		
		float[] valuesBola = sensorBall.readValues();
		float[] valuesCompass = sensorCompass.readValues();
		float[] valuesPosFront = sensorDistanceFront.readValues();
		float[] valuesPosBack = sensorDistanceBack.readValues();
		float[] valuesPosRight = sensorDistanceRight.readValues();
		float[] valuesPosLeft = sensorDistanceLeft.readValues();
		
		String rule_game_strategy = "fcl/rule_game_strategy.fcl";
		String rule_player_velocity = "fcl/rule_player_velocity.fcl";

		FIS fis_rule_game_strategy = FIS.load(rule_game_strategy, true);
		FIS fis_rule_player_velocity = FIS.load(rule_player_velocity, true);


		if( fis_rule_game_strategy == null ) { 
			System.err.println("Can't load file: '" + fis_rule_game_strategy + "'");
			return;
		}

		if( fis_rule_player_velocity == null ) { 
			System.err.println("Can't load file: '" + fis_rule_player_velocity + "'");
			return;
		}

		JFuzzyChart.get().chart(fis_rule_game_strategy.getFunctionBlock("game_strategy"));
		JFuzzyChart.get().chart(fis_rule_player_velocity.getFunctionBlock("player_velocity"));
		
		//Game Strategy
		double time_left = (120 - this.getGameSimulator().getTime()) / 12;
		double match_result = new Random().nextInt(1);
		//Player Velocity
		double lateral_distance = sensorDistanceFront.readValues()[0];
		double opponent_goal_orientation = 0;
		double ball_distance = sensorDistanceFront.readValues()[0];
		if(Float.isNaN(valuesPosFront[0])) {
			ball_distance = 3f;
		}
		fis_rule_game_strategy.setVariable("match_result", match_result);
		fis_rule_game_strategy.setVariable("time_left", time_left);

		fis_rule_player_velocity.setVariable("lateral_distance", lateral_distance);
		fis_rule_player_velocity.setVariable("opponent_goal_orientation", opponent_goal_orientation);
		fis_rule_player_velocity.setVariable("ball_distance", ball_distance);

		fis_rule_game_strategy.evaluate();
		fis_rule_player_velocity.evaluate();

		Variable gameStrategy = fis_rule_game_strategy.getVariable("game_strategy");
		Variable playerVelocity = fis_rule_player_velocity.getVariable("player_velocity");

		this.setSpeed(Float.parseFloat(playerVelocity.toString()));
		
		
		JFuzzyChart.get().chart(gameStrategy, gameStrategy.getDefuzzifier(), true);
		JFuzzyChart.get().chart(playerVelocity, playerVelocity.getDefuzzifier(), true);

		System.out.println(fis_rule_game_strategy);
		System.out.println(fis_rule_player_velocity);
	}

	/*
	 * If you want to code the thread method yourself, instead of using the already
	 * made `setup` and `loop` methods, you can override the method `run`. Uncomment
	 * those lines to use.
	 */
	// public void run(){

	// }

	/*
	 * You can use this method to decorate your robot. use Processing methods from
	 * the `canvas` object.
	 * 
	 * The center of the robot is at [0,0], and the limits are 100px x 100px.
	 */
	public void decorateRobot(PApplet canvas) {

	}

	/*
	 * Called whenever a robot is: PAUSED REMOVED from field PLACED in field STARTED
	 */
	public void onStateChanged(String state) {
	}

}
