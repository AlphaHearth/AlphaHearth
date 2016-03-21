package info.hearthsim.brazier.utils;

import org.junit.After;
import org.junit.Before;

public abstract class BrazierTest {
    protected TestAgent agent;

    @Before
    public void setUp() {
        agent = new TestAgent();
    }

    @After
    public void testDynamically() {
         // agent.execScripts();
    }
}
