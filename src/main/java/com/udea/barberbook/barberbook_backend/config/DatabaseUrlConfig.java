package com.udea.barberbook.barberbook_backend.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;

// Hosts like Render hand out the database as one URL (postgres://user:password@host:port/db),
// which JDBC can't use as is. When DATABASE_URL is set, split it into the JDBC URL, user and
// password. Without it, the usual spring.datasource.* settings (DB_URL, DB_USERNAME,
// DB_PASSWORD) apply, which is what local development uses.
@Configuration
@ConditionalOnExpression("!'${DATABASE_URL:}'.isEmpty()")
public class DatabaseUrlConfig {

    private static final int DEFAULT_PORT = 5432;
    // A small pool: the free database plan allows few simultaneous connections.
    private static final int MAX_POOL_SIZE = 5;

    // user ":" password "@" host [":" port] "/" database. Greedy on the password so that a
    // password containing '@' or ':' still splits at the last '@'.
    private static final Pattern URL_PATTERN =
        Pattern.compile("^postgres(?:ql)?://([^:/@]+):(.*)@([^@/:]+)(?::(\\d+))?/([^?]+)(?:\\?.*)?$");

    @Bean
    public DataSource dataSource(@Value("${DATABASE_URL}") String databaseUrl) {
        Matcher m = URL_PATTERN.matcher(databaseUrl.trim());
        if (!m.matches()) {
            throw new IllegalStateException(
                "DATABASE_URL no tiene el formato postgres://usuario:clave@servidor:puerto/base");
        }

        String user = decode(m.group(1));
        String password = decode(m.group(2));
        String port = m.group(4) != null ? m.group(4) : String.valueOf(DEFAULT_PORT);
        String jdbcUrl = "jdbc:postgresql://" + m.group(3) + ":" + port + "/" + m.group(5);

        HikariDataSource dataSource = DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .url(jdbcUrl)
            .username(user)
            .password(password)
            .build();
        dataSource.setMaximumPoolSize(MAX_POOL_SIZE);
        return dataSource;
    }

    // Render percent-encodes special characters in the credentials; a literal '+' must survive.
    private static String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }
}
