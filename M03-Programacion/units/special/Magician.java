package units.special;
import units.SpecialUnit;

public class Magician extends SpecialUnit {

    public Magician(int technologyAttack) {
        super(0,BASE_DAMAGE_MAGICIAN + (technologyAttack * PLUS_ATTACK_MAGICIAN_BY_TECHNOLOGY * BASE_DAMAGE_MAGICIAN / 100));
    }

    
    public int getFoodCost() { 
    	return FOOD_COST_MAGICIAN; 
    }

    
    public int getWoodCost() { 
    	return WOOD_COST_MAGICIAN; 
    }

    
    public int getIronCost() { 
    	return IRON_COST_MAGICIAN; 
    }

   
    public int getManaCost() { 
    	return MANA_COST_MAGICIAN; 
    }

    
    public int getChanceGeneratingWaste() { 
    	return CHANCE_GENERATNG_WASTE_MAGICIAN; 
    }

    
    public int getChanceAttackAgain() { 
    	return CHANCE_ATTACK_AGAIN_MAGICIAN; 
    }

   
    public String toString() { 
    	return "Magician";    	
    }
}
