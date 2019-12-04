package com.tt;

/**
 * @author zangzh
 * @date 2019/7/31 10:57
 */
public class Test001 {
    public static void main(String[] args) {

    }

    public static void sort(int[] a) {
        for (int i = 0; i < a.length; i++) {
            int temp = a[i];
            int j  = i-1;
            while (j > 0 && temp < a[j]) {
                j--;
            }
        }
    }
}
