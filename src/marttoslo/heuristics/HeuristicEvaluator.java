package marttoslo.heuristics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import action.SingletonAction;
import ai.evaluation.IStateEvaluator;
import ai.util.NormUtil;
import game.GameState;
import libs.UnitClassLib;
import model.Card;
import model.CardType;
import model.Position;
import model.SquareType;
import model.Unit;

public class HeuristicEvaluator implements IStateEvaluator {

	private static final double MAX_VAL = 60000;
	private boolean winVal;
	private List<Position> p1Healers;
	private List<Position> p2Healers;
	public boolean positional = false;
	
	public enum ParameterName {
		//healMultiplier,
		//spreadMultiplier,
		
		upMaxHpFactor,
		downMaxHpFactor,
		
		upSquareFactor,
		downSquareFactor,
		
		upEquipmentFactor,
		downEquipmentFactor,
		
		dragonscaleArcherValue,
		dragonscaleClericValue,
		dragonscaleKnightValue,
		dragonscaleNinjaValue,
		dragonscaleWizardValue,
		
		runemetalArcherValue,
		runemetalClericValue,
		runemetalKnightValue,
		runemetalNinjaValue,
		runemetalWizardValue,
		
		shiningHelmetArcherValue,
		shiningHelmetClericValue,
		shiningHelmetKnightValue,
		shiningHelmetNinjaValue,
		shiningHelmetWizardValue,
		
		scrollArcherValue,
		scrollClericValue,
		scrollKnightValue,
		scrollNinjaValue,
		scrollWizardValue,
		
		deploySquareValue,
		normalSquareValue,
		
		defenseSquareArcherValue,
		defenseSquareClericValue,
		defenseSquareKnightValue,
		defenseSquareNinjaValue,
		defenseSquareWizardValue,
		// Power
		powerSquareArcherValue,
		powerSquareClericValue,
		powerSquareKnightValue,
		powerSquareNinjaValue,
		powerSquareWizardValue,
		// Assault
		assaultSquareArcherValue,
		assaultSquareClericValue,
		assaultSquareKnightValue,
		assaultSquareNinjaValue,
		assaultSquareWizardValue
	}
	
	public HashMap<ParameterName, Double> Parameters;
	
	
	
	public HeuristicEvaluator(boolean winVal) {
		this.winVal = winVal;
		this.p1Healers = new ArrayList<Position>();
		this.p2Healers = new ArrayList<Position>();
		
		SetDefaultParameters();
	}
	
