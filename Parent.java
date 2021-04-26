import java.util.Collection;
import java.util.regex.Pattern;

import static java.lang.System.*;
public class Parent {
    public int number;
    public String parentDescription ;
    Parent(int d){
        this.number = d;
    }//构造器
}
class Son1 extends Parent{
    public String son1Description;
    Son1(String des){
        super(1001);
        this.son1Description = des;
    }
}
class Son2 extends Parent{
    public String son2Description;
    Son2(String des){
        super(1002);
        this.son2Description = des;
    }
}
//?是类型通配符，定义在引用变量上，表明可以引用任意泛型参数；T是用在类、方法、接口上，用于定义泛型
class Generic<T>{
    public T data1;
    public String genericDescription;
    public static <T> void copy(Collection<T> dest, Collection<? extends T> src){
        for (T t : src) {
            dest.add(t);
        }
    }
    Generic(String des,T data){
        this.genericDescription = des;
        this.data1 = data;
    }
    public T data2;
}
