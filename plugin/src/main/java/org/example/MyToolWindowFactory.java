package org.example;

import com.intellij.application.options.editor.fonts.AppConsoleFontConfigurable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.example.settings.AppSettingsComponent;
import org.example.settings.AppSettingsConfigurable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MyToolWindowFactory implements ToolWindowFactory {
	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		Configurable configurable = new AppSettingsConfigurable();
		configurable.createComponent(); // 初始化组件

		// 将 JPanel 放入 Content
		ContentFactory contentFactory = ContentFactory.getInstance();
		Content content = contentFactory.createContent(configurable.createComponent(), "Styler Settings", false);

		toolWindow.getContentManager().addContent(content);
	}


}
