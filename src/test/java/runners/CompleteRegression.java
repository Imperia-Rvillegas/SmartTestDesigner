package runners;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite para ejecutar todos los runners de pruebas.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TrackingChangesBetweenCycles.class,
        TrackingChangesInTheSameCycle.class
})
public class CompleteRegression {
}