import sun.font.TrueTypeFont;

public class Outer extends One {

    /*使用内部类最吸引人的原因是：每个内部类都能独立地继承一个（接口的）实现，所以无论外围类是否已经继承了某个
        （接口的）实现，对于内部类都没有影响*/
    /*内部类拥有类的基本特征。(eg：可以继承父类，实现接口。)在实际问题中我们会遇到一些接口无法解决或难以解决的问题，
    此时我们可以使用内部类继承某个具体的或抽象的类，间接解决类无法多继承引起的一系列问题。（注：内部类可以嵌套内部类，
    但是这极大的破坏了代码的结构，这里不推荐使用。）
    *
    * */
    private int outerVariable = 1;
    private int commonVariable = 2;
    public int outerID;
    /*
    类前不能有访问修饰符。
    仅在此方法内使用
    无法创造静态信息。
    可以直接访问方法内的局部变量和参数（有限制，下面详谈），但是不能更改。
    可以随意的访问外部类的任何信息。
    * */
    public void methodForLocalInnerClass(){
        Boolean tag = true;
        class InnerClassInMethod{
            private int innerVariable = 10;
            private int commonVariable = 20;
            void method(){
                System.out.println(tag);
                System.out.println(outerID);
                System.out.println(outerVariable);
                System.out.println(outerStaticVariable);
            }
        }
        InnerClassInMethod inner = new InnerClassInMethod();
        inner.method();
    }


    private static int outerStaticVariable = 3;
    Outer(int d){
        this.outerID = d;
    }
    public void outerMethod() {
        System.out.println("我是外部类的outerMethod方法");
    }
    public static void outerStaticMethod() {
        System.out.println("我是外部类的outerStaticMethod静态方法");
    }
    public void outerShow() {
        Inner inner = new Inner();
        inner.innerShow();
    }
    public Inner getInnerObject(){
        return new Inner();
    }
    //非静态内部类,这个叫成员内部类
    public class Inner<T>extends Two implements FunctionIterface<T>{

        private int commonVariable = 20;
        public void showOuterID(){
            System.out.println(outerID);
        }
        public void innerShow() {
            //当和外部类冲突时，直接引用属性名，是内部类的成员属性
            System.out.println("内部的commonVariable:" + commonVariable);
            //内部类访问外部属性
            System.out.println("outerVariable:" + outerVariable);
            //当和外部类属性名重叠时，可通过外部类名.this.属性名
            System.out.println("外部的commonVariable:" + Outer.this.commonVariable);
            System.out.println("outerStaticVariable:" + outerStaticVariable);

            outerMethod();
            outerStaticMethod();
        }
        @Override
        public T getDescription(T data) {
            return data;
        }
    }

    static {
        System.out.println("Outer的静态块被执行了……");
    }

    //静态内部类
    public static class StaticInner{
        static {
            System.out.println("Outer.Inner的静态块执行了……");
        }
        private int innerVariable = 10;
        private int commonVariable = 20;
        private static int innerStaticVariable = 30;

        public void innerShow() {
            System.out.println("innerVariable:" + innerVariable);
            System.out.println("内部的commonVariable:" + commonVariable);
            System.out.println("outerStaticVariable:"+outerStaticVariable);
            //因为是静态的，自然不能使用外部类的实例属性
            //System.out.println(outerID);
            outerStaticMethod();
        }

        public static void innerStaticShow() {
            //被调用时会先加载Outer类
            outerStaticMethod();
            System.out.println("outerStaticVariable"+outerStaticVariable);
            System.out.println("这是静态内部类的输出");
        }

        public static void callInner() {
            System.out.println(StaticInner.innerStaticVariable);
            StaticInner.innerStaticShow();
        }
    }
}
