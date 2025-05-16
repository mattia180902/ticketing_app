package com.sincon.troubleticketing.category;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sincon.troubleticketing.exception.ResourceNotFoundException;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllOrderByName().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getAllCategoriesWithTicketCount() {
        List<Object[]> results = categoryRepository.findAllWithTicketCount();
        return results.stream()
                .map(row -> {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setId((Long) row[0]);
                    dto.setName((String) row[1]);
                    dto.setTicketCount(((Number) row[2]).intValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        CategoryDTO dto = convertToDTO(category);
        // Add ticket count
        dto.setTicketCount(categoryRepository.countTicketsByCategoryId(id).intValue());
        return dto;
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Check if category with the same name already exists
        if (categoryRepository.findByName(categoryDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Category with name '" + categoryDTO.getName() + "' already exists");
        }
        
        Category category = convertToEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Check if name is being changed and if it already exists
        if (!existingCategory.getName().equals(categoryDTO.getName()) && 
                categoryRepository.findByName(categoryDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Category with name '" + categoryDTO.getName() + "' already exists");
        }
        
        // Update fields
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());
        
        Category updatedCategory = categoryRepository.save(existingCategory);
        return convertToDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        
        // Check if there are tickets associated with this category
        Long ticketCount = categoryRepository.countTicketsByCategoryId(id);
        if (ticketCount > 0) {
            throw new IllegalStateException("Cannot delete category with id: " + id + 
                    " as it has " + ticketCount + " tickets associated with it");
        }
        
        categoryRepository.deleteById(id);
    }

    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    private Category convertToEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }
}