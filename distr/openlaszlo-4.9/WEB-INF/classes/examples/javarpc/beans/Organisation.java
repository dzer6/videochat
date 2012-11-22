/* ****************************************************************************
 * JavaDataSource.java
 * ****************************************************************************/

/* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2009 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* J_LZ_COPYRIGHT_END *********************************************************/

package examples.javarpc.beans;

/**
 * @author sebastianwagner
 *
 */
public class Organisation {
    
    private Long organisationId;
    private String name;
    
    
    /**
     * @return the organisationId
     */
    public Long getOrganisationId() {
        return organisationId;
    }
    /**
     * @param organisationId the organisationId to set
     */
    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
