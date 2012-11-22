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
public class User {
    
    private Long userid;
    private String username;
    private Organisation organisations;
    
    /**
     * @return the userid
     */
    public Long getUserid() {
        return userid;
    }
    /**
     * @param userid the userid to set
     */
    public void setUserid(Long userid) {
        this.userid = userid;
    }
    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * @return the organisations
     */
    public Organisation getOrganisations() {
        return organisations;
    }
    /**
     * @param organisations the organisations to set
     */
    public void setOrganisations(Organisation organisations) {
        this.organisations = organisations;
    }
    
}
