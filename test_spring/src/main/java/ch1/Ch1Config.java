package ch1;

import org.springframework.context.annotation.*;

/**
 * @author zangzh
 * @date 2019/11/10 15:03
 */
@Configuration
@ComponentScan
@EnableAspectJAutoProxy
public class Ch1Config {

    @Bean
    public FunctionTest2Service functionTest2Service() {
        FunctionTest2Service test2Service = new FunctionTest2Service();
        return test2Service;
    }

    @Bean
    public FunctionService functionService() {
        FunctionService functionService = new FunctionService();
        return functionService;
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Ch1Config.class);
        FunctionService functionService = context.getBean(FunctionService.class);

        functionService.print();

        FunctionTest2Service test2Service = context.getBean(FunctionTest2Service.class);
        test2Service.print();

        test2Service.print2();

        context.close();
    }

}
