package net.suteren.db2puml.domain

import groovy.transform.ToString
import net.suteren.db2puml.domain.AbstractDbObjectInfo

@ToString
abstract class AbstractDbObjectMetadata extends AbstractDbObjectInfo {
	String type
	String remarks
}