	public void SetDefaultParameters() {
		// Parameters
		//Parameters.put(ParameterName.healMultiplier, 150.0);
		//Parameters.put(ParameterName.spreadMultiplier, 50.0);

		Parameters.put(ParameterName.upMaxHpFactor, 2.0);
		Parameters.put(ParameterName.downMaxHpFactor, 1.0);

		Parameters.put(ParameterName.upSquareFactor, 1.0);
		Parameters.put(ParameterName.downSquareFactor, 0.0);
		
		Parameters.put(ParameterName.upEquipmentFactor, 2.0);
		Parameters.put(ParameterName.downEquipmentFactor, 1.0);
		
		// Equipment
		Parameters.put(ParameterName.dragonscaleArcherValue, 30.0);
		Parameters.put(ParameterName.dragonscaleClericValue, 30.0);
		Parameters.put(ParameterName.dragonscaleKnightValue, 30.0);
		Parameters.put(ParameterName.dragonscaleNinjaValue, 30.0);
		Parameters.put(ParameterName.dragonscaleWizardValue, 20.0);
		
		Parameters.put(ParameterName.runemetalArcherValue, 40.0);
		Parameters.put(ParameterName.runemetalClericValue, 20.0);
		Parameters.put(ParameterName.runemetalKnightValue, -50.0);
		Parameters.put(ParameterName.runemetalNinjaValue, 20.0);
		Parameters.put(ParameterName.runemetalWizardValue, 40.0);

		
		Parameters.put(ParameterName.shiningHelmetArcherValue, 20.0);
		Parameters.put(ParameterName.shiningHelmetClericValue, 20.0);
		Parameters.put(ParameterName.shiningHelmetKnightValue, 20.0);
		Parameters.put(ParameterName.shiningHelmetNinjaValue, 10.0);
		Parameters.put(ParameterName.shiningHelmetWizardValue, 20.0);

		Parameters.put(ParameterName.scrollArcherValue, 50.0);
		Parameters.put(ParameterName.scrollClericValue, 30.0);
		Parameters.put(ParameterName.scrollKnightValue, -40.0);
		Parameters.put(ParameterName.scrollNinjaValue, 40.0);
		Parameters.put(ParameterName.scrollWizardValue, 50.0);
		
		// Squares
		Parameters.put(ParameterName.deploySquareValue, -75.0);
		Parameters.put(ParameterName.normalSquareValue, 0.0);
		// Defense
		Parameters.put(ParameterName.defenseSquareArcherValue, 80.0);
		Parameters.put(ParameterName.defenseSquareClericValue, 20.0);
		Parameters.put(ParameterName.defenseSquareKnightValue, 30.0);
		Parameters.put(ParameterName.defenseSquareNinjaValue, 60.0);
		Parameters.put(ParameterName.defenseSquareWizardValue, 70.0);
		// Power
		Parameters.put(ParameterName.powerSquareArcherValue, 120.0);
		Parameters.put(ParameterName.powerSquareClericValue, 40.0);
		Parameters.put(ParameterName.powerSquareKnightValue, 30.0);
		Parameters.put(ParameterName.powerSquareNinjaValue, 70.0);
		Parameters.put(ParameterName.powerSquareWizardValue, 100.0);
		// Assault
		Parameters.put(ParameterName.assaultSquareArcherValue, 40.0);
		Parameters.put(ParameterName.assaultSquareClericValue, 10.0);
		Parameters.put(ParameterName.assaultSquareKnightValue, 120.0);
		Parameters.put(ParameterName.assaultSquareNinjaValue, 50.0);
		Parameters.put(ParameterName.assaultSquareWizardValue, 40.0);
	}

	public double eval(GameState state, boolean p1) {
		
		//findHealers(state);
		
		if (state.isTerminal){
			int winner = state.getWinner();
			if (!winVal && winner == 1)
				return p1 ? MAX_VAL : -MAX_VAL;
			else if (!winVal && winner == 2)
				return p1 ? -MAX_VAL : MAX_VAL;
			else if (winVal && winner == 1)
				return p1 ? 1 : 0;
			else if (winVal && winner == 2)
				return p1 ? 0 : 1;
			
			if (winVal)
				return 0.5;
			return 0;
		}
		
		double hpDif = hpDif(state, p1);
		
		if (!winVal)
			return hpDif;
		
		return NormUtil.normalize(hpDif, -MAX_VAL, MAX_VAL, 1, 0);

	}

	private void findHealers(GameState state) {
		p1Healers.clear();
		p2Healers.clear();
		for (int x = 0; x < state.map.width; x++)
			for (int y = 0; y < state.map.height; y++)
				if (state.units[x][y] != null && state.units[x][y].hp != 0 && state.units[x][y].unitClass.card == Card.CLERIC)
					if (state.units[x][y].p1Owner)
						p1Healers.add(SingletonAction.positions[x][y]);
					else
						p2Healers.add(SingletonAction.positions[x][y]);
	}

