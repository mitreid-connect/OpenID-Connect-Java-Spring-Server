ALTER TABLE client_details ADD accepted_tos BOOLEAN DEFAULT false;
ALTER TABLE client_details ADD jurisdiction VARCHAR(3) DEFAULT NULL;
