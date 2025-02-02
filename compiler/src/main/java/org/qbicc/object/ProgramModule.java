package org.qbicc.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.smallrye.common.constraint.Assert;
import org.qbicc.graph.literal.LiteralFactory;
import org.qbicc.type.TypeSystem;
import org.qbicc.type.definition.DefinedTypeDefinition;

/**
 *
 */
public final class ProgramModule {
    final DefinedTypeDefinition typeDefinition;
    final TypeSystem typeSystem;
    final LiteralFactory literalFactory;
    final Map<String, Section> sections = new ConcurrentHashMap<>();
    final List<GlobalXtor> ctors = new ArrayList<>();
    final List<GlobalXtor> dtors = new ArrayList<>();

    public ProgramModule(final DefinedTypeDefinition typeDefinition, final TypeSystem typeSystem, final LiteralFactory literalFactory) {
        this.typeDefinition = typeDefinition;
        this.typeSystem = Assert.checkNotNullParam("typeSystem", typeSystem);
        this.literalFactory = Assert.checkNotNullParam("literalFactory", literalFactory);
    }

    public DefinedTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }

    public Section getSectionIfExists(String name) {
        return sections.get(name);
    }

    public Section getOrAddSection(String name) {
        return sections.computeIfAbsent(name, n -> new Section(n, typeSystem.getVoidType(), this));
    }

    public Iterable<Section> sections() {
        Section[] array = this.sections.values().toArray(Section[]::new);
        Arrays.sort(array, Comparator.comparing(ProgramObject::getName));
        return List.of(array);
    }

    public List<GlobalXtor> constructors() {
        synchronized (ctors) {
            return List.copyOf(ctors);
        }
    }

    public List<GlobalXtor> destructors() {
        synchronized (dtors) {
            return List.copyOf(dtors);
        }
    }

    public GlobalXtor addConstructor(Function fn, int priority) {
        Assert.checkNotNullParam("fn", fn);
        Assert.checkMinimumParameter("priority", 0, priority);
        GlobalXtor xtor = new GlobalXtor(GlobalXtor.Kind.CTOR, fn, priority);
        synchronized (ctors) {
            ctors.add(xtor);
        }
        return xtor;
    }

    public GlobalXtor addDestructor(Function fn, int priority) {
        Assert.checkNotNullParam("fn", fn);
        Assert.checkMinimumParameter("priority", 0, priority);
        GlobalXtor xtor = new GlobalXtor(GlobalXtor.Kind.DTOR, fn, priority);
        synchronized (dtors) {
            dtors.add(xtor);
        }
        return xtor;
    }
}