	private double hpDif(GameState state, boolean p1) {
		double p1Units = 0;
		double p2Units = 0;
		boolean up = true;
		for (int x = 0; x < state.map.width; x++){
			for (int y = 0; y < state.map.height; y++){
				if (state.units[x][y] != null){
					up = (state.units[x][y].hp > 0);
					if (state.units[x][y].p1Owner){
						p1Units += 
								state.units[x][y].hp
								+ state.units[x][y].unitClass.maxHP * (up ? Parameters.get(ParameterName.upMaxHpFactor) : Parameters.get(ParameterName.downMaxHpFactor))
								+ squareVal(state.map.squares[x][y], state.units[x][y].unitClass.card) * (up ? Parameters.get(ParameterName.upSquareFactor): Parameters.get(ParameterName.downSquareFactor))
								//- healDistance(state, x, y) * healMultiplier
								+ equipmentValue(state.units[x][y]) * (up ? Parameters.get(ParameterName.upEquipmentFactor) : Parameters.get(ParameterName.downEquipmentFactor))
								;
								//- spread(state, x, y) * spreadMultiplier;
					} else {
						p2Units += 
								state.units[x][y].hp
								+ state.units[x][y].unitClass.maxHP * (up ? Parameters.get(ParameterName.upMaxHpFactor) : Parameters.get(ParameterName.downMaxHpFactor))
								+ squareVal(state.map.squares[x][y], state.units[x][y].unitClass.card) * (up ? Parameters.get(ParameterName.upSquareFactor): Parameters.get(ParameterName.downSquareFactor))
								//- healDistance(state, x, y) * healMultiplier
								+ equipmentValue(state.units[x][y])  * (up ? Parameters.get(ParameterName.upEquipmentFactor) : Parameters.get(ParameterName.downEquipmentFactor))
								;
								//- spread(state, x, y) * spreadMultiplier;
					}
				}
			}
		}
		
		// DECK AND HAND UNITS
		int p1Inferno = 0;
		int p2Inferno = 0;
		int p1Potions = 0;
		int p2Potions = 0;
		for (final Card card : Card.values()){
			if (card.type != CardType.UNIT){
				if (card == Card.INFERNO){
					p1Inferno = state.p1Hand.count(card) + state.p1Deck.count(card);
					p2Inferno = state.p2Hand.count(card) + state.p2Deck.count(card);
				}
				continue;
			}
			p1Units += state.p1Deck.count(card) * UnitClassLib.lib.get(card).maxHP * 1.75;
			p2Units += state.p2Deck.count(card) * UnitClassLib.lib.get(card).maxHP * 1.75;
			p1Units += state.p1Hand.count(card) * UnitClassLib.lib.get(card).maxHP * 1.75;
			p2Units += state.p2Hand.count(card) * UnitClassLib.lib.get(card).maxHP * 1.75;
		}
		
		// INFERNO + POTIONS
		int sp1 = p1Inferno * 750 + p1Potions * 600;
		int sp2 = p2Inferno * 750 + p2Potions * 600;
		
		if (p1)
			return (p1Units + sp1) - (p2Units + sp2);
		return (p2Units + sp1) - (p1Units + sp2);
	}

	private double spread(GameState state, int x, int y) {
		if (!positional || state.units[x][y].unitClass.card == Card.CRYSTAL)
			return 0;
		double c = 0;
		boolean p1 = state.units[x][y].p1Owner;
		if (x+1 < state.map.width){
			if (y+1 < state.map.height)
				if (state.units[x+1][y+1] != null && state.units[x+1][y+1].p1Owner == p1)
					if (state.units[x+1][y+1].unitClass.card != Card.CRYSTAL)
						c+=1;
			if (y-1 >= 0)
				if (state.units[x+1][y-1] != null && state.units[x+1][y-1].p1Owner == p1)
					if (state.units[x+1][y-1].unitClass.card != Card.CRYSTAL)
						c+=1;
			if (state.units[x+1][y] != null && state.units[x+1][y].p1Owner == p1)
				if (state.units[x+1][y].unitClass.card != Card.CRYSTAL)
					c+=1;
		}
		if (x-1 >= 0){
			if (y+1 < state.map.height)
				if (state.units[x-1][y+1] != null && state.units[x-1][y+1].p1Owner == p1)
					if (state.units[x-1][y+1].unitClass.card != Card.CRYSTAL)
						c+=1;
			if (y-1 >= 0)
				if (state.units[x-1][y-1] != null && state.units[x-1][y-1].p1Owner == p1)
					if (state.units[x-1][y-1].unitClass.card != Card.CRYSTAL)
						c+=1;
			if (state.units[x-1][y] != null && state.units[x-1][y].p1Owner == p1)
				if (state.units[x-1][y].unitClass.card != Card.CRYSTAL)
					c+=1;
		}
		if (y+1 < state.map.height)
			if (state.units[x][y+1] != null && state.units[x][y+1].p1Owner == p1)
				if (state.units[x][y+1].unitClass.card != Card.CRYSTAL)
					c+=1;
		if (y-1 >= 0)
			if (state.units[x][y-1] != null && state.units[x][y-1].p1Owner == p1)
				if (state.units[x][y-1].unitClass.card != Card.CRYSTAL)
					c+=1;
		
		if (x == 0 || x == state.map.width-1)
			c+=8;
		
		return c;
	}

