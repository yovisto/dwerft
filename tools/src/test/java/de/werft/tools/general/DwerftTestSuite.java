package de.werft.tools.general;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.werft.tools.exporter.ExporterTest;

@RunWith(Suite.class)
@SuiteClasses({ ExporterTest.class})
public class DwerftTestSuite { }
