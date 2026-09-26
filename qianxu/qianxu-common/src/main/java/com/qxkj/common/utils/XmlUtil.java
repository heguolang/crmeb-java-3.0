package com.qxkj.common.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
public class XmlUtil {
    public static Map<String, String> xmlToMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        SAXReader reader = new SAXReader();

        InputStream in = null;
        try {
            in = request.getInputStream();
            Document doc = reader.read(in);
            Element root = doc.getRootElement();
            List<Element> list = root.elements();
            for (Element element : list) {
                map.put(element.getName(), element.getText());
            }
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        } finally {
            try {
                assert in != null;
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 将发送消息封装成对应的xml格式
     */
//    public static HashMap<String, Object> xmlToMap(String strxml) throws Exception {
//        strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");
//
//        HashMap<String, Object> map = new HashMap<>();
//        SAXReader reader = new SAXReader();
//        InputStream inputStream = new ByteArrayInputStream(strxml.getBytes(StandardCharsets.UTF_8));
//
//        if (StringUtils.isBlank(strxml)) {
//            return null;
//        }
//
//        Document document = reader.read(inputStream);
//        Element root = document.getRootElement();
//        List<Element> list = root.elements();
//
//        for (Element e : list) {
//            map.put(e.getName(), e.getText());
//        }
//        inputStream.close();
//
//        return map;
//    }

    public static HashMap<String, Object> xmlToMap(String strxml) {
        strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");

        if (StringUtils.isBlank(strxml)) {
            return null;
        }
        HashMap<String, Object> map = new HashMap<>();
        try {
            DocumentBuilder documentBuilder = WXPayXmlUtil.newDocumentBuilder();
            org.w3c.dom.Document document = documentBuilder.parse(new InputSource(new StringReader(strxml)));
            org.w3c.dom.Element element = document.getDocumentElement();
            NodeList nodeList = element.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    map.put(node.getNodeName(), node.getTextContent());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 将发送消息封装成对应的xml格式
     */
    public static String objectToXml(Object object) {
        XStream xstream = new XStream(new Xpp3Driver(new NoNameCoder())); //不需要转义
        xstream.alias("xml", object.getClass());
        return xstream.toXML(object);
    }

    /**
     * 将Map转换为XML格式的字符串
     *
     * @param data Map类型数据
     * @return XML格式的字符串
     * @throws Exception
     */
    public static String mapToXml(Map<String, String> data) throws Exception {
        org.w3c.dom.Document document = WXPayXmlUtil.newDocument();
        org.w3c.dom.Element root = document.createElement("xml");
        document.appendChild(root);
        for (String key : data.keySet()) {
            String value = data.get(key);
            if (value == null) {
                value = "";
            }
            value = value.trim();
            org.w3c.dom.Element filed = document.createElement(key);
            filed.appendChild(document.createTextNode(value));
            root.appendChild(filed);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMSource source = new DOMSource(document);
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        String output = writer.getBuffer().toString(); //.replaceAll("\n|\r", "");
        writer.close();
        return output;
    }
}
