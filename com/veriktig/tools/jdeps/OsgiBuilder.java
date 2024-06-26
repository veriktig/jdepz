/*
 * Copyright (c) 2015, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.veriktig.tools.jdeps;

import static com.veriktig.tools.jdeps.JdepsTask.*;
import static com.veriktig.tools.jdeps.Analyzer.*;
import static com.veriktig.tools.jdeps.JdepsFilter.DEFAULT_FILTER;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Exports;
import java.lang.module.ModuleDescriptor.Provides;
import java.lang.module.ModuleDescriptor.Requires;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.util.stream.Collectors.*;


public class OsgiBuilder {
    final JdepsConfiguration configuration;

    final DependencyFinder dependencyFinder;
    final Analyzer analyzer;

    // an input JAR file (loaded as an automatic module for analysis)
    // maps to a normal module to generate module-info.java
    final Map<Module, Module> automaticToNormalModule;
    public OsgiBuilder(JdepsConfiguration configuration,
                             List<String> args) {
        this.configuration = configuration;
        this.dependencyFinder = new DependencyFinder(configuration, DEFAULT_FILTER);
        this.analyzer = new Analyzer(configuration, Type.CLASS, DEFAULT_FILTER);

        // add targets to modulepath if it has module-info.class
        List<Path> paths = args.stream()
            .map(fn -> Paths.get(fn))
            .toList();

        // automatic module to convert to normal module
        this.automaticToNormalModule = ModuleFinder.of(paths.toArray(new Path[0]))
                .findAll().stream()
                .map(configuration::toModule)
                .collect(toMap(Function.identity(), Function.identity()));

        Optional<Module> om = automaticToNormalModule.keySet().stream()
                                    .filter(m -> !m.descriptor().isAutomatic())
                                    .findAny();
        if (om.isPresent()) {
            throw new UncheckedBadArgs(new BadArgs("err.genmoduleinfo.not.jarfile",
                                                   om.get().getPathName()));
        }
        if (automaticToNormalModule.isEmpty()) {
            throw new UncheckedBadArgs(new BadArgs("err.invalid.path", args));
        }
    }

    public boolean run(boolean ignoreMissingDeps, PrintWriter log, boolean quiet) throws IOException {
        try {
            // pass 1: find API dependencies
            Map<Archive, Set<Archive>> requiresTransitive = computeRequiresTransitive();

            // pass 2: analyze all class dependences
            dependencyFinder.parse(automaticModules().stream());

            analyzer.run(automaticModules(), dependencyFinder.locationToArchive());

            for (Module m : automaticModules()) {
                Set<Archive> apiDeps = requiresTransitive.containsKey(m)
                                            ? requiresTransitive.get(m)
                                            : Collections.emptySet();

                // if this is a multi-release JAR, write to versions/$VERSION/module-info.java
                Runtime.Version version = configuration.getVersion();

                // computes requires and requires transitive
                Module normalModule = toNormalModule(m, apiDeps, ignoreMissingDeps);
                if (normalModule != null) {
                    automaticToNormalModule.put(m, normalModule);

                    printModuleInfo(log, normalModule.descriptor());
                } else {
                    // find missing dependences
                    return false;
                }
            }

        } finally {
            dependencyFinder.shutdown();
        }
        return true;
    }

    private Module toNormalModule(Module module, Set<Archive> requiresTransitive, boolean ignoreMissingDeps)
        throws IOException
    {
        // done analysis
        module.close();

        if (!ignoreMissingDeps && analyzer.requires(module).anyMatch(Analyzer::notFound)) {
            // missing dependencies
            return null;
        }

        Map<String, Boolean> requires = new HashMap<>();
        requiresTransitive.stream()
            .filter(a -> !(ignoreMissingDeps && Analyzer.notFound(a)))
            .map(Archive::getModule)
            .forEach(m -> requires.put(m.name(), Boolean.TRUE));

        analyzer.requires(module)
            .filter(a -> !(ignoreMissingDeps && Analyzer.notFound(a)))
            .map(Archive::getModule)
            .forEach(d -> requires.putIfAbsent(d.name(), Boolean.FALSE));

        return module.toNormalModule(requires);
    }

    /**
     * Returns the stream of resulting modules
     */
    Stream<Module> modules() {
        return automaticToNormalModule.values().stream();
    }

    /**
     * Returns the stream of resulting ModuleDescriptors
     */
    public Stream<ModuleDescriptor> descriptors() {
        return automaticToNormalModule.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .map(Module::descriptor);
    }

    void visitMissingDeps(Analyzer.Visitor visitor) {
        automaticModules().stream()
            .filter(m -> analyzer.requires(m).anyMatch(Analyzer::notFound))
            .forEach(m -> {
                analyzer.visitDependences(m, visitor, Analyzer.Type.VERBOSE, Analyzer::notFound);
            });
    }

    private void printModuleInfo(PrintWriter writer, ModuleDescriptor md) {
        writer.format("Export-Package:");
        TreeSet<ModuleDescriptor.Exports> tree = new TreeSet<ModuleDescriptor.Exports>();
        tree.addAll(md.exports());
        Iterator<Exports> iterator = tree.iterator();
        while (iterator.hasNext()) {
            Exports exp = iterator.next();
            writer.format("%s", exp.source());
            if (iterator.hasNext()) {
                writer.format(",");
            }
        }
        writer.format("%n");
    }

    private Set<Module> automaticModules() {
        return automaticToNormalModule.keySet();
    }

    /**
     * Returns a string containing the given set of modifiers and label.
     */
    private static <M> String toString(Set<M> mods, String what) {
        return (Stream.concat(mods.stream().map(e -> e.toString().toLowerCase(Locale.US)),
                              Stream.of(what)))
                      .collect(Collectors.joining(" "));
    }

    /**
     * Compute 'requires transitive' dependences by analyzing API dependencies
     */
    private Map<Archive, Set<Archive>> computeRequiresTransitive()
        throws IOException
    {
        // parse the input modules
        dependencyFinder.parseExportedAPIs(automaticModules().stream());

        return dependencyFinder.dependences();
    }
}
