package cz.muni.ics.oidc.models;

import com.google.common.base.Strings;
import java.util.Objects;

/**
 * Resource object model.
 *
 * @author Peter Jancus <jancus@ics.muni.cz>
 */
public class Resource extends Model {

	private Long voId;
	private Long facilityId;
	private String name;
	private String description;
	private Vo vo;

	public Resource() {	}

	public Resource(Long id, Long facilityId, Long voId, String name, String description) {
		super(id);
		this.setVoId(voId);
		this.setFacilityId(facilityId);
		this.setName(name);
		this.setDescription(description);
	}

	/**
	 * Should be used when RichResource is obtained from Perun
	 */
	public Resource(Long id, Long facilityId, Long voId, String name, String description, Vo vo) {
		this(id, facilityId, voId, name, description);
		this.setVo(vo);
	}

	public Long getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(Long facilityId) {
		if (facilityId == null) {
			throw new IllegalArgumentException("facilityId can't be null");
		}

		this.facilityId = facilityId;
	}

	public Long getVoId() {
		return voId;
	}

	public void setVoId(Long voId) {
		if (voId == null) {
			throw new IllegalArgumentException("voId can't be null");
		}

		this.voId = voId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (Strings.isNullOrEmpty(name)) {
			throw new IllegalArgumentException("name can't be null or empty");
		}

		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (description == null) {
			throw new IllegalArgumentException("description can't be null");
		}

		this.description = description;
	}

	public Vo getVo() {
		return vo;
	}

	public void setVo(Vo vo) {
		this.vo = vo;
	}

	@Override
	public String toString() {
		return "Resource{" +
				"id=" + getId() +
				", voId=" + voId +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", vo=" + vo +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Resource resource = (Resource) o;
		return Objects.equals(voId, resource.voId) &&
				Objects.equals(name, resource.name) &&
				Objects.equals(description, resource.description) &&
				Objects.equals(vo, resource.vo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), voId, name, description, vo);
	}

}
