package marttoslo.portfolio;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;

import action.Action;
import ai.util.ActionPruner;
import game.GameState;
import marttoslo.portfolio.behaviours.*;

public class PortfolioController {
	public static enum BehaviourType {
		EquipScroll,
		EquipSword,
		EquipDefense,
		FocusAttackCrystal,
		FocusAttackAndCaptureHealer,
		FocusAttackDPS,
		CaptureUnit,
		FocusAttackLowHP,
		HealMostValuable,
		AdvanceUnit,
		RetreatUnit,
		SpawnDefense,
		SpawnOffense,
		UsePotion,
		
		
		FinalFallback
	}
	
	public static Random random;
	
	private static boolean initialized;
	private static HashMap<BehaviourType, Behaviour> behaviours = new HashMap<BehaviourType, Behaviour>();
	private static HashMap<BehaviourType, BehaviourStatistics> behaviourStatistics = new HashMap<BehaviourType, BehaviourStatistics>();
	
	
	public static ArrayList<Action> GetActions(GameState gameState, boolean isPlayer1, BehaviourType type) {
		if (!initialized)
			Initialize();
		
		BehaviourStatistics statistics = behaviourStatistics.get(type);
		statistics.Run(gameState.turn);
		//ThreadMXBean thx = ManagementFactory.getThreadMXBean();
		//thx.setThreadCpuTimeEnabled(true);
		//double start = thx.getCurrentThreadCpuTime() / 1e6;
		
		
		ArrayList<Action> actions = behaviours.get(type).GetActions(isPlayer1, gameState);
		
		//double end = thx.getCurrentThreadCpuTime() / 1e6;
		//statistics.runTime += end-start;
		
		
		return actions;
	}
	
	public static SmartAction GetRandomSmartAction(Random rng) {
		random = rng;

		BehaviourType behaviour = null;
		while (behaviour == null && behaviour != BehaviourType.FinalFallback) {
			behaviour = BehaviourType.values()[random.nextInt(BehaviourType.values().length)];
		}
		return new SmartAction(behaviour);		
	}
	
	public static BehaviourStatistics GetBehaviourStatistics(BehaviourType behaviourType) {
		return behaviourStatistics.get(behaviourType);
	}
	
	public static void PrintAllBehaviourStatistics() {
		System.out.println("------------BEHAVIOUR STATISTICS------------");
		for (BehaviourType type : BehaviourType.values()) {
			BehaviourStatistics statistic = behaviourStatistics.get(type);
			System.out.println("Behaviour: " + type.toString() +" - TotalRunCount: " + statistic.totalRunCount + " - AvrgRunTime: " + statistic.GetAverageRunTime());
			
		}
		System.out.println("------------END STATISTICS------------");
	}
	
	private static void InitStatistics() {
		behaviourStatistics = new HashMap<BehaviourType, BehaviourStatistics>();
		for (BehaviourType type : BehaviourType.values()) {
			behaviourStatistics.put(type, new BehaviourStatistics());
		}
	}
	
	public static void Reset() {
		InitStatistics();
	}
	
	private static void Initialize() {
		InitStatistics();
		behaviours.put(BehaviourType.EquipScroll, new EquipScroll(BehaviourType.EquipSword));
		behaviours.put(BehaviourType.EquipSword, new EquipSword(BehaviourType.EquipDefense));
		behaviours.put(BehaviourType.EquipDefense, new EquipDefense(BehaviourType.HealMostValuable));
		behaviours.put(BehaviourType.FocusAttackCrystal, new FocusAttackCrystal(BehaviourType.FocusAttackAndCaptureHealer));
		behaviours.put(BehaviourType.FocusAttackAndCaptureHealer, new FocusAttackHealer(BehaviourType.FocusAttackDPS));
		behaviours.put(BehaviourType.FocusAttackDPS, new FocusAttackDPS(BehaviourType.FocusAttackLowHP));
		behaviours.put(BehaviourType.CaptureUnit, new CaptureUnit(BehaviourType.FocusAttackLowHP));
		behaviours.put(BehaviourType.FocusAttackLowHP, new FocusAttackLowHP(BehaviourType.SpawnOffense));
		behaviours.put(BehaviourType.HealMostValuable, new HealMostValuable(BehaviourType.SpawnDefense));
		behaviours.put(BehaviourType.AdvanceUnit, new AdvanceUnit(BehaviourType.FinalFallback));
		behaviours.put(BehaviourType.RetreatUnit, new RetreatUnit(BehaviourType.FinalFallback));
		behaviours.put(BehaviourType.SpawnOffense, new SpawnOffense(BehaviourType.AdvanceUnit));
		behaviours.put(BehaviourType.SpawnDefense, new SpawnDefense(BehaviourType.RetreatUnit));
		behaviours.put(BehaviourType.UsePotion, new UsePotion(BehaviourType.EquipDefense));
		

		behaviours.put(BehaviourType.FinalFallback, new FinalFallback());
		initialized = true;
	}
}
