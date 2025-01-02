package hello.itemservice;

import hello.itemservice.web.validation.ItemValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class ItemServiceApplication
// implements WebMvcConfigurer
{

	public static void main(String[] args) {
		SpringApplication.run(ItemServiceApplication.class, args);
	}

	// 참고) 글로벌 설정을 하면 BeanValidator가 자동 등록되지 않는다.
	// @Override
	// public Validator getValidator() {
	// 	  return new ItemValidator();
	// }
}
