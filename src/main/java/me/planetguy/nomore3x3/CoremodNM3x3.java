package me.planetguy.nomore3x3;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class CoremodNM3x3 implements IFMLLoadingPlugin {
	
	static{
		System.out.println(1/0);
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				"me.planetguy.nomore3x3.Transformer"
		};
	}

	@Override
	public String getModContainerClass() {
		return "me.planetguy.nomore3x3.ModNM3x3";
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
