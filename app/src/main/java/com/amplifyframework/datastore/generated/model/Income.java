package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Income type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Incomes", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "cognito:username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.GROUPS, groupClaim = "cognito:groups", groups = { "us-east-1_Eic9TeHEn_Google" }, provider = "userPools", operations = { ModelOperation.READ, ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE })
})
public final class Income implements Model {
  public static final QueryField ID = field("Income", "id");
  public static final QueryField OWNER = field("Income", "owner");
  public static final QueryField AMOUNT = field("Income", "amount");
  public static final QueryField CATEGORY = field("Income", "category");
  public static final QueryField NOTES = field("Income", "notes");
  public static final QueryField CREATED_AT = field("Income", "createdAt");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String") String owner;
  private final @ModelField(targetType="Float", isRequired = true) Double amount;
  private final @ModelField(targetType="IncomeCategory", isRequired = true) IncomeCategory category;
  private final @ModelField(targetType="String") String notes;
  private final @ModelField(targetType="String", isRequired = true) String createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String resolveIdentifier() {
    return id;
  }
  
  public String getId() {
      return id;
  }
  
  public String getOwner() {
      return owner;
  }
  
  public Double getAmount() {
      return amount;
  }
  
  public IncomeCategory getCategory() {
      return category;
  }
  
  public String getNotes() {
      return notes;
  }
  
  public String getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Income(String id, String owner, Double amount, IncomeCategory category, String notes, String createdAt) {
    this.id = id;
    this.owner = owner;
    this.amount = amount;
    this.category = category;
    this.notes = notes;
    this.createdAt = createdAt;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Income income = (Income) obj;
      return ObjectsCompat.equals(getId(), income.getId()) &&
              ObjectsCompat.equals(getOwner(), income.getOwner()) &&
              ObjectsCompat.equals(getAmount(), income.getAmount()) &&
              ObjectsCompat.equals(getCategory(), income.getCategory()) &&
              ObjectsCompat.equals(getNotes(), income.getNotes()) &&
              ObjectsCompat.equals(getCreatedAt(), income.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), income.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getOwner())
      .append(getAmount())
      .append(getCategory())
      .append(getNotes())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Income {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("owner=" + String.valueOf(getOwner()) + ", ")
      .append("amount=" + String.valueOf(getAmount()) + ", ")
      .append("category=" + String.valueOf(getCategory()) + ", ")
      .append("notes=" + String.valueOf(getNotes()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static AmountStep builder() {
      return new Builder();
  }
  
  /**
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   */
  public static Income justId(String id) {
    return new Income(
      id,
      null,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      owner,
      amount,
      category,
      notes,
      createdAt);
  }
  public interface AmountStep {
    CategoryStep amount(Double amount);
  }
  

  public interface CategoryStep {
    CreatedAtStep category(IncomeCategory category);
  }
  

  public interface CreatedAtStep {
    BuildStep createdAt(String createdAt);
  }
  

  public interface BuildStep {
    Income build();
    BuildStep id(String id);
    BuildStep owner(String owner);
    BuildStep notes(String notes);
  }
  

  public static class Builder implements AmountStep, CategoryStep, CreatedAtStep, BuildStep {
    private String id;
    private Double amount;
    private IncomeCategory category;
    private String createdAt;
    private String owner;
    private String notes;
    @Override
     public Income build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Income(
          id,
          owner,
          amount,
          category,
          notes,
          createdAt);
    }
    
    @Override
     public CategoryStep amount(Double amount) {
        Objects.requireNonNull(amount);
        this.amount = amount;
        return this;
    }
    
    @Override
     public CreatedAtStep category(IncomeCategory category) {
        Objects.requireNonNull(category);
        this.category = category;
        return this;
    }
    
    @Override
     public BuildStep createdAt(String createdAt) {
        Objects.requireNonNull(createdAt);
        this.createdAt = createdAt;
        return this;
    }
    
    @Override
     public BuildStep owner(String owner) {
        this.owner = owner;
        return this;
    }
    
    @Override
     public BuildStep notes(String notes) {
        this.notes = notes;
        return this;
    }
    
    /**
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     */
    public BuildStep id(String id) {
        this.id = id;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String owner, Double amount, IncomeCategory category, String notes, String createdAt) {
      super.id(id);
      super.amount(amount)
        .category(category)
        .createdAt(createdAt)
        .owner(owner)
        .notes(notes);
    }
    
    @Override
     public CopyOfBuilder amount(Double amount) {
      return (CopyOfBuilder) super.amount(amount);
    }
    
    @Override
     public CopyOfBuilder category(IncomeCategory category) {
      return (CopyOfBuilder) super.category(category);
    }
    
    @Override
     public CopyOfBuilder createdAt(String createdAt) {
      return (CopyOfBuilder) super.createdAt(createdAt);
    }
    
    @Override
     public CopyOfBuilder owner(String owner) {
      return (CopyOfBuilder) super.owner(owner);
    }
    
    @Override
     public CopyOfBuilder notes(String notes) {
      return (CopyOfBuilder) super.notes(notes);
    }
  }
  
}
