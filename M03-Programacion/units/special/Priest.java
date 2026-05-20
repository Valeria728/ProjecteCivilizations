package units.special;

import units.SpecialUnit;

public class Priest extends SpecialUnit{

    public Priest() {
        super(0, 0);
    }

    
    public int getFoodCost() { 
    	return FOOD_COST_PRIEST; 
    }

    
    public int getWoodCost() { 
    	return WOOD_COST_PRIEST; 
    }

    
    public int getIronCost() { 
    	return IRON_COST_PRIEST; 
    }

    
    public int getManaCost() { 
    	return MANA_COST_PRIEST; 
    }

   
    public int getChanceGeneratingWaste() {
    	return CHANCE_GENERATNG_WASTE_PRIEST; 
    }

    
    public int getChanceAttackAgain() { 
    	return CHANCE_ATTACK_AGAIN_PRIEST; 
    }

    
    public String toString() { 
    	return "Priest"; 
    }
}
