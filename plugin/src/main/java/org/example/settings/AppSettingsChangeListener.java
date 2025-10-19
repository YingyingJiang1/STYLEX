package org.example.settings;

import com.intellij.util.messages.Topic;

/**
 * 所有配置修改的统一事件
 */
public interface AppSettingsChangeListener {

	Topic<AppSettingsChangeListener> TOPIC =
			Topic.create("AppSettingsChanged", AppSettingsChangeListener.class);

	/**
	 * 配置变更通知
	 *
	 * @param newState 最新配置状态
	 */
	void stateChanged(AppSettings.State newState);
}

