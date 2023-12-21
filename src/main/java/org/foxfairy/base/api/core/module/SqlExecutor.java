package org.foxfairy.base.api.core.module;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.foxfairy.base.api.core.annotations.Loggable;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用于执行SQL
 */
@Slf4j
@Component
public class SqlExecutor {

    @Resource
    private DynamicDataSource dynamicDataSource;
    @Resource
    MyBatisSqlConverter myBatisSqlConverter;

    @Loggable("#key + ' ' + #sql + ' ' + #params")
    public String executeQuery(String key, String sql, Map<String, Object> params) throws SQLException{
        String formatSql = myBatisSqlConverter.conversionSql(sql, params);
        return this.executeQuery(key, formatSql);
    }

    /**
     * 使用当前数据源执行 SQL 查询。
     *
     * @param sql 要执行的 SQL 查询
     */
    public String executeQuery(String sql) throws SQLException{
        DataSource dataSource = dynamicDataSource.getCurrentDataSource();
        return executeSqlToJson(dataSource.getConnection(), sql);
    }

    /**
     * 使用指定的数据源键执行 SQL 查询。
     *
     * @param key 数据源的键
     * @param sql 要执行的 SQL 查询
     * @throws SQLException 如果发生 SQL 异常
     */
    public String executeQuery(String key, String sql) throws SQLException {
        DataSource dataSource = dynamicDataSource.getDataSource(key);

        if (Objects.isNull(dataSource)) {
            return null;
        }

        Connection connection = dataSource.getConnection();

        return executeSqlToJson(connection, sql);
    }


    /**
     * 执行 SQL 查询并将结果整理为 JSON 格式的字符串。
     *
     * @param connection 要执行查询的数据库连接
     * @param sql        要执行的 SQL 查询
     * @return 查询结果的 JSON 格式字符串
     */
    private String executeSqlToJson(Connection connection, String sql) {
        List<JSONObject> resultList = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                // 将每一行的数据存储为 JSONObject
                JSONObject row = processRow(resultSet);
                resultList.add(row);
            }
        } catch (Exception e) {
            // 处理异常
            log.error(e.getMessage());
        }

        // 将结果列表转换为 JSON 格式的字符串
        return JSON.toJSONString(resultList);
    }

    /**
     * 处理每一行结果的方法，将每一行的数据存储为 JSONObject。
     *
     * @param resultSet 查询结果集
     * @return 包含键值对的 JSONObject，表示一行数据
     * @throws Exception 如果处理结果时发生异常
     */
    private JSONObject processRow(ResultSet resultSet) throws Exception {
        // 获取结果集的列数
        int columnCount = resultSet.getMetaData().getColumnCount();
        JSONObject row = new JSONObject();

        // 遍历每一列，将列名和对应的值存储到 JSONObject 中
        for (int i = 1; i <= columnCount; i++) {
            String columnName = resultSet.getMetaData().getColumnName(i);
            Object columnValue = resultSet.getObject(i);
            row.put(columnName, columnValue);
        }

        return row;
    }

}
