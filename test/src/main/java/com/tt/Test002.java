package com.tt;

import java.util.Arrays;

/**
 * 快速排序
 * @author zangzh
 * @date 2019/7/18 15:25
 */
public class Test002 {
    //[6,5,8,3,7,9]
    public static void quick(int[] a, int left, int right) {
        int i,j,t,temp;
        if (left > right) {
            return ;
        }
        temp = a[left];
        i = left;
        j = right;
        while (i != j) {
            while (a[j] >= temp && i < j) {
                j--;
            }
            while (a[i] <= temp && i < j) {
                i++;
            }
            if (i < j) {
                t = a[i];
                a[i] = a[j];
                a[j] = t;
            }
        }
        //
        a[left] = a[i];
        a[i] = temp;

        quick(a,left,i-1);
        quick(a,i+1,right);
    }

    public static void main(String[] args) {
        int[] a = {6,5,8,3,7,9};
        quick(a,0,a.length-1);
        Arrays.stream(a).forEach(System.out::println);
    }
}
