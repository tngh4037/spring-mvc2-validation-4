package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @InitBinder
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(itemValidator);
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

    /**
     * ValidationItemControllerV1 errors map -> bindingResult 적용
     */
    // BindingResult: 말 그대로 바인딩이 된 결과이다. 무엇이 바인딩 된 결과냐면, Item에 바인딩이 된 결과가 bindingReult에 담기는 것이다.
    // ㄴ (참고): 순서가 중요하다 !
    // @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {

        // bindingResult가 v1버전에서 했던 errors 역할을 해준다.
        // Map<String, String> errors = new HashMap<>();

        if (!StringUtils.hasText(item.getItemName())) {
            // errors.put("itemName", "상품 이름은 필수입니다.");

            // 필드 단위의 에러들은 FieldError라는 스프링이 제공하는 객체가 있는데 여기에 담으면 된다.
            // FieldError 생성자는 순서대로 objectName(modelAttribute에 담기는 객체 이름), field, defaultMessage를 인자로 넣어주면 된다.
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            // errors.put("price", "가격은 1,000 ~ 1,000,000 까지 허용합니다.");
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            // errors.put("quantity", "수량은 최대 9,999까지 허용합니다.");
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999까지 허용합니다."));
        }

        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                // errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice);

                // 이건 특정 필드에 대한 오류가 아니다. 따러서 ObjectError에 담으면 된다.
                // 순서대로 objectName(modelAttribute에 담기는 객체 이름), defaultMessage를 인자로 넣어주면 된다.
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }

        // 검증에 실패하면 다시 입력 폼으로 이동한다. (뷰 템플릿으로 보내버린다.)
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            // model.addAttribute("errors", errors);
            // bindingResult는 자동으로 뷰에 넘어간다.
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /**
     * 사용자 입력 값 유지 (2번째 생성자 사용)
     *
     * - 참고) th:field 는 매우 똑똑하게 동작하는데, 정상 상황에는 모델 객체의 값을 사용하지만, 오류가 발생하면 FieldError 에서 보관한 값을 사용해서 값을 출력한다.
     */
    // BindingResult: 말 그대로 바인딩이 된 결과이다. 무엇이 바인딩 된 결과냐면, Item에 바인딩이 된 결과가 bindingReult에 담기는 것이다.
    // ㄴ (참고): 순서가 중요하다 !
    // @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {

        // 특정 필드 검증
        if (!StringUtils.hasText(item.getItemName())) {
            // rejectedValue : 거절된 값(=사용자가 유효하지 않게 입력한 값)
            // bindingFailure : 데이터 자체가 넘어올 때 바인딩에 실패했는지 여부를 지정할 수 있다. (우리는 데이터는 잘 들어왔고, 비즈니스 검증상 요구사항에 맞지않는 것이기때문에 false로 지정한다.)
            // codes, arguments : codes나 arguments는 dafaultMessage를 메시지화 해서 대체하는 방법에 대한 것이다. (뒤에서 설명) 일단은 null로 두자.
            bindingResult.addError(new FieldError(
                    "item", "itemName", item.getItemName(), false,
                    null, null, "상품 이름은 필수입니다."));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError(
                    "item", "price", item.getPrice(), false,
                    null, null, "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError(
                    "item", "quantity", item.getQuantity(), false,
                    null, null, "수량은 최대 9,999까지 허용합니다."));
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                // ObjectError는 기존 입력값을 보관하거나 하지 않음으로 rejectValue가 없다. codes와 arguments만 null로 두자.
                bindingResult.addError(new ObjectError(
                        "item", null, null,
                        "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }

        // 검증에 실패하면 다시 입력 폼으로 이동
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /**
     * 일관성 있는 오류 메시지 관리
     */
    // @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {

        log.info("objectName = {}", bindingResult.getObjectName());
        log.info("target = {}", bindingResult.getTarget());

        // 특정 필드 검증
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError(
                    "item", "itemName", item.getItemName(), false,
                    new String[]{"required.item.itemName"}, null, null));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError(
                    "item", "price", item.getPrice(), false,
                    new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError(
                    "item", "quantity", item.getQuantity(), false,
                    new String[]{"max.item.quantity"}, new Object[]{9999}, null));
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError(
                        "item",
                        new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice},
                        null));
            }
        }

        // 검증에 실패하면 다시 입력 폼으로 이동
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /**
     * 코드가 너무 먾고 복잡하다.
     * BindingResult 가 제공하는 rejectValue(), reject() 를 사용하면 FieldError , ObjectError 를 직접 생성하지 않고, 깔끔하게 검증 오류를 다룰 수 있다.
     *
     * 참고)
     * - 컨트롤러에서 BindingResult 는 검증해야 할 객체인 target 바로 다음에 온다.
     * - 따라서, BindingResult 는 이미 본인이 검증해야 할 객체인 target 을 알고있다.
     * - 이미 target을 알고 있다면, 컨트롤러 로직에서 FieldError, ObjectError 를 생성할 때, 파라미터로 넣어줬던 objectName 등은 생략할 수도 있지 않을까 ?
     * - 맞다. ( rejectValue(), reject() 내부에서 이미 getObjectName 등을 사용해서 다 넣어준다. )
     *
     * 참고)
     * - reject: object error
     * - rejectValue: field error
     *
     * 참고)
     * - reject, rejectValue 는 내부에서 MessageCodesResolver 를 사용해서 검증 오류 코드로 (메시지에서 찾을 수 있는) 메시지 코드들을 생성한다.
     *
     * [ MessageCodesResolver 의 기본 메시지 생성 규칙 - 구체적인 것을 먼저 만들어주고, 덜 구체적인 것을 가장 나중에 만든다. ]
     * 1) object error 의 경우 다음 순서로 2가지 메시지 코드 생성
     * - code + "." + object name -> ( required.item )
     * - code -> ( required )
     *
     * 2) field error 의 경우 다음 순서로 4가지 메시지 코드 생성
     * - code + "." + object name + "." + field -> ( required.item.itemName )
     * - code + "." + field  -> ( required.itemName )
     * - code + "." + field type -> ( required.java.lang.String )
     * - code -> ( required )
     *
     * 참고)
     * - 오류 메시지 출력
     *   ㄴ 타임리프 화면을 렌더링 할 때, th:erros 가 실행된다.
     *   ㄴ 만약 이때 오류가 있다면, 생성된 오류 메시지 코드들을 기반으로 순서대로 돌아가면서 메시지에서 찾는다.
     *   ㄴ 그리고 없으면 디폴트 메시지를 출력한다.
     */
    //@PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {

        log.info("objectName = {}", bindingResult.getObjectName());
        log.info("target = {}", bindingResult.getTarget());

        // 특정 필드 검증
        if (!StringUtils.hasText(item.getItemName())) {
            /*
            bindingResult.addError(new FieldError(
                    "item", "itemName", item.getItemName(), false,
                    new String[]{"required.item.itemName"}, null, null));
            */
            bindingResult.rejectValue("itemName", "required");
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            /*
            bindingResult.addError(new FieldError(
                    "item", "price", item.getPrice(), false,
                    new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
            */
            bindingResult.rejectValue("price", "range",
                    new Object[]{1000, 1000000}, null);
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
             /*
            bindingResult.addError(new FieldError(
                    "item", "quantity", item.getQuantity(), false,
                    new String[]{"max.item.quantity"}, new Object[]{9999}, null));
            */
            bindingResult.rejectValue("quantity", "max",
                    new Object[]{9999}, null);
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                /*
                bindingResult.addError(new ObjectError(
                        "item",
                        new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice},
                        null));
                */
                bindingResult.reject(
                        "totalPriceMin",
                        new Object[]{10000, resultPrice},
                        null);
            }
        }

        // 검증에 실패하면 다시 입력 폼으로 이동
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /**
     * Validator 분리 (1)
     */
    //@PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {

        itemValidator.validate(item, bindingResult);

        // 검증에 실패하면 다시 입력 폼으로 이동
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /**
     * Validator 분리 (2)
     *
     * - @Validated: 검증기를 실행하라는 애노테이션
     */
    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {

        // itemValidator.validate(item, bindingResult);

        // 검증에 실패하면 다시 입력 폼으로 이동
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}