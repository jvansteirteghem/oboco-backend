package com.gitlab.jeeto.oboco.api.v1.book;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gitlab.jeeto.oboco.api.LinkableDto;

@Schema(name = "BookLinkable", description = "A linkable of books.")
@XmlRootElement(name = "BookLinkable")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookLinkableDto extends LinkableDto<BookDto> {

}
