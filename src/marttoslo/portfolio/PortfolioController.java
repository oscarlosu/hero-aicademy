package marttoslo.portfolio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
		FocusAttackHealer
	}
	
	private static boolean initialized;
	private static HashMap<BehaviourType, Behaviour> behaviours = new HashMap<BehaviourType, Behaviour>();
	
	public static ArrayList<Action> GetActions(GameState gameState, boolean isPlayer1, BehaviourType type) {
		if (!initialized)
			Initialize();
		return behaviours.get(type).GetActions(isPlayer1, gameState);
	}
	
	public static SmartAction GetRandomSmartAction(Random random) {
		return new SmartAction(BehaviourType.values()[random.nextInt(BehaviourType.values().length)]);		
	}
	
	private static void Initialize() {
		behaviours.put(BehaviourType.EquipDefense, new EquipDefense());
		behaviours.put(BehaviourType.EquipScroll, new EquipScroll());
		behaviours.put(BehaviourType.EquipSword, new EquipSword());
		behaviours.put(BehaviourType.FocusAttackCrystal, new FocusAttackCrystal());
		behaviours.put(BehaviourType.FocusAttackHealer, new FocusAttackHealer());
		
		initialized = true;
	}
}
