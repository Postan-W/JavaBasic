//函数式接口
@FunctionalInterface
public interface FunctionIterface<T> {
    T getDescription(T data);
}
//直观上看，这很像只实现一个方法的匿名内部类，这样的话，默认调用这个方法，不会有调用选择
