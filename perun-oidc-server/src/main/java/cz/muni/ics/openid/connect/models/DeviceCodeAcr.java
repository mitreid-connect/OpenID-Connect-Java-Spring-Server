package cz.muni.ics.openid.connect.models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Model of ACR for device_code flow.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Entity
@Table(name = "device_code_acrs")
@NamedQueries({
	@NamedQuery(name = DeviceCodeAcr.GET_ACTIVE_BY_DEVICE_CODE,
		query = "SELECT acr FROM DeviceCodeAcr acr WHERE " +
			"acr.deviceCode = :" + DeviceCodeAcr.PARAM_DEVICE_CODE +
			" AND acr.expiresAt > :" + DeviceCodeAcr.PARAM_EXPIRES_AT),
	@NamedQuery(name = DeviceCodeAcr.GET_BY_ID,
		query = "SELECT acr FROM DeviceCodeAcr acr " +
			"WHERE acr.id = :" + DeviceCodeAcr.PARAM_ID +
			" AND acr.expiresAt > :" + DeviceCodeAcr.PARAM_EXPIRES_AT),
	@NamedQuery(name = DeviceCodeAcr.GET_BY_USER_CODE,
		query = "SELECT acr FROM DeviceCodeAcr acr " +
			"WHERE acr.userCode = :" + DeviceCodeAcr.PARAM_USER_CODE),
	@NamedQuery(name = DeviceCodeAcr.DELETE_EXPIRED,
		query = "DELETE FROM DeviceCodeAcr acr WHERE acr.expiresAt <= :" + DeviceCodeAcr.PARAM_EXPIRES_AT)
})
public class DeviceCodeAcr {

	public static final String GET_ACTIVE_BY_DEVICE_CODE = "DeviceCodeAcr.getActive";
	public static final String GET_BY_ID = "DeviceCodeAcr.getById";
	public static final String DELETE_EXPIRED = "DeviceCodeAcr.deleteExpired";
	public static final String GET_BY_USER_CODE = "DeviceCodeAcr.getByUserCode";

	public static final String PARAM_ID = "id";
	public static final String PARAM_USER_CODE = "user_code";
	public static final String PARAM_DEVICE_CODE = "device_code";
	public static final String PARAM_EXPIRES_AT = "expiration";

	private Long id;
	private String userCode;
	private String deviceCode;
	private String shibAuthnContextClass;
	private long expiresAt;

	public DeviceCodeAcr() { }

	public DeviceCodeAcr(String deviceCode, String userCode) {
		this.deviceCode = deviceCode;
		this.userCode = userCode;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	@Column(name = "device_code")
	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	@Basic
	@Column(name = "user_code")
	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	@Basic
	@Column(name = "shib_authn_context_class")
	public String getShibAuthnContextClass() {
		return shibAuthnContextClass;
	}

	public void setShibAuthnContextClass(String shibAuthnContextClass) {
		this.shibAuthnContextClass = shibAuthnContextClass;
	}

	@Basic
	@Column(name = "expiration")
	public long getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(long expiresAt) {
		this.expiresAt = expiresAt;
	}

	@Override
	public String toString() {
		return "Acr{" +
			"id=" + id +
			", deviceCode='" + deviceCode + '\'' +
			", userCode='" + userCode + '\'' +
			", shibAuthnContextClass='" + shibAuthnContextClass + '\'' +
			", expiration=" + expiresAt +
			'}';
	}

}
