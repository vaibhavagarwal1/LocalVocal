package com.dev.localvocal.models;

public class ModelCategories {
    Integer categoriesIcon;
    String categoriesName;

    public ModelCategories() {
    }

    public ModelCategories(Integer categoriesIcon, String categoriesName) {
        this.categoriesIcon = categoriesIcon;
        this.categoriesName = categoriesName;
    }

    public Integer getCategoriesIcon() {
        return categoriesIcon;
    }

    public void setCategoriesIcon(Integer categoriesIcon) {
        this.categoriesIcon = categoriesIcon;
    }

    public String getCategoriesName() {
        return categoriesName;
    }

    public void setCategoriesName(String categoriesName) {
        this.categoriesName = categoriesName;
    }
}
