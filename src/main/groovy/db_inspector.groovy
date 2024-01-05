#!/usr/bin/env groovy

import net.suteren.db2puml.DbInspector
import picocli.CommandLine

System.exit(new CommandLine(new DbInspector()).execute(args))
