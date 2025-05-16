package com.sincon.troubleticketing.category;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    CategoryDTO toDTO(Category category);
    
    Category toEntity(CategoryDTO categoryDTO);
}
