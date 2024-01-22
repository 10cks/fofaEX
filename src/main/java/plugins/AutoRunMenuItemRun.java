package plugins;

import javax.swing.*;

public class AutoRunMenuItemRun {
    // 构造函数私有化，防止外部代码创建此类的实例
    private AutoRunMenuItemRun() {}

    // 使用静态初始化器实例化Singleton，在程序加载类时就完成了实例化
    private static class SingletonHolder{
        private static final AutoRunMenuItemRun INSTANCE = new AutoRunMenuItemRun();
    }

    // 提供全局访问点
    public static AutoRunMenuItemRun getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private JMenuItem autoRunMenuItemRun;

    public void setAutoRunMenuItemRun(JMenuItem autoRunMenuItemRun) {
        this.autoRunMenuItemRun = autoRunMenuItemRun;
    }

    public JMenuItem getAutoRunMenuItemRun() {
        return autoRunMenuItemRun;
    }
}
