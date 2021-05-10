package com.endersuite.database.mysql.builder;

import java.text.MessageFormat;

/**
 * @author TheRealDomm
 * @since 08.05.2021
 */
public enum QueryType {

    SELECT("SELECT {0} FROM {1} "),
    INSERT("INSERT INTO {0} "),
    DELETE("DELETE FROM {0} "),
    UPDATE("UPDATE {0} SET ");

    private final String name;

    QueryType(String name) {
        this.name = name;
    }

    public String format(Object... replacements) {
        return MessageFormat.format(this.name, replacements);
    }

}
