package com.founder.ark.ids.avatar.representations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.founder.ark.ids.avatar.util.NameHelper;
import lombok.Data;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class UserWrapper {
    public static final String mobilePhoneKey = "mobilePhone";
    @Pattern(regexp = "^[a-zA-Z0-9@._-]{1,50}$", message = "{userWrapper.Pattern.username}", groups = {All.class, Post.class})
    private String username;
    @Email(regexp = "^[-A-Za-z0-9_.]+@([_A-Za-z0-9]+\\.)+[A-Za-z0-9]{2,3}$", message = "{userWrapper.Pattern.email}", groups = {All.class, Post.class})
    private String email;
    @Pattern(regexp = "^[-._@a-zA-Z0-9\u4e00-\u9fff]{1,30}$", message = "{userWrapper.Pattern.name}", groups = {All.class, Post.class})
    private String name;
    private Boolean enabled;
    @Pattern(regexp = "(^((\\+86)|(86))?1[3578]\\d{9}$)|(^$)", message = "{userWrapper.Pattern.mobilePhone}", groups = {All.class, Post.class})
    private String mobilePhone;
    @NotEmpty(message = "{userWrapper.NotEmpty.id}", groups = {All.class})
    private String id;
    @JsonIgnore
    private String password = null;

    public static UserWrapper wrap(UserRepresentation user) {
        UserWrapper wrapper = new UserWrapper();
        wrapper.setUsername(user.getUsername());
        wrapper.setEmail(user.getEmail());
        wrapper.setName(NameHelper.combine(user.getFirstName(), user.getLastName()));
        wrapper.setEnabled(user.isEnabled());
        wrapper.setId(user.getId());
        Map<String, List<String>> attr = user.getAttributes();
        if (attr != null && attr.containsKey(mobilePhoneKey) && attr.get(mobilePhoneKey).size() > 0) {
            String mobilePhone = attr.get(mobilePhoneKey).get(0);
            wrapper.setMobilePhone(mobilePhone);
        }
        List<CredentialRepresentation> cres = user.getCredentials();
        if (cres != null && cres.size() > 0) {
            for (CredentialRepresentation rep : cres) {
                if (rep.getType() == CredentialRepresentation.PASSWORD) wrapper.setPassword(rep.getValue());
            }
        }
        return wrapper;
    }

    public UserRepresentation unwrap() {
        UserRepresentation user = new UserRepresentation();
        if (id != null) user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        Map<String, String> map = NameHelper.parseName(name);
        user.setFirstName(map.get(NameHelper.GIVEN_NAME));
        user.setLastName(map.get(NameHelper.FAMILY_NAME));
        user.setEnabled(enabled);
        //把手机号放入附加属性中
        Map<String, List<String>> attr = new HashMap<>();
        List<String> list1 = new ArrayList<>();
        list1.add(mobilePhone);
        attr.put(mobilePhoneKey, list1);
        user.setAttributes(attr);
        if (password != null) {
            List<CredentialRepresentation> cres = new ArrayList<>();
            CredentialRepresentation cre = new CredentialRepresentation();
            cre.setType(CredentialRepresentation.PASSWORD);
            cre.setValue(password);
            cres.add(cre);
            user.setCredentials(cres);
        }
        return user;
    }
}
