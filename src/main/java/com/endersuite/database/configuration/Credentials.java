package com.endersuite.database.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author TheRealDomm
 * @since 08.05.2021
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Credentials {

    private String hostname = "localhost";
    private String database = "database";
    private String username = "username";
    private String password = "password";
    private int port = 3306;
    private boolean useSSL = true;
    private boolean autoConnect = false;

}
