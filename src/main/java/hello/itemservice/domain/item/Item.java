package hello.itemservice.domain.item;

import lombok.Data;

// NotBlank, NotNull, Max: Bean Validation이 표준적으로 제공한다. (따라서 어떤 구현체에서도 동작한다.) (구현체를 변경하더라도 정상 동작.)
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

// Range: hibernate validator 에서만 동작한다. (표준에는 Range가 없음.)
import org.hibernate.validator.constraints.Range;

import org.hibernate.validator.constraints.ScriptAssert;

@Data
// @ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >= 10000")
public class Item {

    //@NotNull(groups = UpdateCheck.class)
    private Long id;

    //@NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
    private String itemName;

    //@NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    //@Range(min = 1000, max = 1000000, groups = {SaveCheck.class, UpdateCheck.class})
    private Integer price;

    //@NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    //@Max(value = 9999, groups = SaveCheck.class)
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
