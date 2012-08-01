/**
 * 
 */
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author jricher
 *
 */
@Component("x509certs")
public class X509CertificateView extends AbstractView {
	
	@Autowired
	private ConfigurationPropertiesBean config;

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		Map<String, JwtSigner> signers = (Map<String, JwtSigner>) model.get("signers");
		
		JsonObject obj = new JsonObject();
		JsonArray keys = new JsonArray();
		obj.add("keys", keys);
		
		for (String keyId : signers.keySet()) {

			JwtSigner src = signers.get(keyId);

			if (src instanceof RsaSigner) {
				
				RsaSigner rsaSigner = (RsaSigner) src;
				
				RSAPublicKey rsa = (RSAPublicKey) rsaSigner.getPublicKey(); // we're sure this is an RSAPublicKey b/c this is an RsaSigner
		
		        UUID uuid = UUID.randomUUID();
		        
		        X509V3CertificateGenerator generator = new X509V3CertificateGenerator();

		        Calendar calendar = Calendar.getInstance();

		        Vector<DERObjectIdentifier> attrsVector = new Vector<DERObjectIdentifier>();
		        Hashtable<DERObjectIdentifier, String> attrsHash = new Hashtable<DERObjectIdentifier, String>();

		        attrsHash.put(X509Principal.CN, config.getIssuer());
		        attrsVector.add(X509Principal.CN);

		        attrsHash.put(X509Principal.UID, config.getIssuer());
		        attrsVector.add(X509Principal.UID);

		        
		        attrsHash.put(X509Principal.EmailAddress, "no@email.com");
		        attrsVector.add(X509Principal.EmailAddress);

		        attrsHash.put(X509Principal.OU, Joiner.on(',').join(new String[] {"group"}));
		        attrsVector.add(X509Principal.OU);
		        
		        generator.setSubjectDN(new X509Principal(attrsVector, attrsHash));

		        int hoursBefore = 24 * 7 * 52;
				calendar.add(Calendar.HOUR, -hoursBefore );
		        generator.setNotBefore(calendar.getTime());

		        int hoursAfter = 24 * 7 * 52;
		        calendar.add(Calendar.HOUR, hoursBefore + hoursAfter);
		        generator.setNotAfter(calendar.getTime());

		        generator.setSerialNumber(BigInteger.ONE);

		        //generator.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));

		        try {
	                generator.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(rsa));
                } catch (InvalidKeyException e1) {
	                // TODO Auto-generated catch block
	                e1.printStackTrace();
                }

		        StringBuilder hostnameAndUUIDBuilder = new StringBuilder(config.getIssuer());
		        hostnameAndUUIDBuilder.append(':');
		        hostnameAndUUIDBuilder.append(uuid.toString());
		        generator.addExtension(X509Extensions.IssuingDistributionPoint, false, hostnameAndUUIDBuilder.toString().getBytes());

		        // Not a CA
		        generator.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));

		        //generator.setIssuerDN(caCert.getSubjectX500Principal());
		        generator.setPublicKey(rsa);
		        //generator.setSignatureAlgorithm(SIGNATURE_ALGORITHM);

		        try {
	                X509Certificate cert = generator.generate(rsaSigner.getPrivateKey(), BouncyCastleProvider.PROVIDER_NAME);

	                OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
			        
	                PEMWriter pemWriter = new PEMWriter(writer);
	                
	                pemWriter.writeObject(cert);
			        
                } catch (CertificateEncodingException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                } catch (InvalidKeyException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                } catch (IllegalStateException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                } catch (NoSuchProviderException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                } catch (SignatureException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }

		        /*
		        if (this.checkCert) {
		            cert.checkValidity();
		            cert.verify(caCert.getPublicKey());
		        }
		        */

		        
		        
			}
			
		}
		
		
		
	}

}
