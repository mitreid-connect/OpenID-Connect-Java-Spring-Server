package cz.muni.ics.oidc.server.adapters.impl;

public class PerunAdapterLdapConstants {

    // COMMON
    public static final String O = "o";
    public static final String CN = "cn";
    public static final String SN = "sn";
    public static final String DESCRIPTION = "description";
    public static final String OBJECT_CLASS = "objectClass";
    public static final String OU_PEOPLE = "ou=People";
    public static final String UUID = "uuid";

    // USER
    public static final String PERUN_USER = "perunUser";
    public static final String PERUN_USER_ID = "perunUserId";
    public static final String GIVEN_NAME = "givenName";
    public static final String MEMBER_OF = "memberOf";
    public static final String EDU_PERSON_PRINCIPAL_NAMES = "eduPersonPrincipalNames";

    // GROUP
    public static final String PERUN_GROUP = "perunGroup";
    public static final String PERUN_GROUP_ID = "perunGroupId";
    public static final String PERUN_PARENT_GROUP_ID = "perunParentGroupId";
    public static final String PERUN_UNIQUE_GROUP_NAME = "perunUniqueGroupName";
    public static final String UNIQUE_MEMBER = "uniqueMember";

    // VO
    public static final String PERUN_VO = "perunVO";
    public static final String PERUN_VO_ID = "perunVoId";
    public static final String MEMBER_OF_PERUN_VO = "memberOfPerunVo";

    // RESOURCE
    public static final String PERUN_RESOURCE = "perunResource";
    public static final String PERUN_RESOURCE_ID = "perunResourceId";

    // FACILITY
    public static final String PERUN_FACILITY = "perunFacility";
    public static final String PERUN_FACILITY_DN = "perunFacilityDn";
    public static final String PERUN_FACILITY_ID = "perunFacilityId";
    public static final String ASSIGNED_GROUP_ID = "assignedGroupId";
    
}
