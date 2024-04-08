package hello.itemservice.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.ObjectError;

public class MessageCodesResolverTest {

    MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();

    // reject()가 동작할 때, 내부에서 ObjectError를 만든다.
    // ObjectError를 만들 때, 이때 codes에 대한 정보를 resolveMessageCodes()를 통해 얻는다.
    @Test
    void messageCodesResolver_object() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");
        Assertions.assertThat(messageCodes).containsExactly("required.item", "required");
    }

    // rejectValue()가 동작할 때, 내부에서 FieldError를 만든다.
    // FieldError를 만들 때, 이때 codes에 대한 정보를 resolveMessageCodes()를 통해 얻는다.
    @Test
    void messageCodesResolver_field() {
        String[] messageCodes = codesResolver.resolveMessageCodes(
                "required",
                "item",
                "itemName",
                String.class);

        Assertions.assertThat(messageCodes).containsExactly(
                "required.item.itemName",
                "required.itemName",
                "required.java.lang.String",
                "required");
    }
}
