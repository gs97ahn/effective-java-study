package ahn.chapter5.item31.chooser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Chooser<T> {

    private final List<T> choiceList;

    public Chooser(Collection<? extends T> choices) {
        choiceList = new ArrayList<>(choices);
    }
}
