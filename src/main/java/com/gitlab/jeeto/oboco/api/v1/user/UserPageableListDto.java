package com.gitlab.jeeto.oboco.api.v1.user;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gitlab.jeeto.oboco.common.PageableListDto;

@Schema(name = "UserPageableList", description = "A pageable list of users.")
@XmlRootElement(name = "UserPageableList")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPageableListDto extends PageableListDto<UserDto> {

}
