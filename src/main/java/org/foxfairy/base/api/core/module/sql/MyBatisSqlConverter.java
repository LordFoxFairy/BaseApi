package org.foxfairy.base.api.core.module.sql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.xmltags.XMLScriptBuilder;
import org.apache.ibatis.session.Configuration;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 将 Mybatis 的SQL模板 转换为可执行SQL
 */

@Component
public class MyBatisSqlConverter {

    public String conversionSql(String content, Map<String, Object> param) {
        content = SqlTypeChecker.determineSqlType(content);
        BoundSql boundSql = this.getBoundSql(content, param);
        String resultSql = this.compile(boundSql);
        return this.formatSql(resultSql);
    }

    private BoundSql getBoundSql(String content, Map<String, Object> param) {
        Configuration configuration = new Configuration();
        //解析成xml
        Document doc = parseXMLDocument(content);
        // XNode
        XPathParser xPathParser = new XPathParser(doc, false);

        Node node = doc.getFirstChild();
        XNode xNode = new XNode(xPathParser, node, null);
        XMLScriptBuilder xmlScriptBuilder = new XMLScriptBuilder(configuration, xNode);
        SqlSource sqlSource = xmlScriptBuilder.parseScriptNode();
        MappedStatement.Builder builder = new MappedStatement.Builder(configuration, content, sqlSource, null);

        List<ResultMap> resultMaps = new ArrayList<>();
        List<ResultMapping> resultMappings = new ArrayList<>();
        ResultMap.Builder resultMapBuilder = new ResultMap.Builder(configuration, content, Map.class, resultMappings, true);
        resultMaps.add(resultMapBuilder.build());
        MappedStatement ms = builder.resultMaps(resultMaps).build();
        return ms.getBoundSql(param);
    }

    private String compile(BoundSql boundSql) {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        Map<String, Object> objectMap = (Map<String, Object>) boundSql.getParameterObject();

        String resultSql = boundSql.getSql();
        for (ParameterMapping mapping : parameterMappings) {
            if (boundSql.getAdditionalParameter(mapping.getProperty()) == null) {
                String placeHolder = mapping.getProperty() ;
                String[] props = placeHolder.split("\\.");
                Object obj = objectMap.get(props[0]);
                JSONObject json = JSON.parseObject(JSONObject.toJSONString(obj));
                String res = null;
                if(props.length > 1){
                    for(int i = 1 ; i < props.length ; i++){
                        String key = props[i];
                        if(props.length == i + 1){
                            res = String.valueOf(json.get(key));
                            break;
                        }
                        json = JSONObject.parseObject(JSONObject.toJSONString(json.get(key)));
                    }
                }else{
                    res = String.valueOf(obj);
                }
                if (res != null) {
                    resultSql = resultSql.replaceFirst("[?]", res);
                }
                continue;
            }
            resultSql = resultSql.replaceFirst("[?]", boundSql.getAdditionalParameter(mapping.getProperty()).toString());
        }
        return resultSql;
    }

    public Document parseXMLDocument(String xmlString) {
        if (xmlString == null) {
            throw new IllegalArgumentException();
        }
        try {
            return newDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());

        }
    }

    public DocumentBuilder newDocumentBuilder() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder();
    }

    public String formatSql(String sql){
        return sql.replaceAll("\\n","").replaceAll(" +"," ");
    }

    public static class SqlTypeChecker {

        private static final Map<String, Function<String, String>> SQL_TYPE_STRATEGIES;

        static {
            SQL_TYPE_STRATEGIES = new HashMap<>();
            SQL_TYPE_STRATEGIES.put("SELECT", SqlTypeChecker::executeSelect);
            SQL_TYPE_STRATEGIES.put("UPDATE", SqlTypeChecker::executeUpdate);
            SQL_TYPE_STRATEGIES.put("INSERT", SqlTypeChecker::executeInsert);
            SQL_TYPE_STRATEGIES.put("DELETE", SqlTypeChecker::executeDelete);
        }

        public static String determineSqlType(String sql) {
            String trimmedSql = sql.trim().toUpperCase();
            return SQL_TYPE_STRATEGIES.get(getMatchingSqlType(trimmedSql)).apply(sql);
        }

        private static String getMatchingSqlType(String trimmedSql) {
            return SQL_TYPE_STRATEGIES.keySet().stream()
                    .filter(trimmedSql::startsWith)
                    .findFirst()
                    .orElse("UNKNOWN");
        }

        private static String executeSelect(String sql) {
            return "<select>" + sql + "</select>";
        }

        private static String executeUpdate(String sql) {
            return "<update>" + sql + "</update>";
        }

        private static String executeInsert(String sql) {
            return "<insert>" + sql + "</insert>";
        }

        private static String executeDelete(String sql) {
            return "<delete>" + sql + "</delete>";
        }
    }

}

