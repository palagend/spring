package com.founder.ark.ids.avatar.representations;

import lombok.Data;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Pattern;

@Data
public class GroupWrapper {
    String id;
    @Pattern(regexp = "^[-._@a-zA-Z0-9\u4e00-\u9fff]{1,50}$", message = "{groupWrapper.Pattern.groupName}")
    String groupName;

    public static GroupWrapper wrap(GroupRepresentation u) {
        GroupWrapper wrapper = new GroupWrapper();
        wrapper.groupName = u.getName();
        wrapper.setId(u.getId());
        return wrapper;
    }

    //    @Pattern(regexp = "^[\\w\\W]{0,50}$", message = "{groupWrapper.Pattern.groupDescription}")
//    String groupDescription;
//    String groupType;
//    String[] apps;
//    String[] users;
//    long createdTimestamp;
//    long updatedTimestamp;
    public GroupRepresentation unwrap() {
        GroupRepresentation gr = new GroupRepresentation();
        gr.setName(this.getGroupName());
        if (!StringUtils.isEmpty(id)) {
            gr.setId(this.id);
        }
        return gr;
    }
}