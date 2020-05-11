package cc.quarkus.qcc.graph.build;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import cc.quarkus.qcc.graph.node.ControlNode;
import cc.quarkus.qcc.type.TypeDescriptor;

public class PhiPlacements {

    public PhiPlacements() {

    }

    public void record(ControlNode<?> src, int index, TypeDescriptor<?> type) {
        Set<Entry> set = this.indexes.computeIfAbsent(src, k -> new HashSet<>());
        set.add(new Entry(index, type));
    }

    public void forEach(BiConsumer<ControlNode<?>, Set<Entry>> fn) {
        this.indexes.forEach(fn);
    }

    private Map<ControlNode<?>, Set<Entry>> indexes = new HashMap<>();

    static class Entry {
        Entry(int index, TypeDescriptor<?> type) {
            this.index = index;
            this.type = type;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "index=" + index +
                    ", type=" + type +
                    '}';
        }

        final int index;
        final TypeDescriptor<?> type;
    }

}
