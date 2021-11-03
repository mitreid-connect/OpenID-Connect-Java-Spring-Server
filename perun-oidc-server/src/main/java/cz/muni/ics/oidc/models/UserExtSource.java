package cz.muni.ics.oidc.models;

import com.google.common.base.Strings;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model of Perun UserExtSource
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class UserExtSource extends Model {

	private ExtSource extSource;
	private String login;
	private int loa = 0;
	private boolean persistent;
	private Timestamp lastAccess;

	public UserExtSource() {
	}

	public UserExtSource(Long id, ExtSource extSource, String login, int loa, boolean persistent, Timestamp lastAccess) {
		super(id);
		this.setExtSource(extSource);
		this.setLogin(login);
		this.setLoa(loa);
		this.setPersistent(persistent);
		this.setLastAccess(lastAccess);
	}

	public ExtSource getExtSource() {
		return extSource;
	}

	public void setExtSource(ExtSource extSource) {
		if (extSource == null) {
			throw new IllegalArgumentException("extSource can't be null");
		}

		this.extSource = extSource;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		if (Strings.isNullOrEmpty(login)) {
			throw new IllegalArgumentException("login can't be null or empty");
		}

		this.login = login;
	}

	public int getLoa() {
		return loa;
	}

	public void setLoa(int loa) {
		if (loa < 0) {
			throw new IllegalArgumentException("loa has to be 0 or higher");
		}

		this.loa = loa;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public Timestamp getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(Timestamp lastAccess) {
		this.lastAccess = lastAccess;
	}

	@Override
	public String toString() {
		return "UserExtSource{" +
				"id=" + getId() +
				", extSource='" + extSource.toString() + '\'' +
				", login='" + login + '\'' +
				", loa=" + loa +
				", persistent=" + persistent +
				", lastAccess=" + lastAccess +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		UserExtSource that = (UserExtSource) o;
		return loa == that.loa &&
				persistent == that.persistent &&
				Objects.equals(extSource, that.extSource) &&
				Objects.equals(login, that.login) &&
				Objects.equals(lastAccess, that.lastAccess);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), extSource, login, loa, persistent, lastAccess);
	}
}
