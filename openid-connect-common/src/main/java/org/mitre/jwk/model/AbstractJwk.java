package org.mitre.jwk.model;

import com.google.gson.JsonObject;

public abstract class AbstractJwk implements Jwk{
	
	public static final String ALGORITHM = "alg";
	public static final String USE = "use";
	public static final String KEY_ID = "kid";
	
	private String kid;
	private String alg;
	private String use;
	
	public AbstractJwk(JsonObject object){
		init(object);
	}
		
	/* (non-Javadoc)
	 * @see org.mitre.jwk.model.Jwk2#getAlg()
	 */
	@Override
	public String getAlg() {
		return alg;
	}

	public void setAlg(String alg) {
		this.alg = alg;
	}
	
	/* (non-Javadoc)
	 * @see org.mitre.jwk.model.Jwk2#getKid()
	 */
	@Override
	public String getKid() {
		return kid;
	}

	public void setKid(String kid) {
		this.kid = kid;
	}

	/* (non-Javadoc)
	 * @see org.mitre.jwk.model.Jwk2#getUse()
	 */
	@Override
	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}
	
	public JsonObject toJsonObject() {
		JsonObject export = new JsonObject();
		export.addProperty(ALGORITHM, getAlg());
		export.addProperty(USE, getUse());
		export.addProperty(KEY_ID, getKid());
		return export;
	}

	protected void init(JsonObject object){
		if(object.get(ALGORITHM) != null){
			setAlg(object.get(ALGORITHM).getAsString());
		}
		if(object.get(KEY_ID) != null){
			setKid(object.get(KEY_ID).getAsString());
		}
		if(object.get(USE) != null){
			setUse(object.get(USE).getAsString());
		}
	}
}