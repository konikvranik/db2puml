package net.suteren.db2puml

import domain.ColumnMetadata
import domain.DbMetadata
import domain.FkMetadata
import domain.IndexMetadata
import domain.PkMetadata
import domain.TableMetadata

class DbMetadataRenderer {

	final static String HEADER = '''

!define table(x) class x << (T,#BBAA88) >>
!define view(x) class x << (V,TECHNOLOGY) >>
!define index(x) class x << (I,White) >>
!define sequence(x) class x << (S,BUSINESS) >>
!procedure pk_index(name, columns)
  {method}<<PK>> name (columns)
!endprocedure
!procedure column(name, type)
  {field}name : type
!endprocedure
!procedure primary_key(name, type)
  {field}<u>name : type <<PK>></u>
!endprocedure
!procedure combined_key(name, type)
  {field}<u><i>name : type <<PK>> <<FK>></i></u>
!endprocedure
!procedure foreign_key(name, type)
  {field}<i>name : type <<FK>></i>
!endprocedure
!procedure index_column(name, columns)
  {method}<<index>> name (columns)
!endprocedure
!procedure fk_constraint(name, col, target, columns)
  {method}<<FK>> name (col) <&arrow-right> target (columns)
!endprocedure
!procedure unique(name, columns)
  {method}<<unique>> name (columns)
!endprocedure
!$null = "<i>NULL</i>"

left to right direction
hide empty members

'''

	static void render(PrintStream out, DbMetadata metadata, Collection<String> tableTypes) {
		out.println('@startuml')
		out.println(HEADER)

		metadata.schemas
				.each { schema ->

					out.println("package ${schema} {")


					def tables = metadata.tables
							.sort { it.type }
							.findAll { !tableTypes || it.type in tableTypes }
					tables.each {
						renderTable(out, it, schema)
					}
					tables.each {
						it.foreignKeys.each {
							renderImportedKey(out, it, schema)
						}
					}
					out.println("}")
				}


		out.println('@enduml')
	}

	static void renderTable(PrintStream out, TableMetadata table, String schema = null) {

		if (schema && schema != table.schema) {
			return
		}

		out.print('  ')
		if (table.type.endsWith('TABLE')) {
			out.print("table")
		} else if (table.type.endsWith('VIEW')) {
			out.print("view")
		} else if (table.type.endsWith('INDEX')) {
			out.print("index")
		} else if (table.type.endsWith('SEQUENCE')) {
			out.print("sequence")
		} else {
			return
		}
		out.println("(${table.name}) ${table.type.startsWith('SYSTEM') ? '<<system>> #Pink ' : ''}{")
		table.columns.each {
			renderColumn(out, it)
		}
		table.primaryKeys
				.groupBy { it.name }
				.each { renderPrimaryKey(out, it.value) }
		table.foreignKeys
				.groupBy { it.name }
				.each { renderFk(out, it.value) }
		table.indexes
				.groupBy { it.name }
				.each { renderIndex(out, it.value) }
		out.println('  }')
	}

	static void renderImportedKey(PrintStream out, FkMetadata fk, String schema) {
		if (schema && schema != fk.schema) {
			return
		}
		out.print('  ')
		out.print(fk.table)
		out.print('::')
		out.print(fk.column)
		out.print(' "')
		out.print(fk.column)
		out.print('" --> "')
		out.print(fk.reference.column)
		out.print('" ')
		out.print(fk.reference.table)
		out.print('::')
		out.print(fk.reference.column)
		out.print(': ')
		out.println(fk.name)
	}

	static void renderColumn(PrintStream out, ColumnMetadata columnMetadata) {
		out.print('    ')
		if (columnMetadata.isFk()) {
			if (columnMetadata.isPk()) {
				out.print('combined_key(')
			} else {
				out.print('foreign_key(')
			}
		} else if (columnMetadata.isPk()) {
			out.print('primary_key(')
		} else {
			out.print('column(')
		}
		out.print(columnMetadata.name)
		out.print(',"')
		out.print(pumlEscape(columnMetadata.type))
		if (!(columnMetadata.type in ['json', 'jsonb', 'text', 'clob', 'blob', 'lob']) && columnMetadata.size > 0) {
			out.print('(')
			out.print(columnMetadata.size)
			if (columnMetadata.decimalDigits) {
				out.print(',')
				out.print(columnMetadata.decimalDigits)
			}
			out.print(')')
		}
		out.print('"')
		out.println(')')
	}

	static String pumlEscape(String s) {
		s.replaceAll(/"/, '""')
	}


	static void renderFk(PrintStream out, List<FkMetadata> fkMetadataList) {
		out.print('    ')
		String columns = fkMetadataList
				.sort { it.keySeq }
				.collect { it.column }
				.unique()
				.join(',')
		String tables = fkMetadataList
				.sort { it.keySeq }
				.collect { it.reference.table }
				.unique()
				.join(',')
		String foreignColumns = fkMetadataList
				.sort { it.keySeq }
				.collect { it.reference.column }
				.unique()
				.join(',')
		out.println("fk_constraint(\"${fkMetadataList.first().name}\", \"${columns}\", \"${tables}\", \"${foreignColumns}\")")
	}

	static void renderIndex(PrintStream out, List<IndexMetadata> indexMetadata) {
		if (indexMetadata.parent.primaryKeys.any { it.name == indexMetadata.name }) {
			return
		}
		out.print('    ')
		String columns = indexMetadata
				.sort { it.ordinalPosition }
				.collect { it.column }
				.unique()
				.join(',')
		if (indexMetadata.first().nonUnique) {
			out.println("index_column(\"${indexMetadata.first().name}\", \"${columns}\")")
		} else {
			out.println("unique(\"${indexMetadata.first().name}\", \"${columns}\")")
		}

	}

	static void renderPrimaryKey(PrintStream out, List<PkMetadata> pkMetadata) {
		out.print('    ')
		String columns = pkMetadata
				.sort { it.keySeq }
				.collect { it.column }
				.unique()
				.join(',')
		out.println("pk_index(${pkMetadata.first().name},\"${columns}\")")
	}
}