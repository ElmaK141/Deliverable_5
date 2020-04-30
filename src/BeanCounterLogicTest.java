import static org.junit.Assert.*;

import gov.nasa.jpf.vm.Verify;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>Uses the Java Path Finder model checking tool to check BeanCounterLogic in
 * various modes of operation. It checks BeanCounterLogic in both "luck" and
 * "skill" modes for various numbers of slots and beans. It also goes down all
 * the possible random path taken by the beans during operation.
 */

public class BeanCounterLogicTest {
	private static BeanCounterLogic logic; // The core logic of the program
	private static Bean[] beans; // The beans in the machine
	private static String failString; // A descriptive fail string for assertions

	private static int slotCount; // The number of slots in the machine we want to test
	private static int beanCount; // The number of beans in the machine we want to test
	private static boolean isLuck; // Whether the machine we want to test is in "luck" or "skill" mode

	/**
	 * Sets up the test fixture.
	 */
	@BeforeClass
	public static void setUp() {
		
		slotCount = Verify.getInt(1, 5);
		beanCount = Verify.getInt(0, 3);
		isLuck = Verify.getBoolean();
				
		// Create the internal logic
		logic = BeanCounterLogic.createInstance(slotCount);
		// Create the beans
		beans = new Bean[beanCount];
		for (int i = 0; i < beanCount; i++) {
			beans[i] = Bean.createInstance(slotCount, isLuck, new Random());
		}
		
		// A failstring useful to pass to assertions to get a more descriptive error.
		failString = "Failure in (slotCount=" + slotCount + ", beanCount=" + beanCount
				+ ", isLucky=" + isLuck + "):";
	}

	@AfterClass
	public static void tearDown() {
	}

