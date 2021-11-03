package cz.muni.ics.oidc.models;

import com.google.common.base.Strings;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents user from Perun.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
public class PerunUser extends Model implements Serializable {

	private String firstName;
	private String lastName;

	public PerunUser() {
	}

	public PerunUser(long id, String firstName, String lastName) {
		super(id);
		this.setFirstName(firstName);
		this.setLastName(lastName);
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		if (Strings.isNullOrEmpty(lastName)) {
			throw new IllegalArgumentException("name can't be null or empty");
		}

		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return "PerunUser{" +
				"id=" + getId() +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		PerunUser user = (PerunUser) o;
		return Objects.equals(firstName, user.firstName) &&
				Objects.equals(lastName, user.lastName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), firstName, lastName);
	}


}
