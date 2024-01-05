#!/usr/bin/env groovy

@Grapes([
		@Grab('org.postgresql:postgresql:42.7.1'),
		@Grab('com.oracle.jdbc:ojdbc10:19.3.0.0'),
		@Grab('org.slf4j:slf4j-api:2.0.10'),
		@Grab('org.slf4j:slf4j-simple:2.0.10'),
		@Grab('info.picocli:picocli-groovy:4.7.4'),
		@Grab('commons-io:commons-io:2.11.0'),
		@Grab('com.fasterxml.jackson.core:jackson-core:2.14.2'),
		@Grab('com.fasterxml.jackson.core:jackson-databind:2.15.2'),
		@Grab('com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2'),
])
@groovy.lang.GrabConfig(systemClassLoader = true)

import DbInspector
import CommandLine

System.exit(new CommandLine(new DbInspector()).execute(args))
