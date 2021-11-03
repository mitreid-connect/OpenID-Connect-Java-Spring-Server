package cz.muni.ics.oidc;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Utility class for working with beans.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Service
public class BeanUtil implements ApplicationContextAware {

	private ApplicationContext context;

	public <T> T getBean(String name, Class<T> beanClass) {
		return context.getBean(name, beanClass);
	}

	public <T> T getBean(Class<T> beanClass) {
		return context.getBean(beanClass);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	public ApplicationContext getContext() {
		return context;
	}
}
