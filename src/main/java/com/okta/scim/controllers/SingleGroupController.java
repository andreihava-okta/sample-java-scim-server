package com.okta.scim.controllers;

import com.okta.scim.database.GroupDatabase;
import com.okta.scim.database.GroupMembershipDatabase;
import com.okta.scim.models.Group;
import com.okta.scim.models.GroupMembership;
import com.okta.scim.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.*;

/**
 *  URL route (root)/scim/v2/Groups/{id}
 */
@Controller
@RequestMapping("/scim/v2/Groups/{id}")
public class SingleGroupController {
    GroupDatabase db;
    GroupMembershipDatabase gmDb;

    @Autowired
    public SingleGroupController(GroupDatabase db, GroupMembershipDatabase gmDb) {
        this.db = db;
        this.gmDb = gmDb;
    }

    /**
     * Queries database for {@link Group} with identifier
     * Updates response code with '404' if unable to locate {@link Group}
     * @param id {@link Group#id}
     * @param response HTTP Response
     * @return {@link #scimError(String, Optional)} / JSON {@link Map} of {@link Group}
     */
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    Map singeGroupGet(@PathVariable String id, HttpServletResponse response) {

        try {
            Group group = db.findById(id).get(0);
            HashMap res = group.toScimResource();

            PageRequest pageRequest = new PageRequest(0, Integer.MAX_VALUE);
            Page<GroupMembership> gmPage = gmDb.findByGroupId(id, pageRequest);
            List<GroupMembership> gmList = gmPage.getContent();
            ArrayList<Map<String, Object>> gmAL = new ArrayList<>();

            for (GroupMembership gm: gmList) {
                gmAL.add(gm.toScimResource());
            }

            res.put("members", gmAL);
            return res;
        } catch (Exception e) {
            response.setStatus(404);
            return scimError("Group not found", Optional.of(404));
        }
    }

    /**
     * Update via Put {@link Group} attributes
     * @param payload Payload from HTTP request
     * @param id {@link Group#id}
     * @return JSON {@link Map} of {@link Group}
     */
    @RequestMapping(method = RequestMethod.PUT)
    public @ResponseBody Map singleGroupPut(@RequestBody Map<String, Object> payload,
                                            @PathVariable String id) {
        Group group = db.findById(id).get(0);
        group.update(payload);
        return group.toScimResource();
    }

    /**
     * Update via Patch {@link Group} attributes
     * @param payload Payload from HTTP request
     * @param id {@link Group#id}
     * @return {@link #scimError(String, Optional)} / JSON {@link Map} of {@link Group}
     */
    @RequestMapping(method = RequestMethod.PATCH)
    public @ResponseBody Map singleGroupPatch(@RequestBody Map<String, Object> payload,
                                              @PathVariable String id) {
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

        int found = db.findById(id).size();

        if (found == 0) {
            return scimError("Group '" + id + "' was not found.", Optional.of(404));
        }

        //Find user for update
        Group group = db.findById(id).get(0);

        HashMap res = group.toScimResource();

        for(Map map : operations){
            if(map.get("op")==null){
                continue;
            }

            if (map.get("op").equals("replace")) {
                Map<String, Object> value = (Map) map.get("userId");

                // Use Java reflection to find and set User attribute
                if (value != null) {
                    for (Map.Entry key : value.entrySet()) {
                        try {
                            Field field = group.getClass().getDeclaredField(key.getKey().toString());
                            field.set(group, key.getValue());
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            // Error - Do not update field
                        }
                    }

                    db.save(group);
                }
            } else if (map.get("op").equals("add")) {
                if (!map.get("path").equals("members")) {
                    continue;
                }

                ArrayList<Map<String, Object>> value = (ArrayList) map.get("userId");

                if (value != null && !value.isEmpty()) {
                    for (Map val: value) {
                        PageRequest pageRequest = new PageRequest(0, Integer.MAX_VALUE);
                        Page<GroupMembership> gmPage = gmDb.findByGroupIdAndUserId(id, val.get("userId").toString(), pageRequest);

                        if (gmPage.hasContent()) {
                            continue;
                        }

                        GroupMembership gm = new GroupMembership(val);
                        gm.id = UUID.randomUUID().toString();
                        gm.groupId = id;
                        gmDb.save(gm);
                    }
                }
            }
        }

        PageRequest pageRequest = new PageRequest(0, Integer.MAX_VALUE);
        Page<GroupMembership> gms = gmDb.findByGroupId(id, pageRequest);
        List<GroupMembership> gmList = gms.getContent();
        ArrayList<Map<String, Object>> gmAL = new ArrayList<>();

        for (GroupMembership gm: gmList) {
            gmAL.add(gm.toScimResource());
        }

        res.put("members", gmAL);

        return res;
    }

    /**
     * Output custom error message with response code
     * @param message Scim error message
     * @param status_code Response status code
     * @return JSON {@link Map} of {@link Group}
     */
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
}
