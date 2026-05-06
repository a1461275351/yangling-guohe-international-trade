package com.trade.platform.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.trade.platform.security.UserContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class MybatisPlusConfig {

    private static final Set<String> IGNORE_TABLES = new HashSet<>(Arrays.asList(
            "sys_tenant",
            "sys_user",
            "sys_user_apply",
            "sys_config_item",
            "sys_config_value",
            "biz_template",
            "biz_order_goods",
            "biz_customs_goods",
            "biz_import_ledger_goods",
            "biz_import_ledger_file",
            "biz_customs_review"
    ));

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // Tenant interceptor
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long tenantId = UserContext.getTenantId();
                if (tenantId != null) {
                    return new LongValue(tenantId);
                }
                return new NullValue();
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return IGNORE_TABLES.contains(tableName);
            }
        }));

        // Pagination interceptor
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());

        return interceptor;
    }
}
