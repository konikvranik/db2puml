package net.suteren.db2puml;

import groovy.lang.GroovySystem;
import picocli.CommandLine;

public class DbInspectorVersionProvider implements CommandLine.IVersionProvider {
	@Override
	public String[] getVersion() {
		return new String[] {
			"       Java version: %s".formatted(System.getProperty("java.version")),
			"     Groovy Version: %s".formatted(GroovySystem.getVersion()),
			"Application version: %s".formatted(DbInspector.class.getAnnotation(CommandLine.Command.class).version()[0])
		};
	}
}
