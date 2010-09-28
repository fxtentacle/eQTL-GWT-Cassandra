package de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller;

public class RegisterForAutomation {
	public static interface HasAutomationHandlers {
		public void onMouseOver(String fromBP, int itemIndex);

		public void onMouseOut(String fromBP, int itemIndex);

		public void onMouseClick(String fromBP, int itemIndex);
	}

	public static interface HasObjectAutomationHandlers {
		public void onMouseOver(Object object);

		public void onMouseOut(Object object);

		public void onMouseClick(Object object);
	}

	public static int regCounter = 0;

	public static String register(String baseId, HasAutomationHandlers registerMe) {
		String id = "d" + (regCounter++) + "b";
		registerInner(baseId, id, registerMe);
		return "document." + baseId + "." + id;
	}

	public static native void registerInner(String baseId, String id, HasAutomationHandlers registerMe) /*-{
		var foo = registerMe;
		$doc[baseId][id] = {
			over: function(f,i) {
		    	foo.@de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.RegisterForAutomation.HasAutomationHandlers::onMouseOver(Ljava/lang/String;I)(f,i);
			},
			out: function(f,i) {
		    	foo.@de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.RegisterForAutomation.HasAutomationHandlers::onMouseOut(Ljava/lang/String;I)(f,i);
			},
			click: function(f,i) {
		    	foo.@de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.RegisterForAutomation.HasAutomationHandlers::onMouseClick(Ljava/lang/String;I)(f,i);
			},
		};
	}-*/;

	public static native void clearBaseId(String baseId) /*-{
		$doc[baseId] = {};
	}-*/;

}
