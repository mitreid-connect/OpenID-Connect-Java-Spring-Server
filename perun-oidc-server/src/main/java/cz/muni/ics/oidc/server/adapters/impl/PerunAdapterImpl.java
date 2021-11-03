package cz.muni.ics.oidc.server.adapters.impl;

import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.Group;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.models.Resource;
import cz.muni.ics.oidc.models.Vo;
import cz.muni.ics.oidc.server.PerunPrincipal;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.connectors.Affiliation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basic adapter. This one should be used across the application to call the methods that are common
 * among all adapters. Otherwise use secific adapter.
 *
 * @author Dominik František Bučík <bucik@ics.muni.cz>
 */
public class PerunAdapterImpl extends PerunAdapter {

    @Override
    public PerunUser getPreauthenticatedUserId(PerunPrincipal perunPrincipal) {
        try {
            return this.getAdapterPrimary().getPreauthenticatedUserId(perunPrincipal);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getPreauthenticatedUserId(perunPrincipal);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Facility getFacilityByClientId(String clientId) {
        try {
            return this.getAdapterPrimary().getFacilityByClientId(clientId);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getFacilityByClientId(clientId);
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean isMembershipCheckEnabledOnFacility(Facility facility) {
        try {
            return this.getAdapterPrimary().isMembershipCheckEnabledOnFacility(facility);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().isMembershipCheckEnabledOnFacility(facility);
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean canUserAccessBasedOnMembership(Facility facility, Long userId) {
        try {
            return this.getAdapterPrimary().canUserAccessBasedOnMembership(facility, userId);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().canUserAccessBasedOnMembership(facility, userId);
            } else {
                throw e;
            }
        }
    }


    @Override
    public boolean isUserInGroup(Long userId, Long groupId) {
        try {
            return this.getAdapterPrimary().isUserInGroup(userId, groupId);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().isUserInGroup(userId, groupId);
            } else {
                throw e;
            }
        }
    }

    @Override
    public List<Affiliation> getGroupAffiliations(Long userId, String groupAffiliationsAttr) {
        try {
            return this.getAdapterPrimary().getGroupAffiliations(userId, groupAffiliationsAttr);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getGroupAffiliations(userId, groupAffiliationsAttr);
            } else {
                throw e;
            }
        }
    }

    @Override
    public List<String> getGroupsAssignedToResourcesWithUniqueNames(Facility facility) {
        try {
            return this.getAdapterPrimary().getGroupsAssignedToResourcesWithUniqueNames(facility);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getGroupsAssignedToResourcesWithUniqueNames(facility);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Vo getVoByShortName(String shortName) {
        try {
            return this.getAdapterPrimary().getVoByShortName(shortName);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getVoByShortName(shortName);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Map<String, PerunAttributeValue> getUserAttributeValues(PerunUser user, Collection<String> attrsToFetch) {
        return this.getUserAttributeValues(user.getId(), attrsToFetch);
    }

    @Override
    public Map<String, PerunAttributeValue> getUserAttributeValues(Long userId, Collection<String> attrsToFetch) {
        try {
            return this.getAdapterPrimary().getUserAttributeValues(userId, attrsToFetch);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getUserAttributeValues(userId, attrsToFetch);
            } else {
                throw e;
            }
        }
    }

    @Override
    public PerunAttributeValue getUserAttributeValue(PerunUser user, String attrToFetch) {
        return this.getUserAttributeValue(user.getId(), attrToFetch);
    }

    @Override
    public PerunAttributeValue getUserAttributeValue(Long userId, String attrToFetch) {
        try {
            return this.getAdapterPrimary().getUserAttributeValue(userId, attrToFetch);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getUserAttributeValue(userId, attrToFetch);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Map<String, PerunAttributeValue> getFacilityAttributeValues(Facility facility, Collection<String> attrsToFetch) {
        return this.getFacilityAttributeValues(facility.getId(), attrsToFetch);
    }

    @Override
    public Map<String, PerunAttributeValue> getFacilityAttributeValues(Long facilityId, Collection<String> attrsToFetch) {
        try {
            return this.getAdapterPrimary().getFacilityAttributeValues(facilityId, attrsToFetch);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getFacilityAttributeValues(facilityId, attrsToFetch);
            } else {
                throw e;
            }
        }
    }

    @Override
    public PerunAttributeValue getFacilityAttributeValue(Facility facility, String attrToFetch) {
        return this.getFacilityAttributeValue(facility.getId(), attrToFetch);
    }

    @Override
    public PerunAttributeValue getFacilityAttributeValue(Long facilityId, String attrToFetch) {
        try {
            return this.getAdapterPrimary().getFacilityAttributeValue(facilityId, attrToFetch);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getFacilityAttributeValue(facilityId, attrToFetch);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Map<String, PerunAttributeValue> getVoAttributeValues(Vo vo, Collection<String> attrsToFetch) {
        return this.getVoAttributeValues(vo.getId(), attrsToFetch);
    }

    @Override
    public Map<String, PerunAttributeValue> getVoAttributeValues(Long voId, Collection<String> attrsToFetch) {
        try {
            return this.getAdapterPrimary().getVoAttributeValues(voId, attrsToFetch);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getVoAttributeValues(voId, attrsToFetch);
            } else {
                throw e;
            }
        }
    }

    @Override
    public PerunAttributeValue getVoAttributeValue(Vo vo, String attrToFetch) {
        return this.getVoAttributeValue(vo.getId(), attrToFetch);
    }

    @Override
    public PerunAttributeValue getVoAttributeValue(Long voId, String attrToFetch) {
        try {
            return this.getAdapterPrimary().getVoAttributeValue(voId, attrToFetch);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getVoAttributeValue(voId, attrToFetch);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Map<String, PerunAttributeValue> getGroupAttributeValues(Group group, Collection<String> attrsToFetch) {
        return this.getGroupAttributeValues(group.getId(), attrsToFetch);
    }

    @Override
    public Map<String, PerunAttributeValue> getGroupAttributeValues(Long groupId, Collection<String> attrsToFetch) {
        try {
            return this.getAdapterPrimary().getGroupAttributeValues(groupId, attrsToFetch);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getGroupAttributeValues(groupId, attrsToFetch);
            } else {
                throw e;
            }
        }
    }

    @Override
    public PerunAttributeValue getGroupAttributeValue(Group group, String attrToFetch) {
        return this.getGroupAttributeValue(group.getId(), attrToFetch);
    }

    @Override
    public PerunAttributeValue getGroupAttributeValue(Long groupId, String attrToFetch) {
        try {
            return this.getAdapterPrimary().getGroupAttributeValue(groupId, attrToFetch);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getGroupAttributeValue(groupId, attrToFetch);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Map<String, PerunAttributeValue> getResourceAttributeValues(Resource resource, Collection<String> attrsToFetch) {
        return this.getResourceAttributeValues(resource.getId(), attrsToFetch);
    }

    @Override
    public Map<String, PerunAttributeValue> getResourceAttributeValues(Long resourceId, Collection<String> attrsToFetch) {
        try {
            return this.getAdapterPrimary().getResourceAttributeValues(resourceId, attrsToFetch);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getResourceAttributeValues(resourceId, attrsToFetch);
            } else {
                throw e;
            }
        }
    }

    @Override
    public PerunAttributeValue getResourceAttributeValue(Resource resource, String attrToFetch) {
        return this.getResourceAttributeValue(resource.getId(), attrToFetch);
    }

    @Override
    public PerunAttributeValue getResourceAttributeValue(Long resourceId, String attrToFetch) {
        try {
            return this.getAdapterPrimary().getResourceAttributeValue(resourceId, attrToFetch);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getResourceAttributeValue(resourceId, attrToFetch);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Set<String> getCapabilities(Facility facility, Set<String> groupNames, String facilityCapabilitiesAttrName, String resourceCapabilitiesAttrName) {
        try {
            return this.getAdapterPrimary().getCapabilities(facility, groupNames, facilityCapabilitiesAttrName, resourceCapabilitiesAttrName);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getCapabilities(facility, groupNames, facilityCapabilitiesAttrName, resourceCapabilitiesAttrName);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Set<String> getCapabilities(Facility facility, Map<Long, String> idToGnameMap, String facilityCapabilitiesAttrName, String resourceCapabilitiesAttrName) {
        try {
            return this.getAdapterPrimary().getCapabilities(facility, idToGnameMap, facilityCapabilitiesAttrName, resourceCapabilitiesAttrName);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getCapabilities(facility, idToGnameMap, facilityCapabilitiesAttrName, resourceCapabilitiesAttrName);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Set<Group> getGroupsWhereUserIsActiveWithUniqueNames(Long facilityId, Long userId) {
        try {
            return this.getAdapterPrimary().getGroupsWhereUserIsActiveWithUniqueNames(facilityId, userId);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getGroupsWhereUserIsActiveWithUniqueNames(facilityId, userId);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Set<Long> getUserGroupsIds(Long userId, Long voId) {
        try {
            return this.getAdapterPrimary().getUserGroupsIds(userId, voId);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().getUserGroupsIds(userId, voId);
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean isValidMemberInGroupsAndVos(Long userId, Set<Long> mandatoryVos, Set<Long> mandatoryGroups,
                                               Set<Long> envVos, Set<Long> envGroups) {
        try {
            return this.getAdapterPrimary().isValidMemberInGroupsAndVos(userId, mandatoryVos, mandatoryGroups,
                    envVos, envGroups);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().isValidMemberInGroupsAndVos(userId, mandatoryVos, mandatoryGroups,
                        envVos, envGroups);
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean isValidMemberInGroupsAndVos(Long userId, Set<Long> vos, Set<Long> groups) {
        try {
            return this.getAdapterPrimary().isValidMemberInGroupsAndVos(userId, vos, groups);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().isValidMemberInGroupsAndVos(userId, vos, groups);
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean isUserInVo(Long userId, String voShortName) {
        try {
            return this.getAdapterPrimary().isUserInVo(userId, voShortName);
        } catch (UnsupportedOperationException e) {
            if (this.isCallFallback()) {
                return this.getAdapterFallback().isUserInVo(userId, voShortName);
            } else {
                throw e;
            }
        }
    }

}
