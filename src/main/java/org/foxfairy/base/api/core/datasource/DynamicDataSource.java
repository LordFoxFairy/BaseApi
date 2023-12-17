package org.foxfairy.base.api.core.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class DynamicDataSource {

    private static final ConcurrentMap<String, DataSourceMeta> DATA_SOURCE_META = new ConcurrentHashMap<>();

    public void addOrUpdateDataSource(DataSourceProperties dataSourceProperties) {
        DataSourceMeta dataSourceMeta = DATA_SOURCE_META.get(dataSourceProperties.getDataSourceKey());

        if(Objects.nonNull(dataSourceMeta) && Objects.equals(dataSourceMeta.getHashCode(), dataSourceProperties.hashCode())) {
            return;
        }

        DataSourceMeta newDataSourceMeta = new DataSourceMeta();
        newDataSourceMeta.setDataSource(this.createDataSource(dataSourceProperties));
        newDataSourceMeta.setHashCode(dataSourceProperties.hashCode());

        if(Objects.isNull(dataSourceMeta)){
            DataSourceSwitcher.addDataSource(dataSourceProperties.dataSourceKey, newDataSourceMeta);
        }else{
            DataSourceSwitcher.updateDataSource(dataSourceProperties.dataSourceKey, newDataSourceMeta);
        }

        DataSourceSwitcher.switchDataSource(dataSourceProperties.dataSourceKey);
    }

    public void removeDataSource(String key) {
        DataSourceSwitcher.removeDataSource(key);
    }

    public DataSource getDataSource(String key){
        DataSourceMeta dataSourceMeta = DataSourceSwitcher.getDataSource(key);
        if(Objects.nonNull(dataSourceMeta)){
            return dataSourceMeta.getDataSource();
        }
        return null;
    }

    public DataSource getCurrentDataSource(){
        DataSourceMeta dataSourceMeta = DataSourceSwitcher.getCurrentDataSource();
        if(Objects.nonNull(dataSourceMeta)){
            return dataSourceMeta.getDataSource();
        }
        return null;
    }

    public DataSource createDataSource(DataSourceProperties dataSourceProperties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSourceProperties.getUrl());
        config.setUsername(dataSourceProperties.getUsername());
        config.setPassword(dataSourceProperties.getPassword());
        config.setDriverClassName(dataSourceProperties.getDriverClassName());
        return new HikariDataSource(config);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataSourceProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private String dataSourceKey;

        @Override
        public int hashCode() {
            return Objects.hash(url, username, password, driverClassName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            DataSourceProperties that = (DataSourceProperties) obj;
            return Objects.equals(url, that.url) &&
                    Objects.equals(username, that.username) &&
                    Objects.equals(password, that.password) &&
                    Objects.equals(driverClassName, that.driverClassName);
        }
    }

    private static class DataSourceSwitcher {

        private static final ThreadLocal<String> CURRENT_DATA_SOURCE_KEY = new ThreadLocal<>();

        public static void addDataSource(String key, DataSourceMeta dataSourceMeta) {
            DATA_SOURCE_META.put(key, dataSourceMeta);
            CURRENT_DATA_SOURCE_KEY.set(key);
        }

        public static void updateDataSource(String key, DataSourceMeta dataSourceMeta) {
            addDataSource(key, dataSourceMeta);
        }

        public static void removeDataSource(String key) {
            DATA_SOURCE_META.remove(key);
            CURRENT_DATA_SOURCE_KEY.remove();
        }

        public static void switchDataSource(String key) {
            if (DATA_SOURCE_META.containsKey(key)) {
                CURRENT_DATA_SOURCE_KEY.set(key);
                log.info("Switched to DataSource: " + key);
            } else {
                log.info("DataSource not found: " + key);
            }
        }

        public static DataSourceMeta getCurrentDataSource() {
            String currentKey = CURRENT_DATA_SOURCE_KEY.get();
            return DATA_SOURCE_META.get(currentKey);
        }


        public static DataSourceMeta getDataSource(String key) {
            return DATA_SOURCE_META.get(key);
        }
    }

    @Data
    public static class DataSourceMeta{
        private DataSource dataSource;
        private Integer hashCode;
    }
}

