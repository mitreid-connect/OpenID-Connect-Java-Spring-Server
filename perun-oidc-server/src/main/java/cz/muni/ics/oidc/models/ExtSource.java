package cz.muni.ics.oidc.models;

import com.google.common.base.Strings;
import java.util.Objects;

/**
 * Model for ExtSource
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class ExtSource extends Model {

	private String name;
	private String type;

	public ExtSource() {
	}

	public ExtSource(Long id, String name, String type) {
		super(id);
		this.setName(name);
		this.setType(type);
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if (Strings.isNullOrEmpty(type)) {
			throw new IllegalArgumentException("type cannot be null nor empty");
		}

		this.type = type;
	}

	@Override
	public String toString() {
		return "ExtSource{" +
				"id=" + getId() +
				", name='" + name + '\'' +
				", type='" + type + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		ExtSource extSource = (ExtSource) o;
		return Objects.equals(name, extSource.name) &&
				Objects.equals(type, extSource.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name, type);
	}
}