	private double equipmentValue(Unit unit) {
		double val = 0;
		for(Card card : unit.equipment){
			if (card == Card.DRAGONSCALE){
				if (unit.unitClass.card == Card.ARCHER)
					val += Parameters.get(ParameterName.dragonscaleArcherValue);
				else if (unit.unitClass.card == Card.CLERIC)
					val += Parameters.get(ParameterName.dragonscaleKnightValue);
				else if (unit.unitClass.card == Card.KNIGHT)
					val += Parameters.get(ParameterName.dragonscaleKnightValue);
				else if(unit.unitClass.card == Card.NINJA)
					val += Parameters.get(ParameterName.dragonscaleNinjaValue);
				else if(unit.unitClass.card == Card.WIZARD)
					val += Parameters.get(ParameterName.dragonscaleWizardValue);				
			} else if (card == Card.RUNEMETAL){
				if (unit.unitClass.card == Card.ARCHER)
					val += Parameters.get(ParameterName.runemetalArcherValue);
				else if (unit.unitClass.card == Card.CLERIC)
					val += Parameters.get(ParameterName.runemetalClericValue);
				else if (unit.unitClass.card == Card.KNIGHT)
					val += Parameters.get(ParameterName.runemetalKnightValue);
				else if(unit.unitClass.card == Card.NINJA)
					val += Parameters.get(ParameterName.runemetalNinjaValue);
				else if(unit.unitClass.card == Card.WIZARD)
					val += Parameters.get(ParameterName.runemetalWizardValue);				
			} else if (card == Card.SHINING_HELM){
				if (unit.unitClass.card == Card.ARCHER)
					val += Parameters.get(ParameterName.shiningHelmetArcherValue);
				else if (unit.unitClass.card == Card.CLERIC)
					val += Parameters.get(ParameterName.shiningHelmetClericValue);
				else if (unit.unitClass.card == Card.KNIGHT)
					val += Parameters.get(ParameterName.shiningHelmetKnightValue);
				else if(unit.unitClass.card == Card.NINJA)
					val += Parameters.get(ParameterName.shiningHelmetNinjaValue);
				else if(unit.unitClass.card == Card.WIZARD)
					val += Parameters.get(ParameterName.shiningHelmetWizardValue);	
			} else if (card == Card.SCROLL){
				if (unit.unitClass.card == Card.ARCHER)
					val += Parameters.get(ParameterName.scrollArcherValue);
				else if (unit.unitClass.card == Card.CLERIC)
					val += Parameters.get(ParameterName.scrollClericValue);
				else if (unit.unitClass.card == Card.KNIGHT)
					val += Parameters.get(ParameterName.scrollKnightValue);
				else if(unit.unitClass.card == Card.NINJA)
					val += Parameters.get(ParameterName.scrollNinjaValue);
				else if(unit.unitClass.card == Card.WIZARD)
					val += Parameters.get(ParameterName.scrollWizardValue);	
			}
		}
		return val;
	}

