package com.data;
/*
* 我是注释
 */
public class A {

    public G a;
    public E b;
    class E{
        void func5(){

        }
    }

    @Repository
    public int func1(int a,int b){
        if(a>b)
        {
            return a - b;
        }
        else
            return b - a;
        a = a + 100;
        b = b + 100;
        int a1;
        int b1;
        if(a1>b1)
        {
            return a1 * b1;
        }
        else
            return b1 / a1;

    }

    public int func2(int a,int b){
        try{
            a = a + 100;

        }catch (Exception e){
            b = b + 100;
        }

        try {
            if (a < b) {
                return a + b;
            } else
                return b - a;
        }catch (Exception e){

        }
    }
}

class G{
    void func6(){
        if(a>b)
        {
            return a - b;
        }
        else
            return b - a;
    }
}