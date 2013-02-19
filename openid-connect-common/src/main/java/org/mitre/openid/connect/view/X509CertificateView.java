/**
 * 
 */
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

/**
 * @author jricher
 *
 */
@Component("x509certs")
public class X509CertificateView extends AbstractView {
	
	private static Logger logger = LoggerFactory.getLogger(X509CertificateView.class);
	
	@Autowired
	private ConfigurationPropertiesBean config;
	private long daysNotValidBefore = 30;
	private long daysNotValidAfter = 365;

	@SuppressWarnings("deprecation")
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws IOException {

		Security.addProvider(new BouncyCastleProvider());
		
		Map<String, PublicKey> keys = (Map<String, PublicKey>) model.get("keys");
		
		response.setContentType("application/x-pem-file");
		
        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
        PEMWriter pemWriter = new PEMWriter(writer);
        
		for (String keyId : keys.keySet()) {

			PublicKey key = keys.get(keyId);

			if (key instanceof RSAPublicKey) {
				
				RSAPublicKey rsa = (RSAPublicKey) key;
				
				X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();

				v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
				v3CertGen.setIssuerDN(new X509Principal("CN=" + config.getIssuer() + ", OU=None, O=None L=None, C=None"));
				v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 24 * daysNotValidBefore )));
				v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * daysNotValidAfter )));
				v3CertGen.setSubjectDN(new X509Principal("CN=" + config.getIssuer() + ", OU=None, O=None L=None, C=None"));
				
				v3CertGen.setPublicKey(key);
				v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

				// FIXME: need to get the private keys over here, too, or make one up
				/*
                try {
                	X509Certificate cert = v3CertGen.generate(rsaSigner.getPrivateKey());
	                pemWriter.writeObject(cert);
                } catch (CertificateEncodingException e) {
	                logger.error("CertificateEncodingException in X509CertificateView.java: ", e);
                } catch (InvalidKeyException e) {
		            logger.error("InvalidKeyException in X509CertificateView.java: ", e);
                } catch (IllegalStateException e) {
	                logger.error("IllegalStateException in X509CertificateView.java", e);
                } catch (NoSuchAlgorithmException e) {
                	logger.error("NoSuchAlgorithmException in X509CertificateView.java", e);
                } catch (SignatureException e) {
                	logger.error("SignatureException in X509CertificateView.java", e);
                } finally {
                	pemWriter.flush();
                	writer.flush();
                }
				*/
				
			}
			
		}
		
		
		
	}

}