	private double healDistance(GameState state, int x, int y) {
		
		if (!positional || state.units[x][y].unitClass.card == Card.CRYSTAL)
			return 0;
		boolean p1 = state.units[x][y].p1Owner;
		
		double shortest = 10000;
		double dis = 0;
		if (p1){
			for(Position pos : p1Healers){
				dis = pos.distance(SingletonAction.positions[x][y]);
				if (dis < shortest)
					shortest = dis;
			}
		} else {
			for(Position pos : p2Healers){
				dis = pos.distance(SingletonAction.positions[x][y]);
				if (dis < shortest)
					shortest = dis;
			}
		}
		
		if (p1){
			for(Position pos : state.map.p1DeploySquares)
				dis = pos.distance(SingletonAction.positions[x][y]);
				if (dis+2 < shortest)
					shortest = dis;
		}else{ 
			for(Position pos : state.map.p2DeploySquares)
				dis = pos.distance(SingletonAction.positions[x][y]);
				if (dis+2 < shortest)
					shortest = dis;
		}
		
		return Math.max(0, shortest-2);
		
	}

	private double squareVal(SquareType square, Card card) {
		
		switch(square){
		case ASSAULT : return assaultValue(card);
		case DEPLOY_1 : return Parameters.get(ParameterName.deploySquareValue);
		case DEPLOY_2 : return Parameters.get(ParameterName.deploySquareValue);
		case DEFENSE : return defenseValue(card);
		case POWER : return powerValue(card);
		case NONE : return Parameters.get(ParameterName.normalSquareValue);
		}
		
		return 0;
		
	}
	
	private double defenseValue(Card card) {
		switch(card){
		case ARCHER : return Parameters.get(ParameterName.defenseSquareArcherValue);
		case CLERIC : return Parameters.get(ParameterName.defenseSquareClericValue);
		case KNIGHT : return Parameters.get(ParameterName.defenseSquareKnightValue);		
		case NINJA : return Parameters.get(ParameterName.defenseSquareNinjaValue);
		case WIZARD : return Parameters.get(ParameterName.defenseSquareWizardValue);
		default : return 0;
		}
	}
	
	private double powerValue(Card card) {
		switch(card){
		case ARCHER : return Parameters.get(ParameterName.powerSquareArcherValue);
		case CLERIC : return Parameters.get(ParameterName.powerSquareClericValue);
		case KNIGHT : return Parameters.get(ParameterName.powerSquareKnightValue);		
		case NINJA : return Parameters.get(ParameterName.powerSquareNinjaValue);
		case WIZARD : return Parameters.get(ParameterName.powerSquareWizardValue);
		default : return 0;
		}
	}

	private double assaultValue(Card card) {
		switch(card){
		case ARCHER : return Parameters.get(ParameterName.assaultSquareArcherValue);
		case CLERIC : return Parameters.get(ParameterName.assaultSquareClericValue);
		case KNIGHT : return Parameters.get(ParameterName.assaultSquareKnightValue);		
		case NINJA : return Parameters.get(ParameterName.assaultSquareNinjaValue);
		case WIZARD : return Parameters.get(ParameterName.assaultSquareWizardValue);
		default : return 0;
		}
	}

	@Override
	public double normalize(double delta) {
		return NormUtil.normalize(delta, -MAX_VAL, MAX_VAL, 1, 0);
	}

	@Override
	public String title() {
		//return "Heuristic Evaluator [positional=" + positional + ", heal="+Parameters.get(ParameterName.healMultiplier)+", spread="+Parameters.get(ParameterName.spreadMultiplier)+"]";
		return "Heuristic Evaluator [positional=" + positional + "]";
	}

	@Override
	public IStateEvaluator copy() {
		return new HeuristicEvaluator(winVal);
	}
}
