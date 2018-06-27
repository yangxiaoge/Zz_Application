package com.seuic.gaopaiyisk.server.multiclient;

/**
 * Created by yangjianan on 2018/6/8.
 */
class CodeItem {
    public String barcode;
    public String weight;

    public CodeItem(String barcode, String weight) {
        this.barcode = barcode;
        this.weight = weight;
    }

    /*public static void main(String[] args) {
        String[] codeList1 = new String[]{"1242","12","dsag","dage"};
        String[] codeList = new String[]{"1242"};

        StringBuilder sb = new StringBuilder();
        for (String code : codeList1) {

            sb.append(code);
            sb.append("\r\n");
        }

        System.out.println(sb.toString().trim());

        String format = String.format("数量:%s", 1111);

        System.out.println(format);

        String raw = "hello";
        String str = String.format("%1$7s", raw);
        System.out.println(str);
    }*/
}
