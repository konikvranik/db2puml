#!/usr/bin/env groovy

import DbInspector
import CommandLine

System.exit(new CommandLine(new DbInspector()).execute(args))
