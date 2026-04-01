package com.rymcu.mortise.agent.application.capability;

import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 计算器相关工具
 */
@Configuration
public class CalculatorFunctions {
    
    /**
     * 基础计算器
     */
    @Bean
    public FunctionCallback calculator() {
        return FunctionCallback.builder()
            .function("calculator", (String expression) -> {
                if (expression == null || expression.isEmpty()) {
                    return "Error: Empty expression";
                }
                
                try {
                    expression = expression.replaceAll("\\s+", "");
                    
                    // 处理加减乘除
                    if (expression.contains("+")) {
                        String[] parts = expression.split("\\+");
                        validateParts(parts);
                        return String.valueOf(Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]));
                    } else if (expression.contains("*")) {
                        String[] parts = expression.split("\\*");
                        validateParts(parts);
                        return String.valueOf(Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]));
                    } else if (expression.contains("/")) {
                        String[] parts = expression.split("/");
                        validateParts(parts);
                        double divisor = Double.parseDouble(parts[1]);
                        if (divisor == 0) {
                            return "Error: Division by zero";
                        }
                        return String.valueOf(Double.parseDouble(parts[0]) / divisor);
                    } else if (expression.contains("-")) {
                        // 处理负数情况
                        int lastMinus = expression.lastIndexOf("-");
                        if (lastMinus > 0) {
                            double a = Double.parseDouble(expression.substring(0, lastMinus));
                            double b = Double.parseDouble(expression.substring(lastMinus + 1));
                            return String.valueOf(a - b);
                        }
                        return expression; // 返回负数本身
                    }
                    
                    // 尝试直接解析为数字
                    return String.valueOf(Double.parseDouble(expression));
                } catch (Exception e) {
                    return "Error: Invalid expression - " + e.getMessage();
                }
            })
            .description("Perform basic arithmetic calculations. Input should be a mathematical expression like '2+2', '10*5', '100/4'")
            .inputType(String.class)
            .build();
    }
    
    private void validateParts(String[] parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expression must have exactly two operands");
        }
    }
}
