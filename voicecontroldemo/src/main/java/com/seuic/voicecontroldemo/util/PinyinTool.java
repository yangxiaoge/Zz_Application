package com.seuic.voicecontroldemo.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Android将军
 * Desc：拼音工具类
 */
public class PinyinTool {
    public static HanyuPinyinOutputFormat format;
    public static List<Map<String, Map<String, String>>> list;
    public static Map<String, Map<String, String>> map;
    public static Map<String, String> pMap;
    public static List<String> strList;

    /**
     * 功能：根据联系人姓名生成拼音缩写与拼写全写
     *
     * @param strList 存储联系人姓名的List
     * @throws BadHanyuPinyinOutputFormatCombination
     */
    public static void setData(List<String> strList) throws BadHanyuPinyinOutputFormatCombination {
        PinyinTool.strList = strList;
        format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        list = new ArrayList<Map<String, Map<String, String>>>();
        for (int i = 0; i < strList.size(); i++) {
            map = new HashMap<String, Map<String, String>>();
            pMap = new HashMap<String, String>();
            pMap.put("simple", getSimple(strList.get(i)));
            pMap.put("complex", getComplex(strList.get(i)));
            map.put(strList.get(i), pMap);
            list.add(map);
        }


    }

    /**
     * 功能：获取字符串str的拼音缩写
     *
     * @param str
     * @return
     */
    public static String getSimple(String str) {
        StringBuilder sb = new StringBuilder();
        String tempSimple = null;
        for (int i = 0; i < str.length(); i++) {
            tempSimple = getCharacterSimple(str.charAt(i));
            if (tempSimple == null) {
                sb.append(str.charAt(i));
            } else {
                sb.append(tempSimple);
            }
        }

        return sb.toString();
    }

    /**
     * 功能：获取字符C的拼音首字母
     *
     * @param c
     * @return
     */
    public static String getCharacterSimple(char c) {
        String[] str = null;
        try {
            str = PinyinHelper.toHanyuPinyinStringArray(c, format);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        if (str == null)
            return null;
        return str[0].charAt(0) + "";
    }

    public static String getComplex(String str) {
        StringBuilder sb = new StringBuilder();
        String tempSimple = null;
        for (int i = 0; i < str.length(); i++) {
            tempSimple = getCharacterComplex(str.charAt(i));
            if (tempSimple == null) {
                sb.append(str.charAt(i));
            } else {
                sb.append(tempSimple);
            }
        }

        return sb.toString();
    }

    public static String getCharacterComplex(char c) {
        String[] str = null;
        try {
            str = PinyinHelper.toHanyuPinyinStringArray(c, format);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        if (str == null)
            return null;
        return str[0];
    }

    /**
     * 功能：搜索符合条件的联系人
     *
     * @param str 当前联系人List
     * @return 新的联系人List
     */
    public static List<String> search(String str) {
        List<String> temp = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get(strList.get(i)).get("complex").contains(str)
                    || list.get(i).get(strList.get(i)).get("simple").contains(str)) {
                System.out.println(strList.get(i));
                temp.add(strList.get(i));
            }
        }
        return temp;

    }

}
