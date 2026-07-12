package com.sharecampus.im.util;

import java.util.*;

/**
 * DFA 敏感词过滤器（Trie 树实现）
 * <p>
 * 敏感词命中后替换为 ***
 */
public class SensitiveWordFilter {

    private final TrieNode root = new TrieNode();

    /** 初始化敏感词库 */
    public SensitiveWordFilter(List<String> words) {
        for (String w : words) {
            addWord(w.trim().toLowerCase());
        }
    }

    /** 默认词库 */
    public static SensitiveWordFilter defaultFilter() {
        return new SensitiveWordFilter(List.of(
            "fuck", "shit", "damn", "asshole",
            "傻逼", "操你", "妈的", "去死", "垃圾"
        ));
    }

    private void addWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isEnd = true;
    }

    /** 过滤文本，敏感词替换为 *** */
    public String filter(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder result = new StringBuilder();
        String lower = text.toLowerCase();
        int i = 0;
        while (i < text.length()) {
            TrieNode node = root;
            int j = i;
            int matchEnd = -1;
            while (j < text.length()) {
                char c = lower.charAt(j);
                node = node.children.get(c);
                if (node == null) break;
                if (node.isEnd) matchEnd = j;
                j++;
            }
            if (matchEnd >= 0) {
                result.append("***");
                i = matchEnd + 1;
            } else {
                result.append(text.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd;
    }
}
