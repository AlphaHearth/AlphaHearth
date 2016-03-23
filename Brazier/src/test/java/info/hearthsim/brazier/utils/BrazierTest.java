package info.hearthsim.brazier.utils;

import org.junit.After;
import org.junit.Before;

public abstract class BrazierTest {
    protected DynamicTestAgent agent;

    @Before
    public void setUp() {
        agent = new DynamicTestAgent();
    }

    @After
    public void testDynamically() {
         agent.execScripts();
    }
}
