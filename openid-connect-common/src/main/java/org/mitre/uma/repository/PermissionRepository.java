/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.mitre.uma.repository;

import java.util.Collection;

import org.mitre.uma.model.Permission;
import org.mitre.uma.model.PermissionTicket;
import org.mitre.uma.model.ResourceSet;

/**
 * @author jricher
 *
 */
public interface PermissionRepository {

	/**
	 *
	 * Save a permission ticket.
	 *
	 * @param p
	 * @return
	 */
	public PermissionTicket save(PermissionTicket p);

	/**
	 * Get the permission indicated by its ticket value.
	 *
	 * @param ticket
	 * @return
	 */
	public PermissionTicket getByTicket(String ticket);

	/**
	 * Get all the tickets in the system (used by the import/export API)
	 *
	 * @return
	 */
	public Collection<PermissionTicket> getAll();

	/**
	 * Save a permission object with no associated ticket (used by the import/export API)
	 *
	 * @param p
	 * @return
	 */
	public Permission saveRawPermission(Permission p);

	/**
	 * Get a permission object by its ID (used by the import/export API)
	 *
	 * @param permissionId
	 * @return
	 */
	public Permission getById(Long permissionId);

	/**
	 * Get all permission tickets issued against a resource set (called when RS is deleted)
	 *
	 * @param rs
	 * @return
	 */
	public Collection<PermissionTicket> getPermissionTicketsForResourceSet(ResourceSet rs);

	/**
	 * Remove the specified ticket.
	 *
	 * @param ticket
	 */
	public void remove(PermissionTicket ticket);

}
