package de.dwerft.lpdc.general;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.dwerft.lpdc.exporter.ExporterTest;
import de.dwerft.lpdc.importer.preproducer.PreProducerGeneratorTest;
import de.dwerft.lpdcimport.dramaqueen.DramaQueenConverterTest;

@RunWith(Suite.class)
@SuiteClasses({ ExporterTest.class, PreProducerGeneratorTest.class,  DramaQueenConverterTest.class})
public class DwerftTestSuite { }
