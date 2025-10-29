package org.openforis.collect.relational.data;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 
 * @author G. Miceli
 *
 */
public final class Dataset {
	private List<Row> rows;
	
	public Dataset() {
		rows = new ArrayList<Row>();
	}
	
	public List<Row> getRows() {
		return Collections.unmodifiableList(rows);
	}
	
	public void addRow(Row row) {
		rows.add(row);
	}
	
	public void addRows(List<Row> rows) {
		this.rows.addAll(rows);
	}
	
	public void print(PrintStream out) {
		for (Row row : rows) {
			row.printDebug(out);
		}
	}
}