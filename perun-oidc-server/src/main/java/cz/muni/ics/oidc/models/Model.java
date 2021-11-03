package cz.muni.ics.oidc.models;

/**
 * Basic model with ID. Should be extended by another specific models with specific variables.
 *
 * @author Peter Jancus <jancus@ics.muni.cz>
 */
public abstract class Model {

	private Long id;

	public Model() {
	}

	public Model(Long id) {
		super();
		this.setId(id);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("id cannot be null");
		}

		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Model)) return false;

		Model model = (Model) o;

		return getId() != null ? getId().equals(model.getId()) : model.getId() == null;
	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}
}
