package org.foxfairy.base.api.config;

//@Configuration
//public class DateSourceConfig {
//
//    @Bean
//    @ConfigurationProperties("spring.datasource.druid.master")
//    public DataSource masterDataSource(){
//        return DruidDataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "dynamicDataSource")
//    @Primary
//    public DynamicDataSource createDynamicDataSource(){
//        Map<Object,Object> dataSourceMap = new HashMap<>();
//        DataSource defaultDataSource = masterDataSource();
//        dataSourceMap.put("master",defaultDataSource);
//        return new DynamicDataSource(defaultDataSource,dataSourceMap);
//    }
//
//}
