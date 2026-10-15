package com.charity.dto;

import java.math.BigDecimal;
import javax.validation.constraints.*;

public class DonationRequest {
    @NotNull(message = "请选择项目")
    @Positive(message = "项目编号无效")
    private Long projectId;

    @NotNull(message = "请输入模拟捐款金额")
    @DecimalMin(value = "0.01", message = "金额必须大于零")
    @DecimalMax(value = "9999999999.99", message = "金额超出允许范围")
    @Digits(integer = 10, fraction = 2, message = "金额最多10位整数、2位小数")
    private BigDecimal amount;

    @NotBlank(message = "缺少提交标识")
    @Pattern(regexp = "[a-zA-Z0-9_-]{1,64}", message = "提交标识需为1至64位字母、数字、下划线或连字符")
    private String requestId;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
