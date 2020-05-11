package org.mitre.xyz;

/**
 * @author jricher
 *
 */
public interface TxService {

	TxEntity loadByHandle(String handle);

	TxEntity loadByInteractUrl(String interaction);

	TxEntity save(TxEntity tx);

	void delete(TxEntity tx);

}
