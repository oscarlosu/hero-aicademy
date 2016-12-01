package marttoslo.portfolio;

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
		EquipDefense,
		EquipScroll,
		EquipSword,
		FocusAttackCrystal,
		FocusAttackAndCaptureHealer,
		FocusAttackDPS,
		FocusAttackLowHP,
		CaptureUnit,
		HealMostValuable,
		
		AdvanceUnit,
		RetreatUnit,
		SpawnDefense,
		SpawnOffense,
		
		
		
		
		FinalFallback
	}
	
	public static Random random;
	
	private static boolean initialized;
	private static HashMap<BehaviourType, Behaviour> behaviours = new HashMap<BehaviourType, Behaviour>();
	
	public static ArrayList<Action> GetActions(GameState gameState, boolean isPlayer1, BehaviourType type) {
		if (!initialized)
			Initialize();
		return behaviours.get(type).GetActions(isPlayer1, gameState);
	}
	
	public static SmartAction GetRandomSmartAction(Random rng) {
		random = rng;

		BehaviourType behaviour = null;
		while (behaviour == null && behaviour != BehaviourType.FinalFallback) {
			behaviour = BehaviourType.values()[random.nextInt(BehaviourType.values().length)];
		}
		return new SmartAction(behaviour);		
	}
	
	private static void Initialize() {
		behaviours.put(BehaviourType.EquipDefense, new EquipDefense(BehaviourType.HealMostValuable));
		behaviours.put(BehaviourType.EquipScroll, new EquipScroll(BehaviourType.EquipSword));
		behaviours.put(BehaviourType.EquipSword, new EquipSword(BehaviourType.EquipDefense));
		behaviours.put(BehaviourType.FocusAttackCrystal, new FocusAttackCrystal(BehaviourType.FocusAttackAndCaptureHealer));
		behaviours.put(BehaviourType.FocusAttackAndCaptureHealer, new FocusAttackHealer(BehaviourType.FocusAttackDPS));
		behaviours.put(BehaviourType.FocusAttackDPS, new FocusAttackDPS(BehaviourType.FocusAttackLowHP));
		behaviours.put(BehaviourType.FocusAttackLowHP, new FocusAttackLowHP(BehaviourType.SpawnOffense));
		behaviours.put(BehaviourType.CaptureUnit, new CaptureUnit(BehaviourType.FocusAttackLowHP));
		behaviours.put(BehaviourType.HealMostValuable, new HealMostValuable(BehaviourType.SpawnDefense));
		behaviours.put(BehaviourType.AdvanceUnit, new AdvanceUnit(BehaviourType.FinalFallback));
		behaviours.put(BehaviourType.RetreatUnit, new RetreatUnit(BehaviourType.FinalFallback));
		behaviours.put(BehaviourType.SpawnOffense, new SpawnOffense(BehaviourType.AdvanceUnit));
		behaviours.put(BehaviourType.SpawnDefense, new SpawnDefense(BehaviourType.RetreatUnit));
		
		
		

		behaviours.put(BehaviourType.FinalFallback, new FinalFallback());
		initialized = true;
	}
}
