package com.gitlab.jeeto.oboco.api.v1.bookmark;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gitlab.jeeto.oboco.common.PageableListDto;

@Schema(name = "BookMarks", description = "A pageable list of bookMarks.")
@XmlRootElement(name = "BookMarks")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookMarksDto extends PageableListDto<BookMarkDto> {

}
