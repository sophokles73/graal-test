/**
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */


package io.bosch.graal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A GraalTest.
 *
 */
public class GraalTest {

    /**
     * @param args The command line arguments
     */
    public static void main(String[] args) {

        // read mountinfo so as to determine root mount path
        String mountPath = null;
        try {
            System.out.println("/proc/self/mountinfo:");
            for (String line : GraalTest.cat(Paths.get("/proc/self/mountinfo"))) {
                System.out.println(line);
                if (line.contains(" - cgroup2 ")) {
                    String[] tokens = line.split(" ");
                    mountPath = tokens[4];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println();

        String cgroupPath = null;
        try {
            String procSelfCgroup = GraalTest.cat(Paths.get("/proc/self/cgroup")).get(0);
            System.out.println("/proc/self/cgroup:");
            System.out.println(procSelfCgroup);
            String[] tokens = procSelfCgroup.split(":");
            if (tokens.length != 3) {
                System.out.println("malformed value found in /proc/self/cgroup"); // something is not right.
                System.exit(1);
            }
            if (mountPath != null && !"0".equals(tokens[0])) {
                // hierarchy must be zero for cgroups v2
                System.out.printf("found unexpected cgroups hierarchy in /proc/self/cgroup: %s", tokens[0]); // something is not right.
                System.exit(1);
            }
            cgroupPath = tokens[2];
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println();

        if (mountPath != null) {

            final Path cgroups = Paths.get(mountPath, cgroupPath);
            System.out.printf("using cgroups v2 path: %s", cgroups.toString());
            System.out.println();
            System.out.println();

            catFiles(cgroups, (dir, name) -> name.startsWith("memory."));
            catFiles(cgroups, (dir, name) -> name.startsWith("cpu."));
        }

        System.out.printf("running on Java VM [version: %s, name: %s, vendor: %s, max memory: %dMiB, processors: %d]%s",
                System.getProperty("java.version"),
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.vendor"),
                Runtime.getRuntime().maxMemory() >> 20,
                Runtime.getRuntime().availableProcessors(),
                System.lineSeparator());
    }

    private static List<String> cat(Path path) throws IOException {
        try (BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(new FileInputStream(path.toString()), StandardCharsets.UTF_8))) {
            String line;
            List<String> lines = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
    }

    private static void catFiles(Path dir, FilenameFilter filter) {
        Stream.of(dir.toFile().listFiles(filter))
            .forEach(file -> {
                System.out.printf("%s%s", file.toString(), System.lineSeparator());
                try {
                    cat(file.toPath()).forEach(line -> System.out.println(line));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println();
            });
    }
}
