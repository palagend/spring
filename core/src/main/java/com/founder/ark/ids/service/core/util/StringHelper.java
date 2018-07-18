package com.founder.ark.ids.service.core.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * @author huyh
 */
public class StringHelper {

    public static final String PASSWORD_REGEX = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,50}$";
    public static final String EMAIL_REGEX = "^[-A-Za-z0-9_.]+@([_A-Za-z0-9]+\\.)+[A-Za-z0-9]{2,3}$";
    public static final String[] COMPLEX_FAMILY_NAME = {"欧阳", "太史", "端木", "上官", "司马", "东方", "独孤", "南宫", "万俟", "闻人", "夏侯", "诸葛", "尉迟", "公羊", "赫连", "澹台", "皇甫",
            "宗政", "濮阳", "公冶", "太叔", "申屠", "公孙", "慕容", "仲孙", "钟离", "长孙", "宇文", "城池", "司徒", "鲜于", "司空", "汝嫣", "闾丘", "子车", "亓官",
            "司寇", "巫马", "公西", "颛孙", "壤驷", "公良", "漆雕", "乐正", "宰父", "谷梁", "拓跋", "夹谷", "轩辕", "令狐", "段干", "百里", "呼延", "东郭", "南门",
            "羊舌", "微生", "公户", "公玉", "公仪", "梁丘", "公仲", "公上", "公门", "公山", "公坚", "左丘", "公伯", "西门", "公祖", "第五", "公乘", "贯丘", "公皙",
            "南荣", "东里", "东宫", "仲长", "子书", "子桑", "即墨", "达奚", "褚师"};
    public static final String FAMILY_NAME = "family_name";
    public static final String GIVEN_NAME = "given_name";
    public static final String NAME_REGEX = "^[-._@a-zA-Z0-9\u4e00-\u9fff]{1,30}$";

    /**
     * 生成长度为{@code len}同时包含大小字母和数字的随机字符串
     *
     * @param len
     * @return
     */
    public static final String genPassword(int len) {
        while (true) {
            String pwd = RandomStringUtils.randomAlphanumeric(len);
            if (pwd.matches(PASSWORD_REGEX)) {
                return pwd;
            }
        }
    }

    /**
     * @param name
     * @return
     * @description 返回的是一个Map对象map, 姓通过map.get(StringHelper.FAMILY_NAME)获得，名通过map.get(StringHelper.GIVEN_NAME)获得
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

//    public static void main(String[] args) {
//        Map<String, String> XM = parseName("欧阳下单");
//        String f = XM.get(FAMILY_NAME);
//        String g = XM.get(GIVEN_NAME);
//        System.out.println(f);
//        System.out.println(g);
//    }
}