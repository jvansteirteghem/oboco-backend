package com.gitlab.jeeto.oboco.api.v1.book;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gitlab.jeeto.oboco.common.PageableListDto;

@Schema(name = "Books", description = "A pageable list of books.")
@XmlRootElement(name = "Books")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BooksDto extends PageableListDto<BookDto> {

}
