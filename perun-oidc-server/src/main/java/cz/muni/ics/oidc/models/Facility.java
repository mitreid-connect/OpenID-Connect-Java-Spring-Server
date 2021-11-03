package cz.muni.ics.oidc.models;

import com.google.common.base.Strings;
import java.util.Objects;

/**
 * Facility object model.
 *
 * @author Peter Jancus <jancus@ics.muni.cz>
 */
public class Facility extends Model {

	private String name;
	private String description;

	public Facility() {
	}

	public Facility(Long id, String name, String description) {
		super(id);
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (Strings.isNullOrEmpty(name)) {
			throw new IllegalArgumentException("name cannot be null nor empty");
		}

		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Facility{" +
				"id=" + getId() +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Facility facility = (Facility) o;
		return Objects.equals(name, facility.name) &&
				Objects.equals(description, facility.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name, description);
	}
}
