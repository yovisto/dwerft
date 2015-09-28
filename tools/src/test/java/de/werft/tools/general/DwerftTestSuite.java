package de.werft.tools.general;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.werft.tools.exporter.ExporterTest;
import de.werft.tools.importer.dramaqueen.DramaQueenConverterTest;
import de.werft.tools.importer.preproducer.PreProducerGeneratorTest;

@RunWith(Suite.class)
@SuiteClasses({ ExporterTest.class, PreProducerGeneratorTest.class,  DramaQueenConverterTest.class})
public class DwerftTestSuite { }
