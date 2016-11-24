package marttoslo.portfolio;

import java.util.HashMap;

import action.Action;
import game.GameState;
import marttoslo.portfolio.behaviours.*;
public class PortfolioController {
	public enum BehaviourType {
		EquipDefense,
		EquipScroll,
		EquipSword,
		FocusAttackCrystal,
		FocusAttackHealer
	}
	
	private HashMap<BehaviourType, Behaviour> behaviours = new HashMap<BehaviourType, Behaviour>();
	
	public PortfolioController() {
		Initialize();
	}
	
	public SmartAction GetAction(GameState gameState, boolean isPlayer1, BehaviourType type) {
		SmartAction smartAction = new SmartAction((Action[]) behaviours.get(type).GetActions(isPlayer1, gameState).toArray());
		return smartAction;
	}
	
	private void Initialize() {
		behaviours.put(BehaviourType.EquipDefense, new EquipDefense());
		behaviours.put(BehaviourType.EquipScroll, new EquipScroll());
		behaviours.put(BehaviourType.EquipSword, new EquipSword());
		behaviours.put(BehaviourType.FocusAttackCrystal, new FocusAttackCrystal());
		behaviours.put(BehaviourType.FocusAttackHealer, new FocusAttackHealer());
	}
}
