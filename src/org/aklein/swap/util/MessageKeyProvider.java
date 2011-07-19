package org.aklein.swap.util;

public interface MessageKeyProvider {
    /**
     * Unique identificator <br/>
     * 
     * It is used as scope in Queues, that only the messages of the visual Queue is displayed.<br/>
     * As well it is used as key in configuration for RuleSets (validation and hints).<br/>
     * 
     * @return
     */
    public String getMessageKey();
}
