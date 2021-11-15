package cz.muni.ics.oidc.web.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.oidc.models.Aup;
import cz.muni.ics.oidc.models.PerunAttribute;
import cz.muni.ics.oidc.models.mappers.RpcMapper;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.langs.Localization;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * Controller of the AUP page
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Controller
@Slf4j
public class AupController {

    public static final String URL = "aup";
    public static final String NEW_AUPS = "newAups";
    public static final String APPROVED = "approved";
    public static final String RETURN_URL = "returnUrl";
    public static final String USER_ATTR = "userAttr";

    private static final SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");

    private final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private PerunAdapter perunAdapter;

    @Autowired
    private PerunOidcConfig perunOidcConfig;

    @Autowired
    private Localization localization;

    @Autowired
    private WebHtmlClasses htmlClasses;

    @GetMapping(value = "/" + URL)
    public String showAup(HttpServletRequest request, Map<String, Object> model,
                          @SessionAttribute(name = NEW_AUPS) String newAupsString) throws IOException
    {
        JsonNode newAupsJson = mapper.readTree(newAupsString);
        Map<String, Aup> newAups = new LinkedHashMap<>();

        Iterator<Map.Entry<String, JsonNode>> iterator = newAupsJson.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> keyAupPair = iterator.next();
            newAups.put(keyAupPair.getKey(), RpcMapper.mapAup(keyAupPair.getValue()));
        }

        model.put(NEW_AUPS, newAups);
        ControllerUtils.setPageOptions(model, request, localization, htmlClasses, perunOidcConfig);

        return "aup";
    }

    @PostMapping(value = "/" + URL, consumes = "application/x-www-form-urlencoded")
    public String storeAup(HttpServletRequest request,
                           @SessionAttribute String returnUrl,
                           @SessionAttribute(name = NEW_AUPS) String newAupsString,
                           @SessionAttribute(name = USER_ATTR) String userAupsAttrName) throws IOException
    {
        JsonNode aupsToApproveJson = mapper.readTree(newAupsString);
        ObjectNode aupsToApproveJsonObject = new ObjectNode(jsonNodeFactory);

        Iterator<Map.Entry<String, JsonNode>> iterator = aupsToApproveJson.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> keyAupPair = iterator.next();
            ObjectNode aup = (ObjectNode) keyAupPair.getValue();

            Date date = new Date(System.currentTimeMillis());
            aup.put(Aup.SIGNED_ON, formatter.format(date));

            if (aupsToApproveJsonObject.has(keyAupPair.getKey())) {
                aupsToApproveJsonObject.replace(keyAupPair.getKey(), aup);
            } else {
                aupsToApproveJsonObject.set(keyAupPair.getKey(), aup);
            }
        }

        Long userId = Long.parseLong(request.getUserPrincipal().getName());

        try {
            PerunAttribute userAupsAttr = perunAdapter.getAdapterRpc().getUserAttribute(userId, userAupsAttrName);
            if (userAupsAttr != null) {
                Map<String, String> userAupsAttrValue = new LinkedHashMap<>();
                if (userAupsAttr.valueAsMap() != null) {
                    userAupsAttrValue = userAupsAttr.valueAsMap();
                }

                ObjectNode userAupsAttrAsObjNode = mapper.convertValue(userAupsAttrValue, ObjectNode.class);

                ObjectNode userAttrValueUpdated = updateUserAupsAttrValue(userAupsAttrAsObjNode, aupsToApproveJsonObject);
                userAupsAttr.setValue(userAupsAttr.getType(), userAttrValueUpdated);
                perunAdapter.getAdapterRpc().setUserAttribute(userId, userAupsAttr);
            }
        } catch (Exception e) {
            log.warn("Exception when storing aup, probably RPC not reachable", e);
        }

        request.getSession().removeAttribute(NEW_AUPS);
        request.getSession().removeAttribute(RETURN_URL);
        request.getSession().removeAttribute(USER_ATTR);
        request.getSession().setAttribute(APPROVED, true);

        return "redirect:" + returnUrl;
    }

    private ObjectNode updateUserAupsAttrValue(ObjectNode userAups, ObjectNode newAups) throws IOException {
        if (userAups == null) {
            userAups = new ObjectNode(jsonNodeFactory);
        }

        Iterator<Map.Entry<String, JsonNode>> newAupsFields = newAups.fields();

        while (newAupsFields.hasNext()) {
            Map.Entry<String, JsonNode> voNameToListOfAups = newAupsFields.next();
            String aupKey = voNameToListOfAups.getKey();
            ObjectNode newApprovedAup = (ObjectNode) voNameToListOfAups.getValue();

            ArrayNode oldAupsArray = null;

            if (userAups.get(voNameToListOfAups.getKey()) != null) {
                String oldAupsAsString = userAups.get(aupKey).asText();
                oldAupsArray = (ArrayNode) mapper.readTree(oldAupsAsString);
            }

            if (oldAupsArray  == null) {
                oldAupsArray = new ArrayNode(jsonNodeFactory);
            }

            oldAupsArray.add(newApprovedAup);
            userAups.put(aupKey, oldAupsArray.toString());
        }

        return userAups;
    }
}
