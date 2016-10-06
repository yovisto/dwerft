package de.werft.tools.general;

import de.werft.tools.rmllib.RmlMapperTest;
import de.werft.tools.rmllib.preprocessing.BasicPreprocessorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Dwerft test suite
 *
 * All test classes collected.
 *
 * @author Henrik (juerges.henrik@gmail.com)
 */
@RunWith(Suite.class)
@SuiteClasses({RmlMapperTest.class, BasicPreprocessorTest.class})
public class DwerftTestSuite { }
