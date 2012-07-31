package org.mitre.jwt.model;

public enum Type {
	
    /**
    * Type ({@code typ}) parameter indicating a JWT.
    *
    * <p>Corresponds to the follwoing {@code typ} values:
    *
    * <ul>
    *     <li>"JWT"
    *     <li>"urn:ietf:params:oauth:token-type:jwt"
    * </ul>
    */
    JWT,
    
    
    /**
    * Type ({@code typ}) parameter indicating a nested JWS.
    *
    * <p>Corresponds to the following {@code typ} value:
    *
    * <ul>
    *     <li>"JWS"
    * </ul>
    */
    JWS,
    

    /**
    * Type ({@code typ}) parameter indicating a nested JWE.
    *
    * <p>Corresponds to the follwoing {@code typ} value:
    *
    * <ul>
    *     <li>"JWE"
    * </ul>
    */
    JWE;
    
    
    /**
    * Parses the specified type string (case sensitive).
    *
    * <p>Note that both "JWT" and 
     * "urn:ietf:params:oauth:token-type:jwt" resolve to 
     * {@link #JWT}.
    *
    * @param s The string to parse.
    *
    * @throws java.text.ParseException If the string couldn't be 
     *                                  parsed to a supported JWT
    *                                  header type.
    */
    public static Type parse(final String s)
                   throws java.text.ParseException {
    
                   if (s == null)
                                  throw new NullPointerException("The parsed JWT header \"typ\" value must not be null");
                   
                   if (s.equals("JWT") || s.equals("urn:ietf:params:oauth:token-type:jwt"))
                                  return JWT;
                   
                   if (s.equals("JWS"))
                                  return JWS;
                   
                   if (s.equals("JWE"))
                                  return JWE;
                   
                   throw new java.text.ParseException("Unsupported JWT header \"typ\" value: " + s, 0);
    }
}
