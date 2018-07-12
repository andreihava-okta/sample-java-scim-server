# Welcome to the Okta SCIM Beta in Java

> Okta's SCIM implementation is currently in Beta status and provides
> no guarantees for backwards-compatibility. Okta is free to break
> this SCIM implementation until it is released.

Thank you for your interest in the Okta SCIM beta. Please visit [Getting into the Okta SCIM Beta](https://github.com/joelfranusic-okta/okta-scim-beta/blob/master/README.md) for more information on setting up a test suite.


## Basic User Schema

Your service must be capable of storing the following four user
attributes:

1.  User ID (`userName`)
2.  First Name (`name.givenName`)
3.  Last Name (`name.familyName`)
4.  Email (`emails`)

Note that Okta supports more than the four user attributes listed
above. However, these four attributes are the base attributes that
you must support.  The full user schema for SCIM 2.0 is described
in [section 4 of RFC 7643](https://tools.ietf.org/html/rfc7643#section-4).

> **Best Practice:** Keep your User ID distinct from the User Email
> Address. Many systems use an email address as a user identifier,
> but this is not recommended, as email addresses often change. Using
> a unique User ID to identify user resources prevents future
> complications.

If your service supports user attributes beyond those four base
attributes, add support for those additional
attributes to your SCIM API. In some cases, you might need to
configure Okta to map non-standard user attributes into the user
profile for your application.

Included in this git repository is a sample application written in
Java/Spring, this sample application implements SCIM 2.0. Below is
how this sample application defines these attributes:
```java
    @Column(unique=true, nullable=false, length=250)
    public String userName;

    @Column(length=250)
    public String familyName;

    @Column(length=250)
    public String middleName;

    @Column(length=250)
    public String givenName;
```
In addition to the basic user schema user attributes described
above, your SCIM API must also have a unique identifier for each
user resource and should also support marking resources as "active"
or "inactive."

In the SCIM specification, the `id` attribute is used to uniquely
identify resources. [Section 3.1](//tools.ietf.org/html/rfc7643#section-3.1) of [RFC 7643](https://tools.ietf.org/html/rfc7643) provides more details
on the `id` attribute:

> A unique identifier for a SCIM resource as defined by the service
> provider.  Each representation of the resource MUST include a
> non-empty "id" value.  This identifier MUST be unique across the
> SCIM service provider's entire set of resources.  It MUST be a
> stable, non-reassignable identifier that does not change when the
> same resource is returned in subsequent transactions.  The value of
> the "id" attribute is always issued by the service provider and
> MUST NOT be specified by the client.  The string "bulkId" is a
> reserved keyword and MUST NOT be used within any unique identifier
> value.  The attribute characteristics are "caseExact" as "true", a
> mutability of "readOnly", and a "returned" characteristic of
> "always".

Our sample application defines `id` as a UUID, since
[RFC 7643](https://tools.ietf.org/html/rfc7643) requires that "this identifier MUST be unique across the
SCIM service provider's entire set of resources."
```java
    @Column(length=36)
    @Id
    public String id;
```
**Note:** Your SCIM API can use anything as an `id`, provided that the `id`
uniquely identifies reach resource, as described in [section 3.1](https://tools.ietf.org/html/rfc7643#section-3.1) of
[RFC 7643](https://tools.ietf.org/html/rfc7643).

Finally, your SCIM API must also support marking a resource as
"active" or "inactive."

In our sample application, each user resource has a Boolean
"active" attribute which is used to mark a user resource as
"active" or "inactive":
```java
    @Column(columnDefinition="boolean default false")
    public Boolean active = false;
```
## Functionality

Below are a list of the SCIM API endpoints that your SCIM API must
support to work with Okta.

## Create Account: POST /Users

Your SCIM 2.0 API should allow the creation of a new user
account.  The four basic attributes listed above must be supported, along
with any additional attributes that your application supports.  If your
application supports entitlements, your SCIM 2.0 API should allow
configuration of those as well.

An HTTP POST to the `/Users` endpoint must return an immutable or
system ID of the user (`id`) must be returned to Okta.

Okta will call this SCIM API endpoint under the following circumstances:

-   **Direct assignment**

    When a user is assigned to an Okta application using the "Assign
    to People" button in the "People" tab.
-   **Group-based assignment**

    When a user is added to a group that is assigned to an Okta
    application. For example, an Okta administrator can assign a
    group of users to an Okta application using the "Assign to
    Groups" button in the "Groups" tab. When a group is assigned to an
    Okta application, Okta sends updates to the assigned
    application when a user is added or removed from that group.

Below is an example demonstrating how the sample application handles account
creation:
```java
      @RequestMapping(method = RequestMethod.POST)
      public @ResponseBody Map usersPost(@RequestBody Map<String, Object> params, HttpServletResponse response){

          User newUser = new User(params);
          newUser.id = UUID.randomUUID().toString();
          db.save(newUser);
          response.setStatus(201);
          return newUser.toScimResource();
      }
```
For more information on user creation via the `/Users` SCIM
endpoint, see [section 3.3](https://tools.ietf.org/html/rfc7644#section-3.3) of the [SCIM 2.0 Protocol Specification](https://tools.ietf.org/html/rfc7644).

## Read list of accounts with search: GET /Users

Your SCIM 2.0 API must support the ability for Okta to retrieve
users (and entitlements like groups if available) from your
service.  This allows Okta to fetch all user resources in an
efficient manner for reconciliation and initial bootstrap (to
get all users from your app into the system).


Below is how the sample application handles listing user resources,
with support for filtering and pagination:

   @RequestMapping(method = RequestMethod.GET)
   public @ResponseBody Map usersGet(@RequestParam Map<String, String> params) {
```java
        Page<User> users;

        // If not given count, default to 100
        int count = (params.get("count") != null) ? Integer.parseInt(params.get("count")) : 100;

        // If not given startIndex, default to 1
        int startIndex = (params.get("startIndex") != null) ? Integer.parseInt(params.get("startIndex")) : 1;

        if(startIndex < 1){
            startIndex = 1;
        }
        startIndex -=1;

        PageRequest pageRequest = new PageRequest(startIndex, count);

        String filter = params.get("filter");
        if (filter != null && filter.contains("eq")) {
            String regex = "(\\w+) eq \"([^\"]*)\"";
            Pattern response = Pattern.compile(regex);

            Matcher match = response.matcher(filter);
            Boolean found = match.find();
            if (found) {
                String searchKeyName = match.group(1);
                String searchValue = match.group(2);
                switch (searchKeyName) {
                    case "active":
                        users = db.findByActive(Boolean.valueOf(searchValue), pageRequest);
                        break;
                    case "faimlyName":
                        users = db.findByFamilyName(searchValue, pageRequest);
                        break;
                    case "givenName":
                        users = db.findByGivenName(searchValue, pageRequest);
                        break;
                    case "tenant":
                        users = db.findByTenant(searchValue, pageRequest);
                        break;
                    default:
                        // Defaults to username lookup
                        users = db.findByUsername(searchValue, pageRequest);
                        break;
                }
            } else {
                users = db.findAll(pageRequest);
            }
        } else {
            users = db.findAll(pageRequest);
        }

        List<User> foundUsers = users.getContent();
        int totalResults = foundUsers.size();

        // Convert optional values into Optionals for ListResponse Constructor
        ListResponse returnValue = new ListResponse(foundUsers, Optional.of(startIndex),
                                        Optional.of(count), Optional.of(totalResults));
        return returnValue.toScimResource();
    }
```
For more details on the `/Users` SCIM endpoint, see [section 3.4.2](https://tools.ietf.org/html/rfc7644#section-3.4.2)
of the [SCIM 2.0 Protocol Specification](https://tools.ietf.org/html/rfc7644).

## Read Account Details: GET /Users/{id}

Your SCIM 2.0 API must support fetching of users by user id.

Below is how the sample application handles returning a user resource
by `id`:
```java
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Map singeUserGet(@PathVariable String id,  HttpServletResponse response) {

            try {
                User user = db.findById(id).get(0);
                return user.toScimResource();

            } catch (Exception e) {
                response.setStatus(404);
                return scimError("User not found", Optional.of(404));
            }
    }
```
If we don't find a user, we return a HTTP status 404 ("Not found")
with SCIM error message.

For more details on the `/Users/{id}` SCIM endpoint, see [section 3.4.1](https://tools.ietf.org/html/rfc7644#section-3.4.1)
of the [SCIM 2.0 Protocol Specification](https://tools.ietf.org/html/rfc7644).

## Update Account Details: PUT /Users/{id}

When a profile attribute of a user assigned to your SCIM enabled
application is changed, Okta will do the following:

-   Make a GET request against `/Users/{id}` on your SCIM API for the
    user to update.
-   Take the resource returned from your SCIM API and update only the
    attributes that need to be updated.
-   Make a PUT request against `/Users/{id}` in your SCIM API with
    the updated resource as the payload.

Examples of things that can cause changes to an Okta user profile
are:

-   A change in profile a master like Active Directory or a Human Resource
    Management Software system.
-   A direct change of a profile attribute in Okta for a local user.

Below is how the sample application handles account profile updates:
```java
    @RequestMapping(method = RequestMethod.PUT)
    public @ResponseBody Map singleUserPut(@RequestBody Map<String, Object> payload,
                                           @PathVariable String id) {

            User user = db.findById(id).get(0);
            user.update(payload);
            return user.toScimResource();
    }
```
For more details on updates to the `/Users/{id}` SCIM endpoint, see [section 3.5.1](https://tools.ietf.org/html/rfc7644#section-3.5.1)
of the [SCIM 2.0 Protocol Specification](https://tools.ietf.org/html/rfc7644).

## Deactivate Account: PATCH /Users/{id}

Deprovisioning is perhaps the most important reason customers why
customers ask that your application supports provisioning
with Okta. Your SCIM API should support account deactivation via a
PATCH to `/Users/{id}` where the payload of the PATCH request sets
the `active` property of the user to `false`.

Your SCIM API should allow account updates at the attribute level.
If entitlements are supported, your SCIM API should also be able
to update entitlements based on SCIM profile updates.

Okta will send a PATCH request to your application to deactivate a
user when an Okta user is "unassigned" from your
application. Examples of when this happen are as follows:

-   A user is manually unassigned from your application.
-   A user is removed from a group which is assigned to your application.
-   When a user is deactivated in Okta, either manually or via
    by an external profile master like Active Directory or a Human
    Resource Management Software system.

Below is how the sample application handles account deactivation:
```java
    @RequestMapping(method = RequestMethod.PATCH)
    public @ResponseBody Map singleUserPatch(@RequestBody Map<String, Object> payload,
                                                 @PathVariable String id) {
            /*
                Updates user attributes

                Params:
                        JSON map - payload
                        String id - id of user for update
                Returns:
                        scimError (See scimError doc)
                        renderJson (See renderJson doc)
            */

            List schema = (List)payload.get("schemas");
            List<Map> operations = (List)payload.get("Operations");

            if(schema == null){
                return scimError("Payload must contain schema attribute.", Optional.of(400));
            }
            if(operations == null){
                return scimError("Payload must contain operations attribute.", Optional.of(400));
            }

            //Verify schema
            String schemaPatchOp = "urn:ietf:params:scim:api:messages:2.0:PatchOp";
            if (!schema.contains(schemaPatchOp)){
                return scimError("The 'schemas' type in this request is not supported.", Optional.of(501));
            }

            //Find user for update
            User user = db.findById(id).get(0);

            for(Map map : operations){
                if(map.get("op")==null && !map.get("op").equals("replace")){
                    continue;
                }
                Map<String, Object> value = (Map)map.get("value");

                // Use Java reflection to find and set User attribute
                if(value != null) {
                    for (Map.Entry key : value.entrySet()) {
                        try {
                            Field field = user.getClass().getDeclaredField(key.getKey().toString());
                            field.set(user, key.getValue());
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            // Error - Do not update field
                        }
                    }
                }
            }
            return user.toScimResource();
    }
```
For more details on user attribute updates to `/Users/{id}` SCIM endpoint, see [section 3.5.2](https://tools.ietf.org/html/rfc7644#section-3.5.2)
of the [SCIM 2.0 Protocol Specification](https://tools.ietf.org/html/rfc7644).

## Filtering on `id`, `userName`, and `emails`

Being able to filter results by the `id`, `userName`, or `emails`
attributes is a critical part of working with Okta.

Your SCIM API must be able to filter users by `userName` and should
also support filtering by `id` and `emails`. Filtering support
is required because most provisioning actions require the ability
for Okta to determine if a user resource exists on your system.

Consider the scenario where an Okta customer with thousands of
users has a provisioning integration with your system, which also
has thousands of users. When an Okta customer adds a new user to
their Okta organization, Okta needs a way to determine quickly if a
resource for the newly created user was previously created on your
system.

Examples of filters that Okta might send to your SCIM API are as
follows:

> userName eq "jane@example.com"

> emails eq "jane@example.com"

At the moment, Okta only supports the `eq` filter operator. However, the
[filtering capabilities](https://tools.ietf.org/html/rfc7644#section-3.4.2.2) described in the SCIM 2.0 Protocol Specification are
much more complicated.

For more details on filtering in SCIM 2.0, see [section 3.4.2.2](https://tools.ietf.org/html/rfc7644#section-3.4.2.2)
of the [SCIM 2.0 Protocol Specification](https://tools.ietf.org/html/rfc7644).

## Filtering on `externalId`

In addition to supporting filtering on `id`, `userName`, and
`emails`, your application should also support filtering on
`externalId`.

Okta will use the `externalId` to determine if your application
already has an account. `externalId` is used as a stable identifier
for users, because the `userName` and email addresses for a user
can change.

Here is an example of an `externalId` filter that might be sent to
your application:

> externalId eq "00u1abcdefGHIJKLMNOP"

Note: The sample application included in this project does not yet
demonstrate how to implement storing and filtering by
`externalId`. However, Okta strongly recommends that your SCIM
implementation supports storing and filtering by `externalId`. For
details on supporting `externalId`, see
[section 3.1](https://tools.ietf.org/html/rfc7643#section-3.1) of [RFC 7643](https://tools.ietf.org/html/rfc7643). Quoted below:

> [externalId is] A String that is an identifier for the resource
> as defined by the provisioning client.  The "externalId" may
> simplify identification of a resource between the provisioning
> client and the service provider by allowing the client to use a
> filter to locate the resource with an identifier from the
> provisioning domain, obviating the need to store a local mapping
> between the provisioning domain's identifier of the resource and
> the identifier used by the service provider.  Each resource MAY
> include a non-empty "externalId" value.  The value of the
> "externalId" attribute is always issued by the provisioning
> client and MUST NOT be specified by the service provider.  The
> service provider MUST always interpret the externalId as scoped
> to the provisioning domain.  While the server does not enforce
> uniqueness, it is assumed that the value's uniqueness is
> controlled by the client setting the value.

When adding support for `externalId` filtering to your application,
we suggest that you use OAuth2.0 for authentication and use the
OAuth2.0 `client_id` to scope the `externalId` to the provisioning
domain.

## Resource Paging

When returning large lists of resources, your SCIM implementation
must support pagination using a *limit* (`count`) and *offset*
(`startIndex`) to return smaller groups of resources in a request.

Below is an example of a `curl` command that makes a request to the
`/Users/` SCIM endpoint with `count` and `startIndex` set:
```
    $ curl 'https://scim-server.example.com/scim/v2/Users?count=1&startIndex=1'
    {
      "Resources": [
        {
          "active": false,
          "id": 1,
          "meta": {
            "location": "http://scim-server.example.com/scim/v2/Users/1",
            "resourceType": "User"
          },
          "name": {
            "familyName": "Doe",
            "givenName": "Jane",
            "middleName": null
          },
          "schemas": [
            "urn:ietf:params:scim:schemas:core:2.0:User"
          ],
          "userName": "jane.doe@example.com"
        }
      ],
      "itemsPerPage": 1,
      "schemas": [
        "urn:ietf:params:scim:api:messages:2.0:ListResponse"
      ],
      "startIndex": 0,
      "totalResults": 1
    }
```
> Note: When returning a paged resource, your API should return a
> capitalized `Resources` JSON key ("Resources"), however Okta will also
> support a lowercase string ("resources"). Okta will also accept
> lowercased JSON strings for the keys of child nodes inside
> `Resources` object ("startindex", "itemsperpage", "totalresults", etc)

One way to handle paged resources is to have your database do the
paging for you. Here is how the sample application handles
pagination:
```java
    int count = (params.get("count") != null) ? Integer.parseInt(params.get("count")) : 100;

    // If not given startIndex, default to 1
    int startIndex = (params.get("startIndex") != null) ? Integer.parseInt(params.get("startIndex")) : 1;

    if(startIndex < 1){
        startIndex = 1;
    }
    startIndex -=1;

    PageRequest pageRequest = new PageRequest(startIndex, count);
    ...
    users = db.findAll(pageRequest);
```

Note: This code subtracts "1" from the
`startIndex` and `count`, because `startIndex` is [1-indexed](https://tools.ietf.org/html/rfc7644#section-3.4.2).

For more details pagination on a SCIM 2.0 endpoint, see [section 3.4.2.4](https://tools.ietf.org/html/rfc7644#section-3.4.2.4)
of the [SCIM 2.0 Protocol Specification](https://tools.ietf.org/html/rfc7644).

## Rate Limiting

Some customer actions, such as adding hundreds of users at once,
causes large bursts of HTTP transactions to your SCIM API. For
scenarios like this, we suggest that your SCIM API return rate
limiting information to Okta via the [HTTP 429 Too Many Requests](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes#429)
status code. This helps Okta throttle the rate at which SCIM
transactions are made to your API.

For more details on rate limiting transactions using the HTTP 429
status code, see [section 4](https://tools.ietf.org/html/rfc6585#section-4) of [RFC 6585](https://tools.ietf.org/html/rfc6585).

## SCIM Features Not Implemented by Okta

The following features are currently not supported by Okta:

### DELETE /Users/{id}

Deleting users via DELETE is covered in
[section 3.6](https://tools.ietf.org/html/rfc7644#section-3.6) of the [SCIM 2.0 Protocol Specification](https://tools.ietf.org/html/rfc7644).

Okta users are never **deleted**; they are **deactivated**
instead. Because of this, Okta never makes an HTTP DELETE
request to a user resource on your SCIM API. Instead, Okta makes
an HTTP PATCH request to set the `active` setting to `false`.

### Querying with POST

The ability to query users with a POST request is described in
[section 3.4.3](https://tools.ietf.org/html/rfc7644#section-3.4.3) of the [SCIM 2.0 Protocol Specification](https://tools.ietf.org/html/rfc7644).

Querying using POST is sometimes useful if your query contains
[personally identifiable information](https://en.wikipedia.org/wiki/Personally_identifiable_information) that would be exposed in
system logs if used query parameters with a GET request.

Okta currently does not support this feature.

### Bulk Operations

The ability to send a large collection of resource operations in a
single request is covered in
[section 3.7](https://tools.ietf.org/html/rfc7644#section-3.7) of the [SCIM 2.0 Protocol Specification](https://tools.ietf.org/html/rfc7644).

Okta currently does not support this feature and makes
one request per resource operation.

### "/Me" Authenticated Subject Alias

The `/Me` URI alias for the current authenticated subject is
covered in
[section 3.11](https://tools.ietf.org/html/rfc7644#section-3.11) of the [SCIM 2.0 Protocol Specification](https://tools.ietf.org/html/rfc7644).

Okta does not currently make SCIM transactions with the `/Me` URI alias.

### /Groups API endpoint

Okta currently does not support using the `/Groups` endpoint of a SCIM
API. When support is added for the `/Groups` endpoint, Okta plans
on using the following HTTP transactions against the `/Groups` endpoint:

-   Read list of Groups: GET /Groups

-   Create Group: POST /Groups

-   Read Group detail: GET /Groups/{id}

-   Delete Group: DELETE /Groups/{id}

### /Schemas API endpoint

Okta does not currently make queries against the `/Schemas`
endpoint, but this functionality is being planned.

Here is the specification for the `/Schemas` endpoint, from
[section 4](https://tools.ietf.org/html/rfc7644#section-4) of [RFC 7644](https://tools.ietf.org/html/rfc7644):

> An HTTP GET to this endpoint is used to retrieve information about
> resource schemas supported by a SCIM service provider.  An HTTP
> GET to the endpoint "/Schemas" SHALL return all supported schemas
> in ListResponse format (see Figure 3).  Individual schema
> definitions can be returned by appending the schema URI to the
> /Schemas endpoint.  For example:
>
> /Schemas/urn:ietf:params:scim:schemas:core:2.0:User
>
> The contents of each schema returned are described in Section 7 of
> RFC7643.  An example representation of SCIM schemas may be found
> in Section 8.7 of RFC7643.

### /ServiceProviderConfig API endpoint

Okta does not currently make queries against the `/ServiceProviderConfig`
endpoint, but this functionality is being planned.

Here is the specification for the `/ServiceProviderConfig` endpoint, from
[section 4](https://tools.ietf.org/html/rfc7644#section-4) of [RFC 7644](https://tools.ietf.org/html/rfc7644):

> An HTTP GET to this endpoint will return a JSON structure that
> describes the SCIM specification features available on a service
> provider.  This endpoint SHALL return responses with a JSON object
> using a "schemas" attribute of
> "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig".
> The attributes returned in the JSON object are defined in
> Section 5 of RFC7643.  An example representation of SCIM service
> provider configuration may be found in Section 8.5 of RFC7643.

### Filtering on `meta.lastModified`

Okta does not currently make queries for resources using
`meta.lastModified` as part of a filter expression.

Okta plans to add functionality to fetch incremental updates
from SCIM APIs by querying for resources using a filter expression
that transactions resources which were updated since the last update.

This will likely be done using the `gt` filter operator. For
example:

> filter=meta.lastModified gt "2011-05-13T04:42:34Z"

# Submitting to Okta

Once you have SCIM provisioning working in your Okta application,
the last thing to do before submitting your
application to Okta is the following:

1.  Check the Profile Attributes for your application.
2.  Check the Attribute Mappings for your application.

## Check the Profile Attributes for Your Application

Before submitting your application to Okta, you should check the
User Attributes to make sure that the attributes are set to what
you would want your users to see.

Check your Profile Attributes as follows:

-   From the "Admin" section in Okta, open the settings page for your
    application.
-   In the "Provisioning" tab, scroll to the bottom and click the
    "Edit Attributes" button in the "User Attributes" section.
-   A "Profile Editor" screen will open, check the following settings:
    -   The "Display name" for the application
    -   The "Description"
    -   In the "Attributes" section, remove all attributes that are not
        supported by your application.

        This is an important step! Your users will get confused if your
        application appears to support attributes that are not
        supported by your SCIM API.

        You can delete an attribute by selecting an attribute, then
        clicking the "Delete" button located in right hand attribute details pane.
    -   After you've removed all unsupported attributes from the
        "Attributes" section, check through the remaining
        attributes. In particular, check that the following properties
        for each attribute are what you expect them to be:
        -   Display name
        -   Variable name
        -   External name
        -   External namespace
        -   Data type
        -   Attribute required
            Only mark an attribute as required if one of the following is
            true:
            1.  The attribute **must** be set for your provisioning
                integration to work.
            2.  An Okta administrator must populate a value for
                this attribute.
        -   Scope
    -   If the settings for any of your supported user attributes are
        incorrect, contact Okta and request the correction for your
        attribute.

    Click the blue "Back to profiles" link when you are done checking
    the Profile Attributes for your application.

## Check the Attribute Mappings for Your Application

The last step for you to complete before submitting your
application to Okta is to check the User Profile Mappings for your
application. These mappings are what determine how profile
attributes are mapped to and from your application to an Okta
user's Universal Directory profile.

To check the User Profile Mappings for your application, do the
following:

-   From the "Admin" section in Okta, open the settings page for your
    application.
-   In the "Provisioning" tab, scroll to the bottom and click the
    "Edit Mappings" button in the "Attribute Mappings" section.
-   Check that each mapping is what you would expect it to be. Be
    sure to check both of the followign:
    1.  From your application to Okta.
    2.  From Okta to your application.

## Contact Okta

After you've finished verifying that your SCIM API works with Okta,
it is time to submit your application to Okta.

Work with your contact at Okta to start your submission.

If you have any questions about this document, or how to work with
SCIM, send an email to [developers@okta.com](developers@okta.com).

# Appendix: Details on the example SCIM server

Included in this git repository is an example SCIM server written in
Java.

This example SCIM server demonstrates how to implement a basic SCIM
server that can create, read, update, and deactivate Okta users.

The "Required SCIM Capabilities" section has the sample code that
handles the HTTP transactions to this sample application, below we
describe the rest of code used in the example.

## How to run

This example code was written in Java.

Here is how to run the example code on your machine:

First, start by doing a `git checkout` of this repository, then
`cd` to directory that `git` creates. Then, do the following:

1.  `cd` to the directory you just checked out:

        $ cd okta-scim-java-beta

2.  Then, install the dependencies for the sample SCIM server by following the directions on
    [Spring Boot](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#getting-started-installing-spring-boot)

5.  Finally, start the example SCIM server using this command:

        $ mvn spring-boot:run

## Introduction

Below are instructions for writing a SCIM server in Java, using
Spring.

A completed version of this example server is available in this git
repository in the folder named `scim\`.


## Spring Data Repository support for the "users" table:

Below is the class that Spring uses to give us easy access to
the "users" table in a JpaRepository.

The `update` method is used to "merge" or "update" a new User object
into an existing User object. This is used to simplify the code for
the code that handles PUT calls to `/Users/{id}`.

The `toScimResource` method is used to turn a User object into
a [SCIM "User" resource schema](https://tools.ietf.org/html/rfc7643#section-4.1).
```java
    public class User {
        ...

        // Default Constructor
        User() {}

        // Creates new User from JSON
        User(Map<String, Object> resource){
            this.update(resource);
        }

        void update(Map<String, Object> resource) {

            try{
                Map<String, Object> names = (Map<String, Object>)resource.get("name");
                for(String subName : names.keySet()){
                    switch (subName) {
                        case "givenName":
                            this.givenName = names.get(subName).toString();
                            break;
                        case "familyName":
                            this.familyName = names.get(subName).toString();
                            break;
                        case "middleName":
                            this.middleName = names.get(subName).toString();
                            break;
                        default:
                            break;
                    }
                }
              this.userName = resource.get("userName").toString();
              this.active = (Boolean)resource.get("active");
            } catch(Exception e) {
                 System.out.println(e);
            }
        }

        Map toScimResource(){

            Map<String, Object> returnValue = new HashMap<>();
            List<String> schemas = new ArrayList<>();
            schemas.add("urn:ietf:params:scim:schemas:core:2.0:User");
            returnValue.put("schemas", schemas);
            returnValue.put("id", this.id);
            returnValue.put("active", this.active);
            returnValue.put("userName", this.userName);

            // Get names
            Map<String, Object> names = new HashMap<>();
            names.put("familyName", this.familyName);
            names.put("givenName", this.givenName);
            names.put("middleName", this.middleName);
            returnValue.put("name", names);

            // Get meta information
            Map<String, Object> meta = new HashMap<>();
            meta.put("resourceType", "User");
            meta.put("meta", ("/scim/v2/Users/" + this.userName));
            returnValue.put("meta", meta);

            return returnValue;
        }
    }
```
## Support for SCIM Query resources

We also define a `ListResponse` class, which is used to return an
array of SCIM resources into a
[Query Resource](https://tools.ietf.org/html/rfc7644#section-3.4.2).
```java
    public class ListResponse {
        private List<User> list;
        private int startIndex;
        private int count;
        private int totalResults;

        // Init
        ListResponse(){
            this.list = new ArrayList<>();
            this.startIndex = 1;
            this.count = 0;
            this.totalResults = 0;
        }
        ListResponse(List<User> list, Optional<Integer> startIndex,
                     Optional<Integer> count, Optional<Integer> totalResults){
            this.list = list;

            // startIndex.orElse checks for optional values
            this.startIndex = startIndex.orElse(1);
            this.count = count.orElse(0);
            this.totalResults = totalResults.orElse(0);
        }


        Map<String, Object> toScimResource(){
        
            Map<String, Object> returnValue = new HashMap<>();

            List<String> schemas = new ArrayList<>();
            schemas.add("urn:ietf:params:scim:api:messages:2.0:ListResponse");
            returnValue.put("schemas", schemas);
            returnValue.put("totalResults", this.totalResults);
            returnValue.put("startIndex", this.startIndex);

            List<Map> resources = this.list.stream().map(User::toScimResource).collect(Collectors.toList());

            if(this.count != 0) {
                returnValue.put("itemsPerPage", this.count);
            }
            returnValue.put("Resources", resources);

            return returnValue;
        }
    }
```

## Support for SCIM error messages

Given a `message` and HTTP `status_code`, this will return a Spring
response with the appropriately formatted SCIM error message.

By default, this function will return an HTTP status of "[HTTP 500
Internal Server Error](https://tools.ietf.org/html/rfc2068#section-10.5.1)". However you should return a more specific
`status_code` when possible.

See [section 3.12](https://tools.ietf.org/html/rfc7644#section-3.12) of [RFC 7644](https://tools.ietf.org/html/rfc7644) for details.
```java
    public Map scimError(String message, Optional<Integer> status_code){

        Map<String, Object> returnValue = new HashMap<>();
        List<String> schemas = new ArrayList<>();
        schemas.add("urn:ietf:params:scim:api:messages:2.0:Error");
        returnValue.put("schemas", schemas);
        returnValue.put("detail", message);

        // Set default to 500
        returnValue.put("status", status_code.orElse(500));
        return returnValue;
      }
```
## Support for running from the command line

This bit of code allows you to run the sample application by typing
`mvn spring-boot:run` from your command line.

## Frequently Asked Questions (FAQ)

-   What are the differences between SCIM 1.1 and 2.0?    

    | Section | SCIM 1.1 | SCIM 2.0 | Notes |
    | --- | --- | --- | --- |
    | Namespaces | <ul><li>urn:scim:schemas:core:1.0</li><li>urn:scim:schemas:extension:enterprise:1.0</li><ul> | <ul><li>urn:ietf:params:scim:schemas:core:2.0:User</li><li>urn:ietf:params:scim:schemas:extension:enterprise:2.0:User</li><ul> | Namespaces are different therefore 2.0 is not backwards compatible with 1.1 |
    | Service Provider Config Endpoint | /ServiceProviderConfig<b>s</b> | /ServiceProviderConfig | Notice 2.0 does NOT have an 's' at the end |
    | Patch Protocol | [Section 3.3.2](http://www.simplecloud.info/specs/draft-scim-api-01.html#edit-resource-with-patch) | [Section 3.5.2: Uses JSON Patch](https://tools.ietf.org/html/rfc7644#section-3.5.2) | |
    | Error Response Schema | [Section 3.9](http://www.simplecloud.info/specs/draft-scim-api-01.html#anchor6) | [Section 3.12](https://tools.ietf.org/html/rfc7644#section-3.12) | |
    | Reference Type | N/A | Supports ref type pointing to the full url of another SCIM Resource | |
    | Query by POST /search | N/A | [Section 3.4.3](https://tools.ietf.org/html/rfc7644#section-3.4.3) | |
-   What if the SCIM 1.1 spec isn't clear on a specific use case or
    scenario?

    Okta recommends looking at the SCIM 2.0 spec for more
    clarification.  The SCIM 2.0 spec provides more guidelines and
    examples for various scenario's.

-   Why do I need to implement the `type` attribute for attributes
    such as emails/phoneNumbers/addresses?

    The SCIM User Profile allows for an array of emails.  The only
    way to differentiate between emails is to use the `type`
    sub-attribute.  See [section 2.4](https://tools.ietf.org/html/rfc7643#section-2.4) of RFC 7643 for more details:

    > When returning multi-valued attributes, service providers SHOULD
    > canonicalize the value returned (e.g., by returning a value for the
    > sub-attribute "type", such as "home" or "work") when appropriate
    > (e.g., for email addresses and URLs).
    >
    > Service providers MAY return element objects with the same "value"
    > sub-attribute more than once with a different "type" sub-attribute
    > (e.g., the same email address may be used for work and home) but
    > SHOULD NOT return the same (type, value) combination more than once
    > per attribute, as this complicates processing by the client.
    >
    > When defining schema for multi-valued attributes, it is considered a
    > good practice to provide a type attribute that MAY be used for the
    > purpose of canonicalization of values.  In the schema definition for
    > an attribute, the service provider MAY define the recommended
    > canonical values (see Section 7).
-   I only have one email/phone number/address in my user profile.
    Do I need to implement the array of emails/phone
    numbers/addresses?

    Yes, the you must return these fields in an array, which is
    specified in the SCIM spec as a multi-valued attribute: [Section
    2.4](https://tools.ietf.org/html/rfc7643#section-2.4)

## Dependencies

Here is a detailed list of the dependencies that this example SCIM
server depends on - located in the `pom.xml` file.
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.hsqldb</groupId>
    <artifactId>hsqldb</artifactId>
</dependency>
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
</dependency>
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-entitymanager</artifactId>
</dependency>
```

# License information

    Copyright © 2018, Okta, Inc.
    
    Licensed under the MIT license, the "License";
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
     
        https://opensource.org/licenses/MIT

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
