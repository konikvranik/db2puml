package net.suteren.db2puml.domain

import groovy.transform.ToString
import net.suteren.db2puml.domain.TableMetadata

@ToString
class DbMetadata {
	final Collection<String> tableTypes = []
	final Map<String, Collection<String>> catalogs = [:]
	final Collection<String> schemas = []
	final Collection<TableMetadata> tables = []
}