package de.werft;

import de.hpi.rdf.tailrapi.Delta;
import de.hpi.rdf.tailrapi.Memento;
import de.hpi.rdf.tailrapi.Repository;
import de.hpi.rdf.tailrapi.Tailr;
import org.apache.http.StatusLine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by ratzeputz on 14.07.16.
 */
public class TailrStub implements Tailr {


    @Override
    public List<Repository> getUserRepositories() throws IOException {
        return null;
    }

    @Override
    public List<Repository> getUserRepositories(String s) throws IOException {
        return null;
    }

    @Override
    public List<String> getRepositoryKeys(Repository repository) {
        return null;
    }

    @Override
    public List<Memento> getMementos(Repository repository, String s) throws IOException {
        return null;
    }

    @Override
    public Memento getLatestMemento(Repository repository, String s) {
        return null;
    }

    @Override
    public StatusLine deleteMemento(Memento memento) throws IOException {
        return null;
    }

    @Override
    public Delta getDelta(Memento memento) throws IOException, URISyntaxException {
        return null;
    }

    @Override
    public Delta getLatestDelta(Repository repository, String s) throws IOException, URISyntaxException {
        return null;
    }

    @Override
    public Delta putMemento(Repository repository, String s, String s1) throws IOException, URISyntaxException {
        return null;
    }
}