	/**
	 * Test case for void void reset(Bean[] beans).
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 * Invariants: If beanCount is greater than 0,
	 *             remaining bean count is beanCount - 1
	 *             in-flight bean count is 1 (the bean initially at the top)
	 *             in-slot bean count is 0.
	 *             If beanCount is 0,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is 0.
	 */
	@Test
	public void testReset() {
		logic.reset(beans);
		int inSlotBeanCount = 0;
		int inFlightBeanCount = 0;
		for (int i = 0; i < slotCount; i++) {
			inSlotBeanCount += logic.getSlotBeanCount(i);
			if (logic.getInFlightBeanXPos(slotCount) != -1) {
				inFlightBeanCount++;
			}
		}
		if (beanCount > 0) {
			assert logic.getRemainingBeanCount() == beanCount - 1;
			assert inFlightBeanCount == 1;
			
		}
		if (beanCount == 0) {
			assert logic.getRemainingBeanCount() == 0;
			assert inFlightBeanCount == 0;
		}
		assert inSlotBeanCount == 0;
		
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             all positions of in-flight beans are legal positions in the logical coordinate system.
	 */
	@Test
	public void testAdvanceStepCoordinates() {
		logic.reset(beans);
		boolean tmp;
		
		do {
			tmp = logic.advanceStep();
			int tmpXPos;
			for (int i = 0; i < slotCount; i++) {
				tmpXPos = logic.getInFlightBeanXPos(i);
				assert tmpXPos <= i;
			}
		}
		while (tmp);
	}
	
	/**
	 * Test case for double getAverageSlotBeanCount()
	 * Preconditions: None
	 * Execution Steps: Call logic.reset(beans).
	 * 					Call logic.advanceStep() in a loop until it returns false
	 * Invariant: Once all beans have been distributed
	 * 			  The beans in the slot on either end of the slots will have less than the average
	 * 			  And the slot in the middle will have more than the average
	 */
	public void testAverageSlotBeanCount() {
		logic.reset(beans);
		while (true) { 
			if (!logic.advanceStep()) {
				break;
			}
		}
		assert logic.getSlotBeanCount(0) < logic.getAverageSlotBeanCount();
		assert logic.getSlotBeanCount(slotCount - 1) < logic.getAverageSlotBeanCount();
		assert logic.getSlotBeanCount((int)slotCount / 2) > logic.getAverageSlotBeanCount();
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             the sum of remaining, in-flight, and in-slot beans is equal to beanCount.
	 */
	@Test
	public void testAdvanceStepBeanCount() {
		logic.reset(beans);
		boolean tmp;
		int inFlight = 0;
		int inSlot = 0; 
		do {
			tmp = logic.advanceStep();
			for (int i = 0; i < slotCount; i++) {
				if (logic.getInFlightBeanXPos(i) != -1) {
					inFlight++;
				}
				inSlot += logic.getSlotBeanCount(i);
			}
			assert (logic.getRemainingBeanCount() + inFlight + inSlot) == beanCount;
		}
		while (tmp);
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 */
	@Test
	public void testAdvanceStepPostCondition() {
		logic.reset(beans);
		boolean tmp;
		do { 
			tmp = logic.advanceStep(); 
		}
		while (tmp);
		assert logic.getRemainingBeanCount() == 0;
		for (int i = 0; i < slotCount; i++) {
			assert logic.getInFlightBeanXPos(i) == -1;
			assert logic.getSlotBeanCount(i) == 0;
		}
	}
	
	/**
	 * Test case for void lowerHalf().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.lowerHalf().
	 * Invariants: After calling logic.lowerHalf(),
	 *             slots in the machine contain only the lower half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.lowerHalf().
	 */
	@Test
	public void testLowerHalf() {
		logic.reset(beans);
		boolean tmp;
		do {
			tmp = logic.advanceStep();
		}
		while (tmp);
		int[] expectedBeans = new int[slotCount];
		int slotNum;
		for (int i = 0; i < slotCount; i++) {
			slotNum = logic.getSlotBeanCount(i);
			if (slotNum % 2 == 0) {
				expectedBeans[i] = slotNum / 2;
			} else {
				expectedBeans[i] = (slotNum + 1) / 2;
			}
		}
		logic.lowerHalf();
		
		for (int i = 0; i < slotCount; i++) {
			assert expectedBeans[i] == logic.getSlotBeanCount(i);
		}
	}
	
	/**
	 * Test case for void upperHalf().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.lowerHalf().
	 * Invariants: After calling logic.upperHalf(),
	 *             slots in the machine contain only the upper half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.upperHalf().
	 */
	@Test
	public void testUpperHalf() {
		// TODO: Implement
		logic.reset(beans);
		boolean tmp;
		do {
			tmp = logic.advanceStep();
		}
		while (tmp);
		int[] expectedBeans = new int[slotCount];
		int slotNum;
		for (int i = 0; i < slotCount; i++) {
			slotNum = logic.getSlotBeanCount(i);
			if (slotNum % 2 == 0) {
				expectedBeans[i] = slotNum / 2;
			} else {
				expectedBeans[i] = (slotNum + 1) / 2;
			}
		}
		logic.upperHalf();
		
		for (int i = 0; i < slotCount; i++) {
			assert expectedBeans[i] == logic.getSlotBeanCount(i);
		}
	}
	
	/**
	 * Test case for void repeat().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.repeat();
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: If the machine is operating in skill mode,
	 *             bean count in each slot is identical after the first run and second run of the machine. 
	 */
	@Test
	public void testRepeat() {
		// TODO: Implement
		logic.reset(beans);
		boolean loop = false;
		while (loop) { 
			loop = logic.advanceStep(); 
		}
		
		int[] firstRunBeans = new int[slotCount];
		for (int i = 0; i < slotCount; i++) { 
			firstRunBeans[i] = logic.getSlotBeanCount(i); 
		}
		
		logic.reset(beans);
		loop = false;
		while (loop) { 
			loop = logic.advanceStep();
		}
		
		if (isLuck == false) {
			for (int i = 0; i < slotCount; i++) { 
				assert firstRunBeans[i] == logic.getSlotBeanCount(i);
			}
		}
	}
}
