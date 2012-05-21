INSERT INTO ADDRESS (ID, STREETADDRESS, LOCALITY, REGION, POSTALCODE, COUNTRY) VALUES (1, '7443 Et Road', 'Pass Christian', 'ID', '16183', 'Jordan');
INSERT INTO ADDRESS (ID, STREETADDRESS, LOCALITY, REGION, POSTALCODE, COUNTRY) VALUES (2, 'P.O. Box 893, 2523 Felis Rd.', 'New Kensington', 'NT', 'I5V 3Z7', 'Israel');

INSERT INTO event (ID, TIMESTAMP, TYPE) VALUES (1, '1970-01-05 19:00:00.0', 0);
INSERT INTO event (ID, TIMESTAMP, TYPE) VALUES (2, '1970-01-10 19:00:00.0', 1);

-- generated data based on clientdetails.sql table, some issues identified with FKc
-- TODO check the table create 
INSERT INTO clientdetails (clientId,clientSecret,registeredRedirectUri,clientName,clientDescription,allowRefresh,accessTokenTimeout,refreshTokenTimeout,owner) 
VALUES ('KSV44GQM8TV439L61QEQ61JD7GCS85HFI1QT322F00RKY10CU1','TLI15BJR7FG04OEC35JNU4OM84PVP30IUI4JK80EAH24WKN1ZX67HGF17VXI1XR9GDJ69KVY6AJOKX73UKE0QJ132C74AKK25TX4ETJ87TKE6CO357M11QOI72ST6','https://www.someRedirectURI.com/redirectURI','Research and Development','Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed tortor. Integer','0','294','255','Lara O. Figueroa');
INSERT INTO clientdetails (clientId,clientSecret,registeredRedirectUri,clientName,clientDescription,allowRefresh,accessTokenTimeout,refreshTokenTimeout,owner) 
VALUES ('VZH41HRZ2GZ828H90WHM00IX2JWB91QUE9HG292L12ILB53DM6','PMR93WIH9GL50PMI87GCE4HM83CGM58CBO0LK37IQE86UJA6CG72RBT53WUX4RZ4FAX63QZA7WSOGS23YZQ6AK142Z26QHC83ZM6QMS43KZO1FX015S19XPD88UO1','https://www.someRedirectURI.com/redirectURI','Human Resources','Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed tortor. Integer','0','179','51','Andrew A. Poole');
INSERT INTO clientdetails (clientId,clientSecret,registeredRedirectUri,clientName,clientDescription,allowRefresh,accessTokenTimeout,refreshTokenTimeout,owner) 
VALUES ('YNP14ZQH8UU276Y05ELR39YZ1KPI43JKV9NG915V93BQZ06OE1','GNC15HPC6BK04GYZ48DAS0IU72UKW95CMQ3ZY80OYM54SNK2KO21QNX48RVR6JU1ACD99FOY6DJKRU60IVF9JC318F84PRP67SP7YDS62BAY8QZ242F83PAC88OK5','https://www.someRedirectURI.com/redirectURI','Asset Management','Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed tortor.','0','151','214','Imani N. Mcguire');
INSERT INTO clientdetails (clientId,clientSecret,registeredRedirectUri,clientName,clientDescription,allowRefresh,accessTokenTimeout,refreshTokenTimeout,owner) 
VALUES ('IEY83ZEF6GB404L89LOA44PP5ZBH46UTV7PE773I04TAX28WQ4','WBY46NLS7WJ56FIG30AUK0FE01JZZ04WBJ3SW73RQT22ZPN3QN10OIF64PPM5AI6KQI24TXB5EIMTV84JYL9HR572E28FOM62VW6OUR09ANR8IC125C52RIM85IB1','https://www.someRedirectURI.com/redirectURI','Customer Service','Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed tortor. Integer aliquam adipiscing lacus. Ut nec urna','1','241','129','Clinton G. Farley');
INSERT INTO clientdetails (clientId,clientSecret,registeredRedirectUri,clientName,clientDescription,allowRefresh,accessTokenTimeout,refreshTokenTimeout,owner) 
VALUES ('TPH83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1','JXL93MIP9ZD63GRQ76BHC5YH60LCE42OKR4TD83CUN71TLU5DV82JTN07ZEY4KN8BZP27ZIO5CSHON86JPX7JK713Y08JPR45HQ3HZX70PBQ1WH441E85ZYY90UH5','https://www.someRedirectURI.com/redirectURI','Customer Relations','Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed tortor. Integer aliquam adipiscing lacus. Ut nec urna','1','37','221','Tasha Q. Beard');
INSERT INTO clientdetails (clientId,clientSecret,registeredRedirectUri,clientName,clientDescription,allowRefresh,accessTokenTimeout,refreshTokenTimeout,owner) 
VALUES ('XVX42QQA9CA348S46TNJ00NP8MRO37FHO1UW748T59BAT74LN9','UPB31XVW4FP65DVH96SWJ2UK25FSY76TJS0OQ94CDZ76DQZ1GR05YUA23FXH9OB3HDW90HAF2UWKLW56OOG4IH665P94DAU41RI2YIL21IQO1EF774F07XSG46TP6','https://www.someRedirectURI.com/redirectURI','C2C','Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed tortor. Integer aliquam adipiscing lacus. Ut nec urna et arcu','1','240','260','Miranda I. Battle');
