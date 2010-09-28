/**
 * 
 */
package de.uni_luebeck.inb.krabbenhoeft.eQTL.application;

public class TraitAndAccession {
	String trait, accession;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((trait == null) ? 0 : trait.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TraitAndAccession other = (TraitAndAccession) obj;
		if (trait == null) {
			if (other.trait != null)
				return false;
		} else if (!trait.equals(other.trait))
			return false;
		return true;
	}
}