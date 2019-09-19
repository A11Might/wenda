package com.nowcoder.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 胡启航
 * @date 2019/8/16 - 13:11
 */
@Service
public class SensitiveService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);

    private static final String DEFAULT_REPLACEMENT = "***";

    // 前缀树节点类
    private class TrieNode {
        private boolean end = false;
        private Map<Character, TrieNode> subNodes = new HashMap<>(); // 在路径上存储字符

        void addSubNode(Character key, TrieNode node) {
            subNodes.put(key, node);
        }

        TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }

        boolean isKeywordEnd() {
            return end;
        }

        void setKeywordEnd(boolean end) {
            this.end = end;
        }

        int getSubNodeCount() {
            return subNodes.size();
        }
    }

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 建前缀树
    private void addWord(String lineTxt) {
        TrieNode curNode = rootNode;
        for (int i = 0; i < lineTxt.length(); i++) {
            Character c = lineTxt.charAt(i);
            if (isSymbol(c)) {
                continue;
            }
            TrieNode node = curNode.getSubNode(c);
            // 没有当前字符，新建
            if (node == null) {
                node = new TrieNode();
                curNode.addSubNode(c, node);
            }
            curNode = node;
            if (i == lineTxt.length() - 1) {
                curNode.setKeywordEnd(true);
            }
        }
    }

    // 判断是否是一个字符(0x2E80, 0x9FFF) 东亚文字范围
    private boolean isSymbol(char c) {
        int ic = (int) c;
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }

    // 过滤敏感词
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        String replacement = DEFAULT_REPLACEMENT;
        StringBuilder result = new StringBuilder();

        TrieNode curNode = rootNode;
        int begin = 0;
        int position = 0;

        while (position < text.length()) {
            char c = text.charAt(position);
            if (isSymbol(c)) {
                // 如果空格在敏感词中间就跳过，否则加入
                if (begin == position) {
                    result.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            curNode = curNode.getSubNode(c);
            if (curNode == null) {
                result.append(text.charAt(begin));
                begin++;
                position = begin;
                curNode = rootNode;
            } else if (curNode.isKeywordEnd()) {
                result.append(replacement);
                position++;
                begin = position;
            } else {
                position++;
            }
        }

        // 加入最后一段非完整敏感词文本
        result.append(text.substring(begin));

        return result.toString();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        rootNode = new TrieNode();

        try {
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("SensitiveWords.txt");
            InputStreamReader read = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                lineTxt = lineTxt.trim();
                addWord(lineTxt);
            }
            read.close();
        } catch (Exception e) {
            logger.error("读取敏感词文件失败" + e.getMessage());
        }
    }

    /*
    public static void main(String[] argv) {
        SensitiveService s = new SensitiveService();
        s.addWord("色情");
        s.addWord("好色");
        System.out.print(s.filter("你好X色**情XX"));
    }*/
}
