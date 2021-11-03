package cz.muni.ics.oidc.saml;

import java.io.File;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.springframework.context.annotation.Bean;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class IdpMetadataBeans {

    @Bean(name = "idpMetadata")
    public ExtendedMetadataDelegate idpMetadata(SamlProperties samlProperties,
                                             ExtendedMetadata extendedMetadata,
                                             ParserPool parserPool) throws MetadataProviderException
    {
        MetadataProvider mp;
        if (StringUtils.hasText(samlProperties.getIdpMetadataUrl())) {
            HttpClientParams clientParams = new HttpClientParams();
            clientParams.setSoTimeout(60000);
            HttpClient httpClient = new HttpClient(clientParams);

            HTTPMetadataProvider httpMp = new HTTPMetadataProvider(null, httpClient,
                    samlProperties.getIdpMetadataUrl());
            httpMp.setParserPool(parserPool);
            mp = httpMp;
        } else {
            FilesystemMetadataProvider fsmp = new FilesystemMetadataProvider(
                    new File(samlProperties.getIdpMetadataFile()));
            fsmp.setParserPool(parserPool);
            mp = fsmp;
        }
        return new ExtendedMetadataDelegate(mp, extendedMetadata);
    }

}
