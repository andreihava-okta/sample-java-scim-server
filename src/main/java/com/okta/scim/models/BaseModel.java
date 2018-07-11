package com.okta.scim.models;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * The base schema for models
 */
public class BaseModel {
    /**
     * The unique identifier of the object
     * UUID4 following the RFC 7643 requirement
     */
    @Column(length = 36)
    @Id
    public String id;
}
