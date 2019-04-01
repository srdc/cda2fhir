package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import tr.com.srdc.cda2fhir.jolt.report.ICondition;

public class MultiOrCondition implements ICondition {
	private SortedSet<ICondition> conditions = new TreeSet<>();

	public MultiOrCondition() {
	}

	public MultiOrCondition(Collection<ICondition> conditions) {
		this.conditions.addAll(conditions);
	}

	@Override
	public MultiOrCondition clone() {
		MultiOrCondition result = new MultiOrCondition();
		conditions.forEach(condition -> result.conditions.add(condition.clone()));
		return result;
	}

	@Override
	public void prependPath(String path) {
		conditions.forEach(condition -> condition.prependPath(path));
	}

	@Override
	public int compareTo(ICondition rhs) {
		return toString().compareTo(rhs.toString());
	}

	@Override
	public ICondition not() {
		throw new NotImplementedException("Not of or condition is not yet implemented.");
	}

	@Override
	public String toString() {
		return conditions.stream().map(c -> c.toString()).collect(Collectors.joining(" or "));
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object rhs) {
		if (rhs == null) {
			return false;
		}
		if (!(rhs instanceof MultiOrCondition)) {
			return false;
		}
		MultiOrCondition oc = (MultiOrCondition) rhs;
		if (oc.conditions.size() != conditions.size()) {
			return false;
		}
		Iterator<ICondition> itr = conditions.iterator();
		Iterator<ICondition> itrRhs = oc.conditions.iterator();
		while (itr.hasNext()) {
			ICondition e = itr.next();
			ICondition eRhs = itrRhs.next();
			if (!e.equals(eRhs)) {
				return false;
			}
		}
		return true;
	}

	public void addCondition(ICondition condition) {
		conditions.add(condition);
	}
}
