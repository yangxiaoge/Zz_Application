package com.seuic.gaopaiyisk;

/**
 * Created by yangjianan on 2018/6/8.
 */
class CodeItem {
    public String barcode;
    public String weight;

    public CodeItem(String barcode,String weight) {
        this.barcode = barcode;
        this.weight = weight;
    }

    public static void main(String[] args) {
//        String[] codeList = new String[]{"1242","12","dsag","dage"};
        String[] codeList = new String[]{"1242"};

        StringBuilder sb = new StringBuilder();
        for (String code : codeList) {

            sb.append(code);
            sb.append("\n");
        }

        System.out.println(sb.toString());
    }
}
