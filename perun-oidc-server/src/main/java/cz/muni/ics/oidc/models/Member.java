package cz.muni.ics.oidc.models;

import cz.muni.ics.oidc.models.enums.MemberStatus;
import java.util.Objects;

/**
 * Member object model.
 *
 * @author Peter Jancus <jancus@ics.muni.cz>
 */
public class Member extends Model {

	private Long userId;
	private Long voId;
	private MemberStatus status;

	public Member() {
	}

	public Member(Long id, Long userId, Long voId, MemberStatus status) {
		super(id);
		this.setUserId(userId);
		this.setVoId(voId);
		this.setStatus(status);
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId cannot be null");
		}

		this.userId = userId;
	}

	public Long getVoId() {
		return voId;
	}

	public void setVoId(Long voId) {
		if (voId == null) {
			throw new IllegalArgumentException("voId cannot be null");
		}

		this.voId = voId;
	}

	public MemberStatus getStatus() {
		return status;
	}

	public void setStatus(MemberStatus status) {
		if (status == null) {
			throw new IllegalArgumentException("status cannot be null nor empty");
		}

		this.status = status;
	}

	@Override
	public String toString() {
		return "Member{" +
				"id=" + getId() +
				", userId=" + userId +
				", voId=" + voId +
				", status='" + status + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Member member = (Member) o;
		return Objects.equals(userId, member.userId) &&
				Objects.equals(voId, member.voId) &&
				Objects.equals(status, member.status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), userId, voId, status);
	}
}
