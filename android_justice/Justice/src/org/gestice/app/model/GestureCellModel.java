package org.gestice.app.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class GestureCellModel {
	
	private static final String TAG = "GestureCellModel";

	public String packageName;
	public String name;
	public String gestureName;
	
	@Override
	public String toString() {
		String result = null;
		
		// Serialize the object into JSON
		JSONObject obj = new JSONObject();
		try {
			// Set fields in the object
			obj.put("packageName", packageName);
			obj.put("name", name);
			obj.put("gestureName", gestureName);
		} catch (Exception e) {
			Log.e(TAG, "Serialization failed!");
			e.printStackTrace();
		}

		result = obj.toString();
		return result;
	}
	
	public GestureCellModel fromString(String json) {
		GestureCellModel result = new GestureCellModel();
		
		// Deserialize the object from a JSON string
		try {
			// Get an object from the string
			JSONObject obj = new JSONObject(json);
			
			// Pull fields out
			result.packageName = obj.getString("packageName");
			result.name = obj.getString("name");
			result.gestureName = obj.getString("gestureName");

		} catch (Exception e) {
			Log.e(TAG, "Deserialization failed!");
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static ArrayList<GestureCellModel> arrayFromString(String jsonArray) {
		ArrayList<GestureCellModel> result = null;
		
		try {
			JSONArray array = new JSONArray(jsonArray);
			result = new ArrayList<GestureCellModel>(array.length());
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				GestureCellModel model = new GestureCellModel();
				model.packageName = obj.getString("packageName");
				model.name = obj.getString("name");
				model.gestureName = obj.getString("gestureName");
				result.add(model);
			}
		} catch (Exception e) {
			Log.e(TAG, "Error deserializing!");
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String toJSONArrayString(ArrayList<GestureCellModel> data) {
		JSONArray array = new JSONArray();
		try {
			for (GestureCellModel model : data) {
				JSONObject obj = new JSONObject();
				obj.put("packageName", model.packageName);
				obj.put("name", model.name);
				obj.put("gestureName", model.gestureName);
				array.put(obj);
			}

		} catch (Exception e) {
			Log.e(TAG, "Serialization failed!");
			e.printStackTrace();
		}
		
		return array.toString();
	}
}
