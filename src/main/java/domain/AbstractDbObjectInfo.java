package net.suteren.db2puml.domain

import groovy.transform.ToString

@ToString
abstract class AbstractDbObjectInfo {
	String catalog
	String schema
	String name
}