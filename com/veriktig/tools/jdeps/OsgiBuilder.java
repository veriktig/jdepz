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

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Exports;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class OsgiBuilder extends ModuleInfoBuilder {
    public OsgiBuilder(JdepsConfiguration configuration,
                             List<String> args,
                             Path outputdir,
                             boolean open) {
        super(configuration, args, outputdir, open);
    }

    public boolean run(boolean ignoreMissingDeps, PrintWriter log, boolean quiet, String version) throws IOException {
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

                // computes requires and requires transitive
                Module normalModule = toNormalModule(m, apiDeps, ignoreMissingDeps);
                if (normalModule != null) {
                    automaticToNormalModule.put(m, normalModule);

                    printModuleInfo(log, normalModule.descriptor(), version);
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

    void printModuleInfo(PrintWriter writer, ModuleDescriptor md, String version) {
        String temp = new String("Export-Package: ");
        TreeSet<ModuleDescriptor.Exports> tree = new TreeSet<ModuleDescriptor.Exports>();
        tree.addAll(md.exports());
        Iterator<Exports> iterator = tree.iterator();
        while (iterator.hasNext()) {
            Exports exp = iterator.next();
            temp = temp.concat(String.format("%s;version=\"%s\"", exp.source(), version));
            if (iterator.hasNext()) {
                temp = temp.concat(",");
            }
        }
        StringBuilder sb = new StringBuilder(temp);
        FixedWidthWriter fw = new FixedWidthWriter(writer, 70);
        fw.format(sb);
    }
}
