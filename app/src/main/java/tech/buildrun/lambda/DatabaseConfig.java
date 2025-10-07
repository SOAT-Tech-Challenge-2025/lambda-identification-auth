//package tech.buildrun.lambda;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//
//import javax.sql.DataSource;
//
//public class DatabaseConfig {
//
//    private static com.zaxxer.hikari.HikariDataSource dataSource;
//
//    static {
//        com.zaxxer.hikari.HikariConfig config = new HikariConfig();
//        config.setJdbcUrl(System.getenv("DB_URL"));
//        config.setUsername(System.getenv("DB_USER"));
//        config.setPassword(System.getenv("DB_PASSWORD"));
//        config.setMaximumPoolSize(2);
//        config.setMinimumIdle(1);
//        config.setIdleTimeout(30000);
//        config.setConnectionTimeout(30000);
//        config.setMaxLifetime(600000);
//
//        dataSource = new HikariDataSource(config);
//    }
//
//    public static DataSource getDataSource() {
//        return dataSource;
//    }
//}
