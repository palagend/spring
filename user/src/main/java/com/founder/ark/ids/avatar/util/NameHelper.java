package com.founder.ark.ids.avatar.util;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class NameHelper {
    public static final String[] COMPLEX_FAMILY_NAME = {"欧阳", "太史", "端木", "上官", "司马", "东方", "独孤", "南宫", "万俟", "闻人", "夏侯", "诸葛", "尉迟", "公羊", "赫连", "澹台", "皇甫",
            "宗政", "濮阳", "公冶", "太叔", "申屠", "公孙", "慕容", "仲孙", "钟离", "长孙", "宇文", "城池", "司徒", "鲜于", "司空", "汝嫣", "闾丘", "子车", "亓官",
            "司寇", "巫马", "公西", "颛孙", "壤驷", "公良", "漆雕", "乐正", "宰父", "谷梁", "拓跋", "夹谷", "轩辕", "令狐", "段干", "百里", "呼延", "东郭", "南门",
            "羊舌", "微生", "公户", "公玉", "公仪", "梁丘", "公仲", "公上", "公门", "公山", "公坚", "左丘", "公伯", "西门", "公祖", "第五", "公乘", "贯丘", "公皙",
            "南荣", "东里", "东宫", "仲长", "子书", "子桑", "即墨", "达奚", "褚师"};
    public static final String FAMILY_NAME = "family_name";
    public static final String GIVEN_NAME = "given_name";

    /**
     * @param name
     * @return
     * @description 返回的是一个Map对象map, 姓通过map.get(NameHelper.FAMILY_NAME)获得，名通过map.get(NameHelper.GIVEN_NAME)获得
     */
    public static final Map<String, String> parseName(String name) {
        Map<String, String> map = new HashMap<>();
        String family_name = String.valueOf(name.charAt(0));
        String given_name = name.substring(1);
        for (String f : COMPLEX_FAMILY_NAME) {
            if (name.startsWith(f)) {
                family_name = f;
                given_name = name.split(f)[1];
                break;
            }

        }
        map.put(FAMILY_NAME, family_name);
        map.put(GIVEN_NAME, given_name);
        return map;
    }

    public static String combine(String givenName, String familyName) {
        return StringUtils.trimAllWhitespace(familyName + givenName);
    }
}
