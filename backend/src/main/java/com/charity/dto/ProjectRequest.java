package com.charity.dto;

import java.math.BigDecimal;
import javax.validation.constraints.*;

public class ProjectRequest {
    @NotBlank(message = "请输入项目标题")
    @Size(max = 120, message = "项目标题最多120个字符")
    private String title;

    @NotBlank(message = "请输入项目说明")
    @Size(max = 10000, message = "项目说明最多10000个字符")
    private String description;

    @NotNull(message = "请输入目标金额")
    @DecimalMin(value = "0.01", message = "目标金额必须大于零")
    @DecimalMax(value = "9999999999.99", message = "目标金额超出允许范围")
    @Digits(integer = 10, fraction = 2, message = "目标金额最多10位整数、2位小数")
    private BigDecimal targetAmount;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }
}
