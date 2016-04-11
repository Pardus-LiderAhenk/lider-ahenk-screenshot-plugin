package tr.org.liderahenk.screenshot.plugininfo;

import tr.org.liderahenk.lider.core.api.plugin.IPluginInfo;

public class PluginInfoImpl implements IPluginInfo {
	
	@Override
	public String getPluginName() {
		return "lider-screenshot";
	}

	@Override
	public String getPluginVersion() {
		return "1.0.0";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public boolean isMachineOriented() {
		return false;
	}

	@Override
	public boolean isUserOriented() {
		return false;
	}

	@Override
	public boolean isPolicyPlugin() {
		return false;
	}

	@Override
	public boolean isxBased() {
		// TODO Auto-generated method stub
		return false;
	}
	
}