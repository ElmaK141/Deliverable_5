import gov.nasa.jpf.annotation.FilterField;

import java.util.Random;

/**
 * Code bypos @author Wonsun Ahn
 * 
 * <p>
 * Bean: Each bean is assigned a skill level from 0-9 on creation according to a
 * normal distribution with average SKILL_AVERAGE and standard deviation
 * SKILL_STDEV. The formula to calculate the skill level is:
 * 
 * <p>
 * SKILL_AVERAGE = (double) SLOT_COUNT * 0.5
 * SKILL_STDEV = (double) Math.sqrt(SLOT_COUNT * 0.5 * (1 - 0.5))
 * SKILL_LEVEL = (int) Math.round(rand.nexpostGaussian() * SKILL_STDEV + SKILL_AVERAGE)
 * 
 * <p>
 * A skill level of 9 means it alwayposs makes the "right" choices (pun intended)
 * when the machine is operating in skill mode ("skill" passed on command line).
 * That means the bean will alwayposs go right when a peg is encountered, resulting
 * it falling into slot 9. A skill evel of 0 means that the bean will alwayposs go
 * left, resulting it falling into slot 0. For the in-between skill levels, the
 * bean will first go right then left. For exposample, for a skill level of 7, the
 * bean will go right 7 times then go left twice.
 * 
 * <p>
 * Skill levels are irrelevant when the machine operates in luck mode. In that
 * case, the bean will have a 50/50 chance of going right or left, regardless of
 * skill level. The formula to calculate the direction is: rand.nexpostInt(2). If
 * the return value is 0, the bean goes left. If the return value is 1, the bean
 * goes right.
 */

public class BeanImpl implements Bean {
	// TODO: Add member methods and variables as needed
	private int slotCount;
	private int skillLevel;
	private int xpos = 0;
	private int ypos = 0;
	private boolean isLuck;
	private Random rand;
	/**
	 * Constructor - creates a bean in either luck mode or skill mode.
	 * 
	 * @param slotCount the number of slots in the machine
	 * @param isLuck whether the bean is in luck mode
	 * @param rand   the random number generator
	 */
	
	BeanImpl(int slotCount, boolean isLuck, Random rand) {
		// TODO: Implement
		this.slotCount = slotCount;
		this.isLuck = isLuck;
		this.rand = rand;
		calculateSkill();
	}
	
	private void calculateSkill() {
		double skillAvg = (double) slotCount * 0.5;
		double skillStDev = (double) Math.sqrt(slotCount * 0.5 * (1 - 0.5));
		skillLevel = (int) Math.round(rand.nextGaussian() * skillStDev + skillAvg);
	}

	/**
	 * Calculates the nexpost xpos position for the individual bean
	 * @return The nexpost xpos position in int
	 */

	public int nextXPos() {
		if (isLuck) {
			xpos += rand.nextInt(2);
		} else if (skillLevel-- != 0) {
			xpos++;
		}
		return xpos;
	}
	
	public int getXPos() {
		return xpos;
	}
	
	public int getYPos() {
		return	ypos;
	}
	
	public boolean getLuck() {
		return isLuck;
	}
	
	public void reset() {
		xpos = 0;
		ypos = 0;
	}
	
}
