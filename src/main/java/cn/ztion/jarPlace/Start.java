package cn.ztion.jarPlace;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.ComponentUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.IconUtil;
import com.intellij.util.text.DateFormatUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

public class Start extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Module[] modules = ModuleManager.getInstance(project).getModules();
        String outDir = project.getBasePath() + File.separator + "jars";
        //先删除所有
        if(new File(outDir).exists()){
            for (File file : new File(outDir).listFiles()) {
                FileUtil.delete(file);
            }
        }
        StringBuilder sb = new StringBuilder();
        int count = 0, jarCount = 0;
        //遍历所有模块
        for (Module module : modules) {
            String modulePath = module.getModuleFilePath().substring(0, module.getModuleFilePath().lastIndexOf("/"));
            String pomPath = modulePath + File.separator + "pom.xml";
            //pom校验
            if (!new File(pomPath).exists()) {
                continue;
            }
            //校验模块是不是jar打包模块，直接判断是否包含spring打包插件
            try {
                String pomStr = FileUtil.loadFile(new File(pomPath));
                if (pomStr.indexOf("spring-boot-maven-plugin") < 0) {
                    continue;
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            count++;
            //判断有没有jar包
            String jarPath = modulePath + File.separator + "target" + File.separator + module.getName() + ".jar";
            File jarFile = new File(jarPath);
            if (jarFile.exists()) {
                jarCount++;
                String outJarPath = outDir + File.separator + module.getName() + ".jar";
                try {
                    FileUtil.copy(jarFile, new File(outJarPath));
                    sb.append("复制包: " + module.getName() + ".jar\n");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        try {
            //创建个空文件，文件名用当前时间
            SimpleDateFormat sdf = new SimpleDateFormat("HH时mm分ss秒");
            String fileName = sdf.format(new Date());
            String alertFilePath = outDir + File.separator + fileName;
            new File(alertFilePath).createNewFile();
            //提示弹窗
            sb.append("Jar包集合完成！总共" + count + "个模块，找到" + jarCount + "个jar包\n");
            URL icon = this.getClass().getClassLoader().getResource("icon/ok.ico");
            Icon okIcon = IconLoader.findIcon(icon);
            int choose = Messages.showOkCancelDialog(sb.toString(), "集合完成", "确认", "打开目录", okIcon);
            System.out.println("back:" + choose);
            if (choose == 2) {
                Desktop.getDesktop().open(new File(outDir));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}
