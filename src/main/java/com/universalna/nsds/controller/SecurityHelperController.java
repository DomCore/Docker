package com.universalna.nsds.controller;

import io.swagger.annotations.ApiOperation;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

@RestController
public class SecurityHelperController {

    private static final List<Method> apiMethods = ((Supplier<List<Method>>) () -> {
        Reflections reflections = new Reflections("com.universalna.nsds.controller", new MethodAnnotationsScanner());
        return Stream.of(GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class)
                .map(reflections::getMethodsAnnotatedWith)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableList());
    }).get();

    @Autowired
    private ConfigurableBeanFactory beanFactory;


    @GetMapping("/access")
    @ApiOperation(value = "Метод для получения названий методов, к которым есть доступ у пользователя")
    public Map<String, Set<String>> getAccessibleMethods() {
        final ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setRootObject(new BeanExpressionContext(beanFactory, null));
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        return apiMethods.stream()
                .filter(m -> m.getDeclaredAnnotation(PreAuthorize.class) == null || (boolean) parser.parseExpression(m.getDeclaredAnnotation(PreAuthorize.class).value()).getValue(context))
                .collect(groupingBy(m-> m.getDeclaringClass().getSimpleName(), mapping(Method::getName, toSet())));
    }
}
