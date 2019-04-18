package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import tr.com.srdc.cda2fhir.jolt.report.ICondition;

public abstract class MultiCondition implements ICondition {
	private SortedSet<ICondition> conditions = new TreeSet<>();

	public MultiCondition() {
	}

	public MultiCondition(Collection<ICondition> conditions) {
		this.conditions.addAll(conditions);
	}

	@Override
	public abstract MultiCondition clone();

	protected void copyConditions(MultiCondition target) {
		conditions.forEach(condition -> target.conditions.add(condition.clone()));
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
		throw new NotImplementedException("Not of multi conditions is not yet implemented.");
	}

	protected String toString(String delimiter) {
		return conditions.stream().map(c -> c.toString()).collect(Collectors.joining(delimiter));
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	protected boolean commonEquals(MultiCondition rhs) {
		if (rhs == null) {
			return false;
		}
		if (rhs.conditions.size() != conditions.size()) {
			return false;
		}
		Iterator<ICondition> itr = conditions.iterator();
		Iterator<ICondition> itrRhs = rhs.conditions.iterator();
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

	public Collection<ICondition> getChildConditions() {
		return conditions;
	}
}
