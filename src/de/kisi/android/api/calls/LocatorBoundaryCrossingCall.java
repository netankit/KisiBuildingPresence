package de.kisi.android.api.calls;

import java.util.Locale;

import de.kisi.android.model.Locator;

public class LocatorBoundaryCrossingCall extends LocatableCall {

	public enum BoundaryCrossing {
		EXIT,ENTER;
		
		@Override
		public String toString(){
			return this.name().toLowerCase(Locale.ENGLISH);
		}
	}
	
	public LocatorBoundaryCrossingCall(Locator locator, BoundaryCrossing action) {
		super(String.format(Locale.ENGLISH, "places/%d/locators/%d/%s", 
				locator.getPlaceId(), 
				locator.getId(),
				action), 
			HTTPMethod.POST);
		
	}
	
}
